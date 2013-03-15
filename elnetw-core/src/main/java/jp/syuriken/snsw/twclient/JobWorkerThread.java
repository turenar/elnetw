package jp.syuriken.snsw.twclient;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ジョブワーカースレッド。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
/*package*/class JobWorkerThread extends Thread {

	protected volatile boolean isCanceled = false;

	private final Object threadHolder;

	private final JobQueue jobQueue;

	private ArrayList<JobWorkerThread> childThreads;

	private final ConcurrentLinkedQueue<Runnable> serializeQueue;

	private static final AtomicInteger threadNumber = new AtomicInteger();

	private final int threadsCount;

	private final boolean isParent;

	private final JobWorkerThread parent;

	private static final Logger logger = LoggerFactory.getLogger(JobWorkerThread.class);


	public JobWorkerThread(JobQueue jobQueue, ClientConfiguration configuration) {
		this(jobQueue, null, new Object(), configuration);
	}

	private JobWorkerThread(JobQueue jobQueue, JobWorkerThread parent, Object threadHolder,
			ClientConfiguration configuration) {
		super("jobworker-" + threadNumber.getAndIncrement());
		setDaemon(true);
		this.parent = parent;
		this.threadHolder = threadHolder;
		this.jobQueue = jobQueue;
		isParent = parent == null;

		if (isParent) {
			ClientProperties properties = configuration.getConfigProperties();
			threadsCount = properties.getInteger("core.jobqueue.threads");
			childThreads = new ArrayList<JobWorkerThread>();
			serializeQueue = new ConcurrentLinkedQueue<Runnable>();
		} else {
			threadsCount = parent.threadsCount;
			serializeQueue = parent.serializeQueue;
		}

	}

	public void cleanUp() {
		jobQueue.setJobWorkerThread(null);
		isCanceled = true;
	}

	private void onExitChildThread(JobWorkerThread jobWorkerThread) {
		synchronized (childThreads) {
			childThreads.remove(jobWorkerThread);
		}
	}

	@Override
	public void run() {
		if (isParent) {
			jobQueue.setJobWorkerThread(threadHolder);

			// 設定された数だけワーカーを追加
			for (int i = 1; i < threadsCount; i++) {
				JobWorkerThread workerThread = new JobWorkerThread(jobQueue, this, threadHolder, null);
				synchronized (childThreads) {
					childThreads.add(workerThread);
				}
				workerThread.start();
			}
		}

		while (isCanceled == false) {
			Runnable job = null;
			if (isParent) { // check serializeQueue
				job = serializeQueue.poll();
			}
			if (job == null) { // check jobQueue
				job = jobQueue.getJob();
			}
			if (job == null) { // no job: wait
				synchronized (threadHolder) {
					try {
						if (isCanceled == false) {
							threadHolder.wait();
						}
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			} else {
				// 親であるか、子でParallelRunnableの場合に起動
				if (isParent || job instanceof ParallelRunnable) {
					try {
						job.run();
					} catch (RuntimeException e) {
						logger.warn("uncaught runtime-exception", e);
					}
				} else { // ただのRunnableは親で動かす
					serializeQueue.add(job);
				}
			}
		}
		Runnable job;
		if (isParent) { // 親の場合は終了時にできるだけジョブを消費する。
			while ((job = jobQueue.getJob()) != null) {
				try {
					job.run();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		} else {
			parent.onExitChildThread(this);
		}
		logger.debug(getName() + " exiting");
	}
}
