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
 * test initializer which has one dependency
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@InitProviderClass
public class SimpleDependencyInitializerTest extends TreeInitializeServiceTest {
	private static int data;

	protected static void assertCalled() {
		assertEquals(0x11, data);
	}

	@Initializer(name = "sd-2", dependencies = "sd-1", phase = "sd")
	public static void fuga() {
		assertEquals(0x10, data);
		data = data | 0x01;
	}

	@Initializer(name = "sd-1", phase = "sd")
	public static void hoge() {
		data = data | 0x10;
	}

	@Test
	public void testSimpleDependenciesInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("sd");
			initService.register(SimpleDependencyInitializerTest.class);
			initService.enterPhase("sd");
			SimpleDependencyInitializerTest.assertCalled();
		} finally {
			unlock();
		}
	}
}
