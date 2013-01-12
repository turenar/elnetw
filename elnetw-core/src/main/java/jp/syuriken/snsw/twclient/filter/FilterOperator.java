package jp.syuriken.snsw.twclient.filter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.syuriken.snsw.twclient.Utility;

/**
 * プロパティの演算子の処理を楽にするためのユーティリティークラス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public enum FilterOperator {
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
	public static FilterOperator compileOperatorBool(String operator) {
		if (operator == null) {
			return IS;
		} else if (operator.length() == 1) {
			switch (operator.charAt(0)) {
				case ':':
				case '=':
					return EQ;
				case '?':
					return IS;
				case '!':
					return IS_NOT;
				default:
					return null;
			}
		} else if (Utility.equalString("!?", operator)) {
			return IS_NOT;
		} else if (Utility.equalString("==", operator)) {
			return EQ;
		} else if (Utility.equalString("!=", operator) || Utility.equalString("!:", operator)) {
			return NE;
		}
		return null;
	}

	/**
	 * 数値を扱う演算子 (文字列) をFilterOperatorに変換する。
	 *
	 * @param operator 演算子文字列
	 * @return 演算子
	 */
	public static FilterOperator compileOperatorInt(String operator) {
		if (operator.length() == 1) {
			switch (operator.charAt(0)) {
				case ':':
				case '=':
					return FilterOperator.EQ;
				case '!':
					return FilterOperator.NE;
				case '>':
					return FilterOperator.GT;
				case '<':
					return FilterOperator.LT;
				default:
					return null;
			}
		} else if (Utility.equalString(">=", operator)) {
			return FilterOperator.GTE;
		} else if (Utility.equalString("<=", operator)) {
			return FilterOperator.LTE;
		} else if (Utility.equalString("==", operator)) {
			return FilterOperator.EQ;
		} else if (Utility.equalString("!=", operator) || Utility.equalString("!:", operator)) {
			return FilterOperator.NE;
		}
		return null;
	}

	/**
	 * 文字列を扱う演算子をFilterOperatorに変換する
	 *
	 * @param operator string演算子文字列
	 * @return 演算子
	 */
	public static FilterOperator compileOperatorString(String operator) {
		if (operator.length() == 1) {
			switch (operator.charAt(0)) {
				case ':':
				case '=':
					return FilterOperator.EQ;
				case '!':
					return FilterOperator.NE;
				default:
					return null;
			}
		} else if (Utility.equalString("==", operator)) {
			return FilterOperator.EQ;
		} else if (Utility.equalString("!=", operator) || Utility.equalString("!:", operator)) {
			return FilterOperator.NE;
		}
		return null;
	}

	/**
	 * 値を使いやすいように変換する
	 *
	 * @param value 値
	 * @return {@link #compare(String, Object)} に渡すことのできるObject
	 * @throws IllegalSyntaxException ぬるぽ→ガッ
	 */
	public static Object compileValueString(String value) throws IllegalSyntaxException {
		if (value == null) {
			throw new IllegalSyntaxException("string演算子には値が必要です");
		}
		if (value.length() >= 1 && value.charAt(0) == '/') {
			return Pattern.compile(value.substring(1));
		} else {
			return value;
		}
	}

	private FilterOperator() {
	}

	/**
	 * boolを比較する。
	 *
	 * @param target 比較される側
	 * @param value 比較する側
	 * @return 演算子が成り立つときはtrue、それ以外はfalse
	 * @throws RuntimeException boolでは対応していない演算子
	 */
	public boolean compare(boolean target, boolean value) throws RuntimeException {
		switch (this) {
			case IS:
				return target;
			case IS_NOT:
				return target == false;
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
	 * @param value 比較値
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
						contains = Utility.equalString(target, str);
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
	 * @param value 値
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
				if (Utility.equalString(lowerValue, "false") || Utility.equalString(lowerValue, "no")) {
					return false;
				} else if (Utility.equalString(lowerValue, "true") || Utility.equalString(lowerValue, "yes")) {
					return true;
				} else {
					throw new IllegalSyntaxException("[" + propName + "] 値がbool型ではありません");
				}
			default:
				throw new IllegalSyntaxException("[" + propName + "] 正しくないbool演算子です");
		}
	}
}
