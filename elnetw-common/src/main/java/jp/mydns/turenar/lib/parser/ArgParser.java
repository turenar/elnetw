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

import java.util.HashMap;

/**
 * ArgParser: parse arguments as gnu coreutils
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgParser {

	private static class OptionConfig {
		private final OptionType type;
		private final boolean multiple;

		public OptionConfig(OptionType type, boolean multiple) {
			this.type = type;
			this.multiple = multiple;
		}

		public OptionType getType() {
			return type;
		}

		public boolean isMultiple() {
			return multiple;
		}
	}

	/**
	 * get long opt from name. name can be prefixed with "--". We return "--XXX"
	 *
	 * @param longOptName name (prefix-able with "--")
	 * @return --XXX style opt
	 */
	protected static String getLongOptName(String longOptName) {
		return longOptName.startsWith("--") ? longOptName : ("--" + longOptName);
	}

	/**
	 * オプション情報を格納するマップ
	 */
	protected final HashMap<String, OptionConfig> optionInfoMap;
	/**
	 * 短いオプションKと長いオプションVを多:1で対応付けるマップ
	 */
	protected final HashMap<String, String> shortOptMap;
	/**
	 * 未知のオプションを無視するかどうか
	 */
	protected boolean ignoreUnknownOption;

	/**
	 * インスタンスを作成する。
	 */
	public ArgParser() {
		optionInfoMap = new HashMap<>();
		shortOptMap = new HashMap<>();
	}

	/**
	 * 長いオプションをパース対象として追加する。
	 *
	 * @param longOptName 長いオプション名
	 * @param type        オプションタイプ
	 * @return このインスタンス
	 */
	public ArgParser addLongOpt(String longOptName, OptionType type) {
		return addLongOpt(longOptName, type, false);
	}

	/**
	 * 長いオプションをパース対象として追加する。
	 *
	 * @param longOptName 長いオプション名
	 * @param type        オプションタイプ
	 * @param multiple    複数指定可能かどうか。falseのときオプションに対する引数等は最後の一つしか保存されない
	 * @return このインスタンス
	 */
	public ArgParser addLongOpt(String longOptName, OptionType type, boolean multiple) {
		optionInfoMap.put(getLongOptName(longOptName), new OptionConfig(type, multiple));
		return this;
	}

	/**
	 * 短いオプションを長いオプション名の別名として追加する。
	 *
	 * @param shortOptName 短いオプション名
	 * @param longOptName  長いオプション名
	 * @return このインスタンス
	 */
	public ArgParser addShortOpt(char shortOptName, String longOptName) {
		return addShortOpt("-" + shortOptName, longOptName);
	}

	/**
	 * 短いオプションを長いオプション名の別名として追加する。
	 *
	 * @param shortOptName 短いオプション名
	 * @param longOptName  長いオプション名
	 * @return このインスタンス
	 */
	public ArgParser addShortOpt(String shortOptName, String longOptName) {
		if (optionInfoMap.containsKey(getLongOptName(longOptName))) {
			if (shortOptName.length() == 1) {
				shortOptName = "-" + shortOptName;
			} else if (!(shortOptName.length() == 2 && shortOptName.charAt(0) == '-')) {
				// shortOptName is not -X style
				throw new IllegalArgumentException("Illegal shortOptName: " + shortOptName);
			}
			shortOptMap.put(shortOptName, getLongOptName(longOptName));
		} else {
			throw new IllegalArgumentException("Unknown long option: " + longOptName);
		}
		return this;
	}

	/**
	 * 未知のオプションを無視するかどうかを取得する
	 *
	 * @return 未知のオプションを無視するかどうか
	 */
	public boolean isIgnoreUnknownOption() {
		return ignoreUnknownOption;
	}

	/**
	 * 引数をパースする
	 *
	 * @param args 引数の配列
	 * @return ParsedArgumentsインスタンス。
	 */
	public ParsedArguments parse(String[] args) {
		ArgTokenizer tokenizer = new ArgTokenizer(args);
		ParsedArguments parsed = new ParsedArguments();
		while (tokenizer.next()) {
			String opt = tokenizer.getOpt();
			if (opt == null) {
				parsed.addProcessArgument(tokenizer.getArg());
			} else if (!opt.equals("--")) {
				String longOpt;
				if (opt.startsWith("--")) {
					longOpt = opt;
				} else {
					longOpt = shortOptMap.get(opt);
					if (longOpt == null) {
						if (!ignoreUnknownOption) {
							parsed.addParseError(ParseErrorType.UNKNOWN_SHORT_OPT, opt);
						}
						parsed.addProcessArgument(opt);
						continue;
					}
				}
				OptionConfig optionConfig = optionInfoMap.get(longOpt);
				if (optionConfig == null) {
					if (!ignoreUnknownOption) {
						parsed.addParseError(ParseErrorType.UNKNOWN_LONG_OPT, opt);
					}
					parsed.addProcessArgument(longOpt);
					continue;
				}
				String arg;
				switch (optionConfig.getType()) {
					case REQUIRED_ARGUMENT:
						arg = tokenizer.getArg();
						if (arg == null) {
							parsed.addParseError(ParseErrorType.MISSING_ARGUMENT, opt);
						}
						tokenizer.consumeArg();
						break;
					case OPTIONAL_ARGUMENT:
						arg = tokenizer.getArg();
						if (arg == null || arg.startsWith("-")) {
							arg = null;
						} else {
							tokenizer.consumeArg();
						}
						break;
					case NO_ARGUMENT:
						arg = null;
						break;
					default:
						throw new AssertionError();
				}

				if (opt.equals(longOpt)) {
					parsed.addLongOpt(longOpt, arg, optionConfig.isMultiple());
				} else {
					parsed.addShortOpt(opt, longOpt, arg, optionConfig.isMultiple());
				}
			}
		}
		return parsed;
	}

	/**
	 * 未知のオプションを無視するかどうかを設定する
	 *
	 * @param ignoreUnknownOption 無視するかどうか
	 * @return このインスタンス
	 */
	public ArgParser setIgnoreUnknownOption(boolean ignoreUnknownOption) {
		this.ignoreUnknownOption = ignoreUnknownOption;
		return this;
	}
}
