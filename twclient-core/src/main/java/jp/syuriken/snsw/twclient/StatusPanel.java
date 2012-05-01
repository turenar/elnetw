package jp.syuriken.snsw.twclient;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * StatusDataを格納するだけのなんちゃら
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
	 * 元になる情報が作成された日時で比較する。同じ場合はidを比較する。
	 */
	@Override
	public int compareTo(StatusPanel o) {
		int result = statusData.date.compareTo(o.statusData.date);
		if (result == 0) {
			result = (statusData.id < o.statusData.id ? -1 : (statusData.id == o.statusData.id ? 0 : 1));
		}
		return result;
	}
	
	/**
	 * statusData
	 * 
	 * @return the statusData
	 */
	public StatusData getStatusData() {
		return statusData;
	}
	
}
