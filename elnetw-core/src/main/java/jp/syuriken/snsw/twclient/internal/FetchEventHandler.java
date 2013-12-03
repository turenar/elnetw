/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2013 Turenai Project
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

package jp.syuriken.snsw.twclient.internal;

import java.io.IOException;
import java.net.URLConnection;

/**
 * interface for NetworkSupport
 */
public interface FetchEventHandler {
	/**
	 * コンテント長が得られた。
	 *
	 * @param contentLength 不明の場合は-1、それ以外はコンテント長
	 * @throws InterruptedException 割り込みされた
	 */
	void onContentLength(int contentLength) throws InterruptedException;

	/**
	 * IO例外が発生
	 *
	 * @param connection コネクション
	 * @param e          例外
	 */
	void onException(URLConnection connection, IOException e);

	/**
	 * 一部のデータが読み込まれた
	 *
	 * @param imageLen 現在の画像データの長さ
	 * @throws InterruptedException 割り込みされた
	 */
	void onLoaded(int imageLen) throws InterruptedException;
}