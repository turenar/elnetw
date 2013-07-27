package jp.syuriken.snsw.twclient;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ジョブワーカースレッド。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/class JobWorkerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(JobWorkerThread.class);

	private static final AtomicInteger threadNumber = new AtomicInteger();

	private final Object threadHolder;

	private final JobQueue jobQueue;

	private final ConcurrentLinkedQueue<Runnable> serializeQueue;

	private final AtomicInteger runningChildThreadCount;

	private final boolean isParent;

	private final JobWorkerThread parent;

	private ArrayList<JobWorkerThread> childThreads;

	public JobWorkerThread(JobQueue jobQueue) {
		this(jobQueue, null, new Object());
	}

	private JobWorkerThread(JobQueue jobQueue, JobWorkerThread parent, Object threadHolder) {
		super("jobworker-" + threadNumber.getAndIncrement());
		setDaemon(false);
		this.parent = parent;
		this.threadHolder = threadHolder;
		this.jobQueue = jobQueue;
		isParent = parent == null;

		if (isParent) {
			ClientProperties properties = ClientConfiguration.getInstance().getConfigProperties();
			int threadsCount = properties.getInteger("core.jobqueue.threads");
			childThreads = new ArrayList<>();
			serializeQueue = new ConcurrentLinkedQueue<>();
			runningChildThreadCount = new AtomicInteger();
			for (int i = 1; i < threadsCount; i++) {
				JobWorkerThread workerThread = new JobWorkerThread(jobQueue, this, threadHolder);
				childThreads.add(workerThread);
			}
		} else {
			serializeQueue = parent.serializeQueue;
			runningChildThreadCount = parent.runningChildThreadCount;
		}
	}

	public void cleanUp() {
		jobQueue.setJobWorkerThread(null);
		interrupt();
		for (JobWorkerThread workerThread : childThreads) {
			workerThread.interrupt();
		}
	}

	@Override
	public void run() {
		// avoid getfield mnemonic
		JobQueue jobQueue = this.jobQueue;
		ConcurrentLinkedQueue<Runnable> serializeQueue = this.serializeQueue;

		if (isParent) {
			jobQueue.setJobWorkerThread(threadHolder);
			// 子ワーカーの開始
			for (JobWorkerThread workerThread : childThreads) {
				workerThread.start();
			}
		} else {
			runningChildThreadCount.incrementAndGet();
		}

		while (true) { // main loop
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
						logger.trace("{}: Try to wait", getName());
						threadHolder.wait();
						logger.trace("{}: Wake up", getName());
					} catch (InterruptedException e) {
						logger.trace("{}: Interrupted", getName());
						break;
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
					logger.trace("{}: Add to SerializeQueue: {}", getName(), job);
					serializeQueue.add(job);
				}
			}
		} // end main loop

		if (isParent) {
			// 子ワーカーの終了確認
			while (runningChildThreadCount.get() != 0) {
				synchronized (threadHolder) {
					try {
						logger.trace("{}: Wait for child destroyed", getName());
						threadHolder.wait();
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
			// serializeQueueの消費
			while (!serializeQueue.isEmpty()) {
				Runnable job = serializeQueue.poll();
				job.run();
			}
		} else {
			runningChildThreadCount.decrementAndGet();
			parent.interrupt();
		}
		logger.debug("{}: Exiting", getName());
	}
}
