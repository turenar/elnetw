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

import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test initializer which is forced to be resolved
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ForceResolvedInitializerTest extends TreeInitializeServiceTest {
	private static int data;

	@Initializer(name = "rp-1", phase = "rp")
	public static void a() {
		data = data | 0x01;
	}

	protected static void assertCalled() {
		assertEquals(0b1111, data);
	}

	@Initializer(name = "rp-2", dependencies = "rp-v2", phase = "rp")
	public static void b() {
		assertEquals(0b0001, data);
		data = data | 0b0010;
	}

	@Initializer(name = "rp-5", dependencies = {"rp-1", "rp-2", "rp-v3", "rp-v4", "rp-4"}, phase = "rp")
	public static void c() {
		assertEquals(0b0111, data);
		data = data | 0b1000;
	}

	@Initializer(name = "rp-4", dependencies = "undefined", phase = "rp")
	public static void d() { // be never called
		fail("ForceResolvedInitializer#d must not be called: force resolved");
	}

	@Initializer(name = "rp-3", dependencies = "rp-2", phase = "rp")
	public static void e() {
		assertEquals(0b0011, data);
		data = data | 0b0100;
		InitializeService.getService().provideInitializer("rp-v3").provideInitializer("rp-v4", true);
	}

	@Initializer(name = "rp-v4", dependencies = "undefined", phase = "rp")
	public static void f() { // be never called
		fail("ForceResolvedInitializer#f must not be called: force resolved");
	}

	@Test
	public void testForceResolveInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("rp");
			initService.register(ForceResolvedInitializerTest.class);
			initService.enterPhase("rp");
			initService.provideInitializer("rp-v2");
			try {
				initService.provideInitializer("rp-4");
				fail(); // rp-4 is already registered.
			} catch (IllegalArgumentException ignore) {
				// do nothing
			}
			initService.provideInitializer("rp-4", true); // force to provide
			initService.waitConsumeQueue();
			ForceResolvedInitializerTest.assertCalled();
		} finally {
			unlock();
		}
	}
}
