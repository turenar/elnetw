package jp.syuriken.snsw.twclient.gui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
public class ImageViewerFrame extends JFrame implements WindowListener {
	protected class ImageFetcher implements ParallelRunnable {
		private final Logger logger = LoggerFactory.getLogger(ImageFetcher.class);
		private final URL url;
		/** フレームが閉じられた */
		private boolean isInterrupted;

		public ImageFetcher(URL url) {
			this.url = url;
		}

		/** フレームが閉じられたことに対する通知 */
		public void interrupt() {
			isInterrupted = true;
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
							if (isInterrupted) {
								logger.debug("Viewer frame closed: cancel fetch");
								return;
							} else {
								wait(1);
							}
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
					updateImageLabel("画像のロードに失敗したもよう");
				} else {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							getComponentImageLabel().setText(null);
							getComponentImageLabel().setIcon(imageIcon);
							pack();
							setIconImage(image);
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
	private ImageViewerFrame.ImageFetcher imageFetcher;

	/**
	 * インスタンスを生成する。
	 *
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
		addWindowListener(this);
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
		imageFetcher = new ImageFetcher(url);
		ClientConfiguration.getInstance().addJob(Priority.HIGH, imageFetcher);
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		imageFetcher.interrupt();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
}
