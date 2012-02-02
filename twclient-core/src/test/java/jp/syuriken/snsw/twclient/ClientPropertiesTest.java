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
 * @author $Author$
 */
public class ClientPropertiesTest {
	
	private static final int BENCH_COUNT = 1000000;
	
	/* **********************
	 *== bench report: BENCH_COUNT=10^7
	 * == No cache result ===
	 *  getBoolean: 65ms
	 *  getColor: 25219ms
	 *  getDimension: 41ms
	 *  getInteger: 4ms
	 *  getLong: 95ms
	 * == cached result ===
	 *  getBoolean: 29ms
	 *  getColor: 1253ms
	 *  getDimension: 10ms
	 *  getInteger: 8ms
	 *  getLong: 9ms
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
		for (int i = 0; i < 10000; i++) {
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
		for (int i = 0; i < 10000; i++) {
			clientProperties.getDimension("aaa");
		}
		timerStop("getDimension");
		assertEquals(new Dimension(100, 100), clientProperties.getDimension("aaa"));
		assertEquals(new Dimension(0, 0), clientProperties.getDimension("bbb"));
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
		for (int i = 0; i < 10000; i++) {
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
		for (int i = 0; i < 10000; i++) {
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
