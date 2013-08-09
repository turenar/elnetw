package jp.syuriken.snsw.twclient.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import jp.syuriken.snsw.twclient.ClientFrameApi;

/**
 * アクションコマンドを指定したコンフィグタイプ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ActionButtonConfigType implements ConfigType {

	private final String buttonText;

	/*package*/final String actionCommand;

	/*package*/final ClientFrameApi frameApi;

	private final ActionListener ACTION_LISTENER = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			frameApi.handleAction(actionCommand, null);
		}
	};


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
		button.addActionListener(ACTION_LISTENER);
		return button;
	}

	@Override
	public String getValue(JComponent component) {
		return null;
	}

	@Override
	public boolean isPreferedAsMultiline() {
		return true;
	}

	@Override
	public boolean isValid(JComponent component) {
		return true;
	}
}
