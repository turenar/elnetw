package jp.syuriken.snsw.twclient.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deadlock Detector
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DeadlockMonitor implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(DeadlockMonitor.class);
	private final ScheduledFuture<?> scheduledFuture;
	private Map<Thread, StackTraceElement[]> blockedThreadEntry = new HashMap<>();

	public DeadlockMonitor() {
		ClientConfiguration configuration = ClientConfiguration.getInstance();
		long intervalTime = configuration.getConfigProperties().getTime("core.supervisor.deadlock.monitor_interval");
		scheduledFuture = configuration.getSupervisorTimer()
				.scheduleWithFixedDelay(this, 0, intervalTime, TimeUnit.MILLISECONDS);
	}

	/**
	 * stop deadlock monitor
	 */
	public void cancel() {
		scheduledFuture.cancel(false);
	}

	@Override
	public synchronized void run() {
		try {
			Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
			for (Iterator<Map.Entry<Thread, StackTraceElement[]>> iterator = stackTraces.entrySet().iterator();
				 iterator.hasNext(); ) {
				Map.Entry<Thread, StackTraceElement[]> threadEntry = iterator.next();
				Thread thread = threadEntry.getKey();
				if (thread.getState() == Thread.State.BLOCKED) {
					StackTraceElement[] newStackTraces = threadEntry.getValue();
					StackTraceElement[] oldStackTraces = blockedThreadEntry.get(thread);
					if (oldStackTraces != null && Arrays.deepEquals(newStackTraces, oldStackTraces)) {
						logger.warn("is deadlock? {}", thread.getName());
						logger.warn("Stacktrace for {}", thread.getName());
						for (StackTraceElement newStackTrace : newStackTraces) {
							logger.warn(" at {}#{}", newStackTrace.getClassName(), newStackTrace.getMethodName());
						}
					}
				} else {
					iterator.remove();
				}
			}
			blockedThreadEntry = stackTraces;
			logger.debug("blocked threads={}", blockedThreadEntry);
		} catch (Throwable e) {
			logger.error("error", e);
		}
	}
}
