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

package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.util.Date;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.syuriken.snsw.twclient.Utility;
import twitter4j.TwitterException;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;

/**
 * Render object for exception
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ExceptionRenderObject extends AbstractRenderObject {

	private final Date date;
	private final Exception ex;
	private final String uniqId;

	public ExceptionRenderObject(SimpleRenderer simpleRenderer, Exception ex) {
		super(simpleRenderer);
		this.ex = ex;
		date = new Date();
		uniqId = "!exception/" + ex.getClass().getName() + "/" + date;
	}

	@Override
	public void focusGained(FocusEvent e) {
		super.focusGained(e);
		Throwable handlingException = ex;
		StringBuilder stringBuilder = new StringBuilder("<html>").append(ex.getLocalizedMessage()).append("<br><br>");
		while (null != (handlingException = handlingException.getCause())) {
			stringBuilder.append("Caused by ").append(handlingException.toString()).append("<br>");
		}
		StringBuilder escaped = escapeHTML(stringBuilder);

		getFrameApi().setTweetViewText(escaped.toString(), null, DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedAt(Utility.getDateString(date, true), null,
				DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedBy(componentUserIcon.getIcon(), ex.getClass().getName(), null,
				DO_NOTHING_WHEN_POINTED);
	}

	@Override
	public Object getBasedObject() {
		return ex;
	}

	@Override
	public String getCreatedBy() {
		return "!exception/" + ex.getClass().getName();
	}

	@Override
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public Date getDate() {
		return date;
	}

	@Override
	public String getUniqId() {
		return uniqId;
	}

	@Override
	protected void initComponents() {
		foregroundColor = Color.RED;
		backgroundColor = Color.BLACK;
		componentSentBy.setText("!ERROR!");
		String exString;
		if (ex instanceof TwitterException) {
			TwitterException twex = (TwitterException) ex;
			if (twex.isCausedByNetworkIssue()) {
				exString = twex.getCause().toString();
			} else {
				exString = twex.getStatusCode() + ": " + twex.getErrorMessage();
			}
		} else {
			exString = ex.toString();
		}
		if (exString.length() > 256) {
			exString = new StringBuilder().append(exString, 0, 256 - 2).append("..").toString();
		}
		componentStatusText.setText(exString);
	}
}
