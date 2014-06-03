package jp.syuriken.snsw.twclient.net;

import java.awt.Image;

import jp.syuriken.snsw.twclient.internal.ConnectionInfo;

/**
 * ImageSetter: 画像を取得したあとのハンドラ。通常、AbstractImageSetterをextendsするのがいいと思います。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ImageSetter {
	/**
	 * 子にImageSetterを追加する。順番はあまり関係ない。
	 * @param setter イメージセッター。null不可。
	 */
	void addSetter(ImageSetter setter);

	/**
	 * 例外が発生した。
	 * @param e 例外
	 * @param connectionInfo コネクション情報
	 */
void onException(Exception e, ConnectionInfo connectionInfo);

	/**
	 * 次のImageSetter。
	 * @return ImageSetterインスタンス or null
	 */
	ImageSetter next();

	/**
	 * このImageSetterのみに画像を設定する。JLabelのsetImageなど。
	 * @param image イメージ
	 */
	void setImage(Image image);

	/**
	 * 子ImageSetterを含めて画像を設定する。
	 * @param image イメージ
	 */
	void setImageRecursively(Image image);

	/**
	 * 子setterを設定する。
	 * @param next 子setter
	 */
	void setNext(ImageSetter next);
}
