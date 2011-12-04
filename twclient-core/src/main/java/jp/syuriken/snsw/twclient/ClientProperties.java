package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Properties;

/**
 * 登録済みのリスナに変更を通知するプロパティーリストです。
 * 
 * @author $Author$
 */
@SuppressWarnings("serial")
public class ClientProperties extends Properties {
	
	private ArrayList<PropertyChangeListener> listeners;
	
	private File storeFile;
	
	
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
	}
	
	/**
	 * PropertyChangedListnerを追加する
	 * @param listener リスナ。nullは不可
	 */
	public void addPropertyChangedListner(PropertyChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listenerはnullであってはいけません。");
		}
		listeners.add(listener);
	}
	
	@Override
	public synchronized boolean equals(Object o) {
		if (o instanceof ClientProperties == false) {
			return false;
		}
		if (super.equals(o) == false) {
			return false;
		}
		ClientProperties c = (ClientProperties) o;
		if (listeners.equals(c.listeners) == false) {
			return false;
		}
		if (storeFile.equals(c.storeFile) == false) {
			return false;
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
	public void firePropetyChanged(String key, String oldValue, String newValue) {
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
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(key);
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
	public Color getColor(String key) throws IllegalArgumentException, NumberFormatException {
		String value = getProperty(key);
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
	 * keyに関連付けられた値を利用して、intを取得する。
	 * 
	 * 書式：int
	 * @param key キー 
	 * @return keyに関連付けられたint
	 */
	public int getInteger(String key) {
		return Integer.parseInt(getProperty(key));
	}
	
	/**
	 * keyに関連付けられた値を利用して、longを取得する。
	 * 
	 * 書式：long
	 * @param key キー
	 * @return keyに関連付けられたlong
	 */
	public long getLong(String key) {
		return Long.parseLong(getProperty(key));
	}
	
	@Override
	public synchronized int hashCode() {
		int hashCode = super.hashCode();
		hashCode += 19 * listeners.hashCode();
		hashCode += 41 * storeFile.hashCode();
		return hashCode;
	}
	
	/**
	 * 登録済みの {@link PropertyChangeListener}を削除する
	 * 
	 * @param listener リスナ
	 * @return 登録されて削除された場合true
	 */
	public boolean removePropertyChangedListener(PropertyChangeListener listener) {
		return listeners.remove(listener);
	}
	
	/**
	 * keyにbooleanを関連付ける。
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public void setBoolean(String key, boolean value) {
		setProperty(key, String.valueOf(value));
	}
	
	/**
	 * keyにColorを関連付ける。
	 * 
	 * @param key キー
	 * @param color Colorインスタンス。null不可。
	 */
	public void setColor(String key, Color color) {
		setProperty(
				key,
				MessageFormat.format("{0},{1},{2},{3}", color.getRed(), color.getGreen(), color.getBlue(),
						color.getAlpha()));
	}
	
	/**
	 * keyにintを関連付ける
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public void setInteger(String key, int value) {
		setProperty(key, String.valueOf(value));
	}
	
	/**
	 * keyにlongを関連付ける。
	 * 
	 * @param key キー
	 * @param value 値
	 */
	public void setLong(String key, long value) {
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
	public void setStoreFile(File storeFile) {
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
	public void store(String comments) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(storeFile);
			bufferedWriter = new BufferedWriter(fileWriter);
			store(bufferedWriter, comments);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
