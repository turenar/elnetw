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

package jp.syuriken.snsw.twclient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static jp.syuriken.snsw.twclient.JobQueue.LinkedQueue;
import static jp.syuriken.snsw.twclient.JobQueue.PHASE_EXITED;
import static jp.syuriken.snsw.twclient.JobQueue.PHASE_NEW;
import static jp.syuriken.snsw.twclient.JobQueue.PHASE_RUNNING;
import static jp.syuriken.snsw.twclient.JobQueue.PHASE_STOPPING;
import static org.junit.Assert.*;

/**
 * Test for {@link JobQueue}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class JobQueueTest {
	public static class AddIntoQueueThread extends Thread {
		private final JobQueue queue;
		private final byte priority;
		private final CountDownLatch latch;

		public AddIntoQueueThread(JobQueue queue, CountDownLatch latch, byte priority) {
			this.latch = latch;
			this.queue = queue;
			this.priority = priority;
		}

		@Override
		public void run() {
			TestRunnable runnable;
			synchronized (queue) {
				runnable = new TestRunnable(0);
			}
			for (int i = 0; i < COUNT_PER_THREAD; i++) {
				queue.addJob(priority, runnable);
			}
			latch.countDown();
		}
	}

	public static class AddJobThread extends Thread {
		private final LinkedQueue<Runnable> queue;
		private final CountDownLatch latch;

		public AddJobThread(LinkedQueue<Runnable> queue, CountDownLatch latch) {
			this.latch = latch;
			this.queue = queue;
		}

		@Override
		public void run() {
			TestRunnable runnable;
			synchronized (queue) {
				runnable = new TestRunnable(0);
			}
			for (int i = 0; i < COUNT_PER_THREAD; i++) {
				queue.add(runnable);
			}
			latch.countDown();
		}
	}

	public static class ConsumeJobThread extends Thread {
		private final LinkedQueue<Runnable> queue;
		private final CountDownLatch latch;
		private volatile int jobsCount;

		public ConsumeJobThread(LinkedQueue<Runnable> queue, CountDownLatch latch) {
			this.latch = latch;
			this.queue = queue;
		}

		public int getCount() {
			return jobsCount;
		}

		@Override
		public void run() {
			synchronized (queue) {
				// wait lock
			}
			while (queue.poll() != null) {
				jobsCount++;
			}
			latch.countDown();
		}
	}

	public static class ConsumeQueueThread extends Thread {
		private final JobQueue queue;
		private final CountDownLatch latch;
		private volatile int jobsCount;

		public ConsumeQueueThread(JobQueue queue, CountDownLatch latch) {
			this.latch = latch;
			this.queue = queue;
		}

		public int getCount() {
			return jobsCount;
		}

		@Override
		public void run() {
			synchronized (queue) {
				// wait lock
			}
			while (queue.getJob() != null) {
				jobsCount++;
			}
			latch.countDown();
		}
	}

	public static class JobQueueTestImpl extends JobQueue {
		public JobQueueTestImpl() {
			coreThreadPoolSize = 1;
			maximumThreadPoolSize = 1;
			execWorkerThreshold = 100;
			keepAliveTime = 100;
			longAliveThreshold = 100;
		}

		@Override
		protected void initProperties() {
		}
	}

	private static class SimpleJob implements ParallelRunnable {
		private final AtomicInteger callCount;

		public SimpleJob(AtomicInteger callCount) {
			this.callCount = callCount;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// do nothing
			}
			callCount.incrementAndGet();
		}
	}

	public static class TestRunnable implements Runnable {
		public final int value;

		public TestRunnable(int i) {
			value = i;
		}

		@Override
		public void run() {
		}
	}

	private static class WorkerChecker implements ParallelRunnable {
		private final AtomicInteger callCount;
		private final int expectedWorkerCount;
		private final AssertionHandler handler;
		private final JobQueueTestImpl jobQueue;

		public WorkerChecker(AtomicInteger callCount, int expectedWorkerCount, AssertionHandler handler,
				JobQueueTestImpl jobQueue) {
			this.callCount = callCount;
			this.expectedWorkerCount = expectedWorkerCount;
			this.handler = handler;
			this.jobQueue = jobQueue;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// do nothing
			}
			callCount.incrementAndGet();
			handler.assertEquals(expectedWorkerCount, jobQueue.getWorkerCount());
		}
	}

	public static final int COUNT_PER_THREAD = 10000;
	public static final int THREADS_COUNT = 100;

	private byte getPriority(int i) {
		return (byte) (i % 16);
	}

	private void shutdownQueue(JobQueue jobQueue) throws InterruptedException {
		while (!jobQueue.shutdownNow(1000)) {
			jobQueue.shutdown();
		}
	}

	@Test
	public void testAddJob() throws Exception {
		JobQueue jobQueue = new JobQueueTestImpl();
		// out-dated
		//jobQueue.setJobWorkerThread(this); // do not run jobs automatically in JobQueue#addJob

		jobQueue.addJob(new TestRunnable(0));
		assertEquals(1, jobQueue.size());
		assertFalse(jobQueue.isEmpty());

		jobQueue.addJob(new TestRunnable(1));
		assertEquals(2, jobQueue.size());

		jobQueue.addJob(new TestRunnable(2));
		assertEquals(3, jobQueue.size());

		assertEquals(0, ((TestRunnable) jobQueue.getJob()).value);
		assertEquals(1, ((TestRunnable) jobQueue.getJob()).value);
		assertEquals(2, ((TestRunnable) jobQueue.getJob()).value);
	}

	@Test
	public void testAddJobParallel() throws Exception {
		JobQueue queue = new JobQueueTestImpl();
		//queue.setJobWorkerThread(this); // do not run jobs automatically in JobQueue#addJob

		// add into queue
		CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
		synchronized (queue) {
			for (int i = 0; i < THREADS_COUNT; i++) {
				new AddIntoQueueThread(queue, latch, getPriority(i)).start();
			}
		}
		latch.await();
		assertEquals(THREADS_COUNT * COUNT_PER_THREAD, queue.size());

		// reset latch
		latch = new CountDownLatch(THREADS_COUNT);
		// consume queue
		ConsumeQueueThread[] threads = new ConsumeQueueThread[THREADS_COUNT];
		synchronized (queue) {
			for (int i = 0; i < THREADS_COUNT; i++) {
				ConsumeQueueThread thread = new ConsumeQueueThread(queue, latch);
				thread.start();
				threads[i] = thread;
			}
		}
		latch.await();
		int total = 0;
		for (int i = 0; i < THREADS_COUNT; i++) {
			total += threads[i].getCount();
		}
		assertEquals(THREADS_COUNT * COUNT_PER_THREAD, total);
		assertEquals(0, queue.size());
	}

	@Test
	public void testGetCoreThreadPoolSize() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		assertEquals(1, jobQueue.getCoreThreadPoolSize());
		jobQueue.setCoreThreadPoolSize(4);
		assertEquals(4, jobQueue.getCoreThreadPoolSize());
	}

	@Test
	public void testGetExecWorkerThreshold() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		assertEquals(100, jobQueue.getExecWorkerThreshold());
		jobQueue.setExecWorkerThreshold(4);
		assertEquals(4, jobQueue.getExecWorkerThreshold());
	}

	@Test
	public void testGetKeepAliveTime() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		assertEquals(100, jobQueue.getKeepAliveTime());
		jobQueue.setKeepAliveTime(4);
		assertEquals(4, jobQueue.getKeepAliveTime());
	}

	@Test
	public void testGetLongAliveThreshold() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		assertEquals(100, jobQueue.getLongAliveThreshold());
		jobQueue.setLongAliveThreshold(4);
		assertEquals(4, jobQueue.getLongAliveThreshold());
	}

	@Test
	public void testGetMaxThreadPoolSize() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		assertEquals(1, jobQueue.getMaximumThreadPoolSize());
		jobQueue.setMaximumThreadPoolSize(4);
		assertEquals(4, jobQueue.getMaximumThreadPoolSize());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCoreThreadPoolSize() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.setCoreThreadPoolSize(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalExecWorkerThreshold() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.setExecWorkerThreshold(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalKeepAliveTime() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.setKeepAliveTime(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalLongAliveThreshold() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.setLongAliveThreshold(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalMaximumThreadPoolSize() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.setMaximumThreadPoolSize(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalMaximumThreadPoolSizeLessThanCore() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.setCoreThreadPoolSize(4);
		jobQueue.setMaximumThreadPoolSize(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalPriority0() {
		new JobQueueTestImpl().addJob((byte) -1, new TestRunnable(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalPriority1() {
		new JobQueueTestImpl().addJob((byte) 127, new TestRunnable(0));
	}

	@Test
	public void testIsEmpty() throws Exception {
		JobQueue jobQueue = new JobQueueTestImpl();
		assertTrue(jobQueue.isEmpty());
		assertEquals(0, jobQueue.size());
	}

	@Test
	public void testKeepAlive() throws Throwable {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		AssertionHandler handler = new AssertionHandler();
		AtomicInteger callCount = new AtomicInteger();

		Runnable job = new SimpleJob(callCount);
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		Runnable workerChecker = new WorkerChecker(callCount, 4, handler, jobQueue);
		jobQueue.addJob(workerChecker);
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.setCoreThreadPoolSize(2);
		jobQueue.setMaximumThreadPoolSize(4);
		jobQueue.setKeepAliveTime(2);
		jobQueue.setExecWorkerThreshold(1);
		assertEquals(201, jobQueue.size());
		assertEquals(1, jobQueue.getWorkerCount());
		assertEquals(0, callCount.get());
		jobQueue.startWorker();

		for (int i = 0; i < 10; i++) {
			jobQueue.addJob(job); // start additional worker
		}
		do {
			Thread.sleep(2); // wait for all jobs consumed
		} while (jobQueue.size() != 0);
		for (int i = 0; i < 101; i++) {
			if (jobQueue.getWorkerCount() == 2) {
				break;
			} else if (i == 100) {
				fail("KeepAlive test failed");
			}
			Thread.sleep(1);
		}
		assertEquals(0, jobQueue.size());
		assertEquals(2, jobQueue.getWorkerCount());
		assertEquals(211, callCount.get());
		shutdownQueue(jobQueue);
		handler.check();
		assertEquals(0, jobQueue.size());
		assertEquals(0, jobQueue.getWorkerCount());
		assertEquals(211, callCount.get());
	}

	@Test
	public void testLinkedQueueAdd() throws Exception {
		LinkedQueue<TestRunnable> queue = new LinkedQueue<>();
		assertEquals(null, queue.poll());
		assertEquals(0, queue.size());
		assertEquals(true, queue.isEmpty());

		queue.add(new TestRunnable(0));
		assertEquals(1, queue.size());
		assertEquals(false, queue.isEmpty());

		assertEquals(0, queue.poll().value);
		assertEquals(null, queue.poll());
		assertEquals(0, queue.size());
		assertEquals(true, queue.isEmpty());
	}

	@Test
	public void testLinkedQueueAddComplex() throws Exception {
		LinkedQueue<TestRunnable> queue = new LinkedQueue<>();
		assertEquals(null, queue.poll());
		assertEquals(0, queue.size());
		assertEquals(true, queue.isEmpty());

		queue.add(new TestRunnable(1));
		queue.add(new TestRunnable(2));
		queue.add(new TestRunnable(3));
		queue.add(new TestRunnable(4));
		queue.add(new TestRunnable(5));
		assertEquals(5, queue.size());
		assertEquals(false, queue.isEmpty());

		assertEquals(1, queue.poll().value);
		assertEquals(2, queue.poll().value);
		assertEquals(3, queue.poll().value);
		assertEquals(4, queue.poll().value);
		assertEquals(5, queue.poll().value);
		assertEquals(null, queue.poll());
		assertEquals(0, queue.size());
		assertEquals(true, queue.isEmpty());

		queue.add(new TestRunnable(1));
		queue.add(new TestRunnable(2));
		queue.add(new TestRunnable(3));
		queue.add(new TestRunnable(4));
		queue.add(new TestRunnable(5));
		assertEquals(5, queue.size());
		assertEquals(false, queue.isEmpty());

		assertEquals(1, queue.poll().value);
		assertEquals(2, queue.poll().value);
		assertEquals(3, queue.poll().value);
		assertEquals(4, queue.poll().value);
		assertEquals(5, queue.poll().value);
		assertEquals(null, queue.poll());
		assertEquals(0, queue.size());
		assertEquals(true, queue.isEmpty());
	}

	@Test
	public void testLinkedQueueAddParallel() throws Exception {
		// add into queue
		LinkedQueue<Runnable> queue = new LinkedQueue<>();
		CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
		synchronized (queue) {
			for (int i = 0; i < THREADS_COUNT; i++) {
				new AddJobThread(queue, latch).start();
			}
		}
		latch.await();
		assertEquals(THREADS_COUNT * COUNT_PER_THREAD, queue.size());

		// reset latch
		latch = new CountDownLatch(THREADS_COUNT);
		// consume queue
		ConsumeJobThread[] threads = new ConsumeJobThread[THREADS_COUNT];
		synchronized (queue) {
			for (int i = 0; i < THREADS_COUNT; i++) {
				ConsumeJobThread thread = new ConsumeJobThread(queue, latch);
				thread.start();
				threads[i] = thread;
			}
		}
		latch.await();
		int total = 0;
		for (int i = 0; i < THREADS_COUNT; i++) {
			total += threads[i].getCount();
		}
		assertEquals(THREADS_COUNT * COUNT_PER_THREAD, total);
		assertEquals(0, queue.size());
	}

	@Test
	public void testMultipleWorkerForCoreThread() throws Throwable {
		final JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		final AtomicInteger callCount = new AtomicInteger();
		final AssertionHandler handler = new AssertionHandler();

		Runnable job = new SimpleJob(callCount);
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.addJob(new WorkerChecker(callCount, 4, handler, jobQueue));
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.setCoreThreadPoolSize(4);
		assertEquals(201, jobQueue.size());
		assertEquals(1, jobQueue.getWorkerCount());
		assertEquals(0, callCount.get());
		jobQueue.startWorker();

		for (int i = 0; i < 10; i++) {
			jobQueue.addJob(job); // start additional worker
		}

		shutdownQueue(jobQueue);
		handler.check();
		assertEquals(0, jobQueue.size());
		assertEquals(0, jobQueue.getWorkerCount());
		assertEquals(211, callCount.get());
	}

	@Test
	public void testMultipleWorkerForDoNotAddExtraThread() throws Throwable {
		// do not run extra thread (threadCount = coreThreadCount)
		final JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		final AtomicInteger callCount = new AtomicInteger();
		final AssertionHandler handler = new AssertionHandler();

		Runnable job = new SimpleJob(callCount);
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.addJob(new WorkerChecker(callCount, 2, handler, jobQueue));
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.setCoreThreadPoolSize(2);
		jobQueue.setMaximumThreadPoolSize(4);
		jobQueue.setExecWorkerThreshold(10000);
		assertEquals(201, jobQueue.size());
		assertEquals(1, jobQueue.getWorkerCount());
		assertEquals(0, callCount.get());
		jobQueue.startWorker();

		for (int i = 0; i < 10; i++) {
			jobQueue.addJob(job); // start additional worker
		}

		shutdownQueue(jobQueue);
		handler.check();
		assertEquals(0, jobQueue.size());
		assertEquals(0, jobQueue.getWorkerCount());
		assertEquals(211, callCount.get());
	}

	@Test
	public void testMultipleWorkerForMaxThread() throws Throwable {
		final JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		final AtomicInteger callCount = new AtomicInteger();
		final AssertionHandler handler = new AssertionHandler();

		Runnable job = new SimpleJob(callCount);
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.addJob(new WorkerChecker(callCount, 4, handler, jobQueue));
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.setCoreThreadPoolSize(2);
		jobQueue.setMaximumThreadPoolSize(4);
		jobQueue.setExecWorkerThreshold(1);
		assertEquals(201, jobQueue.size());
		assertEquals(1, jobQueue.getWorkerCount());
		assertEquals(0, callCount.get());
		jobQueue.startWorker();

		for (int i = 0; i < 10; i++) {
			jobQueue.addJob(job); // start additional worker
		}

		shutdownQueue(jobQueue);
		handler.check();
		assertEquals(0, jobQueue.size());
		assertEquals(0, jobQueue.getWorkerCount());
		assertEquals(211, callCount.get());
	}

	@Test
	public void testPhase() throws Throwable {
		final JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		assertEquals(PHASE_NEW, jobQueue.getPhase());
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(new ParallelRunnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			});
		}
		jobQueue.startWorker();
		assertEquals(PHASE_RUNNING, jobQueue.getPhase());
		jobQueue.shutdown();
		assertEquals(PHASE_STOPPING, jobQueue.getPhase());
		shutdownQueue(jobQueue);
		assertEquals(PHASE_EXITED, jobQueue.getPhase());
	}

	@Test
	public void testShutdownWithNotStarted() throws Exception {
		JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		jobQueue.addJob(new TestRunnable(0));
		jobQueue.addJob(new TestRunnable(1));
		jobQueue.addJob(new TestRunnable(2));

		assertEquals(3, jobQueue.size());

		jobQueue.shutdown();
		assertEquals(0, jobQueue.size());
	}

	@Test
	public void testSingleWorker() throws Throwable {
		final JobQueueTestImpl jobQueue = new JobQueueTestImpl();
		final AtomicInteger callCount = new AtomicInteger();
		final AssertionHandler handler = new AssertionHandler();

		Runnable job = new SimpleJob(callCount);
		for (int i = 0; i < 100; i++) {
			jobQueue.addJob(job);
		}

		jobQueue.addJob(new WorkerChecker(callCount, 1, handler, jobQueue));

		assertEquals(101, jobQueue.size());
		assertEquals(1, jobQueue.getWorkerCount());
		assertEquals(0, callCount.get());
		jobQueue.startWorker();
		for (int i = 0; i < 10; i++) {
			jobQueue.addJob(job);
		}
		shutdownQueue(jobQueue);
		handler.check();
		assertEquals(0, jobQueue.size());
		assertEquals(0, jobQueue.getWorkerCount());
		assertEquals(111, callCount.get());
	}
}
