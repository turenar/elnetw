package jp.syuriken.snsw.twclient.init;

/** exception when initializing */
public class InitializeException extends Exception {
	private static final long serialVersionUID = -4813918292243379594L;

	private final String reason;

	private final int exitCode;

	private /*final*/ InitializerInfo initializerInfo;

	/**
	 * init
	 *
	 * @param cause cause
	 */
	public InitializeException(Throwable cause) {
		this(null, cause);
	}
	public InitializeException(InitializerInfo initializerInfo, String reason) {
		this(initializerInfo, null, reason);
	}
	public InitializeException(InitializerInfo initializerInfo, String reason, int exitCode) {
		this(initializerInfo, null, reason, exitCode);
	}

	public InitializeException(String message) {
		super(message);
		reason = message;
		exitCode = -1;
	}

	public InitializeException(InitializerInfo initializerInfo, Throwable cause,
			String reason) {
		this(initializerInfo, cause, reason, -1);
	}

	public InitializeException(InitializerInfo initializerInfo, Throwable cause,
			String reason, int exitCode) {
		super(reason, cause);
		this.initializerInfo = initializerInfo;
		this.reason = reason;
		this.exitCode = exitCode;
	}

	public InitializeException(String message, Throwable cause) {
		reason = cause.getLocalizedMessage();
		exitCode = -1;
	}

	public int getExitCode() {
		return exitCode;
	}

	public InitializerInfo getInitializerInfo() {
		return initializerInfo;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage() + "\ninitializer:" + initializerInfo;
	}

	public String getReason() {
		return reason;
	}

	/*package*/void setInitializerInfo(InitializerInfo info) {
		initializerInfo = info;
	}
}
