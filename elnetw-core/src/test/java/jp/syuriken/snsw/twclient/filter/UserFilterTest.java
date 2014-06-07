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
import twitter4j.Status;

import static jp.syuriken.snsw.twclient.filter.UserFilter.PROPERTY_KEY_FILTER_GLOBAL_QUERY;
import static org.junit.Assert.*;

/**
 * UserFilterのためのTest
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserFilterTest {
	private static final String PROPERTY_FILTER_ID_NAME = "core.filter.user.ids";
	private ClientConfigurationTestImpl configuration;
	private UserFilter userFilter;
	private ClientProperties properties;

	// @After avoid findbugs warning
	public void tearDown() {
		configuration.clearGlobalInstance();
	}

	// @Before avoid findbugs warning
	public void tearUp() {
		configuration = new ClientConfigurationTestImpl();
		properties = new ClientProperties();
		properties.setProperty(PROPERTY_FILTER_ID_NAME, "1 2 3");
		properties.setProperty("core.filter.queries", "");
		configuration.setConfigProperties(properties);
		configuration.setGlobalInstance();
		userFilter = new UserFilter(PROPERTY_KEY_FILTER_GLOBAL_QUERY);
	}

	/** 正しくないフィルタIDを指定した時のテスト・メソッド */
	@Test
	public void testIllegalFilterIds() {
		tearUp();
		try {
			UserFilter userFilter = new UserFilter(PROPERTY_KEY_FILTER_GLOBAL_QUERY);
			assertNotNull(userFilter.onStatus(new TestStatus(0, null, -1)));

			properties.setProperty(PROPERTY_FILTER_ID_NAME, "a 1");
			userFilter = new UserFilter(PROPERTY_KEY_FILTER_GLOBAL_QUERY);
			assertNotNull(userFilter.onStatus(new TestStatus(0, null, -1)));
			assertNull(userFilter.onStatus(new TestStatus(1, null, -1)));
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onDeletionNotice(long, long)} のためのテスト・メソッド。 */
	@Test
	public void testOnDeletionNoticeLongLong() {
		tearUp();
		try {
			assertFalse(userFilter.onDeletionNotice(0, 0));
			assertTrue(userFilter.onDeletionNotice(0, 1));
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onDeletionNotice(twitter4j.StatusDeletionNotice)} のためのテスト・メソッド。 */
	@Test
	public void testOnDeletionNoticeStatusDeletionNotice() {
		tearUp();
		try {
			assertFalse(userFilter.onDeletionNotice(new TestNotice(0)));
			assertTrue(userFilter.onDeletionNotice(new TestNotice(1)));
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onDirectMessage(twitter4j.DirectMessage)} のためのテスト・メソッド。 */
	@Test
	public void testOnDirectMessage() {
		tearUp();
		try {
			assertNotNull(userFilter.onDirectMessage(new TestMessage(0, 0)));
			assertNull(userFilter.onDirectMessage(new TestMessage(0, 1)));
			assertNull(userFilter.onDirectMessage(new TestMessage(1, 0)));
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onFavorite(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnFavorite() {
		tearUp();
		try {
			Status[] succStatuses = new Status[]{
					new TestStatus(0, null, -1),
					new TestStatus(0, new TestStatus(0, null, -1), -1)
			};
			for (Status status : succStatuses) {
				assertFalse(userFilter.onFavorite(new TestUser(0), new TestUser(0), status));
				assertTrue(userFilter.onFavorite(new TestUser(1), new TestUser(0), status));
				assertTrue(userFilter.onFavorite(new TestUser(0), new TestUser(1), status));
			}
			Status[] failStatuses = new Status[]{
					new TestStatus(1, null, -1),
					new TestStatus(0, null, 1),
					new TestStatus(0, new TestStatus(1, null, -1), -1),
					new TestStatus(0, new TestStatus(0, null, 1), 0)
			};
			for (Status status : failStatuses) {
				assertTrue(userFilter.onFavorite(new TestUser(0), new TestUser(0), status));
				assertTrue(userFilter.onFavorite(new TestUser(1), new TestUser(0), status));
				assertTrue(userFilter.onFavorite(new TestUser(0), new TestUser(1), status));
			}
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onFollow(twitter4j.User, twitter4j.User)} のためのテスト・メソッド。 */
	@Test
	public void testOnFollow() {
		tearUp();
		try {
			assertFalse(userFilter.onFollow(new TestUser(0), new TestUser(0)));
			assertTrue(userFilter.onFollow(new TestUser(1), new TestUser(0)));
			assertTrue(userFilter.onFollow(new TestUser(0), new TestUser(1)));
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onRetweet(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnRetweet() {
		tearUp();
		try {
			Status[] succStatuses = new Status[]{
					new TestStatus(0, null, -1),
					new TestStatus(0, new TestStatus(0, null, -1), -1)
			};
			for (Status status : succStatuses) {
				assertFalse(userFilter.onRetweet(new TestUser(0), new TestUser(0), status));
				assertTrue(userFilter.onRetweet(new TestUser(1), new TestUser(0), status));
				assertTrue(userFilter.onRetweet(new TestUser(0), new TestUser(1), status));
			}
			Status[] failStatuses = new Status[]{
					new TestStatus(1, null, -1),
					new TestStatus(0, null, 1),
					new TestStatus(0, new TestStatus(1, null, -1), -1),
					new TestStatus(0, new TestStatus(0, null, 1), 0)
			};
			for (Status status : failStatuses) {
				assertTrue(userFilter.onRetweet(new TestUser(0), new TestUser(0), status));
				assertTrue(userFilter.onRetweet(new TestUser(1), new TestUser(0), status));
				assertTrue(userFilter.onRetweet(new TestUser(0), new TestUser(1), status));
			}
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onStatus(twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnStatus() {
		tearUp();
		try {
			UserFilter userFilter = new UserFilter(PROPERTY_KEY_FILTER_GLOBAL_QUERY);
			assertNotNull(userFilter.onStatus(new TestStatus(0, null, -1)));
			assertNotNull(userFilter.onStatus(new TestStatus(0, new TestStatus(0, null, -1), -1)));
			assertNotNull(userFilter.onStatus(new TestStatus(0, null, 0)));
			assertNull(userFilter.onStatus(new TestStatus(1, null, -1)));
			assertNull(userFilter.onStatus(new TestStatus(0, new TestStatus(1, null, -1), -1)));
			assertNull(userFilter.onStatus(new TestStatus(0, new TestStatus(0, null, 1), -1)));
			assertNull(userFilter.onStatus(new TestStatus(0, new TestStatus(1, null, 1), -1)));
			assertNull(userFilter.onStatus(new TestStatus(0, null, 1)));
		} finally {
			tearDown();
		}
	}

	/** {@link UserFilter#onUnfavorite(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。 */
	@Test
	public void testOnUnfavorite() {
		tearUp();
		try {
			Status[] succStatuses = new Status[]{
					new TestStatus(0, null, -1),
					new TestStatus(0, new TestStatus(0, null, -1), -1)
			};
			for (Status status : succStatuses) {
				assertFalse(userFilter.onUnfavorite(new TestUser(0), new TestUser(0), status));
				assertTrue(userFilter.onUnfavorite(new TestUser(1), new TestUser(0), status));
				assertTrue(userFilter.onUnfavorite(new TestUser(0), new TestUser(1), status));
			}
			Status[] failStatuses = new Status[]{
					new TestStatus(1, null, -1),
					new TestStatus(0, null, 1),
					new TestStatus(0, new TestStatus(1, null, -1), -1),
					new TestStatus(0, new TestStatus(0, null, 1), 0)
			};
			for (Status status : failStatuses) {
				assertTrue(userFilter.onUnfavorite(new TestUser(0), new TestUser(0), status));
				assertTrue(userFilter.onUnfavorite(new TestUser(1), new TestUser(0), status));
				assertTrue(userFilter.onUnfavorite(new TestUser(0), new TestUser(1), status));
			}
		} finally {
			tearDown();
		}
	}
}
