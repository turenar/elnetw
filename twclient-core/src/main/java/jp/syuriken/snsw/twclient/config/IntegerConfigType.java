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
 * @author $Author$
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
	 * @param maximum 最大値
	 * @param minimum 最小値
	 */
	public IntegerConfigType(int maximum, int minimum) {
		this(maximum, minimum, 1);
	}
	
	/**
	 * インスタンスを生成する。
	 * @param minimum 最大値
	 * @param maximum 最小値
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
