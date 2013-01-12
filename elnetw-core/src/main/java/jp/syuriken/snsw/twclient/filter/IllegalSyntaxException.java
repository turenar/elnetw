package jp.syuriken.snsw.twclient.filter;

/**
 * 正しくない文法のクエリの時の例外クラス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings("serial")
public class IllegalSyntaxException extends Exception {

	/**
	 * インスタンスを生成する。
	 * @param message 詳細メッセージ
	 */
	public IllegalSyntaxException(String message) {
		super(message);
	}

	/**
	 * インスタンスを生成する。
	 * @param message 詳細メッセージ
	 * @param cause この例外クラスが作られる原因となった {@link Throwable}
	 */
	public IllegalSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
}
