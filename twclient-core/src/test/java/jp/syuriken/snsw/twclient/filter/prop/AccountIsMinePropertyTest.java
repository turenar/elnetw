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

/**
 * {@link AccountIsMineProperty}のためのテスト
 * 
 * @author $Author$
 */
public class AccountIsMinePropertyTest extends FilterConstants {
	
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
		defaultProperties.load(AccountIsMineProperty.class
			.getResourceAsStream("/jp/syuriken/snsw/twclient/config.properties"));
		configuration.setConfigDefaultProperties(defaultProperties);
		ClientProperties properties = new ClientProperties(defaultProperties);
		properties
			.setProperty("twitter.oauth.access_token.list", STATUS_2.getUser().getId() + " " + DM_1.getSenderId());
		configuration.setConfigProperties(properties);
	}
	
	/**
	 * {@link AccountIsMineProperty#AccountIsMineProperty(String, String, String)} のためのテスト・メソッド。
	 */
	@Test
	public void testAccountIsMineProperty() {
		try {
			new AccountIsMineProperty("mine", ":", "gte");
			fail("valueを無視したよう");
		} catch (IllegalSyntaxException e) {
			// success
		}
		try {
			new AccountIsMineProperty("mine", "?", "gte");
			fail("valueを無視したよう");
		} catch (IllegalSyntaxException e) {
			// success
		}
		try {
			new AccountIsMineProperty("mine", "?", "true");
			fail("valueを無視したよう");
		} catch (IllegalSyntaxException e) {
			// success
		}
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.prop.AccountIsMineProperty#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		AccountIsMineProperty property = new AccountIsMineProperty("mine", "?", null);
		assertTrue(property.filter(DM_1));
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.prop.AccountIsMineProperty#filter(twitter4j.Status)} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		AccountIsMineProperty property = new AccountIsMineProperty("mine", "?", null);
		assertFalse(property.filter(STATUS_1));
		assertTrue(property.filter(STATUS_2));
		assertTrue(property.filter(STATUS_3));
		assertFalse(property.filter(STATUS_4));
	}
	
}
