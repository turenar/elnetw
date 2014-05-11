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

/*
 * Original source code: ch.qos.logback.core.AsyncAppenderBase
 *
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2012, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package jp.syuriken.snsw.twclient.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.ParallelRunnable;

/**
 * Async Appender for slf4j/logback. Original source code is derived from logback
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AsyncAppenderBase<E> extends UnsynchronizedAppenderBase<E> implements AppenderAttachable<E>, PropertyChangeListener {

	protected class TimerWorker implements Runnable {
		private ClientConfiguration configuration = ClientConfiguration.getInstance();

		@Override
		public void run() {
			if (blockingQueue.remainingCapacity() == 0) {
				flush();
			} else if (blockingQueue.isEmpty()) {
				configuration.addJob(JobQueue.PRIORITY_LOW, worker);
			}
		}
	}

	protected class Worker implements ParallelRunnable {
		public void run() {
			flush();
		}
	}

	/**
	 * The default buffer size.
	 */
	public static final int DEFAULT_QUEUE_SIZE = 256;
	public static final String PROPERTY_FLUSH_INTERVAL = "core.logger.flush_interval";
	protected static final int UNDEFINED = -1;
	protected int queueSize = DEFAULT_QUEUE_SIZE;
	protected int discardingThreshold = UNDEFINED;
	AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<>();
	BlockingQueue<E> blockingQueue;
	protected int appenderCount = 0;
	protected Worker worker = new Worker();
	protected TimerWorker timerWorker = new TimerWorker();
	private ScheduledFuture<?> timerWorkerFuture;

	public void addAppender(Appender<E> newAppender) {
		if (appenderCount == 0) {
			appenderCount++;
			addInfo("Attaching appender named [" + newAppender.getName() + "] to AsyncAppender.");
			aai.addAppender(newAppender);
		} else {
			addWarn("One and only one appender may be attached to AsyncAppender.");
			addWarn("Ignoring additional appender named [" + newAppender.getName() + "]");
		}
	}

	@Override
	protected void append(E eventObject) {
		if (isQueueBelowDiscardingThreshold() && isDiscardable(eventObject)) {
			return;
		}
		preprocess(eventObject);
		put(eventObject);
	}

	public void detachAndStopAllAppenders() {
		aai.detachAndStopAllAppenders();
	}

	public boolean detachAppender(Appender<E> eAppender) {
		return aai.detachAppender(eAppender);
	}

	public boolean detachAppender(String name) {
		return aai.detachAppender(name);
	}

	public void flush() {
		while (true) {
			E e = blockingQueue.poll();
			if (e == null) {
				break;
			}
			aai.appendLoopOnAppenders(e);
		}
	}

	public Appender<E> getAppender(String name) {
		return aai.getAppender(name);
	}

	public int getDiscardingThreshold() {
		return discardingThreshold;
	}

	/**
	 * Returns the number of elements currently in the blocking queue.
	 *
	 * @return number of elements currently in the queue.
	 */
	public int getNumberOfElementsInQueue() {
		return blockingQueue.size();
	}

	public int getQueueSize() {
		return queueSize;
	}

	/**
	 * The remaining capacity available in the blocking queue.
	 *
	 * @return the remaining capacity
	 * @see {@link java.util.concurrent.BlockingQueue#remainingCapacity()}
	 */
	public int getRemainingCapacity() {
		return blockingQueue.remainingCapacity();
	}

	public boolean isAttached(Appender<E> eAppender) {
		return aai.isAttached(eAppender);
	}

	/**
	 * Is the eventObject passed as parameter discardable? The base class's implementation of this method always returns
	 * 'false' but sub-classes may (and do) override this method.
	 * <p/>
	 * <p>Note that only if the buffer is nearly full are events discarded. Otherwise, when the buffer is "not full"
	 * all events are logged.
	 *
	 * @param eventObject event object
	 * @return - true if the event can be discarded, false otherwise
	 */
	protected boolean isDiscardable(E eventObject) {
		if (getRemainingCapacity() == 0) {
			flush(); // force
		}
		return false;
	}

	private boolean isQueueBelowDiscardingThreshold() {
		return (blockingQueue.remainingCapacity() < discardingThreshold);
	}

	public Iterator<Appender<E>> iteratorForAppenders() {
		return aai.iteratorForAppenders();
	}

	/**
	 * Pre-process the event prior to queueing. The base class does no pre-processing but sub-classes can
	 * override this behavior.
	 *
	 * @param eventObject event object
	 */
	protected void preprocess(E eventObject) {
	}

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {
		if (PROPERTY_FLUSH_INTERVAL.equals(evt.getPropertyName())) {
			if (timerWorkerFuture != null) {
				timerWorkerFuture.cancel(false);
			}
			ClientConfiguration configuration = ClientConfiguration.getInstance();
			long interval = configuration.getConfigProperties().getTime(PROPERTY_FLUSH_INTERVAL);
			timerWorkerFuture = configuration.getTimer().scheduleWithFixedDelay(timerWorker, interval, interval, TimeUnit.MILLISECONDS);
		}
	}

	private void put(E eventObject) {
		try {
			blockingQueue.put(eventObject);
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	public void setDiscardingThreshold(int discardingThreshold) {
		this.discardingThreshold = discardingThreshold;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	@Override
	public void start() {
		if (appenderCount == 0) {
			addError("No attached appenders found.");
			return;
		}
		if (queueSize < 1) {
			addError("Invalid queue size [" + queueSize + "]");
			return;
		}
		blockingQueue = new ArrayBlockingQueue<>(queueSize);

		if (discardingThreshold == UNDEFINED) {
			discardingThreshold = queueSize / 5;
		}
		addInfo("Setting discardingThreshold to " + discardingThreshold);
		// make sure this instance is marked as "started" before staring the worker Thread
		super.start();

		// don't queue into Timer: not initialized
		ClientConfiguration configuration = ClientConfiguration.getInstance();
		configuration.getConfigProperties().addPropertyChangedListener(this);
	}

	@Override
	public void stop() {
		if (!isStarted()) {
			return;
		}

		// mark this appender as stopped so that Worker can also processPriorToRemoval if it is invoking aii.appendLoopOnAppenders
		// and sub-appenders consume the interruption
		super.stop();

		flush();

		aai.detachAndStopAllAppenders();
	}
}
