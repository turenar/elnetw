package jp.syuriken.snsw.twclient.internal;

import java.net.URL;
import java.net.URLConnection;

/**
 * Connection Store Class for NetworkSupport
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ConnectionInfo {
	private final URL url;
	private final FetchEventHandler handler;
	private final URLConnection connection;

	public ConnectionInfo(URL url, FetchEventHandler handler, URLConnection connection) {
		this.url = url;
		this.handler = handler;
		this.connection = connection;
	}

	public URLConnection getConnection() {
		return connection;
	}

	public FetchEventHandler getHandler() {
		return handler;
	}

	public URL getUrl() {
		return url;
	}
}
