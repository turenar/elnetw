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

package jp.syuriken.snsw.twclient.net;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JLabel;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.internal.ConnectionInfo;
import jp.syuriken.snsw.twclient.internal.FetchEventHandler;
import jp.syuriken.snsw.twclient.internal.NetworkSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.User;

/**
 * 画像をキャッシュする。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ImageCacher {
	protected static class FetchEntry {
		private final ImageEntry imageEntry;
		private FetchEntry alternateEntry;
		private URL url;
		private ConnectionInfo connectionInfo;
		private ImageSetter setter;

		public FetchEntry(ImageEntry imageEntry) {
			this.imageEntry = imageEntry;
		}

		public synchronized void addSetter(ImageSetter setter) {
			if (this.setter == null) {
				this.setter = setter;
				if (alternateEntry != null) {
					alternateEntry.addSetter(setter);
				}
			} else {
				this.setter.addSetter(setter);
			}
		}

		public String getImageUrl() {
			return imageEntry.imageUrl;
		}

		public boolean isFinished() {
			return imageEntry.image != null;
		}


		public synchronized void setSetter(ImageSetter setter) {
			this.setter = setter;
			if (alternateEntry != null) {
				alternateEntry.setSetter(setter);
			}
		}
	}

	protected static class ImageEntry {
		public final String imageUrl;
		public byte[] rawData;
		public Image image;
		public Path cacheFile;

		public ImageEntry(String imageUrl) {
			this.imageUrl = imageUrl;
		}
	}

	protected class ErrorImageEntry extends ImageEntry {
		private final IOException ex;

		public IOException getException() {
			return ex;
		}

		public ErrorImageEntry(URL url, IOException ex) {
			super(url.toString());
			this.ex = ex;
		}
	}

	protected class ImageFetcher implements ParallelRunnable, FetchEventHandler {
		private FetchEntry entry;

		public ImageFetcher(FetchEntry entry) throws InterruptedException {
			this.entry = entry;
			entry.connectionInfo = NetworkSupport.openConnection(entry.url, this);
		}

		@Override
		public void onConnection(URLConnection connection) throws InterruptedException {
		}

		@Override
		public void onException(URLConnection connection, IOException e) {
			URL url = entry.url;
			if (connection instanceof HttpURLConnection) {
				int responseCode;
				try {
					responseCode = ((HttpURLConnection) connection).getResponseCode();
					if (responseCode >= 400 && responseCode < 500) {
						// url is not local cache
						cachedImages.put(entry.getImageUrl(), new ErrorImageEntry(url, e));
						if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
							logger.warn("not found: url={}", url);
						} else {
							logger.warn("Error while fetching: url={}, statusCode={}", url, responseCode);
						}
					}
				} catch (IOException responseCodeException) {
					logger.warn("Cannot retrieve http status code", responseCodeException);
				}
			} else {
				logger.warn("Error while fetching: {}", url, e);
			}
			if (entry.alternateEntry != null) {
				entry = entry.alternateEntry;
				configuration.addJob(this);
			}
		}

		@Override
		public void onLoaded(int imageLen) throws InterruptedException {
		}

		@Override
		public void run() {
			try {
				fetchImage(entry);
				ImageSetter setter = entry.setter;
				if (setter != null) {
					setter.setImageRecursively(entry.imageEntry.image);
				}
				fetchEntryMap.remove(entry.getImageUrl());
			} catch (InterruptedException e) {
				logger.warn("Interrupted: {}", entry.url);
			}
		}
	}

	protected class ImageFlusher implements ParallelRunnable {
		private ImageEntry entry;

		public ImageFlusher(ImageEntry entry) {
			this.entry = entry;
		}

		@Override
		public void run() {
			Path file = entry.cacheFile;
			if (!Files.exists(file)) {
				try {
					Path parentDir = file.getParent();
					Files.createDirectories(parentDir);
					Path tmpFile = parentDir.resolve("." + file.getFileName().toString() + ".tmp");
					OutputStream tmpStream = Files.newOutputStream(tmpFile);
					tmpStream.write(entry.rawData);
					tmpStream.close();
					Files.move(tmpFile, file);
					logger.trace("Flushed: {}", file);
				} catch (IOException e) {
					logger.warn("Write failed: {}", file, e);
				}
			}
	}
	}

	protected static final ThreadLocal<MessageDigest> shaHash = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				throw new AssertionError(e);
			}
		}
	};

	protected static String toHexString(byte[] digest) {
		int len = digest.length;
		char[] chars = new char[len * 2];
		int j = 0;
		for (byte b : digest) {
			int highBit = (b & 0xf0) >> 4;
			int lowBit = b & 0x0f;
			if (highBit <= 9) {
				chars[j++] = (char) (highBit + '0');
			} else {
				chars[j++] = (char) (highBit - 10 + 'a');
			}
			if (lowBit <= 9) {
				chars[j++] = (char) (lowBit + '0');
			} else {
				chars[j++] = (char) (lowBit - 10 + 'a');
			}
		}
		return String.valueOf(chars);
	}

	private final ClientConfiguration configuration;
	/** ユーザーアイコンのキャッシュ出力先ディレクトリ */
	public final File imageCacheDir;
	private final Logger logger = LoggerFactory.getLogger(ImageCacher.class);
	//	private final int flushThreshold;
