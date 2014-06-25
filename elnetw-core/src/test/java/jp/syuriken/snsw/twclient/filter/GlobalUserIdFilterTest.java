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

package jp.syuriken.snsw.twclient.filter;

import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import jp.syuriken.snsw.twclient.ClientProperties;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;

import static org.junit.Assert.*;

/**
 * UserFilterのためのTest
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class GlobalUserIdFilterTest extends MessageFilterAdapter {
	private static final String PROPERTY_FILTER_ID_NAME = "core.filter.user.ids";
	private ClientConfigurationTestImpl configuration;
	private GlobalUserIdFilter globalUserIdFilter;
	private ClientProperties properties;
	private int calledCount;
	private int expected;

	private void assertCalled() {
		assertEquals(++expected, calledCount);
	}

	private void assertNotCalled() {
		assertEquals(expected, calledCount);
	}

	@Override
	public void onDeletionNotice(long directMessageId, long userId) {
		calledCount++;
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		calledCount++;
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		calledCount++;
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		calledCount++;
	}

	@Override
	public void onFollow(User source, User followedUser) {
		calledCount++;
	}

	@Override
	public void onStatus(Status status) {
		calledCount++;
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		calledCount++;
	}

	// @After avoid findbugs warning
	public void tearDown() {
		configuration.clearGlobalInstance();
	}

	// @Before avoid findbugs warning
	public void tearUp() {
		configuration = new ClientConfigurationTestImpl();
		properties = new ClientProperties();
		properties.setProperty(PROPERTY_FILTER_ID_NAME, "1 2 3");
		configuration.setConfigProperties(properties);
		configuration.setGlobalInstance();
		globalUserIdFilter = new GlobalUserIdFilter();
		globalUserIdFilter.addChild(this);
	}

	/** 正しくないフィルタIDを指定した時のテスト・メソッド */
	@Test
	public void testIllegalFilterIds() {
		tearUp();
		try {
			globalUserIdFilter.onStatus(new TestStatus(0, null, -1));
			assertCalled();

			properties.setProperty(PROPERTY_FILTER_ID_NAME, "a 1");
			globalUserIdFilter = new GlobalUserIdFilter();
			globalUserIdFilter.addChild(this);
			globalUserIdFilter.onStatus(new TestStatus(0, null, -1));
			assertCalled();
			globalUserIdFilter.onStatus(new TestStatus(1, null, -1));
			assertNotCalled();
		} finally {
			tearDown();
		}
	}

	/** {@link GlobalUserIdFilter#onDeletionNotice(long, long)} のためのテスト・メソッド。 */
	@Test
	public void testOnDeletionNoticeLongLong() {
		tearUp();
		try {
			globalUserIdFilter.onDeletionNotice(0, 0);
			assertCalled();
			globalUserIdFilter.onDeletionNotice(0, 1);
			assertNotCalled();
		} finally {
			tearDown();
		}
	}

	/** {@link GlobalUserIdFilter#onDeletionNotice(twitter4j.StatusDeletionNotice)} のためのテスト・メソッド。 */
	@Test
	public void testOnDeletionNoticeStatusDeletionNotice() {
		tearUp();
		try {
			globalUserIdFilter.onDeletionNotice(new TestNotice(0));
			assertCalled();
			globalUserIdFilter.onDeletionNotice(new TestNotice(1));
			assertNotCalled();
		} finally {
			tearDown();
		}
	}

	/** {@link GlobalUserIdFilter#onDirectMessage(twitter4j.DirectMessage)} のためのテスト・メソッド。 */
	@Test
	public void testOnDirectMessage() {
		tearUp();
		try {
			globalUserIdFilter.onDirectMessage(new TestMessage(0, 0));
			assertCalled();
			globalUserIdFilter.onDirectMessage(new TestMessage(0, 1));
			assertNotCalled();
			globalUserIdFilter.onDirectMessage(new TestMessage(1, 0));
			assertNotCalled();
		} finally {
			tearDown();
		}
	}

	/** {@link GlobalUserIdFilter#onFavorite(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnFavorite() {
		tearUp();
		try {
			Status[] succStatuses = new Status[]{
					new TestStatus(0, null, -1),
					new TestStatus(0, new TestStatus(0, null, -1), -1)
			};
			for (Status status : succStatuses) {
				globalUserIdFilter.onFavorite(new TestUser(0), new TestUser(0), status);
				assertCalled();
				globalUserIdFilter.onFavorite(new TestUser(1), new TestUser(0), status);
				assertNotCalled();
				globalUserIdFilter.onFavorite(new TestUser(0), new TestUser(1), status);
				assertNotCalled();
			}
			Status[] failStatuses = new Status[]{
					new TestStatus(1, null, -1),
					new TestStatus(0, null, 1),
					new TestStatus(0, new TestStatus(1, null, -1), -1),
					new TestStatus(0, new TestStatus(0, null, 1), 0)
			};
			for (Status status : failStatuses) {
				globalUserIdFilter.onFavorite(new TestUser(0), new TestUser(0), status);
				assertNotCalled();
				globalUserIdFilter.onFavorite(new TestUser(1), new TestUser(0), status);
				assertNotCalled();
				globalUserIdFilter.onFavorite(new TestUser(0), new TestUser(1), status);
				assertNotCalled();
			}
		} finally {
			tearDown();
		}
	}

	/** {@link GlobalUserIdFilter#onFollow(twitter4j.User, twitter4j.User)} のためのテスト・メソッド。 */
	@Test
	public void testOnFollow() {
		tearUp();
		try {
			globalUserIdFilter.onFollow(new TestUser(0), new TestUser(0));
			assertCalled();
			globalUserIdFilter.onFollow(new TestUser(1), new TestUser(0));
			assertNotCalled();
			globalUserIdFilter.onFollow(new TestUser(0), new TestUser(1));
			assertNotCalled();
		} finally {
			tearDown();
		}
	}


	/** {@link GlobalUserIdFilter#onStatus(twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnStatus() {
		tearUp();
		try {
			globalUserIdFilter.onStatus(new TestStatus(0, null, -1));
			assertCalled();
			globalUserIdFilter.onStatus(new TestStatus(0, new TestStatus(0, null, -1), -1));
			assertCalled();
			globalUserIdFilter.onStatus(new TestStatus(0, null, 0));
			assertCalled();
			globalUserIdFilter.onStatus(new TestStatus(1, null, -1));
			assertNotCalled();
			globalUserIdFilter.onStatus(new TestStatus(0, new TestStatus(1, null, -1), -1));
			assertNotCalled();
			globalUserIdFilter.onStatus(new TestStatus(0, new TestStatus(0, null, 1), -1));
			assertNotCalled();
			globalUserIdFilter.onStatus(new TestStatus(0, new TestStatus(1, null, 1), -1));
			assertNotCalled();
			globalUserIdFilter.onStatus(new TestStatus(0, null, 1));
			assertNotCalled();
		} finally {
			tearDown();
		}
	}

	/** {@link GlobalUserIdFilter#onUnfavorite(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnUnfavorite() {
		tearUp();
		try {
			Status[] succStatuses = new Status[]{
					new TestStatus(0, null, -1),
					new TestStatus(0, new TestStatus(0, null, -1), -1)
			};
			for (Status status : succStatuses) {
				globalUserIdFilter.onUnfavorite(new TestUser(0), new TestUser(0), status);
				assertCalled();
				globalUserIdFilter.onUnfavorite(new TestUser(1), new TestUser(0), status);
				assertNotCalled();
				globalUserIdFilter.onUnfavorite(new TestUser(0), new TestUser(1), status);
				assertNotCalled();
			}
			Status[] failStatuses = new Status[]{
					new TestStatus(1, null, -1),
					new TestStatus(0, null, 1),
					new TestStatus(0, new TestStatus(1, null, -1), -1),
					new TestStatus(0, new TestStatus(0, null, 1), 0)
			};
			for (Status status : failStatuses) {
				globalUserIdFilter.onUnfavorite(new TestUser(0), new TestUser(0), status);
				assertNotCalled();
				globalUserIdFilter.onUnfavorite(new TestUser(1), new TestUser(0), status);
				assertNotCalled();
				globalUserIdFilter.onUnfavorite(new TestUser(0), new TestUser(1), status);
				assertNotCalled();
			}
		} finally {
			tearDown();
		}
	}
}
