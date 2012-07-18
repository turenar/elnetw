package jp.syuriken.snsw.twclient.filter;

import java.util.Locale;

import jp.syuriken.snsw.twclient.Utility;

/**
 * プロパティの演算子の処理を楽にするためのユーティリティークラス。
 * 
 * @author $Author$
 */
public enum FilterOperator {
	/** 比較演算子 == */
	EQ,
	/** 比較演算子 != */
	NE,
	/** 比較演算子 < */
	LT,
	/** 比較演算子 <= */
	LTE,
	/** 比較演算子 > */
	GT,
	/** 比較演算子 >= */
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
					return FilterOperator.EQ;
				case '?':
					return IS;
				case '!':
					return FilterOperator.IS_NOT;
				default:
					return null;
			}
		} else if (Utility.equalString("!?", operator)) {
			return IS_NOT;
		} else if (Utility.equalString("==", operator)) {
			return FilterOperator.EQ;
		} else if (Utility.equalString("!=", operator) || Utility.equalString("!:", operator)) {
			return FilterOperator.NE;
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
