package jp.syuriken.snsw.twclient;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.internal.http.HTMLEntity;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

/**
 * RT/favしたかどうかを格納できるTwitter拡張型
 * 
 * @author $Author$
 */
@SuppressWarnings({
	"serial",
	"deprecation"
})
public class TwitterStatus implements Status {
	
	/*package*/static abstract class EntityImplBase {
		
		private final int start;
		
		private final int end;
		
		
		protected EntityImplBase(int start, int end) {
			if (start < 0 || end < 0) {
				throw new AssertionError("start=" + start + ", end=" + end);
			}
			this.start = start;
			this.end = end;
		}
		
		public int getEnd() {
			return end;
		}
		
		public int getStart() {
			return start;
		}
		
	}
	
	/*package*/static class HashtagEntityImpl extends EntityImplBase implements HashtagEntity {
		
		private final HashtagEntity base;
		
		
		public HashtagEntityImpl(HashtagEntity base, int start, int end) {
			super(start, end);
			this.base = base;
		}
		
		@Override
		public String getText() {
			return base.getText();
		}
	}
	
	/*package*/static class MediaEntityImpl extends URLEntityImpl implements MediaEntity {
		
		private final MediaEntity base;
		
		
		public MediaEntityImpl(MediaEntity base, int start, int end) {
			super(base, start, end);
			this.base = base;
		}
		
		@Override
		public long getId() {
			return base.getId();
		}
		
		@Override
		public URL getMediaURL() {
			return base.getMediaURL();
		}
		
		@Override
		public URL getMediaURLHttps() {
			return base.getMediaURLHttps();
		}
		
		@Override
		public Map<Integer, Size> getSizes() {
			return base.getSizes();
		}
	}
	
	/*package*/static class URLEntityImpl extends EntityImplBase implements URLEntity {
		
		private final URLEntity base;
		
		
		public URLEntityImpl(URLEntity base, int start, int end) {
			super(start, end);
			this.base = base;
		}
		
		@Override
		public String getDisplayURL() {
			return base.getDisplayURL();
		}
		
		@Override
		public URL getExpandedURL() {
			return base.getExpandedURL();
		}
		
		@Override
		public URL getURL() {
			return base.getURL();
		}
	}
	
	/*package*/static class UserMentionEntityImpl extends EntityImplBase implements UserMentionEntity {
		
		private final UserMentionEntity base;
		
		
		public UserMentionEntityImpl(UserMentionEntity base, int start, int end) {
			super(start, end);
			this.base = base;
		}
		
		@Override
		public long getId() {
			return base.getId();
		}
		
		@Override
		public String getName() {
			return base.getName();
		}
		
		@Override
		public String getScreenName() {
			return base.getScreenName();
		}
	}
	
	
	private static final Logger logger;
	
	private static final Map<String, String> escapeEntityMap;
	static {
		logger = LoggerFactory.getLogger(TwitterStatus.class);
		try {
			Class.forName("twitter4j.internal.http.HTMLEntity");
			Field escapeEntityMapField;
			escapeEntityMapField = HTMLEntity.class.getDeclaredField("escapeEntityMap");
			escapeEntityMapField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, String> escapeEntities = (Map<String, String>) escapeEntityMapField.get(null);
			escapeEntityMap = escapeEntities;
		} catch (ClassNotFoundException e) {
			logger.error("Class.forName(twitter4j.internal.http.HTMLEntity) failed", e);
			throw new AssertionError(e);
		} catch (SecurityException e) {
			logger.error("getField(HTMLEntity.escapeEntityMap) blocked", e);
			throw new AssertionError(e);
		} catch (NoSuchFieldException e) {
			logger.error("getField(HTMLEntity.escapeEntityMap) failed", e);
			throw new AssertionError(e);
		} catch (IllegalArgumentException e) {
			logger.error("getField(HTMLEntity.escapeEntityMap) failed", e);
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			logger.error("getField(HTMLEntity.escapeEntityMap) failed", e);
			throw new AssertionError(e);
		} catch (ClassCastException e) {
			logger.error("getField(HTMLEntity.escapeEntityMap) failed", e);
			throw new AssertionError(e);
		}
	}
	
	
	private static <T>T[] getEntities(T[] entities, T[] newEntities, List<int[]> list,
			Constructor<? extends T> constructor) {
		for (int i = 0; i < entities.length; i++) {
			T entity = entities[i];
			int start = getEntityStart(entity);
			int end = getEntityEnd(entity);
			for (int[] ia : list) {
				if (start > ia[0]) {
					int decr = ia[1];
					start = start - decr;
					end = end - decr;
				}
			}
			try {
				newEntities[i] = constructor.newInstance(entity, start, end);
			} catch (Exception e) {
				logger.error("#getEntities got Exception: entity={}, start={}, end={}",
						Utility.toArray(entity, start, end));
				throw new AssertionError(e);
			}
		}
		return newEntities;
	}
	
