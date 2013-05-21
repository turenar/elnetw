package jp.syuriken.snsw.twclient.handler;

import java.awt.Color;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;
import jp.syuriken.snsw.twclient.handler.QuoteTweetActionHandler.QuoteTweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.TweetLengthUpdaterImpl;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * {@link QuoteTweetLengthCalculator}のためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QuoteTweetLengthCalculatorTest {

	private class TestTweetLengthUpdater extends TweetLengthUpdaterImpl {

		@Override
		public void updatePostLength(String length, Color color, String tooltip) {
			lastLengthString = length;
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
			stringBuilder.append("あ");
		}
		return stringBuilder.toString();
	}

	private String lastLengthString;

	private String calcTweetLength(TweetLengthCalculator calculator, String str) {
		calculator.calcTweetLength(str);
		return lastLengthString;
	}

	/**
	 * {@link QuoteTweetLengthCalculator#calcTweetLength(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testCalcTweetLength() {
		TweetLengthCalculator calculator = new QuoteTweetLengthCalculator(new TestTweetLengthUpdater());
		assertEquals("140", calcTweetLength(calculator, getString(120) + QT_10LEN + getString(10)));
		assertEquals("140", calcTweetLength(calculator, getString(110) + QT_10LEN + URL1));
		assertEquals("120+", calcTweetLength(calculator, getString(110) + QT_10LEN + " " + URL1));
		assertEquals("131+", calcTweetLength(calculator, getString(121) + QT_10LEN + getString(10)));
		assertEquals("140+", calcTweetLength(calculator, getString(130) + QT_10LEN + getString(10)));
		assertEquals("141+", calcTweetLength(calculator, getString(131) + QT_10LEN + getString(10)));
		assertEquals("140", calcTweetLength(calculator, getString(110) + QT_10LEN + URL1));
		assertEquals("120+", calcTweetLength(calculator, getString(110) + QT_10LEN + " " + URL1));
		assertEquals("140", calcTweetLength(calculator, URL1 + QT_10LEN + URL1 + " " + getString(68) + " " + URL1));
		assertEquals("30+", calcTweetLength(calculator, URL1 + QT_10LEN + URL1 + " " + getString(70) + " " + URL1));
		assertEquals("30+", calcTweetLength(calculator, URL1 + QT_10LEN + URL1 + " " + getString(90)));
		assertEquals("140", calcTweetLength(calculator, HASHTAG1 + " " + QT_10LEN + URL1 + " " + getString(98)));
		assertEquals("140", calcTweetLength(calculator, QT_10LEN + URL1 + " " + HASHTAG1 + " " + getString(98)));
		assertEquals("10+", calcTweetLength(calculator, QT_10LEN + URL1 + " " + HASHTAG1 + " " + getString(99)));
		assertEquals("140", calcTweetLength(calculator, QT_10LEN + URL1 + " " + HASHTAG2 + " " + getString(98)));
		assertEquals("10+", calcTweetLength(calculator, QT_10LEN + URL1 + " " + HASHTAG2 + " " + getString(99)));
		assertEquals("140", calcTweetLength(calculator, QT_10LEN + URL1 + " " + MENTION1 + " " + getString(98)));
		assertEquals("10+", calcTweetLength(calculator, QT_10LEN + URL1 + " " + MENTION1 + " " + getString(99)));
		assertEquals("140", calcTweetLength(calculator, QT_10LEN + URL1 + " " + MENTION2 + " " + getString(98)));
		assertEquals("10+", calcTweetLength(calculator, QT_10LEN + URL1 + " " + MENTION2 + " " + getString(99)));
	}

	/**
	 * {@link QuoteTweetLengthCalculator#getShortenedText(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetShortenedText() {
		TweetLengthCalculator calculator = new QuoteTweetLengthCalculator(new TestTweetLengthUpdater());
		assertEquals(getString(120) + QT_10LEN + getString(10),
				calculator.getShortenedText(getString(120) + QT_10LEN + getString(10)));
		assertEquals(getString(110) + QT_10LEN + URL1, calculator.getShortenedText(getString(110) + QT_10LEN + URL1));
		assertEquals(getString(110) + QT_10LEN + " ",
				calculator.getShortenedText(getString(110) + QT_10LEN + " " + URL1));
		assertEquals(getString(121) + QT_10LEN + getString(9),
				calculator.getShortenedText(getString(121) + QT_10LEN + getString(10)));
		assertEquals(getString(130) + QT_10LEN, calculator.getShortenedText(getString(130) + QT_10LEN + getString(10)));
		assertEquals(getString(131) + QT_10LEN + getString(10),
				calculator.getShortenedText(getString(131) + QT_10LEN + getString(10)));
		assertEquals(URL1 + QT_10LEN + URL1 + " " + getString(68) + " " + URL1,
				calculator.getShortenedText(URL1 + QT_10LEN + URL1 + " " + getString(68) + " " + URL1));
		assertEquals(URL1 + QT_10LEN + URL1 + " " + getString(70) + " ",
				calculator.getShortenedText(URL1 + QT_10LEN + URL1 + " " + getString(70) + " " + URL1));
		assertEquals(URL1 + QT_10LEN + URL1 + " " + getString(89),
				calculator.getShortenedText(URL1 + QT_10LEN + URL1 + " " + getString(90)));
		assertEquals(QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG1,
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG1));
		assertEquals(QT_10LEN + URL1 + " " + getString(99) + " ",
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(99) + " " + HASHTAG1));
		assertEquals(QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG2,
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(98) + " " + HASHTAG2));
		assertEquals(QT_10LEN + URL1 + " " + getString(99) + " ",
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(99) + " " + HASHTAG2));
		assertEquals(QT_10LEN + URL1 + " " + getString(98) + " " + MENTION1,
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(98) + " " + MENTION1));
		assertEquals(QT_10LEN + URL1 + " " + getString(99) + " ",
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(99) + " " + MENTION1));
		assertEquals(QT_10LEN + URL1 + " " + getString(98) + " " + MENTION2,
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(98) + " " + MENTION2));
		assertEquals(QT_10LEN + URL1 + " " + getString(99) + " ",
				calculator.getShortenedText(QT_10LEN + URL1 + " " + getString(99) + " " + MENTION2));

	}

	/**
	 * テストに用いる文字列の長さが正しいかどうかをテストする
	 */
	@Test
	public void testValidOfTestString() {
		assertEquals(10, HASHTAG1.length());
		assertEquals(10, HASHTAG2.length());
		assertEquals(10, MENTION1.length());
		assertEquals(10, MENTION2.length());
		assertEquals(10, QT_10LEN.length());
	}
}
