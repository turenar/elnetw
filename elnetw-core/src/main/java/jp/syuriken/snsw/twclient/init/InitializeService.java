package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Method;

/**
 * Utility to initialize
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
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
	 * @return this object
	 * @throws InitializeException exception occurred. this shows something is not initialized successfully
	 */
	public abstract InitializeService enterPhase(String phase) throws InitializeException;

	/**
	 * get initializer info of specified name. if not registered, return null
	 *
	 * @param name initializer's name
	 * @return if initializer which has specified name is registered, return {@link InitializerInfo} of that.
	 *         otherwise, return null
	 */
	public abstract InitializerInfo getInfo(String name);

	/** check initializer which has [name] is initialized */
	public abstract boolean isInitialized(String name);

	/**
	 * check initializer which has specified name is already registered.
	 * @param name initializer name
	 * @return registered?
	 */
	public abstract boolean isRegistered(String name);

	/**
	 * provide null-initializer as name
	 * @param name initializer's name
	 * @return this object
	 * @throws IllegalArgumentException name is already registered
	 */
	public abstract InitializeService provideInitializer(String name) throws IllegalArgumentException;

	/**
	 * provide null-initializer as name
	 *
	 * @param name  initializer's name
	 * @param force if true, do not check whether 'name' is already registered.
	 *              otherwise, check and throw IllegalArgumentException when 'name' is already registered.
	 * @return this object
	 * @throws IllegalArgumentException name is already registered
	 */
	public abstract InitializeService provideInitializer(String name, boolean force);

	/**
	 * register initializer
	 *
	 * @param instance instance to invoke instance-method
	 * @param method   method object
	 * @return this object
	 */
	public abstract InitializeService register(Object instance, Method method) throws IllegalArgumentException;

	/**
	 * register initializer
	 *
	 * @param initClass class object. initClass is not needed to have {@link InitProviderClass} Annotation.
	 * @return this object
	 */
	public abstract InitializeService register(Class<?> initClass) throws IllegalArgumentException;

	/**
	 * register phase.
	 *
	 * @param phase the name of phase.
	 * @return this object
	 */
	public abstract InitializeService registerPhase(String phase);

	/**
	 * un-initialize initializer
	 *
	 * <p>Once called this method, calling all other methods is act indefinitely</p>
	 *
	 * @throws InitializeException exception occurred.
	 */
	public abstract void uninit() throws InitializeException;
}