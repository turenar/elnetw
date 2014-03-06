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
import java.util.HashMap;

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

	protected void addLongOpt(String longOptName, String arg) {
		optionInfos.put(longOptName, new OptionInfo(null, longOptName, arg));
	}

	public void addParseError(ParseErrorType errorType, String opt) {
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

	public void addProcArg(String arg) {
		processArguments.add(arg);
	}

	protected void addShortOpt(String shortOptName, String longOptName, String arg) {
		optionInfos.put(longOptName, new OptionInfo(shortOptName, longOptName, arg));
	}

	public int getErrorCount() {
		return errorMessages.size();
	}

	public String[] getErrorMessages() {
		return errorMessages.toArray(new String[errorMessages.size()]);
	}

	public Object getOptArg(String longOptName) {
		OptionInfo optionInfo = optionInfos.get(ArgParser.getLongOptName(longOptName));
		return optionInfo == null ? null : optionInfo.getArg();
	}

	public OptionInfo getOptInfo(String longOptName) {
		return optionInfos.get(ArgParser.getLongOptName(longOptName));
	}

	public String[] getProcessArguments() {
		return processArguments.toArray(new String[processArguments.size()]);
	}

	public String getProcessArgument() {
		return getProcessArgument(0);
	}

	public String getProcessArgument(int i) {
		return processArguments.get(i);
	}

	public int getProcessArgumentCount() {
		return processArguments.size();
	}

	public boolean hasError() {
		return !errorMessages.isEmpty();
	}

	public boolean hasOpt(String longOptName) {
		return optionInfos.containsKey(ArgParser.getLongOptName(longOptName));
	}
}
