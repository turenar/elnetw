package jp.syuriken.snsw.twclient.media;

import jp.syuriken.snsw.twclient.ClientConfiguration;

import static jp.syuriken.snsw.twclient.JobQueue.Priority;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/08/18
 * Time: 18:45
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractMediaUrlProviderImpl implements MediaUrlProvider {

	protected final ClientConfiguration configuration;

	protected AbstractMediaUrlProviderImpl() {
		configuration = ClientConfiguration.getInstance();
	}

	@Override
	public void async(String url, MediaUrlDispatcher dispatcher) {
		async(url, dispatcher, Priority.MEDIUM);
	}

	@Override
	public void async(final String url, final MediaUrlDispatcher dispatcher, byte priority) {
		configuration.addJob(priority, new Runnable() {
			@Override
			public void run() {
				try {
					dispatcher.gotMediaUrl(url, getUrl(url));
				} catch (Exception e) {
					dispatcher.onException(url, e);
				}
			}
		});
	}
}
