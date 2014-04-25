package jp.syuriken.snsw.twclient.gui.render.simple;

import java.util.HashMap;

import javax.swing.JLabel;

import jp.syuriken.snsw.twclient.gui.ImageResource;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import jp.syuriken.snsw.twclient.media.MediaUrlDispatcher;
import jp.syuriken.snsw.twclient.media.UrlInfo;
import jp.syuriken.snsw.twclient.media.UrlResolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.EntitySupport;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.TweetEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Abstract Render Object for EntitySupport
 */
public abstract class EntitySupportRenderObject extends AbstractRenderObject implements MediaUrlDispatcher {
	protected HashMap<String, UrlInfo> urlInfoMap;

	public EntitySupportRenderObject(SimpleRenderer renderer) {
		super(renderer);
	}

	protected String getTweetViewText(EntitySupport status, String text) {
		StringBuilder stringBuilder = new StringBuilder(text.length() * 2);

		TweetEntity[] entities = sortEntities(status);
		int offset = 0;
		for (TweetEntity entity : entities) {
			int start = entity.getStart();
			int end = entity.getEnd();
			String url;

			stringBuilder.append(escapeHTML(text.substring(offset, start)));

			if (entity instanceof HashtagEntity) {
				HashtagEntity hashtagEntity = (HashtagEntity) entity;
				IntentArguments intent = getIntentArguments("hashtag");
				intent.putExtra("name", hashtagEntity.getText());
				url = getFrameApi().getCommandUrl(intent);
				stringBuilder.append("<a href='").append(url).append("'>")
						.append(escapeHTML(text.substring(start, end)))
						.append("</a>");
			} else if (entity instanceof URLEntity) {
				URLEntity urlEntity = (URLEntity) entity;
				boolean isMediaFile;
				// entityがMediaEntity (=pic.twitter.com)かどうかを調べる。
				// MediaEntityの場合は無条件で画像ファイルとみなす。
				// URLEntityの場合は、UrlResolverManagerに#initComponents()で問い合わせた結果を元に判断。
				if (urlEntity instanceof MediaEntity) {
					MediaEntity mediaEntity = (MediaEntity) urlEntity;
					IntentArguments intent = getIntentArguments("openimg");
					intent.putExtra("url", mediaEntity.getMediaURL());
					url = getFrameApi().getCommandUrl(intent);
					isMediaFile = true;
				} else {
					UrlInfo urlInfo = urlInfoMap.get(urlEntity.getExpandedURL());
					if (urlInfo != null && urlInfo.isMediaFile()) {
						IntentArguments intent = getIntentArguments("openimg");
						intent.putExtra("url", urlInfo.getResolvedUrl());
						url = getFrameApi().getCommandUrl(intent);
						isMediaFile = true;
					} else {
						url = urlEntity.getURL();
						isMediaFile = false;
					}
				}
				stringBuilder.append("<a href='").append(url).append("'>")
						.append(escapeHTML(urlEntity.getDisplayURL()));
				if (isMediaFile) {
					stringBuilder.append("<img src='")
							.append(ImageResource.getUrlImageFileIcon())
							.append("' border='0'>");
				}
				stringBuilder.append("</a>");
			} else if (entity instanceof UserMentionEntity) {
				UserMentionEntity mentionEntity = (UserMentionEntity) entity;
				IntentArguments intent = getIntentArguments("userinfo");
				intent.putExtra("screenName", mentionEntity.getScreenName());
				url = getFrameApi().getCommandUrl(intent);
				stringBuilder.append("<a href='").append(url).append("'>")
						.append(escapeHTML(text.substring(start, end)))
						.append("</a>");
			} else {
				throw new AssertionError();
			}

			offset = end;
		}
		escapeHTML(text.substring(offset), stringBuilder);
		return stringBuilder.toString();
	}

	@Override
	public void gotMediaUrl(String original, UrlInfo resolvedUrl) {
		urlInfoMap.put(original, resolvedUrl);
	}

	private static final Logger logger = LoggerFactory.getLogger(EntitySupportRenderObject.class);

	@Override
	public void onException(String url, Exception ex) {
		logger.warn("failed resolving url: {}", url, ex);
		renderer.onException(ex);
	}

	protected void setStatusTextWithEntities(EntitySupport status, String text) {
		StringBuilder statusText = new StringBuilder(text);

		URLEntity[] urlEntities = status.getURLEntities();
		if (urlEntities != null) {
			urlInfoMap = new HashMap<>();
			for (URLEntity entity : urlEntities) {
				String entityText = entity.getText();
				int start = statusText.indexOf(entityText);
				statusText.replace(start, start + entityText.length(), entity.getDisplayURL());
				UrlResolverManager.async(entity.getExpandedURL(), this);
			}
		}
		MediaEntity[] mediaEntities = status.getMediaEntities();
		if (mediaEntities != null) {
			for (URLEntity entity : mediaEntities) {
				String entityText = entity.getText();
				int start = statusText.indexOf(entityText);
				statusText.replace(start, start + entityText.length(), entity.getDisplayURL());
			}
		}
		componentStatusText = new JLabel(statusText.toString());
	}
}
