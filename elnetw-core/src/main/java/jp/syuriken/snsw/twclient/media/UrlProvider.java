package jp.syuriken.snsw.twclient.media;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/08/18
 * Time: 18:42
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UrlProvider {
	private static class MediaProviderInfo {
		public Pattern urlPattern;
		public MediaUrlProvider mediaProvider;

		public MediaProviderInfo(String urlPattern, MediaUrlProvider mediaProvider) {
			this.urlPattern = Pattern.compile(urlPattern);
			this.mediaProvider = mediaProvider;
		}
	}

	private static ReentrantReadWriteLock mediaProvidersLock = new ReentrantReadWriteLock();
	private static CopyOnWriteArrayList<MediaProviderInfo> mediaProviders = new CopyOnWriteArrayList<>();

	public static void addMediaProvider(String urlPattern, MediaUrlProvider mediaUrlProvider) {
		mediaProvidersLock.writeLock().lock();
		try {
			mediaProviders.add(new MediaProviderInfo(urlPattern, mediaUrlProvider));
		} finally {
			mediaProvidersLock.writeLock().unlock();
		}
	}

	public static void async(String url, MediaUrlDispatcher dispatcher) {
		MediaUrlProvider provider = getProvider(url);
		if (provider == null) {
			dispatcher.gotMediaUrl(url, url);
		} else {
			provider.async(url, dispatcher);
		}
	}

	public static void async(String url, MediaUrlDispatcher dispatcher, byte priority) {
		MediaUrlProvider provider = getProvider(url);
		if (provider == null) {
			dispatcher.gotMediaUrl(url, url);
		} else {
			provider.async(url, dispatcher, priority);
		}
	}

	public static MediaUrlProvider getProvider(String url) {
		mediaProvidersLock.readLock().lock();
		try {
			for (int i = mediaProviders.size() - 1; i >= 0; i--) {
				MediaProviderInfo info = mediaProviders.get(i);
				Matcher matcher = info.urlPattern.matcher(url);
				if (matcher.find()) {
					return info.mediaProvider;
				}
			}
			return null;
		} finally {
			mediaProvidersLock.readLock().unlock();
		}
	}

	public static String getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		MediaUrlProvider provider = getProvider(url);
		if (provider == null) {
			return null;
		} else {
			return provider.getUrl(url);
		}
	}
}
