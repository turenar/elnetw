package jp.syuriken.snsw.twclient.gui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.syuriken.snsw.twclient.JobQueue.Priority;

/**
 * image viewer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ImageViewerFrame extends JFrame {
	protected class ImageFetcher implements ParallelRunnable {
		private final URL url;

		public ImageFetcher(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			URLConnection connection;
			try {
				updateImageLabel("コネクションを開いています...");
				connection = url.openConnection();

				int contentLength = connection.getContentLength();
				InputStream stream = connection.getInputStream();
				int bufLength = contentLength < 0 ? BUFSIZE : contentLength + 1;
				byte[] data = new byte[bufLength];
				int imageLen = 0;
				int loadLen;
				while ((loadLen = stream.read(data, imageLen, bufLength - imageLen)) != -1) {
					imageLen += loadLen;

					if (contentLength == -1) {
						updateImageLabel("読込中(" + imageLen + ")");
					} else {
						updateImageLabel("読込中(" + imageLen + "/" + contentLength + ")");
					}

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
							updateImageLabel("割り込まれました");
							Thread.currentThread().interrupt();
							stream.close(); // throw remain data away
							return;
						}
					}
				}
				stream.close(); // help keep-alive
				final Image image = Toolkit.getDefaultToolkit().createImage(data, 0, imageLen);
				final ImageIcon imageIcon = new ImageIcon(image);
				if (imageIcon.getIconHeight() < 0) {
					updateImageLabel("画像のロードに失敗");
				} else {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							getComponentImageLabel().setText(null);
							getComponentImageLabel().setIcon(imageIcon);
							pack();
						}
					});
				}
			} catch (IOException e) {
				logger.warn(MessageFormat.format("Error while fetching: {0}", url), e);
			}
		}

		private void updateImageLabel(final String progress) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					getComponentImageLabel().setText(progress);
				}
			});
		}
	}

	private static final int BUFSIZE = 65536;
	private static Logger logger = LoggerFactory.getLogger(ImageViewerFrame.class);
	private final URL url;
	private JLabel imageLabel;

	/**
	 * インスタンスを生成する。
	 * @param url 画像のURL。
	 */
	public ImageViewerFrame(URL url) {
		this.url = url;
		initComponents();
		scheduleImageFetcher(url);
	}

	private JLabel getComponentImageLabel() {
		if (imageLabel == null) {
			imageLabel = new JLabel("キューが開くのを待っています...");
		}
		return imageLabel;
	}

	private void initComponents() {
		setSize(256, 256);
		setTitle(url.toString());
		add(getComponentImageLabel());
	}

	@Override
	public void pack() {
		super.pack();
		int width = getWidth();
		int height = getHeight();
		if (width < 64) {
			width = 64;
		}
		if (height < 64) {
			height = 64;
		}
		setSize(width, height);
	}

	private void scheduleImageFetcher(URL url) {
		ClientConfiguration.getInstance().addJob(Priority.HIGH, new ImageFetcher(url));
	}
}
