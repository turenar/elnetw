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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
	private static final long SEC2MS = 1000;
	private static final long MIN2MS = SEC2MS * 60;
	private static final long HOUR2MS = MIN2MS * 60;
	private static final long DAY2MS = HOUR2MS * 24;

	private static byte[] decrypt(byte[] src, Key decryptKey) throws InvalidKeyException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			int ivLength = (src[0] & 0xff) + ((src[1] << 8) & 0xff00); // 無符号にするためにandを使っている
			byte[] iv = Arrays.copyOfRange(src, 2, 2 + ivLength);
			byte[] dat = Arrays.copyOfRange(src, 2 + ivLength, src.length);

			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("AES");
			algorithmParameters.init(iv);

			cipher.init(Cipher.DECRYPT_MODE, decryptKey, algorithmParameters);
			return cipher.doFinal(dat);
		} catch (IllegalBlockSizeException | InvalidAlgorithmParameterException | NoSuchPaddingException
				| NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			// BadPadding is because of InvalidKey
			throw new InvalidKeyException("passphrase seems to be illegal", e);
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
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException | IOException e) {
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
		listeners = new ArrayList<>();
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

	@Override
	public synchronized boolean equals(Object o) {
		if (!(o instanceof ClientProperties)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ClientProperties c = (ClientProperties) o;
		synchronized (c) {
			if (!listeners.equals(c.listeners)) {
				return false;
			}
			if (!storeFile.equals(c.storeFile)) {
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
	public synchronized void firePropertyChanged(String key, String oldValue, String newValue) {
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
		String property = getProperty(key, "").trim();
		if (property.isEmpty()) {
			return new String[0];
		} else {
			return property.split(" ");
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
		String value = getProperty(key);
		return Boolean.parseBoolean(value);
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
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		String[] rgba = value.split(",");
		if (rgba.length == 4) {
			return new Color(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]), Integer.parseInt(rgba[2]),
					Integer.parseInt(rgba[3]));
		} else if (rgba.length == 3) {
			return new Color(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]), Integer.parseInt(rgba[2]));
		} else {
			throw new IllegalArgumentException(MessageFormat.format("{0}はColorに使用できる値ではありません: {1}", key, value));
		}
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
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		String[] rgba = value.split(",");
		if (rgba.length == 2) {
			return new Dimension(Integer.parseInt(rgba[0]), Integer.parseInt(rgba[1]));
		} else {
			throw new IllegalArgumentException(MessageFormat.format("{0}はDimensionに使用できる値ではありません: {1}", key, value));
		}
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
		return Double.valueOf(getProperty(key));
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
		String value = getProperty(key);
		return Float.valueOf(value);
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
			switch (fontStyleString) {
				case "plain":
					fontStyle = Font.PLAIN;
					break;
				case "bold":
					fontStyle = Font.BOLD;
					break;
				case "italic":
					fontStyle = Font.ITALIC;
					break;
				case "bold|italic":
				case "italic|bold":
					fontStyle = Font.BOLD | Font.ITALIC;
					break;
				default:
					fontStyle = Integer.parseInt(fontStyleString);
					break;
			}
		}
		int fontSize = Integer.parseInt(fontSizeString.trim());

		return new Font(fontName, fontStyle, fontSize);
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
		String value = getProperty(key);
		return Integer.parseInt(value);
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
		String value = getProperty(key);
		return Long.parseLong(value);
	}

	/**
	 * keyに関連付けられた値を利用して、longを取得する。
	 *
	 * 書式：long
	 *
	 * @param key          キー
	 * @param defaultValue デフォルト値
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

	/**
	 * get milliseconds
	 *
	 * @param key property key
	 * @return milliseconds
	 */
	public synchronized long getTime(String key) {
		return getTime(key, TimeUnit.MILLISECONDS);
	}

	/**
	 * get time
	 *
	 * @param key    property key
	 * @param asUnit unit of time
	 * @return time converted by asUnit
	 */
	public synchronized long getTime(String key, TimeUnit asUnit) {
		String property = getProperty(key);
		char lastChar = property.charAt(property.length() - 1);
		TimeUnit unit = null;
		switch (lastChar) {
			case 's':
				unit = TimeUnit.SECONDS;
				break;
			case 'm':
				unit = TimeUnit.MINUTES;
				break;
			case 'h':
				unit = TimeUnit.HOURS;
				break;
			case 'd':
				unit = TimeUnit.DAYS;
				break;
			default:
				// will be integer...?
		}
		if (unit == null) {
			return asUnit.convert(Long.parseLong(property), TimeUnit.MILLISECONDS);
		} else {
			return asUnit.convert(Long.parseLong(property.substring(0, property.length() - 1)), unit);
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
		listeners = new ArrayList<>();
	}

	@Override
	public synchronized Object remove(Object key) {
		firePropertyChanged((String) key, getProperty((String) key), null);
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
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにColorを関連付ける。
	 *
	 * @param key   キー
	 * @param color Colorインスタンス。null不可。
	 */
	public synchronized void setColor(String key, Color color) {
		setProperty(
				key,
				MessageFormat.format("{0},{1},{2},{3}", color.getRed(), color.getGreen(), color.getBlue(),
						color.getAlpha())
		);
	}

	/**
	 * keyにDimensionを関連付ける。
	 *
	 * @param key       キー
	 * @param dimension Dimensionインスタンス。null不可。
	 */
	public synchronized void setDimension(String key, Dimension dimension) {
		setProperty(key, dimension.width + "," + dimension.height);
	}

	/**
	 * keyにdoubleを関連付ける。
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setDouble(String key, double value) {
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにfloatを関連付ける
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setFloat(String key, float value) {
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにFontを関連付ける
	 *
	 * @param key  キー
	 * @param font フォントインスタンス
	 */
	public synchronized void setFont(String key, Font font) {
		setProperty(key, font.getName() + ',' + font.getSize() + ',' + font.getStyle());
	}

	/**
	 * keyにintを関連付ける
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setInteger(String key, int value) {
		setProperty(key, String.valueOf(value));
	}

	/**
	 * keyにlongを関連付ける。
	 *
	 * @param key   キー
	 * @param value 値
	 */
	public synchronized void setLong(String key, long value) {
		setProperty(key, String.valueOf(value));
	}

	/**
	 * Set value.
	 * But actual value is obfuscated, so you should use {@link #getPrivateString(String, String)} etc.
	 *
	 * @param key        property key
	 * @param value      property value
	 * @param passphrase Passphrase
	 * @throws InvalidKeyException 暗号キーがおかしい
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
	 * @throws InvalidKeyException 暗号キーがおかしい
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
		firePropertyChanged(key, oldValue, newValue);
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

	/**
	 * set user-friendly property for time as millisecond
	 *
	 * @param key  key
	 * @param time time as milliseconds
	 */
	public void setTime(String key, long time) {
		setTime(key, time, TimeUnit.MILLISECONDS);
	}

	/**
	 * set user-friendly property for time
	 *
	 * @param key    property key
	 * @param time   time as asUnit
	 * @param asUnit unit of time
	 */
	public synchronized void setTime(String key, long time, TimeUnit asUnit) {
		long milliTime = TimeUnit.MILLISECONDS.convert(time, asUnit);
		String unit;
		long unitTime;
		if (milliTime % DAY2MS == 0) {
			unitTime = milliTime / DAY2MS;
			unit = "d";
		} else if (milliTime % HOUR2MS == 0) {
			unitTime = milliTime / HOUR2MS;
			unit = "h";
		} else if (milliTime % MIN2MS == 0) {
			unitTime = milliTime / MIN2MS;
			unit = "m";
		} else if (milliTime % SEC2MS == 0) {
			unitTime = milliTime / SEC2MS;
			unit = "s";
		} else {
			unitTime = milliTime;
			unit = "";
		}
		setProperty(key, unitTime + unit);
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
