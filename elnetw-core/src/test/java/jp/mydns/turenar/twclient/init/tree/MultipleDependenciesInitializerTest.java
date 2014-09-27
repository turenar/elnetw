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
import jp.mydns.turenar.twclient.init.InitProviderClass;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test initializer which has multiple dependencies
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@InitProviderClass
public class MultipleDependenciesInitializerTest extends TreeInitializeServiceTestBase {
	/*package*/ static int data;

	@Initializer(name = "md-1", phase = "md1")
	public static void a() {
		assertEquals(0, data);
		data = data | 0x01;
	}

	public static void assertCalled1() {
		assertEquals(0x1f, data);
	}

	public static void assertCalled2() {
		assertEquals(0x7f, data);
	}

	@Initializer(name = "md-2", phase = "md1")
	@InitDepends("md-1")
	public static void b() {
		assertEquals(0x01, data & 0x01);
		data = data | 0x02;
	}

	@Initializer(name = "md-3", phase = "md1")
	@InitDepends("md-1")
	public static void c() {
		assertEquals(0x01, data & 0x01);
		data = data | 0x04;
	}

	@Initializer(name = "md-4", phase = "md1")
	@InitDepends({"md-1", "md-2"})
	public static void d() {
		assertEquals(0x03, data & 0x03);
		data = data | 0x08;
	}

	@Initializer(name = "md-5", phase = "md1")
	@InitDepends({"md-1", "md-3", "md-4"})
	public static void e() {
		assertEquals(0x0d, data & 0x0d);
		data = data | 0x10;
	}

	@Initializer(name = "md-6", phase = "md2")
	@InitDepends({"md-1", "md-5"})
	public static void f() {
		assertEquals(0x1f, data);
		data = data | 0x20;
	}

	@Initializer(name = "md-7", phase = "md2")
	@InitDepends("md-6")
	public static void g() {
		assertEquals(0x3f, data);
		data = data | 0x40;
	}

	@Test
	public void testMultipleDependenciesInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("md1").registerPhase("md2");
			initService.register(MultipleDependenciesInitializerTest.class);
			initService.enterPhase("md1");
			MultipleDependenciesInitializerTest.assertCalled1();
			initService.enterPhase("md2");
			MultipleDependenciesInitializerTest.assertCalled2();
		} finally {
			unlock();
		}
	}
}
