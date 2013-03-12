package jp.syuriken.snsw.twclient.init;

/** exception when initializing */
public class InitializeException extends Exception {
	private final String reason;

	private final int exitCode;

	private /*final*/ InitializerInfo initializerInfo;

	/**
	 * init
	 *
	 * @param cause cause
	 */
	public InitializeException(Throwable cause) {
		super(cause);
		reason = cause.getLocalizedMessage();
		exitCode = -1;
	}

	public InitializeException(InitializerInfo initializerInfo, String reason, int exitCode) {
		super(reason);
		this.initializerInfo = initializerInfo;
		this.reason = reason;
		this.exitCode = exitCode;
	}

	public InitializeException(String message) {
		super(message);
		reason = message;
		exitCode = -1;
	}

	public int getExitCode() {
		return exitCode;
	}

	public InitializerInfo getInitializerInfo() {
		return initializerInfo;
	}

	public String getReason() {
		return reason;
	}

	/*package*/void setInitializerInfo(InitializerInfo info) {
		initializerInfo = info;
	}
}