	/**
	 * Entityのindice startを取得する
	 * 
	 * @param entity エンティティ
	 * @return indice start
	 */
	public static int getEntityEnd(Object entity) {
		if (entity instanceof URLEntity) {
			return ((URLEntity) entity).getEnd();
		} else if (entity instanceof HashtagEntity) {
			return ((HashtagEntity) entity).getEnd();
		} else if (entity instanceof UserMentionEntity) {
			return ((UserMentionEntity) entity).getEnd();
		} else {
			throw new IllegalArgumentException("entity is not instanceof *Entity");
		}
	}
	
	/**
	 * Entityのindice endを取得する
	 * 
	 * @param entity エンティティ
	 * @return indice end
	 */
	public static int getEntityStart(Object entity) {
		if (entity instanceof URLEntity) {
			return ((URLEntity) entity).getStart();
		} else if (entity instanceof HashtagEntity) {
			return ((HashtagEntity) entity).getStart();
		} else if (entity instanceof UserMentionEntity) {
			return ((UserMentionEntity) entity).getStart();
		} else {
			throw new IllegalArgumentException("entity is not instanceof *Entity");
		}
	}
	
	
	private final Status originalStatus;
	
	private volatile boolean favorited;
	
	private volatile boolean retweetedByMe;
	
	private TwitterStatus retweetedStatus;
	
	private String escapedText;
	
	private UserMentionEntity[] userMentionEntities;
	
	private MediaEntity[] mediaEntities;
	
	private URLEntity[] urlEntities;
	
	private HashtagEntity[] hashtagEntities;
	
	private String text;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param originalStatus オリジナルステータス
	 */
	public TwitterStatus(Status originalStatus) {
		this.originalStatus = originalStatus;
		Status retweetedStatus = originalStatus.getRetweetedStatus();
		if (retweetedStatus != null && retweetedStatus instanceof TwitterStatus == false) {
			retweetedStatus = new TwitterStatus(retweetedStatus);
		}
		this.retweetedStatus = (TwitterStatus) retweetedStatus;
		favorited = originalStatus.isFavorited();
		retweetedByMe = originalStatus.isRetweetedByMe();
		urlEntities = originalStatus.getURLEntities();
		hashtagEntities = originalStatus.getHashtagEntities();
		mediaEntities = originalStatus.getMediaEntities();
		userMentionEntities = originalStatus.getUserMentionEntities();
		text = originalStatus.getText();
		
		if (originalStatus instanceof TwitterStatus) {
			escapedText = originalStatus.getText();
		} else {
			String json = DataObjectFactory.getRawJSON(originalStatus);
			if (json == null) {
				escapedText = HTMLEntity.escape(originalStatus.getText());
			} else {
				try {
					JSONObject jsonObject = new JSONObject(json);
					escapedText = jsonObject.getString("text");
				} catch (JSONException e) {
					logger.error("Cannot parse json", e); // already parsed by StatusJSONImpl
					throw new AssertionError(e);
				}
			}
			ArrayList<int[]> replacedList = new ArrayList<int[]>();
			StringBuilder escapedTextBuilder = new StringBuilder(escapedText);
			
			// == Original is Twitter4j v2.2.5 HTMLEntity.java ==
			int index = 0;
			int semicolonIndex;
			String escaped;
			String entity;
			while (index < escapedTextBuilder.length()) {
				index = escapedTextBuilder.indexOf("&", index);
				if (-1 == index) {
					break;
				}
				semicolonIndex = escapedTextBuilder.indexOf(";", index);
				if (-1 != semicolonIndex) {
					escaped = escapedTextBuilder.substring(index, semicolonIndex + 1);
					entity = escapeEntityMap.get(escaped);
					if (entity != null) {
						replacedList.add(new int[] {
							index,
							escaped.length() - entity.length()
						});
						escapedTextBuilder.replace(index, semicolonIndex + 1, entity);
					}
					index++;
				} else {
					break;
				}
			}
			// == here is end ==
			text = escapedTextBuilder.toString();
			
			try {
				if (urlEntities != null) {
					urlEntities =
							getEntities(urlEntities, new URLEntity[urlEntities.length], replacedList,
									URLEntityImpl.class.getDeclaredConstructor(URLEntity.class, int.class, int.class));
				}
				if (hashtagEntities != null) {
					hashtagEntities =
							getEntities(hashtagEntities, new HashtagEntity[hashtagEntities.length], replacedList,
									HashtagEntityImpl.class.getConstructor(HashtagEntity.class, int.class, int.class));
				}
				if (mediaEntities != null) {
					mediaEntities =
							getEntities(mediaEntities, new MediaEntity[mediaEntities.length], replacedList,
									MediaEntityImpl.class.getConstructor(MediaEntity.class, int.class, int.class));
				}
				if (userMentionEntities != null) {
					userMentionEntities =
							getEntities(userMentionEntities, new UserMentionEntity[userMentionEntities.length],
									replacedList, UserMentionEntityImpl.class.getConstructor(UserMentionEntity.class,
											int.class, int.class));
				}
			} catch (AssertionError error) {
				logger.error("caught error on preparing entities", error);
				logger.error("original={}, json={}", originalStatus, json);
			} catch (Exception e) {
				logger.error("Exception on converting entity", e);
				throw new AssertionError(e);
			}
		}
	}
	
