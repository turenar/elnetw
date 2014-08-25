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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hash set for primitive long
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class LongHashSet {
	/**
	 * this indicates free element. if this is zero, don't have to fill array with FREE.
	 */
	protected static final long FREE = 0;
	/**
	 * this indicates zero element. 0 indicates FREE, so ZERO must be existed.
	 */
	protected static final long ZERO = Long.MIN_VALUE;
	/**
	 * default initial capacity. should be power of two.
	 */
	protected static final int INITIAL_CAPACITY = 16;
	/**
	 * default load factor. usually, 0.5 is faster than 0.3, 0.7 with random long value.
	 */
	protected static final double LOAD_FACTOR = 0.5;
	private static final Logger logger = LoggerFactory.getLogger(LongHashSet.class);

	/*package*/
	static int powerOf(int initialCapacity) {
		int l = Integer.highestOneBit(initialCapacity);
		return l < initialCapacity ? l << 1 : l;
	}

	/**
	 * load factor
	 */
	protected final double loadFactor;
	/**
	 * for developers: count of hash conflicting: synonym is found.
	 */
	protected int hashConflict;
	/**
	 * size
	 */
	protected volatile int size;
	/**
	 * hash bit set: pow2(n) - 1 (0b0000..0111..1)
	 */
	protected volatile int bitSet;
	/**
	 * values
	 */
	protected volatile long[] values;

	/**
	 * instance
	 *
	 * @param initialCapacity initial capacity(16)
	 */
	public LongHashSet(int initialCapacity) {
		this(initialCapacity, LOAD_FACTOR);
	}

	/**
	 * instance
	 */
	public LongHashSet() {
		this(INITIAL_CAPACITY);
	}

	/**
	 * instance
	 *
	 * @param initialCapacity initial capacity(16)
	 * @param loadFactor      load factor(0.5)
	 */
	public LongHashSet(int initialCapacity, double loadFactor) {
		this.loadFactor = loadFactor;
		initialCapacity = powerOf(initialCapacity);
		values = new long[initialCapacity];
		size = 0;
		bitSet = initialCapacity - 1;
	}

	/**
	 * put long value. putting {@link Long#MIN_VALUE} is not permitted
	 *
	 * @param aLong long value
	 * @return if this does not contains aLong, return true. otherwise, false.
	 */
	public synchronized boolean add(long aLong) {
		if (aLong == 0) {
			aLong = ZERO;
		}
		int hash = hash(aLong);
		int index = hash & bitSet;
		while (values[index] != FREE && values[index] != aLong) {
			index = (index + 1) & bitSet;
			hashConflict++;
		}
		if (values[index] == FREE) {
			values[index] = aLong;
			size++;
			if (size > values.length * loadFactor) {
				rehash();
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * clear all values
	 */
	public synchronized void clear() {
		for (int i = 0; i < values.length; i++) {
			values[i] = FREE;
		}
		size = 0;
	}

	@Override
	public LongHashSet clone() throws CloneNotSupportedException {
		return (LongHashSet) super.clone();
	}

	/**
	 * check if contains value
	 *
	 * @param o long value
	 * @return if contains, return true. otherwise, false.
	 */
	public synchronized boolean contains(long o) {
		if (o == 0) {
			o = ZERO;
		}
		int hash = hash(o);
		int index = hash & bitSet;
		while (values[index] != FREE && values[index] != o) {
			index = (index + 1) & bitSet;
		}
		return values[index] == o;
	}

	/**
	 * get count of hash conflict. The larger this value, the slower the performance
	 *
	 * @return hash conflict count.
	 */
	public int getHashConflict() {
		return hashConflict;
	}

	// This method copied from Murmur3, written by Austin Appleby released under Public Domain
	private int hash(long value) {
		value ^= value >>> 33; // CS-IGNORE
		value *= 0xff51afd7ed558ccdL; // CS-IGNORE
		value ^= value >>> 33; // CS-IGNORE
		value *= 0xc4ceb9fe1a85ec53L; // CS-IGNORE
		value ^= value >>> 33; // CS-IGNORE
		return (int) value;
	}

	/**
	 * check set does not contain any value.
	 *
	 * @return empty?
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	private void rehash() {
		long[] newArray = new long[values.length << 1];
		bitSet = (bitSet << 1) + 1;
		for (long aLong : values) {
			int hash = hash(aLong);
			int index = hash & bitSet;
			while (newArray[index] != FREE) {
				index = (index + 1) & bitSet;
				hashConflict++;
			}
			newArray[index] = aLong;
		}
		values = newArray;
	}

	/**
	 * remove specified value
	 *
	 * @param o long value
	 * @return if contains, return true
	 */
	public synchronized boolean remove(long o) {
		if (o == 0) {
			o = ZERO;
		}
		int hash = hash(o);
		int index = hash & bitSet;
		while (values[index] != FREE && values[index] != o) {
			index = (index + 1) & bitSet;
		}
		if (values[index] == o) {
			values[index] = FREE;
			size--;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * return size
	 *
	 * @return size
	 */
	public int size() {
		return size;
	}
}
