package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.filter.FilterOperator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * このステータスは自分の発言なのかを調べる。
 * 
 * @author $Author$
 */
public class AccountIsMineProperty implements FilterProperty {
	
	private static Constructor<? extends FilterProperty> factory;
	
	private FilterOperator operatorType;
	
	private boolean value;
	
	private ClientConfiguration configuration;
	
	static {
		try {
			factory = AccountIsMineProperty.class.getConstructor(String.class, String.class, String.class);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	
	/**
	 * コンストラクターを取得する
	 * @return ファクトリー
	 */
	public static Constructor<? extends FilterProperty> getFactory() {
		return factory;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param name プロパティ名
	 * @param operator 演算子文字列。ない場合は null。
	 * @param value 比較する値。ない場合は null。
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public AccountIsMineProperty(String name, String operator, String value) throws IllegalSyntaxException {
		configuration = ClientConfiguration.getInstance();
		// operator 処理
		operatorType = FilterOperator.compileOperatorBool(operator);
		if (operatorType == null) {
			throw new IllegalSyntaxException("[" + name + "] 正しくないbool演算子です: " + operator);
		}
		// value 処理
		this.value = operatorType.compileValueBool(name, value);
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		return operatorType.compare(configuration.isMyAccount(directMessage.getSender().getId()), value);
	}
	
	@Override
	public boolean filter(Status status) {
		return operatorType.compare(configuration.isMyAccount(status.getUser().getId()), value);
	}
}
