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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.LoggerFactory;

/**
 * User test for {@link jp.syuriken.snsw.twclient.init.DynamicInitializeService}
 */
public class InitializerDependencyChecker extends DynamicInitializeService {
	public static void main(String[] args) throws ClassNotFoundException, InitializeException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		InitializerDependencyChecker initializer = new InitializerDependencyChecker(ClientConfiguration.getInstance());
		initializer.registerPhase("earlyinit")
				.registerPhase("preinit")
				.registerPhase("init")
				.registerPhase("postinit")
				.registerPhase("prestart")
				.registerPhase("start")
				.registerPhase("poststart");
		for (String arg : args) {
			initializer.register(Class.forName(arg));
		}
		initializer.enterPhase("earlyinit")
				.enterPhase("preinit")
				.enterPhase("init")
				.enterPhase("postinit")
				.enterPhase("prestart")
				.enterPhase("start")
				.enterPhase("poststart");
		for (String arg : args) {
			Class<?> clazz = Class.forName(arg);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				Initializer annotation = method.getAnnotation(Initializer.class);
				if (!(annotation == null || initializer.isInitialized(annotation.name()))) {
					System.out.println("not initialized: " + initializer.getInfo(annotation.name()));
				}
			}
		}
	}

	private String nowPhase;

	private InitializerDependencyChecker(ClientConfiguration configuration) {
		super(configuration);
	}

	@Override
	public synchronized InitializeService enterPhase(String phase) throws InitializeException {
		nowPhase = phase;
		System.out.println("@@ phase: " + phase);
		return super.enterPhase(phase);
	}

	private void getDeepDependencyCount(Set<InitializerInfoImpl> set, InitializerInfoImpl info) {
		set.add(info);
		for (String dep : info.getDependencies()) {
			getDeepDependencyCount(set, initializerInfoMap.get(dep));
		}
	}

	private int getDeepDependencyCount(InitializerInfoImpl info) {
		Set<InitializerInfoImpl> set = new HashSet<>();
		getDeepDependencyCount(set, info);
		return set.size() - 1;
	}

	@Override
	protected void runResolvedInitializer() throws InitializeException {
		while (!initQueue.isEmpty()) {
			InitializerInfoImpl info = initQueue.pop();
			int length = getDeepDependencyCount(info);
			for (int i = 0; i < length; i++) {
				System.out.print("  ");
			}
			System.out.print(info.getName());
			System.out.println(info.getPhase().equals(nowPhase) ? "" : "@" + info.getPhase());
			uninitStack.push(info);
			resolve(info.getName());
		}
	}
}
