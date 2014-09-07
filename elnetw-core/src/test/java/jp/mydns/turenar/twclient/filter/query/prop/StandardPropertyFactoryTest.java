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

package jp.mydns.turenar.twclient.filter.query.prop;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientConfigurationTestImpl;
import jp.mydns.turenar.twclient.ClientProperties;
import jp.mydns.turenar.twclient.filter.FilterConstants;
import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.Status;

import static org.junit.Assert.*;

/**
 * {@link jp.mydns.turenar.twclient.filter.query.prop.StandardPropertyFactory}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StandardPropertyFactoryTest extends FilterConstants {

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

		InputStream stream = null;
		try {
			stream = ClientConfiguration.class.getResourceAsStream("/jp/mydns/turenar/twclient/config.properties");
			InputStreamReader reader = new InputStreamReader(stream, ClientConfiguration.UTF8_CHARSET);
			defaultProperties.load(reader);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		configuration.setConfigDefaultProperties(defaultProperties);
		ClientProperties properties = new ClientProperties(defaultProperties);
		List<String> list = properties.getList(ClientConfiguration.PROPERTY_ACCOUNT_LIST);
		list.add(String.valueOf(STATUS_2.getUser().getId()));
		list.add(String.valueOf(DM_1.getSenderId()));
		configuration.setConfigProperties(properties);
	}

	public static boolean testEqual(String propName, String target, DirectMessage directMessage)
			throws IllegalSyntaxException {
		return StandardPropertyFactory.SINGLETON.getInstance(null, propName, ":", target).filter(directMessage);
	}

	public static boolean testEqual(String propName, String target, Status status) throws IllegalSyntaxException {
		return StandardPropertyFactory.SINGLETON.getInstance(null, propName, ":", target).filter(status);
	}

	public static boolean testEqual(String propName, long target, DirectMessage directMessage)
			throws IllegalSyntaxException {
		return StandardPropertyFactory.SINGLETON.getInstance(null, propName, ":", target).filter(directMessage);
	}

	public static boolean testEqual(String propName, long target, Status status) throws IllegalSyntaxException {
		return StandardPropertyFactory.SINGLETON.getInstance(null, propName, ":", target).filter(status);
	}

	public static boolean testIs(String propName, DirectMessage directMessage) throws IllegalSyntaxException {
		return StandardPropertyFactory.SINGLETON.getInstance(null, propName, "?", false).filter(directMessage);
	}

	public static boolean testIs(String propName, Status status) throws IllegalSyntaxException {
		return StandardPropertyFactory.SINGLETON.getInstance(null, propName, "?", false).filter(status);
	}

	/**
	 * client のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterClient() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertTrue(testEqual("client", "*elnetw*", STATUS_5));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * status のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testIs("dm", STATUS_1));
			assertFalse(testIs("dm", STATUS_2));
			assertFalse(testIs("dm", STATUS_3));
			assertFalse(testIs("dm", STATUS_4));
			assertFalse(testIs("dm", STATUS_5));

			assertTrue(testIs("dm", DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * has_hashtag のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterHasHashtag() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testEqual("has_hashtag", "てす", STATUS_1));
			assertFalse(testEqual("has_hashtag", "てす", STATUS_2));
			assertTrue(testEqual("has_hashtag", "てす", STATUS_3));
			assertFalse(testEqual("has_hashtag", "#てす", STATUS_3));
			assertFalse(testEqual("has_hashtag", "てす", STATUS_4));
			assertFalse(testEqual("has_hashtag", "てす", STATUS_5));

			assertFalse(testEqual("has_hashtag", "てす", DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * has_url のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterHasUrl() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testEqual("has_url", "http://example.com", STATUS_1));
			assertFalse(testEqual("has_url", "http://example.com", STATUS_2));
			assertTrue(testEqual("has_url", "http://example.com", STATUS_3));
			assertFalse(testEqual("has_url", "*t.co*", STATUS_3));
			assertFalse(testEqual("has_url", "http://example.com", STATUS_4));
			assertFalse(testEqual("has_url", "http://example.com", STATUS_5));

			assertFalse(testEqual("has_url", "*t.co*", DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
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
			assertTrue(testEqual("in_reply_to_userid", -1, STATUS_1));
			assertTrue(testEqual("in_reply_to_userid", -1, STATUS_2));
			assertTrue(testEqual("in_reply_to_userid", STATUS_2.getUser().getId(), STATUS_3));
			assertTrue(testEqual("in_reply_to_userid", -1, STATUS_4));

			assertTrue(testEqual("in_reply_to_userid", DM_1.getRecipientId(), DM_1));
			assertFalse(testEqual("in_reply_to_userid", STATUS_1.getUser().getId(), DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * mine のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterMine() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testIs("mine", STATUS_1));
			assertTrue(testIs("mine", STATUS_2));
			assertTrue(testIs("mine", STATUS_3));
			assertFalse(testIs("mine", STATUS_4));
			assertTrue(testIs("mine", DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * protected のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterProtected() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testIs("protected", STATUS_1));
			assertFalse(testIs("protected", STATUS_2));
			assertFalse(testIs("protected", STATUS_3));
			assertFalse(testIs("protected", STATUS_4));
			assertTrue(testIs("protected", STATUS_5));

			assertFalse(testIs("protected", DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * retweeted のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterRetweeted() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testIs("retweeted", STATUS_1));
			assertFalse(testIs("retweeted", STATUS_2));
			assertFalse(testIs("retweeted", STATUS_3));
			assertTrue(testIs("retweeted", STATUS_4));

			assertFalse(testIs("retweeted", DM_1));
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

			assertTrue(testEqual("rtcount", 0, DM_1));
			assertFalse(testEqual("rtcount", -1, DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * status のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertTrue(testIs("status", STATUS_1));
			assertTrue(testIs("status", STATUS_2));
			assertTrue(testIs("status", STATUS_3));
			assertTrue(testIs("status", STATUS_4));
			assertTrue(testIs("status", STATUS_5));

			assertFalse(testIs("status", DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * text のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterText() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testEqual("text", "*@ture7*", STATUS_1));
			assertTrue(testEqual("text", "*@ture7*", STATUS_2));
			assertTrue(testEqual("text", "*@ture7*", STATUS_3));
			assertFalse(testEqual("text", "*@ture7*", STATUS_4));

			assertTrue(testEqual("text", DM_1.getText(), DM_1));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** 無知の名前に対するテスト */
	@Test(expected = IllegalSyntaxException.class)
	public void testFilterUnknownName() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			StandardPropertyFactory.SINGLETON.getInstance(null, "unknown unknown", "", "");
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/**
	 * user のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterUser() throws IllegalSyntaxException {
		configuration.setGlobalInstance();
		try {
			assertFalse(testEqual("user", "*ture*", STATUS_1));
			assertTrue(testEqual("user", "*ture*", STATUS_2));
			assertTrue(testEqual("user", "*ture*", STATUS_3));
			assertFalse(testEqual("user", "*ture*", STATUS_4));

			assertTrue(testEqual("user", "*ture*", DM_1));
			assertTrue(testEqual("user", DM_1.getSenderScreenName(), DM_1));
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

	/**
	 * verified のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterVerified() throws IllegalSyntaxException {
		assertFalse(testIs("verified", STATUS_1));
		assertFalse(testIs("verified", STATUS_2));
		assertFalse(testIs("verified", STATUS_3));
		assertFalse(testIs("verified", STATUS_4));
		assertTrue(testIs("verified", STATUS_5));

		assertFalse(testIs("verified", DM_1));
	}
}
