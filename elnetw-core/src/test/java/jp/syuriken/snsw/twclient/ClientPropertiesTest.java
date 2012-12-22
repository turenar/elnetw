package jp.syuriken.snsw.twclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Test;

/**
 * ClientPropertiesのためのテスト
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class ClientPropertiesTest {

	private static final int BENCH_COUNT = 100000;

	/* **********************
	 *== bench report: BENCH_COUNT=10^7
	 * == No cache result ===
	 *  getBoolean: 1451ms
	 *  getColor: 14640ms
	 *  getDimension: 10878ms
	 *  getInteger: 2196ms
	 *  getLong: 3388ms
	 * == cached result ===
	 *  getBoolean: 1318ms
	 *  getColor: 1327ms
	 *  getDimension: 1135ms
	 *  getInteger: 625ms
	 *  getLong: 627ms
	 * ***********************/
	private long timerDate;


	/**
	 * {@link ClientProperties#addPropertyChangedListener(PropertyChangeListener)} のためのテスト・メソッド。
	 */
	@Test
	public void testAddPropertyChangedListener() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.addPropertyChangedListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				assertEquals("test", evt.getPropertyName());
				assertEquals(null, evt.getOldValue());
				assertEquals("aaa", evt.getNewValue());
			}
		});
		clientProperties.setProperty("test", "aaa");
	}

	/**
	 * {@link ClientProperties#getBoolean(String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetBoolean() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setBoolean("aaa", true);
		clientProperties.setBoolean("bbb", false);

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getBoolean("aaa");
		}
		timerStop("getBoolean");
		assertTrue(clientProperties.getBoolean("aaa"));
		assertFalse(clientProperties.getBoolean("bbb"));
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.ClientProperties#getColor(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetColor() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setColor("aaa", Color.black);
		clientProperties.setColor("bbb", Color.white);

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getColor("aaa");
		}
		timerStop("getColor");
		assertEquals(Color.black, clientProperties.getColor("aaa"));
		assertEquals(Color.white, clientProperties.getColor("bbb"));
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.ClientProperties#getDimension(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetDimension() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setDimension("aaa", new Dimension(100, 100));
		clientProperties.setDimension("bbb", new Dimension(0, 0));

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getDimension("aaa");
		}
		timerStop("getDimension");
		assertEquals(new Dimension(100, 100), clientProperties.getDimension("aaa"));
		assertEquals(new Dimension(0, 0), clientProperties.getDimension("bbb"));
	}

	/**
	 * {@link ClientProperties#getDouble(String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetDouble() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setDouble("aaa", Double.MAX_VALUE);
		clientProperties.setDouble("bbb", Double.MIN_NORMAL);
		clientProperties.setDouble("ccc", Double.MIN_VALUE);
		clientProperties.setDouble("ddd", Double.NaN);
		clientProperties.setDouble("eee", Double.NEGATIVE_INFINITY);
		clientProperties.setDouble("fff", Double.POSITIVE_INFINITY);

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getDouble("aaa");
		}
		timerStop("getDouble");
		assertEquals(Double.MAX_VALUE, clientProperties.getDouble("aaa"), 0.0001);
		assertEquals(Double.MIN_NORMAL, clientProperties.getDouble("bbb"), 0.0001);
		assertEquals(Double.MIN_VALUE, clientProperties.getDouble("ccc"), 0.0001);
		assertEquals(Double.NaN, clientProperties.getDouble("ddd"), 0.0001);
		assertEquals(Double.NEGATIVE_INFINITY, clientProperties.getDouble("eee"), 0.0001);
		assertEquals(Double.POSITIVE_INFINITY, clientProperties.getFloat("fff"), 0.0001);
	}

	/**
	 * {@link ClientProperties#getFloat(String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetFloat() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setFloat("aaa", Float.MAX_VALUE);
		clientProperties.setFloat("bbb", Float.MIN_NORMAL);
		clientProperties.setFloat("ccc", Float.MIN_VALUE);
		clientProperties.setFloat("ddd", Float.NaN);
		clientProperties.setFloat("eee", Float.NEGATIVE_INFINITY);
		clientProperties.setFloat("fff", Float.POSITIVE_INFINITY);

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getFloat("aaa");
		}
		timerStop("getFloat");
		assertEquals(Float.MAX_VALUE, clientProperties.getFloat("aaa"), 0.0001);
		assertEquals(Float.MIN_NORMAL, clientProperties.getFloat("bbb"), 0.0001);
		assertEquals(Float.MIN_VALUE, clientProperties.getFloat("ccc"), 0.0001);
		assertEquals(Float.NaN, clientProperties.getFloat("ddd"), 0.0001);
		assertEquals(Float.NEGATIVE_INFINITY, clientProperties.getFloat("eee"), 0.0001);
		assertEquals(Float.POSITIVE_INFINITY, clientProperties.getFloat("fff"), 0.0001);
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.ClientProperties#getInteger(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetInteger() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setInteger("aaa", Integer.MAX_VALUE);
		clientProperties.setInteger("bbb", Integer.MIN_VALUE);

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getInteger("aaa");
		}
		timerStop("getInteger");
		assertEquals(Integer.MAX_VALUE, clientProperties.getInteger("aaa"));
		assertEquals(Integer.MIN_VALUE, clientProperties.getInteger("bbb"));
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.ClientProperties#getLong(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	public void testGetLong() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setLong("aaa", Long.MAX_VALUE);
		clientProperties.setLong("bbb", Long.MIN_VALUE);

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getLong("aaa");
		}
		timerStop("getLong");
		assertEquals(Long.MAX_VALUE, clientProperties.getLong("aaa"));
		assertEquals(Long.MIN_VALUE, clientProperties.getLong("bbb"));
	}

	private void timerStart() {
		timerDate = System.currentTimeMillis();
	}

	private void timerStop(String messagePrefix) {
		long processTime = System.currentTimeMillis() - timerDate;
		System.out.println(messagePrefix + ": " + processTime + "ms");
	}

}