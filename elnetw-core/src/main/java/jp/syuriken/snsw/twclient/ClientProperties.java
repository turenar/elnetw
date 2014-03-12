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

package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登録済みのリスナに変更を通知できるプロパティーリストです。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientProperties extends Properties {

	private static final long serialVersionUID = -9065135476197360347L;
	private static final Logger logger = LoggerFactory.getLogger(ClientProperties.class);
	private static final int KEY_BIT = 128;
	private static final String ENCRYPT_HEADER = "$priv$0$";
	private static final String ENCRYPT_FOOTER = "$";

	private static byte[] decrypt(byte[] src, Key decryptKey) throws InvalidKeyException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			int ivLength = (src[0] & 0xff) + ((src[1] << 8) & 0xff00);
			byte[] iv = Arrays.copyOfRange(src, 2, 2 + ivLength);
			byte[] dat = Arrays.copyOfRange(src, 2 + ivLength, src.length);

			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("AES");
			algorithmParameters.init(iv);

			cipher.init(Cipher.DECRYPT_MODE, decryptKey, algorithmParameters);
			return cipher.doFinal(dat);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			// BadPadding is because of InvalidKey
			throw new InvalidKeyException("passphrase seems to be illegal", e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] encrypt(byte[] src, Key encryptKey) throws InvalidKeyException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, encryptKey);

			byte[] iv = cipher.getParameters().getEncoded();
			byte[] enc = cipher.doFinal(src);
			byte[] ret = new byte[2 + iv.length + enc.length];
			ret[0] = (byte) (iv.length & 0xff);
			ret[1] = (byte) ((iv.length >> 8) & 0xff);
			System.arraycopy(iv, 0, ret, 2, iv.length);
			System.arraycopy(enc, 0, ret, 2 + iv.length, enc.length);

			return ret;
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Make key for ciphering.
	 * This is for {@link #getPrivateString(String, java.security.Key)}
	 * or {@link #setPrivateString(String, String, java.security.Key)}
	 *
	 * @param passphrase Passphrase
	 * @return {@link Key} instance
	 */
	public static Key makeKey(String passphrase) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		messageDigest.update(passphrase.getBytes(ClientConfiguration.UTF8_CHARSET));
		byte[] digest = messageDigest.digest();
		byte[] keyBytes = Arrays.copyOf(digest, KEY_BIT / 8);
		return new SecretKeySpec(keyBytes, "AES");
	}

	/** リスナの配列 */
	protected transient ArrayList<PropertyChangeListener> listeners;
	/** 保存先のファイル */
	protected File storeFile;
	private transient Hashtable<String, Object> cacheTable;

	/** インスタンスを生成する。 */
	public ClientProperties() {
		this(null);
	}

	/**
	 * defaults を使用してClientPropertiesインスタンスを生成する。
	 *
	 * @param defaults デフォルトプロパティー
	 */
	public ClientProperties(Properties defaults) {
		super(defaults);
		listeners = new ArrayList<PropertyChangeListener>();
		cacheTable = new Hashtable<String, Object>();
	}

	/**
	 * PropertyChangedListnerを追加する
	 *
	 * @param listener リスナ。nullは不可
	 */
	public synchronized void addPropertyChangedListener(PropertyChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listenerはnullであってはいけません。");
		}
		listeners.add(listener);
	}

	/**
	 * 変換済みの値をキャッシュする。
	 *
	 * @param key   キー
	 * @param value 値
	 */
	protected synchronized void cacheValue(String key, Object value) {
		cacheTable.put(key, value);
	}

	/**
	 * キャッシュ済みの値を削除する。
	 *
	 * @param key キー
	 * @return キャッシュされていた変換済みの値。キャッシュされていなかった場合null
	 */
	protected synchronized Object clearCachedValue(String key) {
		return cacheTable.remove(key);
	}

	@Override
	public synchronized boolean equals(Object o) {
		if (o instanceof ClientProperties == false) {
			if (o instanceof Properties && listeners == null && storeFile == null) {
				return super.equals(o);
			} else {
				return false;
			}
		}
		if (super.equals(o) == false) {
			return false;
		}
		ClientProperties c = (ClientProperties) o;
		synchronized (c) {
			if (listeners.equals(c.listeners) == false) {
				return false;
			}
			if (storeFile.equals(c.storeFile) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * プロパティーが変更されたことを通知する。
	 *
	 * @param key      キー
	 * @param oldValue 古い値
	 * @param newValue 新しい値
	 */
	public synchronized void firePropetyChanged(String key, String oldValue, String newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent(this, key, oldValue, newValue);
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(evt);
		}
	}

	/**
	 * スペースで区切られた設定値を配列にして返す。この関数はキャッシュされません。
	 *
	 * @param key キー
	 * @return space-separated array
	 */
	public synchronized String[] getArray(String key) {
		String accountListString = getProperty(key, "").trim();
		if (accountListString.isEmpty()) {
			return new String[0];
		} else {
			return accountListString.split(" ");
		}
	}

	/**
	 * keyと関連付けられた値を利用して、boolean値を取得する。変換できない場合はfalseを返す。
	 *
	 * <p>書式：(true|false)</p>
	 *
	 * @param key キー
	 * @return boolean値
	 */
	public synchronized boolean getBoolean(String key) {
		Boolean boolean1 = getCachedValue(key, Boolean.class);
		if (boolean1 != null) {
			return boolean1;
		}

		String value = getProperty(key);
		boolean1 = Boolean.parseBoolean(value);
		cacheValue(key, boolean1);
		return boolean1;
	}

	/**
	 * キャッシュされた値を取得する。
	 *
	 * @param key           キー
	 * @param expectedClass 期待するClass
	 * @return キャッシュされていない、またはexpectedClassのインスタンスではない場合null。
	 * それ以外はキャッシュされた値
	 */
	@SuppressWarnings("unchecked")
	protected synchronized <T> T getCachedValue(String key, Class<T> expectedClass) {
		Object cachedValue = cacheTable.get(key);
		if (cachedValue == null || expectedClass.isInstance(cachedValue) == false) {
			return null;
		} else {
			return (T) cachedValue;
		}
	}

	/**
	 * keyと関連付けられた値を利用して、Colorインスタンスを作成する。
	 *
	 * <p>書式：int,int,int[,int]</p>
	 *
	 * @param key キー
	 * @return Colorインスタンス
	 * @throws IllegalArgumentException int,int,int[,int]の形ではありません
	 * @throws NumberFormatException    数値に変換できない値です
	 */
	public synchronized Color getColor(String key) throws IllegalArgumentException, NumberFormatException {
		Color color = getCachedValue(key, Color.class);
		if (color != null) {
			return color;
		}

		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		String[] rgba = value.split(",");
		if (rgba.length == 4) {
			color =
					new Color(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]), Integer.parseInt(rgba[2]),
							Integer.parseInt(rgba[3]));
		} else if (rgba.length == 3) {
			color = new Color(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]), Integer.parseInt(rgba[2]));
		} else {
			throw new IllegalArgumentException(MessageFormat.format("{0}はColorに使用できる値ではありません: {1}", key, value));
		}
		cacheValue(key, color);
		return color;
	}

	/**
	 * keyに関連付けられた値を利用して、Dimensionを取得する。
	 *
	 * 書式：int,int
	 *
	 * @param key キー
	 * @return keyに関連付けられたDimension
	 * @throws IllegalArgumentException 正しくない設定値
	 */
	public synchronized Dimension getDimension(String key) throws IllegalArgumentException {
		Dimension dimension = getCachedValue(key, Dimension.class);
		if (dimension != null) {
			return dimension;
		}

		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		String[] rgba = value.split(",");
		if (rgba.length == 2) {
			dimension = new Dimension(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]));
		} else {
			throw new IllegalArgumentException(MessageFormat.format("{0}はDimensionに使用できる値ではありません: {1}", key, value));
		}
		cacheValue(key, dimension);
		return dimension;
	}

	/**
	 * keyに関連付けられた値を利用して、doubleを取得する。
	 *
	 * 書式：double
	 *
	 * @param key キー
	 * @return keyに関連付けられたdouble
	 */
	public synchronized double getDouble(String key) {
		Double double1 = getCachedValue(key, Double.class);
		if (double1 != null) {
			return double1;
		}

		String value = getProperty(key);
		double1 = Double.valueOf(value);
		cacheValue(key, double1);
		return double1;
	}

	/**
	 * keyに関連付けられた値を利用して、floatを取得する。
	 *
	 * 書式：float
	 *
	 * @param key キー
	 * @return keyに関連付けられたfloat
	 */
	public synchronized float getFloat(String key) {
		Float float1 = getCachedValue(key, Float.class);
		if (float1 != null) {
			return float1;
		}

		String value = getProperty(key);
		float1 = Float.valueOf(value);
		cacheValue(key, float1);
		return float1;
	}

	/**
	 * keyに関連付けられた値を利用して、Fontインスタンスを取得する。
	 *
	 * <p>
	 * 書式：<i>String&lt;font-name&gt;</i>,<i>int&lt;font-size&gt;</i>[,<i>mixed&lt;font-style&gt;</i>]
	 * </p>
	 * <p>
	 * font-styleにはint値、または&quot;plain&quot;, &quot;italic&quot;, &quot;bold&quot;,
	 * &quot;bold|italic&quot;, &quot;italic|bold&quot;を指定してください。
	 * </p>
	 *
	 * @param key キー
	 * @return keyに関連付けられたfloat
	 * @throws IllegalArgumentException font-sizeが指定されていない。font-styleが指定されていない。
	 *                                  正しいint値が指定されていない。
	 */
	public synchronized Font getFont(String key) throws IllegalArgumentException {
		Font font = getCachedValue(key, Font.class);
		if (font != null) {
			return font;
		}

		String value = getProperty(key);
		if (value == null) {
			return null;
		}

		int indexOfFontNameSeparator = value.indexOf(',');
		if (indexOfFontNameSeparator == -1) {
			throw new IllegalArgumentException(
					MessageFormat.format("Font size is not specified: `{1}' ({0})", key, value));
		}
		String fontName = value.substring(0, indexOfFontNameSeparator).trim();

		int indexOfFontSizeSeparator = value.indexOf(',', indexOfFontNameSeparator + 1);
		String fontSizeString;
		int fontStyle;
		if (indexOfFontSizeSeparator == -1) {
			fontSizeString = value.substring(indexOfFontNameSeparator + 1);
			fontStyle = Font.PLAIN;
		} else {
			fontSizeString = value.substring(indexOfFontNameSeparator + 1, indexOfFontSizeSeparator);
			String fontStyleString = value.substring(indexOfFontSizeSeparator + 1).trim().toLowerCase();
			if (fontStyleString.equals("plain")) {
				fontStyle = Font.PLAIN;
			} else if (fontStyleString.equals("bold")) {
				fontStyle = Font.BOLD;
			} else if (fontStyleString.equals("italic")) {
				fontStyle = Font.ITALIC;
			} else if (fontStyleString.equals("bold|italic") || fontStyleString.equals("italic|bold")) {
				fontStyle = Font.BOLD | Font.ITALIC;
			} else {
				fontStyle = Integer.parseInt(fontStyleString);
			}
		}
		int fontSize = Integer.parseInt(fontSizeString.trim());

		font = new Font(fontName, fontStyle, fontSize);
		cacheValue(key, font);
		return font;
	}

	/**
	 * keyに関連付けられた値を利用して、intを取得する。
	 *
	 * 書式：int
	 *
	 * @param key キー
	 * @return keyに関連付けられたint
	 * @throws NumberFormatException 数値として認識できない値
	 */
	public synchronized int getInteger(String key) throws NumberFormatException {
		Integer integer = getCachedValue(key, Integer.class);
		if (integer != null) {
			return integer;
		}

		String value = getProperty(key);
		integer = Integer.valueOf(value);
		cacheValue(key, integer);
		return integer;
	}

	/**
	 * keyに関連付けられた値を利用して、intを取得する。
	 *
	 * 書式：int
	 *
	 * @param key          キー
	 * @param defaultValue デフォルト値
	 * @return keyに関連付けられたint
	 */
	public synchronized int getInteger(String key, int defaultValue) {
		try {
			return getInteger(key);
		} catch (NumberFormatException e) {
			logger.warn("#getInteger() failed with key `" + key + "'", e);
			return defaultValue;
		}
	}

	/**
	 * keyに関連付けられた値を利用して、longを取得する。
	 *
	 * 書式：long
	 *
	 * @param key キー
	 * @return keyに関連付けられたlong
	 * @throws NumberFormatException 数値として認識できない値
	 */
	public synchronized long getLong(String key) throws NumberFormatException {
		Long long1 = getCachedValue(key, Long.class);
		if (long1 != null) {
			return long1;
		}

		String value = getProperty(key);
		long1 = Long.valueOf(value);
		cacheValue(key, long1);
		return long1;
	}

	/**
	 * keyに関連付けられた値を利用して、longを取得する。
	 *
	 * 書式：long
	 *
	 * @param key キー
	 * @return keyに関連付けられたlong
	 */
	public synchronized long getLong(String key, long defaultValue) {
		try {
			return getLong(key);
		} catch (NumberFormatException e) {
			logger.warn("#getLong() failed with key `" + key + "'", e);
			return defaultValue;
		}
	}

	/**
	 * Get decrypted value
	 *
	 * @param key          property key
	 * @param defaultValue if value related linked with key is not found, return defaultValue
	 * @param passphrase   Passphrase.
	 * @return original value
	 * @throws InvalidKeyException passphrase seems to be wrong
	 * @throws RuntimeException    Exception occured while decrypting
	 */
	public synchronized String getPrivateString(String key, String defaultValue, String passphrase)
			throws InvalidKeyException, RuntimeException {
		String value = getPrivateString(key, passphrase);
		return value == null ? defaultValue : value;
	}

	/**
	 * Get decrypted value
	 *
	 * @param key          property key
	 * @param defaultValue if value related linked with key is not found, return defaultValue
	 * @param decryptKey   {@link Key} instance. use {@link #makeKey(String)}
	 * @return original value
	 * @throws InvalidKeyException passphrase seems to be wrong
	 * @throws RuntimeException    Exception occured while decrypting
	 */
	public synchronized String getPrivateString(String key, String defaultValue, Key decryptKey)
			throws InvalidKeyException {
		String value = getPrivateString(key, decryptKey);
		return value == null ? defaultValue : value;
	}

	/**
	 * Get decrypted value
	 *
	 * @param key        property key
	 * @param decryptKey {@link Key} instance. use {@link #makeKey(String)}
	 * @return original value
	 * @throws InvalidKeyException passphrase seems to be wrong
	 * @throws RuntimeException    Exception occured while decrypting
	 */
	public synchronized String getPrivateString(String key, Key decryptKey) throws InvalidKeyException {
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		if (value.startsWith(ENCRYPT_HEADER) && value.endsWith(ENCRYPT_FOOTER)) {
			value = value.substring(ENCRYPT_HEADER.length(), value.length() - ENCRYPT_FOOTER.length());
			try {
				byte[] decrypted = decrypt(Base64.decode(value), decryptKey);
				return new String(decrypted, ClientConfiguration.UTF8_CHARSET);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return value;
		}
	}

	/**
	 * Get decrypted value
	 *
	 * @param key        property key
	 * @param passphrase Passphrase.
	 * @return original value
	 * @throws InvalidKeyException passphrase seems to be wrong
	 * @throws RuntimeException    Exception occured while decrypting
	 */
	public synchronized String getPrivateString(String key, String passphrase) throws InvalidKeyException {
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		if (value.startsWith(ENCRYPT_HEADER) && value.endsWith(ENCRYPT_FOOTER)) {
			value = value.substring(ENCRYPT_HEADER.length(), value.length() - ENCRYPT_FOOTER.length());
			Key decryptKey = makeKey(passphrase);
			try {
				byte[] decrypted = decrypt(Base64.decode(value), decryptKey);
				return new String(decrypted, ClientConfiguration.UTF8_CHARSET);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return value;
		}
	}

	@Override
	public synchronized int hashCode() {
		int hashCode = super.hashCode();
		hashCode += 19 * listeners.hashCode();
		hashCode += 19 * storeFile.hashCode();
		return hashCode;
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		listeners = new ArrayList<PropertyChangeListener>();
		cacheTable = new Hashtable<String, Object>();
	}

	@Override
	public synchronized Object remove(Object key) {
		firePropetyChanged((String) key, getProperty((String) key), null);
		return super.remove(key);
	}

	/**
	 * 登録済みの {@link PropertyChangeListener}を削除する
	 *
	 * @param listener リスナ
	 * @return 登録されて削除された場合true
	 */
	public synchronized boolean removePropertyChangedListener(PropertyChangeListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * keyにbooleanを関連付ける。
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setBoolean(String key, boolean value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにColorを関連付ける。
	 *
	 * @param key   キー
	 * @param color Colorインスタンス。null不可。
	 */
	public synchronized void setColor(String key, Color color) {
		clearCachedValue(key);
		setProperty(
				key,
				MessageFormat.format("{0},{1},{2},{3}", color.getRed(), color.getGreen(), color.getBlue(),
						color.getAlpha()));
	}

	/**
	 * keyにDimensionを関連付ける。
	 *
	 * @param key       キー
	 * @param dimension Dimensionインスタンス。null不可。
	 */
	public synchronized void setDimension(String key, Dimension dimension) {
		clearCachedValue(key);
		setProperty(key, dimension.width + "," + dimension.height);
	}

	/**
	 * keyにdoubleを関連付ける。
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setDouble(String key, double value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにfloatを関連付ける
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setFloat(String key, float value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにFontを関連付ける
	 *
	 * @param key  キー
	 * @param font フォントインスタンス
	 */
	public synchronized void setFont(String key, Font font) throws IllegalArgumentException {
		clearCachedValue(key);
		StringBuilder property = new StringBuilder(font.getName()).append(',')
				.append(font.getSize()).append(',').append(font.getStyle());
		setProperty(key, property.toString());
	}

	/**
	 * keyにintを関連付ける
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setInteger(String key, int value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにlongを関連付ける。
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setLong(String key, long value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}

	/**
	 * Set value.
	 * But actual value is obfuscated, so you should use {@link #getPrivateString(String, String)} etc.
	 *
	 * @param key        property key
	 * @param value      property value
	 * @param passphrase Passphrase
	 * @throws InvalidKeyException
	 */
	public synchronized void setPrivateString(String key, String value, String passphrase) throws InvalidKeyException {
		setPrivateString(key, value, makeKey(passphrase));
	}

	/**
	 * Set value.
	 * But actual value is obfuscated, so you should use {@link #getPrivateString(String, Key)} etc.
	 *
	 * @param key        property key
	 * @param value      property value
	 * @param encryptKey Key for encryption. Use {@link #makeKey(String)}.
	 * @throws InvalidKeyException
	 */
	public synchronized void setPrivateString(String key, String value, Key encryptKey) throws InvalidKeyException {
		byte[] bytes = value.getBytes(ClientConfiguration.UTF8_CHARSET);
		String encoded = Base64.encodeBytes(encrypt(bytes, encryptKey));
		setProperty(key, ENCRYPT_HEADER + encoded + ENCRYPT_FOOTER);
	}

	/**
	 * <p>プロパティを設定して、登録済みのリスナに変更を通知します。</p>
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Object setProperty(String key, String newValue) {
		String oldValue = getProperty(key);
		firePropetyChanged(key, oldValue, newValue);
		return super.setProperty(key, newValue);
	}

	/**
	 * デフォルトの保存先のファイルを設定する。
	 *
	 * @param storeFile デフォルトの保存先のファイル
	 */
	public synchronized void setStoreFile(File storeFile) {
		this.storeFile = storeFile;
	}

	/** ファイルに保存する。 */
	public void store() {
		store("Auto generated by jp.syuriken.snsw.twclient.ClientProperties");
	}

	/**
	 * ファイルに保存する
	 *
	 * @param comments ファイルのコメント
	 */
	public synchronized void store(String comments) {
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		try {
			stream = new FileOutputStream(storeFile);
			writer = new OutputStreamWriter(stream, "UTF-8");
			store(writer, comments);
		} catch (IOException e) {
			logger.warn("Propertiesファイルの保存中にエラー", e);
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					logger.warn("Propertiesファイルのクローズ中にエラー", e);
				}
			} else if (stream != null) { // writer.close()によりstreamは自動的に閉じられる
				try {
					stream.flush();
					stream.close();
				} catch (IOException e) {
					logger.warn("Propertiesファイルのクローズ中にエラー", e);
				}
			}
		}
	}
}
