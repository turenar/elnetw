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
		for (int i = 8; i < 24; i++) {
			sum = times[i];
		}
		return sum / 16;
	}

	private static long bench(int max, boolean rand, boolean prealloc) {
		System.gc();
		long[] myTimes = new long[32];
		for (int time = -256; time < 32; time++) {
			System.out.printf("\r\033[Ksize=%d, %s, repeat=%d", max, rand ? "random" : "sequence", time);
			long timeMillis = System.nanoTime();
			testMine(max, rand, prealloc);
			myTimes[((time + 256) % 32)] = System.nanoTime() - timeMillis;
		}
		System.gc();
		Arrays.sort(myTimes);
		return averageOf(myTimes);
	}

	public static void main(String[] args) {
		outerBench(false);
		outerBench(true);
	}

	/*
=== rand=false ===
size		average
      16	3008
      64	886
     256	3578
    1024	10478
    4096	45293
   16384	181489
   65536	452252
=== rand=true ===
size		average
      16	1117
      64	632
     256	2063
    1024	7988
    4096	31335
   16384	125880
   65536	545260
	 */
	private static void outerBench(boolean rand) {
		System.out.printf("=== rand=%s ===%n", Boolean.toString(rand));
		System.out.println("size\t\taverage\t(prealloc)");
		for (int i = 4; i < 17; i += 2) {
			System.out.printf("\r\033[K%8d\t%d\t%d%n", 1 << i, bench(1 << i, rand, false), bench(1 << i, rand, true));
		}
	}

	private static void testMine(int max, boolean rand, boolean prealloc) {
		LongHashSet longHashSet = new LongHashSet();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int i = 0;
		int randMax = max << 4;
		if (prealloc) {
			longHashSet.ensureCapacity(randMax);
		}
		for (; i < max >> 3; i++) {
			long e = rand ? random.nextLong(0, randMax) : i;
			longHashSet.add(e);
			longHashSet.contains(e);
		}
		for (; i < max >> 2; i++) {
			long e = rand ? random.nextLong(0, randMax) : i - (max >> 3);
			longHashSet.remove(e);
		}
		for (; i < max; i++) {
			long e = rand ? random.nextLong(0, randMax) : i;
			longHashSet.add(e);
			longHashSet.contains(e);
		}
	}
}
