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

import jp.mydns.turenar.twclient.init.InitDepends;
import jp.mydns.turenar.twclient.init.InitProvide;
import jp.mydns.turenar.twclient.init.InitProviderClass;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test initializer which has InitAfter annotation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@InitProviderClass
public class ProviderInitializerTest extends TreeInitializeServiceTest {
	private static int data = 0;

	@Initializer(name = "pi-1", phase = "pi")
	@InitProvide("pi-a")
	public static void a() {
		data |= 1;
	}

	private static void assertCalled() {
		assertEquals(data, 15);
	}

	@Initializer(name = "pi-2", phase = "pi")
	@InitProvide({"pi-a", "pi-b"})
	public static void b() {
		data |= 2;
	}

	@Initializer(name = "pi-3", phase = "pi")
	@InitDepends("pi-a")
	@InitProvide("pi-b")
	public static void c() {
		assertEquals(3, data & 3);
		data |= 4;
	}

	@Initializer(name = "pi-4", phase = "pi")
	@InitDepends("pi-b")
	public static void d() {
		assertEquals(7, data & 7);
		data |= 8;
	}

	@Test
	public void testAfterInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("pi");
			initService.register(ProviderInitializerTest.class);
			initService.enterPhase("pi");
			assertCalled();
		} finally {
			unlock();
		}
	}
}
