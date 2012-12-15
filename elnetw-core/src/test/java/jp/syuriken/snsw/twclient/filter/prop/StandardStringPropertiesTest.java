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
 * {@link StandardStringProperties}のためのテスト
 *
 * @author $Author$
 */
public class StandardStringPropertiesTest extends FilterConstants {

	private static boolean testEqual(String propName, String target, DirectMessage directMessage)
			throws IllegalSyntaxException {
		return new StandardStringProperties(propName, ":", target).filter(directMessage);
	}

	private static boolean testEqual(String propName, String target, Status status) throws IllegalSyntaxException {
		return new StandardStringProperties(propName, ":", target).filter(status);
	}

	/**
	 * client のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterClient() throws IllegalSyntaxException {
		assertTrue(testEqual("client", "TweetDeck", STATUS_5));
	}

	/**
	 * text のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterText() throws IllegalSyntaxException {
		assertFalse(testEqual("text", "*@ture7*", STATUS_1));
		assertTrue(testEqual("text", "*@ture7*", STATUS_2));
		assertTrue(testEqual("text", "*@ture7*", STATUS_3));
		assertFalse(testEqual("text", "*@ture7*", STATUS_4));

		assertTrue(testEqual("text", DM_1.getText(), DM_1));
	}

	/**
	 * 無知の名前に対するテスト
	 */
	@Test
	public void testFilterUnknownName() {
		try {
			new StandardIntProperties("unknown unknown", "", "");
			fail("prop nameを無視してるかな？");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}

	/**
	 * user のテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterUser() throws IllegalSyntaxException {
		assertFalse(testEqual("user", "*ture*", STATUS_1));
		assertTrue(testEqual("user", "*ture*", STATUS_2));
		assertTrue(testEqual("user", "*ture*", STATUS_3));
		assertFalse(testEqual("user", "*ture*", STATUS_4));

		assertFalse(testEqual("user", "*ture*", DM_1));
		assertTrue(testEqual("user", DM_1.getSenderScreenName(), DM_1));
	}

}
