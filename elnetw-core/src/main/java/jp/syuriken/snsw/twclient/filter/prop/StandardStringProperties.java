package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.filter.FilterOperator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 標準装備されたbool値を比較するプロパティ
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class StandardStringProperties implements FilterProperty {

	private static Constructor<? extends FilterProperty> factory;

	private static final byte PROPERTY_ID_USER = 1;

	private static final byte PROPERTY_ID_TEXT = 2;

	private static final byte PROPERTY_ID_CLIENT = 3;

	private FilterOperator operatorType;

	private Object value;

	private byte propertyId;

	static {
		try {
			factory =
					StandardStringProperties.class.getConstructor(ClientConfiguration.class, String.class,
							String.class, Object.class);
		} catch (Throwable e) {
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
	 * @param configuration 設定
	 * @param name プロパティ名
	 * @param operator 演算子文字列。ない場合は null。
	 * @param value 比較する値。ない場合は null。
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public StandardStringProperties(ClientConfiguration configuration, String name, String operator, Object value)
			throws IllegalSyntaxException {
		// name 処理
		if (Utility.equalString(name, "user")) {
			propertyId = PROPERTY_ID_USER;
		} else if (Utility.equalString(name, "text")) {
			propertyId = PROPERTY_ID_TEXT;
		} else if (Utility.equalString(name, "client")) {
			propertyId = PROPERTY_ID_CLIENT;
		} else {
			throw new IllegalSyntaxException("[StandardStringProperties] 対応してないプロパティ名です。バグ報告をお願いします: " + name);
		}
		// operator 処理
		if (operator == null) {
			throw new IllegalSyntaxException("[" + name + "] string演算子が必要です");
		}
		operatorType = FilterOperator.compileOperatorString(operator);
		if (operatorType == null) {
			throw new IllegalSyntaxException("[" + name + "] 正しくないstring演算子です: " + operator);
		}
		// value 処理
		if (value instanceof String) {

			this.value = FilterOperator.compileValueString((String) value);
		} else {
			throw new IllegalSyntaxException("[" + name + "] 正しくないstring値です: " + operator);
		}
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
			case PROPERTY_ID_CLIENT:
				return false;
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
			case PROPERTY_ID_CLIENT:
				target = status.getSource();
				int nameStart = target.indexOf('>');
				int nameEnd = target.lastIndexOf('<');
				target = target.substring(nameStart < 0 ? 0 : nameStart + 1, nameEnd < 0 ? target.length() : nameEnd);
				break;
			default:
				throw new AssertionError("StandardBooleanProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
}
