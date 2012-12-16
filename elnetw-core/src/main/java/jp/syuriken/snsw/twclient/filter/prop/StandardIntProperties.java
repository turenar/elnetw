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
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class StandardIntProperties implements FilterProperty {

	private static Constructor<? extends FilterProperty> factory;

	private static final byte PROPERTY_ID_USERID = 1;

	private static final byte PROPERTY_ID_IN_REPLY_TO_USERID = 2;

	private static final byte PROPERTY_ID_RT_COUNT = 3;

	private static final byte PROPERTY_ID_TIMEDIFF = 4;

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
		} else if (Utility.equalString(name, "in_reply_to_userid")) {
			propertyId = PROPERTY_ID_IN_REPLY_TO_USERID;
		} else if (Utility.equalString(name, "rtcount")) {
			propertyId = PROPERTY_ID_RT_COUNT;
		} else if (Utility.equalString(name, "timediff")) {
			propertyId = PROPERTY_ID_TIMEDIFF;
		} else {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_NAME,
					"[StandardIntProperties] 対応してないプロパティ名です。バグ報告をお願いします: " + name);
		}
		// operator 処理
		if (operator == null) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_OPERATOR, "[" + name
					+ "] 正しいint演算子が必要です");
		}
		operatorType = FilterOperator.compileOperatorInt(operator);
		if (operatorType == null) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_OPERATOR, "[" + name
					+ "] 正しくないint演算子です: " + operator);
		}
		// value 処理: 整数は必ず指定しないとダメ。
		try {
			this.value = Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_VALUE, "[" + name + "] 値を整数型に変換できません",
					e);
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		long target;
		switch (propertyId) {
			case PROPERTY_ID_USERID:
				target = directMessage.getSenderId();
				break;
			case PROPERTY_ID_IN_REPLY_TO_USERID:
				target = directMessage.getRecipientId();
				break;
			case PROPERTY_ID_RT_COUNT:
				return false; // DM is not supported
			case PROPERTY_ID_TIMEDIFF:
				target = (System.currentTimeMillis() - directMessage.getCreatedAt().getTime()) / 1000;
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
			case PROPERTY_ID_IN_REPLY_TO_USERID:
				target = status.getInReplyToUserId();
				if (target == -1) {
					return false;
				}
				break;
			case PROPERTY_ID_RT_COUNT:
				target = status.getRetweetCount();
				break;
			case PROPERTY_ID_TIMEDIFF:
				target = (System.currentTimeMillis() - status.getCreatedAt().getTime()) / 1000;
				break;
			default:
				throw new AssertionError("StandardIntProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
}
