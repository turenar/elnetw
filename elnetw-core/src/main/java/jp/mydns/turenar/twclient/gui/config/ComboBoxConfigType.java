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

package jp.mydns.turenar.twclient.gui.config;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import jp.mydns.turenar.twclient.Utility;

import static java.util.AbstractMap.SimpleEntry;

/**
 * 数値を設定するタイプ。JSpinnerを使用する。(正しくない文字列の場合はJLabel)
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ComboBoxConfigType implements ConfigType {
	private final class FocusAdapterExtension extends FocusAdapter {

		private final String configKey;
		private final JComboBox<SimpleEntryImpl> component;
		private final ConfigFrame listener;


		private FocusAdapterExtension(String configKey, JComboBox<SimpleEntryImpl> component, ConfigFrame listener) {
			this.configKey = configKey;
			this.component = component;
			this.listener = listener;
		}

		@Override
		public void focusLost(FocusEvent e) {
			listener.setValue(configKey, String.valueOf(getValue(component)));
		}
	}

	private class SimpleEntryImpl extends SimpleEntry<String, String> {
		private static final long serialVersionUID = -4030435257677306806L;

		public SimpleEntryImpl(String viewText, String confValue) {
			super(viewText, confValue);
		}

		@Override
		public String toString() {
			return getKey();
		}
	}

	/**
	 * entry list
	 */
	protected ArrayList<SimpleEntryImpl> entries = new ArrayList<>();

	/**
	 * add item for combo box
	 *
	 * @param viewText  text for user interface
	 * @param confValue value for configuration
	 * @return this instance
	 */
	public ComboBoxConfigType addItem(String viewText, String confValue) {
		entries.add(new SimpleEntryImpl(viewText, confValue));
		return this;
	}

	@Override
	public JComponent getComponent(String configKey, String nowValue, ConfigFrame listener) {
		JComboBox<SimpleEntryImpl> comboBox = new JComboBox<>();
		DefaultComboBoxModel<SimpleEntryImpl> comboBoxModel = new DefaultComboBoxModel<>();
		comboBox.setModel(comboBoxModel);

		int selectingIndex = 0;
		for (int i = 0; i < entries.size(); i++) {
			SimpleEntryImpl entry = entries.get(i);
			comboBoxModel.addElement(entry);
			if (entry.getValue().equals(nowValue)) {
				selectingIndex = i;
			}
		}
		comboBox.setSelectedIndex(selectingIndex);
		comboBox.addFocusListener(new FocusAdapterExtension(configKey, comboBox, listener));
		return comboBox;
	}

	@Override
	public String getValue(JComponent component) {
		if (component instanceof JComboBox) {
			JComboBox<SimpleEntryImpl> comboBox = Utility.uncheckedCast(component);
			return comboBox.getModel().getElementAt(comboBox.getSelectedIndex()).getValue();
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public boolean isPreferredAsMultiline() {
		return false;
	}

	@Override
	public boolean isValid(JComponent component) {
		if (component instanceof JComboBox) {
			return true;
		} else {
			throw new AssertionError();
		}
	}
}
