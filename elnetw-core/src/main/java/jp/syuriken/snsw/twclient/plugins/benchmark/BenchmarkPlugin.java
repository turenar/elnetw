package jp.syuriken.snsw.twclient.plugins.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.bus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

/**
 * Benchmark Plugin
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class BenchmarkPlugin implements Runnable {
	private static class JobQueueTracing implements Runnable {
		private static final Logger logger = LoggerFactory.getLogger(JobQueueTracing.class);

		@Override
		public void run() {
			logger.debug("queue: {}", ClientConfiguration.getInstance().getJobQueue().toString());
		}
	}

	private class LoaderThread extends Thread {
		private final Logger logger = LoggerFactory.getLogger(LoaderThread.class);
		private String filename;

		/*package*/ LoaderThread(String filename) {
			super("benchmark");
			setDaemon(true);
			this.filename = filename;
		}

		@Override
		public void run() {
			try (Scanner scanner = new Scanner(new File(filename), "UTF-8")) {
				while (scanner.hasNextLine()) {
					try {
						Status status = DataObjectFactory.createStatus(scanner.nextLine());
						addStatus(status);
					} catch (TwitterException e) {
						logger.warn("illegal benchmark json", e);
					}
				}
				finished.set(true);
			} catch (FileNotFoundException e) {
				logger.warn("failed opening benchmark json", e);
			}
		}
	}

	private class RestPublishingJob implements Runnable {
		@Override
		public void run() {
			ArrayList<Status> list = new ArrayList<>();
			for (int i = 0; i < 200; i++) {
				Status status = getStatus();
				if (status == null) {
					break;
				} else {
					list.add(status);
				}
			}
			if (list.size() == 0 && finished.get()) {
				return;
			} else {
				synchronized (this) {
					try {
						wait(1);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
			for (Status status : list) {
				configuration.getMessageBus().getListeners(MessageBus.READER_ACCOUNT_ID, false,
						"statuses/timeline").onStatus(status);
			}
			configuration.addJob(JobQueue.PRIORITY_LOW, this);
		}
	}

	private class StreamPublishingJob extends Thread {
		/*package*/ StreamPublishingJob() {
			super("benchmark-stream");
			setDaemon(true);
		}

		@Override
		public void run() {
			Status status = getStatus();
			if (status == null && finished.get()) {
				return;
			}
			configuration.getMessageBus().getListeners(MessageBus.READER_ACCOUNT_ID, false,
					"statuses/timeline").onStatus(
					status);
		}
	}

	private static String filename;
	private static Logger logger = LoggerFactory.getLogger(BenchmarkPlugin.class);

	public static void setFilename(String filename) {
		BenchmarkPlugin.filename = filename;
	}

	public static void start() {
		new BenchmarkPlugin();
	}

	private final ClientConfiguration configuration;
	private LinkedList<Status> statuses = new LinkedList<>();
	/*package*/ AtomicBoolean finished = new AtomicBoolean();

	private BenchmarkPlugin() {
		new LoaderThread(filename).start();
		configuration = ClientConfiguration.getInstance();
		configuration.getTimer().schedule(this, 30, TimeUnit.SECONDS);
	}

	/*package*/
	synchronized void addStatus(Status status) {
		if (statuses.size() > 1000) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		statuses.add(status);
	}

	/*package*/
	synchronized Status getStatus() {
		if (statuses.size() < 1000) {
			notify();
		}
		return statuses.poll();
	}

	@Override
	public void run() {
		logger.debug("BenchmarkPlugin is enabled");
		configuration.addJob(JobQueue.PRIORITY_LOW, new RestPublishingJob());
		configuration.getTimer().scheduleWithFixedDelay(new JobQueueTracing(), 1, 1, TimeUnit.SECONDS);
		new StreamPublishingJob().start();
	}
}
