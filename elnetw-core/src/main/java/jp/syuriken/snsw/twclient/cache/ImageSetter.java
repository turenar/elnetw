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

package jp.syuriken.snsw.twclient.cache;

import java.awt.Image;

import jp.syuriken.snsw.twclient.internal.ConnectionInfo;

/**
 * ImageSetter: 画像を取得したあとのハンドラ。通常、AbstractImageSetterをextendsするのがいいと思います。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ImageSetter {
	/**
	 * 子にImageSetterを追加する。順番はあまり関係ない。
	 *
	 * @param setter イメージセッター。null不可。
	 */
	void addSetter(ImageSetter setter);

	/**
	 * 次のImageSetter。
	 *
	 * @return ImageSetterインスタンス or null
	 */
	ImageSetter next();

	/**
	 * 例外が発生した。
	 *
	 * @param e              例外
	 * @param connectionInfo コネクション情報
	 */
	void onException(Exception e, ConnectionInfo connectionInfo);

	/**
	 * このImageSetterのみに画像を設定する。JLabelのsetImageなど。
	 *
	 * @param image イメージ
	 */
	void setImage(Image image);

	/**
	 * 子ImageSetterを含めて画像を設定する。
	 *
	 * @param image イメージ
	 */
	void setImageRecursively(Image image);

	/**
	 * 子setterを設定する。
	 *
	 * @param next 子setter
	 */
	void setNext(ImageSetter next);
}
