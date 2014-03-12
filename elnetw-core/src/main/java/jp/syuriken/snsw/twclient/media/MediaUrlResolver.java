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

package jp.syuriken.snsw.twclient.media;

import java.io.IOException;

/**
 * Media URL Resolver
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MediaUrlResolver {
	/**
	 * urlを解決する
	 *
	 * @param url 解決するURL
	 * @return 解決済みURL (=短縮されていない、画像ファイルURLなど)
	 * @throws IllegalArgumentException urlとして正しくない
	 * @throws InterruptedException     スレッドをブロックを必要とする処理中に割り込まれた
	 * @throws IOException              解決中にIO例外が発生した
	 */
	UrlInfo getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException;
}
