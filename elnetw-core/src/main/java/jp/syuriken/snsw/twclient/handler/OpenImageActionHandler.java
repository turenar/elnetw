package jp.syuriken.snsw.twclient.handler;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.gui.ImageViewerFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open Image
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com
 */
public class OpenImageActionHandler implements ActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(OpenImageActionHandler.class);

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
		Object urlObject = args.getExtra("url");
		URL url;
		if (urlObject instanceof URL) {
			url = (URL) urlObject;
		} else if (urlObject instanceof String) {
			try {
				url = new URL((String) urlObject);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			throw new IllegalArgumentException("arg `url' is missing");
		}
		new ImageViewerFrame(url).setVisible(true);
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
	}
}
