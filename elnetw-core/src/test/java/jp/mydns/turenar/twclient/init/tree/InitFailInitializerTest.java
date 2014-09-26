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

import jp.mydns.turenar.twclient.init.InitCondition;
import jp.mydns.turenar.twclient.init.InitProviderClass;
import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test initializer which fails
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@InitProviderClass
public class InitFailInitializerTest extends TreeInitializeServiceTest {
	@Initializer(name = "initfail", phase = "initfail")
	public static void a(InitCondition condition) {
		condition.setFailStatus("test", 0xf0000000);
	}

	@Test
	public void testInitFailInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("initfail");
			initService.register(InitFailInitializerTest.class);
			try {
				initService.enterPhase("initfail");
				fail();
			} catch (InitializeException e) {
				assertEquals(0xf0000000, e.getExitCode());
				assertEquals("test", e.getMessage());
				assertEquals(InitFailInitializerTest.class.getMethod("a", InitCondition.class),
						e.getInitializerInfo().getInitializer());
			}
		} finally {
			unlock();
		}
	}
}
