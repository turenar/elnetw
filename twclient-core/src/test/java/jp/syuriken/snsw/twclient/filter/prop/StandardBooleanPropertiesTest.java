package jp.syuriken.snsw.twclient.filter.prop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;

import org.junit.Test;

import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * {@link StandardBooleanProperties}のためのテスト
 * 
 * @author $Author$
 */
public class StandardBooleanPropertiesTest extends FilterConstants {
	
	private static boolean testIs(String propName, DirectMessage directMessage) throws IllegalSyntaxException {
		return new StandardBooleanProperties(propName, "?", null).filter(directMessage);
	}
	
	private static boolean testIs(String propName, Status status) throws IllegalSyntaxException {
		return new StandardBooleanProperties(propName, "?", null).filter(status);
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
