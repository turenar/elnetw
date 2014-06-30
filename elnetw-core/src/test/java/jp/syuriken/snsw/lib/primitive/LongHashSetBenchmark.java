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

/**
 * Created with IntelliJ IDEA.
 * Date: 6/29/14
 * Time: 11:11 PM
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class LongHashSetBenchmark {	private static long averageOf(long[] times) {
	long sum = 0;
	for (int i = 4; i < 28; i++) {
		sum = times[i];
	}
	return sum / 24;
}

	private static long[] bench(int max, boolean rand) {
		System.out.print("\r\033[KRunning prebooster");
		for (int i = 0; i < 20; i++) {
			testHashSet(max, rand);
			testTreeSet(max, rand);
			testBukkit(max, rand);
			testMine(max, rand, 0.5);
			testMine(max, rand, 0.6);
		}
		long[] treeSetTimes = new long[32];
		long[] hashSetTimes = new long[32];
		long[] bukkitTimes = new long[32];
		long[] myTimes = new long[32];
		long[] myTimes7 = new long[32];
		System.gc();
		for (int i = -4; i < 32; i++) {
			System.out.printf("\r\033[KRunning TreeSet(size=%d, %d times)", max, i);
			long timeMillis = System.nanoTime();
			testTreeSet(max, rand);
			treeSetTimes[((i + 32) % 32)] = System.nanoTime() - timeMillis;
		}
		System.gc();
		for (int i = -4; i < 32; i++) {
			System.out.printf("\r\033[KRunning hashSet(size=%d, %d times)", max, i);
			long timeMillis = System.nanoTime();
			testHashSet(max, rand);
			hashSetTimes[((i + 32) % 32)] = System.nanoTime() - timeMillis;
		}
		System.gc();
		for (int i = -4; i < 32; i++) {
			System.out.printf("\r\033[KRunning bukkit(size=%d, %d times)", max, i);
			long timeMillis = System.nanoTime();
			testBukkit(max, rand);
			bukkitTimes[((i + 32) % 32)] = System.nanoTime() - timeMillis;
		}
		System.gc();
		for (int i = -4; i < 32; i++) {
			System.out.printf("\r\033[KRunning mine(size=%d, %d times)", max, i);
			long timeMillis = System.nanoTime();
			testMine(max, rand, 0.5);
			myTimes[((i + 32) % 32)] = System.nanoTime() - timeMillis;
		}
		System.gc();
		for (int i = -4; i < 32; i++) {
			System.out.printf("\r\033[KRunning mine7(size=%d, %d times)", max, i);
			long timeMillis = System.nanoTime();
			testMine(max, rand, 0.6);
			myTimes7[((i + 32) % 32)] = System.nanoTime() - timeMillis;
		}
		System.gc();
		Arrays.sort(hashSetTimes);
		Arrays.sort(treeSetTimes);
		Arrays.sort(bukkitTimes);
		Arrays.sort(myTimes);
		Arrays.sort(myTimes7);
		return new long[]{averageOf(treeSetTimes), averageOf(hashSetTimes), averageOf(bukkitTimes), averageOf(myTimes),
				averageOf(myTimes7)};
	}

	private static String fastest(long[] result) {
		if (result[0] < result[1]) {
			if (result[0] < result[2]) {
				if (result[0] < result[3]) {
					return "TreeSet";
				} else {
					return "mine";
				}
			} else {
				if (result[2] < result[3]) {
					return "bukkit";
				} else {
					return "mine";
				}
			}
		} else {
			if (result[1] < result[2]) {
				if (result[1] < result[3]) {
					return "HashSet";
				} else {
					return "mine";
				}
			} else {
				if (result[2] < result[3]) {
					return "bukkit";
				} else {
					return "mine";
				}
			}
		}
	}

	public static void main(String[] args) {
		outerBench(false);
		outerBench(true);
	}

	private static void outerBench(boolean rand) {
		long[] bukkitTimes = new long[20];
		long[] myTimes = new long[20];
		System.out.printf("=== rand=%s ===%n", Boolean.toString(rand));
		System.out.println("size\t\tTreeSet\t\tHashSet\t\tbukkit\t\tmine\t\tmine0.6\t\tfastest");
		for (int i = 4; i < 19; i++) {
			long[] result = bench(1 << i, rand);
			System.out.printf("\r\033[K%d\t%16d%16d%16d%16d%16d\t%s%n", i, result[0], result[1], result[2], result[3],
					result[4],
					fastest(result));
		}
	}

	private static void testBukkit(int max, boolean rand) {
		BukkitLongHashSet bukkitLongHashSet = new BukkitLongHashSet();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < max; i++) {
			if (rand) {
				long e = random.nextLong();
				bukkitLongHashSet.add(e);
				bukkitLongHashSet.contains(e);
			} else {
				bukkitLongHashSet.add(i);
				bukkitLongHashSet.contains(i);
			}
		}
	}

	private static void testHashSet(int max, boolean rand) {
		Set<Long> set = new HashSet<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < max; i++) {
			if (rand) {
				long e = random.nextLong();
				set.add(e);
				set.contains(e);
			} else {
				long e = (long) i;
				set.add(e);
				set.contains(e);
			}
		}
	}

	private static void testMine(int max, boolean rand, double f) {
		LongHashSet longHashSet = new LongHashSet(16, f);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < max; i++) {
			if (rand) {
				long e = random.nextLong();
				longHashSet.add(e);
				longHashSet.contains(e);
			} else {
				longHashSet.add(i);
				longHashSet.contains(i);
			}
		}
	}

	private static void testTreeSet(int max, boolean rand) {
		Set<Long> set = new TreeSet<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < max; i++) {
			if (rand) {
				long e = random.nextLong();
				set.add(e);
				set.contains(e);
			} else {
				long e = (long) i;
				set.add(e);
				set.contains(e);
			}
		}
	}
}
