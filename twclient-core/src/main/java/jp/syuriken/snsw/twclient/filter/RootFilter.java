package jp.syuriken.snsw.twclient.filter;

import java.text.MessageFormat;
import java.util.TreeSet;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.TwitterStatus;
import jp.syuriken.snsw.twclient.internal.InitialMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;

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
	public boolean onChangeAccount(boolean forWrite) {
		configuration.getFetchScheduler().onChangeAccount(forWrite);
		return false;
	}
	
	@Override
	public DirectMessage onDirectMessage(DirectMessage message) {
		if (message instanceof InitialMessage == false) {
			User sender = message.getSender();
			configuration
				.getFrameApi()
				.getUtility()
				.sendNotify(MessageFormat.format("{0} ({1})", sender.getScreenName(), sender.getName()),
						"DMを受信しました：" + message.getText(), imageCacher.getImageFile(sender));
		}
		return message;
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