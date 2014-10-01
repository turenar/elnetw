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

package jp.mydns.turenar.twclient.media;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.internal.FetchEventHandler;
import jp.mydns.turenar.twclient.internal.NetworkSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provide getContentsFromUrl
 */
public abstract class AbstractMediaResolver implements MediaUrlResolver {
	private static class MyFetchEventHandler implements FetchEventHandler {

		private String contentEncoding;
		private IOException exception;

		public String getContentEncoding() {
			return contentEncoding;
		}

		public IOException getException() {
			return exception;
		}

		@Override
		public void onConnection(URLConnection connection) throws InterruptedException {
			String contentType = connection.getContentType();
			int indexOf = contentType.indexOf(";charset=");
			contentEncoding = indexOf < 0 ? null : contentType.substring(indexOf + ";charset=".length());
		}

		@Override
		public void onException(URLConnection connection, IOException e) {
			exception = e;
			logger.warn("fetch", e);
		}

		@Override
		public void onLoaded(int imageLen) throws InterruptedException {
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(RegexpMediaResolver.class);

	public static String getContentsFromUrl(URL mediaUrl) throws IOException, InterruptedException {
		MyFetchEventHandler handler = new MyFetchEventHandler();
		byte[] contents = NetworkSupport.fetchContents(mediaUrl, handler);
		if (handler.getException() != null) {
			throw handler.getException();
		}

		Charset charset = ClientConfiguration.UTF8_CHARSET;
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
}
