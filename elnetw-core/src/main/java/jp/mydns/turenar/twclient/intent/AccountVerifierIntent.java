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

import javax.swing.JOptionPane;

import jp.mydns.turenar.twclient.ClientConfiguration;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * アカウント認証するアクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AccountVerifierIntent implements Intent {
	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
		// do nothing
	}

	@Override
	public void handleAction(IntentArguments args) {
		final ClientConfiguration configuration = ClientConfiguration.getInstance();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Exception exception = configuration.tryGetOAuthToken();
				if (exception != null) {
					JOptionPane.showMessageDialog(configuration.getFrameApi().getFrame(),
							tr("Failed authentication: %s", exception.getMessage()), tr("Error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}, "oauth-thread");
		thread.start();
	}
}
