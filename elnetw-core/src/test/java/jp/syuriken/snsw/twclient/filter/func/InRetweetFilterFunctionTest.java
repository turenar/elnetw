package jp.syuriken.snsw.twclient.filter.func;

import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TODO tyanar
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InRetweetFilterFunctionTest extends FilterConstants {

	/** コンストラクタのテスト */
	@Test
	public void testConstructor() {
		try {
			new InRetweetFilterFunction("inrt", new FilterDispatcherBase[] {});
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}

		try {
			new InRetweetFilterFunction("inrt", thisDispatcher);
		} catch (IllegalSyntaxException e) {
			throw new AssertionError(e);
		}

		FilterDispatcherBase[] arr = new FilterDispatcherBase[2];
		try {
			new InRetweetFilterFunction("inrt", arr);
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.func.InRetweetFilterFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		InRetweetFilterFunction filterFunction = new InRetweetFilterFunction("inrt", thisDispatcher);

		assertFalse(filterFunction.filter(DM_1));
		assertFilteringObject(DM_1);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.func.InRetweetFilterFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		InRetweetFilterFunction filterFunction = new InRetweetFilterFunction("inrt", thisDispatcher);

		assertFalse(filterFunction.filter(STATUS_1));
		assertFilteringObject(STATUS_1);

		assertFalse(filterFunction.filter(STATUS_2));
		assertFilteringObject(STATUS_2);

		assertFalse(filterFunction.filter(STATUS_3));
		assertFilteringObject(STATUS_3);

		assertFalse(filterFunction.filter(STATUS_4));
		assertFilteringObject(STATUS_4.getRetweetedStatus());
	}
}
