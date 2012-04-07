package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;

/**
 * {@link TweetLengthUpdater}のテンプレートクラス (テスト用)
 * 
 * @author $Author$
 */
public class TweetLengthUpdaterImpl implements TweetLengthUpdater {
	
	@Override
	public TweetLengthCalculator setTweetLengthCalculator(TweetLengthCalculator newCalculator) {
		return null; // do nothing
	}
	
	@Override
	public void updatePostLength(String length, Color color, String tooltip) {
		// do nothing
	}
}
