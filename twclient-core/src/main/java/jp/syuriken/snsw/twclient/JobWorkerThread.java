package jp.syuriken.snsw.twclient;

/**
 * ジョブワーカースレッド。
 * 
 * @author $Author$
 */
/*package*/class JobWorkerThread extends Thread {
	
	protected volatile boolean isCanceled = false;
	
	private Object threadHolder = new Object();
	
	private final JobQueue jobQueue;
	
	
	public JobWorkerThread(String threadName, JobQueue jobQueue) {
		super(threadName);
		this.jobQueue = jobQueue;
		setDaemon(true);
	}
	
	public void cleanUp() {
		jobQueue.setJobWorkerThread(null, null);
		isCanceled = true;
	}
	
	@Override
	public void run() {
		jobQueue.setJobWorkerThread(threadHolder, this);
		while (isCanceled == false) {
			Runnable job = jobQueue.getJob();
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
				try {
					job.run();
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
