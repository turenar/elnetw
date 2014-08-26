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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.iharder.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.syuriken.snsw.twclient.ClientConfiguration.UTF8_CHARSET;


/**
 * 登録済みのリスナに変更を通知できるプロパティーリストです。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClientProperties implements Map<String, String> {
	/**
	 * Read in a "logical line" from an InputStream/Reader, skip all comment
	 * and blank lines and filter out those leading whitespace characters
	 * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
	 * Method returns the char length of the "logical line" and stores
	 * the line in "lineBuf".
	 */
	/*package*/ class LineReader {
		protected static final int IN_CHAR_BUF_SIZE = 8192;
		protected static final int LINE_BUF_SIZE = 1024;
		protected char[] lineBuf = new char[LINE_BUF_SIZE];
		protected final Reader reader;
		protected char[] inCharBuf;
		protected int inLimit = 0;
		protected int inOff = 0;

		/**
		 * make instance
		 *
		 * @param reader reader
		 */
		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[IN_CHAR_BUF_SIZE];
		}

		/**
		 * read line from reader
		 *
		 * @return read bytes
		 * @throws IOException exception occurred
		 */
		public int readLine() throws IOException {
			int len = 0;
			char c;

			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = reader.read(inCharBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						return len;
					}
				}
				c = inCharBuf[inOff++];
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					//flip the preceding backslash flag
					precedingBackslash = c == '\\' && !precedingBackslash;
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = reader.read(inCharBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						//skip the leading whitespace characters in following line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}

	private class PropWrappedList extends AbstractList<String> implements PropertyChangeListener {
		private final String key;
		private int len;

		public PropWrappedList(String key) {
			this.key = key;
			addPropertyChangedListener(this);
			updateLen();
		}

		@Override
		public boolean add(String s) {
			synchronized (ClientProperties.this) {
				setProperty(getKeyOf(len), s);
				updateLen(+1);
				return true;
			}
		}

		@Override
		public void add(int index, String element) {
			checkRange(index, true);
			synchronized (ClientProperties.this) {
				for (int i = len; i > index; i--) {
					setProperty(getKeyOf(i), getProperty(getKeyOf(i - 1)));
				}
			}
			setProperty(getKeyOf(index), element);
			updateLen(+1);
		}

		private void checkRange(int index, boolean insert) {
			if (index < 0 || (insert ? index > len : index >= len)) {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void clear() {
			synchronized (ClientProperties.this) {
				ClientProperties.this.removePrefixed(key + "[");
				updateLen(-len);
			}
		}

		@Override
		public String get(int index) {
			synchronized (ClientProperties.this) {
				checkRange(index, false);
				return getProperty(getKeyOf(index));
			}
		}

		private String getKeyOf(int index) {
			return key + "[" + index + "]";
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(key)) {
				updateLen();
			}
		}

		@Override
		public String remove(int index) {
			synchronized (ClientProperties.this) {
				checkRange(index, false);
				String removed = getProperty(getKeyOf(index));
				synchronized (ClientProperties.this) {
					for (int i = index; i < len; i++) {
						setProperty(getKeyOf(i - 1), getProperty(getKeyOf(i)));
					}
				}
				ClientProperties.this.remove(getKeyOf(len - 1));
				updateLen(-1);
				return removed;
			}
		}

		@Override
		public int size() {
			return len;
		}

		protected void updateLen() {
			String len = getProperty(key, "#list:0").substring("#list:".length());
			this.len = Integer.parseInt(len);
		}

		private void updateLen(int delta) {
			len += delta;
			setProperty(key, "#list:" + len);
		}
	}

	/**
	 * weak reference which supports #equals()
	 *
	 * @param <T> referent type
	 */
	protected class WeakReferenceEx<T> extends WeakReference<T> {
		private int hash;

		public WeakReferenceEx(T referent) {
			super(referent);
			hash = referent.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof WeakReferenceEx && ((WeakReferenceEx) obj).get() == get();
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ClientProperties.class);
	private static final int KEY_BIT = 128;
	private static final String ENCRYPT_HEADER = "$priv$0$";
	private static final String ENCRYPT_FOOTER = "$";
	private static final long SEC2MS = 1000;
	private static final long MIN2MS = SEC2MS * 60;
	private static final long HOUR2MS = MIN2MS * 60;
	private static final long DAY2MS = HOUR2MS * 24;
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

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
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
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
		messageDigest.update(passphrase.getBytes(UTF8_CHARSET));
		byte[] digest = messageDigest.digest();
		byte[] keyBytes = Arrays.copyOf(digest, KEY_BIT / 8);
		return new SecretKeySpec(keyBytes, "AES");
	}

	/**
	 * A property list that contains default values for any keys not
	 * found in this property list.
	 */
	protected final ClientProperties defaults;
	/** リスナの配列 */
	protected transient ConcurrentLinkedQueue<WeakReferenceEx<PropertyChangeListener>> listeners;
	/** 保存先のファイル */
	protected Path storePath;
	/**
	 * properties map
	 */
	protected HashMap<String, String> properties = new HashMap<>();

	/** インスタンスを生成する。 */
	public ClientProperties() {
		this(null);
	}

	/**
	 * defaults を使用してClientPropertiesインスタンスを生成する。
	 *
	 * @param defaults デフォルトプロパティー
	 */
	public ClientProperties(ClientProperties defaults) {
		this.defaults = defaults;
		listeners = new ConcurrentLinkedQueue<>();
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
		listeners.add(new WeakReferenceEx<>(listener));
	}

	@Override
	public synchronized void clear() {
		properties.clear();
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		return properties.containsKey(key);
	}

	@Override
	public synchronized boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	@Override
	public synchronized Set<Entry<String, String>> entrySet() {
		return properties.entrySet();
	}

	@Override
	public synchronized boolean equals(Object o) {
		if (!(o instanceof ClientProperties)) {
			return false;
		}
		ClientProperties c = (ClientProperties) o;
		synchronized (c) {
			if (!properties.equals(c.properties)) {
				return false;
			}
			if (!listeners.equals(c.listeners)) {
				return false;
			}
			if (!storePath.equals(c.storePath)) {
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
		for (Iterator<WeakReferenceEx<PropertyChangeListener>> iterator = listeners.iterator(); iterator.hasNext(); ) {
			WeakReferenceEx<PropertyChangeListener> ref = iterator.next();
			PropertyChangeListener listener = ref.get();
			if (listener == null) {
				iterator.remove();
			} else {
				listener.propertyChange(evt);
			}
		}
	}

	@Override
	public synchronized String get(Object key) {
		return getProperty((String) key);
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
	 */
	public synchronized Color getColor(String key) throws IllegalArgumentException {
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
	 * return default properties
	 *
	 * @return defaults
	 */
	public synchronized ClientProperties getDefaults() {
		return defaults;
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

		//noinspection MagicConstant
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
	 * listを取得する
	 *
	 * @param key キー
	 * @return リスト
	 */
	public List<String> getList(String key) {
		if (isValidListProp(key)) {
			return new PropWrappedList(key);
		} else {
			throw new IllegalArgumentException("`" + key + "' is not valid list.");
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
				return new String(decrypted, UTF8_CHARSET);
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
				return new String(decrypted, UTF8_CHARSET);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return value;
		}
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. The method returns
	 * <code>null</code> if the property is not found.
	 *
	 * @param key the property key.
	 * @return the value in this property list with the specified key value.
	 * @see #setProperty
	 * @see #defaults
	 */
	public synchronized String getProperty(String key) {
		String prop = properties.get(key);
		if (prop == null && defaults != null) {
			prop = defaults.getProperty(key);
		}
		return prop;
	}

	/**
	 * Searches for the property with the specified key in this property list.
	 * If the key is not found in this property list, the default property list,
	 * and its defaults, recursively, are then checked. The method returns the
	 * default value argument if the property is not found.
	 *
	 * @param key          the hashtable key.
	 * @param defaultValue a default value.
	 * @return the value in this property list with the specified key value.
	 * @see #setProperty
	 * @see #defaults
	 */
	public synchronized String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value == null ? defaultValue : value;
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
		hashCode += 19 * properties.hashCode();
		hashCode += 19 * listeners.hashCode();
		hashCode += 19 * storePath.hashCode();
		return hashCode;
	}

	@Override
	public synchronized boolean isEmpty() {
		return properties.isEmpty();
	}

	private boolean isValidListProp(String key) {
		String value = getProperty(key, "#list:0");
		return value.startsWith("#list:");
	}

	@Override
	public synchronized Set<String> keySet() {
		return properties.keySet();
	}

	/**
	 * Reads a property list (key and element pairs) from the input
	 * character stream in a simple line-oriented format.
	 * <p>
	 * Properties are processed in terms of lines. There are two
	 * kinds of line, <i>natural lines</i> and <i>logical lines</i>.
	 * A natural line is defined as a line of
	 * characters that is terminated either by a set of line terminator
	 * characters (<code>\n</code> or <code>\r</code> or <code>\r\n</code>)
	 * or by the end of the stream. A natural line may be either a blank line,
	 * a comment line, or hold all or some of a key-element pair. A logical
	 * line holds all the data of a key-element pair, which may be spread
	 * out across several adjacent natural lines by escaping
	 * the line terminator sequence with a backslash character
	 * <code>\</code>.  Note that a comment line cannot be extended
	 * in this manner; every natural line that is a comment must have
	 * its own comment indicator, as described below. Lines are read from
	 * input until the end of the stream is reached.
	 *
	 * <p>
	 * A natural line that contains only white space characters is
	 * considered blank and is ignored.  A comment line has an ASCII
	 * <code>'#'</code> or <code>'!'</code> as its first non-white
	 * space character; comment lines are also ignored and do not
	 * encode key-element information.  In addition to line
	 * terminators, this format considers the characters space
	 * (<code>' '</code>, <code>'&#92;u0020'</code>), tab
	 * (<code>'\t'</code>, <code>'&#92;u0009'</code>), and form feed
	 * (<code>'\f'</code>, <code>'&#92;u000C'</code>) to be white
	 * space.
	 *
	 * <p>
	 * If a logical line is spread across several natural lines, the
	 * backslash escaping the line terminator sequence, the line
	 * terminator sequence, and any white space at the start of the
	 * following line have no affect on the key or element values.
	 * The remainder of the discussion of key and element parsing
	 * (when loading) will assume all the characters constituting
	 * the key and element appear on a single natural line after
	 * line continuation characters have been removed.  Note that
	 * it is <i>not</i> sufficient to only examine the character
	 * preceding a line terminator sequence to decide if the line
	 * terminator is escaped; there must be an odd number of
	 * contiguous backslashes for the line terminator to be escaped.
	 * Since the input is processed from left to right, a
	 * non-zero even number of 2<i>n</i> contiguous backslashes
	 * before a line terminator (or elsewhere) encodes <i>n</i>
	 * backslashes after escape processing.
	 *
	 * <p>
	 * The key contains all of the characters in the line starting
	 * with the first non-white space character and up to, but not
	 * including, the first unescaped <code>'='</code>,
	 * <code>':'</code>, or white space character other than a line
	 * terminator. All of these key termination characters may be
	 * included in the key by escaping them with a preceding backslash
	 * character; for example,<p>
	 *
	 * <code>\:\=</code><p>
	 *
	 * would be the two-character key <code>":="</code>.  Line
	 * terminator characters can be included using <code>\r</code> and
	 * <code>\n</code> escape sequences.  Any white space after the
	 * key is skipped; if the first non-white space character after
	 * the key is <code>'='</code> or <code>':'</code>, then it is
	 * ignored and any white space characters after it are also
	 * skipped.  All remaining characters on the line become part of
	 * the associated element string; if there are no remaining
	 * characters, the element is the empty string
	 * <code>&quot;&quot;</code>.  Once the raw character sequences
	 * constituting the key and element are identified, escape
	 * processing is performed as described above.
	 *
	 * <p>
	 * As an example, each of the following three lines specifies the key
	 * <code>"Truth"</code> and the associated element value
	 * <code>"Beauty"</code>:
	 * <p>
	 * <pre>
	 * Truth = Beauty
	 *  Truth:Beauty
	 * Truth                    :Beauty
	 * </pre>
	 * As another example, the following three lines specify a single
	 * property:
	 * <p>
	 * <pre>
	 * fruits                           apple, banana, pear, \
	 *                                  cantaloupe, watermelon, \
	 *                                  kiwi, mango
	 * </pre>
	 * The key is <code>"fruits"</code> and the associated element is:
	 * <p>
	 * <pre>"apple, banana, pear, cantaloupe, watermelon, kiwi, mango"</pre>
	 * Note that a space appears before each <code>\</code> so that a space
	 * will appear after each comma in the final result; the <code>\</code>,
	 * line terminator, and leading white space on the continuation line are
	 * merely discarded and are <i>not</i> replaced by one or more other
	 * characters.
	 * <p>
	 * As a third example, the line:
	 * <p>
	 * <pre>cheeses
	 * </pre>
	 * specifies that the key is <code>"cheeses"</code> and the associated
	 * element is the empty string <code>""</code>.<p>
	 * <p>
	 *
	 * <a name="unicodeescapes"></a>
	 * Characters in keys and elements can be represented in escape
	 * sequences similar to those used for character and string literals
	 * (see sections 3.3 and 3.10.6 of
	 * <cite>The Java&trade; Language Specification</cite>).
	 *
	 * The differences from the character escape sequences and Unicode
	 * escapes used for characters and strings are:
	 *
	 * <ul>
	 * <li> Octal escapes are not recognized.
	 *
	 * <li> The character sequence <code>\b</code> does <i>not</i>
	 * represent a backspace character.
	 *
	 * <li> The method does not treat a backslash character,
	 * <code>\</code>, before a non-valid escape character as an
	 * error; the backslash is silently dropped.  For example, in a
	 * Java string the sequence <code>"\z"</code> would cause a
	 * compile time error.  In contrast, this method silently drops
	 * the backslash.  Therefore, this method treats the two character
	 * sequence <code>"\b"</code> as equivalent to the single
	 * character <code>'b'</code>.
	 *
	 * <li> Escapes are not necessary for single and double quotes;
	 * however, by the rule above, single and double quote characters
	 * preceded by a backslash still yield single and double quote
	 * characters, respectively.
	 *
	 * <li> Only a single 'u' character is allowed in a Uniocde escape
	 * sequence.
	 *
	 * </ul>
	 * <p>
	 * The specified stream remains open after this method returns.
	 *
	 * @param reader the input character stream.
	 * @throws IOException              if an error occurred when reading from the
	 *                                  input stream.
	 * @throws IllegalArgumentException if a malformed Unicode escape
	 *                                  appears in the input.
	 * @since 1.6
	 */
	public synchronized void load(Reader reader) throws IOException {
		load0(new LineReader(reader));
	}

	private void load0(LineReader lr) throws IOException {
		char[] convtBuf = new char[1024];
		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;

		while ((limit = lr.readLine()) >= 0) {
			keyLen = 0;
			valueStart = limit;
			hasSep = false;

			//System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
			precedingBackslash = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				//need check if escaped.
				if ((c == '=' || c == ':') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				}
				precedingBackslash = c == '\\' && !precedingBackslash;
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != ' ' && c != '\t' && c != '\f') {
					if (!hasSep && (c == '=' || c == ':')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
			put(key, value);
		}
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars
	 * and changes special saved chars to their original forms
	 */
	private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
							case '0':
							case '1':
							case '2':
							case '3':
							case '4':
							case '5':
							case '6':
							case '7':
							case '8':
							case '9':
								value = (value << 4) + aChar - '0';
								break;
							case 'a':
							case 'b':
							case 'c':
							case 'd':
							case 'e':
							case 'f':
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A':
							case 'B':
							case 'C':
							case 'D':
							case 'E':
							case 'F':
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default:
								throw new IllegalArgumentException(
										"Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't') {
						aChar = '\t';
					} else if (aChar == 'r') {
						aChar = '\r';
					} else if (aChar == 'n') {
						aChar = '\n';
					} else if (aChar == 'f') {
						aChar = '\f';
					}
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = aChar;
			}
		}
		return new String(out, 0, outLen);
	}

	@Override
	public synchronized String put(String key, String value) {
		return setProperty(key, value);
	}

	@Override
	public synchronized void putAll(Map<? extends String, ? extends String> m) {
		for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public synchronized String remove(Object key) {
		firePropertyChanged((String) key, getProperty((String) key), null);
		return properties.remove(key);
	}

	/**
	 * remove list from properties
	 *
	 * @param key property key
	 */
	public void removeList(String key) {
		if (isValidListProp(key)) {
			remove(key);
			removePrefixed(key + "[");
		} else {
			throw new IllegalArgumentException("property `" + key + "' is not list");
		}
	}

	/**
	 * remove all entry with key started with prefix
	 *
	 * @param prefix prefix key
	 */
	public synchronized void removePrefixed(String prefix) {
		for (Iterator<String> iterator = properties.keySet().iterator(); iterator.hasNext(); ) {
			String key = iterator.next();
			if (key.startsWith(prefix)) {
				iterator.remove();
			}
		}
	}

	/**
	 * 登録済みの {@link PropertyChangeListener}を削除する
	 *
	 * @param listener リスナ
	 * @return 登録されて削除された場合true
	 */
	public synchronized boolean removePropertyChangedListener(PropertyChangeListener listener) {
		return listeners.remove(new WeakReferenceEx<>(listener));
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes
	 * special characters with a preceding slash
	 */
	private String saveConvert(String theString, boolean escapeSpace) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuilder outBuffer = new StringBuilder(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			switch (aChar) {
				case ' ':
					if (x == 0 || escapeSpace) {
						outBuffer.append('\\');
					}
					outBuffer.append(' ');
					break;
				case '\t':
					outBuffer.append('\\');
					outBuffer.append('t');
					break;
				case '\n':
					outBuffer.append('\\');
					outBuffer.append('n');
					break;
				case '\r':
					outBuffer.append('\\');
					outBuffer.append('r');
					break;
				case '\f':
					outBuffer.append('\\');
					outBuffer.append('f');
					break;
				case '=': // Fall through
				case ':': // Fall through
				case '#': // Fall through
				case '!': // Fall through
				case '\\':
					outBuffer.append('\\');
					outBuffer.append(aChar);
					break;
				default:
					outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
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
		byte[] bytes = value.getBytes(UTF8_CHARSET);
		String encoded = Base64.encodeBytes(encrypt(bytes, encryptKey));
		setProperty(key, ENCRYPT_HEADER + encoded + ENCRYPT_FOOTER);
	}

	/**
	 * Calls the <tt>Map</tt> method <code>put</code>. Provided for
	 * parallelism with the <tt>getProperty</tt> method. Enforces use of
	 * strings for property keys and values. The value returned is the
	 * result of the <tt>Map</tt> call to <code>put</code>.
	 *
	 * @param key      the key to be placed into this property list.
	 * @param newValue the value corresponding to <tt>key</tt>.
	 * @return the previous value of the specified key in this property
	 * list, or <code>null</code> if it did not have one.
	 * @see #getProperty
	 */
	public synchronized String setProperty(String key, String newValue) {
		String oldValue = properties.put(key, newValue);
		firePropertyChanged(key, oldValue, newValue);
		return oldValue;
	}

	/**
	 * デフォルトの保存先のファイルを設定する。
	 *
	 * @param storePath デフォルトの保存先のファイル
	 */
	public synchronized void setStorePath(Path storePath) {
		this.storePath = storePath;
	}

	/**
	 * set user-friendly property for time as millisecond
	 *
	 * @param key  key
	 * @param time time as milliseconds
	 */
	public synchronized void setTime(String key, long time) {
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

	@Override
	public synchronized int size() {
		return properties.size();
	}

	/** ファイルに保存する。 */
	public synchronized void store() {
		store("Auto generated by jp.syuriken.snsw.twclient.ClientProperties");
	}

	/**
	 * ファイルに保存する
	 *
	 * @param comments ファイルのコメント
	 */
	public void store(String comments) {
		try (BufferedWriter writer = Files.newBufferedWriter(storePath, UTF8_CHARSET)) {
			store(writer, comments);
		} catch (IOException e) {
			logger.warn("Propertiesファイルの保存中にエラー", e);
		}
	}

	/**
	 * store data
	 *
	 * @param writer   writer
	 * @param comments comment
	 */
	public synchronized void store(BufferedWriter writer, String comments) {
		try {
			if (comments != null) {
				writer.write("# ");
				writer.write(comments);
				writer.newLine();
			}
			writer.write("#" + new Date().toString());
			writer.newLine();
			synchronized (this) {
				TreeMap<String, String> sortedMap = new TreeMap<>(properties);
				for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
					String key = saveConvert(entry.getKey(), true);
					String value = saveConvert(entry.getValue(), false);
					writer.write(key + "=" + value);
					writer.newLine();
				}
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized Collection<String> values() {
		return properties.values();
	}
}
