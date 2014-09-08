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

package jp.mydns.turenar.twclient.gui.tab;

import javax.swing.JComponent;

/**
 * factory for client tab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ClientTabFactory {
	/**
	 * get instance from uniq id
	 *
	 * @param tabId  tab id
	 * @param uniqId uniq id
	 * @return restored tab
	 */
	ClientTab getInstance(String tabId, String uniqId);

	/**
	 * get name of tab
	 *
	 * @return name
	 */
	String getName();

	/**
	 * get component for tab specific configuration
	 *
	 * @return component or null. null shows no specific configuration
	 */
	JComponent getOtherConfigurationComponent();

	/**
	 * get priority for adding tab menu. smaller will be above larger
	 */
	int getPriority();

	/**
	 * create tab
	 *
	 * @param tabId                       tab id
	 * @param accountId                   account id (String)
	 * @param otherConfigurationComponent {@link #getOtherConfigurationComponent()}. if it is null,
	 *                                    implementer must ignore this arg.
	 * @return tab instance or null. null shows factory handles adding tab
	 */
	ClientTab newTab(String tabId, String accountId, JComponent otherConfigurationComponent);
}
