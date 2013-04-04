package jp.syuriken.snsw.twclient.filter.func;

import jp.syuriken.snsw.twclient.filter.FilterConstants;
import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/** Test for IfFilterFunction */
public class IfFilterFunctionTest extends FilterConstants {

	private void test(Object obj) throws IllegalSyntaxException {
		test1(obj, false, false, false);
		test1(obj, false, false, true);
		test1(obj, false, true, false);
		test1(obj, false, true, true);
		test1(obj, true, false, false);
		test1(obj, true, false, true);
		test1(obj, true, true, false);
		test1(obj, true, true, true);
		test1(obj, false, false);
		test1(obj, false, true);
		test1(obj, true, false);
		test1(obj, true, true);

	}

	private void test1(Object obj, boolean expr, boolean trueCond, boolean falseCond) throws IllegalSyntaxException {
		IfFilterFunction ifFilterFunction = new IfFilterFunction("if", new FilterDispatcherBase[]{
				expr ? TRUE_DISPATCHER : FALSE_DISPATCHER,
				trueCond ? TRUE_DISPATCHER : FALSE_DISPATCHER,
				falseCond ? TRUE_DISPATCHER : FALSE_DISPATCHER
		});
		if (obj instanceof Status) {
			assertEquals(expr ? trueCond : falseCond, ifFilterFunction.filter((Status) obj));
		} else if (obj instanceof DirectMessage) {
			assertEquals(expr ? trueCond : falseCond, ifFilterFunction.filter((DirectMessage) obj));
		}

	}

	private void test1(Object obj, boolean expr, boolean trueCond) throws IllegalSyntaxException {
		IfFilterFunction ifFilterFunction = new IfFilterFunction("if", new FilterDispatcherBase[]{
				expr ? TRUE_DISPATCHER : FALSE_DISPATCHER,
				trueCond ? TRUE_DISPATCHER : FALSE_DISPATCHER,
		});
		if (obj instanceof Status) {
			assertEquals(expr ? trueCond : false, ifFilterFunction.filter((Status) obj));
		} else if (obj instanceof DirectMessage) {
			assertEquals(expr ? trueCond : false, ifFilterFunction.filter((DirectMessage) obj));
		}
	}

	/** コンストラクタのテスト */
	@Test
	public void testConstructor() {
		try {
			new IfFilterFunction("if", new FilterDispatcherBase[]{});
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}

		FilterDispatcherBase[] arr = new FilterDispatcherBase[4];
		try {
			new IfFilterFunction("if", arr);
			fail("childの個数を無視したよう");
		} catch (IllegalSyntaxException e) {
			// do nothing
		}
	}

	/**
	 * {@link IfFilterFunction#filter(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterDirectMessage() throws IllegalSyntaxException {
		test(DM_1);
	}

	/**
	 * {@link IfFilterFunction#filter(twitter4j.Status)} のためのテスト・メソッド。
	 *
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testFilterStatus() throws IllegalSyntaxException {
		test(STATUS_1);
		test(STATUS_2);
		test(STATUS_3);
		test(STATUS_4);
		test(STATUS_5);
	}

}
