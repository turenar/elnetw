package jp.syuriken.snsw.twclient;

import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * アニメーションをキャンセルする ImageObserver
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public final class AnimationCanceledImageObserver implements ImageObserver {
	
	/** シングルトンインスタンス */
	public static final AnimationCanceledImageObserver SINGLETON = new AnimationCanceledImageObserver();
	
	
	private AnimationCanceledImageObserver() {
	}
	
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return false; // Animation GIF is disabled.
	}
}
