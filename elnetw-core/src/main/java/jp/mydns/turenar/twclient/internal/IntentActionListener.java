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

package jp.mydns.turenar.twclient.internal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.intent.IntentArguments;

/**
 * actionPerformedでIntentArgumentをhandleするActionListener
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class IntentActionListener implements ActionListener {
	private final IntentArguments intentArguments;

	/**
	 * create instance
	 *
	 * @param intentName intent "name"
	 */
	public IntentActionListener(String intentName) {
		intentArguments = new IntentArguments(intentName);
	}

	/**
	 * create instance
	 *
	 * @param intentArguments intent argument
	 */
	public IntentActionListener(IntentArguments intentArguments) {
		this.intentArguments = intentArguments;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/* StatusData statusData;
		 if (selectingPost == null) {
		statusData = null;
		} else {
		statusData = statusMap.get(selectingPost.getRenderObject().id);
		} */
		ClientConfiguration.getInstance().handleAction(intentArguments);
	}

	/**
	 * put extra message into intent argument
	 *
	 * @param name name
	 * @param arg  argument
	 * @return this instance
	 */
	public IntentActionListener putExtra(String name, Object arg) {
		intentArguments.putExtra(name, arg);
		return this;
	}
}
