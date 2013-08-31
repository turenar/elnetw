package jp.syuriken.snsw.twclient;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import static jp.syuriken.snsw.twclient.JobQueue.LinkedQueue;
import static jp.syuriken.snsw.twclient.JobQueue.Priority;
import static org.junit.Assert.*;

/**
 * Test for {@link JobQueue}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class JobQueueTest {
	public static class AddIntoQueueThread extends Thread {
		private final JobQueue queue;

		private final Priority priority;

		private final CountDownLatch latch;

		public AddIntoQueueThread(JobQueue queue, CountDownLatch latch, Priority priority) {
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

		public AddJobThread(LinkedQueue<Runnable> queue, CountDownLatch latch, int loop) {
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

	public static class TestRunnable implements Runnable {
		public final int value;

		public TestRunnable(int i) {
			value = i;
		}

		@Override
		public void run() {
		}
	}

	public static final int COUNT_PER_THREAD = 10000;

	public static final int THREADS_COUNT = 100;

	private Priority getPriority(int i) {
		switch (i % 4) {
			case 0:
			case 1:
				return Priority.MEDIUM;
			case 2:
				return Priority.HIGH;
			case 3:
				return Priority.LOW;
		}
		throw new AssertionError();
	}

	@Test
	public void testAddJob() throws Exception {
		JobQueue jobQueue = new JobQueue();
		jobQueue.setJobWorkerThread(this); // do not run jobs automatically in JobQueue#addJob

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
		JobQueue queue = new JobQueue();
		queue.setJobWorkerThread(this); // do not run jobs automatically in JobQueue#addJob
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
	public void testIsEmpty() throws Exception {
		JobQueue jobQueue = new JobQueue();
		assertTrue(jobQueue.isEmpty());
		assertEquals(0, jobQueue.size());
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
				new AddJobThread(queue, latch, COUNT_PER_THREAD).start();
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
}