package jp.syuriken.snsw.twclient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.syuriken.snsw.twclient.net.TwitterDataFetchScheduler;
import twitter4j.Twitter;

/**
 * テスト用の{@link ClientConfiguration}。継承するなりそのまま使うなり何なりと
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientConfigurationTestImpl extends ClientConfiguration {
	private static final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();

	public void clearGlobalInstance() {
		ClientConfiguration.setInstance(null);
		reentrantLock.readLock().unlock();
	}

	@Override
	public Twitter getTwitterForRead() {
		return null;
	}

	// proxy
	@Override
	public void setFetchScheduler(TwitterDataFetchScheduler fetchScheduler) {
		super.setFetchScheduler(fetchScheduler);
	}

	public void setGlobalInstance() {
		while (true) {
			reentrantLock.readLock().lock();
			if (ClientConfiguration.getInstance() == this) {
				break;
			} else {
				reentrantLock.readLock().unlock();
				try {
					if (reentrantLock.writeLock().tryLock(1, TimeUnit.MILLISECONDS)) {
						ClientConfiguration.setInstance(this);
						reentrantLock.readLock().lock();
						reentrantLock.writeLock().unlock();
						break;
					}
				} catch (InterruptedException e) {
					//do nothing
				}
			}
		}
	}
}
