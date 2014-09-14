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

package jp.mydns.turenar.twclient.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientFrameApi;
import jp.mydns.turenar.twclient.intent.IntentArguments;

/**
 * アクションコマンドを指定したコンフィグタイプ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ActionButtonConfigType implements ConfigType {

	private final String buttonText;
	/*package*/final String actionCommand;
	private final ActionListener actionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			ClientConfiguration.getInstance().handleAction(new IntentArguments(actionCommand));
		}
	};
	/*package*/final ClientFrameApi frameApi;


	/**
	 * インスタンスを生成する。
	 *
	 * @param buttonText    ボタンに表示するテキスト
	 * @param actionCommand アクションコマンド
	 * @param frameApi      フレーム操作用API
	 */
	public ActionButtonConfigType(String buttonText, String actionCommand, ClientFrameApi frameApi) {
		this.buttonText = buttonText;
		this.actionCommand = actionCommand;
		this.frameApi = frameApi;
	}

	@Override
	public JComponent getComponent(String configKey, String nowValue, ConfigFrame listener) {
		JButton button = new JButton(buttonText);
		button.setActionCommand(actionCommand);
		button.addActionListener(actionListener);
		return button;
	}

	@Override
	public String getValue(JComponent component) {
		return null;
	}

	@Override
	public boolean isPreferredAsMultiline() {
		return true;
	}

	@Override
	public boolean isValid(JComponent component) {
		return true;
	}
}
