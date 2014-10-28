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

package jp.mydns.turenar.twclient.gui.tab.factory;

import javax.swing.JComponent;

import jp.mydns.turenar.twclient.gui.tab.ClientTab;
import jp.mydns.turenar.twclient.gui.tab.ClientTabFactory;
import jp.mydns.turenar.twclient.gui.tab.UpdateProfileTab;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * factory for UpdateProfileTab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UpdateProfileTabFactory implements ClientTabFactory {

	public static final int PRIORITY = 0x03000000;

	@Override
	public ClientTab getInstance(String tabId, String uniqId) {
		return new UpdateProfileTab(tabId, uniqId);
	}

	@Override
	public String getName() {
		return tr("Update profile");
	}

	@Override
	public JComponent getOtherConfigurationComponent() {
		return null;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public ClientTab newTab(String tabId, String accountId, JComponent otherConfigurationComponent) {
		return null;
	}
}
