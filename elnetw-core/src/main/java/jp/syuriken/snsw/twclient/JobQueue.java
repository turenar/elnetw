/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ジョブキューを保持するクラスです。時間のかかる作業 (HTTP通信など) をする場合は、このクラスに
 * ジョブを追加することが推奨されます。(推奨であって強制ではありません)
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class JobQueue {
	/** 優先度を格納するクラスもどき。このクラスの定数は全て{@link JobQueue}で定義されています。 */
	public /*static*/ interface Priority {
		/** 優先度 高 */
		/*public static final*/ byte HIGH = PRIORITY_HIGH;
		/** 優先度 中 */
		/*public static final*/ byte MEDIUM = PRIORITY_MEDIUM;
		/** 優先度 低 */
		/*public static final*/ byte LOW = PRIORITY_LOW;
		/** 優先度 最高 */
		/*public static final*/ byte MAX = PRIORITY_MAX;
		/** UI更新用の高め優先度 */
		/*public static final*/ byte UI = PRIORITY_UI;
		/** アイドル時に... */
		/*public static final*/ byte IDLE = PRIORITY_IDLE;
	}

	protected static class JobWorkerThread extends Thread {
		private static final Logger logger = LoggerFactory.getLogger(JobWorkerThread.class);
		private static final AtomicInteger threadNumber = new AtomicInteger();
		protected final JobQueue jobQueue;
		protected final boolean isParent;
		private final ReentrantLock lock = new ReentrantLock();

		public JobWorkerThread(JobQueue jobQueue, boolean isParent) {
			super("worker-" + threadNumber.getAndIncrement());
			this.jobQueue = jobQueue;
			this.isParent = isParent;
			setDaemon(false);
		}

		public boolean isLocked() {
			return lock.isLocked();
		}

		public void lock() {
			lock.lock();
		}

		@Override
		public void run() {
			// avoid getfield mnemonic
			JobQueue jobQueue = this.jobQueue;
			Logger logger = JobWorkerThread.logger;

			while (true) { // main loop
				lock(); // set JobWorking State
				try {
					Runnable job = jobQueue.getJob(this);
					if (job == null) {
						break;
					}
					try {
						job.run();
					} catch (RuntimeException e) {
						logger.warn("{}: uncaught runtime-exception", getName(), e);
					}
				} finally {
					unlock(); // set Idle State
				}
			} // end main loop

			jobQueue.finishWorker(this);
		}

		@Override
		public String toString() {
			return getName();
		}

		public boolean tryLock() {
			return lock.tryLock();
		}

		public void unlock() {
			lock.unlock();
		}
	}

	/**
	 * 簡易LinkedList。最後に追加と先頭から取得しかできない。
	 *
	 * @param <E> 格納する値のクラス
	 */
	protected static class LinkedQueue<E> {
		private int size;
		private Node<E> first;
		private Node<E> last;

		/**
		 * キューに追加する
		 *
		 * @param value 追加するもの。
		 */
		public synchronized void add(E value) {
			Node<E> node = new Node<>(value, null);
			if (last == null) {
				first = last = node;
			} else {
				last.next = node;
				last = node;
			}
			size++;
		}

		/**
		 * emptyかしらべる
		 *
		 * @return サイズ0かどうか
		 */
		public synchronized boolean isEmpty() {
			return size == 0;
		}

		public synchronized E poll() {
			Node<E> node = first;
			if (node != null) {
				if (node == last) {
					last = null;
				}
				first = node.next;
				size--;
				return node.item;
			} else {
				return null;
			}
		}

		public synchronized int size() {
			return size;
		}
	}

	protected static class Node<E> {
		public final E item;
		public Node<E> next;

		public Node(E element, Node<E> next) {
			this.item = element;
			this.next = next;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(JobQueue.class);
	/** 優先度 最高 */
	public static final byte PRIORITY_MAX = 15;
	/** 優先度 高 */
	public static final byte PRIORITY_HIGH = 12;
	/** UI更新用の高め優先度 */
	public static final byte PRIORITY_UI = 10;
	/** 優先度 中 */
	public static final byte PRIORITY_MEDIUM = 8;
	/** 優先度 デフォルト */
	public static final byte PRIORITY_DEFAULT = PRIORITY_MEDIUM;
	/** 優先度 低 */
	public static final byte PRIORITY_LOW = 4;
	/** アイドル時に... */
	public static final byte PRIORITY_IDLE = 0;

	/*package*/
	static int binarySearch(int[] table, int needle) {
		int start = 0;
		int end = table.length - 1;
		while (start <= end) {
			int pivot = (start + end) >>> 1;
			int v = table[pivot];
			if (v == needle) {
				return pivot;
			} else if (v > needle) {
				end = pivot - 1;
			} else {
				start = pivot + 1;
			}
		}
		return end;
	}

	@SuppressWarnings("unchecked")
	private static LinkedQueue<Runnable>[] makeLinkedQueueArray() {
		return (LinkedQueue<Runnable>[]) new LinkedQueue<?>[PRIORITY_MAX + 1];
	}

	/**
	 * ワーカースレッド数。
	 */
	protected final AtomicInteger runningWorkerThreadCount = new AtomicInteger();
	/**
	 * ジョブキュー本体。0が優先度低く、15が優先度高い
	 */
	protected final LinkedQueue<Runnable>[] queues;
	/**
	 * どのジョブを選ぶかに使用する2のべき乗テーブル。
	 */
	protected final int[] randomToPriorityTable;
	/**
	 * ジョブ数
	 */
	protected final AtomicInteger size = new AtomicInteger();
	/**
	 * どのジョブを選ぶかを決めるときにどこまで大きい乱数を作るべきか
	 */
	protected final int priorityRandomMax;
	/**
	 * ジョブワーカースレッドのリスト。get(0)==workerMainThread
	 */
	protected final ArrayList<JobWorkerThread> childThreads;
	/**
	 * ワーカーのメインスレッド (application main threadとは違う)。
	 * このインスタンスでのみParallelRunnableではないジョブが動く。
	 */
	protected final JobWorkerThread workerMainThread;
	/**
	 * ParallelRunnableではないジョブを格納するキュー。
	 */
	protected final ConcurrentLinkedQueue<Runnable> serializedJobQueue = new ConcurrentLinkedQueue<>();
	/**
	 * {@link #shutdown()}が呼ばれたらtrue。
	 */
	protected volatile boolean isShutdownPhase;
	/**
	 * core threads count
	 */
	protected int coreThreadsCount;

	/** インスタンスを生成する。 */
	public JobQueue() {
		queues = makeLinkedQueueArray();
		randomToPriorityTable = new int[PRIORITY_MAX + 2];

		for (int i = 0; i <= PRIORITY_MAX; i++) {
			queues[i] = new LinkedQueue<>();
			randomToPriorityTable[i] = (i == 0 ? 0 : 1 << i);
		}
		priorityRandomMax = 1 << (PRIORITY_MAX + 1);
		randomToPriorityTable[PRIORITY_MAX + 1] = priorityRandomMax;

		initProperties();
		childThreads = new ArrayList<>();
		workerMainThread = addWorker(0, true);
	}

	/**
	 * ジョブを追加する。
	 *
	 * <p>
	 * priorityごとにキューは独立しており、同じpriority内では追加された順番に取得されます。
	 * しかし、全体ではrandomによるpriorityによるキューの選択があるため、
	 * 高いpriorityのジョブはあとから追加されても、低いpriorityのジョブより早くキューから出る可能性が「高い」です。
	 * 逆も同様で、低いpriorityのジョブは高いpriorityのジョブより早くキューから出る可能性は「低い」です。
	 * </p>
	 *
	 * @param priority 優先度
	 * @param job      追加するジョブ
	 */
	public void addJob(byte priority, Runnable job) {
		if (priority > PRIORITY_MAX || priority < 0) {
			throw new IllegalArgumentException("priority must be 0 - 15");
		}

		if (job != null) {
			if (isShutdownPhase) {
				job.run();
			} else {
				LinkedQueue<Runnable> queue = queues[priority];
				queue.add(job);
				size.incrementAndGet();

				ensureWorkerThreads();
				synchronized (this) {
					notify();
				}
			}
		}
	}

	/**
	 * ジョブを追加する。優先度はMEDIUMで追加します。
	 *
	 * @param job ジョブ
	 */
	public void addJob(Runnable job) {
		addJob(PRIORITY_DEFAULT, job);
	}

	protected JobWorkerThread addWorker(int expectedThreadCount, boolean isParent) {
		if (runningWorkerThreadCount.compareAndSet(expectedThreadCount, expectedThreadCount + 1)) {
			synchronized (childThreads) {
				JobWorkerThread worker = new JobWorkerThread(this, isParent);
				childThreads.add(worker);
				worker.start();
				logger.debug("Worker thread {} started", worker);
				return worker;
			}
		} else {
			return null;
		}
	}

	protected void ensureWorkerThreads() {
		int threadCount = runningWorkerThreadCount.get();
		if (threadCount < coreThreadsCount) {
			addWorker(threadCount, false);
		}
	}

	protected void finishWorker(JobWorkerThread jobWorkerThread) {
		synchronized (this) {
			synchronized (childThreads) {
				childThreads.remove(jobWorkerThread);
			}
			runningWorkerThreadCount.decrementAndGet();
			notify(); // sometimes make JobQueue#shutdownNow wake up and recheck workers alive
		}
		logger.debug("{}: Finish", jobWorkerThread);
	}

	protected Runnable getJob(JobWorkerThread worker) {
		while (true) {
			Runnable job = null;
			if (worker.isParent) {
				job = serializedJobQueue.poll();
			}
			if (job == null) {
				job = getJob();
			}
			if (job == null) {
				synchronized (this) {
					try {
						logger.trace("{}: Try to wait", worker);
						wait();
						logger.trace("{}: Wake up", worker);
					} catch (InterruptedException e) {
						logger.info("{}: Interrupted", worker);
						if (isShutdownPhase) {
							return null;
						}
					}
				}
			} else {
				if (worker.isParent || job instanceof ParallelRunnable) {
					return job;
				} else {
					logger.trace("{}: Add to SerializeQueue: {}", worker, job);
					serializedJobQueue.add(job);
					if (workerMainThread.tryLock()) { // check idle state
						try {
							workerMainThread.interrupt(); // wake up worker main thread
						} finally {
							workerMainThread.unlock();
						}
					} // if main worker is working, worker will work soon.
				}
			}
		}
	}

	/**
	 * ジョブを取得する。
	 *
	 * @return ジョブ。必ずしも優先度が一番高いものというわけではなく、
	 * たまに優先度が低いものが返って来る時があります。
	 */
	public Runnable getJob() {
		if (size.get() == 0) {
			return null;
		}
		Runnable job;

		int randomInt = ThreadLocalRandom.current().nextInt(priorityRandomMax);
		int priorityNumber = binarySearch(randomToPriorityTable, randomInt);

		int i = priorityNumber;
		do {
			if (size.get() == 0) {
				return null;
			}
			job = queues[i--].poll();
			if (job != null) {
				size.decrementAndGet();
				return job;
			}
			if (i < 0) {
				i = PRIORITY_MAX;
			}
		} while (i != priorityNumber);
		return null;
	}

	protected void initProperties() {
		ClientProperties properties = ClientConfiguration.getInstance().getConfigProperties();
		coreThreadsCount = properties.getInteger("core.jobqueue.threads");
	}

	/**
	 * キューが空かどうかを調べる
	 *
	 * @return キューが空かどうか
	 */
	public boolean isEmpty() {
		return size.get() == 0;
	}

	public void setCoreThreadsCount(int coreThreadsCount) {
		this.coreThreadsCount = coreThreadsCount;
	}

	/**
	 * ジョブワーカースレッドをシャットダウンする。新しく追加されるジョブはそのままそのスレッド上で動くようになる。
	 */
	public void shutdown() {
		isShutdownPhase = true;
		synchronized (childThreads) {
			for (Thread childThread : childThreads) {
				childThread.interrupt();
			}
		}
	}

	/**
	 * 今すぐシャットダウンする。
	 *
	 * @param jobworkerJoinTimeout ジョブワーカースレッドが終了するまでの最大待機時間
	 * @return シャットダウンに成功したかどうか。
	 * @throws InterruptedException 割り込まれた。
	 */
	public boolean shutdownNow(int jobworkerJoinTimeout) throws InterruptedException {
		shutdown();
		long timeout = System.currentTimeMillis() + jobworkerJoinTimeout;
		synchronized (this) {
			while (runningWorkerThreadCount.get() != 0) {
				long remain = timeout - System.currentTimeMillis();
				if (remain <= 0) {
					return false;
				} else {
					wait(remain);
				}
			}
			return true;
		}

	}

	/**
	 * ジョブキューが保持してるジョブの数
	 *
	 * @return ジョブ数
	 */
	public int size() {
		return size.get();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("JobQueue{size=[");
		for (LinkedQueue<Runnable> queue : queues) {
			stringBuilder.append(queue.size()).append(", ");
		}
		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append("]}");
		return stringBuilder.toString();
	}
}
