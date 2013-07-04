package jp.syuriken.snsw.twclient;

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

	public void setInstance() {
		ClientConfiguration.setInstance(this);
	}

	public void clearInstance(){
		ClientConfiguration.setInstance(null);
	}
}
