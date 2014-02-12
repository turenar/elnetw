package jp.syuriken.snsw.twclient.media;

import java.io.IOException;

/**
 * 何もしないMediaUrlProvider
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class NullMediaResolver implements MediaUrlResolver {
	@Override
	public void async(String url, MediaUrlDispatcher dispatcher) {
		dispatcher.gotMediaUrl(url, url);
	}

	@Override
	public void async(String url, MediaUrlDispatcher dispatcher, byte priority) {
		dispatcher.gotMediaUrl(url, url);
	}

	@Override
	public String getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		return url;
	}
}
