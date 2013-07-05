package jp.syuriken.snsw.twclient;

import java.util.concurrent.locks.ReentrantLock;

import twitter4j.Twitter;

/**
 * テスト用の{@link ClientConfiguration}。継承するなりそのまま使うなり何なりと
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientConfigurationTestImpl extends ClientConfiguration {
	// proxy
	@Override
	public void setFetchScheduler(TwitterDataFetchScheduler fetchScheduler) {
		super.setFetchScheduler(fetchScheduler);
	}

	@Override
	public Twitter getTwitterForRead() {
		return null;
	}

	private static final ReentrantLock reentrantLock = new ReentrantLock();

	public void setGlobalInstance() {
		reentrantLock.lock();
		ClientConfiguration.setInstance(this);
	}

	public void clearGlobalInstance() {
		ClientConfiguration.setInstance(null);
		reentrantLock.unlock();
	}
}
