package jp.syuriken.snsw.twclient.media;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/08/18
 * Time: 18:44
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface MediaUrlDispatcher {
	void gotMediaUrl(String original, String url);

	void onException(String url, Exception ex);
}
