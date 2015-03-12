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
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import jp.mydns.turenar.twclient.internal.ConcurrentSoftHashMap;

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

		tryGetInstance(stack, baseName + ".po", locale);
		tryGetInstance(stack, baseName + "_base.po", locale);
		getInstance(stack, baseName, Locale.getDefault());
		if (locale != null) {
			getInstance(stack, baseName, locale);
		}
		return stack.isEmpty() ? new PoBundle() : stack.peek();
	}

	private static void getInstance(Stack<PoBundle> stack, String baseName, Locale locale) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		if (language != null) {
			tryGetInstance(stack, baseName + "_" + language + ".po", locale);
			if (country != null) {
				tryGetInstance(stack, baseName + "_" + language + "_" + country + ".po", locale);
				if (variant != null) {
					tryGetInstance(stack, baseName + "_" + language + "_" + country + "_" + variant + ".po", locale);
				}
			}
		}
	}


	private static void tryGetInstance(Stack<PoBundle> stack, String resourcePath, Locale locale) {
		try {
			stack.push(new PoBundle(resourcePath, stack.isEmpty() ? null : stack.peek(), locale));
		} catch (IOException | IllegalArgumentException e) {
			// do nothing
		}
	}

	private final Locale locale;
	private PoBundle parent;
	private Map<String, String> bundleMap;
	private ConcurrentSoftHashMap<String, MessageFormatter> formatCacheMap = new ConcurrentSoftHashMap<>();

	/**
	 * create instance
	 *
	 * @param resourcePath resource path (*.po)
	 * @throws IOException error occurred
	 */
	public PoBundle(String resourcePath) throws IOException {
		this(resourcePath, null);
	}

	/**
	 * create instance
	 *
	 * @param resourcePath resource path (*.po)
	 * @param parent       parent PoBundle
	 * @throws IOException error occurred
	 */
	public PoBundle(String resourcePath, PoBundle parent) throws IOException {
		this(resourcePath, parent, Locale.getDefault());
	}

	/**
	 * create instance
	 *
	 * @param resourcePath resource path (*.po)
	 * @param parent       parent poBundle
	 * @param locale       locale
	 * @throws IOException error occurred
	 */
	public PoBundle(String resourcePath, PoBundle parent, Locale locale) throws IOException {
		this(parent, locale);
		InputStream stream = PoBundle.class.getClassLoader().getResourceAsStream(resourcePath);
		if (stream == null) {
			throw new IllegalArgumentException("missing resource!");
		}
		bundleMap = PoParser.parse(new InputStreamReader(stream, UTF8_CHARSET));
	}

	/**
	 * create instance
	 *
	 * @param parent parent PoBundle
	 * @param locale locale
	 */
	public PoBundle(PoBundle parent, Locale locale) {
		this.parent = parent;
		this.locale = locale;
		bundleMap = Collections.emptyMap();
	}

	/**
	 * create instance
	 *
	 * @param parent parent PoBundle
	 */
	public PoBundle(PoBundle parent) {
		this(parent, Locale.getDefault());
	}

	/**
	 * create instance
	 */
	public PoBundle() {
		this((PoBundle) null);
	}

	private String format(String messageId, Object... arg) {
		return getFormat(messageId).format(arg);
	}

	private void format(StringBuilder builder, String messageId, Object[] args) {
		getFormat(messageId).format(builder, args);
	}

	private MessageFormatter getFormat(String messageId) {
		MessageFormatter formatter = formatCacheMap.get(messageId);
		if (formatter == null) {
			formatter = new MessageFormatter(locale, getFormatString(messageId));
			formatCacheMap.put(messageId, formatter);
		}
		return formatter;
	}

	private String getFormatString(String messageId) {
		String messageString = bundleMap.get(messageId);
		if (messageString == null) {
			if (parent != null) {
				messageString = parent.getFormatString(messageId);
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
