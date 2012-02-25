package jp.syuriken.snsw.twclient;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.UserStreamListener;

/**
 * 入出力データをディスパッチするためのクラス。
 * 
 * @author $Author$
 */
public interface ClientMessageListener extends UserStreamListener, ConnectionLifeCycleListener {
	
	/** フォーカス要請: 最初のコンポーネント (arg: なし) */
	public static final String REQUEST_FOCUS_FIRST_COMPONENT = "request focus firstComponent";
	
	/** フォーカス要請: 最後のコンポーネント (arg: なし) */
	
	public static final String REQUEST_FOCUS_LAST_COMPONENT = "request focus lastComponent";
	
	/** フォーカス要請: 次のコンポーネント (arg: なし) */
	
	public static final String REQUEST_FOCUS_NEXT_COMPONENT = "request focus nextComponent";
	
	/** フォーカス要請: 前のコンポーネント (arg: なし) */
	
	public static final String REQUEST_FOCUS_PREV_COMPONENT = "request focus prevComponent";
	
	/** フォーカス要請: タブコンポーネント (arg: なし) */
	
	public static final String REQUEST_FOCUS_TAB_COMPONENT = "request focus tabComponent";
	
	
	/**
	 * アカウント変更
	 * 
	 * @param forWrite 書き込み用アカウントが変更されたかどうか。falseは読み込み用
	 */
	void onChangeAccount(boolean forWrite);
	
	/**
	 * core等が発する情報をキャッチする。この関数は自由に使えますが、間違ってもあらゆる情報をキャッチするようにはしないでください。
	 * 
	 * @param name リクエスト名。この名前で区別するのでできるだけFQCNなどで記述すると衝突の可能性が少なくなります。
	 * @param arg 引数。Stringが投げられると過信してはいけません。
	 */
	void onClientMessage(String name, Object arg);
	
}
