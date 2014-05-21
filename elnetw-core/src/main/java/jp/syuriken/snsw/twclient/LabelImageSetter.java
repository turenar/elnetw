package jp.syuriken.snsw.twclient;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
* LabelImageSetter: ImageSetterのJLabel対応版。
*
* @author Turenar (snswinhaiku dot lo at gmail dot com)
*/
public class LabelImageSetter extends AbstractImageSetter {
	private JLabel label;

	public LabelImageSetter(JLabel label) {
		this.label = label;
	}

	@Override
	public void setImage(Image image) {
		ImageIcon icon = new ImageIcon(image);
		icon.setImageObserver(AnimationCanceledImageObserver.SINGLETON);
		label.setIcon(icon);
	}

}
