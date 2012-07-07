package jp.syuriken.snsw.twclient.filter.func;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.filter.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.FilterFunction;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 一つしかマッチしないことを確認するフィルタ関数。
 * 
 * @author $Author$
 */
public class OneOfFilterFunction implements FilterFunction {
	
	private static Constructor<OneOfFilterFunction> constructor;
	
	private final FilterDispatcherBase[] child;
	
	static {
		try {
			constructor = OneOfFilterFunction.class.getConstructor(String.class, FilterDispatcherBase[].class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	
	/**
	 * コンストラクタを取得する。
	 * 
	 * @return コンストラクタ
	 */
	public static Constructor<OneOfFilterFunction> getFactory() {
		return constructor;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param functionName 関数名
	 * @param child 子要素の配列
	 */
	public OneOfFilterFunction(String functionName, FilterDispatcherBase[] child) {
		this.child = child;
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		int i = 0;
		for (FilterDispatcherBase operator : child) {
			if (operator.filter(directMessage)) {
				i++;
			}
		}
		return i == 1;
	}
	
	@Override
	public boolean filter(Status status) {
		int i = 0;
		for (FilterDispatcherBase operator : child) {
			if (operator.filter(status)) {
				i++;
			}
		}
		return i == 1;
	}
	
}
