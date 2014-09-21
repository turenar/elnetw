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

import org.junit.Test;

import static org.junit.Assert.*;

public class LongHashSetTest {
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
	public void testHashConflict() throws Exception {
		LongHashSet longHashSet = new LongHashSet(16);
		assertTrue(longHashSet.add(4));
		assertTrue(longHashSet.add(5));
		assertEquals(1, longHashSet.getHashConflict());
		assertTrue(longHashSet.contains(4));
		assertTrue(longHashSet.contains(5));
		assertTrue(longHashSet.remove(4));
		assertTrue(longHashSet.contains(5));
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
		assertTrue(longHashSet.remove(1));
		assertEquals(0, longHashSet.size());
		assertFalse(longHashSet.remove(0));
	}

	@Test
	public void testSize() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		longHashSet.add(1L);
		assertEquals(1, longHashSet.size());
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
