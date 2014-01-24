package jp.syuriken.snsw.twclient;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.UserStreamListener;

/**
 * 入出力データをディスパッチするためのクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface ClientMessageListener extends UserStreamListener, ConnectionLifeCycleListener, ClientEventConstants {

	/**
	 * アカウント変更
	 *
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。falseは読み込み用
	 */
	void onChangeAccount(boolean forWrite);

	/**
	 * core等が発する情報をキャッチする。この関数は自由に使えます。
	 *
	 * @param name リクエスト名。この名前で区別するのでできるだけFQCNなどで記述すると衝突の可能性が少なくなります。
	 * @param arg  引数。Stringが投げられると過信してはいけません。
	 */
	void onClientMessage(String name, Object arg);
}
