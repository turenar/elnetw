package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.filter.FilterOperator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 標準装備されたbool値を比較するプロパティ
 * 
 * @author $Author$
 */
public class StandardStringProperties implements FilterProperty {
	
	private static Constructor<? extends FilterProperty> factory;
	
	private static final byte PROPERTY_ID_USER = 1;
	
	private static final byte PROPERTY_ID_TEXT = 2;
	
	private FilterOperator operatorType;
	
	private Object value;
	
	private byte propertyId;
	
	static {
		try {
			factory = StandardStringProperties.class.getConstructor(String.class, String.class, String.class);
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
	public StandardStringProperties(String name, String operator, String value) throws IllegalSyntaxException {
		// name 処理
		if (Utility.equalString(name, "user")) {
			propertyId = PROPERTY_ID_USER;
		} else if (Utility.equalString(name, "text")) {
			propertyId = PROPERTY_ID_TEXT;
		} else {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_NAME,
					"[StandardStringProperties] 対応してないプロパティ名です。バグ報告をお願いします: " + name);
		}
		// operator 処理
		if (operator == null) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_OPERATOR, "[" + name
					+ "] string演算子が必要です");
		}
		operatorType = FilterOperator.compileOperatorString(operator);
		if (operatorType == null) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_OPERATOR, "[" + name
					+ "] 正しくないstring演算子です: " + operator);
		}
		// value 処理
		this.value = FilterOperator.compileValueString(value);
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		String target;
		switch (propertyId) {
			case PROPERTY_ID_USER:
				target = directMessage.getSenderScreenName();
				break;
			case PROPERTY_ID_TEXT:
				target = directMessage.getText();
				break;
			default:
				throw new AssertionError("StandardBooleanProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
	
	@Override
	public boolean filter(Status status) {
		String target;
		switch (propertyId) {
			case PROPERTY_ID_USER:
				target = status.getUser().getScreenName();
				break;
			case PROPERTY_ID_TEXT:
				target = status.getText();
				break;
			default:
				throw new AssertionError("StandardBooleanProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
}
