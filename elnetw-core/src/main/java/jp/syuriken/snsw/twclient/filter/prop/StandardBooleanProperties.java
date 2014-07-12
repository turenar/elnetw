/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.filter.FilterOperator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 標準装備されたbool値を比較するプロパティ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StandardBooleanProperties implements FilterProperty {

	private static final byte PROPERTY_ID_RETWEETED = 1;
	private static final byte PROPERTY_ID_MINE = 2;
	private static final byte PROPERTY_ID_PROTECTED = 3;
	private static final byte PROPERTY_ID_VERIFIED = 4;
	private static final byte PROPERTY_ID_DM = 5;
	private static final byte PROPERTY_ID_STATUS = 6;
	private static Constructor<? extends FilterProperty> factory;

	/**
	 * コンストラクターを取得する
	 *
	 * @return ファクトリー
	 */
	public static Constructor<? extends FilterProperty> getFactory() {
		return factory;
	}

	private final FilterOperator operatorType;
	private final boolean value;
	private final byte propertyId;
	private final ClientConfiguration configuration;

	static {
		try {
			factory =
					StandardBooleanProperties.class.getConstructor(ClientConfiguration.class, String.class,
							String.class, Object.class);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 * @param name          プロパティ名
	 * @param operator      演算子文字列。ない場合は null。
	 * @param value         比較する値。ない場合は null。
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public StandardBooleanProperties(ClientConfiguration configuration, String name, String operator, Object value)
			throws IllegalSyntaxException {
		this.configuration = configuration;
		// name 処理
		switch (name) {
			case "retweeted":
				propertyId = PROPERTY_ID_RETWEETED;
				break;
			case "mine":
				propertyId = PROPERTY_ID_MINE;
				break;
			case "protected":
				propertyId = PROPERTY_ID_PROTECTED;
				break;
			case "verified":
				propertyId = PROPERTY_ID_VERIFIED;
				break;
			case "dm":
				propertyId = PROPERTY_ID_DM;
				break;
			case "status":
				propertyId = PROPERTY_ID_STATUS;
				break;
			default:
				throw new IllegalSyntaxException("[StandardBooleanProperties] 対応してないプロパティ名です。バグ報告をお願いします: " + name);
		}
		// operator 処理
		if (operator == null) {
			operatorType = FilterOperator.IS;
		} else {
			operatorType = FilterOperator.compileOperatorBool(operator);
		}
		if (operatorType == null) {
			throw new IllegalSyntaxException("[" + name + "] 正しくないbool演算子です: " + operator);
		}
		if (value == null || value instanceof Boolean) {
			// value 処理
			if (!(operatorType == FilterOperator.IS || operatorType == FilterOperator.IS_NOT)) {
				if (value == null) {
					throw new IllegalSyntaxException("[" + name + "] 比較値が必要です");
				} else {
					this.value = (Boolean) value;
				}
			} else {
				this.value = false; // init because this field is final
			}
		} else {
			throw new IllegalSyntaxException("[" + name + "] 値がbool型ではありません");
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		boolean target;
		switch (propertyId) {
			case PROPERTY_ID_DM:
				target = true;
				break;
			case PROPERTY_ID_STATUS:
			case PROPERTY_ID_RETWEETED:
				target = false;
				break;
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
				target = false;
				break;
			case PROPERTY_ID_STATUS:
				target = true;
				break;
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
