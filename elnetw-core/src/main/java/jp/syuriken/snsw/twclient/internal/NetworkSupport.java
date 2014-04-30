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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import com.squareup.okhttp.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network fetch etc.
 */
public class NetworkSupport {
	private static class NullFetchEventHandler implements FetchEventHandler {
		@Override
		public void onConnection(URLConnection connection) throws InterruptedException {

		}

		@Override
		public void onException(URLConnection connection, IOException e) {
		}

		@Override
		public void onLoaded(int imageLen) {
		}
	}

	private static final int BUFFER_SIZE = 65536;
	private static final Logger logger = LoggerFactory.getLogger(NetworkSupport.class);
	public static final NullFetchEventHandler NULL_FETCH_EVENT_HANDLER = new NullFetchEventHandler();
	private static final OkHttpClient httpClient = new OkHttpClient();

	private static byte[] copyOfRange(byte[] data, int imageLen) {
		return data.length == imageLen ? data : Arrays.copyOfRange(data, 0, imageLen);
	}

	/**
	 * URLからデータを取得する
	 *
	 * @param url URL
	 * @return データ
	 */
	public static byte[] fetchContents(URL url) throws InterruptedException {
		return fetchContents(url, NULL_FETCH_EVENT_HANDLER);
	}

	/**
	 * URLからデータを取得する
	 *
	 * @param url     URL
	 * @param handler ハンドラ
	 * @return データ
	 */
	public static byte[] fetchContents(URL url, FetchEventHandler handler) throws InterruptedException {
		URLConnection connection = null;
		InputStream stream = null;
		try {
			if (url.getProtocol().startsWith("https")) {
				connection = httpClient.open(url);
			} else {
				connection = url.openConnection();
			}
			int contentLength = connection.getContentLength();
			handler.onConnection(connection);

			stream = connection.getInputStream();

			/*
			Local File URL InputStream read(buf, start, 0) always returns 0
			Http URL InputStream read(buf, start, 0) returns 0 or -1
			Should InputStream have isEOF()?
			*/
			int bufLength = contentLength < 0 ? BUFFER_SIZE : contentLength;
			byte[] data = new byte[bufLength];
			int imageLen = 0;
			int loadLen;
			while ((loadLen = stream.read(data, imageLen, bufLength - imageLen)) != -1) {
				imageLen += loadLen;

				if (loadLen == 0 && bufLength == imageLen) {
					int nextByte = stream.read();
					if (nextByte == -1) {
						break;
					} else {
						bufLength = bufLength << 1;
						if (bufLength < 0) {
							bufLength = Integer.MAX_VALUE;
						}
						byte[] newData = new byte[bufLength];
						System.arraycopy(data, 0, newData, 0, imageLen);
						newData[imageLen++] = (byte) nextByte;
						data = newData;
					}
				}

				handler.onLoaded(imageLen);
				logger.trace("Image: Loaded {} bytes: buffer {}/{}", loadLen, imageLen, bufLength);

				Thread.sleep(1);
			}
			return copyOfRange(data, imageLen);
		} catch (IOException e) {
			handler.onException(connection, e);
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					logger.error("fail closing stream", e);
				}
			}
		}
	}
}
