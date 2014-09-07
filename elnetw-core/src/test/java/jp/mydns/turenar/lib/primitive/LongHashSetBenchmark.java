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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Benchmark for some Long Container
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class LongHashSetBenchmark {
	private static long averageOf(long[] times) {
		long sum = 0;
		for (int i = 4; i < 28; i++) {
			sum = times[i];
		}
		return sum / 24;
	}

	private static long[] bench(int max, boolean rand) {
		long[] factorArray = new long[61];
		System.gc();
		for (int rehashFactor = 20; rehashFactor <= 80; rehashFactor++) {
			long[] myTimes = new long[32];
			for (int time = -4; time < 32; time++) {
				System.out.printf("\r\033[KRunning LongHashSet(size=%d, factor=0.%02d)", max, rehashFactor);
				long timeMillis = System.nanoTime();
				testMine(max, rand, rehashFactor * 0.01);
				myTimes[((time + 32) % 32)] = System.nanoTime() - timeMillis;
			}
			System.gc();
			Arrays.sort(myTimes);
			factorArray[rehashFactor - 20] = averageOf(myTimes);
		}
		return factorArray;
	}

	public static void main(String[] args) {
		outerBench(false);
		outerBench(true);
	}

	private static void outerBench(boolean rand) {
		System.out.printf("=== rand=%s ===%n", Boolean.toString(rand));
		System.out.println("size\t\t1st\t\t2nd\t\t3rd\t\t4th");
		for (int i = 4; i < 17; i++) {
			long[] result = bench(1 << i, rand);
			System.out.printf("\r\033[K%8d\t", 1 << i);
			showFastest(result);
		}
	}

	private static void showFastest(long[] result) {
		int[] index = new int[]{-1, -1, -1, -1};
		long[] max = new long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
		for (int i = 0; i < result.length; i++) {
			if (max[3] > result[i]) {
				max[3] = result[index[3] = i];
				sort(max, index);
			}
		}
		for (int i = 0, maxLength = max.length; i < maxLength; i++) {
			System.out.printf("[%d]%8d\t", index[i] + 20, max[i]);
		}
		System.out.println();
	}

	private static void sort(long[] max, int[] index) {
		for (int i = 2; i >= 0; i--) {
			if (max[i] > max[i + 1]) {
				swap(max, index, i);
			}
		}
	}

	private static void swap(long[] max, int[] index, int i) {
		max[i + 1] = max[i] ^ max[i + 1];
		max[i] = max[i] ^ max[i + 1];
		max[i + 1] = max[i] ^ max[i + 1];
		index[i + 1] = index[i] ^ index[i + 1];
		index[i] = index[i] ^ index[i + 1];
		index[i + 1] = index[i] ^ index[i + 1];
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
}
