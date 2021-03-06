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

package jp.mydns.turenar.twclient.internal;

import jp.mydns.turenar.twclient.ClientConfigurationTestImpl;
import jp.mydns.turenar.twclient.bus.MessageBus;
import org.junit.Test;

import static jp.mydns.turenar.twclient.internal.DefaultTweetLengthCalculator.DEFAULT_SHORT_URL_LENGTH;
import static jp.mydns.turenar.twclient.internal.DefaultTweetLengthCalculator.DEFAULT_SHORT_URL_LENGTH_HTTPS;
import static org.junit.Assert.*;

/**
 * {@link DefaultTweetLengthCalculator}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DefaultTweetLengthCalculatorTest {

	private static final String TEXT6 = "http: https: t.co";
	private static final String TEXT5 = getString(140);
	private static final String TEXT4 =
			"http://example.com/aaa?aaa=aaa.aaa&poet=1aaa https://turetwcl.googlecode.com/svn/app/trunk/twclient-core/README.txt";
	private static final String TEXT3 = "http://t.co http://t.co";
	private static final String TEXT2 = getString(120) + "https://t.co";
	private static final String TEXT1 = getString(120) + "http://t.co";

	private static String getString(int width) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < width; i++) {
			stringBuilder.append('あ');
		}
		return stringBuilder.toString();
	}

	private final ClientConfigurationTestImpl configuration;


	public DefaultTweetLengthCalculatorTest() {
		configuration = new ClientConfigurationTestImpl();
		configuration.setGlobalInstance();
		try {
			configuration.setMessageBus(new MessageBus() {
				@Override
				public void init() {
				}
			});
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** {@link DefaultTweetLengthCalculator#getShortenedText(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testGetShortenedText() {
		configuration.setGlobalInstance();
		try {
			DefaultTweetLengthCalculator.clearApiConfiguration();
			DefaultTweetLengthCalculator lengthCalculator = new DefaultTweetLengthCalculator(
					new TweetLengthUpdaterImpl());
			assertEquals(TEXT1, lengthCalculator.getShortenedText(TEXT1));
			assertEquals(TEXT2, lengthCalculator.getShortenedText(TEXT2));
			assertEquals(TEXT3, lengthCalculator.getShortenedText(TEXT3));
			assertEquals(TEXT4, lengthCalculator.getShortenedText(TEXT4));
			assertEquals(TEXT5, lengthCalculator.getShortenedText(TEXT5));
			assertEquals(TEXT6, lengthCalculator.getShortenedText(TEXT6));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** {@link DefaultTweetLengthCalculator#calcTweetLength(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testGetTweetLength() {
		configuration.setGlobalInstance();
		try {
			// apiConfigurationの初期化
			DefaultTweetLengthCalculator.clearApiConfiguration();
			assertEquals(120 + DEFAULT_SHORT_URL_LENGTH, DefaultTweetLengthCalculator.getTweetLength(TEXT1));
			assertEquals(120 + DEFAULT_SHORT_URL_LENGTH_HTTPS, DefaultTweetLengthCalculator.getTweetLength(TEXT2));
			assertEquals(1 + (DEFAULT_SHORT_URL_LENGTH * 2), DefaultTweetLengthCalculator.getTweetLength(TEXT3));
			assertEquals(1 + DEFAULT_SHORT_URL_LENGTH + DEFAULT_SHORT_URL_LENGTH_HTTPS,
					DefaultTweetLengthCalculator.getTweetLength(TEXT4));
			assertEquals(140, DefaultTweetLengthCalculator.getTweetLength(TEXT5));
			assertEquals(17, DefaultTweetLengthCalculator.getTweetLength(TEXT6));
		} finally {
			configuration.clearGlobalInstance();
		}
	}
}