	@Override
	public int compareTo(Status o) {
		return originalStatus.compareTo(o);
	}
	
	@Override
	public int getAccessLevel() {
		return originalStatus.getAccessLevel();
	}
	
	@Deprecated
	@Override
	public Annotations getAnnotations() {
		return originalStatus.getAnnotations();
	}
	
	@Override
	public long[] getContributors() {
		return originalStatus.getContributors();
	}
	
	@Override
	public Date getCreatedAt() {
		return originalStatus.getCreatedAt();
	}
	
	/**
	 * HTMLEntityが含まれたtext。
	 * この文字列を使用するときは originalStatus.get*Entities() を使用してください。
	 * 
	 * @return HTMLEntityでエスケープされたテキスト
	 */
	public String getEscapedText() {
		return escapedText;
	}
	
	@Override
	public GeoLocation getGeoLocation() {
		return originalStatus.getGeoLocation();
	}
	
	@Override
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}
	
	@Override
	public long getId() {
		return originalStatus.getId();
	}
	
	@Override
	public String getInReplyToScreenName() {
		return originalStatus.getInReplyToScreenName();
	}
	
	@Override
	public long getInReplyToStatusId() {
		return originalStatus.getInReplyToStatusId();
	}
	
	@Override
	public long getInReplyToUserId() {
		return originalStatus.getInReplyToUserId();
	}
	
	@Override
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}
	
	/**
	 * twitter4jから渡されたStatusを取得する。
	 * 
	 * @return twitter4jから渡されたStatus
	 */
	public Status getOriginalStatus() {
		return originalStatus;
	}
	
	@Override
	public Place getPlace() {
		return originalStatus.getPlace();
	}
	
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return originalStatus.getRateLimitStatus();
	}
	
	@Override
	public long getRetweetCount() {
		return originalStatus.getRetweetCount();
	}
	
	@Override
	public TwitterStatus getRetweetedStatus() {
		return retweetedStatus;
	}
	
	@Override
	public String getSource() {
		return originalStatus.getSource();
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public URLEntity[] getURLEntities() {
		return urlEntities;
	}
	
	@Override
	public User getUser() {
		return originalStatus.getUser();
	}
	
	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}
	
	@Override
	public boolean isFavorited() {
		return favorited;
	}
	
	@Override
	public boolean isRetweet() {
		return originalStatus.isRetweet();
	}
	
	@Override
	public boolean isRetweetedByMe() {
		return retweetedByMe;
	}
	
	@Override
	public boolean isTruncated() {
		return originalStatus.isTruncated();
	}
	
	/**
	 * ふぁぼられたかを設定する
	 * 
	 * @param favorited ふぁぼられたかどうか
	 */
	public void setFavorited(boolean favorited) {
		this.favorited = favorited;
	}
	
	/**
	 * ユーザーがリツイートしたかどうかを設定する
	 * 
	 * @param retweetedByMe リツイートされたかどうか
	 */
	public void setRetweetedByMe(boolean retweetedByMe) {
		this.retweetedByMe = retweetedByMe;
	}
}
