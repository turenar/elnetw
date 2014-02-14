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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
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
public class StandardStringProperties implements FilterProperty {

	private static final byte PROPERTY_ID_USER = 1;
	private static final byte PROPERTY_ID_TEXT = 2;
	private static final byte PROPERTY_ID_CLIENT = 3;
	private static Constructor<? extends FilterProperty> factory;

	/**
	 * コンストラクターを取得する
	 *
	 * @return ファクトリー
	 */
	public static Constructor<? extends FilterProperty> getFactory() {
		return factory;
	}

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
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 * @param name          プロパティ名
	 * @param operator      演算子文字列。ない場合は null。
	 * @param value         比較する値。ない場合は null。
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public StandardStringProperties(ClientConfiguration configuration, String name, String operator, Object value)
			throws IllegalSyntaxException {
		// name 処理
		switch (name) {
			case "user":
				propertyId = PROPERTY_ID_USER;
				break;
			case "text":
				propertyId = PROPERTY_ID_TEXT;
				break;
			case "client":
				propertyId = PROPERTY_ID_CLIENT;
				break;
			default:
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
