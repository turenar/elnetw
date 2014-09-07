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
	 * enter phase and run initializers which dependencies is resolved
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
	 * otherwise, return null
	 */
	public abstract InitializerInfo getInfo(String name);

	/**
	 * check initializer which has [name] is initialized
	 * <p>
	 * This method checks only <i>name</i> is resolved.
	 * There is no way to check if THIS-FUNC worked.
	 * </p>
	 *
	 * @param name initializer's name
	 * @return if initializer which has specified name is initialized, return true.
	 * otherwise, return false.
	 */
	public abstract boolean isInitialized(String name);

	/**
	 * check initializer which has specified name is already registered.
	 *
	 * @param name initializer name
	 * @return registered?
	 */
	public abstract boolean isRegistered(String name);

	/**
	 * check if service is uninitialized. If this function returns true,
	 * other functions which do some operation may throw {@link java.lang.IllegalStateException}
	 * @return uninitialized?
	 */
	public abstract boolean isUninitialized();

	/**
	 * provide null-initializer as name
	 *
	 * <p>This method don't run initializer. If you want this to run, you can call {@link #waitConsumeQueue()}</p>
	 *
	 * @param name initializer's name
	 * @return this object
	 * @throws IllegalArgumentException name is already registered
	 * @see #waitConsumeQueue()
	 */
	public abstract InitializeService provideInitializer(String name) throws IllegalArgumentException;

	/**
	 * provide null-initializer as name
	 *
	 * <p>This method don't run initializer. If you want this to run, you can call {@link #waitConsumeQueue()}</p>
	 *
	 * @param name  initializer's name
	 * @param force if true, do not check whether 'name' is already registered.
	 *              otherwise, check and throw IllegalArgumentException when 'name' is already registered.
	 * @return this object
	 * @throws IllegalArgumentException name is already registered
	 * @see #waitConsumeQueue()
	 */
	public abstract InitializeService provideInitializer(String name, boolean force);

	/**
	 * register initializer
	 *
	 * <p>This method don't run initializer. If you want this to run, you can call {@link #waitConsumeQueue()}</p>
	 *
	 * @param instance instance to invoke instance-method
	 * @param method   method object
	 * @return this object
	 * @throws java.lang.IllegalArgumentException method don't have @Initializer
	 */
	public abstract InitializeService register(Object instance, Method method) throws IllegalArgumentException;

	/**
	 * register initializer
	 *
	 * <p>This method don't run initializer. If you want this to run, you can call {@link #waitConsumeQueue()}</p>
	 *
	 * @param initClass class object. initClass is not needed to have {@link InitProviderClass} Annotation.
	 * @return this object
	 * @throws java.lang.IllegalArgumentException wrong class
	 */
	public abstract InitializeService register(Class<?> initClass) throws IllegalArgumentException;

	/**
	 * register phase.
	 *
	 * <p>This method don't run initializer. If you want this to run, you can call {@link #waitConsumeQueue()}</p>
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
	public void uninit() throws InitializeException {
		uninit(false);
	}

	/**
	 * un-initialize initializer
	 *
	 * <p>Once called this method, calling all other methods is act indefinitely</p>
	 *
	 * @param fastUninit fast uninit? This should be false.
	 * @throws InitializeException exception occurred.
	 */
	public abstract void uninit(boolean fastUninit) throws InitializeException;

	/**
	 * wait for init-queue consumed
	 *
	 * @return this object
	 * @throws IllegalStateException                              {@link #uninit()} is already called
	 * @throws jp.mydns.turenar.twclient.init.InitializeException error in initializer
	 */
	public abstract InitializeService waitConsumeQueue() throws IllegalStateException, InitializeException;
}
