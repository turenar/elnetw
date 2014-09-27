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

import jp.mydns.turenar.twclient.init.InitAfter;
import jp.mydns.turenar.twclient.init.InitBefore;
import jp.mydns.turenar.twclient.init.InitCondition;
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
public class WeightCheckedInitializerTest extends TreeInitializeServiceTestBase {
	@Initializer(name = "wc-1", phase = "wc")
	public static void a(InitCondition condition) {
		assertEquals(1, getWeight(condition));
	}

	@Initializer(name = "wc-2", phase = "wc")
	@InitAfter("wc-1")
	public static void b(InitCondition condition) {
		assertEquals(2, getWeight(condition));
	}

	@Initializer(name = "wc-3", phase = "wc")
	public static void c(InitCondition condition) {
		assertEquals(4, getWeight(condition));
	}

	@Initializer(name = "wc-4", phase = "wc")
	@InitDepends("wc-1")
	public static void d(InitCondition condition) {
		assertEquals(2, getWeight(condition));
	}

	@Initializer(name = "wc-5", phase = "wc")
	@InitDepends({"wc-1", "wc-2"})
	@InitProvide("wc-5v")
	@InitBefore("wc-3")
	public static void e(InitCondition condition) {
		assertEquals(3, getWeight(condition));
	}

	@Initializer(name = "wc-6", phase = "wc")
	@InitDepends("wc-5v")
	public static void f(InitCondition condition) {
		assertEquals(5, getWeight(condition));
	}

	protected static int getWeight(InitCondition condition) {
		return ((TreeInitConditionImpl) condition).getInfo().getWeight();
	}

	@Test
	public void testAfterInitializer() throws Exception {
		InitializeService initService = getInitService();
		try {
			initService.registerPhase("wc");
			initService.register(WeightCheckedInitializerTest.class);
			initService.enterPhase("wc");
		} finally {
			unlock();
		}
	}
}
