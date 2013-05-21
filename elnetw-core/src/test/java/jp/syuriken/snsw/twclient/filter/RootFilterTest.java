package jp.syuriken.snsw.twclient.filter;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

import javax.swing.Icon;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.ClientTab;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.TweetLengthCalculator;
import jp.syuriken.snsw.twclient.Utility;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * RootFilter のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RootFilterTest {

	private static final class MyClientConfiguration extends ClientConfiguration {

		private static final Logger logger = LoggerFactory.getLogger(MyClientConfiguration.class);

		private ClientProperties clientProperties;

		/*package*/MyClientConfiguration() {
			// protected access
			super();
		}

		@Override
		public ClientProperties getConfigProperties() {
			if (clientProperties == null) {
				InputStream stream = null;
				try {
					clientProperties = new ClientProperties();
					stream = RootFilterTest.class.getResourceAsStream("/jp/syuriken/snsw/twclient/config.properties");
					clientProperties.load(stream);
				} catch (IOException e) {
					throw new AssertionError(e);
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							logger.error("failed closing stream", e);
						}
					}
				}
			}
			return clientProperties;
		}

		@Override
		public ClientFrameApi getFrameApi() {
			return new TestFrameApi();
		}

		@Override
		public ImageCacher getImageCacher() {
			return new TestImageCacher(this);
		}

		@Override
		public Twitter getTwitterForRead() {
			return null;
		}
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.filter.RootFilter#onStatus(twitter4j.Status)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnStatus() {
		ClientConfiguration configuration = new MyClientConfiguration();
		RootFilter rootFilter = new RootFilter(configuration);
		assertNotNull(rootFilter.onStatus(new TestStatus(0)));
		assertNotNull(rootFilter.onStatus(new TestStatus(1)));
		assertNull(rootFilter.onStatus(new TestStatus(0)));
	}
}

class TestFrameApi implements ClientFrameApi {
	@Deprecated
	@Override
	public ActionHandler addActionHandler(String name, ActionHandler handler) {
		return null;
	}

	@Deprecated
	@Override
	public void addJob(Priority priority, Runnable job) {
	}

	@Deprecated
	@Override
	public void addJob(Runnable job) {
	}

	@Override
	public void addShortcutKey(String keyCode, String actionName) {
	}

	@Override
	public void clearTweetView() {
	}

	@Override
	public void doPost() {
	}

	@Override
	public void focusPostBox() {
	}

	@Override
	public String getActionCommandByShortcutKey(String component, String keyString) {
		return null;
	}

	@Deprecated
	@Override
	public ActionHandler getActionHandler(String actionCommand) {
		return null;
	}

	@Override
	public ClientConfiguration getClientConfiguration() {
		return null;
	}

	@Override
	public Font getDefaultFont() {
		return null;
	}

	@Override
	public ImageCacher getImageCacher() {
		return null;
	}

	@Override
	public int getInfoSurviveTime() {
		return 0;
	}

	@Override
	public User getLoginUser() {
		return null;
	}

	@Override
	public String getPostText() {
		return null;
	}

	@Override
	public ClientTab getSelectingTab() {
		return null;
	}

	@Deprecated
	@Override
	public Timer getTimer() {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public Twitter getTwitter() {
		return null;
	}

	@Override
	public Twitter getTwitterForRead() {
		return null;
	}

	@Override
	public Twitter getTwitterForWrite() {
		return null;
	}

	@Override
	public Font getUiFont() {
		return null;
	}

	@Override
	public Utility getUtility() {
		return null;
	}

	@Override
	public void handleAction(String name, StatusData statusData) {
	}

	@Override
	public void handleException(Exception ex) {
	}

	@Override
	public void handleException(TwitterException ex) {
	}

	@Override
	public void handleShortcutKey(String component, KeyEvent e) {
	}

	@Override
	public Status setInReplyToStatus(Status status) {
		return null;
	}

	@Override
	public String setPostText(String text) {
		return null;
	}

	@Override
	public String setPostText(String text, int selectingStart, int selectingEnd) {
		return null;
	}

	@Override
	public TweetLengthCalculator setTweetLengthCalculator(TweetLengthCalculator newCalculator) {
		return null;
	}

	@Override
	public void setTweetViewCreatedAt(String createdAt, String toolTip, int flag) {
	}

	@Override
	public void setTweetViewCreatedBy(Icon icon, String createdBy, String toolTip, int flag) {
	}

	@Override
	public void setTweetViewOperationPanel(JPanel operationPanel) {
	}

	@Override
	public void setTweetViewText(String tweetData, String overlayString, int flag) {
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public void setTweetViewText(String tweetData, String createdBy, String createdByToolTip, String createdAt,
			String createdAtToolTip, Icon icon, JPanel panel) {
	}

	@Override
	public void updatePostLength(String length, Color color, String tooltip) {
	}
}

class TestImageCacher extends ImageCacher {

	public TestImageCacher(ClientConfiguration configuration) {
		super(configuration);
	}
}