//	private final int flushResetInterval;
	/*package*/ ConcurrentHashMap<String, ImageEntry> cachedImages = new ConcurrentHashMap<>();
	/*package*/ ConcurrentHashMap<String, FetchEntry> fetchEntryMap = new ConcurrentHashMap<>();

	/**
	 * インスタンスを生成する。
	 */
	public ImageCacher() {
		this.configuration = ClientConfiguration.getInstance();

//		flushThreshold = configuration.getConfigProperties().getInteger("core.cache.icon.flush_threshold");
//		flushResetInterval = configuration.getConfigProperties().getInteger("core.cache.icon.flush_reset_interval");

		imageCacheDir = new File(System.getProperty("elnetw.cache.dir"), "user");
	}

	/**
	 * 画像を取得する。
	 *
	 * @param entry イメージエントリ
	 */
	protected void fetchImage(FetchEntry entry) throws InterruptedException {
		synchronized (entry) {
			if (entry.isFinished()) {
				return;
			}

			byte[] imageData = NetworkSupport.fetchContents(entry.connectionInfo);
			ImageEntry imageEntry = entry.imageEntry;
			imageEntry.rawData = imageData;
			imageEntry.image = Toolkit.getDefaultToolkit().createImage(imageData);
			configuration.addJob(JobQueue.PRIORITY_IDLE, new ImageFlusher(imageEntry));
		}
	}

	private String getCacheFileName(String hash, String urlString) {
		int dotPosition = urlString.lastIndexOf('.');
		int slashPosition = urlString.lastIndexOf('/');
		String extension = dotPosition < slashPosition ? "" : urlString.substring(dotPosition);
		return imageCacheDir + "/" + hash.substring(0, 2) + '/' + hash.substring(2) + extension;
	}

	private FetchEntry getFetchEntry(String urlString, URL url) {
		ImageEntry imageEntry = new ImageEntry(urlString);
		String hash = toHexString(shaHash.get().digest(urlString.getBytes(ClientConfiguration.UTF8_CHARSET)));
		String cacheFileName = getCacheFileName(hash, urlString);
		imageEntry.cacheFile = Paths.get(cacheFileName);
		// from url
		FetchEntry fetchEntry = new FetchEntry(imageEntry);
		fetchEntry.url = url;
		// from cache
		Path cachePath = Paths.get(cacheFileName);
		if (Files.exists(cachePath)) {
			FetchEntry cacheFetchEntry = new FetchEntry(imageEntry);
			try {
				cacheFetchEntry.url = cachePath.toUri().toURL();
			} catch (MalformedURLException e) {
				throw new AssertionError(e);
			}
			cacheFetchEntry.alternateEntry = fetchEntry;
			fetchEntry = cacheFetchEntry;
		}else{
			logger.debug("Cache miss: {}", urlString);
		}
		return fetchEntry;
	}

	/**
	 * 指定したユーザーの画像を取得
	 *
	 * @param user ユーザー
	 * @return 画像がすでに取得されていればその画像、そうでなければnull
	 */
	public Image getImage(User user) {
		ImageEntry entry = cachedImages.get(user.getProfileImageURLHttps());
		return entry == null ? null : entry.image;
	}

	/**
	 * 指定したユーザーの画像を取得
	 *
	 * @param url URL
	 * @return 画像がすでに取得されていればその画像、そうでなければnull
	 */
	public Image getImage(URL url) {
		ImageEntry entry = cachedImages.get(url.toString());
		return entry == null ? null : entry.image;
	}

	/**
	 * URLの画像をストレージ上に保存し、そのファイル名を返す。
	 * この呼び出しは極力キャッシュされます。
	 *
	 * @param url 画像URL
	 * @return ストレージ上の画像ファイル。存在しない場合はnull。
	 */
	public File getImageFile(URL url) {
		return getImageFile(url.toString());
	}

	/**
	 * 画像ファイル名を取得する。
	 * @param url URL
	 * @return 画像ファイル名。まだキャッシュされていないときはnull。TODO 問答無用で取得させる。
	 */
	public File getImageFile(String url) {
		ImageEntry entry = cachedImages.get(url);
		return entry == null ? null : entry.cacheFile.toFile();
	}

	/**
	 * userのプロフィール画像をストレージ上に保存し、そのファイル名を返す。
	 * この呼び出しは極力キャッシュされます。
	 *
	 * @param user Twitterユーザー
	 * @return ストレージ上の画像ファイル。存在しない場合はnull。
	 */
	public File getImageFile(User user) throws InterruptedException {
		return getImageFile(user.getProfileImageURLHttps());
	}

	/**
	 * 画像を取得し、label.setIcon(...)する。
	 * おそらくlabel.setHorizontalAlignment(JLabel.CENTER)を呼び出す必要があるでしょう。
	 *
	 * @param label JLabelインスタンス
	 * @param url   画像URL
	 * @return キャッシュヒットしたかどうか
	 */
	public boolean setImageIcon(JLabel label, URL url) throws InterruptedException {
		return setImageIcon(new LabelImageSetter(label), url);
	}

	/**
	 * 画像を取得し、imageSetterを呼び出す。
	 *
	 * フェッチに失敗したときは{@link ImageSetter#onException(Exception, ConnectionInfo)}が呼び出される
	 * @param imageSetter 画像セッター
	 * @param url URL
	 * @return キャッシュヒットしたかどうか
	 * @throws InterruptedException 割り込まれた。
	 */
	public boolean setImageIcon(ImageSetter imageSetter, URL url) throws InterruptedException {
		String urlString = url.toString();
		ImageEntry imageEntry = cachedImages.get(urlString);
		if (imageEntry == null) {
			FetchEntry fetchEntry = fetchEntryMap.get(urlString);
			FetchEntry newEntry = null;
			if (fetchEntry == null) {
				newEntry = getFetchEntry(urlString, url);
				fetchEntry = fetchEntryMap.putIfAbsent(urlString, newEntry);
			}
			if (fetchEntry == null) {
				newEntry.addSetter(imageSetter);
				configuration.addJob(JobQueue.PRIORITY_UI, new ImageFetcher(newEntry));
				return false;
			} else {
				synchronized (fetchEntry) {
					if (fetchEntry.isFinished()) {
						imageEntry = fetchEntry.imageEntry;
					} else {
						fetchEntry.addSetter(imageSetter);
						return false;
					}
				}
			}
		}

		if(imageEntry instanceof ErrorImageEntry){
			return false;
		}else{
		imageSetter.setImageRecursively(imageEntry.image);
		return true;}
	}

	/**
	 * 指定されたユーザーのプロフィール画像を取得し、label.setIcon(...)する。
	 * おそらくlabel.setHorizontalAlignment(JLabel.CENTER)を呼び出す必要があるでしょう。
	 *
	 * @param label JLabelインスタンス
	 * @param user  Twitterユーザー
	 * @return キャッシュヒットしたかどうか。エラーキャッシュされている時もtrueを返しますが、画像は設定されません
	 */
	public boolean setImageIcon(JLabel label, User user) throws InterruptedException {
		try {
			return setImageIcon(new LabelImageSetter(label), new URL(user.getProfileImageURLHttps()));
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}
	}
}
