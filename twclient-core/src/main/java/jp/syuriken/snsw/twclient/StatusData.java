package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * ポストリストに登録するために必要な情報を格納するクラス。
 * 
 * @author $Author$
 */
public class StatusData {
	
	/** この情報が作られる要因となった元オブジェクト。nullの可能性があります。 */
	public final Object tag;
	
	public JLabel image;
	
	public JLabel sentBy;
	
	public JLabel data;
	
	public JPopupMenu popupMenu = new JPopupMenu();
	
	public Color backgroundColor = new Color(255, 255, 255);
	
	public Color foregroundColor = new Color(0, 0, 0);
	
	public String tooltip = null;
	
	public final Date date;
	
	public final Long id;
	
	private static AtomicLong uniqueLong = new AtomicLong(0xffffffff00000000L);
	
	
	public StatusData(Object tag, Date date) {
		this(tag, date, uniqueLong.getAndIncrement());
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
	public StatusData(Object tag, Date date, Long id) {
		this.tag = tag;
		this.id = id;
		this.date = (Date) date.clone();
	}
	
	public boolean isSystemNotify() {
		return sentBy.getName().startsWith("!");
	}
	
}
