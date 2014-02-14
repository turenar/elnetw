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

package jp.syuriken.snsw.twclient.config;

import javax.swing.JComponent;

/**
 * コンポーネントとかを生成するクラスインターフェース。設定用。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ConfigType {

	/**
	 * コンポーネントを取得する。値が変更されたかどうかは実装元が判断してlistenerに投げること
	 *
	 * @param configKey 設定キー
	 * @param nowValue  現在の値
	 * @param listener  リスナ
	 * @return コンポーネント
	 */
	JComponent getComponent(String configKey, String nowValue, ConfigFrame listener);

	/**
	 * 指定されたコンポーネントの値を取得する。
	 *
	 * @param component コンポーネント
	 * @return 値
	 */
	String getValue(JComponent component);

	/**
	 * 複数行 (説明とコンポーネントを別の行) にしたほうがいいかを返す。
	 *
	 * @return 複数行にしてもらいときはtrue
	 */
	boolean isPreferedAsMultiline();

	/**
	 * 正しいあたいかどうかを判定する
	 *
	 * @param component コンポーネント
	 * @return 正しいかどうか
	 */
	boolean isValid(JComponent component);
}
