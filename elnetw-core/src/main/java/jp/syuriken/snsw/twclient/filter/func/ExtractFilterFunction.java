package jp.syuriken.snsw.twclient.filter.func;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.FilterFunction;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;


/** extract: {@link jp.syuriken.snsw.twclient.filter.FilterEditFrame}用 */
public class ExtractFilterFunction implements FilterFunction {

	private static Constructor<ExtractFilterFunction> constructor;

	/**
	 * コンストラクタを取得する。
	 *
	 * @return コンストラクタ
	 */
	public static Constructor<ExtractFilterFunction> getFactory() {
		return constructor;
	}

	private final FilterDispatcherBase child;

	static {
		try {
			constructor = ExtractFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param functionName 関数名
	 * @param child        子要素の配列
	 * @throws IllegalSyntaxException エラー
	 */
	public ExtractFilterFunction(String functionName, FilterDispatcherBase[] child) throws IllegalSyntaxException {
		int length = child.length;
		if (length == 0) {
			this.child = null;
		} else if (length == 1) {
			this.child = child[0];
		} else {
			throw new IllegalSyntaxException("func<" + functionName + "> の引数は一つでなければなりません");
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		FilterDispatcherBase child = this.child;
		if (child == null) {
			return true;
		} else {
			return child.filter(directMessage) == false;
		}
	}

	@Override
	public boolean filter(Status status) {
		FilterDispatcherBase child = this.child;
		if (child == null) {
			return true;
		} else {
			return child.filter(status) == false;
		}
	}
}
