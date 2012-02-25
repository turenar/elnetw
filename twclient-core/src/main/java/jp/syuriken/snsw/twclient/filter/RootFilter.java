package jp.syuriken.snsw.twclient.filter;

import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ImageCacher;

import twitter4j.Status;

/**
 * ルートフィルター
 * 
 * @author $Author$
 */
public class RootFilter extends MessageFilterAdapter {
	
	private TreeSet<Long> statusSet;
	
	private final ClientConfiguration configuration;
	
	private ImageCacher imageCacher;
	
	
	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 * 
	 */
	public RootFilter(ClientConfiguration configuration) {
		this.configuration = configuration;
		imageCacher = configuration.getImageCacher();
		statusSet = new TreeSet<Long>();
	}
	
	@Override
	public Status onStatus(Status originalStatus) {
		synchronized (statusSet) {
			if (statusSet.contains(originalStatus.getId())) {
				return null;
			} else {
				Status status = originalStatus.isRetweet() ? originalStatus.getRetweetedStatus() : originalStatus;
				if (configuration.isMentioned(originalStatus.getUserMentionEntities())) {
					configuration.getUtility().sendNotify(status.getUser().getName(), originalStatus.getText(),
							imageCacher.getImageFile(status.getUser()));
				}
				statusSet.add(originalStatus.getId());
				return originalStatus;
			}
		}
	}
}
