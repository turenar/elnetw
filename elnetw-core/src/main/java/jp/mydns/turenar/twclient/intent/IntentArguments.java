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

package jp.mydns.turenar.twclient.intent;

import java.util.HashMap;

import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.gui.ShortcutKeyManager;
import jp.mydns.turenar.twclient.internal.IntentActionListener;

/**
 * ActionHandler用の引数管理
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class IntentArguments implements Cloneable {
	private static final ClientConfiguration configuration = ClientConfiguration.getInstance();
	/**
	 * 名前なし引数名
	 */
	public static final String UNNAMED_ARG = "_arg";
	private String intentName;
	private HashMap<String, Object> extraArgs;

	/**
	 * インスタンスの生成。
	 *
	 * @param intentName アクション名
	 */
	public IntentArguments(String intentName) {
		this.intentName = intentName;
	}

	@SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
	@Override
	public IntentArguments clone() {
		IntentArguments newInstance;
		try {
			newInstance = (IntentArguments) super.clone();
		} catch (CloneNotSupportedException e) {
			// will not occurred
			throw new AssertionError(e);
		}
		newInstance.extraArgs = new HashMap<>(extraArgs);
		return newInstance;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntentArguments) {
			IntentArguments another = (IntentArguments) obj;
			if (!intentName.equals(another.intentName)) {
				return false;
			} else if (extraArgs == null || extraArgs.isEmpty()) {
				return another.extraArgs == null || another.extraArgs.isEmpty();
			}
			return extraArgs.equals(another.extraArgs);
		} else {
			return false;
		}
	}

	/**
	 * 引数を取得する
	 *
	 * @param name 引数名
	 * @return 引数として指定された値。指定されなかった場合はnull
	 * @see #getExtra(String, Object)
	 * @see #getExtraObj(String, Class)
	 */
	public Object getExtra(String name) {
		return getExtra(name, null);
	}

	/**
	 * 引数を取得する
	 *
	 * @param name         引数名
	 * @param defaultValue デフォルトの値
	 * @return 引数と指定された値。指定されていなかった場合は defaultValue
	 * @see #getExtra(String)
	 * @see #getExtraObj(String, Class)
	 */
	public Object getExtra(String name, Object defaultValue) {
		if (extraArgs != null) {
			return extraArgs.get(name);
		}
		return defaultValue;
	}

	/**
	 * 引数を取得する。 {@link #getExtra(String)}との違いは、指定された値が指定されたクラスにキャスト可能の場合のみ値を返す点です。
	 *
	 * @param name  引数名
	 * @param clazz 引数として期待するクラスのClassインスタンス。
	 * @param <T>   引数として期待するクラス
	 * @return 引数として指定された値。指定されなかった場合はnull
	 * @see #getExtraObj(String, Class, Object)
	 */
	public <T> T getExtraObj(String name, Class<T> clazz) {
		return getExtraObj(name, clazz, null);
	}

	/**
	 * 引数を取得する。 {@link #getExtra(String, Object)}との違いは、指定された値が指定されたクラスにキャスト可能の場合のみ値を返す点です。
	 *
	 * @param name         引数名
	 * @param clazz        引数として期待するクラスのClassインスタンス。
	 * @param defaultValue デフォルト値
	 * @param <T>          引数として期待するクラス
	 * @return 引数として指定された値。指定されていなかった場合は defaultValue
	 * @see #getExtraObj(String, Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getExtraObj(String name, Class<T> clazz, T defaultValue) {
		if (extraArgs != null) {
			Object v = extraArgs.get(name);
			if (clazz.isInstance(v)) {
				return (T) v;
			}
		}
		return defaultValue;
	}

	/**
	 * アクション名を取得する
	 *
	 * @return アクション名
	 */
	public String getIntentName() {
		return intentName;
	}

	/**
	 * check if argument 'name' is put
	 *
	 * @param name argument name
	 * @return put?
	 */
	public boolean hasExtra(String name) {
		return extraArgs.containsKey(name);
	}

	/**
	 * check if argument 'name' is put and argument is not null and cast-able into clazz
	 *
	 * @param name  argument name
	 * @param clazz class to check cast
	 * @return getExtraObj(name, clazz) !=null
	 */
	public boolean hasExtraObj(String name, Class<?> clazz) {
		return getExtraObj(name, clazz) != null;
	}

	@Override
	public int hashCode() {
		return intentName.hashCode() * 31 + (extraArgs == null ? 0 : extraArgs.hashCode());
	}

	/**
	 * delegate for {@link jp.mydns.turenar.twclient.ClientConfiguration#handleAction(IntentArguments)}
	 */
	public void invoke() {
		configuration.handleAction(this);
	}

	/**
	 * 指定した値を引数として設定する
	 *
	 * @param name  引数名
	 * @param value 引数値
	 * @return このIntentArguments自身
	 */
	public IntentArguments putExtra(String name, Object value) {
		if (extraArgs == null) {
			extraArgs = new HashMap<>();
		}
		extraArgs.put(name, value);
		return this;
	}

	/**
	 * 指定した名前を引数から削除する
	 * @param name 引数名
	 * @return このインスタンス
	 */
	public IntentArguments removeExtra(String name) {
		extraArgs.remove(name);
		return this;
	}

	/**
	 * アクション名を指定する。
	 *
	 * @param name アクション名
	 * @return このIntentArguments自身
	 */
	public IntentArguments setIntentName(String name) {
		intentName = name;
		return this;
	}

	/**
	 * メニューにこのIntentArgumentsを関連付ける。
	 * これはあくまでヘルパーメソッドであり、IntentArgumentsはメニューとの関係をもちません。あくまで
	 * 呼び出されるだけです。
	 *
	 * @param menuItem メニューアイテム
	 */
	public void setMenu(JMenuItem menuItem) {
		menuItem.addActionListener(new IntentActionListener(this));
		menuItem.setAccelerator(ShortcutKeyManager.getKeyStrokeFromIntent(this));
	}

	@Override
	public String toString() {
		return "IntentArguments{name=" + intentName + ",args=" + extraArgs + "}";
	}
}
