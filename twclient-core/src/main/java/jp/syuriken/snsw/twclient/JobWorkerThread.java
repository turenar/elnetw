package jp.syuriken.snsw.twclient;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jp.syuriken.snsw.twclient.JobQueue.PararellRunnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ジョブワーカースレッド。
 * 
 * @author $Author$
 */
/*package*/class JobWorkerThread extends Thread {
	
	protected volatile boolean isCanceled = false;
	
	private final Object threadHolder;
	
	private final JobQueue jobQueue;
	
	private ArrayList<JobWorkerThread> childThreads;
	
	private final ConcurrentLinkedQueue<Runnable> serializeQueue;
	
	private static final AtomicInteger threadNumber = new AtomicInteger();
	
	private static final int JOB_PER_A_WORKER = 5;
	
	private final boolean isParent;
	
	private final JobWorkerThread parent;
	
	private Logger logger = LoggerFactory.getLogger(JobWorkerThread.class);
	
	
	public JobWorkerThread(JobQueue jobQueue) {
		this(jobQueue, null, new Object());
	}
	
	private JobWorkerThread(JobQueue jobQueue, JobWorkerThread parent, Object threadHolder) {
		super("jobworker-" + threadNumber.getAndIncrement());
		setDaemon(true);
		this.parent = parent;
		this.threadHolder = threadHolder;
		this.jobQueue = jobQueue;
		isParent = parent == null;
		
		if (isParent) {
			childThreads = new ArrayList<JobWorkerThread>();
			serializeQueue = new ConcurrentLinkedQueue<Runnable>();
		} else {
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
		}
		
		while (isCanceled == false) {
			int queueSize = jobQueue.size();
			if (isParent && queueSize > childThreads.size() * JOB_PER_A_WORKER) {
				JobWorkerThread workerThread = new JobWorkerThread(jobQueue, this, threadHolder);
				synchronized (childThreads) {
					childThreads.add(workerThread);
				}
				workerThread.start();
			} else if (isParent == false && queueSize == 0) {
				return; // if i am child thread and what have to do is nothing, exit thread.
			}
			Runnable job = null;
			if (isParent) {
				job = serializeQueue.poll();
			}
			if (job == null) {
				job = jobQueue.getJob();
			}
			if (job == null) {
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
				if (isParent || job instanceof PararellRunnable) {
					try {
						job.run();
					} catch (RuntimeException e) {
						logger.warn("uncaught runtime-exception", e);
					}
				} else {
					serializeQueue.add(job);
				}
			}
		}
		Runnable job;
		if (isParent) {
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
		System.out.println(getName() + " exiting");
		// threadNumber.getAndDecrement();
	}
}
