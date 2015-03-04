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
 *
 * <p>このクラスは、GNU styleの引数検索をします。</p>
 * <p>すなわち、長いオプションを取ることができる、--hoge=fugaと書くことができる、-abcdeと続けて書くことができる、-a123のように
 * 値を取ることもできる、--でオプションと引数とを分けることができる、、などです。</p>
 * <h2>オプションに対する引数の指定</h2>
 * <p>{@link jp.mydns.turenar.lib.parser.ArgumentType}を見ていただきたいのですが、引数必須、引数任意、引数なしの三パターンあります。
 * 引数任意の場合は、後ろに来る引数が"-"で始まらない (つまり引数ではなくオプションだと思われる )ときに引数として解釈されます。
 * 引数必須の場合は、後ろに来る引数が何であろうと引数として解釈されます。ただし、引数が存在しない時はパースエラーとして登録されます。
 * (参照: {@link ParsedArguments#getErrorMessages()})</p>
 * <h2>オプションの複数指定</h2>
 * <p>{@link OptionConfig#multiple(boolean)}でtrueを指定したオプションは、複数指定することが可能になります。未指定 (false)
 * の時はオプションのうち最後の一つのみが尊重されます。</p>
 * <h2>オプションのグループ化</h2>
 * <p>オプションをグループに分けることで、相反するオプションの中で、最後に書かれた一つのみを返すことが出来ます。
 * 意味がわからないと思うので、例をあげます。</p>
 * <p>例えば、--quietと--verboseが共にqvoptionグループであると指定したとします。これは次のコードで表すことが出来ます。</p>
 * <pre>
 *     ArgParser parser = new ArgParser();
 *     parser.addOption("--quiet").group("qvoption");
 *     parser.addOption("--verbose").group("qvoption");
 * </pre>
 * <p>その上で、parserに"--quiet","--verbose"を渡すと、"--verbose"のみが引数を持っていると判定されます。これは、同じグループ名を
 * 持つために、最後に指定されたオプションのみが保持されたからです。</p>
 * <p>なお、multiple指定がされている場合、そのグループ内で最後にパースしたオプションが、新しくパースしたオプションと同じだった時のみ
 * 保持されます。これは、-e,-iがmultipleであるときに、-e 's/hoge//' -i -e's/fuga//'は-e's/fuga//'しか保持しないということを表します。
 * この挙動は直感的ではないので、multiple指定とgroup指定は同時には使わないのが望ましいでしょう。</p>
 * <p>なお、group名が省略された時には、便宜的に長いオプション (--付き) と同じものが指定されます。</p>
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgParser {

	/**
	 * get long opt from name. name can be prefixed with "--". We return "--XXX"
	 *
	 * @param longOptName name (prefix-able with "--")
	 * @return --XXX style opt
	 */
	protected static String getLongOptName(String longOptName) {
		return longOptName == null ? null
				: longOptName.startsWith("--") ? longOptName : ("--" + longOptName);
	}

	private static String getShortOptName(String shortOptName) {
		if (shortOptName == null) {
			return null;
		} else if (shortOptName.length() == 2 && shortOptName.charAt(0) == '-' && shortOptName.charAt(1) != '-') {
			return shortOptName;
		} else if (shortOptName.length() == 1) {
			return "-" + shortOptName;
		} else {
			throw new IllegalArgumentException("Wrong short opt format: " + shortOptName);
		}
	}

	/**
	 * オプション情報を格納するマップ
	 */
	protected final HashMap<String, OptionConfig> optionInfoMap;
	/**
	 * 未知のオプションを無視するかどうか
	 */
	protected boolean ignoreUnknownOption;

	/**
	 * インスタンスを作成する。
	 */
	public ArgParser() {
		optionInfoMap = new HashMap<>();
	}

	/**
	 * 引数グループを追加する。単なるユーティリティクラスを返すだけなので、わざわざこのメソッドを呼ぶ必要はない
	 *
	 * @param groupName グループ名
	 * @return グループ名を設定しやすいユーティリティクラス
	 * @see jp.mydns.turenar.lib.parser.OptionConfig#group(String)
	 */
	public OptionGroup addGroup(String groupName) {
		return new OptionGroup(this, groupName);
	}

	/**
	 * オプションを登録する
	 *
	 * @param shortOptName 短いオプション名。"-?"。null可
	 * @param longOptName  長いオプション名。"--?*"
	 * @return オプション設定用のクラス
	 */
	public OptionConfig addOption(String shortOptName, String longOptName) {
		if (longOptName == null) {
			throw new IllegalArgumentException("longOptName must not be null");
		}

		OptionConfig config = new OptionConfig(getShortOptName(shortOptName), getLongOptName(longOptName));
		if (shortOptName != null) {
			putOptionIfAbsent(config.getShortOptName(), config);
		}
		putOptionIfAbsent(config.getLongOptName(), config);
		return config;
	}

	/**
	 * オプションを登録する
	 *
	 * @param shortOptChar 短いオプション名。
	 * @param longOptName  長いオプション名。"--?*"
	 * @return オプション設定用のクラス
	 */
	public OptionConfig addOption(char shortOptChar, String longOptName) {
		return addOption("-" + shortOptChar, longOptName);
	}

	/**
	 * オプションを登録する
	 *
	 * @param longOptName 長いオプション名。"--?*"
	 * @return オプション設定用のクラス
	 */
	public OptionConfig addOption(String longOptName) {
		return addOption(null, longOptName);
	}

	/**
	 * 指定したオプション名に対応する {@link jp.mydns.turenar.lib.parser.OptionConfig} を取得する
	 *
	 * @param optName オプション名。"-?"または"--?*"の形で指定する
	 * @return ない場合はnull
	 */
	public OptionConfig getOptConfig(String optName) {
		return optionInfoMap.get(optName);
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
		ParsedArguments parsed = new ParsedArguments(this);
		while (tokenizer.next()) {
			String opt = tokenizer.getOpt();
			if (opt == null) {
				parsed.addProcessArgument(tokenizer.getArg());
			} else if (!opt.equals("--")) {
				OptionConfig optionConfig = optionInfoMap.get(opt);
				if (optionConfig == null) {
					if (!ignoreUnknownOption) {
						parsed.addParseError(ParseErrorType.UNKNOWN_LONG_OPT, opt);
					}
					parsed.addProcessArgument(opt);
					continue;
				}
				String arg;
				switch (optionConfig.argType()) {
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

				parsed.addOpt(opt, arg, optionConfig);
			}
		}
		return parsed;
	}

	/**
	 * オプションがすでに登録してないかどうかを確認して登録する。
	 *
	 * @param optName オプション名
	 * @param config  オプション
	 */
	private void putOptionIfAbsent(String optName, OptionConfig config) {
		OptionConfig alreadyInserted = optionInfoMap.putIfAbsent(optName, config);
		if (alreadyInserted != null) {
			throw new IllegalArgumentException(optName + " is already registered");
		}
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
