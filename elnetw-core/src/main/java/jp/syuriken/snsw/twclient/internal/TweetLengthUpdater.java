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

package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;

/**
 * ツイートの長さを更新するAPI
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface TweetLengthUpdater {

	/**
	 * ツイートの長さを計算するクラスを設定する。
	 *
	 * @param newCalculator 新しいインスタンス
	 * @return 前設定されていたクラス。
	 */
	TweetLengthCalculator setTweetLengthCalculator(TweetLengthCalculator newCalculator);

	/**
	 * ポストの長さを示すラベルを更新する
	 *
	 * @param length  長さを表す文字列。int文字列である必要はありません
	 * @param color   前景色
	 * @param tooltip ツールチップ
	 */
	void updatePostLength(String length, Color color, String tooltip);
}
