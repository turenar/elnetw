package jp.syuriken.snsw.twclient.filter;

import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.TwitterStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private Logger logger = LoggerFactory.getLogger(RootFilter.class);
	
	
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
	public boolean onException(Exception ex) {
		logger.warn("handling onException", ex);
		return false;
	}
	
	@Override
	public Status onStatus(Status originalStatus) {
		synchronized (statusSet) {
			if (statusSet.contains(originalStatus.getId())) {
				return null;
			} else {
				Status status = originalStatus.isRetweet() ? originalStatus.getRetweetedStatus() : originalStatus;
				if ((status instanceof TwitterStatus ? ((TwitterStatus) status).isLoadedInitialization() == false
						: true) && configuration.isMentioned(status.getUserMentionEntities())) {
					configuration.getUtility().sendNotify(originalStatus.getUser().getName(), originalStatus.getText(),
							imageCacher.getImageFile(originalStatus.getUser()));
				}
				statusSet.add(originalStatus.getId());
				return originalStatus;
			}
		}
	}
}
