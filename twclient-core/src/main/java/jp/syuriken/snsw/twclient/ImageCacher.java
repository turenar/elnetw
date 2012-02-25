package jp.syuriken.snsw.twclient;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
 * @author $Author$
 */
public class ImageCacher {
	
	/**
	 * 画像をフェッチするためのエントリ
	 * 
	 * @author $Author$
	 */
	protected class FetchEntry {
		
		/** イメージエントリ */
		public final ImageEntry entry;
		
		/** イメージアイコンを設定するJLabel */
		public final JLabel label;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param imageEntry イメージエントリ
		 * @param label イメージアイコンを設定するラベル
		 */
		public FetchEntry(ImageEntry imageEntry, JLabel label) {
			entry = imageEntry;
			this.label = label;
		}
	}
	
	/**
	 * 画像情報を保存するエントリ。
	 * 
	 * @author $Author$
	 */
	protected class ImageEntry {
		
		/** イメージURL */
		public final URL url;
		
		/** Image インスタンス */
		public Image image;
		
		/** キャッシュ先のファイル */
		public File cacheFile;
		
		/** 生の画像データ */
		public byte[] rawimage;
		
		/** すでに書きこまれたかどうか */
		protected volatile boolean isWritten = false;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param url URL
		 */
		public ImageEntry(URL url) {
			this.url = url;
		}
	}
	
	/**
	 * イメージフェッチャ
	 * 
	 * @author $Author$
	 */
	protected class ImageFetcher implements Runnable {
		
		@Override
		public void run() {
			FetchEntry fetchEntry = fetchQueue.poll();
			if (fetchEntry == null) {
				return;
			}
			if (cacheManager.containsKey(fetchEntry.entry.url.toString())) {
				setImageIcon(fetchEntry.label, fetchEntry.entry.url);
				return;
			}
			
			fetchImage(fetchEntry.entry);
			fetchEntry.label.setIcon(getImageIcon(fetchEntry.entry.image));
			fetchEntry.label.revalidate();
			
			if (fetchQueue.peek() != null) {
				configuration.getFrameApi().addJob(Priority.LOW, this);
			}
		}
	}
	
	/**
	 * 画像をStorageにフラッシュするクラス
	 * 
	 * @author $Author$
	 */
	public class ImageFlusher extends TimerTask {
		
		@Override
		public void run() {
			ImageEntry entry = fluseQueue.poll();
			if (entry == null) {
				return;
			}
			flushImage(entry);
			if (fluseQueue.peek() != null) {
				configuration.getFrameApi().addJob(Priority.LOW, this);
			}
		}
		
	}
	
	
	/** Buffersize */
	private static final int BUFSIZE = 65536;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ClientConfiguration configuration;
	
	private ConcurrentHashMap<String, ImageEntry> cacheManager = new ConcurrentHashMap<String, ImageEntry>();
	
	private ConcurrentLinkedQueue<FetchEntry> fetchQueue = new ConcurrentLinkedQueue<FetchEntry>();
	
	private ConcurrentLinkedQueue<ImageEntry> fluseQueue = new ConcurrentLinkedQueue<ImageEntry>();
	
	private ImageFetcher imageFetcher = new ImageFetcher();
	
