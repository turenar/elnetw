package jp.syuriken.snsw.twclient.filter;

/**
 * 正しくない文法のクエリの時の例外クラス。
 * 
 * @author $Author$
 */
@SuppressWarnings("serial")
public class IllegalSyntaxException extends Exception {
	
	private static final String wrapChar(char character) {
		return character == FilterCompiler.EOD_CHAR ? "<EOF>" : String.valueOf(character);
	}
	
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
	 * 
	 * @param message 詳細メッセージ
	 */
	public IllegalSyntaxException(String message) {
		super(message);
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
	 * 
	 * @param message 詳細メッセージ
	 * @param cause この例外クラスが作られる原因となった {@link Throwable}
	 */
	public IllegalSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
}
