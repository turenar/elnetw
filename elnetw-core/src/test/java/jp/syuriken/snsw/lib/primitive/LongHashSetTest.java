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

package jp.syuriken.snsw.lib.primitive;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import static org.junit.Assert.*;

public class LongHashSetTest {
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
	public void testContains() throws Exception {
		LongHashSet longHashSet = new LongHashSet();
		assertEquals(0, longHashSet.size());
		longHashSet.add(1L);
		assertEquals(1, longHashSet.size());
		assertTrue(longHashSet.contains(1));
		assertFalse(longHashSet.contains(2));
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
}