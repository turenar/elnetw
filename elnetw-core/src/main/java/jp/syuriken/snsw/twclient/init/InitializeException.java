package jp.syuriken.snsw.twclient.init;

/** exception when initializing */
public class InitializeException extends Exception {
	/**
	 * init
	 *
	 * @param cause cause
	 */
	public InitializeException(Throwable cause) {
		super(cause);
	}
}
