package jp.syuriken.snsw.twclient;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ジョブキューを保持するクラスです。時間のかかる作業 (HTTP通信など) をする場合は、このクラスに
 * ジョブを追加することが推奨されます。(推奨であって強制ではありません)
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class JobQueue {

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

	/**
	 * 優先度を格納する
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public static enum Priority {
		/** 優先度：高 */
		HIGH,
		/** 優先度：中 */
		MEDIUM,
		/** 優先度：低 */
		LOW;
		public static final int PRIORITY_COUNT = 3;

		public int getPriorityValue() {
			switch (this) {
				case HIGH:
					return 0;
				case MEDIUM:
					return 1;
				case LOW:
					return 2;
				default:
					throw new AssertionError();
			}
		}
	}

	private static final int HIGH_THRESHOLD = 100;
	private static final int MEDIUM_THRESHOLD = 10;
	private static final int LOW_THRESHOLD = 1;
	private final LinkedQueue<Runnable>[] queues;
	private final AtomicInteger size = new AtomicInteger();
	private final Random random;
	private Object jobWorkerThreadHolder = null;


	/** インスタンスを生成する。 */
	public JobQueue() {
		queues = makeLinkedQueueArray();
		for (int i = 0; i < Priority.PRIORITY_COUNT; i++) {
			queues[i] = new LinkedQueue<>();
		}
		random = new Random();
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
	public void addJob(Priority priority, Runnable job) {
		if (job != null) {
			if (jobWorkerThreadHolder == null) {
				job.run();
			} else {

				LinkedQueue<Runnable> queue = queues[priority.getPriorityValue()];
				queue.add(job);
				size.incrementAndGet();
			}
			if (jobWorkerThreadHolder != null) {
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
		addJob(Priority.MEDIUM, job);
	}

	/**
	 * ジョブを取得する。
	 *
	 * @return ジョブ。必ずしも優先度が一番高いものというわけではなく、
	 *         たまに優先度が低いものが返って来る時があります。
	 */
	public Runnable getJob() {
		if (size.get() == 0) {
			return null;
		}
		Runnable job;

		// 1: 乱数ベースのジョブ選択
		int randomInt = random.nextInt(HIGH_THRESHOLD);
		int priorityNumber;
		if (randomInt < LOW_THRESHOLD) {
			priorityNumber = 2;
		} else if (randomInt < MEDIUM_THRESHOLD) {
			priorityNumber = 1;
		} else {
			priorityNumber = 0;
		}
		LinkedQueue<Runnable> queue = queues[priorityNumber];
		job = queue.poll();
		if (job != null) {
			size.decrementAndGet();
			return job;
		}

		// 2: 総当り式
		final int max = priorityNumber + Priority.PRIORITY_COUNT;
		for (int i = priorityNumber + 1; i < max; i++) {
			if (size.get() == 0) {
				return null;
			}
			job = queues[i % Priority.PRIORITY_COUNT].poll();
			if (job != null) {
				size.decrementAndGet();
				return job;
			}
		}
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

	@SuppressWarnings("unchecked")
	private LinkedQueue<Runnable>[] makeLinkedQueueArray() {
		return new LinkedQueue[Priority.PRIORITY_COUNT];
	}

	/**
	 * ジョブワーカースレッドを設定する。
	 * <p>
	 * ジョブワーカースレッドは、ジョブキューにジョブが追加されたときに {@link Thread#notifyAll()}される
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
