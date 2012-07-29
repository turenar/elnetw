package jp.syuriken.snsw.twclient.filter.prop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;

import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * {@link StandardBooleanProperties}のためのテスト
 * 
 * @author $Author$
 */
public class StandardBooleanPropertiesTest extends FilterConstants {
	
	private static ClientConfiguration configuration;
	
	
	/**
	 * テスト前に呼ばれるクラス
	 * 
	 * @throws Exception 例外
	 */
	@BeforeClass
	public static void tearUpClass() throws Exception {
		Constructor<ClientConfiguration> constructor = ClientConfiguration.class.getDeclaredConstructor(boolean.class); // テスト用メソッド
		constructor.setAccessible(true);
		configuration = constructor.newInstance(true);
		ClientProperties defaultProperties = new ClientProperties();
		defaultProperties.load(ClientConfiguration.class
			.getResourceAsStream("/jp/syuriken/snsw/twclient/config.properties"));
		configuration.setConfigDefaultProperties(defaultProperties);
		ClientProperties properties = new ClientProperties(defaultProperties);
		properties
			.setProperty("twitter.oauth.access_token.list", STATUS_2.getUser().getId() + " " + DM_1.getSenderId());
		configuration.setConfigProperties(properties);
	}
	
	private static boolean testIs(String propName, DirectMessage directMessage) throws IllegalSyntaxException {
		return new StandardBooleanProperties(propName, "?", null).filter(directMessage);
	}
	
	private static boolean testIs(String propName, Status status) throws IllegalSyntaxException {
		return new StandardBooleanProperties(propName, "?", null).filter(status);
	}
	
	/**
	 * mine のテスト
	 * 
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterMine() throws IllegalSyntaxException {
		StandardBooleanProperties property = new StandardBooleanProperties("mine", "?", null);
		assertFalse(property.filter(STATUS_1));
		assertTrue(property.filter(STATUS_2));
		assertTrue(property.filter(STATUS_3));
		assertFalse(property.filter(STATUS_4));
		assertTrue(property.filter(DM_1));
	}
	
	/**
	 * retweeted のテスト
	 * 
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterRetweeted() throws IllegalSyntaxException {
		assertFalse(testIs("retweeted", STATUS_1));
		assertFalse(testIs("retweeted", STATUS_1));
		assertFalse(testIs("retweeted", STATUS_2));
		assertTrue(testIs("retweeted", STATUS_4));
		
		assertTrue(testIs("retweeted", DM_1));
	}
	
	/**
	 * 無知の名前に対するテスト
	 */
	@Test
	public void testFilterUnknownName() {
		try {
			new StandardBooleanProperties("unknown unknown", "", "");
			fail("prop nameを無視してるかな？");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}
	
}
