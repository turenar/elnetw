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

package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * information of @{@link Initializer}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface InitializerInfo {
	/**
	 * get Initializer Annotation
	 *
	 * @return annotation
	 */
	Initializer getAnnotation();

	/**
	 * get count of dependencies which is not resolved yet.
	 *
	 * @return dependency count
	 */
	int getDepCount();

	/**
	 * get all dependencies (including resolved)
	 *
	 * @return dependencies array
	 */
	String[] getDependencies();

	/**
	 * get initializer method
	 *
	 * @return method
	 */
	Method getInitializer();

	/**
	 * get initializer's name
	 *
	 * @return name
	 */
	String getName();

	/**
	 * get initializer's phase
	 *
	 * @return phase
	 */
	String getPhase();

	/**
	 * get dependencies which is not resolved yet.
	 *
	 * @return dependencies which is not resolved yet
	 */
	LinkedList<String> getRemainDependencies();
}
