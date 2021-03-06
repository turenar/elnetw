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

package jp.mydns.turenar.lib.primitive;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.*;

public class LongHashSetTest {

	private static final int TEST_SIZE = 0x10000;

	public static void main(String[] args) {
		for (int i = 0; i < 16; i++) {
			System.out.printf("%d: %d%n", i, LongHashSet.hash(i) & 0x0f);
		}
	}

	private long[] newSeqArray(int start, int len) {
		long[] arr = new long[len];
		for (int i = 0; i < len; i++) {
			arr[i] = start + i;
		}
		return arr;
	}

	@Test
	public void testAdd() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		for (long i = 0; i < 16; i++) {
			assertTrue(longHashSet.add(i));
		}
		assertEquals(16, longHashSet.size());
		for (long i = 0; i < 16; i++) {
			assertTrue(longHashSet.contains(i));
			assertFalse(longHashSet.add(i));
		}
	}

	@Test
	public void testAddAll() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		longHashSet.addAll(newSeqArray(0, 16));
		assertEquals(16, longHashSet.size());
		longHashSet.addAll(newSeqArray(0, 24));
		assertEquals(24, longHashSet.size());
	}

	@Test
	public void testAddMore() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		for (long i = 0; i < 65536; i++) {
			assertTrue(longHashSet.add(i));
		}
		assertEquals(65536, longHashSet.size());
		for (long i = 0; i < 65536; i++) {
			assertTrue(longHashSet.contains(i));
			assertFalse(longHashSet.add(i));
		}
	}

	@Test
	public void testAddWithHashConflict() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		for (long i = 0; i < 256; i++) {
			assertTrue(longHashSet.add(i));
		}
		assertEquals(256, longHashSet.size());
		for (long i = 0; i < 256; i += 2) {
			assertTrue(longHashSet.remove(i));
		}
		assertEquals(128, longHashSet.size());
		for (int i = 256; i < 65536; i++) {
			assertTrue(longHashSet.add(i));
		}
		for (long i = 0; i < 256; i += 2) {
			assertTrue(longHashSet.add(i));
		}
	}

	@Test
	public void testClear() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		longHashSet.add(1L);
		assertEquals(1, longHashSet.size());
		longHashSet.clear();
	}

	@Test
	public void testClone() throws Exception {
		LongHashSet base = new LongHashSet();
		base.add(0L);
		base.add(2L);
		LongHashSet clone = base.clone();
		clone.add(1L);
		assertTrue(clone.contains(1L));
		assertEquals(3, clone.size());
		assertFalse(base.contains(1L));
		assertEquals(2, base.size());
	}

	@Test
	public void testContains() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		longHashSet.add(1L);
		assertEquals(1, longHashSet.size());
		assertTrue(longHashSet.contains(1));
		assertFalse(longHashSet.contains(2));
	}

	@Test
	public void testEnsureCapacity() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		longHashSet.ensureCapacity(65536);
		for (long i = 0; i < 65536; i++) {
			assertTrue(longHashSet.add(i));
		}
		assertEquals(65536, longHashSet.size());
		for (long i = 0; i < 65536; i++) {
			assertTrue(longHashSet.contains(i));
			assertFalse(longHashSet.add(i));
		}
	}

	@Test
	public void testHashConflict() throws Exception {
		LongHashSet longHashSet = new LongHashSet(16);
		assertTrue(longHashSet.add(4));
		assertTrue(longHashSet.add(5));
		// check hash conflicted. if conflicted, values = [.., 4, 5, ..]
		// remove 4, values = [.., REMOVED, 5, ..]
		// if we don't treat REMOVED, when we add 5, values=[.., 5, 5, ..]
		assertEquals(1, longHashSet.getHashConflict());
		assertTrue(longHashSet.contains(4));
		assertTrue(longHashSet.contains(5));
		assertTrue(longHashSet.remove(4));
		assertTrue(longHashSet.contains(5));
		assertFalse(longHashSet.add(5));
	}

	@Test
	public void testIsEmpty() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		assertTrue(longHashSet.isEmpty());
		longHashSet.add(1L);
		assertEquals(1, longHashSet.size());
		assertFalse(longHashSet.isEmpty());
	}

	@Test
	public void testParallelAdd() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		int parallelSize = 64;
		long limit = 512 + 1024 * (parallelSize - 1);
		CountDownLatch latch = new CountDownLatch(parallelSize);
		Thread[] threads = new Thread[parallelSize];
		for (long i = 0; i < limit; i += 512) {
			longHashSet.add(i);
		}
		// 0x1ff == 511
		for (int i = 0; i < threads.length; i++) {
			final int finalI = i;
			threads[i] = new Thread(() -> {
				for (long j = 512 * finalI; j < 512 + 1024 * finalI; j++) {
					if ((j & 0x1ff) == 0) {
						longHashSet.remove(j);
					} else {
						longHashSet.add(j);
					}
				}
				latch.countDown();
			});
		}
		for (Thread thread : threads) {
			thread.start();
		}
		latch.await();
		assertFalse(longHashSet.isEmpty());
		for (int i = 0; i < limit; i++) {
			if ((i & 0x1ff) == 0) {
				assertFalse(longHashSet.contains(i));
			} else {
				assertTrue(i + " is not found", longHashSet.contains(i));
			}
		}
		assertEquals(limit - limit / 512, longHashSet.size());
	}

	@Test
	public void testPowerOf() throws Exception {
		assertEquals(2, LongHashSet.powerOf(2));
		assertEquals(8, LongHashSet.powerOf(7));
		assertEquals(32, LongHashSet.powerOf(17));
	}

	@Test
	public void testRemove() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		assertTrue(longHashSet.add(1L));
		assertEquals(1, longHashSet.size());
		assertFalse(longHashSet.remove(0));
		assertEquals(1, longHashSet.size());
		assertTrue(longHashSet.remove(1));
		assertEquals(0, longHashSet.size());
	}

	@Test
	public void testSize() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		longHashSet.add(1L);
		assertEquals(1, longHashSet.size());
	}

	@Test
	public void testStream() throws Exception {
		LongHashSet longHashSet = new LongHashSet(TEST_SIZE);
		for (int i = 0; i < TEST_SIZE; i++) {
			longHashSet.add(i);
		}
		long[] expected = longHashSet.toArray();
		Arrays.sort(expected);

		List<Long> actualList = longHashSet.stream()
				.parallel()
				.boxed()
				.collect(Collectors.toList());
		long[] actual = actualList.stream()
				.mapToLong(Long::longValue)
				.sorted()
				.toArray();
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testToArray() throws Exception {
		LongHashSet longHashSet = new LongHashSet(4);
		longHashSet.addAll(newSeqArray(0, 16));
		assertEquals(16, longHashSet.size());
		long[] array = longHashSet.toArray();
		Arrays.sort(array);
		assertArrayEquals(newSeqArray(0, 16), array);
	}
}
