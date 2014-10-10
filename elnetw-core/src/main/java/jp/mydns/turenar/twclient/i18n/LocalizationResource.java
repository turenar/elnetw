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

import java.util.Locale;

/**
 * Resource Utility class for localization
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class LocalizationResource {

	private static PoBundle bundle = PoBundle.getInstance("jp/mydns/turenar/twclient/i18n/msg", Locale.getDefault());

	/**
	 * Translate and format
	 *
	 * @param format original format text
	 * @param args   format arguments
	 * @return translated and formatted text
	 */
	public static String tr(String format, Object... args) {
		return bundle.tr(format, args);
	}

	/**
	 * Translate and format
	 *
	 * @param builder append to
	 * @param format  original format text
	 * @param args    format arguments
	 * @return builder
	 */
	public static StringBuilder trb(StringBuilder builder, String format, Object... args) {
		bundle.trb(builder, format, args);
		return builder;
	}

	/**
	 * Translate and format
	 *
	 * @param builder append to
	 * @param format  original format text
	 * @param comment format comment
	 * @param args    format arguments
	 * @return builder
	 */
	public static StringBuilder trbc(StringBuilder builder, String comment, String format, Object... args) {
		bundle.trbc(builder, comment, format, args);
		return builder;
	}

	/**
	 * Translate and format
	 *
	 * @param format  original format text
	 * @param comment format comment
	 * @param args    format arguments
	 * @return builder
	 */
	public static String trc(String comment, String format, Object... args) {
		return bundle.trc(comment, format, args);
	}
}
