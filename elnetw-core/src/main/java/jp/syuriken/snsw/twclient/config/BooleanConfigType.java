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

package jp.syuriken.snsw.twclient.config;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 真偽値を設定するタイプ。JCheckBoxを使用
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class BooleanConfigType implements ConfigType {

	@Override
	public JComponent getComponent(final String configKey, String nowValue, final ConfigFrame listener) {
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(Boolean.parseBoolean(nowValue));
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				listener.setValue(configKey, String.valueOf(checkBox.isSelected()));
			}
		});
		return checkBox;
	}

	@Override
	public String getValue(JComponent component) {
		if (component instanceof JCheckBox == false) {
			throw new AssertionError();
		}
		return String.valueOf(((JCheckBox) component).isSelected());
	}

	@Override
	public boolean isPreferedAsMultiline() {
		return false;
	}

	@Override
	public boolean isValid(JComponent component) {
		return true;
	}
}
