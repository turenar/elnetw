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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * 数値を設定するタイプ。JSpinnerを使用する。(正しくない文字列の場合はJLabel)
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class IntegerConfigType implements ConfigType {

	private final class FocusAdapterExtension extends FocusAdapter {

		private final String configKey;
		private final JComponent component;
		private final ConfigFrame listener;


		private FocusAdapterExtension(String configKey, JComponent component, ConfigFrame listener) {
			this.configKey = configKey;
			this.component = component;
			this.listener = listener;
		}

		@Override
		public void focusLost(FocusEvent e) {
			listener.setValue(configKey, String.valueOf(getValue(component)));
		}
	}

	private final int minimum;
	private final int maximum;
	private final int multiplier;


	/**
	 * インスタンスを生成する。
	 *
	 * @param maximum 最大値
	 * @param minimum 最小値
	 */
	public IntegerConfigType(int maximum, int minimum) {
		this(maximum, minimum, 1);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param minimum    最大値
	 * @param maximum    最小値
	 * @param multiplier 倍数。秒をミリ秒に変換するときは1000を指定する。
	 */
	public IntegerConfigType(int minimum, int maximum, int multiplier) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.multiplier = multiplier;
	}

	@Override
	public JComponent getComponent(String configKey, String nowValue, ConfigFrame listener) {
		try {
			JSpinner spinner =
					new JSpinner(new SpinnerNumberModel(Integer.parseInt(nowValue) / multiplier, minimum, maximum, 1));
			spinner.addFocusListener(new FocusAdapterExtension(configKey, spinner, listener));
			return spinner;
		} catch (NumberFormatException e) {
			final JTextArea textArea = new JTextArea("[不正な値:数値である必要があります] " + nowValue);
			textArea.addFocusListener(new FocusAdapterExtension(configKey, textArea, listener));
			return textArea;
		}
	}

	@Override
	public String getValue(JComponent component) {
		if (component instanceof JSpinner) {
			return String.valueOf(((JSpinner) component).getValue());
		} else if (component instanceof JLabel) {
			return String.valueOf(Integer.parseInt(((JLabel) component).getText().trim()));
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public boolean isPreferedAsMultiline() {
		return false;
	}

	@Override
	public boolean isValid(JComponent component) {
		if (component instanceof JSpinner) {
			return true;
		} else if (component instanceof JLabel) {
			try {
				Integer.parseInt(((JLabel) component).getText().trim());
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			throw new AssertionError();
		}
	}
}
