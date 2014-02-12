package jp.syuriken.snsw.twclient.media;

import java.io.IOException;

/**
 * Media URL Resolver
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MediaUrlResolver {
	void async(String url, MediaUrlDispatcher dispatcher);

	void async(String url, MediaUrlDispatcher dispatcher, byte priority);

	String getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException;
}
