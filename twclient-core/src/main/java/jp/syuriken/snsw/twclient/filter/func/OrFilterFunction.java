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
public class OrFilterFunction implements FilterFunction {
	
	private static Constructor<OrFilterFunction> constructor;
	
	private final FilterDispatcherBase[] child;
	
	static {
		try {
			constructor = OrFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	
	/**
	 * コンストラクタを取得する。
	 * 
	 * @return コンストラクタ
	 */
	public static Constructor<OrFilterFunction> getFactory() {
		return constructor;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param functionName 関数名
	 * @param child 子要素の配列
	 * @throws IllegalSyntaxException エラー
	 */
	public OrFilterFunction(String functionName, FilterDispatcherBase[] child) throws IllegalSyntaxException {
		if (child.length == 0) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_FUNC_ARGS, "func<" + functionName
					+ ">: 子要素の個数が0です");
		}
		this.child = child;
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		for (FilterDispatcherBase operator : child) {
			if (operator.filter(directMessage)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean filter(Status status) {
		for (FilterDispatcherBase operator : child) {
			if (operator.filter(status)) {
				return true;
			}
		}
		return false;
	}
	
}
