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

package jp.mydns.turenar.twclient.intent;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.render.RenderObject;
import jp.mydns.turenar.twclient.twitter.TwitterStatus;
import twitter4j.Status;
import twitter4j.User;

/**
 * abstract action intent which uses status argument
 */
public abstract class AbstractIntent implements Intent {

	/**
	 * get string @&lt;screenName&gt; (&lt;name&gt;)
	 *
	 * @param user user
	 * @return string
	 */
	protected static String getUserString(User user) {
		return "@" + user.getScreenName() + " (" + user.getName() + ")";
	}

	/**
	 * configuration
	 */
	protected final ClientConfiguration configuration;

	/**
	 * init
	 */
	public AbstractIntent() {
		configuration = ClientConfiguration.getInstance();
	}

	/**
	 * get argument for arg name
	 *
	 * @param arguments    argument
	 * @param argName      name
	 * @param defaultValue value
	 * @return boolean
	 * @throws IllegalArgumentException arg is not Boolean or String
	 */
	protected boolean getBoolean(IntentArguments arguments, String argName, boolean defaultValue)
			throws IllegalArgumentException {
		Object obj = arguments.getExtra(argName);
		if (obj == null) {
			return defaultValue;
		} else if (obj instanceof Boolean) {
			return (Boolean) obj;
		} else if (obj instanceof String) {
			String str = (String) obj;
			switch (str) {
				case "y":
				case "yes":
				case "t":
				case "true":
					return true;
				case "n":
				case "no":
				case "f":
				case "false":
					return false;
				default:
					return defaultValue;
			}
		} else {
			throw new IllegalArgumentException("argument[" + argName + "] is not valid boolean");
		}
	}

	/**
	 * get login user id
	 *
	 * @return user id
	 */
	protected long getLoginUserId() {
		return Long.parseLong(configuration.getAccountIdForRead());
	}

	/**
	 * get status from intent arguments
	 *
	 * @param arguments arguments
	 * @return status or null
	 */
	protected TwitterStatus getStatus(IntentArguments arguments) {
		RenderObject renderObject = arguments.getExtraObj(INTENT_ARG_NAME_SELECTING_POST_DATA,
				RenderObject.class);
		Status status = null;

		if (renderObject == null) {
			status = arguments.getExtraObj("status", Status.class);
		} else {
			Object tag = renderObject.getBasedObject();
			if (tag instanceof Status) {
				status = (Status) tag;
			}
		}

		return status == null ? null : TwitterStatus.getInstance(status);
	}

	/**
	 * throw illegal argument
	 *
	 * @throws IllegalArgumentException error
	 */
	protected void throwIllegalArgument() throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Specify arg `status`(Status) or `" + Intent.INTENT_ARG_NAME_SELECTING_POST_DATA
						+ "`(RenderPanel)"
		);
	}
}
