package jp.syuriken.snsw.twclient;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.User;

/**
 * 画像をキャッシュする。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ImageCacher {

	/**
	 * 画像情報を保存するエントリ。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected static class ImageEntry {

		/** 画像キー */
		public final String imageKey;
		/** イメージURL */
		public volatile URL url;
		/** Image インスタンス */
		public Image image;
		/** キャッシュ先のファイル */
		public File cacheFile;
		/** 生の画像データ */
		public byte[] rawimage;
		/** すでに書きこまれたかどうか */
		protected volatile boolean isWritten = false;
		/** 指定時間内の出現回数 */
		public volatile int appearCount;
		/** カウント終了時間 (ms) */
		public volatile long countEndTime;

		/**
		 * インスタンスを生成する。
		 *
		 * @param url URL
		 */
		public ImageEntry(URL url) {
			this.url = url;
			imageKey = url.toString();
		}

		/**
		 * インスタンスを生成する。
		 *
		 * @param url      URL
		 * @param imageKey 画像キー
		 */
		public ImageEntry(URL url, String imageKey) {
			this.url = url;
			this.imageKey = imageKey;
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("ImageEntry{imageKey=").append(imageKey).append(",url=").append(url.toString())
					.append(",cacheFile=").append(cacheFile == null ? "null" : cacheFile.getPath())
					.append(",rawimage=byte[").append(rawimage == null ? "null" : rawimage.length).append(
					"],isWritten=")
					.append(isWritten).append(",appearCount=").append(appearCount).append("}");
			return stringBuilder.toString();
		}
	}

	/**
	 * イメージフェッチャ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class ImageFetcher implements ParallelRunnable {

		/** イメージエントリ */
		public final ImageEntry entry;
		/** イメージアイコンを設定するJLabel */
		public final JLabel label;

		/**
		 * インスタンスを生成する。
		 *
		 * @param entry イメージエントリ
		 * @param label イメージアイコンを設定するJLabel
		 */
		public ImageFetcher(ImageEntry entry, JLabel label) {
			this.entry = entry;
			this.label = label;
		}

		@Override
		public void run() {
			final ImageEntry entry = this.entry;
			if (cachedImages.containsKey(entry.imageKey)) {
				synchronized (entry) {
					label.setIcon(getImageIcon(cachedImages.get(entry.imageKey).image));
					incrementAppearCount(entry);
				}
				return;
			}

			try {
				fetchImage(entry);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			if (label != null) {
				label.setIcon(getImageIcon(entry.image));
				incrementAppearCount(entry);
			}
		}
	}

	/**
	 * イメージを恒久的保存するジョブ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class ImageFlusher implements ParallelRunnable {

		private ImageEntry entry;

		/**
		 * インスタンスを生成する。
		 *
		 * @param entry イメージエントリ
		 */
		public ImageFlusher(ImageEntry entry) {
			this.entry = entry;
		}

		@Override
		public void run() {
			flushImage(entry);
		}
	}

	/** Buffer size */
	private static final int BUFSIZE = 65536;
	private final ClientConfiguration configuration;
	/** キャッシュ出力先ディレクトリ */
	public final File cacheDir;
	/** ユーザーアイコンのキャッシュ出力先ディレクトリ */
	public final File userIconCacheDir;
	private final Logger logger = LoggerFactory.getLogger(ImageCacher.class);
	/** キャッシュ有効時間 */
	private final long cacheExpire;
	private final int flushThreshold;
	private final int flushResetInterval;
	/*private*/ ConcurrentHashMap<String, ImageEntry> cachedImages = new ConcurrentHashMap<>();

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration ClientConfigurationインスタンス。
	 */
	public ImageCacher(ClientConfiguration configuration) {
		this.configuration = configuration;
		cacheExpire = configuration.getConfigProperties().getLong("core.cache.icon.survive_time");
		flushThreshold = configuration.getConfigProperties().getInteger("core.cache.icon.flush_threshold");
		flushResetInterval = configuration.getConfigProperties().getInteger("core.cache.icon.flush_reset_interval");

		switch (Utility.getOstype()) {
			case WINDOWS:
				cacheDir = new File(System.getProperty("java.io.tmpdir") + "/elnetw/cache");
				break;
			default:
				cacheDir = new File(System.getProperty("user.home") + "/.cache/elnetw");
				break;
		}
		userIconCacheDir = new File(cacheDir, "user");

		cleanOldUserIconCache(userIconCacheDir);
	}

	/**
	 * ディスクキャッシュから期限切れのユーザーアイコンを削除する。
	 *
	 * @param directory 再起対象ディレクトリ
	 */
	private void cleanOldUserIconCache(File directory) {
		File[] listFiles = directory.listFiles();
		if (listFiles == null) {
			return;
		}
		for (File file : listFiles) {
			if (file.isDirectory()) {
				cleanOldUserIconCache(file);
			}

			long lastModified = file.lastModified();
			if (lastModified == 0) {
				continue;
			} else if (lastModified + cacheExpire < System.currentTimeMillis()) {
				if (file.delete()) {
					logger.debug("clean expired cache: {} (lastModified:{})", Utility.protectPrivacy(file.getPath()),
							lastModified);
					continue;
				} else {
					logger.warn("Failed cleaning cache: {}", Utility.protectPrivacy(file.getPath()));
				}
			}
		}
	}

	/**
	 * 画像を取得する。
	 *
	 * @param entry イメージエントリ
	 */
	protected void fetchImage(ImageEntry entry) throws InterruptedException {
		URL url = entry.url;
		if (cachedImages.containsKey(entry.imageKey)) {
			return;
		}

		synchronized (entry) {
			if (entry.cacheFile != null && entry.cacheFile.exists()) {
				try {
					File cacheFile = entry.cacheFile;
					logger.debug("loadCache: file={}", cacheFile.getPath());
					entry.url = cacheFile.toURI().toURL();
					entry.isWritten = true;
				} catch (MalformedURLException e) {
					// would never happen
					throw new AssertionError(e);
				}
			}

			URLConnection connection;
			try {
				connection = url.openConnection();
				int contentLength = connection.getContentLength();
				InputStream stream = connection.getInputStream();

				int bufLength = contentLength < 0 ? BUFSIZE : contentLength + 1;
				byte[] data = new byte[bufLength];
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

					logger.trace("Image: Loaded {} bytes: buffer {}/{}", loadLen, imageLen, bufLength);

					synchronized (this) {
						try {
							wait(1);
						} catch (InterruptedException e) {
							throw e;
						}
					}
				}
				stream.close(); // help keep-alive

				entry.rawimage = Arrays.copyOfRange(data, 0, bufLength);
				Image image = Toolkit.getDefaultToolkit().createImage(entry.rawimage);
				entry.image = image;
				cachedImages.put(entry.imageKey, entry);
			} catch (IOException e) {
				logger.warn(MessageFormat.format("Error while fetching: {0}", url), e);
			}
		}
	}

	/**
	 * 画像をストレージに書きこむ
	 *
	 * @param entry エントリ
	 * @return 書き込みが成功したかどうか
	 */
	protected boolean flushImage(ImageEntry entry) {
		synchronized (entry) {
			if (!entry.isWritten) {
				if (entry.cacheFile == null) {
					return false;
				}
				FileOutputStream outputStream = null;
				try {
					File file = entry.cacheFile;
					File dirName = file.getParentFile();
					if (!dirName.exists()) {
						if (!dirName.mkdirs()) {
							logger.warn("{} is not exist and can't mkdir", Utility.protectPrivacy(dirName.getPath()));
						}
					}
					outputStream = new FileOutputStream(file);
					outputStream.write(entry.rawimage);
					entry.isWritten = true;
					logger.debug("Flushed: {}", Utility.protectPrivacy(entry.cacheFile.getPath()));
					/*} catch (FileNotFoundException e) {*/
				} catch (IOException e) {
					StringBuilder stringBuilder = new StringBuilder("Failed flushing cache: ");
					stringBuilder.append(Utility.protectPrivacy(entry.cacheFile.getPath()));
					logger.warn(stringBuilder.toString(), e);
					return false;
				} finally {
					try {
						if (outputStream != null) {
							outputStream.close();
						}
					} catch (IOException e) {
						StringBuilder stringBuilder = new StringBuilder("Failed close file: ");
						stringBuilder.append(Utility.protectPrivacy(entry.cacheFile.getPath()));

						logger.warn(stringBuilder.toString(), e);
					}
				}
			}
			return true;
		}
	}

	/**
	 * 指定したユーザーの画像を取得
	 *
	 * @param user ユーザー
	 * @return 画像がすでに取得されていればその画像、そうでなければnull
	 */
	public Image getImage(User user) {
		String imageKey = getImageKey(user);
		String url = user.getProfileImageURL();
		ImageEntry entry = cachedImages.get(imageKey);
		if (entry == null) {
			try {
				entry = new ImageEntry(new URL(url), imageKey);
			} catch (MalformedURLException e) {
				throw new AssertionError(e); // would never happen
			}
			entry.cacheFile = getImageFilename(user);
			configuration.addJob(new ImageFetcher(entry, null));
			return null;
		} else {
			incrementAppearCount(entry);
			return entry.image;
		}
	}

	/**
	 * URLの画像をストレージ上に保存し、そのファイル名を返す。
	 * この呼び出しは極力キャッシュされます。
	 *
	 * @param url 画像URL
	 * @return ストレージ上の画像ファイル。存在しない場合はnull。
	 */
	public File getImageFile(URL url) {
		return null;
	}

	/**
	 * userのプロフィール画像をストレージ上に保存し、そのファイル名を返す。
	 * この呼び出しは極力キャッシュされます。
	 *
	 * @param user Twitterユーザー
	 * @return ストレージ上の画像ファイル。存在しない場合はnull。
	 */
	public File getImageFile(User user) throws InterruptedException {
		String imageKey = getImageKey(user);
		ImageEntry entry = cachedImages.get(imageKey);
		if (entry == null) {
			try {
				entry = new ImageEntry(new URL(user.getProfileImageURL()), imageKey);
			} catch (MalformedURLException e) {
				throw new AssertionError(e); // would never happen
			}
			entry.cacheFile = getImageFilename(user);
			fetchImage(entry);
		}
		if (!entry.isWritten) {
			flushImage(entry);
		}
		return entry.cacheFile;
	}

	/**
	 * 画像ファイル名を取得する。
	 *
	 * @param user Twitterユーザー
	 * @return もしストレージに保存するならばこのファイル名で
	 */
	protected File getImageFilename(User user) {
		String fileName = getProfileImageName(user);
		long id = user.getId();
		String subdir = Integer.toHexString((int) (id & 0xff));
		return new File(userIconCacheDir, MessageFormat.format("{0}/{1}-{2}", subdir, Long.toString(id), fileName));
	}

	/**
	 * イメージアイコンを取得する。
	 *
	 * @param image 画像データ
	 * @return ImageIconインスタンス
	 */
	protected Icon getImageIcon(Image image) {
		ImageIcon imageIcon = new ImageIcon(image);
		imageIcon.setImageObserver(AnimationCanceledImageObserver.SINGLETON);
		return imageIcon;
	}

	/**
	 * 画像キーを取得する
	 *
	 * @param userId           ユーザーID
	 * @param profileImageName ファイル名
	 * @return 画像キー
	 */
	protected String getImageKey(String userId, String profileImageName) {
		return "user://" + userId + "/" + profileImageName;
	}

	/**
	 * 画像キーを取得する
	 *
	 * @param user ユーザー
	 * @return 画像キー
	 */
	protected String getImageKey(User user) {
		return getImageKey(String.valueOf(user.getId()), getProfileImageName(user));
	}

	/**
	 * プロフィール画像のファイル名を取得する
	 *
	 * @param user ユーザー
	 * @return ファイル名
	 */
	protected String getProfileImageName(User user) {
		String url = user.getProfileImageURL();
		String fileName = url.substring(url.lastIndexOf('/') + 1);
		return fileName;
	}

	protected void incrementAppearCount(ImageEntry entry) {
		if (entry.isWritten) {
			return;
		}
		if (entry.countEndTime < System.currentTimeMillis()) { // reset
			entry.countEndTime = System.currentTimeMillis() + flushResetInterval;
			entry.appearCount = 0;
		}
		int appearCount = ++entry.appearCount;
		if (appearCount > flushThreshold) {
			configuration.addJob(Priority.LOW, new ImageFlusher(entry));
		}
	}

	/**
	 * 画像を取得し、label.setIcon(...)する。
	 * おそらくlabel.setHorizontalAlignment(JLabel.CENTER)を呼び出す必要があるでしょう。
	 *
	 * @param label JLabelインスタンス
	 * @param url   画像URL
	 * @return キャッシュヒットしたかどうか
	 */
	public boolean setImageIcon(JLabel label, URL url) {
		String urlString = url.toString();
		ImageEntry entry = cachedImages.get(urlString);
		if (entry == null) {
			configuration.addJob(new ImageFetcher(new ImageEntry(url, urlString), label));
			return false;
		} else {
			label.setIcon(getImageIcon(entry.image));
			incrementAppearCount(entry);
			return true;
		}
	}

	/**
	 * 指定されたユーザーのプロフィール画像を取得し、label.setIcon(...)する。
	 * おそらくlabel.setHorizontalAlignment(JLabel.CENTER)を呼び出す必要があるでしょう。
	 *
	 * @param label JLabelインスタンス
	 * @param user  Twitterユーザー
	 * @return キャッシュヒットしたかどうか
	 */
	public boolean setImageIcon(JLabel label, User user) {
		String imageKey = getImageKey(user);
		String url = user.getProfileImageURL();
		ImageEntry entry = cachedImages.get(imageKey);
		if (entry == null) {
			try {
				entry = new ImageEntry(new URL(url), imageKey);
			} catch (MalformedURLException e) {
				throw new AssertionError(e); // would never happen
			}
			entry.cacheFile = getImageFilename(user);
			configuration.addJob(new ImageFetcher(entry, label));
			return false;
		} else {
			label.setIcon(getImageIcon(entry.image));
			incrementAppearCount(entry);
			return true;
		}
	}
}
