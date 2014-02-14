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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * twitpicのImage URLを取得するプロバイダー
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RegexpMediaResolver extends AbstractMediaUrlResolver {

	private static final int BUFSIZE = 65536;
	private final Pattern regexp;

	public RegexpMediaResolver(String regexp) {
		this.regexp = Pattern.compile("<img[^>]+src=[\"']?(" + regexp + ")[\"']?");
	}

	private String getContentsFromUrl(URL mediaUrl) throws IOException, InterruptedException {
		int bufLength;
		byte[] data;
		URLConnection connection = mediaUrl.openConnection();
		int contentLength = connection.getContentLength();
		InputStream stream = connection.getInputStream();

		bufLength = contentLength < 0 ? BUFSIZE : contentLength + 1;
		data = new byte[bufLength];
		int imageLen = 0;
		int loadLen;
		while ((loadLen = stream.read(data, imageLen, bufLength - imageLen)) != -1) {
			imageLen += loadLen;

			if (bufLength == imageLen) {
				bufLength = bufLength << 1;
				if (bufLength < 0) {
					bufLength = Integer.MAX_VALUE;
				}
				byte[] newData = new byte[bufLength];
				System.arraycopy(data, 0, newData, 0, imageLen);
				data = newData;
			}

			synchronized (this) {
				try {
					wait(1);
				} catch (InterruptedException e) {
					throw e;
				}
			}
		}
		stream.close(); // help keep-alive

		return new String(data, 0, imageLen);
	}

	@Override
	public String getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		URL mediaUrl;
		try {
			mediaUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		String contents = getContentsFromUrl(mediaUrl);
		Matcher matcher = regexp.matcher(contents);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
}
