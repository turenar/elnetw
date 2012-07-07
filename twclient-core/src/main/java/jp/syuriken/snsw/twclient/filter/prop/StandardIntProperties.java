package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.filter.FilterOperator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 標準装備された数値を処理するプロパティ
 * 
 * @author $Author$
 */
public class StandardIntProperties implements FilterProperty {
	
	private static Constructor<? extends FilterProperty> factory;
	
	private static final byte PROPERTY_ID_USERID = 1;
	
	private FilterOperator operatorType;
	
	private long value;
	
	private byte propertyId;
	
	static {
		try {
			factory = StandardIntProperties.class.getConstructor(String.class, String.class, String.class);
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
	public StandardIntProperties(String name, String operator, String value) throws IllegalSyntaxException {
		// name 処理
		if (Utility.equalString(name, "userid")) {
			propertyId = PROPERTY_ID_USERID;
		} else {
			throw new IllegalSyntaxException("[StandardIntProperties] 対応してないプロパティ名です。バグ報告をお願いします: " + name);
		}
		// operator 処理
		if (operator == null) {
			throw new IllegalSyntaxException("[" + name + "] 正しいint演算子が必要です");
		}
		operatorType = FilterOperator.compileOperatorInt(operator);
		if (operatorType == null) {
			throw new IllegalSyntaxException("[" + name + "] 正しくないint演算子です: " + operator);
		}
		// value 処理
		try {
			this.value = Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new IllegalSyntaxException("[" + name + "] 値を整数型に変換できません", e);
		}
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		long target;
		switch (propertyId) {
			case PROPERTY_ID_USERID:
				target = directMessage.getRecipientId();
				break;
			default:
				throw new AssertionError("StandardIntProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
	
	@Override
	public boolean filter(Status status) {
		long target;
		switch (propertyId) {
			case PROPERTY_ID_USERID:
				target = status.getUser().getId();
				break;
			default:
				throw new AssertionError("StandardIntProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
}
