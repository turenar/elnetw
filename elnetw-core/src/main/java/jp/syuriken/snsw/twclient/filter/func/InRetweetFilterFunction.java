package jp.syuriken.snsw.twclient.filter.func;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.FilterFunction;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * RTを対象にするクラス。
 *
 * @author $Author$
 */
public class InRetweetFilterFunction implements FilterFunction {

	private static Constructor<InRetweetFilterFunction> constructor;


	/**
	 * ファクトリーメソッドを取得する。
	 *
	 * @return コンストラクタ
	 */
	public static Constructor<? extends FilterFunction> getFactory() {
		return constructor;
	}


	private FilterDispatcherBase child;

	static {
		try {
			constructor = InRetweetFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}


	/**
	 * インスタンスを生成する。
	 *
	 * @param name 関数名
	 * @param child 子要素
	 * @throws IllegalSyntaxException エラー
	 */
	public InRetweetFilterFunction(String name, FilterDispatcherBase[] child) throws IllegalSyntaxException {
		if (child.length != 1) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_FUNC_ARGS, "func<inrt> の引数は一つでなければなりません");
		}
		this.child = child[0];
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return child.filter(directMessage); // DM is not supported retweet
	}

	@Override
	public boolean filter(Status status) {
		return child.filter(status.isRetweet() ? status.getRetweetedStatus() : status);
	}

}
