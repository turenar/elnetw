/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.media;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Url Resolver Manager. This provides url resolver delegation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UrlResolverManager {
	private static class MediaProviderInfo {
		public Pattern urlPattern;
		public MediaUrlResolver mediaProvider;

		public MediaProviderInfo(String urlPattern, MediaUrlResolver mediaProvider) {
			this.urlPattern = Pattern.compile(urlPattern);
			this.mediaProvider = mediaProvider;
		}
	}

	private static ReentrantReadWriteLock mediaResolversLock = new ReentrantReadWriteLock();
	private static CopyOnWriteArrayList<MediaProviderInfo> mediaResolvers = new CopyOnWriteArrayList<>();

	public static void addMediaProvider(String urlPattern, MediaUrlResolver mediaUrlProvider) {
		mediaResolversLock.writeLock().lock();
		try {
			mediaResolvers.add(new MediaProviderInfo(urlPattern, mediaUrlProvider));
		} finally {
			mediaResolversLock.writeLock().unlock();
		}
	}

	public static void async(String url, MediaUrlDispatcher dispatcher) {
		MediaUrlResolver resolver = getProvider(url);
		if (resolver == null) {
			dispatcher.gotMediaUrl(url, url);
		} else {
			resolver.async(url, dispatcher);
		}
	}

	public static void async(String url, MediaUrlDispatcher dispatcher, byte priority) {
		MediaUrlResolver resolver = getProvider(url);
		if (resolver == null) {
			dispatcher.gotMediaUrl(url, url);
		} else {
			resolver.async(url, dispatcher, priority);
		}
	}

	public static MediaUrlResolver getProvider(String url) {
		mediaResolversLock.readLock().lock();
		try {
			for (int i = mediaResolvers.size() - 1; i >= 0; i--) {
				MediaProviderInfo info = mediaResolvers.get(i);
				Matcher matcher = info.urlPattern.matcher(url);
				if (matcher.find()) {
					return info.mediaProvider;
				}
			}
			return null;
		} finally {
			mediaResolversLock.readLock().unlock();
		}
	}

	public static String getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		MediaUrlResolver provider = getProvider(url);
		if (provider == null) {
			return null;
		} else {
			return provider.getUrl(url);
		}
	}
}
