package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登録済みのリスナに変更を通知できるプロパティーリストです。
 * 
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings("serial")
public class ClientProperties extends Properties {
	
	/** リスナの配列 */
	protected ArrayList<PropertyChangeListener> listeners;
	
	/** 保存先のファイル */
	protected File storeFile;
	
	private transient Hashtable<String, Object> cacheTable;
	
	private static final Logger logger = LoggerFactory.getLogger(ClientProperties.class);
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
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
	 * @param key キー
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
	* @param key キー
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
	 * keyと関連付けられた値を利用して、boolean値を取得する。変換できない場合はfalseを返す。
	 * 
	 * <p>書式：(true|false)</p>
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
	 * @param key キー
	 * @param expectedClass 期待するClass
	 * @return
	 * 	キャッシュされていない、またはexpectedClassのインスタンスではない場合null。
	 * 	それ以外はキャッシュされた値
	 */
	@SuppressWarnings("unchecked")
	protected synchronized <T>T getCachedValue(String key, Class<T> expectedClass) {
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
	 * @param key キー
	 * @return Colorインスタンス
	 * @throws IllegalArgumentException int,int,int[,int]の形ではありません 
	 * @throws NumberFormatException 数値に変換できない値です
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
	 * keyに関連付けられた値を利用して、intを取得する。
	 * 
	 * 書式：int
	 * @param key キー 
	 * @return keyに関連付けられたint
	 */
	public synchronized int getInteger(String key) {
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
	 * keyに関連付けられた値を利用して、longを取得する。
	 * 
	 * 書式：long
	 * @param key キー
	 * @return keyに関連付けられたlong
	 */
	public synchronized long getLong(String key) {
		Long long1 = getCachedValue(key, Long.class);
		if (long1 != null) {
			return long1;
		}
		
		String value = getProperty(key);
		long1 = Long.valueOf(value);
		cacheValue(key, long1);
		return long1;
	}
	
	@Override
	public synchronized int hashCode() {
		int hashCode = super.hashCode();
		hashCode += 19 * listeners.hashCode();
		hashCode += 19 * storeFile.hashCode();
		return hashCode;
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
	 * @param key キー
	 * @param value 値
	 */
	public synchronized void setBoolean(String key, boolean value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}
	
	/**
	 * keyにColorを関連付ける。
	 * 
	 * @param key キー
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
	 * @param key キー
	 * @param dimension Dimensionインスタンス。null不可。
	 */
	public synchronized void setDimension(String key, Dimension dimension) {
		clearCachedValue(key);
		setProperty(key, dimension.width + "," + dimension.height);
	}
	
	/**
	 * keyにdoubleを関連付ける。
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public synchronized void setDouble(String key, double value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}
	
	/**
	 * keyにfloatを関連付ける
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public synchronized void setFloat(String key, float value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}
	
	/**
	 * keyにintを関連付ける
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public synchronized void setInteger(String key, int value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
	}
	
	/**
	 * keyにlongを関連付ける。
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public synchronized void setLong(String key, long value) {
		clearCachedValue(key);
		setProperty(key, String.valueOf(value));
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
	
	/**
	 * ファイルに保存する。
	 * 
	 */
	public void store() {
		store("Auto generated by jp.syuriken.snsw.twclient.ClientProperties");
	}
	
	/**
	* ファイルに保存する
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
