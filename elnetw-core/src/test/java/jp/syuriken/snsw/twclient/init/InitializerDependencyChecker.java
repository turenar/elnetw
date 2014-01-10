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
				.registerPhase("start");
		for (String arg : args) {
			initializer.register(Class.forName(arg));
		}
		initializer.enterPhase("earlyinit")
				.enterPhase("preinit")
				.enterPhase("init")
				.enterPhase("postinit")
				.enterPhase("prestart")
				.enterPhase("start");
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
