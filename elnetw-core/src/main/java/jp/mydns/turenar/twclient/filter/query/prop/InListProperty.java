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

package jp.mydns.turenar.twclient.filter.query.prop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.query.QueryOperator;
import jp.mydns.turenar.twclient.filter.query.QueryProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.HttpResponseCode;
import twitter4j.PagableResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * リストに入っているかどかを用いてフィルタする
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InListProperty implements QueryProperty {
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
		protected long listId;
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
						if (e.getStatusCode() >= 500) { // CS-IGNORE
							life--;
							if (life <= 0) {
								throw new IllegalSyntaxException("[inlist] Could not retrieve ListInformation: @"
										+ listOwner + "/" + slug, e);
							}
						} else if (e.getStatusCode() == HttpResponseCode.NOT_FOUND) {
							throw new IllegalSyntaxException("[inlist] Not Found list: @" + listOwner + "/" + slug);
						}
					}
				}
			}
		}

		@Override
		public void run() {
			ArrayList<User> userListMembers = new ArrayList<>();
			long listId = this.listId;
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
						// continue; // with (prev) cursor
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
	/** 設定 */
	protected ClientConfiguration configuration;
	private boolean isEqual;
	/** (:&lt;listId&gt;|&lt;listName&gt;|@&lt;owner&gt;/&lt;listName&gt;) */
	protected String listIdentifier;
	/** リストでフォローされているユーザーIDの配列 (ソート済み) */
	protected long[] userIdsFollowedByList;
	/** リストフェッチャ */
	protected UserFollewedByListFetcher listFetcher;

	/**
	 * インスタンスを生成する。
	 *
	 * @param name        プロパティ名 (in_list)
	 * @param operatorStr 演算子
	 * @param value       値 (文字列)
	 * @throws IllegalSyntaxException 正しくないarg
	 */
	public InListProperty(String name, String operatorStr, Object value)
			throws IllegalSyntaxException {
		this.configuration = ClientConfiguration.getInstance();
		if (!"in_list".equals(name)) {
			throw new AssertionError("[in_list] プロパティ名が不正です: " + name);
		}
		if (operatorStr == null || value == null) {
			throw new IllegalSyntaxException("[in_list] operatorおよびvalueは省略できません");
		}
		if (!(value instanceof String)) {
			throw new IllegalSyntaxException("[in_list] valueは文字列であるべきです");
		}
		isEqual = QueryOperator.compileOperatorString(operatorStr) == QueryOperator.EQ;

		listIdentifier = (String) value;
		listFetcher = new UserFollewedByListFetcher(listIdentifier);
		listFetcher.run();
		configuration.getTimer().scheduleWithFixedDelay(new ListFetcherScheduler(), 60, 60,
				TimeUnit.MINUTES); // TODO from conf
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return isEqual == ((Arrays.binarySearch(userIdsFollowedByList, directMessage.getSenderId()) >= 0)
				|| (Arrays.binarySearch(userIdsFollowedByList, directMessage.getRecipientId()) >= 0));
	}

	@Override
	public boolean filter(Status status) {
		return isEqual == (Arrays.binarySearch(userIdsFollowedByList, status.getUser().getId()) >= 0);
	}

	@Override
	public void init() {
	}
}
