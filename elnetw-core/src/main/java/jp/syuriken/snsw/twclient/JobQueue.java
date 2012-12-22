package jp.syuriken.snsw.twclient;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * ジョブキューを保持するクラスです。時間のかかる作業 (HTTP通信など) をする場合は、このクラスに
 * ジョブを追加することが推奨されます。(推奨であって強制ではありません)
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class JobQueue {

	/**
	 * 優先度を格納する
	 *
	 * @author Turenar <snswinhaiku dot lo at gmail dot com>
	 */
	public enum Priority {
		/** 優先度：高 */
		HIGH,
		/** 優先度：中 */
		MEDIUM,
		/** 優先度：低 */
		LOW;
	}


	private static final int HIGH_THRESHOLD = 100;

	private static final int MEDIUM_THRESHOLD = 10;

	private static final int LOW_THRESHOLD = 1;

	private int mediumStart = 0;

	private int lowStart = 0;

	private LinkedList<Runnable> jobList;

	private Random random;

	private Object jobWorkerThreadHolder = null;


	/**
	 * インスタンスを生成する。
	 *
	 */
	public JobQueue() {
		jobList = new LinkedList<Runnable>();
		random = new Random();
	}

	/**
	 * ジョブを追加する
	 *
	 * @param priority 優先度
	 * @param job 追加するジョブ
	 */
	public void addJob(Priority priority, Runnable job) {
		if (job != null) {
			if (jobWorkerThreadHolder == null) {
				job.run();
			} else {
				synchronized (jobList) {
					switch (priority) {
						case HIGH:
							jobList.add(mediumStart, job);
							mediumStart++;
							lowStart++;
							break;
						case MEDIUM:
							jobList.add(lowStart, job);
							lowStart++;
							break;
						case LOW:
						default:
							jobList.add(job);
							break;
					}
				}
			}
			if (jobWorkerThreadHolder != null) {
				synchronized (jobWorkerThreadHolder) {
					jobWorkerThreadHolder.notifyAll();
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
	 * @return
	 * 	ジョブ。必ずしも優先度が一番高いものというわけではなく、
	 * 	たまに優先度が低いものが返って来る時があります。
	 */
	public Runnable getJob() {
		if (jobList.isEmpty()) {
			return null;
		}
		int randomInt = random.nextInt(HIGH_THRESHOLD);
		try {
			synchronized (jobList) {
				int size = jobList.size();
				if (randomInt < LOW_THRESHOLD && size > lowStart) {
					return jobList.remove(lowStart);
				} else if (randomInt < MEDIUM_THRESHOLD && size > mediumStart) {
					lowStart--;
					if (lowStart < 0) {
						lowStart = 0;
					}
					return jobList.remove(mediumStart);
				} else {
					mediumStart--;
					if (mediumStart < 0) {
						mediumStart = 0;
					}
					lowStart--;
					if (lowStart < 0) {
						lowStart = 0;
					}
					return jobList.removeFirst();
				}
			}
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * キューが空かどうかを調べる
	 *
	 * @return キューが空かどうか
	 */
	public boolean isEmpty() {
		synchronized (jobList) {
			return jobList.isEmpty();
		}
	}

	/**
	 * ジョブワーカースレッドを設定する。
	 *
	 * <p>
	 * ジョブワーカースレッドは、ジョブキューにジョブが追加されたときに {@link Thread#notifyAll()}される
	 * スレッドです。このスレッドでは、ジョブキューの消費が仕事となります。
	 * </p>
	 *
	 * <p>
	 * <strong>パラメータにnullを指定して呼び出す</strong>と、ジョブワーカースレッドの仕事が解除され、
	 * ジョブが追加されると同時にJobQueue内部で {@link Runnable#run()}が呼び出されるようになります。
	 * </p>
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
		synchronized (jobList) {
			return jobList.size();
		}
	}
}