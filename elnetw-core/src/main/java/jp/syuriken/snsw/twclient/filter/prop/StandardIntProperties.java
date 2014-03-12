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
 * 標準装備された数値を処理するプロパティ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StandardIntProperties implements FilterProperty {

	private static final byte PROPERTY_ID_USERID = 1;
	private static final byte PROPERTY_ID_IN_REPLY_TO_USERID = 2;
	private static final byte PROPERTY_ID_RT_COUNT = 3;
	private static final byte PROPERTY_ID_TIMEDIFF = 4;
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
	private long value;
	private byte propertyId;

	static {
		try {
			factory =
					StandardIntProperties.class.getConstructor(ClientConfiguration.class, String.class, String.class,
							Object.class);
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
	public StandardIntProperties(ClientConfiguration configuration, String name, String operator, Object value)
			throws IllegalSyntaxException {
		// name 処理
		switch (name) {
			case "userid":
				propertyId = PROPERTY_ID_USERID;
				break;
			case "in_reply_to_userid":
				propertyId = PROPERTY_ID_IN_REPLY_TO_USERID;
				break;
			case "rtcount":
				propertyId = PROPERTY_ID_RT_COUNT;
				break;
			case "timediff":
				propertyId = PROPERTY_ID_TIMEDIFF;
				break;
			default:
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
		// value 処理: 整数は必ず指定しないとダメ。
		if (value instanceof Long) {
			this.value = (Long) value;
		} else if (value == null) {
			throw new IllegalSyntaxException("[" + name + "] 比較値が必要です");
		} else {
			throw new IllegalSyntaxException("[" + name + "] 値が整数型ではありません");
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
