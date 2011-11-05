package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class StatusData {
	
	public final Object tag;
	
	public JLabel image;
	
	public JLabel sentBy;
	
	public JLabel data;
	
	public JPopupMenu popupMenu = new JPopupMenu();
	
	public Color backgroundColor = new Color(255, 255, 255);
	
	public Color foregroundColor = new Color(0, 0, 0);
	
	public String tooltip = null;
	
	public final Date date;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
	public StatusData(Object tag, Date date) {
		this.tag = tag;
		this.date = (Date) date.clone();
	}
	
	public boolean isSystemNotify() {
		return sentBy.getName().startsWith("!");
	}
	
}
