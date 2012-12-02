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
 * RT/favしたかどうかを格納できるStatus拡張型
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings({
	"serial"
})
public class TwitterStatus implements Status, TwitterExtendedObject {
	
	/*package*/static abstract class EntityImplBase {
		
		private final int end;
		
		private final int start;
		
		
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
		
		@Override
		public String getType() {
			return base.getType();
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
	
	
	private static final Map<String, String> escapeEntityMap;
	
	private static final Constructor<HashtagEntityImpl> HASHTAG_ENTITY_CONSTRUCTOR;
	
	private static final Logger logger;
	
	private static final Constructor<MediaEntityImpl> MEDIA_ENTITY_CONSTRUCTOR;
	
	private static final Constructor<URLEntityImpl> URL_ENTITY_CONSTRUCTOR;
	
	private static final Constructor<UserMentionEntityImpl> USERMENTION_ENTITY_CONSTRUCTOR;
	
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
		try {
			USERMENTION_ENTITY_CONSTRUCTOR =
					UserMentionEntityImpl.class.getConstructor(UserMentionEntity.class, int.class, int.class);
			MEDIA_ENTITY_CONSTRUCTOR = MediaEntityImpl.class.getConstructor(MediaEntity.class, int.class, int.class);
			HASHTAG_ENTITY_CONSTRUCTOR =
					HashtagEntityImpl.class.getConstructor(HashtagEntity.class, int.class, int.class);
			URL_ENTITY_CONSTRUCTOR = URLEntityImpl.class.getDeclaredConstructor(URLEntity.class, int.class, int.class);
		} catch (SecurityException e) {
			logger.error("getConstructor(*EntityImpl) blocked", e);
			throw new AssertionError(e);
		} catch (NoSuchMethodException e) {
			logger.error("getConstructor(*EntityImpl) failed", e);
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
	public static final int getEntityEnd(Object entity) {
		if (entity instanceof URLEntity) {
			return ((URLEntity) entity).getEnd();
		} else if (entity instanceof HashtagEntity) {
			return ((HashtagEntity) entity).getEnd();
		} else if (entity instanceof UserMentionEntity) {
			return ((UserMentionEntity) entity).getEnd();
		} else if (entity instanceof EntityImplBase) {
			return ((EntityImplBase) entity).getEnd();
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
	public static final int getEntityStart(Object entity) {
		if (entity instanceof URLEntity) {
			return ((URLEntity) entity).getStart();
		} else if (entity instanceof HashtagEntity) {
			return ((HashtagEntity) entity).getStart();
		} else if (entity instanceof UserMentionEntity) {
			return ((UserMentionEntity) entity).getStart();
		} else if (entity instanceof EntityImplBase) {
			return ((EntityImplBase) entity).getStart();
		} else {
			throw new IllegalArgumentException("entity is not instanceof *Entity");
		}
	}
	
	private static JSONObject getJsonObject(ClientConfiguration configuration, Status originalStatus)
			throws AssertionError {
		String json = DataObjectFactory.getRawJSON(originalStatus);
		JSONObject jsonObject = null;
		if (json != null) {
			try {
				jsonObject = new JSONObject(json);
			} catch (JSONException e) {
				logger.error("Cannot parse json", e); // already parsed by Status*Impl
				throw new AssertionError(e);
			}
		}
		return jsonObject;
	}
	
	
	private transient final ClientConfiguration configuration;
	
	private final long[] contributors;
	
	private final Date createdAt;
	
	private final String escapedText;
	
	private final GeoLocation geoLocation;
	
	private/*final*/HashtagEntity[] hashtagEntities;
	
	private final long id;
	
	private final String inReplyToScreenName;
	
	private final long inReplyToStatusId;
	
	private final long inReplyToUserId;
	
	private final boolean isTruncated;
	
	private boolean loadedInitialization;
	
	private/*final*/MediaEntity[] mediaEntities;
	
	private final Place place;
	
	private final TwitterStatus retweetedStatus;
	
	private final String source;
	
	private/*final*/String text;
	
	private/*final*/URLEntity[] urlEntities;
	
	private final User user;
	
	private/*final*/UserMentionEntity[] userMentionEntities;
	
	private volatile boolean favorited;
	
	private volatile long retweetCount;
	
	private volatile boolean retweetedByMe;
	
	private final transient int accessLevel;
	
	private final transient RateLimitStatus rateLimitStatus;
	
	private final String json;
	
	private final UserMentionEntity[] escapedUserMentionEntities;
	
	private final MediaEntity[] escapedMediaEntities;
	
	private final HashtagEntity[] escapedHashtagEntities;
	
	private final URLEntity[] escapedUrlEntities;
	
	
	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 * @param originalStatus オリジナルステータス
	 */
	public TwitterStatus(ClientConfiguration configuration, Status originalStatus) {
		this(configuration, originalStatus, getJsonObject(configuration, originalStatus));
	}
	
	/**
	 * インスタンスを生成する。
	 * @param configuration 設定
	 * @param originalStatus オリジナルステータス
	 * @param jsonObject 生JSON。取得できなかった場合にはnull。
	 */
	public TwitterStatus(ClientConfiguration configuration, Status originalStatus, JSONObject jsonObject) {
		this.configuration = configuration;
		json = jsonObject == null ? null : jsonObject.toString();
		
		favorited = originalStatus.isFavorited();
		retweetedByMe = originalStatus.isRetweetedByMe();
		escapedUrlEntities = urlEntities = originalStatus.getURLEntities();
		escapedHashtagEntities = hashtagEntities = originalStatus.getHashtagEntities();
		escapedMediaEntities = mediaEntities = originalStatus.getMediaEntities();
		escapedUserMentionEntities = userMentionEntities = originalStatus.getUserMentionEntities();
		text = originalStatus.getText();
		createdAt = originalStatus.getCreatedAt();
		id = originalStatus.getId();
		source = originalStatus.getSource();
		isTruncated = originalStatus.isTruncated();
		inReplyToStatusId = originalStatus.getInReplyToStatusId();
		inReplyToUserId = originalStatus.getInReplyToUserId();
		favorited = originalStatus.isFavorited();
		inReplyToScreenName = originalStatus.getInReplyToScreenName();
		geoLocation = originalStatus.getGeoLocation();
		place = originalStatus.getPlace();
		retweetCount = originalStatus.getRetweetCount();
		retweetedByMe = originalStatus.isRetweetedByMe();
		contributors = originalStatus.getContributors();
		user = getCachedUser(originalStatus.getUser());
		rateLimitStatus = originalStatus.getRateLimitStatus();
		accessLevel = originalStatus.getAccessLevel();
		
		Status retweetedStatus = originalStatus.getRetweetedStatus();
		if (originalStatus instanceof TwitterStatus) {
			escapedText = originalStatus.getText();
			this.retweetedStatus = (TwitterStatus) retweetedStatus;
			return;
		}
		
		if (jsonObject == null) {
			escapedText = HTMLEntity.escape(originalStatus.getText());
		} else {
			try {
				escapedText = jsonObject.getString("text");
				if (retweetedStatus != null && retweetedStatus instanceof TwitterStatus == false) {
					CacheManager cacheManager = configuration.getCacheManager();
					Status cachedStatus = cacheManager.getCachedStatus(retweetedStatus.getId());
					if (cachedStatus == null || cachedStatus instanceof TwitterStatus == false) {
						Status status =
								new TwitterStatus(configuration, retweetedStatus, jsonObject.isNull("retweeted_status")
										? null : jsonObject.getJSONObject("retweeted_status"));
						cachedStatus = cacheManager.cacheStatusIfAbsent(status);
						if (cachedStatus == null) {
							cachedStatus = status;
						}
					}
					retweetedStatus = cachedStatus;
				}
			} catch (JSONException e) {
				logger.error("Cannot parse json", e); // already parsed by StatusJSONImpl
				throw new RuntimeException(e);
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
								URL_ENTITY_CONSTRUCTOR);
			}
			if (hashtagEntities != null) {
				hashtagEntities =
						getEntities(hashtagEntities, new HashtagEntity[hashtagEntities.length], replacedList,
								HASHTAG_ENTITY_CONSTRUCTOR);
			}
			if (mediaEntities != null) {
				mediaEntities =
						getEntities(mediaEntities, new MediaEntity[mediaEntities.length], replacedList,
								MEDIA_ENTITY_CONSTRUCTOR);
			}
			if (userMentionEntities != null) {
				userMentionEntities =
						getEntities(userMentionEntities, new UserMentionEntity[userMentionEntities.length],
								replacedList, USERMENTION_ENTITY_CONSTRUCTOR);
			}
		} catch (AssertionError error) {
			logger.error("caught error on preparing entities", error);
			logger.error("original={}, json={}", originalStatus, jsonObject);
		}
		this.retweetedStatus = (TwitterStatus) retweetedStatus;
	}
	
	@Override
	public int compareTo(Status b) {
		long thisId = id;
		long thatId = b.getId();
		if (thisId < thatId) {
			return -1;
		} else if (thisId > thatId) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (this == obj) {
			return true;
		} else if (obj instanceof Status == false) {
			return false;
		}
		return ((Status) obj).getId() == id;
	}
	
	@Override
	public int getAccessLevel() {
		return accessLevel;
	}
	
	/**
	 * @deprecated (Twitter4Jにならい、非推奨メソッドです) この実装では常にnullを返します。
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public twitter4j.Annotations getAnnotations() {
		return null;
	}
	
	private User getCachedUser(User user) {
		if (user instanceof TwitterUser) {
			return user;
		}
		
		CacheManager cacheManager = configuration.getCacheManager();
		User cachedUser = cacheManager.getCachedUser(user.getId());
		if (cachedUser == null) {
			TwitterUser twitterUser = new TwitterUser(configuration, user);
			cachedUser = cacheManager.cacheUserIfAbsent(twitterUser);
			if (cachedUser == null) {
				cachedUser = twitterUser;
			}
		}
		return cachedUser;
	}
	
	@Override
	public long[] getContributors() {
		return contributors;
	}
	
	@Override
	public Date getCreatedAt() {
		return createdAt;
	}
	
	/**
	 * {@link #getEscapedText()} を使用した時の {@link HashtagEntity} 配列
	 * 
	 * @return {@link HashtagEntity}配列
	 */
	public HashtagEntity[] getEscapedHashtagEntities() {
		return escapedHashtagEntities;
	}
	
	/**
	 * {@link #getEscapedText()} を使用した時の {@link MediaEntity} 配列
	 * 
	 * @return {@link MediaEntity}配列
	 */
	public MediaEntity[] getEscapedMediaEntities() {
		return escapedMediaEntities;
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
	
	/**
	 * {@link #getEscapedText()} を使用した時の {@link URLEntity} 配列
	 * 
	 * @return {@link URLEntity}配列
	 */
	public URLEntity[] getEscapedURLEntities() {
		return escapedUrlEntities;
	}
	
	/**
	 * {@link #getEscapedText()} を使用した時の {@link UserMentionEntity} 配列
	 * 
	 * @return {@link UserMentionEntity}配列
	 */
	public UserMentionEntity[] getEscapedUserMentionEntities() {
		return escapedUserMentionEntities;
	}
	
	@Override
	public GeoLocation getGeoLocation() {
		return geoLocation;
	}
	
	@Override
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}
	
	@Override
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}
	
	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}
	
	@Override
	public String getJson() {
		return json;
	}
	
	@Override
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}
	
	@Override
	public Place getPlace() {
		return place;
	}
	
	@Override
	public RateLimitStatus getRateLimitStatus() {
		return rateLimitStatus;
	}
	
	@Override
	public long getRetweetCount() {
		return retweetCount;
	}
	
	@Override
	public TwitterStatus getRetweetedStatus() {
		return retweetedStatus;
	}
	
	@Override
	public String getSource() {
		return source;
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
		return user;
	}
	
	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}
	
	@Override
	public int hashCode() {
		return (int) id;
	}
	
	@Override
	public boolean isFavorited() {
		return favorited;
	}
	
	/**
	 * このステータスが起動時に読み込まれたものかどうかを調べる
	 * 
	 * @return 起動時に読み込まれたならtrue
	 */
	public boolean isLoadedInitialization() {
		return loadedInitialization;
	}
	
	@Override
	public boolean isRetweet() {
		return retweetedStatus != null;
	}
	
	@Override
	public boolean isRetweetedByMe() {
		return retweetedByMe;
	}
	
	@Override
	public boolean isTruncated() {
		return isTruncated;
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
	 * このステータスは起動時に読み込まれたものです
	 * @param loadedInitialization 起動時に読み込まれたならtrue 
	 */
	public void setLoadedInitialization(boolean loadedInitialization) {
		this.loadedInitialization = loadedInitialization;
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
