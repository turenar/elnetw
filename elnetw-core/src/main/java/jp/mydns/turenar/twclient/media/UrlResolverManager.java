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
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.media;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.internal.ConcurrentSoftHashMap;

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
	private static ConcurrentSoftHashMap<String, UrlInfo> cacheMap = new ConcurrentSoftHashMap<>();
	private static ClientConfiguration configuration = ClientConfiguration.getInstance();

	public static void addMediaProvider(String urlPattern, MediaUrlResolver mediaUrlProvider) {
		mediaResolversLock.writeLock().lock();
		try {
			mediaResolvers.add(new MediaProviderInfo(urlPattern, mediaUrlProvider));
		} finally {
			mediaResolversLock.writeLock().unlock();
		}
	}

	/**
	 * 非同期的にurlを解決する。
	 *
	 * @param url        URL
	 * @param dispatcher 解決されたURLを受け取るハンドラ。null不可
	 */
	public static void async(String url, MediaUrlDispatcher dispatcher) {
		async(url, dispatcher, JobQueue.PRIORITY_DEFAULT);
	}

	/**
	 * 非同期的にurlを解決する。
	 *
	 * @param url        URL
	 * @param dispatcher 解決されたURLを受け取るハンドラ。null不可
	 * @param priority   ジョブ優先度
	 */
	public static void async(final String url, final MediaUrlDispatcher dispatcher, byte priority) {
		UrlInfo cachedUrl = getCachedUrl(url);
		if (cachedUrl != null) {
			dispatcher.gotMediaUrl(url, cachedUrl);
		} else {
			configuration.addParallelJob(priority, () -> {
				try {
					dispatcher.gotMediaUrl(url, getUrl(url));
				} catch (Exception e) {
					dispatcher.onException(url, e);
				}
			});
		}
	}

	/**
	 * キャッシュされたURL情報を再帰的に確認しながら取得する
	 *
	 * @param url URL
	 * @return キャッシュ済みURL情報。nullの可能性あり。
	 */
	public static UrlInfo getCachedUrl(String url) {
		UrlInfo oldCachedUrl = null;
		UrlInfo cachedUrl;
		String processingUrl = url;
		while (true) {
			// キャッシュの確認
			cachedUrl = cacheMap.get(processingUrl);
			if (cachedUrl == null || cachedUrl.getResolvedUrl().equals(processingUrl)) {
				// キャッシュが見つからないときは最後に確認できたキャッシュを返す
				return oldCachedUrl;
			} else if (!cachedUrl.shouldRecursive()) {
				// キャッシュは見つかったけどこれ以上確認する必要はない
				return cachedUrl;
			} else {
				// まだ確認する必要があるかもしれない
				oldCachedUrl = cachedUrl;
				processingUrl = cachedUrl.getResolvedUrl();
			}
		}
	}

	/**
	 * get resolver for url
	 *
	 * @param url url
	 * @return resolver
	 */
	public static MediaUrlResolver getResolver(String url) {
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

	/**
	 * urlを解決する。
	 *
	 * @param url URL
	 * @return 解決されたURL情報。nullの可能性あり。
	 * @throws IllegalArgumentException urlとして正しくない
	 * @throws InterruptedException     スレッドをブロックを必要とする処理中に割り込まれた
	 * @throws IOException              解決中にIO例外が発生した
	 */
	public static UrlInfo getUrl(String url) throws IllegalArgumentException, InterruptedException, IOException {
		UrlInfo oldUrl = null;
		String processingUrl = url;
		while (true) {
			// キャッシュのチェック
			UrlInfo cachedUrl = getCachedUrl(processingUrl);
			if (cachedUrl != null) {
				// キャッシュに存在する場合は、再帰的に解決すべきかどうかを確認する。
				// またresolvedUrl==processingUrl (resolvedUrlはこれ以上解決のしようがない) かどうかも確認する
				// あてはまる場合はキャッシュを返す。
				if (!cachedUrl.shouldRecursive() || cachedUrl.getResolvedUrl().equals(processingUrl)) {
					return cachedUrl; // avoid inf-loop
				}
				// resolvedUrlは解決できる可能性がある場合 (=前回の解決時にエラー落ち？)
				// それをもとに解決を再開する。
				oldUrl = cachedUrl;
				processingUrl = cachedUrl.getResolvedUrl();
				continue;
			}
			// 解決してくれそうなクラスを取得する。
			MediaUrlResolver provider = getResolver(url);
			if (provider == null) {
				// 解決できそうもないよ
				return oldUrl;
			}
			// resolveを試してみる
			UrlInfo resolvedUrl = provider.getUrl(processingUrl);
			if (resolvedUrl == null) {
				// やっぱり解決できなかった
				// 一度も解決できなかった時はエラー (=これ以上解決できないもの) として保存
				cacheMap.put(processingUrl, oldUrl == null ? new UrlInfo(url) : oldUrl);
				return oldUrl;
			} else {
				// 解決できた
				// キャッシュに保存する
				cacheMap.put(processingUrl, resolvedUrl);
				if (!resolvedUrl.shouldRecursive()) {
					// 再帰的に解決しなくてもいい時＝終了
					return resolvedUrl;
				}
				// 次のresolveを試す。
				processingUrl = resolvedUrl.getResolvedUrl();
				oldUrl = resolvedUrl;
			}
		}
	}

	private UrlResolverManager() {
	}
}
