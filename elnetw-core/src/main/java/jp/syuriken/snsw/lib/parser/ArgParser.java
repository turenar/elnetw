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

import java.util.HashMap;

/**
 * ArgParser: parse arguments as gnu coreutils
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgParser {

	protected final HashMap<String, OptionType> optionInfoMap;
	private final HashMap<String, String> shortOptMap;

	public ArgParser() {
		optionInfoMap = new HashMap<>();
		shortOptMap = new HashMap<>();
	}

	public ArgParser addLongOpt(String longOptName, OptionType type) {
		optionInfoMap.put(getLongOptName(longOptName), type);
		return this;
	}

	public ArgParser addShortOpt(char shortOptName, String longOptName) {
		return addShortOpt("-" + shortOptName, longOptName);
	}

	public ArgParser addShortOpt(String shortOptName, String longOptName) {
		if (optionInfoMap.containsKey(getLongOptName(longOptName))) {
			if (shortOptName.length() == 1) {
				shortOptName = "-" + shortOptName;
			} else if (shortOptName.length() != 2 || shortOptName.charAt(0) != '-') {
				// shortOptName is not -X style
				throw new IllegalArgumentException("Illegal shortOptName: " + shortOptName);
			}
			shortOptMap.put(shortOptName, getLongOptName(longOptName));
		} else {
			throw new IllegalArgumentException("Unknown long option: " + longOptName);
		}
		return this;
	}

	public static String getLongOptName(String longOptName) {
		return longOptName.startsWith("--") ? longOptName : ("--" + longOptName);
	}

	public ParsedArguments parse(String[] argv) {
		ArgTokenizer tokenizer = new ArgTokenizer(argv);
		ParsedArguments parsed = new ParsedArguments();
		while (tokenizer.next()) {
			String opt = tokenizer.getOpt();
			if (opt == null) {
				parsed.addProcArg(tokenizer.getArg());
			} else if (!opt.equals("--")) {
				String longOpt;
				if (opt.startsWith("--")) {
					longOpt = opt;
				} else {
					longOpt = shortOptMap.get(opt);
					if (longOpt == null) {
						parsed.addParseError(ParseErrorType.UNKNOWN_SHORT_OPT, opt);
						parsed.addProcArg(opt);
						continue;
					}
				}
				OptionType optionType = optionInfoMap.get(longOpt);
				if (optionType == null) {
					parsed.addParseError(ParseErrorType.UNKNOWN_LONG_OPT, opt);
					parsed.addProcArg(longOpt);
					continue;
				}
				String arg;
				switch (optionType) {
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
					parsed.addLongOpt(longOpt, arg);
				} else {
					parsed.addShortOpt(opt, longOpt, arg);
				}
			}
		}
		return parsed;
	}
}
