package jp.syuriken.snsw.twclient.internal;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Random;

import jp.syuriken.snsw.twclient.ClientConfigurationTestImpl;
import org.junit.Test;

import static jp.syuriken.snsw.twclient.internal.ConcurrentSoftHashMap.SoftReferenceUtil;
import static org.junit.Assert.*;

/**
 * Test for ConcurrentSoftHashMap
 */
public class ConcurrentSoftHashMapTest {

	private static class JobThrowAwayConfiguration extends ClientConfigurationTestImpl {
		@Override
		public void addJob(Runnable job) {
			// do nothing
		}

		@Override
		public void addJob(byte priority, Runnable job) {
			// do nothing
		}
	}

	public static final long TEST_VALUE = 1234567890L;
	public static final String TEST_KEY = "test";
	public static final long ILLEGAL_VALUE = 9876543210L;
	private static final JobThrowAwayConfiguration configuration = new JobThrowAwayConfiguration();

	@Test
	public void testRemove() throws Exception {
		configuration.setGlobalInstance();
		try {
			ConcurrentSoftHashMap<String, Object> map = new ConcurrentSoftHashMap<>();
			Long obj = TEST_VALUE;
			map.put(TEST_KEY, obj);
			assertEquals(TEST_VALUE, map.get(TEST_KEY));
			assertTrue(map.containsKey(TEST_KEY));
			assertFalse(map.remove(TEST_KEY, ILLEGAL_VALUE));
			assertTrue(map.containsKey(TEST_KEY));
			assertTrue(map.remove(TEST_KEY, TEST_VALUE));
			assertNull(map.get(TEST_KEY));
			assertFalse(map.containsKey(TEST_KEY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testReplace() throws Exception {
		configuration.setGlobalInstance();
		try {
			ConcurrentSoftHashMap<String, Object> map = new ConcurrentSoftHashMap<>();
			map.put(TEST_KEY, TEST_VALUE);
			assertEquals(TEST_VALUE, map.get(TEST_KEY));
			assertTrue(map.containsKey(TEST_KEY));
			assertFalse(map.replace(TEST_KEY, ILLEGAL_VALUE, 54321L));
			assertEquals(TEST_VALUE, map.get(TEST_KEY));
			assertTrue(map.replace(TEST_KEY, TEST_VALUE, 10123L));
			assertEquals(10123L, map.get(TEST_KEY));
			assertTrue(map.containsKey(TEST_KEY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testSoftReferenceImpl() throws Exception {
		String str = "terminal";
		String val = "chunk";
		SoftReferenceUtil<String, String> ref = new SoftReferenceUtil<>(str, val);
		assertEquals(str, ref.getKey());
		assertEquals(val.hashCode(), ref.hashCode());
		assertEquals(val, ref.get());
	}

	@Test
	public void testWeakGet() throws Exception {
		configuration.setGlobalInstance();
		try {
			ConcurrentSoftHashMap<String, Object> map = new ConcurrentSoftHashMap<>();
			WeakReference<Long> reference;
			{
				Long obj = TEST_VALUE;
				ReferenceQueue<Object> queue = new ReferenceQueue<>();
				reference = new WeakReference<>(obj, queue);
				map.put(TEST_KEY, obj);
			}
			while (!reference.isEnqueued()) {
				byte[] unused = new byte[0x1_000_000];
				Random random = new Random();
				map.put("unused" + random.nextLong(), unused);
			}
			assertNull(map.get(TEST_KEY));
			assertTrue(map.remove(TEST_KEY, ILLEGAL_VALUE));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testWeakReplace() throws Exception {
		configuration.setGlobalInstance();
		try {
			ConcurrentSoftHashMap<String, Object> map = new ConcurrentSoftHashMap<>();
			WeakReference<Long> reference;
			{
				Long obj = TEST_VALUE;
				ReferenceQueue<Object> queue = new ReferenceQueue<>();
				reference = new WeakReference<>(obj, queue);
				map.put(TEST_KEY, obj);
			}
			while (!reference.isEnqueued()) {
				byte[] unused = new byte[0x1_000_000];
				Random random = new Random();
				map.put("unused" + random.nextLong(), unused);
			}
			assertNull(map.get(TEST_KEY));
			assertTrue(map.replace(TEST_KEY, ILLEGAL_VALUE, 10123L));
			assertEquals(10123L, map.get(TEST_KEY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}
}
