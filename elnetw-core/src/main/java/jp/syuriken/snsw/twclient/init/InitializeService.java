package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Method;

/** Utility to initialize */
public abstract class InitializeService {

	private static InitializeService service;

	/**
	 * get InitializeService
	 *
	 * @return instance
	 * @throws IllegalStateException InitializeService is not registered yet
	 */
	public static synchronized InitializeService getService() throws IllegalStateException {
		InitializeService service = InitializeService.service;
		if (service == null) {
			throw new IllegalStateException("InitializeService is not registered yet");
		}
		return service;
	}

	/*package*/
	static synchronized void setService(InitializeService service) throws IllegalStateException {
		if (InitializeService.service != null) {
			throw new IllegalStateException("InitializeService is already registered");
		}
		InitializeService.service = service;
	}

	protected InitializeService() {
	}

	/**
	 * enter phase.
	 *
	 * @param phase phase name
	 * @throws InitializeException exception occured. this shows something is not initialized successfully
	 */
	public abstract void enterPhase(String phase) throws InitializeException;

	/** check initializer which has [name] is initialized */
	public abstract boolean isInitialized(String name);

	/**
	 * check initializer which has specified name is already registered.
	 * @param name initializer name
	 * @return registered?
	 */
	public abstract boolean isRegistered(String name);

	/**
	 * register initializer
	 *
	 * @param initClass class object. initClass is not needed to have {@link InitProviderClass} Annotation.
	 */
	public abstract void register(Class<?> initClass) throws IllegalArgumentException;

	/**
	 * register initializer
	 *
	 * @param instance instance to invoke instance-method
	 * @param method   method object
	 */
	public abstract void register(Object instance, Method method) throws IllegalArgumentException;
}
