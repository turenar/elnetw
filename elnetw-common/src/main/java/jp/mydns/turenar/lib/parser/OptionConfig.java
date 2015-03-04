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

package jp.mydns.turenar.lib.parser;

import java.util.Objects;

/**
 * オプションについての設定
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class OptionConfig {
	private final String shortOptName;
	private final String longOptName;
	private ArgumentType type = ArgumentType.NO_ARGUMENT;
	private String group;
	private boolean multiple;

	/**
	 * インスタンス生成
	 *
	 * @param shortOptName 短いオプション名。null可
	 * @param longOptName  長いオプション名。null不可
	 */
	public OptionConfig(String shortOptName, String longOptName) {
		Objects.requireNonNull("longOptName must not be null");
		this.shortOptName = shortOptName;
		this.group = this.longOptName = longOptName;
	}

	/**
	 * 引数のタイプを取得する
	 *
	 * @return 引数のタイプ
	 */
	public ArgumentType argType() {
		return type;
	}

	/**
	 * 引数のタイプを設定する
	 *
	 * @param type 引数のタイプ
	 * @return このインスタンス
	 */
	public OptionConfig argType(ArgumentType type) {
		this.type = type;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof OptionConfig)) {
			return false;
		}
		OptionConfig another = (OptionConfig) obj;
		return type == another.type
				&& group.equals(another.group)
				&& multiple == another.multiple
				&& longOptName.equals(another.longOptName)
				&& (shortOptName == null ? another.shortOptName == null : shortOptName.equals(another.shortOptName));
	}

	/**
	 * 長いオプション名を取得する
	 *
	 * @return 長いオプション名
	 */
	public String getLongOptName() {
		return longOptName;
	}

	/**
	 * 短いオプション名を取得する
	 *
	 * @return 短いオプション名
	 */
	public String getShortOptName() {
		return shortOptName;
	}

	/**
	 * グループ名の設定
	 *
	 * @param group グループ
	 * @return このインスタンス
	 */
	public OptionConfig group(OptionGroup group) {
		return group(group.getName());
	}

	/**
	 * グループ名の設定
	 *
	 * @param group グループ
	 * @return このインスタンス
	 */
	public OptionConfig group(String group) {
		this.group = group;
		return this;
	}

	/**
	 * グループ名を取得する
	 *
	 * @return グループ名
	 */
	public String group() {
		return group;
	}

	@Override
	public int hashCode() {
		int hash = shortOptName == null ? 0 : shortOptName.hashCode();
		// hash << 5 - hash = hash * 31
		hash = (hash << 5) - hash + longOptName.hashCode();
		hash = (hash << 5) - hash + type.hashCode();
		hash = (hash << 5) - hash + group.hashCode();
		hash = (hash << 5) - hash + (multiple ? 1 : 0);
		return hash;
	}

	/**
	 * 複数オプションを取れるかどうかを設定する
	 *
	 * @param isMulti 取れるかどうか
	 * @return このインスタンス
	 */
	public OptionConfig multiple(boolean isMulti) {
		this.multiple = isMulti;
		return this;
	}

	/**
	 * 複数オプションを取れるかどうかを取得する
	 *
	 * @return 取れるかどうか
	 */
	public boolean multiple() {
		return multiple;
	}
}
