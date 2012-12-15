package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;
import java.util.Locale;

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
 * @author $Author$
 */
public class StandardBooleanProperties implements FilterProperty {

	private static Constructor<? extends FilterProperty> factory;

	private static final byte PROPERTY_ID_RETWEETED = 1;

	private static final byte PROPERTY_ID_MINE = 2;

	private static final byte PROPERTY_ID_PROTECTED = 3;

	private static final byte PROPERTY_ID_VERIFIED = 4;

	private static final byte PROPERTY_ID_DM = 5;

	private static final byte PROPERTY_ID_STATUS = 6;

	private final FilterOperator operatorType;

	private final boolean value;

	private final byte propertyId;

	private final ClientConfiguration configuration;

	static {
		try {
			factory = StandardBooleanProperties.class.getConstructor(String.class, String.class, String.class);
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
	public StandardBooleanProperties(String name, String operator, String value) throws IllegalSyntaxException {
		configuration = ClientConfiguration.getInstance();
		// name 処理
		if (Utility.equalString(name, "retweeted")) {
			propertyId = PROPERTY_ID_RETWEETED;
		} else if (Utility.equalString(name, "mine")) {
			propertyId = PROPERTY_ID_MINE;
		} else if (Utility.equalString(name, "protected")) {
			propertyId = PROPERTY_ID_PROTECTED;
		} else if (Utility.equalString(name, "verified")) {
			propertyId = PROPERTY_ID_VERIFIED;
		} else if (Utility.equalString(name, "dm")) {
			propertyId = PROPERTY_ID_DM;
		} else if (Utility.equalString(name, "status")) {
			propertyId = PROPERTY_ID_STATUS;
		} else {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_NAME,
					"[StandardBooleanProperties] 対応してないプロパティ名です。バグ報告をお願いします: " + name);
		}
		// operator 処理
		if (operator == null) {
			operatorType = FilterOperator.IS;
		} else {
			operatorType = FilterOperator.compileOperatorBool(operator);
		}
		if (operatorType == null) {
			throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_OPERATOR, "[" + name
					+ "] 正しくないbool演算子です: " + operator);
		}
		// value 処理
		if ((operatorType == FilterOperator.IS || operatorType == FilterOperator.IS_NOT) == false) {
			String lowerValue = value.toLowerCase(Locale.ENGLISH);
			if (Utility.equalString(lowerValue, "false") || Utility.equalString(lowerValue, "no")) {
				this.value = false;
			} else if (Utility.equalString(lowerValue, "true") || Utility.equalString(lowerValue, "yes")) {
				this.value = true;
			} else {
				throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_VALUE, "[" + name
						+ "] 値がbool型ではありません");
			}
		} else {
			this.value = false; // init because this field is final
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		boolean target;
		switch (propertyId) {
			case PROPERTY_ID_DM:
				return true;
			case PROPERTY_ID_STATUS:
			case PROPERTY_ID_RETWEETED:
				return false;
			case PROPERTY_ID_MINE:
				target = configuration.isMyAccount(directMessage.getSenderId());
				break;
			case PROPERTY_ID_VERIFIED:
				target = directMessage.getSender().isVerified();
				break;
			case PROPERTY_ID_PROTECTED:
				target = directMessage.getSender().isProtected();
				break;
			default:
				throw new AssertionError("StandardBooleanProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}

	@Override
	public boolean filter(Status status) {
		boolean target;
		switch (propertyId) {
			case PROPERTY_ID_DM:
				return false;
			case PROPERTY_ID_STATUS:
				return true;
			case PROPERTY_ID_RETWEETED:
				target = status.isRetweet();
				break;
			case PROPERTY_ID_MINE:
				target = configuration.isMyAccount(status.getUser().getId());
				break;
			case PROPERTY_ID_PROTECTED:
				target = status.getUser().isProtected();
				break;
			case PROPERTY_ID_VERIFIED:
				target = status.getUser().isVerified();
				break;
			default:
				throw new AssertionError("StandardBooleanProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
}
