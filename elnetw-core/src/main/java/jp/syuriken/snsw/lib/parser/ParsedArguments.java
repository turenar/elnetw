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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.lib.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * arguments parsed by ArgParser.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ParsedArguments {
	private final ArrayList<String> errorMessages;
	private final ArrayList<String> processArguments;
	private final HashMap<String, OptionInfo> optionInfos;

	protected ParsedArguments() {
		errorMessages = new ArrayList<>(0);
		processArguments = new ArrayList<>();
		optionInfos = new HashMap<>();
	}

	/**
	 * 長いオプションを追加する
	 *
	 * @param longOptName 長いオプション名
	 * @param arg         引数
	 * @param multiple    引数として複数指定できるかどうか
	 */
	protected void addLongOpt(String longOptName, String arg, boolean multiple) {
		addShortOpt(null, longOptName, arg, multiple);
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
	 * @param arg プロセス引数 (オプションと何も関係がない引数)
	 */
	protected void addProcessArgument(String arg) {
		processArguments.add(arg);
	}

	/**
	 * 短いオプションを追加する
	 *
	 * @param shortOptName    短いオプション名
	 * @param longOptName     長いオプション名
	 * @param arg             引数
	 * @param supportMultiArg 引数として複数指定できるかどうか
	 */
	protected void addShortOpt(String shortOptName, String longOptName, String arg, boolean supportMultiArg) {
		OptionInfo optionInfo = optionInfos.get(longOptName);
		OptionInfo newInfo = new OptionInfo(shortOptName, longOptName, arg);
		if (optionInfo == null) {
			optionInfos.put(longOptName, newInfo);
		} else {
			if (supportMultiArg) {
				optionInfo.add(newInfo);
			} else {
				optionInfo.update(shortOptName, longOptName, arg);
			}
		}
	}

	/**
	 * パースエラー数を取得する。
	 * @return エラー数
	 */
	public int getErrorCount() {
		return errorMessages.size();
	}

	/**
	 * エラーメッセージの配列を取得する。
	 * @return パースエラーメッセージ。
	 */
	public String[] getErrorMessages() {
		return errorMessages.toArray(new String[errorMessages.size()]);
	}

	/**
	 * 長いオプションと関連付けられる引数を取得する。
	 * @param longOptName 長いオプション名
	 * @return 引数。オプションが複数指定された場合は最初のオプションの引数。nullの可能性あり。
	 */
	public String getOptArg(String longOptName) {
		OptionInfo optionInfo = optionInfos.get(ArgParser.getLongOptName(longOptName));
		return optionInfo == null ? null : optionInfo.getArg();
	}

	/**
	 * 長いオプションと関連付けられる{@link jp.syuriken.snsw.lib.parser.OptionInfo}インスタンスを取得する。
	 * @param longOptName 長いオプション名
	 * @return オプション情報。オプションが指定されていない場合null。
	 */
	public OptionInfo getOptInfo(String longOptName) {
		return optionInfos.get(ArgParser.getLongOptName(longOptName));
	}

	/**
	 * オプションと関連付けられないプロセス引数配列を取得する。
	 * @return プロセス引数配列。
	 */
	public String[] getProcessArguments() {
		return processArguments.toArray(new String[processArguments.size()]);
	}

	/**
	 * 最初のプロセス引数を取得する。
	 * @return プロセス引数
	 */
	public String getProcessArgument() {
		return getProcessArgument(0);
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
	 * 指定した番号のプロセス引数を取得する。
	 * @param i 番号
	 * @return プロセス引数
	 */
	public String getProcessArgument(int i) {
		return processArguments.get(i);
	}

	/**
	 * プロセス引数の数を取得する
	 * @return 数
	 */
	public int getProcessArgumentCount() {
		return processArguments.size();
	}

	/**
	 * パースエラーがあるかどうかを調べる。
	 * @return パースエラーがある場合true。それ以外false。
	 */
	public boolean hasError() {
		return !errorMessages.isEmpty();
	}

	/**
	 * オプションが指定されたかどうかを返す。
	 * @param longOptName 長いオプション名
	 * @return 指定されているかどうか。
	 */
	public boolean hasOpt(String longOptName) {
		return optionInfos.containsKey(ArgParser.getLongOptName(longOptName));
	}
}
