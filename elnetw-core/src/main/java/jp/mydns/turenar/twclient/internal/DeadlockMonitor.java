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

package jp.mydns.turenar.twclient.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jp.mydns.turenar.twclient.ClientConfiguration;
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

	/**
	 * create instance
	 */
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
			if (!blockedThreadEntry.isEmpty()) {
				logger.debug("blocked threads={}", blockedThreadEntry);
			}
		} catch (Throwable e) {
			logger.error("error", e);
		}
	}
}
