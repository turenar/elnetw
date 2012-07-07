package jp.syuriken.snsw.twclient.filter;

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
	GTE;
	
	/**
	 * 比較する。 {@link #compare(long, long)} インスタンスメソッドと同じ。
	 * 
	 * @param filterOperator 演算子
	 * @param a 被比較数値
	 * @param b 比較数値
	 * @return 演算子が成り立つときはtrue、それ以外はfalse
	 */
	public static boolean compare(FilterOperator filterOperator, long a, long b) {
		return filterOperator.compare(a, b);
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
	 * 比較する。 {@link #compare(FilterOperator, long, long)} インスタンスメソッドと同じ。
	 * 
	 * @param a 被比較数値
	 * @param b 比較数値
	 * @return 演算子が成り立つときはtrue、それ以外はfalse
	 */
	public boolean compare(long a, long b) {
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
}
