package jp.syuriken.snsw.twclient.gui;

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
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Hashtable;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue;
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
						ImageViewerFrame.this.image = createBufferedImage(image);
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
	private final URL url;
	private int resizeScaleFactor;
	private MouseListener zoomMouseListener = new MouseAdapter() {
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
	/*package*/ BufferedImage image;
	private JLabel imageLabel;
	private ImageViewerFrame.ImageFetcher imageFetcher;
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

	/*package*/BufferedImage createBufferedImage(Image image) throws InterruptedException {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(image, 0);
		tracker.waitForAll();

		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, false);
		pixelGrabber.grabPixels();
		ColorModel cm = pixelGrabber.getColorModel();

		final int w = pixelGrabber.getWidth();
		final int h = pixelGrabber.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
		BufferedImage renderedImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(),
				new Hashtable());
		renderedImage.getRaster().setDataElements(0, 0, w, h, pixelGrabber.getPixels());
		return renderedImage;
	}

	private JLabel getComponentImageLabel() {
		if (imageLabel == null) {
			imageLabel = new JLabel("キューが開くのを待っています...");
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
		super.pack();
		int width = image.getWidth();
		int height = image.getHeight();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (width < 64) {
			width = 64;
		} else if (width > screenSize.width) {
			width = screenSize.width;
		}
		if (height < 64) {
			height = 64;
		} else if (height > screenSize.height) {
			height = screenSize.height;
		}
		setSize(width, height);
	}

	/*package*/ void queueUpdateImage() {
		getComponentImageLabel().setIcon(null);
		getComponentImageLabel().setText("Loading...");
		ClientConfiguration.getInstance().addJob(JobQueue.PRIORITY_MAX, new ParallelRunnable() {
			@Override
			public void run() {
				synchronized (ImageViewerFrame.this) {
					try {
						final MediaContainer mediaContainer = updateImageSize();
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
