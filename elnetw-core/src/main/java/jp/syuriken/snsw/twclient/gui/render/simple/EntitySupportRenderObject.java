/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient.gui.render.simple;

import java.util.ArrayList;
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
	private static final Logger logger = LoggerFactory.getLogger(EntitySupportRenderObject.class);
	protected HashMap<String, UrlInfo> urlInfoMap;

	/**
	 * インスタンス生成
	 *
	 * @param renderer レンダラ
	 */
	public EntitySupportRenderObject(SimpleRenderer renderer) {
		super(renderer);
	}

	/**
	 * ツイートビューに表示するテキストを取得する
	 *
	 * @param entitySupport エンティティ対応オブジェクト
	 * @param text          テキスト
	 * @return ツイートビューに表示するテキスト
	 */
	protected String getTweetViewText(EntitySupport entitySupport, String text) {
		StringBuilder stringBuilder = new StringBuilder(text.length() * 2);

		TweetEntity[] entities = sortEntities(entitySupport);
		MediaEntity[] extendedMediaEntities = entitySupport.getExtendedMediaEntities();
		if (extendedMediaEntities.length == 0) {
			extendedMediaEntities = null; // clearer condition
		}


		int offset = 0;
		for (TweetEntity entity : entities) {
			int start = entity.getStart();
			int end = entity.getEnd();
			String url;

			if (offset >= end) {
				//assert entity instanceof URLEntity;
				// entitySupport may be DM
				continue; // prev is MediaEntity and now is URLEntity...?
			}
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
				// さらに、extendedMediaEntityが付いている場合は、それも考慮。
				// URLEntityの場合は、UrlResolverManagerに#initComponents()で問い合わせた結果を元に判断。
				if (urlEntity instanceof MediaEntity) {
					MediaEntity mediaEntity = (MediaEntity) urlEntity;
					if (extendedMediaEntities != null && mediaEntity.getStart() == extendedMediaEntities[0].getStart()) {
						IntentArguments intent = getIntentArguments("openimg");
						ArrayList<String> imageLists = new ArrayList<>();
						intent.putExtra("urls", imageLists);
						url = getFrameApi().getCommandUrl(intent);
						stringBuilder.append("<a href='")
								.append(url)
								.append("'>")
								.append(escapeHTML(urlEntity.getDisplayURL()))
								.append("</a>");
						for (MediaEntity extendedMediaEntity : extendedMediaEntities) {
							IntentArguments oneImageIntent = getIntentArguments("openimg");
							imageLists.add(extendedMediaEntity.getMediaURLHttps());
							oneImageIntent.putExtra("url", extendedMediaEntity.getMediaURLHttps());
							String imgUrl = getFrameApi().getCommandUrl(oneImageIntent);
							stringBuilder.append("<a href='")
									.append(imgUrl)
									.append("'><img src='")
									.append(ImageResource.getUrlImageFileIcon())
									.append("' border='0'></a>");
						}
						offset = end;
						continue;
					}
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

			// don't forget update extendedMediaEntity case
			offset = end;
		}
		escapeHTML(text.substring(offset), stringBuilder);
		return stringBuilder.toString();
	}

	@Override
	public void gotMediaUrl(String original, UrlInfo resolvedUrl) {
		urlInfoMap.put(original, resolvedUrl);
	}

	@Override
	public void onException(String url, Exception ex) {
		logger.warn("failed resolving url: {}", url, ex);
		renderer.onException(ex);
	}

	/**
	 * リストに表示するのに適切なステータステキストを設定する
	 *
	 * @param entitySupport エンティティ対応オブジェクト
	 * @param text          テキスト
	 */
	protected void setStatusTextWithEntities(EntitySupport entitySupport, String text) {
		StringBuilder statusText = new StringBuilder(text);

		URLEntity[] urlEntities = entitySupport.getURLEntities();
		if (urlEntities != null) {
			urlInfoMap = new HashMap<>();
			for (URLEntity entity : urlEntities) {
				String entityText = entity.getText();
				int start = statusText.indexOf(entityText);
				statusText.replace(start, start + entityText.length(), entity.getDisplayURL());
				UrlResolverManager.async(entity.getExpandedURL(), this);
			}
		}
		MediaEntity[] mediaEntities = entitySupport.getMediaEntities();
		if (mediaEntities != null) {
			for (URLEntity entity : mediaEntities) {
				String entityText = entity.getText();
				int start = statusText.indexOf(entityText);
				if (start >= 0) {
					statusText.replace(start, start + entityText.length(), entity.getDisplayURL());
				}
			}
		}
		componentStatusText = new JLabel(statusText.toString());
	}
}
