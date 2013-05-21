package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * ポストリストに登録するために必要な情報を格納するクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class StatusData {

	/** この情報が作られる要因となった元オブジェクト。nullの可能性があります。 */
	public final Object tag;

	/** イメージ部分用のコンポーネント */
	public JComponent image;

	/** 送信元表示用のコンポーネント */
	public JComponent sentBy;

	/** 実際のテキスト表示用のコンポーネント */
	public JLabel data;

	/** ポップアップメニュー */
	public JPopupMenu popupMenu = null;

	/** 背景色 */
	public Color backgroundColor = Color.WHITE;

	/** 前景色 */
	public Color foregroundColor = Color.BLACK;

	/** ツールチップ */
	public String tooltip = null;

	/** このクラスのインスタンスが作られる原因を作ったユーザー名 (Twitterユーザーじゃなくてもおk) */
	public String user = null;

	/** このクラスのインスタンスが作られる原因の発生日時 */
	public final Date date;

	/** ユニークなID */
	public final long id;

	private static AtomicLong uniqueLong = new AtomicLong(0xffffffff00000000L);


	/**
	 * インスタンスを生成する。
	 *
	 * <p>idは自動で <code>0xffff_ffff_0000_0000L</code>を増分していった値を付ける。</p>
	 * @param tag インスタンスを生成する原因の情報。null可。
	 * @param date インスタンスを生成する原因の発生日時
	 */
	public StatusData(Object tag, Date date) {
		this(tag, date, uniqueLong.getAndIncrement());
	}

	/**
	 * インスタンスを生成する。
	 * @param tag インスタンスを生成する原因の情報。null可。
	 * @param date インスタンスを生成する原因の発生日時
	 * @param id ユニークなID
	 *
	 */
	public StatusData(Object tag, Date date, long id) {
		this.tag = tag;
		this.id = id;
		this.date = (Date) date.clone();
	}

	/**
	 * Tweet/DirectMessageでは無いことを調べる。
	 *
	 * @return sentBy.getName()が"!"で始まるかどうか
	 */
	public boolean isSystemNotify() {
		return user.startsWith("!");
	}
}