	/** キャッシュ出力先ディレクトリ */
	public static final File OUTPUT_CACHE_DIR = new File(System.getProperty("user.home") + "/.cache/turetwcl/");
	
	
	/**
	 * インスタンスを生成する。
	 * @param configuration ClientConfigurationインスタンス。
	 */
	public ImageCacher(ClientConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * 画像を取得する。
	 * @param entry イメージエントリ
	 */
	protected void fetchImage(ImageEntry entry) {
		URL url = entry.url;
		if (cacheManager.containsKey(entry.url.toString())) {
			return;
		}
		
		synchronized (entry) {
			InputStream stream;
			try {
				stream = entry.url.openStream();
				byte[] buf = new byte[BUFSIZE];
				byte[] imagedata = new byte[0];
				int imagelen = 0;
				int loadlen;
				while ((loadlen = stream.read(buf)) != -1) {
					if (loadlen == 0) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// do nothing
						}
						continue;
					}
					byte[] oldimage = imagedata;
					imagedata = new byte[imagelen + loadlen];
					System.arraycopy(oldimage, 0, imagedata, 0, imagelen);
					System.arraycopy(buf, 0, imagedata, imagelen, loadlen);
					imagelen += loadlen;
				}
				entry.rawimage = imagedata;
				entry.image = Toolkit.getDefaultToolkit().createImage(imagedata);
				cacheManager.put(url.toString(), entry);
				fluseQueue.add(entry);
			} catch (IOException e) {
				logger.warn(MessageFormat.format("{0}の取得中にエラー", url), e);
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
			if (entry.isWritten == false) {
				if (entry.cacheFile == null) {
					return false;
				}
				FileOutputStream outputStream = null;
				try {
					if (OUTPUT_CACHE_DIR.exists() == false) {
						if (OUTPUT_CACHE_DIR.mkdirs() == false) {
							logger.warn("{} is not exist and can't mkdir", OUTPUT_CACHE_DIR.getPath());
						}
					}
					outputStream = new FileOutputStream(entry.cacheFile);
					outputStream.write(entry.rawimage);
					entry.isWritten = true;
					logger.debug("{} を出力", entry.cacheFile.getPath());
					/*} catch (FileNotFoundException e) {*/
				} catch (IOException e) {
					logger.warn(entry.cacheFile.getPath() + " の出力に失敗", e);
					return false;
				} finally {
					try {
						if (outputStream != null) {
							outputStream.close();
						}
					} catch (IOException e) {
						logger.warn(entry.cacheFile.getPath() + " のクローズに失敗", e);
					}
				}
			}
			return true;
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
	public File getImageFile(User user) {
		ImageEntry entry = cacheManager.get(user.getProfileImageURL().toExternalForm());
		if (entry == null) {
			entry = new ImageEntry(user.getProfileImageURL());
			entry.cacheFile = getImageFilename(user);
			fetchImage(entry);
		}
		if (entry.isWritten == false) {
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
		String url = user.getProfileImageURL().toExternalForm();
		String fileName = url.substring(url.lastIndexOf('/') + 1);
		
		return new File(OUTPUT_CACHE_DIR, MessageFormat.format("{0}-{1}", Long.toString(user.getId()), fileName));
	}
	
	/**
	 * イメージアイコンを取得する。
	 * 
	 * @param image 画像データ
	 * @return ImageIconインスタンス
	 */
	public Icon getImageIcon(Image image) {
		ImageIcon imageIcon = new ImageIcon(image);
		imageIcon.setImageObserver(AnimationCanceledImageObserver.SINGLETON);
		return imageIcon;
	}
	
	/**
	 * 画像を取得し、label.setIcon(...)する。
	 * おそらくlabel.setHorizontalAlignment(JLabel.CENTER)を呼び出す必要があるでしょう。
	 * 
	 * @param label JLabelインスタンス
	 * @param url 画像URL
	 * @return キャッシュヒットしたかどうか
	 */
	public boolean setImageIcon(JLabel label, URL url) {
		ImageEntry entry = cacheManager.get(url.toString());
		if (entry == null) {
			fetchQueue.add(new FetchEntry(new ImageEntry(url), label));
			configuration.getFrameApi().addJob(Priority.LOW, imageFetcher);
			return false;
		} else {
			label.setIcon(getImageIcon(entry.image));
			return true;
		}
	}
	
	/**
	 * 指定されたユーザーのプロフィール画像を取得し、label.setIcon(...)する。
	 * おそらくlabel.setHorizontalAlignment(JLabel.CENTER)を呼び出す必要があるでしょう。
	 * 
	 * @param label JLabelインスタンス
	 * @param user Twitterユーザー
	 * @return キャッシュヒットしたかどうか
	 */
	public boolean setImageIcon(JLabel label, User user) {
		URL url = user.getProfileImageURL();
		ImageEntry entry = cacheManager.get(url.toString());
		if (entry == null) {
			entry = new ImageEntry(url);
			entry.cacheFile = getImageFilename(user);
			fetchQueue.add(new FetchEntry(entry, label));
			configuration.getFrameApi().addJob(Priority.LOW, imageFetcher);
			return false;
		} else {
			label.setIcon(getImageIcon(entry.image));
			return true;
		}
	}
}
