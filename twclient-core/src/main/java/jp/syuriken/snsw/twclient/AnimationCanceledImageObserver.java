package jp.syuriken.snsw.twclient;

import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * アニメーションをキャンセルする ImageObserver
 * 
 * @author $Author$
 */
public final class AnimationCanceledImageObserver implements ImageObserver {
	
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return false; // Animation GIF is disabled.
	}
}