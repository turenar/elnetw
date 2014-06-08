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

/**
 * Url Info
 */
public class UrlInfo {
	private final String resolvedUrl;
	private final boolean isMediaFile;
	private boolean shouldRecursive;

	public UrlInfo(String url) {
		this(url, false, false);
	}

	/**
	 * インスタンスの作成
	 *
	 * @param resolvedUrl     解決済みURL。null不可
	 * @param shouldRecursive もう一度resolveするべきかどうか。bit.ly等はtrueであるべき。逆にjpg等はfalseであるのが望ましい。
	 * @param isMediaFile     画像ファイルかどうか。
	 */
	public UrlInfo(String resolvedUrl, boolean shouldRecursive, boolean isMediaFile) {
		this.resolvedUrl = resolvedUrl;
		this.shouldRecursive = shouldRecursive;
		this.isMediaFile = isMediaFile;
	}

	/**
	 * 解決済みURLを返す。
	 *
	 * @return 解決済みURL。検索に使ったURLと同一でも泣かない。
	 */
	public String getResolvedUrl() {
		return resolvedUrl;
	}

	/**
	 * 画像ファイルかどうかを返す。
	 *
	 * @return 画像ファイルかどうか。
	 */
	public boolean isMediaFile() {
		return isMediaFile;
	}

	/**
	 * もう一度resolveするべきかどうかを返す。
	 *
	 * @return trueの場合、{@link UrlResolverManager}は内部でもう一度{@link UrlResolverManager#getUrl(String)}を呼び出す。
	 */
	public boolean shouldRecursive() {
		return shouldRecursive;
	}

	@Override
	public String toString() {
		return "UrlInfo {resolvedUrl=" + resolvedUrl + ",shouldRecursive=" + shouldRecursive
				+ ",isMediaFile=" + isMediaFile + "}";
	}
}
