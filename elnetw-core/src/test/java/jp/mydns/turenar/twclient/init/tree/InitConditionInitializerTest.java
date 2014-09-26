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
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test initializer which has InitCondition argument
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@InitProviderClass
public class InitConditionInitializerTest extends TreeInitializeServiceTest {
	/*package*/ static boolean isCalled;

	@Initializer(name = "initcond", phase = "initcond")
	public static void testInitConditionInitializer(InitCondition initCondition) {
		assertTrue(initCondition.isInitializingPhase());
		isCalled = true;
	}

	@Test
	public void testInitConditionUsedInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("initcond");
			initService.register(InitConditionInitializerTest.class);
			initService.enterPhase("initcond");
			assertTrue(InitConditionInitializerTest.isCalled);
		} finally {
			unlock();
		}
	}
}
