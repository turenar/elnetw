package jp.syuriken.snsw.twclient.init;

import jp.syuriken.snsw.twclient.ClientConfiguration;

/** Information for @{@link Initializer} */
public interface InitCondition {

	/** clear fail status */
	void clearFailStatus();

	/**
	 * get ClientConfiguration
	 *
	 * @return configuration
	 */
	ClientConfiguration getConfiguration();

	/**
	 * check this invocation is initializing stage
	 *
	 * @return if true, initializing. otherwise, de-initializing.
	 */
	boolean isInitializingPhase();

	/**
	 * Check already called {@link #setFailStatus(String, int)}
	 *
	 * @return
	 */
	boolean isSetFailStatus();

	/**
	 * set status as fail
	 *
	 * @param reason   fail reason
	 * @param exitCode exit code
	 */
	void setFailStatus(String reason, int exitCode);
}
