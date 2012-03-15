package jp.syuriken.snsw.twclient.internal;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

/**
 * {@link DefaultTweetLengthCalculator}のためのテスト
 * 
 * @author $Author$
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
			stringBuilder.append("あ");
		}
		return stringBuilder.toString();
	}
	
	/**
	 * {@link DefaultTweetLengthCalculator#getShortenedText(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetShortenedText() {
		DefaultTweetLengthCalculator lengthCalculator = new DefaultTweetLengthCalculator(new TweetLengthUpdaterImpl());
		assertEquals(TEXT1, lengthCalculator.getShortenedText(TEXT1));
		assertEquals(TEXT2, lengthCalculator.getShortenedText(TEXT2));
		assertEquals(TEXT3, lengthCalculator.getShortenedText(TEXT3));
		assertEquals(TEXT4, lengthCalculator.getShortenedText(TEXT4));
		assertEquals(TEXT5, lengthCalculator.getShortenedText(TEXT5));
		assertEquals(TEXT6, lengthCalculator.getShortenedText(TEXT6));
	}
	
	/**
	 * {@link DefaultTweetLengthCalculator#calcTweetLength(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetTweetLength() {
		assertEquals(140, DefaultTweetLengthCalculator.getTweetLength(TEXT1));
		assertEquals(140, DefaultTweetLengthCalculator.getTweetLength(TEXT2));
		assertEquals(41, DefaultTweetLengthCalculator.getTweetLength(TEXT3));
		assertEquals(41, DefaultTweetLengthCalculator.getTweetLength(TEXT4));
		assertEquals(140, DefaultTweetLengthCalculator.getTweetLength(TEXT5));
		assertEquals(17, DefaultTweetLengthCalculator.getTweetLength(TEXT6));
	}
	
}