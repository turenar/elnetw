package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic dependencies resolver
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DynamicInitializeService extends InitializeService {
	protected class InitConditionImpl implements InitCondition {

		private final InitializerInfoImpl initializerInfo;

		private final boolean initializingPhase;

		private InitializeException failException;

		protected InitConditionImpl(InitializerInfoImpl info, boolean isInitializingPhase) {
			this.initializerInfo = info;
			initializingPhase = isInitializingPhase;
		}

		@Override
		public void clearFailStatus() {
			failException = null;
		}

		@Override
		public ClientConfiguration getConfiguration() {
			return configuration;
		}

		protected InitializeException getException() {
			return failException;
		}

		@Override
		public boolean isInitializingPhase() {
			return initializingPhase;
		}

		@Override
		public boolean isSetFailStatus() {
			return failException != null;
		}

		@Override
		public void setFailStatus(Throwable cause, String reason, int exitCode) {
			failException = new InitializeException(initializerInfo, cause, reason, exitCode);
		}

		@Override
		public void setFailStatus(String reason, int exitCode) {
			failException = new InitializeException(initializerInfo, reason, exitCode);
		}
	}

	/** store initializer's information */
	protected class InitializerInfoImpl implements InitializerInfo {
		private final Method initializer;

		private final Initializer annotation;

		private final String phase;

		private final Object instance;

		private LinkedList<String> remainDependencies;

		private int depCount;

		private boolean isExecuted;

		private boolean uninitable;

		/**
		 * init
		 *
		 * @param instance    initializer will be invoked with 'instance'.
		 * @param initializer initializer method
		 * @param annotation  initializer.getAnnotation(Initializer.class)
		 */
		public InitializerInfoImpl(Object instance, Method initializer, Initializer annotation) {
			this.initializer = initializer;
			this.instance = instance;
			this.annotation = annotation;
			this.phase = annotation.phase();
			if (!phaseSet.contains(phase)) {
				logger.warn("QA: {} has unknown phase: {}", toString(), phase);
			}
			LinkedList<String> remainDependencies = new LinkedList<String>();
			this.remainDependencies = remainDependencies;

			String[] dependencies = annotation.dependencies();
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
					ArrayList<InitializerInfoImpl> initializerInfos = initializerDependencyMap.get(dependency);
					if (initializerInfos == null) {
						// initializer is hardly depended by over 10 initializers
						initializerInfos = new ArrayList<InitializerInfoImpl>(1);
						initializerDependencyMap.put(dependency, initializerInfos);
					}
					initializerInfos.add(this);
				}
			}

			this.depCount = remainDependencies.size();

			Class<?>[] parameterTypes = initializer.getParameterTypes();
			if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(InitCondition.class)) {
				uninitable = true;
			}
		}

		@Override
		public Initializer getAnnotation() {
			return annotation;
		}

		@Override
		public int getDepCount() {
			return depCount;
		}

		@Override
		public String[] getDependencies() {
			return annotation.dependencies();
		}

		@Override
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

		@Override
		public String getName() {
			return annotation.name();
		}

		@Override
		public String getPhase() {
			return phase;
		}

		@Override
		public LinkedList<String> getRemainDependencies() {
			return remainDependencies;
		}

		public boolean isUninitable() {
			return uninitable;
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
		 * @throws InitializeException exception occurred
		 */
		public void run(boolean initializePhase) throws InitializeException {
			if (initializePhase && isExecuted) {
				logger.error("BUG:{} is already initialized", this);
				return;
			}
			if (!initializePhase) { // un-initialize phase
				if (!isExecuted) {
					logger.error("BUG:{} is to be uninitialized, but not executed", this);
					return;
				} else if (!isUninitable()) {
					return;
				} else {
					logger.trace(" uninit: {}", this);
				}
			}

			try {
				Class<?>[] parameterTypes = initializer.getParameterTypes();
				int parameterCount = parameterTypes.length;
				if (parameterCount == 0) {
					if (initializePhase) {
						try {
							initializer.invoke(instance);
						} catch (NullPointerException e) {
							throw new InitializeException(this, e, "initializer is not static method!!");
						}
					} // if no argument, method cannot determine to be initializing
				} else if (parameterCount == 1 && parameterTypes[0].isAssignableFrom(InitCondition.class)) {
					InitConditionImpl initCondition = new InitConditionImpl(this, initializePhase);
					try {
						initializer.invoke(instance, initCondition);
					} catch (NullPointerException e) {
						throw new InitializeException(this, e, "initializer is not static method!!");
					}
					if (initCondition.isSetFailStatus()) {
						throw initCondition.getException();
					}
				} else {
					throw new InitializeException(this, "Registered exception has non-usable initializer");
				}
			} catch (IllegalAccessException e) {
				throw new InitializeException(this, e, "not accessible for " + initializer.getName());
			} catch (InvocationTargetException e) {
				throw new InitializeException(this, e.getCause(), null);
			}
			isExecuted = true;
		}

		public String toString() {
			return getName() + " (" + initializer.getDeclaringClass().getSimpleName() + "#" + initializer.getName()
					+ ")";
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
	public static DynamicInitializeService use(ClientConfiguration configuration) throws IllegalStateException {
		DynamicInitializeService service = new DynamicInitializeService(configuration);
		InitializeService.setService(service);
		return service;
	}

	/** configuration */
	protected final ClientConfiguration configuration;

	/** List of called initializer's name */
	protected final HashSet<String> initializedSet;

	/** Map of initializer's */
	protected HashMap<String, InitializerInfoImpl> initializerInfoMap;

	/** K=name, V=List of depending on K */
	protected HashMap<String, ArrayList<InitializerInfoImpl>> initializerDependencyMap;

	/** Queue to invoke initializer */
	protected LinkedList<InitializerInfoImpl> initQueue;

	/** stack to invoke de-initializer */
	protected Stack<InitializerInfoImpl> uninitStack;

	private HashSet<String> phaseSet = new HashSet<String>();

	private DynamicInitializeService(ClientConfiguration configuration) {
		this.configuration = configuration;
		initializerInfoMap = new HashMap<String, InitializerInfoImpl>();
		initializerDependencyMap = new HashMap<String, ArrayList<InitializerInfoImpl>>();
		initQueue = new LinkedList<InitializerInfoImpl>();
		initializedSet = new HashSet<String>();
		uninitStack = new Stack<InitializerInfoImpl>();
	}

	private void ensureNotCalledUninit() {
		if (uninitStack == null) {
			throw new IllegalStateException("Already #uninit() is called!");
		}
	}

	@Override
	public synchronized InitializeService enterPhase(String phase) throws InitializeException {
		ensureNotCalledUninit();

		logger.info("Entering {} phase", phase);
		resolve("phase-" + phase);
		runResolvedInitializer();
		return this;
	}

	@Override
	public InitializerInfo getInfo(String name) {
		return initializerInfoMap.get(name);
	}

	@Override
	public synchronized boolean isInitialized(String name) {
		ensureNotCalledUninit();

		return initializedSet.contains(name);
	}

	@Override
	public synchronized boolean isRegistered(String name) {
		ensureNotCalledUninit();

		return initializerInfoMap.containsKey(name);
	}

	@Override
	public InitializeService provideInitializer(String name) throws IllegalArgumentException {
		return provideInitializer(name, false);
	}

	@Override
	public InitializeService provideInitializer(String name, boolean force) {
		if (!force && initializerInfoMap.containsKey(name)) {
			throw new IllegalArgumentException(name + " is already registered as initializer");
		}

		resolve(name);
		return this;
	}

	@Override
	public synchronized InitializeService register(Object instance, Method method) throws IllegalArgumentException {
		Initializer initializer = method.getAnnotation(Initializer.class);
		if (initializer != null) {
			register(instance, method, initializer);
		} else {
			throw new IllegalArgumentException("method must have @Initializer annotation.");
		}
		return this;
	}

	@Override
	public synchronized InitializeService register(Class<?> initClass) throws IllegalArgumentException {
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
		return this;
	}

	private void register(Object instance, Method method, Initializer initializer) throws IllegalArgumentException {
		ensureNotCalledUninit();

		String name = initializer.name();
		if (initializerInfoMap.containsKey(name)) {
			throw new IllegalArgumentException("\'" + name + "\' is already registered");
		} else if (initializedSet.contains(name)) {
			throw new IllegalArgumentException("\'" + name + "\' is already provided");
		}

		InitializerInfoImpl initializerInfo = new InitializerInfoImpl(instance, method, initializer);
		initializerInfoMap.put(name, initializerInfo);

		if (initializerInfo.depCount <= 0) {
			initQueue.push(initializerInfo);
		}
	}

	@Override
	public InitializeService registerPhase(String phase) {
		phaseSet.add(phase);
		return this;
	}

	private void resolve(String name) {
		if (initializedSet.contains(name)) {
			return;
		}

		initializedSet.add(name);

		ArrayList<InitializerInfoImpl> infos = initializerDependencyMap.get(name);
		if (infos == null) {
			return;
		}

		for (InitializerInfoImpl info : infos) {
			info.resolveDependency(name);
			if (info.getDepCount() <= 0) {
				initQueue.push(info);
			}
		}
	}

	private void runResolvedInitializer() throws InitializeException {
		while (initQueue.isEmpty() == false) {
			InitializerInfoImpl info = initQueue.poll();
			logger.trace(" {}:{}", info.getPhase(), info);
			info.run(true);
			uninitStack.push(info);
			resolve(info.getName());
		}
	}

	@Override
	public void uninit() throws InitializeException {
		if (uninitStack == null) {
			throw new IllegalStateException("already uninitialized");
		}

		logger.info("Starting uninitializing");
		while (uninitStack.isEmpty() == false) {
			InitializerInfoImpl info = uninitStack.pop();
			info.isUninitable();
			info.run(false);
		}
		uninitStack = null;
	}
}
