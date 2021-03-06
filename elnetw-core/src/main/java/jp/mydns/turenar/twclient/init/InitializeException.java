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
 * exception when initializing
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InitializeException extends Exception {
	private static final long serialVersionUID = -4813918292243379594L;
	private final String reason;
	private final int exitCode;
	private /*final*/ InitializerInfo initializerInfo;

	/**
	 * init
	 *
	 * @param cause cause
	 */
	public InitializeException(Throwable cause) {
		this(null, cause);
	}

	/**
	 * init
	 *
	 * @param initializerInfo info
	 * @param reason          cause reason
	 */
	public InitializeException(InitializerInfo initializerInfo, String reason) {
		this(initializerInfo, null, reason);
	}

	/**
	 * init
	 *
	 * @param initializerInfo info
	 * @param reason          cause reason
	 * @param exitCode        application exit code
	 */
	public InitializeException(InitializerInfo initializerInfo, String reason, int exitCode) {
		this(initializerInfo, null, reason, exitCode);
	}

	/**
	 * init
	 *
	 * @param message message
	 */
	public InitializeException(String message) {
		super(message);
		reason = message;
		exitCode = -1;
	}

	/**
	 * init
	 *
	 * @param initializerInfo info
	 * @param cause           causedBy
	 * @param reason          cause reason
	 */
	public InitializeException(InitializerInfo initializerInfo, Throwable cause,
			String reason) {
		this(initializerInfo, cause, reason, -1);
	}

	/**
	 * init
	 *
	 * @param initializerInfo info
	 * @param cause           causedBy
	 * @param reason          cause reason
	 * @param exitCode        application exit code
	 */
	public InitializeException(InitializerInfo initializerInfo, Throwable cause,
			String reason, int exitCode) {
		super(reason, cause);
		this.initializerInfo = initializerInfo;
		this.reason = reason;
		this.exitCode = exitCode;
	}

	/**
	 * init
	 *
	 * @param message message
	 * @param cause   causedBy
	 */
	public InitializeException(String message, Throwable cause) {
		reason = cause.getLocalizedMessage();
		exitCode = -1;
	}

	/**
	 * get application exit code
	 *
	 * @return exit code
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * get info
	 *
	 * @return info
	 */
	public InitializerInfo getInitializerInfo() {
		return initializerInfo;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage() + "\ninitializer:" + initializerInfo;
	}

	/**
	 * get cause reason
	 *
	 * @return reason
	 */
	public String getReason() {
		return reason;
	}

	/*package*/void setInitializerInfo(InitializerInfo info) {
		initializerInfo = info;
	}
}
