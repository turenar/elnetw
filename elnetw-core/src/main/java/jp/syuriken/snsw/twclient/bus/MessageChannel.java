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

package jp.syuriken.snsw.twclient.bus;

import jp.syuriken.snsw.twclient.ClientMessageListener;

/**
 * Twitterからデータを取得して他のハンドラに渡すためのインターフェース
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MessageChannel {
	/**
	 * このDataFetcherが使用されるため、コネクションの準備をしてください。
	 *
	 * <p>
	 * このメソッドが呼ばれるのはClientTabが復元される前なので、
	 * このメソッドでのハンドラに渡すようなデータを取得するためのREST APIの使用は避けるべき。
	 * </p>
	 *
	 * @see #realConnect()
	 */
	void connect();

	/**
	 * このDataFetcherが使用されなくなったので、もう通知する必要がなくなった。
	 * <p>
	 * accountIdが
	 * {@link MessageBus#READER_ACCOUNT_ID} {@link MessageBus#WRITER_ACCOUNT_ID}で、
	 * 読み込み用/書き込み用アカウントが変更された時もこのメソッドが呼び出されます。
	 * この場合は続いて {@link #connect()} {@link #realConnect()}(アプリケーションの初期化が完了しているのみ)が呼び出されますが、
	 * 呼ばれた理由がアカウント変更であるかを取得できるメソッドは現在のところありません。
	 * </p>
	 */
	void disconnect();

	/**
	 * 新しく接続された
	 *
	 * @param listener リスナ
	 */
	void establish(ClientMessageListener listener);

	/**
	 * このDataFetcherが使用されるので、データの取得を開始してください。
	 *
	 * <p>
	 * このメソッドが呼ばれるのはClientTabが復元されたあとなので、
	 * このメソッドでREST APIを使用するのは一向に構わない。(ただしスレッドをブロックしないよう注意)
	 * </p>
	 */
	void realConnect();
}
