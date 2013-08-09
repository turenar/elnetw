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

import static org.junit.Assert.*;

/**
 * {@link StandardBooleanProperties}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StandardBooleanPropertiesTest extends FilterConstants {

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

	private static boolean testIs(String propName, DirectMessage directMessage) throws IllegalSyntaxException {
		return new StandardBooleanProperties(configuration, propName, "?", false).filter(directMessage);
	}

	private static boolean testIs(String propName, Status status) throws IllegalSyntaxException {
		return new StandardBooleanProperties(configuration, propName, "?", false).filter(status);
	}

	/**
	 * status のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		assertFalse(testIs("dm", STATUS_1));
		assertFalse(testIs("dm", STATUS_2));
		assertFalse(testIs("dm", STATUS_3));
		assertFalse(testIs("dm", STATUS_4));
		assertFalse(testIs("dm", STATUS_5));

		assertTrue(testIs("dm", DM_1));
	}

	/**
	 * mine のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterMine() throws IllegalSyntaxException {
		assertFalse(testIs("mine", STATUS_1));
		assertTrue(testIs("mine", STATUS_2));
		assertTrue(testIs("mine", STATUS_3));
		assertFalse(testIs("mine", STATUS_4));
		assertTrue(testIs("mine", DM_1));
	}

	/**
	 * protected のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterProtected() throws IllegalSyntaxException {
		assertFalse(testIs("protected", STATUS_1));
		assertFalse(testIs("protected", STATUS_2));
		assertFalse(testIs("protected", STATUS_3));
		assertFalse(testIs("protected", STATUS_4));
		assertTrue(testIs("protected", STATUS_5));

		assertFalse(testIs("protected", DM_1));
	}

	/**
	 * retweeted のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterRetweeted() throws IllegalSyntaxException {
		assertFalse(testIs("retweeted", STATUS_1));
		assertFalse(testIs("retweeted", STATUS_2));
		assertFalse(testIs("retweeted", STATUS_3));
		assertTrue(testIs("retweeted", STATUS_4));

		assertFalse(testIs("retweeted", DM_1));
	}

	/**
	 * status のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		assertTrue(testIs("status", STATUS_1));
		assertTrue(testIs("status", STATUS_2));
		assertTrue(testIs("status", STATUS_3));
		assertTrue(testIs("status", STATUS_4));
		assertTrue(testIs("status", STATUS_5));

		assertFalse(testIs("status", DM_1));
	}

	/** 無知の名前に対するテスト */
	@Test
	public void testFilterUnknownName() {
		try {
			new StandardBooleanProperties(configuration, "unknown unknown", "?", false);
			fail("prop nameを無視してるかな？");
		} catch (IllegalSyntaxException e) {
			// do nothing
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
