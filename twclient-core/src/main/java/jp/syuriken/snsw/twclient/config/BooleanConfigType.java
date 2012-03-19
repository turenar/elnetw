package jp.syuriken.snsw.twclient.config;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 真偽値を設定するタイプ。JCheckBoxを使用
 * 
 * @author $Author$
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
		return String.valueOf(((JCheckBox) component).isSelected());
	}
	
	@Override
	public boolean isValid(JComponent component) {
		return true;
	}
	
}
