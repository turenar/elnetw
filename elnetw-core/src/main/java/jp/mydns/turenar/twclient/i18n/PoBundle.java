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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import static jp.mydns.turenar.twclient.ClientConfiguration.UTF8_CHARSET;
import static jp.mydns.turenar.twclient.i18n.PoParser.getRawMessageId;

/**
 * Bundle Instance for *.po
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PoBundle {
	/**
	 * get instance for locale
	 *
	 * @param baseName base name
	 * @param locale   locale
	 * @return PoBundle instance
	 */
	public static PoBundle getInstance(String baseName, Locale locale) {
		Stack<PoBundle> stack = new Stack<>();

		tryGetInstance(stack, baseName + ".po");
		tryGetInstance(stack, baseName + "_base.po");
		tryGetInstance(stack, baseName, Locale.getDefault());
		if (locale != null) {
			tryGetInstance(stack, baseName, locale);
		}
		return stack.peek();
	}

	private static void tryGetInstance(Stack<PoBundle> stack, String baseName, Locale locale) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		if (language != null) {
			tryGetInstance(stack, baseName + "_" + language + ".po");
			if (country != null) {
				tryGetInstance(stack, baseName + "_" + language + "_" + country + ".po");
				if (variant != null) {
					tryGetInstance(stack, baseName + "_" + language + "_" + country + "_" + variant + ".po");
				}
			}
		}
	}

	private static void tryGetInstance(Stack<PoBundle> stack, String resourcePath) {
		try {
			stack.push(new PoBundle(resourcePath, stack.isEmpty() ? null : stack.peek()));
		} catch (IOException | IllegalArgumentException e) {
			// do nothing
		}
	}

	private PoBundle parent;
	private Map<String, String> bundleMap;

	/**
	 * create instance
	 *
	 * @param resourcePath resource path (*.po)
	 * @throws IOException error occurred
	 */
	public PoBundle(String resourcePath) throws IOException {
		InputStream stream = PoBundle.class.getClassLoader().getResourceAsStream(resourcePath);
		if (stream == null) {
			throw new IllegalArgumentException("missing resource!");
		}
		bundleMap = PoParser.parse(new InputStreamReader(stream, UTF8_CHARSET));
	}

	/**
	 * create instance
	 *
	 * @param resourcePath resource path (*.po)
	 * @param parent       parent PoBundle
	 * @throws IOException error occurred
	 */
	public PoBundle(String resourcePath, PoBundle parent) throws IOException {
		this(resourcePath);
		this.parent = parent;
	}

	private String format(String messageId, Object... arg) {
		return String.format(getFormat(messageId), arg);
	}

	private void format(StringBuilder builder, String format, Object[] args) {
		new Formatter(builder).format(getFormat(format), args);
	}

	private String getFormat(String messageId) {
		String messageString = bundleMap.get(messageId);
		if (messageString == null) {
			if (parent != null) {
				messageString = parent.getFormat(messageId);
			}
			if (messageString == null) {
				messageString = getRawMessageId(messageId);
			}
		}
		return messageString;
	}


	/**
	 * set parent
	 *
	 * @param parent parent PoBundle
	 */
	public void setParent(PoBundle parent) {
		this.parent = parent;
	}

	/**
	 * Translate and format
	 *
	 * @param format original format text
	 * @param args   format arguments
	 * @return formatted text
	 */
	public String tr(String format, Object... args) {
		return format(format, args);
	}

	/**
	 * Translate and format
	 *
	 * @param builder append to
	 * @param format  original format text
	 * @param args    format arguments
	 */
	public void trb(StringBuilder builder, String format, Object... args) {
		format(builder, format, args);
	}

	/**
	 * Translate and format
	 *
	 * @param builder append to
	 * @param format  original format text
	 * @param comment format comment
	 * @param args    format arguments
	 */
	public void trbc(StringBuilder builder, String comment, String format, Object... args) {
		format(builder, PoParser.getMessageId(comment, format), args);
	}

	/**
	 * Translate and format
	 *
	 * @param format  original format text
	 * @param comment format comment
	 * @param args    format arguments
	 * @return formatted text
	 */
	public String trc(String comment, String format, Object... args) {
		return format(PoParser.getMessageId(comment, format), args);
	}
}
