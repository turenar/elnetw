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

package jp.mydns.turenar.twclient.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.internal.FetchEventHandler;
import jp.mydns.turenar.twclient.internal.NetworkSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.mydns.turenar.twclient.JobQueue.Priority;
import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * image viewer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ImageViewerFrame extends JFrame implements WindowListener {
	private static final class MediaContainer {
		public final MediaTracker mediaTracker;
		public final Image fastScaledImage;
		public final Image slowScaledImage;

		public MediaContainer(MediaTracker tracker, Image fastScaledImage, Image slowScaledImage) {
			mediaTracker = tracker;
			this.fastScaledImage = fastScaledImage;
			this.slowScaledImage = slowScaledImage;
		}
	}

	/**
	 * イメージフェッチハンドラ
	 */
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
		public void onConnection(URLConnection connection) throws InterruptedException {
			this.contentLength = connection.getContentLength();
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
				updateImageLabel(tr("Loading (%d)", imageLen));
			} else {
				updateImageLabel(tr("Loading (%d/%d)", imageLen, contentLength));
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
						updateImageLabel(tr("Failed loading image"));
					} else {
						ImageViewerFrame.this.image = Utility.createBufferedImage(image,
								new MediaTracker(ImageViewerFrame.this));
						checkImageSize();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								getComponentImageLabel().setText(null);
								pack();
								queueUpdateImage();
								setIconImage(image);
							}
						});
					}
				}
			} catch (InterruptedException e) {
				updateImageLabel(tr("Thread interrupted"));
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

	private class ImageUpdater implements ParallelRunnable {
		private final Image image;

		public ImageUpdater(Image image) {
			this.image = image;
		}

		@Override
		public void run() {
			logger.debug("ivf.seticon.start");
			getComponentImageLabel().setText(null);
			getComponentImageLabel().setIcon(new ImageIcon(image));
			logger.debug("ivf.seticon.end");
		}
	}

	/*package*/static final Logger logger = LoggerFactory.getLogger(ImageViewerFrame.class);
	private static final long serialVersionUID = -6123453663166656756L;
	/**
	 * minimum image size
	 */
	public static final int MIN_IMAGE_SIZE = 64;
	private final URL url;
	private int resizeScaleFactor;
	private transient MouseListener zoomMouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getComponent() == getComponentZoomIn()) {
				resizeScaleFactor++;
			} else {
				resizeScaleFactor--;
			}
			e.consume();
			queueUpdateImage();
		}
	};
	/*package*/transient BufferedImage image;
	private JLabel imageLabel;
	private transient ImageViewerFrame.ImageFetcher imageFetcher;
	private JLabel componentZoomIn;
	private JLabel componentZoomOut;
	private Font uiFont;
	private JScrollPane componentImageScrollPane;

	/**
	 * インスタンスを生成する。
	 *
	 * @param url 画像のURL。
	 */
	public ImageViewerFrame(URL url) {
		this.url = url;
		initComponents();
		scheduleImageFetcher(url);
		uiFont = ClientConfiguration.getInstance().getConfigProperties()
				.getFont(ClientConfiguration.PROPERTY_GUI_FONT_UI).deriveFont(Font.BOLD);
	}

	/**
	 * ディスプレイサイズに合うように画像の縮尺を変更する
	 */
	protected void checkImageSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = screenSize.height;
		int width = screenSize.width;
		if (image != null) {
			int imageHeight = image.getHeight();
			int imageWidth = image.getWidth();
			if (imageHeight <= height && imageWidth <= width) {
				return;
			}
			double maxImageLength;
			if ((double) imageHeight / height <= (double) imageWidth / width) {
				maxImageLength = (double) imageWidth / width;
			} else {
				maxImageLength = (double) imageHeight / height;
			}
			while (maxImageLength > 1.000D) {
				resizeScaleFactor--;
				maxImageLength = maxImageLength * 2 / 3;
			}
		}
	}

	private JLabel getComponentImageLabel() {
		if (imageLabel == null) {
			imageLabel = new JLabel(tr("Waiting for queue"));
		}
		return imageLabel;
	}

	private JScrollPane getComponentImageScrollPane() {
		if (componentImageScrollPane == null) {
			componentImageScrollPane = new JScrollPane(getComponentImageLabel());
		}
		return componentImageScrollPane;
	}

	private JLabel getComponentZoomIn() {
		if (componentZoomIn == null) {
			componentZoomIn = new JLabel("Zoom+");
			componentZoomIn.setFont(uiFont);
			componentZoomIn.addMouseListener(zoomMouseListener);
		}
		return componentZoomIn;
	}

	private JLabel getComponentZoomOut() {
		if (componentZoomOut == null) {
			componentZoomOut = new JLabel("Zoom-");
			componentZoomOut.setFont(uiFont);
			componentZoomOut.addMouseListener(zoomMouseListener);
		}
		return componentZoomOut;
	}

	private void initComponents() {
		setSize(256, 256);
		setTitle(url.toString());
		addWindowListener(this);
		GroupLayout layout = new GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getComponentImageScrollPane())
				.addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
						.addComponent(getComponentZoomIn())
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(getComponentZoomOut())));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getComponentImageScrollPane())
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
						.addComponent(getComponentZoomIn())
						.addComponent(getComponentZoomOut())));
	}

	@Override
	public void pack() {
		Dimension labelSize = getComponentImageLabel().getSize();
		labelSize.width += 2; // border... should not be const...
		labelSize.height += 2; // border...
		getComponentImageScrollPane().setPreferredSize(labelSize);
		getComponentImageScrollPane().validate();
		validate();
		super.pack();

		int width = getWidth();
		int height = getHeight();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (width < MIN_IMAGE_SIZE) {
			width = MIN_IMAGE_SIZE;
		} else if (width > screenSize.width) {
			width = screenSize.width;
		}
		if (height < MIN_IMAGE_SIZE) {
			height = MIN_IMAGE_SIZE;
		} else if (height > screenSize.height) {
			height = screenSize.height;
		}
		setSize(width, height);
		validate();
	}

	/*package*/ void queueUpdateImage() {
		getComponentImageLabel().setIcon(null);
		getComponentImageLabel().setText("Loading...");
		final MediaContainer mediaContainer = updateImageSize();
		ClientConfiguration.getInstance().addJob(JobQueue.PRIORITY_MAX, new ParallelRunnable() {
			@Override
			public void run() {
				synchronized (ImageViewerFrame.this) {
					try {
						mediaContainer.mediaTracker.waitForID(0);
						EventQueue.invokeLater(new ImageUpdater(mediaContainer.fastScaledImage));
						logger.debug("ivf.job.fastdone");
						mediaContainer.mediaTracker.waitForID(1);
						EventQueue.invokeLater(new ImageUpdater(mediaContainer.slowScaledImage));
						logger.debug("ivf.job.done");
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		});
	}

	private void scheduleImageFetcher(URL url) {
		imageFetcher = new ImageFetcher(url);
		ClientConfiguration.getInstance().addJob(Priority.HIGH, imageFetcher);
	}

	private MediaContainer updateImageSize() {
		int factor = resizeScaleFactor;
		int height = image.getHeight();
		int width = image.getWidth();
		while (factor < 0) {
			factor++;
			height = height * 2 / 3;
			width = width * 2 / 3;
		}
		while (factor > 0) {
			factor--;
			height = height * 3 / 2;
			width = width * 3 / 2;
		}
		getComponentImageLabel().setSize(width, height);
		pack();
		Image fastScaledInstance = image.getScaledInstance(width, height, Image.SCALE_FAST);
		Image slowScaledInstance = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(fastScaledInstance, 0);
		mediaTracker.addImage(slowScaledInstance, 1);
		return new MediaContainer(mediaTracker, fastScaledInstance, slowScaledInstance);
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
