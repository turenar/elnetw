package jp.syuriken.snsw.twclient.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;

import org.junit.BeforeClass;
import org.junit.Test;

import twitter4j.Status;

/**
 * UserFilterのためのTest
 * 
 * @author $Author$
 */
public class UserFilterTest {
	
	private static final class MyClientConfiguration extends ClientConfiguration {
		
		/*package*/MyClientConfiguration() {
			super(true);
		}
	}
	
	
	private static final String PROPERTY_FILTER_ID_NAME = "core.filter.user.ids";
	
	private static ClientConfiguration configuration;
	
	private static UserFilter userFilter;
	
	
	/**
	 * テスト前の準備
	 */
	@BeforeClass
	public static void tearUpClass() {
		configuration = new MyClientConfiguration();
		ClientProperties properties = new ClientProperties();
		properties.setProperty(PROPERTY_FILTER_ID_NAME, "1 2 3");
		configuration.setConfigProperties(properties);
		userFilter = new UserFilter(configuration);
	}
	
	/**
	 * 正しくないフィルタIDを指定した時のテスト・メソッド
	 */
	@Test
	public void testIllegalFilterIds() {
		ClientConfiguration configuration = new MyClientConfiguration();
		ClientProperties properties = new ClientProperties();
		configuration.setConfigProperties(properties);
		
		UserFilter userFilter = new UserFilter(configuration);
		assertNotNull(userFilter.onStatus(new TestStatus(0, null, -1)));
		
		properties.setProperty(PROPERTY_FILTER_ID_NAME, "a 1");
		userFilter = new UserFilter(configuration);
		assertNotNull(userFilter.onStatus(new TestStatus(0, null, -1)));
		assertNull(userFilter.onStatus(new TestStatus(1, null, -1)));
	}
	
	/**
	 * {@link UserFilter#onDeletionNotice(long, long)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnDeletionNoticeLongLong() {
		assertFalse(userFilter.onDeletionNotice(0, 0));
		assertTrue(userFilter.onDeletionNotice(0, 1));
	}
	
	/**
	 * {@link UserFilter#onDeletionNotice(twitter4j.StatusDeletionNotice)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnDeletionNoticeStatusDeletionNotice() {
		assertNotNull(userFilter.onDeletionNotice(new TestNotice(0)));
		assertNull(userFilter.onDeletionNotice(new TestNotice(1)));
	}
	
	/**
	 * {@link UserFilter#onDirectMessage(twitter4j.DirectMessage)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnDirectMessage() {
		assertNotNull(userFilter.onDirectMessage(new TestMessage(0, 0)));
		assertNull(userFilter.onDirectMessage(new TestMessage(0, 1)));
		assertNull(userFilter.onDirectMessage(new TestMessage(1, 0)));
	}
	
	/**
	 * {@link UserFilter#onFavorite(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnFavorite() {
		Status[] succStatuses = new Status[] {
			new TestStatus(0, null, -1),
			new TestStatus(0, new TestStatus(0, null, -1), -1)
		};
		for (Status status : succStatuses) {
			assertFalse(userFilter.onFavorite(new TestUser(0), new TestUser(0), status));
			assertTrue(userFilter.onFavorite(new TestUser(1), new TestUser(0), status));
			assertTrue(userFilter.onFavorite(new TestUser(0), new TestUser(1), status));
		}
		Status[] failStatuses = new Status[] {
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
	}
	
	/**
	 * {@link UserFilter#onFollow(twitter4j.User, twitter4j.User)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnFollow() {
		assertFalse(userFilter.onFollow(new TestUser(0), new TestUser(0)));
		assertTrue(userFilter.onFollow(new TestUser(1), new TestUser(0)));
		assertTrue(userFilter.onFollow(new TestUser(0), new TestUser(1)));
	}
	
	/**
	 * {@link UserFilter#onRetweet(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnRetweet() {
		Status[] succStatuses = new Status[] {
			new TestStatus(0, null, -1),
			new TestStatus(0, new TestStatus(0, null, -1), -1)
		};
		for (Status status : succStatuses) {
			assertFalse(userFilter.onRetweet(new TestUser(0), new TestUser(0), status));
			assertTrue(userFilter.onRetweet(new TestUser(1), new TestUser(0), status));
			assertTrue(userFilter.onRetweet(new TestUser(0), new TestUser(1), status));
		}
		Status[] failStatuses = new Status[] {
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
	}
	
	/**
	 * {@link UserFilter#onStatus(twitter4j.Status)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnStatus() {
		UserFilter userFilter = new UserFilter(configuration);
		assertNotNull(userFilter.onStatus(new TestStatus(0, null, -1)));
		assertNotNull(userFilter.onStatus(new TestStatus(0, new TestStatus(0, null, -1), -1)));
		assertNotNull(userFilter.onStatus(new TestStatus(0, null, 0)));
		assertNull(userFilter.onStatus(new TestStatus(1, null, -1)));
		assertNull(userFilter.onStatus(new TestStatus(0, new TestStatus(1, null, -1), -1)));
		assertNull(userFilter.onStatus(new TestStatus(0, new TestStatus(0, null, 1), -1)));
		assertNull(userFilter.onStatus(new TestStatus(0, new TestStatus(1, null, 1), -1)));
		assertNull(userFilter.onStatus(new TestStatus(0, null, 1)));
	}
	
	/**
	 * {@link UserFilter#onUnfavorite(twitter4j.User, twitter4j.User, twitter4j.Status)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnUnfavorite() {
		Status[] succStatuses = new Status[] {
			new TestStatus(0, null, -1),
			new TestStatus(0, new TestStatus(0, null, -1), -1)
		};
		for (Status status : succStatuses) {
			assertFalse(userFilter.onUnfavorite(new TestUser(0), new TestUser(0), status));
			assertTrue(userFilter.onUnfavorite(new TestUser(1), new TestUser(0), status));
			assertTrue(userFilter.onUnfavorite(new TestUser(0), new TestUser(1), status));
		}
		Status[] failStatuses = new Status[] {
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
	}
	
}