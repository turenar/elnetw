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

package jp.syuriken.snsw.twclient.filter.prop;

import java.io.InputStream;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.Status;

import static org.junit.Assert.*;

/**
 * {@link StandardIntProperties}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StandardIntPropertiesTest extends FilterConstants {

	private static ClientConfigurationTestImpl configuration;

	/**
	 * テスト前に呼ばれる関数
	 *
	 * @throws Exception 例外
	 */
	@BeforeClass
	public static void tearUpClass() throws Exception {
		configuration = new ClientConfigurationTestImpl();
		ClientProperties defaultProperties = new ClientProperties();

		InputStream resourceStream = null;
		try {
			resourceStream =
					ClientConfiguration.class.getResourceAsStream("/jp/syuriken/snsw/twclient/config.properties");
			defaultProperties.load(resourceStream);
		} finally {
			if (resourceStream != null) {
				resourceStream.close();
			}
		}

		configuration.setConfigDefaultProperties(defaultProperties);
		ClientProperties properties = new ClientProperties(defaultProperties);
		properties
				.setProperty("twitter.oauth.access_token.list", STATUS_2.getUser().getId() + " " + DM_1.getSenderId());
		configuration.setConfigProperties(properties);
	}

	private static boolean testEqual(String propName, long target, DirectMessage directMessage)
			throws IllegalSyntaxException {
		return new StandardIntProperties(configuration, propName, ":", target).filter(directMessage);
	}

	private static boolean testEqual(String propName, long target, Status status) throws IllegalSyntaxException {
		return new StandardIntProperties(configuration, propName, ":", target).filter(status);
	}

	/**
	 * in_reply_to_userid のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterInReplyToUserId() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertEquals(-1, STATUS_1.getInReplyToUserId());
			assertFalse(testEqual("in_reply_to_userid", -1, STATUS_1));
			assertFalse(testEqual("in_reply_to_userid", -1, STATUS_2));
			assertTrue(testEqual("in_reply_to_userid", STATUS_2.getUser().getId(), STATUS_3));
			assertFalse(testEqual("in_reply_to_userid", -1, STATUS_4));

			assertTrue(testEqual("in_reply_to_userid", DM_1.getRecipientId(), DM_1));
			assertFalse(testEqual("in_reply_to_userid", STATUS_1.getUser().getId(), DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * rtcount のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterRtCount() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertTrue(testEqual("rtcount", STATUS_1.getRetweetCount(), STATUS_1));
			assertFalse(testEqual("rtcount", STATUS_2.getRetweetCount(), STATUS_4));
			assertFalse(testEqual("rtcount", STATUS_3.getRetweetCount(), STATUS_4));
			assertTrue(testEqual("rtcount", STATUS_4.getRetweetCount(), STATUS_4));

			assertFalse(testEqual("rtcount", 0, DM_1));
			assertFalse(testEqual("rtcount", -1, DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** 無知の名前に対するテスト */
	@Test
	public void testFilterUnknownName() {
		configuration.setGlobalInstance();
		try {
			new StandardIntProperties(configuration, "unknown unknown", "", "");
			fail("prop nameを無視してるかな？");
		} catch (IllegalSyntaxException e) {
			// do nothing
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * userid のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterUserId() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertTrue(testEqual("userid", STATUS_1.getUser().getId(), STATUS_1));
			assertFalse(testEqual("userid", STATUS_2.getUser().getId(), STATUS_1));
			assertTrue(testEqual("userid", STATUS_3.getUser().getId(), STATUS_2));
			assertTrue(testEqual("userid", STATUS_4.getUser().getId(), STATUS_4));

			assertTrue(testEqual("userid", DM_1.getSenderId(), DM_1));
			assertFalse(testEqual("userid", STATUS_1.getUser().getId(), DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}
}
