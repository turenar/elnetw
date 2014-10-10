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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.mydns.turenar.twclient.init.InitAfter;
import jp.mydns.turenar.twclient.init.InitBefore;
import jp.mydns.turenar.twclient.init.InitCondition;
import jp.mydns.turenar.twclient.init.InitDepends;
import jp.mydns.turenar.twclient.init.InitProvide;
import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.Initializer;
import jp.mydns.turenar.twclient.init.SplashScreenCtrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * initializer's info
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ class TreeInitInfo extends TreeInitInfoBase {
	private static final Logger logger = LoggerFactory.getLogger(TreeInitInfo.class);
	private final Object instance;
	private final Method method;
	private final Initializer initializer;
	private boolean forceProvided;

	public TreeInitInfo(Object instance, Method method, Initializer initializer) {
		super(initializer.name());
		this.instance = instance;
		this.method = method;
		this.initializer = initializer;
		InitDepends initDepends = method.getAnnotation(InitDepends.class);
		if (initDepends != null) {
			for (String dependsName : initDepends.value()) {
				dependencies.add(new Depend(dependsName, this, null));
			}
		}
		InitAfter initAfter = method.getAnnotation(InitAfter.class);
		if (initAfter != null) {
			for (String afterName : initAfter.value()) {
				dependencies.add(new After(afterName, this, null, true));
			}
		}
		InitBefore initBefore = method.getAnnotation(InitBefore.class);
		if (initBefore != null) {
			for (String beforeName : initBefore.value()) {
				dependencies.add(new Before(beforeName, this, null, true));
			}
		}
		InitProvide initProvide = method.getAnnotation(InitProvide.class);
		if (initProvide != null) {
			for (String provideName : initProvide.value()) {
				ProviderInitInfo provider = TreeInitializeService.instance.getProvider(provideName);
				dependencies.add(new Provide(provideName, this, provider));
			}
		}
		// update weight
		addDependency(new Depend(PhaseInitInfo.getNameFromPhase(initializer.phase()), this));
	}

	@Override
	public Initializer getAnnotation() {
		return initializer;
	}

	@Override
	public Method getInitializer() {
		return method;
	}

	@Override
	public String getPhase() {
		return initializer.phase();
	}

	@Override
	public void invoke() throws InitializeException {
		if (forceProvided) {
			isInitialized = true;
			return;
		}
		if (isInitialized) {
			logger.error("already initialized: {}", this);
			return;
		}
		if (!isAllDependenciesInitialized()) {
			throw new InitializeException("All dependencies is not initialized");
		}
		try {
			SplashScreenCtrl.setString(tr("Initializing: (%s)%s", getPhase(), name));
			logger.trace(" {}:{} weight={}", getPhase(), this, weight);
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 0) {
				method.invoke(instance);
			} else if (parameterTypes.length == 1 && InitCondition.class.isAssignableFrom(parameterTypes[0])) {
				TreeInitConditionImpl initCondition = new TreeInitConditionImpl(this, true);
				method.invoke(instance, initCondition);
				if (initCondition.isSetFailStatus()) {
					throw initCondition.getException();
				}
			} else {
				throw new InitializeException(this, "Unexpected argument method");
			}
			isInitialized = true;
		} catch (IllegalAccessException e) {
			throw new InitializeException(this, e, "Failed to invoke initializer");
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof InitializeException) {
				throw (InitializeException) cause;
			} else {
				throw new InitializeException(this, cause, "Failed to invoke initializer");
			}
		}
	}

	@Override
	public boolean isAllDependenciesResolved() {
		return forceProvided || super.isAllDependenciesResolved();
	}

	@Override
	public void provide() {
		forceProvided = true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!isAllDependenciesResolved()) {
			builder.append("<NotResolved>");
		}
		builder.append(getName())
				.append(" (")
				.append(method.getDeclaringClass().getSimpleName())
				.append("#")
				.append(method.getName())
				.append(")");
		if (forceProvided) {
			builder.append(" <skip>");
		}
		return builder.toString();
	}

	@Override
	public void uninit(boolean fastUninit) throws InitializeException {
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 0) {
				// not supported uninit
			} else if (parameterTypes.length == 1 && InitCondition.class.isAssignableFrom(parameterTypes[0])) {
				TreeInitConditionImpl initCondition = new TreeInitConditionImpl(this, false);
				method.invoke(instance, initCondition);
				logger.trace(" uninit: {}", this);
				if (initCondition.isSetFailStatus()) {
					throw initCondition.getException();
				}
			} else {
				throw new InitializeException(this, "Unexpected argument method");
			}
		} catch (IllegalAccessException e) {
			throw new InitializeException(this, e, "Failed to invoke initializer");
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof InitializeException) {
				throw (InitializeException) cause;
			} else {
				throw new InitializeException(this, cause, "Failed to invoke initializer");
			}
		}
	}
}
