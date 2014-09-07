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

import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.spi.ILoggingEvent;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.JobQueue;

/**
 * AsyncAppenderBase implementation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AsyncAppender extends AsyncAppenderBase<ILoggingEvent> {
	/**
	 * high priority flusher. this stores queued flag
	 */
	protected class HighPriorityWorker extends Worker {
		public volatile AtomicBoolean isQueued = new AtomicBoolean();

		@Override
		public void run() {
			super.run();
			isQueued.set(false);
		}
	}

	/**
	 * high priority worker singleton
	 */
	protected HighPriorityWorker highPriorityWorker = new HighPriorityWorker();

	@Override
	protected boolean isDiscardable(ILoggingEvent eventObject) {
		if (highPriorityWorker.isQueued.compareAndSet(false, true)) {
			ClientConfiguration.getInstance().addJob(JobQueue.PRIORITY_MAX, highPriorityWorker);
		}
		return super.isDiscardable(eventObject);
	}
}
