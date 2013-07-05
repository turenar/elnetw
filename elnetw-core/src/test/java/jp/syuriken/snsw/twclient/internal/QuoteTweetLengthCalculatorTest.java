package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;
import java.util.Map;

import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import jp.syuriken.snsw.twclient.TweetLengthCalculator;
import jp.syuriken.snsw.twclient.TwitterDataFetchScheduler;
import org.junit.Test;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterAPIConfiguration;

import static junit.framework.Assert.assertEquals;

/**
 * {@link QuoteTweetLengthCalculator}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QuoteTweetLengthCalculatorTest {

	private static class TestTweetLengthUpdater extends TweetLengthUpdaterImpl {
		public String lastLengthString;

		@Override
		public void updatePostLength(String length, Color color, String tooltip) {
			lastLengthString = length;
		}
	}

	private static class MyTwitterAPIConfiguration implements TwitterAPIConfiguration {

		@Override
		public int getPhotoSizeLimit() {
			return 0;
		}

		@Override
		public int getShortURLLength() {
			return 20;
		}

		@Override
		public int getShortURLLengthHttps() {
			return 20;
		}

		@Override
		public int getCharactersReservedPerMedia() {
			return 0;
		}

		@Override
		public Map<Integer, MediaEntity.Size> getPhotoSizes() {
			return null;
		}

		@Override
		public String[] getNonUsernamePaths() {
			return new String[0];
		}

		@Override
		public int getMaxMediaPerUpload() {
			return 0;
		}

		@Override
		public RateLimitStatus getRateLimitStatus() {
			return null;
		}

		@Override
		public int getAccessLevel() {
			return 0;
		}
	}

	private static final String URL1 = "http://example.com/?test="; // 20char

	private static final String HASHTAG1 = "#aaaaaaaaa"; // 10char

	private static final String HASHTAG2 = "＃aaaaaaaaa"; // 10char

	private static final String MENTION1 = "@aaaaaaaaa"; // 10char

	private static final String MENTION2 = "＠aaaaaaaaa"; // 10char

	/** 長さ10のQTヘッダ */
	private static final String QT_10LEN = " QT @aaaa:"; // 10char

	private static String getString(int width) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < width; i++) {
			stringBuilder.append((char) (((short) 'あ') + (i % 75)));
		}
		return stringBuilder.toString();
	}

	private final ClientConfigurationTestImpl configuration;

	public QuoteTweetLengthCalculatorTest() {
		configuration = new ClientConfigurationTestImpl();
		configuration.setGlobalInstance();
		try {
			configuration.setFetchScheduler(new TwitterDataFetchScheduler() {
				@Override
				public void init() {
				}

				@Override
				public TwitterAPIConfiguration getApiConfiguration() {
					return new MyTwitterAPIConfiguration();
				}
			});
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	private String calcTweetLength(String str) {
		TestTweetLengthUpdater tweetLengthUpdater = new TestTweetLengthUpdater();
		TweetLengthCalculator calculator = new QuoteTweetLengthCalculator(tweetLengthUpdater);
		calculator.calcTweetLength(str);
		return tweetLengthUpdater.lastLengthString;
	}

	private void assertText(String expected, String text) {
		TweetLengthCalculator calculator = new QuoteTweetLengthCalculator(new TestTweetLengthUpdater());
		assertEquals(expected, calculator.getShortenedText(text));
	}

	/** {@link QuoteTweetLengthCalculator#calcTweetLength(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testCalcTweetLength() {
		configuration.setGlobalInstance();
		try {
			assertEquals("140", calcTweetLength(getString(120) + QT_10LEN + getString(10)));
			assertEquals("140", calcTweetLength(getString(110) + QT_10LEN + URL1));
			assertEquals("120+", calcTweetLength(getString(110) + QT_10LEN + " " + URL1));
			assertEquals("131+", calcTweetLength(getString(121) + QT_10LEN + getString(10)));
			assertEquals("140+", calcTweetLength(getString(130) + QT_10LEN + getString(10)));
			assertEquals("141+", calcTweetLength(getString(131) + QT_10LEN + getString(10)));
			assertEquals("140", calcTweetLength(getString(110) + QT_10LEN + URL1));
			assertEquals("120+", calcTweetLength(getString(110) + QT_10LEN + " " + URL1));
			assertEquals("140", calcTweetLength(URL1 + QT_10LEN + URL1 + " " + getString(68) + " " + URL1));
			assertEquals("30+", calcTweetLength(URL1 + QT_10LEN + URL1 + " " + getString(70) + " " + URL1));
			assertEquals("30+", calcTweetLength(URL1 + QT_10LEN + URL1 + " " + getString(90)));
			assertEquals("140", calcTweetLength(HASHTAG1 + " " + QT_10LEN + URL1 + " " + getString(98)));
			assertEquals("140", calcTweetLength(QT_10LEN + URL1 + " " + HASHTAG1 + " " + getString(98)));
			assertEquals("10+", calcTweetLength(QT_10LEN + URL1 + " " + HASHTAG1 + " " + getString(99)));
			assertEquals("140", calcTweetLength(QT_10LEN + URL1 + " " + HASHTAG2 + " " + getString(98)));
			assertEquals("10+", calcTweetLength(QT_10LEN + URL1 + " " + HASHTAG2 + " " + getString(99)));
			assertEquals("140", calcTweetLength(QT_10LEN + URL1 + " " + MENTION1 + " " + getString(98)));
			assertEquals("10+", calcTweetLength(QT_10LEN + URL1 + " " + MENTION1 + " " + getString(99)));
			assertEquals("140", calcTweetLength(QT_10LEN + URL1 + " " + MENTION2 + " " + getString(98)));
			assertEquals("10+", calcTweetLength(QT_10LEN + URL1 + " " + MENTION2 + " " + getString(99)));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** {@link QuoteTweetLengthCalculator#getShortenedText(java.lang.String)} のためのテスト・メソッド。 */
	@Test
	public void testGetShortenedText() {
		configuration.setGlobalInstance();
		try {
			assertText(
					getString(110) + QT_10LEN + getString(20),
					getString(110) + QT_10LEN + getString(20));
			assertText(
					getString(110) + QT_10LEN + URL1,
					getString(110) + QT_10LEN + URL1);
			assertText(
					getString(110) + QT_10LEN + " ",
					getString(110) + QT_10LEN + " " + URL1);
			assertText(
					getString(121) + QT_10LEN + getString(9),
					getString(121) + QT_10LEN + getString(10));
			assertText(
					getString(130) + QT_10LEN,
					getString(130) + QT_10LEN + getString(10));
			assertText(
					getString(131) + QT_10LEN,
					getString(131) + QT_10LEN + getString(10));
			assertText(
					URL1 + QT_10LEN + URL1 + " " + getString(68) + " " + URL1,
					URL1 + QT_10LEN + URL1 + " " + getString(68) + " " + URL1);
			assertText(
					URL1 + QT_10LEN + URL1 + " " + getString(70) + " ",
					URL1 + QT_10LEN + URL1 + " " + getString(70) + " " + URL1);
			assertText(
					URL1 + QT_10LEN + URL1 + " " + getString(89),
					URL1 + QT_10LEN + URL1 + " " + getString(90));
			assertText(
					QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG1,
					QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG1);
			assertText(
					QT_10LEN + URL1 + " " + getString(99) + " ",
					QT_10LEN + URL1 + " " + getString(99) + " " + HASHTAG1);
			assertText(
					QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG2,
					QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG2);
			assertText(
					QT_10LEN + URL1 + " " + getString(99) + " ",
					QT_10LEN + URL1 + " " + getString(99) + " " + HASHTAG2);
			assertText(
					QT_10LEN + URL1 + " " + getString(98) + " " + MENTION1,
					QT_10LEN + URL1 + " " + getString(98) + " " + MENTION1);
			assertText(
					QT_10LEN + URL1 + " " + getString(99) + " ",
					QT_10LEN + URL1 + " " + getString(99) + " " + MENTION1);
			assertText(
					QT_10LEN + URL1 + " " + getString(98) + " " + MENTION2,
					QT_10LEN + URL1 + " " + getString(98) + " " + MENTION2);
			assertText(
					QT_10LEN + URL1 + " " + getString(99) + " ",
					QT_10LEN + URL1 + " " + getString(99) + " " + MENTION2);
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	/** テストに用いる文字列の長さが正しいかどうかをテストする */
	@Test
	public void testValidOfTestString() {
		assertEquals(10, HASHTAG1.length());
		assertEquals(10, HASHTAG2.length());
		assertEquals(10, MENTION1.length());
		assertEquals(10, MENTION2.length());
		assertEquals(10, QT_10LEN.length());
	}
}
