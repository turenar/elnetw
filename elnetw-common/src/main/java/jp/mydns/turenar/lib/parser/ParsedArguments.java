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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * arguments parsed by ArgParser.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ParsedArguments {
	private static final NullOptionInfo NULL_OPTION_INFO = new NullOptionInfo();
	/**
	 * エラーメッセージのリスト。
	 */
	protected final List<String> errorMessages = new ArrayList<>(0);
	/**
	 * optionとは関係のないプロセス引数
	 */
	protected final List<String> processArguments = new ArrayList<>();
	/**
	 * オプション情報のマップ
	 */
	protected final Map<String, OptionInfo> optionInfos = new HashMap<>();
	/**
	 * オプションの先頭からトラバースできるリスト
	 */
	protected final LinkedList<OptionInfo> optionInfoList = new LinkedList<>();
	private final ArgParser parser;

	/**
	 * create instance
	 *
	 * @param parser ArgParser instance
	 */
	protected ParsedArguments(ArgParser parser) {
		this.parser = parser;
	}

	/**
	 * add option
	 *
	 * @param optName option name (such as -g, --debug)
	 * @param arg     argument (nullable)
	 * @param config  option configuration
	 */
	public void addOpt(String optName, String arg, OptionConfig config) {
		OptionInfo info = new OptionInfo(optName, arg, config);
		optionInfoList.add(info);
		if (config.multiple()) {
			OptionInfo another = optionInfos.putIfAbsent(config.group(), info);
			if (another != null && another.getConfig().equals(config)) {
				another.add(info);
				return;
			}
		}
		optionInfos.put(config.group(), info);
	}

	/**
	 * パースエラーを追加する
	 *
	 * @param errorType エラータイプ
	 * @param opt       オプション名
	 */

	protected void addParseError(ParseErrorType errorType, String opt) {
		String message;
		switch (errorType) {
			case UNKNOWN_LONG_OPT:
			case UNKNOWN_SHORT_OPT:
				message = "Unknown option: " + opt;
				break;
			case MISSING_ARGUMENT:
				message = "Missing argument for `" + opt + "'";
				break;
			default:
				message = errorType + ": " + opt;
		}
		errorMessages.add(message);
	}

	/**
	 * プロセス引数を追加する
	 *
	 * @param arg プロセス引数 (オプションと何も関係がない引数)
	 */
	protected void addProcessArgument(String arg) {
		optionInfoList.add(new OptionInfo(null, arg, null));
		processArguments.add(arg);
	}

	/**
	 * パースエラー数を取得する。
	 *
	 * @return エラー数
	 */
	public int getErrorCount() {
		return errorMessages.size();
	}

	/**
	 * エラーメッセージリストのイテレーターを取得する
	 *
	 * @return イテレータ
	 */
	public Iterator<String> getErrorMessageIterator() {
		return Collections.unmodifiableList(errorMessages).iterator();
	}

	/**
	 * エラーメッセージの配列を取得する。
	 *
	 * @return パースエラーメッセージ。
	 */
	public String[] getErrorMessages() {
		return errorMessages.toArray(new String[errorMessages.size()]);
	}

	/**
	 * 引数情報を取得する
	 *
	 * @param optName 短いまたは長いオプション名。ハイフン付きである必要がある。
	 * @return 引数情報
	 */
	public OptionInfo getOpt(String optName) {
		OptionConfig optConfig = parser.getOptConfig(optName);
		if (optConfig != null) {
			OptionInfo optInfo = getOptGroup(optConfig.group());
			return optInfo != null && optInfo.getConfig().equals(optConfig) ? optInfo : null;
		}
		return null;
	}

	/**
	 * 引数情報を取得する
	 *
	 * @param optName  短いまたは長いオプション名。ハイフン付きである必要がある。
	 * @param iterable イテレート可能かどうか。trueの場合nullを返さない。
	 * @return 引数情報
	 */
	public OptionInfo getOpt(String optName, boolean iterable) {
		OptionInfo optInfo = getOpt(optName);
		return iterable && optInfo == null ? NULL_OPTION_INFO : optInfo;
	}

	/**
	 * 指定された引数に渡された値を取得する。
	 *
	 * @param optName オプション名。ハイフン付きである必要がある。
	 * @return 引数がない、または値が指定されていない時はnull。値が指定されていれば、その値。
	 */
	public String getOptArg(String optName) {
		return getOpt(optName, true).getArg();
	}

	/**
	 * オプション情報を取得する。
	 *
	 * @param group    オプショングループ名。通常長いオプション名と同じ。
	 * @param iterable イテレータブル: nullを返さない。拡張for文等でこのまま使用可能である
	 * @return オプション情報
	 */
	public OptionInfo getOptGroup(String group, boolean iterable) {
		OptionInfo optInfo = getOptGroup(group);
		return iterable && optInfo == null ? NULL_OPTION_INFO : optInfo;
	}

	/**
	 * 長いオプションと関連付けられる{@link jp.mydns.turenar.lib.parser.OptionInfo}インスタンスを取得する。
	 *
	 * @param group オプショングループ名。通常長いオプション名と同じ。
	 * @return オプション情報。オプションが指定されていない場合null。
	 */
	public OptionInfo getOptGroup(String group) {
		return optionInfos.get(group);
	}

	/**
	 * すべてのオプション情報を取得する。これは指定された順番通りに並べられている
	 *
	 * @return オプション情報のイテレータ
	 */
	public Iterator<OptionInfo> getOptionListIterator() {
		return Collections.unmodifiableList(optionInfoList).iterator();
	}

	/**
	 * 最初のプロセス引数を取得する。
	 *
	 * @return プロセス引数
	 */
	public String getProcessArgument() {
		return getProcessArgument(0);
	}

	/**
	 * 指定した番号のプロセス引数を取得する。
	 *
	 * @param i 番号
	 * @return プロセス引数
	 */
	public String getProcessArgument(int i) {
		return processArguments.get(i);
	}

	/**
	 * プロセス引数の数を取得する
	 *
	 * @return 数
	 */
	public int getProcessArgumentCount() {
		return processArguments.size();
	}

	/**
	 * プロセス引数のイテレータを取得する。
	 *
	 * @return イテレータ
	 */
	public Iterator<String> getProcessArgumentIterator() {
		return Collections.unmodifiableList(processArguments).iterator();
	}

	/**
	 * オプションと関連付けられないプロセス引数配列を取得する。
	 *
	 * @return プロセス引数配列。
	 */
	public String[] getProcessArguments() {
		return processArguments.toArray(new String[processArguments.size()]);
	}

	/**
	 * パースエラーがあるかどうかを調べる。
	 *
	 * @return パースエラーがある場合true。それ以外false。
	 */
	public boolean hasError() {
		return !errorMessages.isEmpty();
	}

	/**
	 * オプションが指定されているかどうかを調べる。同じグループ内で複数のオプションが指定されている時、最後にパースされたオプションのみが
	 * 残っている。詳しくは{@link jp.mydns.turenar.lib.parser.ArgParser}の説明を参照する。
	 *
	 * @param optName オプション名
	 * @return 指定された名前を持つオプションが有効であるかどうかを返す。単にオプションが渡されているかどうかを返すわけではないことに
	 * 注意してください。これは、同じグループ名を持つ場合、--verbose,--quietが渡されたからと言って、hasOpt("--verbose")がtrueになる
	 * わけではないということです。
	 */
	public boolean hasOpt(String optName) {
		return getOpt(optName) != null;
	}

	/**
	 * オプションが指定されたかどうかを返す。
	 *
	 * @param group オプショングループ名。通常長いオプション名と同じ。
	 * @return 指定されているかどうか。
	 */
	public boolean hasOptGroup(String group) {
		return optionInfos.containsKey(group);
	}

	/**
	 * オプションが指定されたかどうかを返す。
	 *
	 * @param group オプショングループ。
	 * @return 指定されているかどうか。
	 */
	public boolean hasOptGroup(OptionGroup group) {
		return hasOptGroup(group.getName());
	}
}
