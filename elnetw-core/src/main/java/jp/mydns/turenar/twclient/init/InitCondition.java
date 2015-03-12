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

/**
 * Information for @{@link Initializer}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface InitCondition {

	/** clear fail status */
	void clearFailStatus();

	/**
	 * get if should be uninit fast? fast uninit should not wait for blocking operation, such as stream finalize
	 *
	 * @return fast uninit?
	 */
	boolean isFastUninit();

	/**
	 * check this invocation is initializing stage
	 *
	 * @return if true, initializing. otherwise, de-initializing.
	 */
	boolean isInitializingPhase();

	/**
	 * Check already called {@link #setFailStatus(String, int)}
	 *
	 * @return whether fail status is set
	 */
	boolean isSetFailStatus();

	/**
	 * !isFastUninit(): application can take much time for terminating
	 *
	 * @return negated fast uninit.
	 */
	boolean isSlowUninit();

	/**
	 * set status as fail
	 *
	 * @param cause    cause of failure
	 * @param reason   fail reason
	 * @param exitCode exit code
	 */
	void setFailStatus(Throwable cause, String reason, int exitCode);

	/**
	 * set status as fail
	 *
	 * @param reason   fail reason
	 * @param exitCode exit code
	 */
	void setFailStatus(String reason, int exitCode);
}
