package jp.syuriken.snsw.twclient;

/**
 * タイムラインビュー
 * 
 * @author $Author: snsoftware $
 */
public class TimelineViewTab extends DefaultClientTab {
	
	private DefaultRenderer renderer = new DefaultRenderer();
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 */
	protected TimelineViewTab(ClientConfiguration configuration) {
		super(configuration);
	}
	
	@Override
	public DefaultRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public String getTitle() {
		return "Timeline";
	}
}
