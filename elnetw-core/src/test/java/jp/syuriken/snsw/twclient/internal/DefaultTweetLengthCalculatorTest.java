package jp.syuriken.snsw.twclient.internal;

import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import jp.syuriken.snsw.twclient.TwitterDataFetchScheduler;
import org.junit.Test;

import static jp.syuriken.snsw.twclient.internal.DefaultTweetLengthCalculator.DEFAULT_SHORT_URL_LENGTH;
import static jp.syuriken.snsw.twclient.internal.DefaultTweetLengthCalculator.DEFAULT_SHORT_URL_LENGTH_HTTPS;
import static junit.framework.Assert.assertEquals;

/**
 * {@link DefaultTweetLengthCalculator}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DefaultTweetLengthCalculatorTest {

	private static final String TEXT6 = "http: https: t.co";
	private static final String TEXT5 = getString(140);
	private static final String TEXT4 =
			"http://example.com/aaa?aaa=aaa.aaa&poet=1aaa https://turetwcl.googlecode.com/svn/app/trunk/twclient-core/README.txt";
	private static final String TEXT3 = "http://t.co http://t.co";
	private static final String TEXT2 = getString(120) + "https://t.co";
	private static final String TEXT1 = getString(120) + "http://t.co";

	private static final String getString(int width) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < width; i++) {
			stringBuilder.append('あ');
		}
		return stringBuilder.toString();
	}

	private final ClientConfigurationTestImpl configuration;


	public DefaultTweetLengthCalculatorTest() {
		configuration = new ClientConfigurationTestImpl();
		configuration.setGlobalInstance();
		try {
			configuration.setFetchScheduler(new TwitterDataFetchScheduler() {
				@Override
				public void init() {
				}
			});
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** {@link DefaultTweetLengthCalculator#getShortenedText(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testGetShortenedText() {
		configuration.setGlobalInstance();
		try {
			DefaultTweetLengthCalculator.clearApiConfiguration();
			DefaultTweetLengthCalculator lengthCalculator = new DefaultTweetLengthCalculator(new TweetLengthUpdaterImpl());
			assertEquals(TEXT1, lengthCalculator.getShortenedText(TEXT1));
			assertEquals(TEXT2, lengthCalculator.getShortenedText(TEXT2));
			assertEquals(TEXT3, lengthCalculator.getShortenedText(TEXT3));
			assertEquals(TEXT4, lengthCalculator.getShortenedText(TEXT4));
			assertEquals(TEXT5, lengthCalculator.getShortenedText(TEXT5));
			assertEquals(TEXT6, lengthCalculator.getShortenedText(TEXT6));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** {@link DefaultTweetLengthCalculator#calcTweetLength(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testGetTweetLength() {
		configuration.setGlobalInstance();
		try {
			// apiConfigurationの初期化
			DefaultTweetLengthCalculator.clearApiConfiguration();
			assertEquals(120 + DEFAULT_SHORT_URL_LENGTH, DefaultTweetLengthCalculator.getTweetLength(TEXT1));
			assertEquals(120 + DEFAULT_SHORT_URL_LENGTH_HTTPS, DefaultTweetLengthCalculator.getTweetLength(TEXT2));
			assertEquals(1 + (DEFAULT_SHORT_URL_LENGTH * 2), DefaultTweetLengthCalculator.getTweetLength(TEXT3));
			assertEquals(1 + DEFAULT_SHORT_URL_LENGTH + DEFAULT_SHORT_URL_LENGTH_HTTPS, DefaultTweetLengthCalculator.getTweetLength(TEXT4));
			assertEquals(140, DefaultTweetLengthCalculator.getTweetLength(TEXT5));
			assertEquals(17, DefaultTweetLengthCalculator.getTweetLength(TEXT6));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

}
