package jp.syuriken.snsw.twclient;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
@SuppressWarnings("serial")
public class StatusPanel extends JPanel implements Comparable<StatusPanel> {
	
	private final StatusData statusData;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param layout レイアウトマネージャ
	 * @param statusData 元になる {@link StatusData}
	 */
	public StatusPanel(LayoutManager layout, StatusData statusData) {
		super(layout);
		this.statusData = statusData;
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param statusData 元になる {@link StatusData}
	 */
	public StatusPanel(StatusData statusData) {
		super();
		this.statusData = statusData;
	}
	
	/**
	 * 元になる情報が作成された日時で比較する。
	 * 
	 * <p><code>o1.getStatusData.date.compareTo(o2.getStatusData.date)</code></p>
	 */
	@Override
	public int compareTo(StatusPanel o) {
		return statusData.date.compareTo(o.statusData.date);
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @return the statusData
	 */
	public StatusData getStatusData() {
		return statusData;
	}
	
}
