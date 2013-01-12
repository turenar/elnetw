package jp.syuriken.snsw.twclient.filter.func;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;

import org.junit.Test;

/**
 * {@link OneOfFilterFunction}のためのテスト
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class OneOfFilterFunctionTest extends FilterConstants {

	private OneOfFilterFunction get(boolean... bools) throws IllegalSyntaxException {
		return new OneOfFilterFunction("exactly_one_of", getDispatchers(bools));
	}

	/**
	 * コンストラクタのテスト
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testConstructor() throws IllegalSyntaxException {
		try {
			get();
			fail("子要素の個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
		try {
			get(true);
			get(true, false, true);
		} catch (IllegalSyntaxException ex) {
			throw ex;
		}
	}

	/**
	 * {@link OneOfFilterFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		assertTrue(get(true).filter(DM_1));
		assertFalse(get(false).filter(DM_1));
		assertFalse(get(true, true).filter(DM_1));
		assertTrue(get(true, false).filter(DM_1));
		assertTrue(get(false, true).filter(DM_1));
		assertFalse(get(false, false).filter(DM_1));
		assertFalse(get(true, true, true, true, true, true, true, true, true, true).filter(DM_1));
		assertFalse(get(true, false, false, true, true, true, false, true, true, true).filter(DM_1));
		assertTrue(get(true, false, false, false, false, false, false, false, false, false).filter(DM_1));
		assertFalse(get(false, false, false, false, false, false, false, false, false, false).filter(DM_1));
	}

	/**
	 * {@link OneOfFilterFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		assertTrue(get(true).filter(STATUS_1));
		assertFalse(get(false).filter(STATUS_1));
		assertFalse(get(true, true).filter(STATUS_1));
		assertTrue(get(true, false).filter(STATUS_1));
		assertTrue(get(false, true).filter(STATUS_1));
		assertFalse(get(false, false).filter(STATUS_1));
		assertFalse(get(true, true, true, true, true, true, true, true, true, true).filter(STATUS_1));
		assertFalse(get(true, false, false, true, true, true, false, true, true, true).filter(STATUS_1));
		assertTrue(get(true, false, false, false, false, false, false, false, false, false).filter(STATUS_1));
		assertFalse(get(false, false, false, false, false, false, false, false, false, false).filter(STATUS_1));
	}

}
