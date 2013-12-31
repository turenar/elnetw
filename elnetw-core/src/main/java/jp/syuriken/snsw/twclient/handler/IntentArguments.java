package jp.syuriken.snsw.twclient.handler;

import java.util.HashMap;

/**
 * ActionHandler用の引数管理
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class IntentArguments implements Cloneable {
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
	 * アクション名を指定する。
	 *
	 * @param name アクション名
	 * @return このIntentArguments自身
	 */
	public IntentArguments setIntentName(String name) {
		intentName = name;
		return this;
	}

	@Override
	public String toString() {
		return "IntentArguments{name=" + intentName + ",args=" + extraArgs + "}";
	}
}
