package jp.syuriken.snsw.twclient.gui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.internal.FetchEventHandler;
import jp.syuriken.snsw.twclient.internal.NetworkSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.syuriken.snsw.twclient.JobQueue.Priority;

/**
 * image viewer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ImageViewerFrame extends JFrame implements WindowListener {
	protected class ImageFetcher implements ParallelRunnable, FetchEventHandler {
		private final Logger logger = LoggerFactory.getLogger(ImageFetcher.class);
		private final URL url;
		/** フレームが閉じられた */
		private volatile boolean isInterrupted;
		private int contentLength;

		public ImageFetcher(URL url) {
			this.url = url;
		}

		/** フレームが閉じられたことに対する通知 */
		public void interrupt() {
			isInterrupted = true;
		}

		@Override
		public void onContentLength(int contentLength) throws InterruptedException {
			this.contentLength = contentLength;
			onLoaded(0);
		}

		@Override
		public void onException(URLConnection connection, IOException e) {
			logger.warn(MessageFormat.format("Error while fetching: {0}", url), e);
		}

		@Override
		public void onLoaded(int imageLen) throws InterruptedException {
			if (isInterrupted) {
				throw new InterruptedException();
			} else if (contentLength == -1) {
				updateImageLabel("読込中(" + imageLen + ")");
			} else {
				updateImageLabel("読込中(" + imageLen + "/" + contentLength + ")");
			}
		}

		@Override
		public void run() {
			byte[] contents;
			try {
				contents = NetworkSupport.fetchContents(url, this);
				if (contents != null) {
					final Image image = Toolkit.getDefaultToolkit().createImage(contents);
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
				}
			} catch (InterruptedException e) {
				updateImageLabel("割り込まれました");
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
