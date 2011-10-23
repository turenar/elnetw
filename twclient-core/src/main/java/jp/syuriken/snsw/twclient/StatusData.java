package jp.syuriken.snsw.twclient;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
public class StatusData {
	
	public JLabel image;
	
	public JLabel sentBy;
	
	public JLabel data;
	
	public JPopupMenu popupMenu = new JPopupMenu();
	
	public Color backgroundColor = new Color(255, 255, 255);
	
	public Color foregroundColor = new Color(0, 0, 0);
}
