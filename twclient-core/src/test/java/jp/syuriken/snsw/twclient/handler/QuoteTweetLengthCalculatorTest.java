package jp.syuriken.snsw.twclient.handler;

import static junit.framework.Assert.assertEquals;

import java.awt.Color;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;
import jp.syuriken.snsw.twclient.handler.QuoteTweetActionHandler.QuoteTweetLengthCalculator;
import jp.syuriken.snsw.twclient.internal.TweetLengthUpdaterImpl;

import org.junit.Test;

/**
 * {@link QuoteTweetLengthCalculator}のためのテスト
 * 
 * @author $Author$
 */
public class QuoteTweetLengthCalculatorTest {
	
	private class TestTweetLengthUpdater extends TweetLengthUpdaterImpl {
		
		@Override
		public void updatePostLength(String length, Color color, String tooltip) {
			lastLengthString = length;
		}
	}
	
	
	private static final String URL1 = "http://example.com/?test=";
	
	private static final String BLANK_URL1 = " " + URL1;
	
	/** 長さ10のQTヘッダ */
	private static final String QT_10LEN = " QT @aaaa:";
	
	
	private static final String getString(int width) {
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
		assertEquals("121+", calcTweetLength(calculator, getString(110) + QT_10LEN + BLANK_URL1));
		assertEquals("131+", calcTweetLength(calculator, getString(121) + QT_10LEN + getString(10)));
		assertEquals("140+", calcTweetLength(calculator, getString(130) + QT_10LEN + getString(10)));
		assertEquals("141+", calcTweetLength(calculator, getString(131) + QT_10LEN + getString(10)));
		assertEquals("140+", calcTweetLength(calculator, getString(110) + QT_10LEN + URL1));
		assertEquals("141+", calcTweetLength(calculator, getString(110) + QT_10LEN + BLANK_URL1));
	}
	
	/**
	 * {@link QuoteTweetLengthCalculator#getShortenedText(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetShortenedText() {
		TweetLengthCalculator calculator = new QuoteTweetLengthCalculator(new TestTweetLengthUpdater());
		assertEquals(getString(120) + QT_10LEN + getString(10),
				calculator.getShortenedText(getString(120) + QT_10LEN + getString(10)));
		assertEquals(getString(110) + QT_10LEN + URL1, calcTweetLength(calculator, getString(110) + QT_10LEN + URL1));
		assertEquals(getString(110) + QT_10LEN, calcTweetLength(calculator, getString(110) + QT_10LEN + BLANK_URL1));
		assertEquals(getString(121) + QT_10LEN + getString(9),
				calculator.getShortenedText(getString(121) + QT_10LEN + getString(10)));
		assertEquals(getString(130) + QT_10LEN, calculator.getShortenedText(getString(130) + QT_10LEN + getString(10)));
		assertEquals(getString(131) + QT_10LEN, calculator.getShortenedText(getString(131) + QT_10LEN + getString(10)));
	}
}
