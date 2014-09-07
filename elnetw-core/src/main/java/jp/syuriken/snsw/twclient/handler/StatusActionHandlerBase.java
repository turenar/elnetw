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

package jp.syuriken.snsw.twclient.handler;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.twitter.TwitterStatus;
import twitter4j.Status;

/**
 * abstract action handler which uses status argument
 */
public abstract class StatusActionHandlerBase implements ActionHandler {

	protected final ClientConfiguration configuration;

	/**
	 * init
	 */
	public StatusActionHandlerBase() {
		configuration = ClientConfiguration.getInstance();
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

		return TwitterStatus.getInstance(status);
	}


	/**
	 * throw illegal argument
	 *
	 * @throws IllegalArgumentException error
	 */
	protected void throwIllegalArgument() throws IllegalArgumentException {
		throw new IllegalArgumentException(
				"Specify arg `status`(Status) or `" + ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA
						+ "`(RenderPanel)"
		);
	}
}
