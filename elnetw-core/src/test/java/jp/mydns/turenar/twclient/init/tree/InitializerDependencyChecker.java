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

import java.lang.reflect.Method;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import org.slf4j.LoggerFactory;

/**
 * User test for {@link jp.mydns.turenar.twclient.init.tree.TreeInitializeService}
 */
public class InitializerDependencyChecker extends TreeInitializeService {
	public static void main(String[] args) throws ClassNotFoundException, InitializeException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();

		InitializerDependencyChecker initializer = new InitializerDependencyChecker();
		TreeInitializeService.instance = initializer;
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
					System.out.print("not initialized: \033[01;31m" + initializer.getInfo(annotation.name()) + "\033[00m");

					StringBuilder builder = new StringBuilder("\033[34m dependencies=[");
					TreeInitInfoBase info = initializer.infoMap.get(annotation.name());
					for (Relation relation : info.dependencies) {
						if (relation.getWeight() == UNRESOLVED_WEIGHT) {
							builder.append("\033[01;31m").append(relation.getTargetName()).append("\033[00;34m")
									.append(", ");
						} else if (relation.getWeight() != 0) {
							builder.append(relation.getTargetName())
									.append(", ");
						}
					}
					builder.setLength(builder.length() - 2);
					builder.append("]\033[00m");
					System.out.println(builder.toString());
				}
			}
		}
	}

	private String nowPhase;

	private InitializerDependencyChecker() {
		super();
	}

	@Override
	public synchronized InitializeService enterPhase(String phase) throws InitializeException {
		nowPhase = phase;
		return super.enterPhase(phase);
	}

	@Override
	public synchronized InitializeService waitConsumeQueue() throws IllegalStateException, InitializeException {
		assertNotUninit();
		do {
			rebuildTree();
			treeRebuildRequired = false;
			TreeInitInfoBase info;
			while ((info = flatTree.next()) != null) {
				if (info instanceof PhaseInitInfo) {
					System.out.println("\033[01;31m" + info.getName() + "\033[00m");
					continue;
				}
				for (int i = 1; i < info.weight; i++) {
					System.out.print(" ");
				}
				StringBuilder builder = new StringBuilder();
				builder.append(info instanceof ProviderInitInfo ? "\033[01;36m" : "\033[01;35m")
						.append(info.getName()).append("\033[00m");
				String phase = info.getPhase();
				if (!(phase == null || phase.equals(nowPhase))) {
					builder.append("\033[01;32m@").append(phase).append("\033[00m");
				}

				int actualDependenciesCount = 0;
				for (Relation relation : info.dependencies) {
					if (relation.getWeight() != 0) {
						actualDependenciesCount++;
					}
				}
				if (actualDependenciesCount != 0) {
					builder.append("\033[34m dependencies=[");
					for (Relation relation : info.dependencies) {
						if (relation.getWeight() != 0) {
							builder.append(relation.getTargetName())
									.append(", ");
						}
					}
					builder.setLength(builder.length() - 2);
					builder.append("]\033[00m");
				}
				System.out.println(builder.toString());
				if (info instanceof TreeInitInfo) {
					info.isInitialized = true;
				}
				if (treeRebuildRequired) {
					break; // back to rebuild tree
				}
			}
		} while (treeRebuildRequired);
		return this;
	}
}
