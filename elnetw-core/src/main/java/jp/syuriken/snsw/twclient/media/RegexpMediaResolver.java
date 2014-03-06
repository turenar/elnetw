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

package jp.syuriken.snsw.twclient.media;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.syuriken.snsw.twclient.internal.FetchEventHandler;
import jp.syuriken.snsw.twclient.internal.NetworkSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 汎用的な、&lt;img&gt;のsrc属性からImage URLを取得するリゾルバ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RegexpMediaResolver implements MediaUrlResolver {
	private static class MyFetchEventHandler implements FetchEventHandler {

		private String contentEncoding;

		public String getContentEncoding() {
			return contentEncoding;
		}

		@Override
		public void onConnection(URLConnection connection) throws InterruptedException {
			contentEncoding = connection.getContentEncoding();
		}

		@Override
		public void onException(URLConnection connection, IOException e) {
			logger.warn("fetch", e);
		}

		@Override
		public void onLoaded(int imageLen) throws InterruptedException {
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(RegexpMediaResolver.class);
	private final Pattern regexp;

	/**
	 * インスタンスを作成する
	 *
	 * @param regexp 画像URL正規表現
	 */
	public RegexpMediaResolver(String regexp) {
		this.regexp = Pattern.compile("<img[^>]+src=[\"']?(" + regexp + ")[\"']?");
	}

	private String getContentsFromUrl(URL mediaUrl) throws IOException, InterruptedException {
		MyFetchEventHandler handler = new MyFetchEventHandler();
		byte[] contents = NetworkSupport.fetchContents(mediaUrl, handler);

		Charset charset = Charset.forName("UTF-8");
		try {
			String encoding = handler.getContentEncoding();
			if (encoding != null) {
				charset = Charset.forName(encoding);
			}
		} catch (UnsupportedCharsetException e) {
			logger.warn("Invalid Charset", e);
		}
		return new String(contents, charset);
	}

	@Override
	public UrlInfo getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		URL mediaUrl;
		try {
			mediaUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		String contents = getContentsFromUrl(mediaUrl);
		Matcher matcher = regexp.matcher(contents);
		if (matcher.find()) {
			return new UrlInfo(matcher.group(1), false, true);
		} else {
			return null;
		}
	}
}
