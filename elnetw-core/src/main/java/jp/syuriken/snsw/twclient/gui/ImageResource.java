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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.gui;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Public Image Resources Holder
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class ImageResource {

	private static final ImageIcon IMG_FAV_OFF;

	private static final ImageIcon IMG_FAV_ON;

	private static final ImageIcon IMG_FAV_HOVER;

	private static final ImageIcon IMG_TWITTER_LOGO;

	private static final URL URL_IMAGE_FILE_ICON;

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
		URL_IMAGE_FILE_ICON = classLoader.getResource("jp/syuriken/snsw/twclient/img/img_icon.png");
	}

	/** ふぁぼの星 (フォーカスが当たっている時用) 32x32 */
	public static ImageIcon getImgFavHover() {
		return IMG_FAV_HOVER;
	}

	/** ふぁぼの星 (ふぁぼされていない時用) 32x32 */
	public static ImageIcon getImgFavOff() {
		return IMG_FAV_OFF;
	}

	/** ふぁぼの星 (ふぁぼされている時用) 32x32 */
	public static ImageIcon getImgFavOn() {
		return IMG_FAV_ON;
	}

	/** Twitterのロゴ (青背景に白) */
	public static ImageIcon getImgTwitterLogo() {
		return IMG_TWITTER_LOGO;
	}

	/** 画像ファイルアイコンURL */
	public static URL getUrlImageFileIcon() {
		return URL_IMAGE_FILE_ICON;
	}
}
