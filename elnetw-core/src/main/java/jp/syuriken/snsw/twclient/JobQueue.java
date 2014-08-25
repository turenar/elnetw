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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ジョブキューを保持するクラスです。時間のかかる作業 (HTTP通信など) をする場合は、このクラスに
 * ジョブを追加することが推奨されます。(推奨であって強制ではありません)
 *
 * <p>インスタンス生成直後はワーカーは動いていません。{@link jp.syuriken.snsw.twclient.TwitterClientMain} が必要なときに
 * ワーカースレッドを開始します。</p>
 *
 * <h2>技術的情報</h2>
 * <p>このクラスは、次のフェーズを持ちます</p>
 * <p>{@link #PHASE_NEW} → {@link #PHASE_RUNNING} → {@link #PHASE_STOPPING} → {@link #PHASE_EXITED}</p>
 * <p>PHASE_NEW では、{@link #getWorkerCount()}は<strong>1を返しますがワーカーは動いていません</strong>。</p>
 * <p>
 * PHASE_RUNNING では、実際にジョブを動かします。まず、ジョブが追加されるごとにworkerCountがcoreThreadPoolSizeになるまで
 * ワーカーが追加されます。coreThreadPoolSizeになったあとは、
 * <pre>jobCount &gt;= (workerCount * execWorkerThreshold)</pre> を満たすとき、
 * maxThreadPoolSizeになるまでワーカーが追加されます。また、workerCountがcoreThreadPoolSizeよりも大きく、
 * 先述の条件を満たさないとき、最大keepAliveTime待ったあと、ワーカーが終了します。本当に無ジョブ時間が
 * keepAliveTimeになるまで待つかどうかは保証されません。なお、workerCountが0になることはありません。
 * </p>
 * <p>PHASE_STOPPING以下では、ワーカーは新規ジョブを受け付けません。すべて{@link #addJob(byte, Runnable)}の呼び出し元のスレッドで
 * そのまま実行されます。PHASE_STOPPINGではworkerCountが1以上です。PHASE_EXITEDではworkerCountが0です。</p>
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

	/**
	 * worker thread
	 */
	protected static class JobWorkerThread extends Thread {
		private static final Logger logger = LoggerFactory.getLogger(JobWorkerThread.class);
		private static final AtomicInteger threadNumber = new AtomicInteger();
		protected final JobQueue jobQueue;
		/**
		 * is main thread: {@link jp.syuriken.snsw.twclient.JobQueue#workerMainThread} == this
		 */
		protected final boolean isMainThread;
		private final ReentrantLock lock = new ReentrantLock();

		/**
		 * create instance
		 *
		 * @param jobQueue     queue
		 * @param isMainThread is worker main thread? should run just Runnable?
		 */
		public JobWorkerThread(JobQueue jobQueue, boolean isMainThread) {
			super("worker-" + threadNumber.getAndIncrement());
			this.jobQueue = jobQueue;
			this.isMainThread = isMainThread;
			setDaemon(false);
		}

		/**
		 * holds lock: avoid interruption
		 */
		public void lock() {
			lock.lock();
		}

		@Override
		public void run() {
			// avoid getfield mnemonic
			JobQueue jobQueue = this.jobQueue;
			Logger logger = JobWorkerThread.logger;
			long longAliveThreshold = jobQueue.getLongAliveThreshold();

			while (true) { // main loop
				Runnable job = jobQueue.getJob(this);
				if (job == null) {
					break;
				}
				lock(); // set JobWorking State
				try {
					long startTime = System.currentTimeMillis();
					job.run();
					long tookTime = System.currentTimeMillis() - startTime;
					if (tookTime > longAliveThreshold) {
						logger.info("{}: took long time. {}ms", job, tookTime);
					}
				} catch (RuntimeException e) {
					logger.warn("{}: uncaught runtime-exception", getName(), e);
				} finally {
					unlock(); // set Idle State
				}
			} // end main loop

			// finishWorker is called by getJob
		}

		@Override
		public String toString() {
			return getName();
		}

		/**
		 * try to hold lock: check interrupt-able
		 *
		 * @return can hold lock
		 */
		public boolean tryLock() {
			return lock.tryLock();
		}

		/**
		 * release lock: set interrupt-able
		 */
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

		/**
		 * get job
		 *
		 * @return job
		 */
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

		/**
		 * return size
		 *
		 * @return size
		 */
		public synchronized int size() {
			return size;
		}
	}

	/**
	 * one-directional node class
	 *
	 * @param <E> type
	 */
	protected static class Node<E> {
		/**
		 * item
		 */
		public final E item;
		/**
		 * next
		 */
		public Node<E> next;

		/**
		 * create instance
		 *
		 * @param element item
		 * @param next    next
		 */
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
	/** phaseをstateに格納するためのビットオフセット */
	private static final int PHASE_BIT_OFFSET = Integer.SIZE - 3;
	/** worker count最大値 */
	protected static final int WORKER_CAPACITY = (1 << PHASE_BIT_OFFSET) - 1;
	/** インスタンスが作成された直後でまだワーカーは動いていない。 initProperties()必要 */
	public static final int PHASE_NEW = 0 << PHASE_BIT_OFFSET;
	/** ワーカーが動いている */
	public static final int PHASE_RUNNING = 1 << PHASE_BIT_OFFSET;
	/** #shutdown() が呼ばれた */
	public static final int PHASE_STOPPING = 2 << PHASE_BIT_OFFSET;
	/** ワーカーが全て停止した */
	public static final int PHASE_EXITED = 3 << PHASE_BIT_OFFSET;

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

	/**
	 * ワーカー数を取得する
	 *
	 * @param state ステータス値
	 * @return ワーカー数
	 */
	protected static int countOf(int state) {
		return state & WORKER_CAPACITY;
	}

	@SuppressWarnings("unchecked")
	private static LinkedQueue<Runnable>[] makeLinkedQueueArray() {
		return (LinkedQueue<Runnable>[]) new LinkedQueue<?>[PRIORITY_MAX + 1];
	}

	/**
	 * フェーズを取得する
	 *
	 * @param state ステータス値
	 * @return フェーズ値
	 */
	protected static int phaseOf(int state) {
		return state & ~WORKER_CAPACITY;
	}

	/**
	 * 与えられたフェーズとワーカー数からステータス値を作成する
	 *
	 * @param phase       フェーズ
	 * @param workerCount ワーカー数
	 * @return ステータス値
	 */
	protected static int stateOf(int phase, int workerCount) {
		return phase | workerCount;
	}

	/**
	 * ワーカースレッド数。
	 */
	protected final AtomicInteger state = new AtomicInteger(stateOf(PHASE_NEW, 0));
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
	 * lock object for property
	 */
	protected final Object propertyLock = new Object();
	/**
	 * core threads pool size
	 */
	protected volatile int coreThreadPoolSize;
	/**
	 * maximum threads pool size
	 */
	protected volatile int maximumThreadPoolSize;
	/**
	 * threshold for adding worker
	 */
	protected volatile int execWorkerThreshold;
	/**
	 * worker keep alive time
	 */
	protected volatile long keepAliveTime;
	/**
	 * time for detect job as long running
	 */
	protected volatile long longAliveThreshold;

	/** インスタンスを生成する。 */
	public JobQueue() {
		queues = makeLinkedQueueArray();
		randomToPriorityTable = new int[PRIORITY_MAX + 2];

		for (int i = 0; i <= PRIORITY_MAX; i++) {
			queues[i] = new LinkedQueue<>();
			randomToPriorityTable[i] = i == 0 ? 0 : 1 << i;
		}
		priorityRandomMax = 1 << (PRIORITY_MAX + 1);
		randomToPriorityTable[PRIORITY_MAX + 1] = priorityRandomMax;

		childThreads = new ArrayList<>();
		workerMainThread = addWorker(0, true);
	}

	/**
	 * ジョブを追加する。
	 *
	 * <p>
	 * priorityごとにキューは独立しており、{@link #getPhase()}が{@link #PHASE_NEW}または{@link #PHASE_RUNNING}の場合、
	 * 同じpriority内では追加された順番に取得/実行されます。
	 * しかし、全体ではrandomによるpriorityによるキューの選択があるため、
	 * 高いpriorityのジョブはあとから追加されても、低いpriorityのジョブより早くキューから出る可能性が「高い」です。
	 * 逆も同様で、低いpriorityのジョブは高いpriorityのジョブより早くキューから出る可能性は「低い」です。
	 * </p>
	 * <p>
	 * なお、{@link #getPhase()}が{@link #PHASE_STOPPING}または{@link #PHASE_EXITED}の場合、
	 * 呼び出された時点でjob引数が実行されます。すでにキューに追加されているジョブの順番は全く考慮しません。
	 * </p>
	 *
	 * @param priority 優先度
	 * @param job      追加するジョブ
	 */
	@SuppressFBWarnings("NN_NAKED_NOTIFY")
	public void addJob(byte priority, Runnable job) {
		if (priority > PRIORITY_MAX || priority < 0) {
			throw new IllegalArgumentException("priority must be 0 - 15");
		}

		if (job != null) {
			if (phaseOf(state.get()) >= PHASE_STOPPING) {
				logger.warn("Job is registered into outdated jobqueue: {}", job);
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

	/**
	 * ワーカーを追加する。そして、フェーズが{@link #PHASE_NEW}でなければスレッドを起動する。
	 *
	 * @param state    ステータス値。現在のステータス値が一致しなければ追加しない。
	 * @param isParent メインスレッドかどうか
	 * @return ワーカーを追加した場合そのワーカー。追加しなかった場合null。
	 */
	protected JobWorkerThread addWorker(int state, boolean isParent) {
		if (this.state.compareAndSet(state, state + 1)) {
			synchronized (childThreads) {
				JobWorkerThread worker = new JobWorkerThread(this, isParent);
				childThreads.add(worker);
				if (phaseOf(state) != PHASE_NEW) {
					worker.start();
					logger.debug("Worker thread {} started", worker);
				}
				return worker;
			}
		} else {
			return null;
		}
	}

	/**
	 * check if should start another worker thread
	 */
	protected void ensureWorkerThreads() {
		int state = this.state.get();
		if (phaseOf(state) < PHASE_RUNNING) {
			return;
		}

		int threadCount = countOf(state);
		if (threadCount < coreThreadPoolSize
				|| (threadCount < maximumThreadPoolSize && size.get() >= threadCount * execWorkerThreshold)) {
			addWorker(state, false);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		shutdown();
	}

	/**
	 * exit worker
	 *
	 * @param jobWorkerThread worker thread
	 * @param state           expected state
	 * @return if worker termination logic is succeeded
	 */
	protected boolean finishWorker(JobWorkerThread jobWorkerThread, int state) {
		synchronized (this) {
			if (!this.state.compareAndSet(state, state - 1)) {
				return false;
			}
			synchronized (childThreads) {
				childThreads.remove(jobWorkerThread);
			}

			notify(); // sometimes make JobQueue#shutdownNow wake up and recheck workers alive
			if (countOf(state - 1) == 0) {
				this.state.compareAndSet(state - 1, stateOf(PHASE_EXITED, 0));
			}
		}
		logger.debug("{}: Finish", jobWorkerThread);
		return true;
	}

	/**
	 * get core thread pool size
	 *
	 * @return core thread pool size
	 */
	public int getCoreThreadPoolSize() {
		return coreThreadPoolSize;
	}

	/**
	 * ワーカーを追加するためのしきい値を取得する。
	 * 詳しくはクラスのJavadocを見てください。
	 *
	 * @return しきい値
	 */
	public int getExecWorkerThreshold() {
		return execWorkerThreshold;
	}

	/**
	 * get a job for WorkerThread.
	 *
	 * @param worker WorkerThread
	 * @return job. if {@link #isShutdownPhase()} is true, return null.
	 * if wait(keepAliveTime) timed out and working thread count is larger than coreThreadPoolSize, return null.
	 */
	protected Runnable getJob(JobWorkerThread worker) {
		boolean wakeUpFlag = false;
		while (true) {
			Runnable job = null;
			if (worker.isMainThread) {
				job = serializedJobQueue.poll();
			}
			if (job == null) {
				job = getJob();
			}
			if (job == null) {
				try {
					int tempState = state.get();
					if (isShutdownPhase()
							|| (wakeUpFlag && !worker.isMainThread && countOf(tempState) > coreThreadPoolSize)) {
						// exit free worker
						logger.debug("{}: No job! try to exit...", worker);
						if (finishWorker(worker, tempState)) {
							return null;
						} else {
							continue;
						}
					}
					logger.trace("{}: Try to wait", worker);
					synchronized (this) {
						wait(keepAliveTime);
					}
					logger.trace("{}: Wake up", worker);
					wakeUpFlag = true;
				} catch (InterruptedException e) {
					logger.info("{}: Interrupted", worker);
					if (isShutdownPhase()) {
						while (!finishWorker(worker, state.get())) {
							// continue
						}
						return null;
					} else {
						wakeUpFlag = false; // clear wake up flag
					}
				}
			} else {
				wakeUpFlag = false; // clear wake up flag if job is found
				if (worker.isMainThread || job instanceof ParallelRunnable) {
					//  main worker:  run <extends Runnable>
					// other workers: run <extends ParallelRunnable>
					return job;
				} else {
					// other workers don't run just Runnable
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

	/**
	 * get worker keep alive time
	 *
	 * @return worker keep alive time
	 */
	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	/**
	 * get time threshold for detecting long running job
	 *
	 * @return time threshold for detecting long running job
	 */
	public long getLongAliveThreshold() {
		return longAliveThreshold;
	}

	/**
	 * get maximum thread pool size
	 *
	 * @return maximum thread pool size
	 */
	public int getMaximumThreadPoolSize() {
		return maximumThreadPoolSize;
	}

	/**
	 * get phase.
	 *
	 * @return phase (MAGIC CONST)
	 * @see #PHASE_NEW
	 * @see #PHASE_RUNNING
	 * @see #PHASE_STOPPING
	 * @see #PHASE_EXITED
	 */
	public int getPhase() {
		return phaseOf(state.get());
	}

	/**
	 * get worker count. count is not count of running worker but count of initialized worker.
	 * if {@link #getPhase()} returns {@link #PHASE_NEW}, no worker may be running.
	 *
	 * @return initialized worker count
	 */
	public int getWorkerCount() {
		return countOf(state.get());
	}

	/**
	 * プロパティを初期化する。
	 */
	protected void initProperties() {
		ClientProperties properties = ClientConfiguration.getInstance().getConfigProperties();
		coreThreadPoolSize = properties.getInteger("core.worker.core_threads");
		maximumThreadPoolSize = properties.getInteger("core.worker.max_threads");
		execWorkerThreshold = properties.getInteger("core.worker.exec_threshold");
		keepAliveTime = properties.getTime("core.worker.keep_alive");
		longAliveThreshold = properties.getTime("core.worker.long_alive_threshold");
	}

	/**
	 * キューが空かどうかを調べる
	 *
	 * @return キューが空かどうか
	 */
	public boolean isEmpty() {
		return size.get() == 0;
	}

	private boolean isShutdownPhase() {
		return phaseOf(state.get()) >= PHASE_STOPPING;
	}

	private String phaseOfString(int state) {
		int phase = phaseOf(state);
		switch (phase) {
			case PHASE_NEW:
				return "NEW";
			case PHASE_RUNNING:
				return "RUNNING";
			case PHASE_STOPPING:
				return "STOPPING";
			case PHASE_EXITED:
				return "EXITED";
			default:
				return String.valueOf(phase >>> PHASE_BIT_OFFSET);
		}
	}

	/**
	 * set core thread pool size. if coreThreadPoolSize&lt;=maximumThreadPoolSize, set large maximumThreadPoolSize
	 *
	 * @param poolSize new size
	 */
	public void setCoreThreadPoolSize(int poolSize) {
		if (poolSize <= 0) {
			throw new IllegalArgumentException("core threads pool size must be larger than zero.");
		}
		synchronized (propertyLock) {
			this.coreThreadPoolSize = poolSize;
			if (maximumThreadPoolSize < poolSize) {
				maximumThreadPoolSize = poolSize;
			}
		}
	}

	/**
	 * set execWorkerThreshold
	 *
	 * @param execWorkerThreshold threshold per thread
	 */
	public void setExecWorkerThreshold(int execWorkerThreshold) {
		if (execWorkerThreshold <= 0) {
			throw new IllegalArgumentException("maximum threads pool size must be larger than zero.");
		}
		synchronized (propertyLock) {
			this.execWorkerThreshold = execWorkerThreshold;
		}
	}

	/**
	 * set keep alive time
	 *
	 * @param keepAliveTime worker thread keep-alive time.
	 *                      if 0, we try to keep worker alive
	 *                      until invoked {@link #shutdown()} or {@link #shutdownNow(int)}
	 */
	public void setKeepAliveTime(long keepAliveTime) {
		if (keepAliveTime < 0) {
			throw new IllegalArgumentException("keep alive time must not be minus number");
		}
		synchronized (propertyLock) {
			this.keepAliveTime = keepAliveTime;
		}
	}

	/**
	 * set longAliveThreshold
	 *
	 * @param longAliveThreshold threshold (msec.)
	 */
	public void setLongAliveThreshold(long longAliveThreshold) {
		if (longAliveThreshold <= 0) {
			throw new IllegalArgumentException("longAliveThreshold must be larger than zero");
		}
		synchronized (propertyLock) {
			this.longAliveThreshold = longAliveThreshold;
		}
	}

	/**
	 * set maximum thread pool size
	 *
	 * @param poolSize new size
	 */
	public void setMaximumThreadPoolSize(int poolSize) {
		synchronized (propertyLock) {
			if (poolSize <= 0) {
				throw new IllegalArgumentException("maximum threads pool size must be larger than zero.");
			} else if (coreThreadPoolSize > poolSize) {
				throw new IllegalArgumentException("maximum threads pool size must be larger than core threads pool size");
			}
			this.maximumThreadPoolSize = poolSize;
		}
	}

	/**
	 * フェーズを設定する。
	 *
	 * @param phase フェーズ。
	 */
	protected final void setPhase(int phase) {
		int state;
		int prevState;
		do {
			prevState = this.state.get();
			state = stateOf(phase, countOf(prevState));
		} while (!this.state.compareAndSet(prevState, state));
	}

	/**
	 * ジョブワーカースレッドをシャットダウンする。新しく追加されるジョブはそのままそのスレッド上で動くようになる。
	 */
	public void shutdown() {
		int state = this.state.get();
		if (phaseOf(state) < PHASE_STOPPING) {
			setPhase(PHASE_STOPPING);
		}
		if (phaseOf(state) == PHASE_NEW) {
			logger.warn("JobQueue which initialized but not started is requested to exit. Queue has {} job(s)."
					+ " Running remain jobs...", size);
			Runnable job;
			while ((job = getJob()) != null) {
				try {
					job.run();
				} catch (RuntimeException e) {
					logger.warn("uncaught exception", e);
				}
			}
		}
		synchronized (childThreads) {
			for (JobWorkerThread childThread : childThreads) {
				if (childThread.tryLock()) {
					try {
						childThread.interrupt();
					} finally {
						childThread.unlock();
					}
				}
			}
		}
	}

	/**
	 * 今すぐシャットダウンする。
	 *
	 * @param joinTimeout ジョブワーカースレッドが終了するまでの最大待機時間
	 * @return シャットダウンに成功したかどうか。
	 * @throws InterruptedException 割り込まれた。
	 */
	public boolean shutdownNow(int joinTimeout) throws InterruptedException {
		shutdown();
		long timeout = System.currentTimeMillis() + joinTimeout;
		synchronized (this) {
			while (countOf(state.get()) != 0) {
				long remain = timeout - System.currentTimeMillis();
				if (remain <= 0) {
					return false;
				} else {
					wait(remain);
				}
			}
			setPhase(PHASE_EXITED);
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

	/**
	 * ワーカースレッドを開始する。
	 */
	public void startWorker() {
		if (phaseOf(state.get()) == PHASE_NEW) {
			initProperties();
			setPhase(PHASE_RUNNING);
			workerMainThread.start();
		}
	}

	@Override
	public String toString() {
		int state = this.state.get();

		StringBuilder stringBuilder = new StringBuilder("JobQueue{state=").append(phaseOfString(state))
				.append(",worker=").append(countOf(state))
				.append(",pool=").append(coreThreadPoolSize).append('-').append(maximumThreadPoolSize)
				.append(",keepAlive=").append(keepAliveTime).append(",lAT=").append(longAliveThreshold)
				.append(",size=[");
		for (LinkedQueue<Runnable> queue : queues) {
			stringBuilder.append(queue.size()).append(',');
		}
		stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length()).append("]}");
		return stringBuilder.toString();
	}
}
