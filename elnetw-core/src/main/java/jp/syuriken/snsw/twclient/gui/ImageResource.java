package jp.syuriken.snsw.twclient.gui;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/09/07
 * Time: 18:09
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class ImageResource {
	public static ImageIcon getImgFavOff() {
		return IMG_FAV_OFF;
	}

	public static ImageIcon getImgFavOn() {
		return IMG_FAV_ON;
	}

	public static ImageIcon getImgFavHover() {
		return IMG_FAV_HOVER;
	}

	public static ImageIcon getImgTwitterLogo() {
		return IMG_TWITTER_LOGO;
	}

	/** ふぁぼの星 (ふぁぼされていない時用) 32x32 */
	private static final ImageIcon IMG_FAV_OFF;
	/** ふぁぼの星 (ふぁぼされている時用) 32x32 */
	private static final ImageIcon IMG_FAV_ON;
	/** ふぁぼの星 (フォーカスが当たっている時用) 32x32 */
	private static final ImageIcon IMG_FAV_HOVER;
	/** Twitterのロゴ (青背景に白) */
	private static final ImageIcon IMG_TWITTER_LOGO;

	static {
		ClassLoader classLoader = ImageResource.class.getClassLoader();
		try {
			IMG_FAV_OFF = new ImageIcon(
					ImageIO.read(classLoader.getResource("jp/syuriken/snsw/twclient/img/fav_off32.png")));
			IMG_FAV_ON = new ImageIcon(
					ImageIO.read(classLoader.getResource("jp/syuriken/snsw/twclient/img/fav_on32.png")));
			IMG_FAV_HOVER = new ImageIcon(
					ImageIO.read(classLoader.getResource("jp/syuriken/snsw/twclient/img/fav_hover32.png")));
		} catch (IOException e) {
			throw new AssertionError("必要なリソース img/fav_{off,on,hover}32.png が読み込めませんでした");
		}
		try {
			IMG_TWITTER_LOGO =
					new ImageIcon(ImageIO.read(classLoader.getResource("com/twitter/twitter-bird-white-on-blue.png")));
		} catch (IOException e) {
			throw new AssertionError("必要なリソース Twitterのロゴ が読み込めませんでした");
		}
	}
}
