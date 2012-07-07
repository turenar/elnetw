package jp.syuriken.snsw.twclient.filter;

/**
 * トークンタイプ
 * 
 * @author $Author$
 */
public enum TokenType {
	/*
	 * <query>		::= <query_op>
	 * <query_op>	::= <func>|<prop>
	 * <func>		::= <func_name> ( <query_op> { , <query_op>} )
	 * <prop>		::= <prop_name>[ <operator> <scalar>]
	 *==========
	 *and( userid:99999, text:"/[#＃]aaa \"まみむめも\"", not(faved?), favcount<9)
	 * and		-> FUNC_NAME
	 * (		-> FUNC_START
	 * userid	-> PROPERTY_NAME
	 * :			-> PROPERTY_OPERATOR
	 * 99999	-> SCALAR_INT
	 * ,		-> FUNC_ARG_SEPARATOR
	 * text		-> PROPERTY_NAME
	 * :			-> PROPERTY_OPERATOR
	 * "		-> SCALAR_STRING_START
	 * /[#...も"	-> SCALAR_STRING
	 * "		-> SCALAR_STRING_END
	 *  :(snip)
	 * favcount	-> PROPERTY_NAME
	 * <		-> PROPERTY_OPERATOR
	 * 9		-> SCALAR_INT
	 * )		-> FUNC_END
	 */
	
	/** デフォルトの状態 (トークンが分けられていない状態; トークンのタイプとしては正しくない */
	DEFAULT,
	/** 関数名 */
	FUNC_NAME,
	/** 関数のはじめの括弧 '(' */
	FUNC_START,
	/** 関数のargumentを分ける ',' */
	FUNC_ARG_SEPARATOR,
	/** 関数の終わりの括弧 ')' */
	FUNC_END,
	/** プロパティ名 */
	PROPERTY_NAME,
	/** プロパティ演算子 */
	PROPERTY_OPERATOR,
	/** 数値 */
	SCALAR_INT,
	/** 文字列のはじめの '"' */
	SCALAR_STRING_START,
	/** 文字列の中身 */
	SCALAR_STRING,
	/** 文字列の終わりの '"' */
	SCALAR_STRING_END,
	/** 予期しない状態 (正しくない状態で EndOfData に達した時など) */
	UNEXPECTED,
}
