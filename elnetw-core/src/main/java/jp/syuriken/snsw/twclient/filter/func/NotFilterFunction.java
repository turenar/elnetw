package jp.syuriken.snsw.twclient.filter.func;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.FilterFunction;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * どれかマッチすることを確認するフィルタ関数
 * 
 * @author $Author$
 */
public class NotFilterFunction implements FilterFunction {
	
	private static Constructor<NotFilterFunction> constructor;
	
	private final FilterDispatcherBase child;
	
	static {
		try {
			constructor = NotFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	
	/**
	 * コンストラクタを取得する。
	 * 
	 * @return コンストラクタ
	 */
	public static Constructor<NotFilterFunction> getFactory() {
		return constructor;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param functionName 関数名
	 * @param child 子要素の配列
	 * @throws IllegalSyntaxException エラー
	 */
	public NotFilterFunction(String functionName, FilterDispatcherBase[] child) throws IllegalSyntaxException {
		if (child.length != 1) {
			throw new IllegalSyntaxException(child.length == 0 ? IllegalSyntaxException.ID_FUNC_ARGS : 2, "func<"
					+ functionName + "> の引数は一つでなければなりません");
		}
		this.child = child[0];
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		return child.filter(directMessage) == false;
	}
	
	@Override
	public boolean filter(Status status) {
		return child.filter(status) == false;
	}
}
