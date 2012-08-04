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
	
	private FilterOperator operatorType;
	
	private boolean value;
	
	private byte propertyId;
	
	private ClientConfiguration configuration;
	
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
		}
	}
	
	@Override
	public boolean filter(DirectMessage directMessage) {
		boolean target;
		switch (propertyId) {
			case PROPERTY_ID_RETWEETED:
				return true;
			case PROPERTY_ID_MINE:
				target = configuration.isMyAccount(directMessage.getSenderId());
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
			case PROPERTY_ID_RETWEETED:
				target = status.isRetweet();
				break;
			case PROPERTY_ID_MINE:
				target = configuration.isMyAccount(status.getUser().getId());
				break;
			default:
				throw new AssertionError("StandardBooleanProperties: 予期しないpropertyId");
		}
		return operatorType.compare(target, value);
	}
}
