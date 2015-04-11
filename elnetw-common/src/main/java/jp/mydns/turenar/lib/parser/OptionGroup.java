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

/**
 * オプションのグループ化を支援するユーティリティ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class OptionGroup {
	private final ArgParser parser;
	private final String groupName;

	/**
	 * インスタンス生成
	 *
	 * @param parser    ArgParser
	 * @param groupName グループ名
	 */
	public OptionGroup(ArgParser parser, String groupName) {
		this.parser = parser;
		this.groupName = groupName;
	}

	/**
	 * オプションを登録する
	 *
	 * @param shortOptName 短いオプション名。"-?"。null可
	 * @param longOptName  長いオプション名。"--?*"
	 * @return オプション設定用のクラス
	 */
	public OptionConfig addOption(String shortOptName, String longOptName) {
		return parser.addOption(shortOptName, longOptName).group(groupName);
	}

	/**
	 * オプションを登録する
	 *
	 * @param shortOptChar 短いオプション名。
	 * @param longOptName  長いオプション名。"--?*"
	 * @return オプション設定用のクラス
	 */
	public OptionConfig addOption(char shortOptChar, String longOptName) {
		return parser.addOption(shortOptChar, longOptName).group(groupName);
	}

	/**
	 * オプションを登録する
	 *
	 * @param longOptName 長いオプション名。"--?*"
	 * @return オプション設定用のクラス
	 */
	public OptionConfig addOption(String longOptName) {
		return parser.addOption(longOptName).group(groupName);
	}

	public String getName() {
		return groupName;
	}
}
