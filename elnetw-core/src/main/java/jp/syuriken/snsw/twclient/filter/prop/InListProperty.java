package jp.syuriken.snsw.twclient.filter.prop;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import jp.syuriken.snsw.twclient.filter.FilterOperator;
import jp.syuriken.snsw.twclient.filter.FilterProperty;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.PagableResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.internal.http.HttpResponseCode;

/**
 * リストに入っているかどかを用いてフィルタする
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InListProperty implements FilterProperty {

	/** {@link UserFollewedByListFetcher} のスケジューラ */
	public class ListFetcherScheduler extends TimerTask {

		@Override
		public void run() {
			configuration.addJob(listFetcher);
		}
	}

	/** リストでフォローされているユーザーIDを取得するクラス */
	protected class UserFollewedByListFetcher implements ParallelRunnable {

		/** リストID */
		protected int listId;
		private Logger logger = LoggerFactory.getLogger(UserFollewedByListFetcher.class);


		/**
		 * インスタンスを生成する。
		 *
		 * @param listIdentifier リスト指定子 (:&lt;listId&gt;|&lt;listName&gt;|@&lt;owner&gt;/&lt;listName&gt;)
		 * @throws IllegalSyntaxException エラー
		 */
		public UserFollewedByListFetcher(String listIdentifier) throws IllegalSyntaxException {
			if (listIdentifier.startsWith(":")) {
				listId = Integer.parseInt(listIdentifier.substring(1));
			} else {
				String listOwner;
				String slug;
				if (listIdentifier.startsWith("@")) {
					int indexOf = listIdentifier.indexOf('/');
					if (indexOf == -1) {
						throw new IllegalSyntaxException(
								"[inlist] specify listIdentifier as :<listId> or <listName> or @<listOwner>/<listName>");
					}
					listOwner = listIdentifier.substring(1, indexOf);
					slug = listIdentifier.substring(indexOf);
				} else {
					listOwner =
							configuration.getCacheManager()
									.getUser(Long.parseLong(configuration.getAccountIdForRead())).getScreenName();
					slug = listIdentifier;
				}

				int life = 3;
				while (true) {
					try {
						listId = configuration.getTwitterForRead().showUserList(listOwner, slug).getId();
						break;
					} catch (TwitterException e) {
						if (e.getStatusCode() >= 500) {
							life--;
							if (life <= 0) {
								throw new IllegalSyntaxException("[inlist] Could not retrieve ListInformation: @"
										+ listOwner + "/" + slug, e);
							}
						} else if (e.getStatusCode() == 404) {
							throw new IllegalSyntaxException("[inlist] Not Found list: @" + listOwner + "/" + slug);
						}
					}
				}
			}
		}

		@Override
		public void run() {
			ArrayList<User> userListMembers = new ArrayList<User>();
			int listId = this.listId;
			long cursor = -1;
			int life = 10;
			do {
				try {
					PagableResponseList<User> userListMembersResponseList;
					userListMembersResponseList = configuration.getTwitterForRead().getUserListMembers(listId, cursor);
					cursor = userListMembersResponseList.getNextCursor();
					for (User user : userListMembersResponseList) {
						userListMembers.add(user);
					}
				} catch (TwitterException e) {
					int statusCode = e.getStatusCode();
					if (500 <= statusCode) {
						life--;
						if (life <= 0) {
							logger.info("failed retrieving listMembers (over retry limit)");
							return; // fail fetcher
						}
						continue; // with (prev) cursor
					} else if (statusCode == HttpResponseCode.NOT_FOUND) {
						logger.info("failed retrieving listMembers (Not Found): listId={}", listId);
						return; // fail fetcher
					} else {
						logger.warn("Twitter#getUserListMembers(listId=" + listId + ") returned " + statusCode, e);
						return; // fail fetcher
					}
				}
			} while (cursor != 0);

			long[] users = new long[userListMembers.size()];
			int i = 0;
			for (User user : userListMembers) {
				users[i++] = user.getId();
			}
			Arrays.sort(users);
			userIdsFollowedByList = users;
		}
	}

	/*package*/static final Logger logger = LoggerFactory.getLogger(InListProperty.class);
	private static final Constructor<? extends FilterProperty> factory;

	/**
	 * コンストラクタを取得
	 *
	 * @return factory
	 */
	public static Constructor<? extends FilterProperty> getFactory() {
		return factory;
	}

	/** 設定 */
	protected ClientConfiguration configuration;
	private boolean isEqual;
	/** (:&lt;listId&gt;|&lt;listName&gt;|@&lt;owner&gt;/&lt;listName&gt;) */
	protected String listIdentifier;
	/** リストでフォローされているユーザーIDの配列 (ソート済み) */
	protected long[] userIdsFollowedByList;
	/** リストフェッチャ */
	protected UserFollewedByListFetcher listFetcher;

	static {
		try {
			factory =
					InListProperty.class.getConstructor(ClientConfiguration.class, String.class, String.class,
							Object.class);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}


	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 * @param name          プロパティ名 (in_list)
	 * @param operatorStr   演算子
	 * @param value         値 (文字列)
	 * @throws IllegalSyntaxException 正しくないarg
	 */
	public InListProperty(ClientConfiguration configuration, String name, String operatorStr, Object value)
			throws IllegalSyntaxException {
		this.configuration = configuration;
		if ("in_list".equals(name) == false) {
			throw new AssertionError("[in_list] プロパティ名が不正です: " + name);
		}
		if (operatorStr == null || value == null) {
			throw new IllegalSyntaxException("[in_list] operatorおよびvalueは省略できません");
		}
		if (value instanceof String == false) {
			throw new IllegalSyntaxException("[in_list] valueは文字列であるべきです");
		}
		isEqual = FilterOperator.compileOperatorString(operatorStr) == FilterOperator.EQ;

		listIdentifier = (String) value;
		listFetcher = new UserFollewedByListFetcher(listIdentifier);
		listFetcher.run();
		configuration.getTimer().scheduleWithFixedDelay(new ListFetcherScheduler(), 60, 60,
				TimeUnit.MINUTES); // TODO from config
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return isEqual == ((Arrays.binarySearch(userIdsFollowedByList, directMessage.getSenderId()) >= 0) ? true : //
				(Arrays.binarySearch(userIdsFollowedByList, directMessage.getRecipientId()) >= 0));
	}

	@Override
	public boolean filter(Status status) {
		return isEqual == (Arrays.binarySearch(userIdsFollowedByList, status.getUser().getId()) >= 0);
	}
}
