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

package jp.mydns.turenar.twclient.bus.channel;

import java.util.Map;
import java.util.WeakHashMap;

import jp.mydns.turenar.twclient.ClientMessageListener;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * ストリームからデータを取得するDataFetcher
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FilterStreamChannel implements MessageChannel {
	private static final WeakHashMap<String, FilterQuery> filterQueryMap = new WeakHashMap<>();
	private static int nextQueryId = 0;

	/**
	 * get channel path from query
	 *
	 * @param query query. all values should be sorted.
	 * @return message bus channel path: should be referenced strongly
	 */
	public static synchronized String getChannelPath(FilterQuery query) {
		for (Map.Entry<String, FilterQuery> queryEntry : filterQueryMap.entrySet()) {
			if (queryEntry.getValue().equals(query)) {
				String key = queryEntry.getKey();
				if (key == null) {
					break;
				} else {
					return key;
				}
			}
		}
		String key = "stream/filter?id=" + (++nextQueryId);
		filterQueryMap.put(key, query);
		return key;
	}

	private final String accountId;
	/**
	 * This strong reference is required. otherwise, even if this channel with the same FilterQuery is running,
	 * multi stream connection may be established
	 */
	private final String queryId;
	private final ClientMessageListener listener;
	private final MessageBus messageBus;
	private final FilterQuery filterQuery;
	private volatile TwitterStream stream;

	/**
	 * create instance
	 *
	 * @param messageBus message bus
	 * @param accountId  account id
	 * @param path       path
	 * @param queryId    argument
	 */
	public FilterStreamChannel(MessageBus messageBus, String accountId, String path, String queryId) {
		this.messageBus = messageBus;
		this.accountId = accountId;
		this.queryId = queryId;
		listener = messageBus.getListeners(accountId, path);
		FilterQuery filterQuery = filterQueryMap.get(path);
		if (filterQuery == null) {
			throw new IllegalArgumentException("query key is not found: " + path);
		}
		this.filterQuery = filterQuery;
	}

	@Override
	public synchronized void connect() {
		if (stream == null) {
			stream = new TwitterStreamFactory(
					messageBus.getTwitterConfiguration(accountId)).getInstance();
			stream.addConnectionLifeCycleListener(listener);
			stream.addListener(listener);
			stream.filter(filterQuery);
		}
	}

	@Override
	public synchronized void disconnect() {
		if (stream != null) {
			stream.shutdown();
			stream = null;
		}
	}

	@Override
	public void establish(ClientMessageListener listener) {
		// suppress unused warning
		listener.onClientMessage("jp.mydns.turenar.twclient.bus.channel.FilterStreamChannel established", queryId);
	}

	@Override
	public void realConnect() {
		// #connect() works.
	}
}
