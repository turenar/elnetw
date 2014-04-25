package jp.syuriken.snsw.twclient.media;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import jp.syuriken.snsw.twclient.internal.FetchEventHandler;
import jp.syuriken.snsw.twclient.internal.NetworkSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provide getContentsFromUrl
 */
public abstract class AbstractMediaResolver implements MediaUrlResolver {
	private static class MyFetchEventHandler implements FetchEventHandler {

		private String contentEncoding;

		public String getContentEncoding() {
			return contentEncoding;
		}

		@Override
		public void onConnection(URLConnection connection) throws InterruptedException {
			contentEncoding = connection.getContentEncoding();
		}

		@Override
		public void onException(URLConnection connection, IOException e) {
			logger.warn("fetch", e);
		}

		@Override
		public void onLoaded(int imageLen) throws InterruptedException {
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(RegexpMediaResolver.class);

	public static String getContentsFromUrl(URL mediaUrl) throws IOException, InterruptedException {
		MyFetchEventHandler handler = new MyFetchEventHandler();
		byte[] contents = NetworkSupport.fetchContents(mediaUrl, handler);

		Charset charset = Charset.forName("UTF-8");
		try {
			String encoding = handler.getContentEncoding();
			if (encoding != null) {
				charset = Charset.forName(encoding);
			}
		} catch (UnsupportedCharsetException e) {
			logger.warn("Invalid Charset", e);
		}
		return new String(contents, charset);
	}
}
