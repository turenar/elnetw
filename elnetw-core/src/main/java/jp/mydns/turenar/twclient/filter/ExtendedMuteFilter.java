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

package jp.mydns.turenar.twclient.filter;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.conf.PropertyUpdateEvent;
import jp.mydns.turenar.twclient.conf.PropertyUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * ユーザー設定によりフィルタを行うフィルタクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ExtendedMuteFilter extends AbstractMessageFilter implements PropertyUpdateListener {

	/**
	 * property name for filtering user ids
	 */
	public static final String PROPERTY_KEY_FILTER_CLIENT = "core.filter.clients";
	/**
	 * property name for filtering words
	 */
	public static final String PROPERTY_KEY_FILTER_WORDS = "core.filter.words";
	private final ClientConfiguration configuration;
	private final Logger logger = LoggerFactory.getLogger(ExtendedMuteFilter.class);
	private volatile HashSet<String> clients;
	private volatile Pattern wordPatterns;


	/**
	 * インスタンスを生成する。
	 */
	public ExtendedMuteFilter() {
		this.configuration = ClientConfiguration.getInstance();
		configuration.getConfigProperties().addPropertyUpdatedListener(this);
		initFilter();
	}

	@Override
	public ExtendedMuteFilter clone() throws CloneNotSupportedException {
		ExtendedMuteFilter clone = (ExtendedMuteFilter) super.clone();
		configuration.getConfigProperties().addPropertyUpdatedListener(clone);
		return clone;
	}

	/**
	 * filter client text
	 *
	 * @param client client
	 * @return client
	 */
	protected boolean filterClient(String client) {
		int startTagEnd = client.indexOf('>');
		int endTagStart = client.lastIndexOf('<');
		String clientName = startTagEnd < 0 ? client : client.substring(startTagEnd + 1, endTagStart);
		return clients.contains(clientName);
	}

	@Override
	protected boolean filterStatus(Status status) {
		return filterClient(status.getSource()) || filterWord(status.getText());
	}

	@Override
	protected boolean filterUser(long userId) {
		return false;
	}

	/**
	 * filter words
	 *
	 * @param text text
	 * @return filtered?
	 */
	protected boolean filterWord(String text) {
		return wordPatterns != null && wordPatterns.matcher(text).find();
	}

	private void initFilter() {
		ClientProperties configProperties = configuration.getConfigProperties();
		this.clients = new HashSet<>(configProperties.getList(PROPERTY_KEY_FILTER_CLIENT));

		List<String> wordList = configProperties.getList(PROPERTY_KEY_FILTER_WORDS);
		if (!wordList.isEmpty()) {
			StringBuilder wordMuteBuilder = new StringBuilder();
			for (int i = 0, wordListSize = wordList.size(); i < wordListSize; i++) {
				String word = wordList.get(i);
				try {
					// check compilable word
					Pattern pattern = Pattern.compile(word);
					if (wordMuteBuilder.length() != 0) {
						wordMuteBuilder.append('|');
					}
					wordMuteBuilder.append('(')
							.append(pattern)
							.append(')');
				} catch (PatternSyntaxException e) {
					onException(
							new RuntimeException(tr("Illegal regex in %s[%d]", PROPERTY_KEY_FILTER_WORDS, i), e));
					logger.warn("Illegal regex syntax: {}", word);
				}
			}
			this.wordPatterns = Pattern.compile(wordMuteBuilder.toString());
		} else {
			this.wordPatterns = null;
		}
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		if (!filterWord(message.getText())) {
			child.onDirectMessage(message);
		}
	}

	@Override
	public void propertyUpdate(PropertyUpdateEvent evt) {
		switch (evt.getPropertyName()) {
			case PROPERTY_KEY_FILTER_CLIENT:
			case PROPERTY_KEY_FILTER_WORDS:
				initFilter();
				break;
			default:
				// do nothing
		}
	}
}
