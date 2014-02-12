package jp.syuriken.snsw.twclient.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * twitpicのImage URLを取得するプロバイダー
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RegexpMediaResolver extends AbstractMediaUrlResolver {

	private static final int BUFSIZE = 65536;
	private final Pattern regexp;

	public RegexpMediaResolver(String regexp) {
		this.regexp = Pattern.compile("<img[^>]+src=[\"']?(" + regexp + ")[\"']?");
	}

	private String getContentsFromUrl(URL mediaUrl) throws IOException, InterruptedException {
		int bufLength;
		byte[] data;
		URLConnection connection = mediaUrl.openConnection();
		int contentLength = connection.getContentLength();
		InputStream stream = connection.getInputStream();

		bufLength = contentLength < 0 ? BUFSIZE : contentLength + 1;
		data = new byte[bufLength];
		int imageLen = 0;
		int loadLen;
		while ((loadLen = stream.read(data, imageLen, bufLength - imageLen)) != -1) {
			imageLen += loadLen;

			if (bufLength == imageLen) {
				bufLength = bufLength << 1;
				if (bufLength < 0) {
					bufLength = Integer.MAX_VALUE;
				}
				byte[] newData = new byte[bufLength];
				System.arraycopy(data, 0, newData, 0, imageLen);
				data = newData;
			}

			synchronized (this) {
				try {
					wait(1);
				} catch (InterruptedException e) {
					throw e;
				}
			}
		}
		stream.close(); // help keep-alive

		return new String(data, 0, imageLen);
	}

	@Override
	public String getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		URL mediaUrl;
		try {
			mediaUrl = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		String contents = getContentsFromUrl(mediaUrl);
		Matcher matcher = regexp.matcher(contents);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
}
