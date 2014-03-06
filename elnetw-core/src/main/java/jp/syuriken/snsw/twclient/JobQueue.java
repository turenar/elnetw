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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

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
		return new LinkedQueue[PRIORITY_MAX + 1];
	}

	private final LinkedQueue<Runnable>[] queues;
	private final int[] randomToPriorityTable;
	private final AtomicInteger size = new AtomicInteger();
	private final int priorityRandomMax;
	private Object jobWorkerThreadHolder = null;

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
			if (jobWorkerThreadHolder == null) {
				job.run();
			} else {
				LinkedQueue<Runnable> queue = queues[priority];
				queue.add(job);
				size.incrementAndGet();
				synchronized (jobWorkerThreadHolder) {
					jobWorkerThreadHolder.notify();
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
	 * キューが空かどうかを調べる
	 *
	 * @return キューが空かどうか
	 */
	public boolean isEmpty() {
		return size.get() == 0;
	}

	/**
	 * ジョブワーカースレッドを設定する。
	 * <p>
	 * ジョブワーカースレッドは、ジョブキューにジョブが追加されたときに {@link Thread#notify()}される
	 * スレッドです。このスレッドでは、ジョブキューの消費が仕事となります。
	 * </p>
	 * <p>
	 * <strong>パラメータにnullを指定して呼び出す</strong>と、ジョブワーカースレッドの仕事が解除され、
	 * ジョブが追加されると同時にJobQueue内部で {@link Runnable#run()}が呼び出されるようになります。
	 * </p>
	 *
	 * @param threadHolder スレッドホルダ
	 */
	public void setJobWorkerThread(Object threadHolder) {
		jobWorkerThreadHolder = threadHolder;
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
