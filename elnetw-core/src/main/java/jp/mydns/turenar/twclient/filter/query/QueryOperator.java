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

package jp.mydns.turenar.twclient.filter.query;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;

/**
 * プロパティの演算子の処理を楽にするためのユーティリティークラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public enum QueryOperator {
	/** 比較演算子 == */
	EQ,
	/** 比較演算子 != */
	NE,
	/** 比較演算子 &lt; */
	LT,
	/** 比較演算子 &lt;= */
	LTE,
	/** 比較演算子 &gt; */
	GT,
	/** 比較演算子 &gt;= */
	GTE,
	/** bool演算子 ==true */
	IS,
	/** bool演算子 ==false */
	IS_NOT;

	/**
	 * bool値扱う演算子 (文字列) をFilterOperatorに変換する
	 *
	 * @param operator 演算子文字列
	 * @return 演算子
	 */
	public static QueryOperator compileOperatorBool(String operator) {
		if (operator == null) {
			return IS;
		}
		switch (operator) {
			case ":":
			case "=":
			case "==":
				return EQ;
			case "!=":
			case "!:":
				return NE;
			case "?":
				return IS;
			case "!":
			case "!?":
				return IS_NOT;
			default:
				return null;
		}
	}

	/**
	 * 数値を扱う演算子 (文字列) をFilterOperatorに変換する。
	 *
	 * @param operator 演算子文字列
	 * @return 演算子
	 */
	public static QueryOperator compileOperatorInt(String operator) {

		switch (operator) {
			case ":":
			case "=":
			case "==":
				return QueryOperator.EQ;
			case "!":
			case "!=":
			case "!:":
				return QueryOperator.NE;
			case ">":
				return QueryOperator.GT;
			case ">=":
				return QueryOperator.GTE;
			case "<":
				return QueryOperator.LT;
			case "<=":
				return QueryOperator.LTE;
			default:
				return null;
		}
	}

	/**
	 * 文字列を扱う演算子をFilterOperatorに変換する
	 *
	 * @param operator string演算子文字列
	 * @return 演算子
	 */
	public static QueryOperator compileOperatorString(String operator) {
		switch (operator) {
			case ":":
			case "=":
			case "==":
				return QueryOperator.EQ;
			case "!":
			case "!=":
			case "!:":
				return QueryOperator.NE;
			default:
				return null;
		}
	}

	/**
	 * 値を使いやすいように変換する
	 *
	 * @param value 値
	 * @return {@link #compare(String, Object)} に渡すことのできるObject
	 * @throws jp.mydns.turenar.twclient.filter.IllegalSyntaxException ぬるぽ→ガッ
	 */
	public static Object compileValueString(String value) throws IllegalSyntaxException {
		if (value == null) {
			throw new IllegalSyntaxException("string演算子には値が必要です");
		}
		if (value.length() >= 1 && value.charAt(0) == '/') {
			try {
				return Pattern.compile(value.substring(1));
			} catch (PatternSyntaxException e) {
				throw new IllegalSyntaxException(e);
			}
		} else {
			return value;
		}
	}

	private QueryOperator() {
	}

	/**
	 * boolを比較する。
	 *
	 * @param target 比較される側
	 * @param value  比較する側
	 * @return 演算子が成り立つときはtrue、それ以外はfalse
	 * @throws RuntimeException boolでは対応していない演算子
	 */
	public boolean compare(boolean target, boolean value) throws RuntimeException {
		switch (this) {
			case IS:
				return target;
			case IS_NOT:
				return !target;
			case EQ:
				return target == value;
			case NE:
				return target != value;
			default:
				throw new RuntimeException("boolでは対応していない演算子です");
		}
	}

	/**
	 * intを比較する。
	 *
	 * @param a 被比較数値
	 * @param b 比較数値
	 * @return 演算子が成り立つときはtrue、それ以外はfalse
	 * @throws RuntimeException longの比較では対応していない演算子
	 */
	public boolean compare(long a, long b) throws RuntimeException {
		switch (this) {
			case EQ:
				return a == b;
			case NE:
				return a != b;
			case LT:
				return a < b;
			case LTE:
				return a <= b;
			case GT:
				return a > b;
			case GTE:
				return a >= b;
			default:
				throw new RuntimeException("longの比較では対応していない演算子です");
		}
	}

	/**
	 * stringを比較する。
	 *
	 * @param target 被比較値
	 * @param value  比較値
	 * @return 演算子が成り立つときはtrue、それ以外はfalse
	 * @throws RuntimeException 比較失敗
	 */
	public boolean compare(String target, Object value) throws RuntimeException {
		if (this == EQ || this == NE) {
			if (value instanceof Pattern) {
				Matcher matcher = ((Pattern) value).matcher(target);
				return matcher.find() == (this == EQ);
			} else if (value instanceof String) {
				String str = (String) value;
				boolean contains;
				int len = str.length();
				if (len > 0 && str.charAt(0) == '*') { // start-wildcard
					if (/*len>0 && */str.charAt(len - 1) == '*') { // start+end-wildcard
						contains = target.contains(str.substring(1, len - 1));
					} else {
						contains = target.endsWith(str.substring(1));
					}
				} else {
					if (len > 0 && str.charAt(len - 1) == '*') { // end-wildcard
						contains = target.startsWith(str.substring(0, len - 1));
					} else {
						contains = target.equals(str);
					}
				}
				return contains == (this == EQ);
			} else {
				throw new RuntimeException("stringの被比較値が正しくありません");
			}
		} else {
			throw new RuntimeException("stringの比較では対応していない演算子です");
		}
	}

	/**
	 * 真偽値 (文字列) をbooleanに変換する
	 *
	 * @param propName プロパティ名
	 * @param value    値
	 * @return valueを変換した真偽値、あるいはvalueを必要としない場合false
	 * @throws IllegalSyntaxException エラー
	 */
	public boolean compileValueBool(String propName, String value) throws IllegalSyntaxException {
		switch (this) {
			case IS:
			case IS_NOT:
				if (value == null) {
					return false; // valid; no error
				} else {
					throw new IllegalSyntaxException("[" + propName + "] 値を指定する必要がありません");
				}
			case EQ:
			case NE:
				String lowerValue = value.toLowerCase(Locale.ENGLISH);
				switch (lowerValue) {
					case "false":
					case "no":
						return false;
					case "true":
					case "yes":
						return true;
					default:
						throw new IllegalSyntaxException("[" + propName + "] 値がbool型ではありません");
				}
			default:
				throw new IllegalSyntaxException("[" + propName + "] 正しくないbool演算子です");
		}
	}
}
