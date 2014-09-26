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

package jp.mydns.turenar.twclient.init.tree;

import jp.mydns.turenar.twclient.init.InitProviderClass;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test initializer which has crossing phase
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@InitProviderClass
public class CrossingPhaseInitializerTest extends TreeInitializeServiceTest {
	/*package*/ static int data;

	@Initializer(name = "cp-1", phase = "cp1")
	public static void a() {
		assertEquals(0, data);
		data = data | 0x01;
	}

	public static void assertCalled1() {
		assertEquals(1, data);
	}

	public static void assertCalled2() {
		assertEquals(7, data);
	}

	@Initializer(name = "cp-2", dependencies = "cp-3", phase = "cp1")
	public static void b() {
		assertEquals(5, data);
		data = data | 0x02;
	}

	@Initializer(name = "cp-3", phase = "cp2")
	public static void c() {
		assertEquals(1, data);
		data = data | 0x04;
	}

	@Test
	public void testCrossingPhaseDependenciesInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("cp1").registerPhase("cp2");
			initService.register(CrossingPhaseInitializerTest.class);
			initService.enterPhase("cp1");
			CrossingPhaseInitializerTest.assertCalled1();
			initService.enterPhase("cp2");
			CrossingPhaseInitializerTest.assertCalled2();
		} finally {
			unlock();
		}
	}
}
