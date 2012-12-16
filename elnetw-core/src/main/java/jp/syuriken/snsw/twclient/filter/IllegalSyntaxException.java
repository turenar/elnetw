package jp.syuriken.snsw.twclient.filter;

/**
 * 正しくない文法のクエリの時の例外クラス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings("serial")
public class IllegalSyntaxException extends Exception {

	/** クエリ周辺で抜き出す文字列の長さ */
	private static final int QUERY_PART_AROUND_LENGTH = 20;

	/** 関数名のトークン種類 */
	public static final int ID_FUNC_NAME = -1;

	/** 関数の引数のトークン種類 */
	public static final int ID_FUNC_ARGS = -5;

	/** プロパティ名のトークン種類 */
	public static final int ID_PROPERTY_NAME = -2;

	/** プロパティの演算子のトークン種類 */
	public static final int ID_PROPERTY_OPERATOR = -3;

	/** プロパティの値のトークン種類 */
	public static final int ID_PROPERTY_VALUE = -4;

	/** 不明のトークン種類 */
	public static final int ID_UNKNOWN = 0xf0000000;


	private static final String wrapChar(char character) {
		return character == 0xffff ? "<EOF>" : String.valueOf(character);
	}


	private String query;

	private int tokenType;

	private int tokenIndex = -1;


	/**
	 * インスタンスを生成する。
	 *
	 * @param expectedChar 予期した文字
	 * @param actualChar 実際の文字
	 * @param tokenType 処理中のトークンタイプ
	 */
	public IllegalSyntaxException(char expectedChar, char actualChar, String tokenType) {
		this("Syntax error: expected '" + expectedChar + "' but got '" + wrapChar(actualChar) + "' (token type:"
				+ tokenType + ")");
	}

	/**
	 * インスタンスを生成する。
	 * @param tokenType エラーの原因となったトークンの種類
	 * @param message 詳細メッセージ
	 */
	public IllegalSyntaxException(int tokenType, String message) {
		super(message);
		this.tokenType = tokenType;
	}

	/**
	 * インスタンスを生成する。
	 * @param tokenType
	 *   エラーの原因となったトークンの種類。ID_で始まる定数か、関数の引数によるエラーは1から始まる値を使用する。
	 * @param message 詳細メッセージ
	 * @param cause この例外クラスが作られる原因となった {@link Throwable}
	 */
	public IllegalSyntaxException(int tokenType, String message, Throwable cause) {
		super(message, cause);
		this.tokenType = tokenType;
	}

	/**
	 * インスタンスを生成する。
	 * @param message 詳細メッセージ
	 */
	public IllegalSyntaxException(String message) {
		this(ID_UNKNOWN, message);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param expectedChars 予期した文字列
	 * @param actualChar 実際の文字
	 * @param tokenType 処理中のトークンタイプ
	 */
	public IllegalSyntaxException(String expectedChars, char actualChar, String tokenType) {
		this("Syntax error: expected " + expectedChars + " but got '" + wrapChar(actualChar) + "' (token type:"
				+ tokenType + ")");
	}

	/**
	 * インスタンスを生成する。
	 * @param message 詳細メッセージ
	 * @param cause この例外クラスが作られる原因となった {@link Throwable}
	 */
	public IllegalSyntaxException(String message, Throwable cause) {
		this(ID_UNKNOWN, message, cause);
	}

	@Override
	public String getLocalizedMessage() {
		StringBuilder stringBuilder = new StringBuilder(super.getLocalizedMessage());
		String lineSeparator = System.getProperty("line.separator");
		if (query != null || tokenIndex != -1) {
			int tokenIndex = this.tokenIndex;
			int queryStart = tokenIndex > QUERY_PART_AROUND_LENGTH ? tokenIndex - QUERY_PART_AROUND_LENGTH : 0;
			int queryEnd =
					tokenIndex < query.length() - QUERY_PART_AROUND_LENGTH ? tokenIndex + QUERY_PART_AROUND_LENGTH
							: query.length();
			stringBuilder.append(lineSeparator).append(' ').append(query, queryStart, queryEnd);
			int queryArrowPosition = tokenIndex - queryStart;
			int i = 0;
			stringBuilder.append(lineSeparator).append(' ');
			for (; i < queryArrowPosition; i++) {
				stringBuilder.append('-');
			}
			stringBuilder.append('^');
		}
		return stringBuilder.toString();
	}

	/**
	 * この例外が発生する原因となったクエリを取得する
	 *
	 * @return クエリ
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * エラーの原因となったトークンのインデックスを取得する。一度も {@link #setTokenPostion(int)} が
	 * 呼び出されていないときは<code>-1</code>が返ります
	 *
	 * @return トークンのインデックス
	 */
	public int getTokenPosition() {
		return tokenIndex;
	}

	/**
	 * エラーの原因となったトークンの種類を取得する。設定されていないときは {@link #ID_UNKNOWN}
	 *
	 * @return
	 *   エラーの原因となったトークンの種類。正の数の時は関数の引数の番号。
	 *   負の数の時はこのクラスのID_で始まる定数です。
	 */
	public int getTokenType() {
		return tokenType;
	}

	/**
	 * この例外が発生する原因となったクエリを設定する
	 *
	 * @param query クエリ
	 */
	public void setQuery(String query) {
		if (query == null) {
			throw new IllegalStateException("IllegalSyntaxException#setQuery() は一度だけ呼び出すようにしてください。");
		}
		this.query = query;
	}

	/**
	 * エラー場所をtokenIndexに設定する。
	 *
	 * @param tokenIndex トークン場所
	 */
	public void setTokenPostion(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	/**
	 * エラーの原因となったトークンの種類を設定する。
	 *
	 * 関数の引数の番号 (1から始まる) を設定するか、ID_で始まる定数を設定する。
	 *
	 * @param tokenType the tokenType to set
	 */
	public void setTokenType(int tokenType) {
		this.tokenType = tokenType;
	}
}
