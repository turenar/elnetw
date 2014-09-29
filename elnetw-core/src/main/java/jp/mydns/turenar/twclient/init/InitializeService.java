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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jp.mydns.turenar.twclient.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to initialize
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class InitializeService {

	private static final Logger logger = LoggerFactory.getLogger(InitializeService.class);
	private static InitializeService service;

	/*package*/
	static void clearService() {
		InitializeService.service = null;
	}

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

	/**
	 * set service
	 *
	 * @param service instance
	 * @throws IllegalStateException InitializeService is already registered
	 */
	protected static synchronized void setService(InitializeService service) throws IllegalStateException {
		if (InitializeService.service != null) {
			throw new IllegalStateException("InitializeService is already registered");
		}
		InitializeService.service = service;
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
	 *
	 * @return uninitialized?
	 */
	public abstract boolean isUninitialized();

	public InitializeService provideInitializer(String name) throws IllegalArgumentException {
		return provideInitializer(name, false);
	}

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
	 * register method as initializer
	 *
	 * @param instance instance for invocation. If method is static, it can be null.
	 * @param method   initializer method. It must be annotated by Initializer
	 * @return this instance
	 * @throws IllegalArgumentException {@link jp.mydns.turenar.twclient.init.Initializer} is missing
	 */
	public InitializeService register(Object instance, Method method) throws IllegalArgumentException {
		Initializer initializer = method.getAnnotation(Initializer.class);
		if (initializer != null) {
			register(instance, method, initializer);
		} else {
			throw new IllegalArgumentException("method must have @Initializer annotation.");
		}
		return this;
	}

	/**
	 * register all methods which are annotated by {@link jp.mydns.turenar.twclient.init.Initializer} in initClass
	 *
	 * @param initClass initializer class
	 * @return this instance
	 * @throws IllegalArgumentException something is wrong
	 */
	public InitializeService register(Class<?> initClass) throws IllegalArgumentException {
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
					// should not happen
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

	/**
	 * register initializer
	 *
	 * @param instance    instance for invocation. If method is static, it can be null.
	 * @param method      initializer method.
	 * @param initializer initializer annotation
	 * @throws IllegalArgumentException something is wrong
	 */
	protected abstract void register(Object instance, Method method,
			Initializer initializer) throws IllegalArgumentException;

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
	 * set job queue for parallelize
	 *
	 * @param jobQueue queue
	 */
	public abstract void setJobQueue(JobQueue jobQueue);

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
