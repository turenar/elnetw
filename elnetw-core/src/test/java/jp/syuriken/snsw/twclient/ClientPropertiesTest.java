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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ClientPropertiesのためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientPropertiesTest {

	/*package*/static final class PropertyChangeListenerTestImpl implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			assertEquals("test", evt.getPropertyName());
			assertEquals(null, evt.getOldValue());
			assertEquals("aaa", evt.getNewValue());
		}
	}

	private static final int BENCH_COUNT = 100000;

	/**
	 * obfuscate string
	 *
	 * @param args no effect
	 * @throws IOException         error
	 * @throws InvalidKeyException error
	 */
	public static void main(String[] args) throws IOException, InvalidKeyException {
		ClientProperties configProperties = new ClientProperties();

		Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name());
		String key = "/testtest/";
		System.out.print("value> ");
		String value = scanner.nextLine().trim();
		System.out.print("passphrase> ");
		String passphrase = scanner.nextLine().trim();
		configProperties.setPrivateString(key, value, passphrase);
		System.out.print("Obfuscated value: ");
		System.out.println(configProperties.getProperty(key));
	}

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

	/** {@link ClientProperties#addPropertyChangedListener(PropertyChangeListener)} のためのテスト・メソッド。 */
	@Test
	public void testAddPropertyChangedListener() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.addPropertyChangedListener(new PropertyChangeListenerTestImpl());
		clientProperties.setProperty("test", "aaa");
	}

	/** {@link ClientProperties#getBoolean(String)} のためのテスト・メソッド。 */
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

	/** {@link jp.syuriken.snsw.twclient.ClientProperties#getColor(java.lang.String)} のためのテスト・メソッド。 */
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

	/** {@link jp.syuriken.snsw.twclient.ClientProperties#getDimension(java.lang.String)} のためのテスト・メソッド。 */
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

	/** {@link ClientProperties#getDouble(String)} のためのテスト・メソッド。 */
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

	/** {@link ClientProperties#getFloat(String)} のためのテスト・メソッド。 */
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

	@Test
	public void testGetFont() {
		ClientProperties clientProperties = new ClientProperties();
		Font fontA = new Font(Font.MONOSPACED, Font.PLAIN, 10);
		clientProperties.setFont("aaa", fontA);
		Font fontB = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 11);
		clientProperties.setFont("bbb", fontB);
		clientProperties.setProperty("ccc", "Monospaced,10,plain");
		clientProperties.setProperty("ddd", "SansSerif,  11, bold|italic");
		clientProperties.setProperty("eee", "Monospaced,10,bold");
		clientProperties.setProperty("fff", "Monospaced,12,italic");
		clientProperties.setProperty("ggg", "Monospaced,13,italic|bold");
		clientProperties.setProperty("hhh", "Monospaced,14");

		timerStart();
		for (int i = 0; i < BENCH_COUNT; i++) {
			clientProperties.getFont("aaa");
		}
		timerStop("getFont");

		assertEquals(fontA, clientProperties.getFont("aaa"));
		assertEquals(fontB, clientProperties.getFont("bbb"));
		assertEquals(fontA, clientProperties.getFont("ccc"));
		assertEquals(fontB, clientProperties.getFont("ddd"));
		Font fontE = clientProperties.getFont("eee");
		assertEquals("Monospaced", fontE.getName());
		assertEquals(10, fontE.getSize());
		assertEquals(Font.BOLD, fontE.getStyle());
		Font fontF = clientProperties.getFont("fff");
		assertEquals("Monospaced", fontF.getName());
		assertEquals(12, fontF.getSize());
		assertEquals(Font.ITALIC, fontF.getStyle());
		Font fontG = clientProperties.getFont("ggg");
		assertEquals("Monospaced", fontG.getName());
		assertEquals(13, fontG.getSize());
		assertEquals(Font.ITALIC | Font.BOLD, fontG.getStyle());
		Font fontH = clientProperties.getFont("hhh");
		assertEquals("Monospaced", fontH.getName());
		assertEquals(14, fontH.getSize());
		assertEquals(Font.PLAIN, fontH.getStyle());
	}

	/** {@link jp.syuriken.snsw.twclient.ClientProperties#getInteger(java.lang.String)} のためのテスト・メソッド。 */
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

	/** {@link jp.syuriken.snsw.twclient.ClientProperties#getLong(java.lang.String)} のためのテスト・メソッド。 */
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

	/** {@link ClientProperties#getPrivateString(String, String)} */
	@Test
	public void testGetPrivateString() throws InvalidKeyException {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setPrivateString("aaa", "abcdefg", "0xcafebabe");
		clientProperties.setPrivateString("bbb", "hijklmn", "elnetw");
		clientProperties.setPrivateString("ccc", "opqrstu", ClientProperties.makeKey("cipher"));
		clientProperties.setProperty("fff", "vwxyz");

		int benchCount = BENCH_COUNT / 100;
		timerStart();
		for (int i = 0; i < benchCount; i++) {
			clientProperties.getPrivateString("aaa", "0xcafebabe");
		}
		timerStop("getPrivateString (without Key)", 100);

		Key keyCafebabe = ClientProperties.makeKey("0xcafebabe");
		timerStart();
		for (int i = 0; i < benchCount; i++) {
			clientProperties.getPrivateString("aaa", keyCafebabe);
		}
		timerStop("getPrivateString (with Key)", 100);

		assertEquals("abcdefg", clientProperties.getPrivateString("aaa", "0xcafebabe"));
		assertEquals("abcdefg", clientProperties.getPrivateString("aaa", keyCafebabe));
		assertEquals("hijklmn", clientProperties.getPrivateString("bbb", "elnetw"));
		assertEquals("hijklmn", clientProperties.getPrivateString("bbb", "test", "elnetw"));
		assertEquals("opqrstu", clientProperties.getPrivateString("ccc", "cipher"));
		assertNull(clientProperties.getPrivateString("ddd", "test"));
		assertEquals("terminal", clientProperties.getPrivateString("ddd", "terminal", "test"));
		assertEquals("vwxyz", clientProperties.getPrivateString("fff", "kill me baby"));
		assertEquals("vwxyz", clientProperties.getPrivateString("fff", "$priv$0", "kill me baby"));

		try { // illegal key
			clientProperties.getPrivateString("aaa", "Oxcafebaby");
			fail("InvalidKey");
		} catch (InvalidKeyException e) {
			// success
		}
	}

	@Test
	public void testGetTime() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setTime("ms", 1, TimeUnit.MILLISECONDS);
		clientProperties.setTime("sec", 2, TimeUnit.SECONDS);
		clientProperties.setTime("min", 3, TimeUnit.MINUTES);
		clientProperties.setTime("hr", 4, TimeUnit.HOURS);
		clientProperties.setTime("day", 5, TimeUnit.DAYS);
		clientProperties.setTime("extra", 1000 * 60 * 60 * 24 * 7, TimeUnit.MILLISECONDS);
		assertEquals(1, clientProperties.getTime("ms", TimeUnit.MILLISECONDS));
		assertEquals(1, clientProperties.getTime("ms"));
		assertEquals(TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS),
				clientProperties.getTime("sec", TimeUnit.MILLISECONDS));
		assertEquals(2, clientProperties.getTime("sec", TimeUnit.SECONDS));
		assertEquals(TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES),
				clientProperties.getTime("min", TimeUnit.MILLISECONDS));
		assertEquals(3, clientProperties.getTime("min", TimeUnit.MINUTES));
		assertEquals(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS),
				clientProperties.getTime("hr", TimeUnit.MILLISECONDS));
		assertEquals(4, clientProperties.getTime("hr", TimeUnit.HOURS));
		assertEquals(TimeUnit.MILLISECONDS.convert(5, TimeUnit.DAYS),
				clientProperties.getTime("day", TimeUnit.MILLISECONDS));
		assertEquals(5, clientProperties.getTime("day", TimeUnit.DAYS));
		assertEquals(7, clientProperties.getTime("extra", TimeUnit.DAYS));
		assertEquals(TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS),
				clientProperties.getTime("extra", TimeUnit.MILLISECONDS));
		assertEquals(TimeUnit.SECONDS.convert(7, TimeUnit.DAYS),
				clientProperties.getTime("extra", TimeUnit.SECONDS));
		assertEquals(TimeUnit.MINUTES.convert(7, TimeUnit.DAYS),
				clientProperties.getTime("extra", TimeUnit.MINUTES));
		assertEquals(TimeUnit.HOURS.convert(7, TimeUnit.DAYS),
				clientProperties.getTime("extra", TimeUnit.HOURS));
	}

	@Test
	public void testSetTime() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setTime("ms_no_unit", 1);
		clientProperties.setTime("ms", 1, TimeUnit.MILLISECONDS);
		clientProperties.setTime("sec", 2, TimeUnit.SECONDS);
		clientProperties.setTime("min", 3, TimeUnit.MINUTES);
		clientProperties.setTime("hr", 4, TimeUnit.HOURS);
		clientProperties.setTime("day", 5, TimeUnit.DAYS);
		clientProperties.setTime("extra", 1000 * 60 * 60 * 24 * 7, TimeUnit.MILLISECONDS);
		assertEquals("1", clientProperties.getProperty("ms_no_unit"));
		assertEquals("1", clientProperties.getProperty("ms"));
		assertEquals("2s", clientProperties.getProperty("sec"));
		assertEquals("3m", clientProperties.getProperty("min"));
		assertEquals("4h", clientProperties.getProperty("hr"));
		assertEquals("5d", clientProperties.getProperty("day"));
		assertEquals("7d", clientProperties.getProperty("extra"));

	}

	private void timerStart() {
		timerDate = System.currentTimeMillis();
	}

	private void timerStop(String messagePrefix, int scaler) {
		long processTime = (System.currentTimeMillis() - timerDate) * scaler;
		System.out.println(messagePrefix + ": " + processTime + "ms");
	}

	private void timerStop(String messagePrefix) {
		timerStop(messagePrefix, 1);
	}
}
