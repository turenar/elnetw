package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dynamic dependencies resolver */
public class DynamicInitializeService extends InitializeService {
	/** store initializer's information */
	protected class InitializerInfo {
		private final Method initializer;

		private final Initializer annotation;

		private final String phase;

		private final Object instance;

		private LinkedList<String> remainDependencies;

		private String[] dependencies;

		private int depCount;

		/**
		 * init
		 *
		 * @param instance    initializer will be invoked with 'instance'.
		 * @param initializer initializer method
		 * @param annotation  initializer.getAnnotation(Initializer.class)
		 */
		public InitializerInfo(Object instance, Method initializer, Initializer annotation) {
			this.initializer = initializer;
			this.instance = instance;
			this.annotation = annotation;
			this.phase = annotation.phase();
			this.dependencies = annotation.dependencies();
			LinkedList<String> remainDependencies = new LinkedList<String>();
			this.remainDependencies = remainDependencies;

			for (String dependency : dependencies) {
				if (initializedSet.contains(dependency) == false) {
					remainDependencies.add(dependency);
				}
			}
			remainDependencies.add("phase-" + phase);

			for (ListIterator<String> iterator = remainDependencies.listIterator();
				 iterator.hasNext(); ) {
				String dependency = iterator.next();
				if (initializedSet.contains(dependency)) {
					iterator.remove();
				} else {
					ArrayList<InitializerInfo> initializerInfos = initializerDependencyMap.get(dependency);
					if (initializerInfos == null) {
						initializerInfos = new ArrayList<InitializerInfo>();
						initializerDependencyMap.put(dependency, initializerInfos);
					}
					initializerInfos.add(this);
				}
			}

			this.depCount = remainDependencies.size();
		}

		/**
		 * get Initializer Annotation
		 *
		 * @return annotation
		 */
		public Initializer getAnnotation() {
			return annotation;
		}

		/**
		 * get count of dependencies which is not resolved yet.
		 *
		 * @return dependency count
		 */
		public int getDepCount() {
			return depCount;
		}

		/**
		 * get all dependencies (including resolved)
		 *
		 * @return dependencies array
		 */
		public String[] getDependencies() {
			return dependencies;
		}

		/**
		 * get initializer method
		 *
		 * @return method
		 */
		public Method getInitializer() {
			return initializer;
		}

		/**
		 * get instance to invoke initializer.
		 *
		 * @return instance or null
		 */
		public Object getInstance() {
			return instance;
		}

		/**
		 * get initializer's name
		 *
		 * @return name
		 */
		public String getName() {
			return annotation.name();
		}

		/**
		 * get initializer's phase
		 *
		 * @return phase
		 */
		public String getPhase() {
			return phase;
		}

		/**
		 * get dependencies which is not resolved yet.
		 *
		 * @return
		 */
		public LinkedList<String> getRemainDependencies() {
			return remainDependencies;
		}

		/**
		 * resolve dependency.
		 *
		 * @param name dependency's name
		 */
		public void resolveDependency(String name) {
			boolean isRemoved = remainDependencies.remove(name);
			if (isRemoved) {
				depCount--;
			}
		}

		/**
		 * invoke initializer
		 *
		 * @throws InitializeException exception occured
		 */
		public void run() throws InitializeException {
			try {
				initializer.invoke(instance);
			} catch (IllegalAccessException e) {
				logger.error("not accessible", e);
				throw new InitializeException(e);
			} catch (InvocationTargetException e) {
				logger.warn("caught exception", e);
				throw new InitializeException(e.getCause());
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(DynamicInitializeService.class);

	/**
	 * register this to {@link InitializeService}.
	 * If something is already registered, throw {@link IllegalStateException}.
	 *
	 * @return this instance
	 * @throws IllegalStateException something is already registered to InitializeService
	 */
	public static DynamicInitializeService use() throws IllegalStateException {
		DynamicInitializeService service = new DynamicInitializeService();
		InitializeService.setService(service);
		return service;
	}

	/** List of called initializer's name */
	protected final HashSet<String> initializedSet;

	/** Map of initializer's */
	protected HashMap<String, InitializerInfo> initializerInfoMap;

	/** K=name, V=List of depending on K */
	protected HashMap<String, ArrayList<InitializerInfo>> initializerDependencyMap;

	/** Queue to invoke initializer */
	protected LinkedList<InitializerInfo> initQueue;

	private DynamicInitializeService() {
		initializerInfoMap = new HashMap<String, InitializerInfo>();
		initializerDependencyMap = new HashMap<String, ArrayList<InitializerInfo>>();
		initQueue = new LinkedList<InitializerInfo>();
		initializedSet = new HashSet<String>();
	}

	@Override
	public void enterPhase(String phase) throws InitializeException {
		resolve("phase-" + phase);
		runResolvedInitializer();
	}

	@Override
	public boolean isInitialized(String name) {
		return initializedSet.contains(name);
	}

	@Override
	public void register(Class<?> initClass) {
		Field[] declaredFields = initClass.getDeclaredFields();
		Object instance = null;
		for (Field field : declaredFields) {
			InitializerInstance initializerInstance = field.getAnnotation(InitializerInstance.class);
			if (initializerInstance != null) {
				field.setAccessible(true);
				try {
					instance = field.get(null);
					break;
				} catch (IllegalAccessException e) {
					logger.warn("not accessible to " + field.getName(), e);
				}
			}
		}

		Method[] declaredMethods = initClass.getDeclaredMethods();
		for (Method method : declaredMethods) {
			Initializer initializer = method.getAnnotation(Initializer.class);
			if (initializer != null) {
				register(instance, method, initializer);
			}
		}
	}

	@Override
	public void register(Object instance, Method method) {
		Initializer initializer = method.getAnnotation(Initializer.class);
		if (initializer != null) {
			register(instance, method, initializer);
		} else {
			throw new IllegalArgumentException("method must have @Initializer annotation.");
		}
	}

	private void register(Object instance, Method method, Initializer initializer) {
		String name = initializer.name();
		InitializerInfo initializerInfo = new InitializerInfo(instance, method, initializer);
		initializerInfoMap.put(name, initializerInfo);

		if (initializerInfo.depCount <= 0) {
			initQueue.push(initializerInfo);
		}
	}

	private void resolve(String name) {
		if (initializedSet.contains(name)) {
			return;
		}

		initializedSet.add(name);

		ArrayList<InitializerInfo> infos = initializerDependencyMap.get(name);
		if (infos == null) {
			return;
		}

		for (InitializerInfo info : infos) {
			info.resolveDependency(name);
			if (info.getDepCount() <= 0) {
				initQueue.push(info);
			}
		}
	}

	private void runResolvedInitializer() throws InitializeException {
		while (initQueue.isEmpty() == false) {
			InitializerInfo info = initQueue.poll();
			info.run();
			resolve(info.getName());
		}
	}
}
