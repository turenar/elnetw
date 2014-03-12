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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Initializer Annotation.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Initializer {
	/**
	 * Specify required initializer
	 *
	 * @return initializer's name
	 */
	String[] dependencies() default {};

	/**
	 * Specify Initializer's name
	 *
	 * @return name
	 */
	String name();

	/**
	 * Init-Phase.
	 *
	 * <dl>
	 * <dt>preinit</dt><dd>Pre-initialize phase. Use this phase to load extra library etc.</dd>
	 * <dt>init</dt><dd>Initialize phase. Default phase is this.</dd>
	 * <dt>postinit</dt><dd>Post-initialize phase.</dd>
	 * <dt>prestart</dt><dd>Before showing main frame.</dd>
	 * <dt>start</dt><dd>Showing main frame phase.</dd>
	 * </dl>
	 *
	 * @return phase (preinit, init, postinit, prestart, start)
	 */
	String phase() default "init";
}
