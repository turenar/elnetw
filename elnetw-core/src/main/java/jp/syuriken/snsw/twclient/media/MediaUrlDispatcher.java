package jp.syuriken.snsw.twclient.media;

/**
 * Dispatcher for url resolver
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MediaUrlDispatcher {
	/**
	 * resolved media url
	 *
	 * If url can't resolve, url is as well as original
	 *
	 * @param original original url
	 * @param url      resolved url. if original url can't be resolved by resolver, url is just original url
	 */
	void gotMediaUrl(String original, String url);

	/**
	 * got exception during url resolving
	 *
	 * @param url original url
	 * @param ex  exception
	 */
	void onException(String url, Exception ex);
}
