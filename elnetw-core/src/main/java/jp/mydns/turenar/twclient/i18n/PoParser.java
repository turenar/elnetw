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

package jp.mydns.turenar.twclient.i18n;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Parser for *.po
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PoParser {
	/**
	 * indicate not processing any other column
	 */
	private static final int TYPE_NOT_PROCESSING = 0;
	/**
	 * indicate processing msgctxt
	 */
	private static final int TYPE_MSGCTXT = 1;
	/**
	 * indicate processing msgid
	 */
	private static final int TYPE_MSGID = 2;
	/**
	 * indicate processing msgstr
	 */
	private static final int TYPE_MSGSTR = 3;
	/**
	 * separator between msgctxt and msgid
	 */
	public static final char CHAR_SEPARATOR = '\003';

	/**
	 * get message id
	 *
	 * @param comment msgctxt
	 * @param msgId   msgid
	 * @return real message id
	 */
	public static String getMessageId(String comment, String msgId) {
		return comment == null ? msgId : comment + '\003' + msgId;
	}

	/**
	 * get raw message string from real message id
	 *
	 * @param msgId real message id
	 * @return raw message id
	 */
	public static String getRawMessageId(String msgId) {
		int commentSeparator = msgId.indexOf(CHAR_SEPARATOR);
		return commentSeparator < 0 ? msgId : msgId.substring(commentSeparator + 1);
	}

	/**
	 * parse from reader
	 *
	 * @param reader reader instance
	 * @return K=real message id, V=message str
	 */
	public static Map<String, String> parse(Reader reader) {
		StringBuilder builder = new StringBuilder();
		HashMap<String, String> map = new HashMap<>();
		Scanner scanner = new Scanner(reader);

		String comment = null;
		String msgId = null;
		String msgStr = null;
		boolean isFuzzy = false;
		int type = TYPE_NOT_PROCESSING;
		while (true) {
			int substrIndex = 0;
			String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
			int newType = type;
			if (line.isEmpty()) {
				newType = TYPE_NOT_PROCESSING;
			} else if (line.charAt(0) == '#') {
				// #. translators comment
				// #; reference
				// #, flag
				// #| previous msg...
				if (line.startsWith("#,")) {
					if (line.contains("fuzzy")) {
						isFuzzy = true;
					}
				}
				continue;
			} else if (line.startsWith("msgctxt ")) {
				newType = TYPE_MSGCTXT;
				substrIndex = "msgctxt ".length();
			} else if (line.startsWith("msgid ")) {
				newType = TYPE_MSGID;
				substrIndex = "msgid ".length();
			} else if (line.startsWith("msgstr ")) {
				newType = TYPE_MSGSTR;
				substrIndex = "msgstr ".length();
			}
			if (substrIndex > 0) {
				line = line.substring(substrIndex).trim();
			}
			if (type != newType) {
				switch (type) {
					case TYPE_MSGCTXT:
						comment = builder.toString();
						break;
					case TYPE_MSGID:
						msgId = builder.toString();
						break;
					case TYPE_MSGSTR:
						msgStr = builder.toString();
						break;
					default:
						// do nothing
				}
				type = newType;
				builder.setLength(0);
			}
			if (newType == TYPE_NOT_PROCESSING) {
				if (!(msgId == null || msgStr == null)) {
					if (!isFuzzy) {
						map.put(getMessageId(comment, msgId), msgStr);
					}
				} else if (!(msgId == null && msgStr == null)) {
					throw new IllegalArgumentException("msgId and msgStr is required");
				}
				if (scanner.hasNextLine()) {
					// re-init
					comment = null;
					msgId = null;
					msgStr = null;
					isFuzzy = false;
					continue;
				} else {
					break;
				}
			}

			if (line.charAt(0) == '"' && line.charAt(line.length() - 1) == '"') {
				processString(builder, line);
			}
		}
		return map;
	}

	/**
	 * process string with double quotation
	 *
	 * @param builder builder
	 * @param line    line
	 */
	protected static void processString(StringBuilder builder, String line) {
		int end = line.length() - 1;
		boolean isPrecedingBackSlash = false;
		builder.ensureCapacity(builder.length() + end - 1);
		for (int i = 1; i < end; i++) {
			char charAt = line.charAt(i);
			if (isPrecedingBackSlash) {
				switch (charAt) {
					case 'n':
						builder.append('\n');
						break;
					case 'r':
						builder.append('\r');
						break;
					case 't':
						builder.append('\t');
						break;
					default:
						builder.append(charAt);
				}
				isPrecedingBackSlash = false;
			} else if (charAt == '\\') {
				isPrecedingBackSlash = true;
			} else {
				builder.append(charAt);
			}
		}
	}

	private PoParser() {
	}
}
