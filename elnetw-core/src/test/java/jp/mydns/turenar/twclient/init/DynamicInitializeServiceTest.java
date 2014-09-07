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

package jp.mydns.turenar.twclient.init;

import org.junit.Test;

import static org.junit.Assert.*;

/** Test for {@link DynamicInitializeService} */
public class DynamicInitializeServiceTest {
	@InitProviderClass
	protected static class CrossingPhaseInitializer {
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
	}

	private static class ForceResolvedInitializer {
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

		@Initializer(name = "rp-4", dependencies = "rp-5", phase = "rp")
		public static void d() { // be never called
			fail("ForceResolvedInitializer#d must not be called: force resolved");
		}

		@Initializer(name = "rp-3", dependencies = "rp-2", phase = "rp")
		public static void e() {
			assertEquals(0b0011, data);
			data = data | 0b0100;
			InitializeService.getService().provideInitializer("rp-v3").provideInitializer("rp-v4", true);
		}

		@Initializer(name = "rp-v4", dependencies = "rp-5", phase = "rp")
		public static void f() { // be never called
			fail("ForceResolvedInitializer#f must not be called: force resolved");
		}
	}

	@InitProviderClass
	protected static class InitConditionInitializer {
		/*package*/ static boolean isCalled;

		@Initializer(name = "initcond", phase = "initcond")
		public static void testInitConditionInitializer(InitCondition initCondition) {
			assertTrue(initCondition.isInitializingPhase());
			isCalled = true;
		}
	}

	@InitProviderClass
	protected static class InitFailInitializer {
		/*package*/ static boolean isCalled = false;

		@Initializer(name = "initfail", phase
				= "initfail")
		public static void a(InitCondition condition) {
			condition.setFailStatus("test", 0xf0000000);
		}
	}

	@InitProviderClass
	protected static class InstanceInitializer {

		/** loaded from DynamicInitializeService */
		@InitializerInstance
		private static final InstanceInitializer instance = new InstanceInitializer();

		/*package*/
		static InstanceInitializer getInstance() {
			return instance;
		}

		/*package*/ boolean isCalled = false;

		@Initializer(name = "instance", phase = "instance")
		public void testInstanceInitializer() {
			isCalled = true;
		}
	}

	@InitProviderClass
	protected static class MultipleDependenciesInitializer {
		/*package*/ static int data;

		@Initializer(name = "md-1", phase = "md1")
		public static void a() {
			assertEquals(0, data);
			data = data | 0x01;
		}

		public static void assertCalled1() {
			assertEquals(data, 0x1f);
		}

		public static void assertCalled2() {
			assertEquals(data, 0x7f);
		}

		@Initializer(name = "md-2", dependencies = "md-1", phase = "md1")
		public static void b() {
			assertEquals(0x01, data & 0x01);
			data = data | 0x02;
		}

		@Initializer(name = "md-3", dependencies = "md-1", phase = "md1")
		public static void c() {
			assertEquals(0x01, data & 0x01);
			data = data | 0x04;
		}

		@Initializer(name = "md-4", dependencies = {"md-1", "md-2"}, phase = "md1")
		public static void d() {
			assertEquals(0x03, data & 0x03);
			data = data | 0x08;
		}

		@Initializer(name = "md-5", dependencies = {"md-1", "md-3", "md-4"}, phase = "md1")
		public static void e() {
			assertEquals(0x0d, data & 0x0d);
			data = data | 0x10;
		}

		@Initializer(name = "md-6", dependencies = {"md-1", "md-5"}, phase = "md2")
		public static void f() {
			assertEquals(0x1f, data);
			data = data | 0x20;
		}

		@Initializer(name = "md-7", dependencies = "md-6", phase = "md2")
		public static void g() {
			assertEquals(0x3f, data);
			data = data | 0x40;
		}
	}

	@InitProviderClass
	protected static class SimpleDependencyInitializer {
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
	}

	@InitProviderClass
	protected static class StaticInitializer {
		/*package*/ static boolean isCalled = false;

		@Initializer(name = "static", phase = "static")
		public static void testStaticInitializer() {
			isCalled = true;
		}
	}

	private static boolean isRegistered = false;

	private InitializeService getInitService() throws Exception {
		if (isRegistered == false) {
			DynamicInitializeService.use(null);
			isRegistered = true;
		}
		return InitializeService.getService();
	}

	@Test
	public void testUse() throws Exception {
		getInitService();
		if (InitializeService.getService() instanceof DynamicInitializeService == false) {
			fail();
		}
		try {
			DynamicInitializeService.use(null);
			fail();
		} catch (IllegalStateException e) {
			// success
		}
	}

	@Test
	public void test_01_initConditionUsedInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("initcond");
		initService.register(InitConditionInitializer.class);
		initService.enterPhase("initcond");
		assertTrue(InitConditionInitializer.isCalled);
	}

	@Test
	public void test_01_instanceInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("instance");
		initService.register(InstanceInitializer.class);
		initService.enterPhase("instance");
		assertTrue(InstanceInitializer.getInstance().isCalled);
	}

	@Test
	public void test_01_staticInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("static");
		initService.register(StaticInitializer.class);
		initService.enterPhase("static");
		assertTrue(StaticInitializer.isCalled);
	}

	@Test
	public void test_02_initFailInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("initfail");
		initService.register(InitFailInitializer.class);
		try {
			initService.enterPhase("initfail");
			fail();
		} catch (InitializeException e) {
			assertEquals(0xf0000000, e.getExitCode());
			assertEquals("test", e.getMessage());
			assertEquals(InitFailInitializer.class.getMethod("a", InitCondition.class),
					e.getInitializerInfo().getInitializer());
		}
	}

	@Test
	public void test_02_simpleDependenciesInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("sd");
		initService.register(SimpleDependencyInitializer.class);
		initService.enterPhase("sd");
		SimpleDependencyInitializer.assertCalled();
	}

	@Test
	public void test_03_multipleDependenciesInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("md1").registerPhase("md2");
		initService.register(MultipleDependenciesInitializer.class);
		initService.enterPhase("md1");
		MultipleDependenciesInitializer.assertCalled1();
		initService.enterPhase("md2");
		MultipleDependenciesInitializer.assertCalled2();
	}

	@Test
	public void test_04_crossingPhaseDependenciesInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("cp1").registerPhase("cp2");
		initService.register(CrossingPhaseInitializer.class);
		initService.enterPhase("cp1");
		CrossingPhaseInitializer.assertCalled1();
		initService.enterPhase("cp2");
		CrossingPhaseInitializer.assertCalled2();
	}

	@Test
	public void test_05_forceResolveInitializer() throws Exception {
		InitializeService initService = getInitService();
		initService.registerPhase("rp");
		initService.register(ForceResolvedInitializer.class);
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
		ForceResolvedInitializer.assertCalled();
	}
}
