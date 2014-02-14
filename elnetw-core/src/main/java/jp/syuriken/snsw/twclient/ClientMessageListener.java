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

package jp.syuriken.snsw.twclient;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.UserStreamListener;

/**
 * 入出力データをディスパッチするためのクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ClientMessageListener extends UserStreamListener, ConnectionLifeCycleListener, ClientEventConstants {

	/**
	 * アカウント変更
	 *
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。falseは読み込み用
	 */
	void onChangeAccount(boolean forWrite);

	/**
	 * core等が発する情報をキャッチする。この関数は自由に使えます。
	 *
	 * @param name リクエスト名。この名前で区別するのでできるだけFQCNなどで記述すると衝突の可能性が少なくなります。
	 * @param arg  引数。Stringが投げられると過信してはいけません。
	 */
	void onClientMessage(String name, Object arg);
}
