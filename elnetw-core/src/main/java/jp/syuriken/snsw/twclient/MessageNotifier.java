package jp.syuriken.snsw.twclient;

import java.io.File;
import java.io.IOException;

/**
 * 通知が送信されるクラスのインターフェース
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public interface MessageNotifier {

	/**
	 * 通知を送信する
	 *
	 * @param summary   概要
	 * @param text      テキスト
	 * @param imageFile アイコン。ない場合はnull
	 * @throws java.io.IOException 外部プロセスの起動に失敗
	 */
	void sendNotify(String summary, String text, File imageFile) throws IOException;
}
