package jp.syuriken.snsw.twclient.filter.func;

import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {@link AndFilterFunction}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AndFilterFunctionTest extends FilterConstants {

	private AndFilterFunction get(boolean... bools) throws IllegalSyntaxException {
		return new AndFilterFunction("and", getDispatchers(bools));
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
	 * {@link jp.syuriken.snsw.twclient.filter.func.AndFilterFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		assertTrue(get(true).filter(DM_1));
		assertFalse(get(false).filter(DM_1));
		assertTrue(get(true, true).filter(DM_1));
		assertFalse(get(true, false).filter(DM_1));
		assertFalse(get(false, true).filter(DM_1));
		assertFalse(get(false, false).filter(DM_1));
		assertTrue(get(true, true, true, true, true, true, true, true, true, true).filter(DM_1));
		assertFalse(get(true, false, false, true, true, true, false, true, true, true).filter(DM_1));
		assertFalse(get(true, false, false, false, false, false, false, false, false, false).filter(DM_1));
		assertFalse(get(false, false, false, false, false, false, false, false, false, false).filter(DM_1));
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.func.AndFilterFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		assertTrue(get(true).filter(STATUS_1));
		assertFalse(get(false).filter(STATUS_1));
		assertTrue(get(true, true).filter(STATUS_1));
		assertFalse(get(true, false).filter(STATUS_1));
		assertFalse(get(false, true).filter(STATUS_1));
		assertFalse(get(false, false).filter(STATUS_1));
		assertTrue(get(true, true, true, true, true, true, true, true, true, true).filter(STATUS_1));
		assertFalse(get(true, false, false, true, true, true, false, true, true, true).filter(STATUS_1));
		assertFalse(get(true, false, false, false, false, false, false, false, false, false).filter(STATUS_1));
		assertFalse(get(false, false, false, false, false, false, false, false, false, false).filter(STATUS_1));
	}
}
