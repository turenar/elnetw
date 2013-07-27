package jp.syuriken.snsw.twclient.internal;

import java.awt.Color;

import jp.syuriken.snsw.twclient.TweetLengthCalculator;

/**
 * ツイートの長さを更新するAPI
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface TweetLengthUpdater {

	/**
	 * ツイートの長さを計算するクラスを設定する。
	 *
	 * @param newCalculator 新しいインスタンス
	 * @return 前設定されていたクラス。
	 */
	TweetLengthCalculator setTweetLengthCalculator(TweetLengthCalculator newCalculator);

	/**
	 * ポストの長さを示すラベルを更新する
	 *
	 * @param length  長さを表す文字列。int文字列である必要はありません
	 * @param color   前景色
	 * @param tooltip ツールチップ
	 */
	void updatePostLength(String length, Color color, String tooltip);
}
