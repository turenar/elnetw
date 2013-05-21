package jp.syuriken.snsw.twclient.filter.prop;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link StandardIntProperties}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StandardIntPropertiesTest extends FilterConstants {

	private static ClientConfiguration configuration;

	/**
	 * テスト前に呼ばれる関数
	 *
	 * @throws Exception 例外
	 */
	@BeforeClass
	public static void tearUpClass() throws Exception {
		Constructor<ClientConfiguration> constructor = ClientConfiguration.class.getDeclaredConstructor(); // テスト用メソッド
		constructor.setAccessible(true);
		configuration = constructor.newInstance();
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
		assertEquals(-1, STATUS_1.getInReplyToUserId());
		assertFalse(testEqual("in_reply_to_userid", -1, STATUS_1));
		assertFalse(testEqual("in_reply_to_userid", -1, STATUS_2));
		assertTrue(testEqual("in_reply_to_userid", STATUS_2.getUser().getId(), STATUS_3));
		assertFalse(testEqual("in_reply_to_userid", -1, STATUS_4));

		assertTrue(testEqual("in_reply_to_userid", DM_1.getRecipientId(), DM_1));
		assertFalse(testEqual("in_reply_to_userid", STATUS_1.getUser().getId(), DM_1));

	}

	/**
	 * rtcount のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterRtCount() throws IllegalSyntaxException {
		assertTrue(testEqual("rtcount", STATUS_1.getRetweetCount(), STATUS_1));
		assertFalse(testEqual("rtcount", STATUS_2.getRetweetCount(), STATUS_4));
		assertFalse(testEqual("rtcount", STATUS_3.getRetweetCount(), STATUS_4));
		assertTrue(testEqual("rtcount", STATUS_4.getRetweetCount(), STATUS_4));

		assertFalse(testEqual("rtcount", 0, DM_1));
		assertFalse(testEqual("rtcount", -1, DM_1));
	}

	/**
	 * 無知の名前に対するテスト
	 */
	@Test
	public void testFilterUnknownName() {
		try {
			new StandardIntProperties(configuration, "unknown unknown", "", "");
			fail("prop nameを無視してるかな？");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}

	/**
	 * userid のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterUserId() throws IllegalSyntaxException {
		assertTrue(testEqual("userid", STATUS_1.getUser().getId(), STATUS_1));
		assertFalse(testEqual("userid", STATUS_2.getUser().getId(), STATUS_1));
		assertTrue(testEqual("userid", STATUS_3.getUser().getId(), STATUS_2));
		assertTrue(testEqual("userid", STATUS_4.getUser().getId(), STATUS_4));

		assertTrue(testEqual("userid", DM_1.getSenderId(), DM_1));
		assertFalse(testEqual("userid", STATUS_1.getUser().getId(), DM_1));
	}

}
