/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package jp.mydns.turenar.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compare version strings.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class VersionComparator {
	protected static enum VersionComponentType {
		ALPHABETS, NUMERIC, OP
	}

	private static final String OP_CHARS = "~\\-_\\.+";
	/** Version Pattern */
	public static final String VERSION_PATTERN_STRING =
		/**/"\\G(?:"
		/*  */ + "(?:[0-9]+)"// numeric
		/*  */ + "|(?:[a-zA-Z]+)"// alphabets
		/*  */ + "|(?:[" + OP_CHARS + "])(?![" + OP_CHARS + "]|\\z)" // op (not followed by op or EndOfString)
		/**/ + ")"
		/**/ + "|\\G\\z"; // End Of String
	private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_PATTERN_STRING);

	/**
	 * compare versions.
	 *
	 * <p>
	 * args must be pattern of {@link #VERSION_PATTERN_STRING}. otherwise throw {@link IllegalArgumentException}.
	 * args must not contains over-two-length-op (like ".~" or "..").
	 * </p>
	 * <p>
	 * Version's priority is (numeric &gt; alphabets &gt; op).
	 * This means &quot;0&quot; is larger version than &quot;a&quot;,
	 * and &quot;a&quot; is larger than &quot;-&quot;
	 * </p>
	 * <p>
	 * Numeric is compared as not string but integer. Alphabets is compared as string.
	 * Op has priorities ('+' &lt; '.' &lt; (end of string) &lt; '_' &lt; '-' &lt; '~').
	 * </p>
	 * <p>
	 * This method's strategy is first-match.
	 * <code>compareVersion("0.1.0", "0.2...#illegal#...0")</code> returns -1.
	 * If you check version is valid, you should use {@link #isValidVersion(String)}
	 * </p>
	 *
	 * @param a one
	 * @param b the other
	 * @return if a is equal with b, return 0.
	 * if a is smaller than b, return negative integer.
	 * otherwise, return positive integer.
	 */
	public static int compareVersion(String a, String b) {
		if (a.equals(b)) {
			return 0;
		}

		// snapshot is before -(alpha|beta|...)
		Matcher srcMatcher = VERSION_PATTERN.matcher(a.replace("-SNAPSHOT", "~snapshot"));
		Matcher tgtMatcher = VERSION_PATTERN.matcher(b.replace("-SNAPSHOT", "~snapshot"));

		while (true) { // [:an:][~-_.+][:an:]!
			String srcGroup;
			if (srcMatcher.find()) { // Is next region valid?
				srcGroup = srcMatcher.group(); // get region
				if (srcGroup.isEmpty()) { // check End Of String
					srcGroup = "!"; // virtual op
				}
			} else {
				throw new IllegalArgumentException("Illegal version string: " + a);
			}
			String tgtGroup;
			if (tgtMatcher.find()) { // Is next region valid?
				tgtGroup = tgtMatcher.group(); // get region
				if (tgtGroup.isEmpty()) { // check End Of String
					tgtGroup = "!"; // virtual op
				}
			} else {
				throw new IllegalArgumentException("Illegal version string: " + b);
			}

			VersionComponentType srcType = getVersionComponentType(srcGroup.charAt(0));
			VersionComponentType tgtType = getVersionComponentType(tgtGroup.charAt(0));

			switch (srcType) {
				case NUMERIC:
					if (tgtType == VersionComponentType.NUMERIC) {
						int srcVersionNumeric = Integer.parseInt(srcGroup);
						int tgtVersionNumeric = Integer.parseInt(tgtGroup);
						int compare = srcVersionNumeric - tgtVersionNumeric;
						if (compare != 0) {
							return compare;
						}
					} else {
						// num > other
						return 1;
					}
					break;
				case ALPHABETS:
					if (tgtType == VersionComponentType.NUMERIC) {
						// alphabets < numeric
						return -1;
					} else if (tgtType == VersionComponentType.ALPHABETS) {
						int compare = srcGroup.compareTo(tgtGroup);
						if (compare != 0) {
							return compare;
						}
					} else {
						// alphabets > op
						return 1;
					}
					break;
				case OP:
					if (tgtType != VersionComponentType.OP) {
						// op < others
						return -1;
					} else {
						int srcPriorityOfOperator = getPriorityOfOperator(srcGroup);
						int tgtPriorityOfOperator = getPriorityOfOperator(tgtGroup);
						int compare = srcPriorityOfOperator - tgtPriorityOfOperator;
						if (compare != 0) {
							return compare;
						}
					}
					break;
				default:
					throw new AssertionError("Not implemented");
			}
		}
	}

	private static int getPriorityOfOperator(String opChar) {
		if (opChar.length() != 1) {
			throw new AssertionError("opChar must be one-length-string");
		}
		char c = opChar.charAt(0);
		switch (c) {
			case '~':
				return -3;
			case '-':
				return -2;
			case '_':
				return -1;
			case '!':
				return 0;
			case '.':
				return 1;
			case '+':
				return 2;
			default:
				throw new AssertionError("opChar must be one of [~-_!.+]");
		}
	}

	private static VersionComponentType getVersionComponentType(char srcFirstChar) {
		if (srcFirstChar >= '0' && srcFirstChar <= '9') { // numeric
			return VersionComponentType.NUMERIC;
		} else if ((srcFirstChar >= 'a' && srcFirstChar <= 'z')
				|| (srcFirstChar >= 'A' && srcFirstChar <= 'Z')) {
			return VersionComponentType.ALPHABETS;
		} else {
			return VersionComponentType.OP;
		}
	}

	/**
	 * check if version is valid.
	 *
	 * @param version version string
	 * @return is valid version
	 * @see #compareVersion(String, String)
	 */
	public static boolean isValidVersion(String version) {
		Matcher srcMatcher = VERSION_PATTERN.matcher(version);
		while (srcMatcher.find()) { // Is next region valid?
			if (srcMatcher.group().isEmpty()) { // check End Of String
				return true;
			}
		}
		return false; // EndOfString returns true, but if illegal version returns false
	}

	private VersionComparator() {
	}
}
