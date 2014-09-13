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

package jp.mydns.turenar.twclient.filter.delayed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.NullQueryDispatcher;
import jp.mydns.turenar.twclient.filter.query.QueryCompiler;
import jp.mydns.turenar.twclient.filter.query.QueryController;
import jp.mydns.turenar.twclient.filter.query.QueryDispatcherBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * Query Filter: handles QueryDispatcherBase just as normal filter
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QueryFilter extends DelayedFilter implements PropertyChangeListener, QueryController {
	private static final Logger logger = LoggerFactory.getLogger(QueryFilter.class);
	/**
	 * property name for global filter
	 */
	public static final String PROPERTY_KEY_FILTER_GLOBAL_QUERY = "core.filter._global";
	protected final ArrayList<QueryDispatcherBase> delayers = new ArrayList<>();
	private final ClientProperties configProperties;
	private final String userId;
	private final String queryPropertyKey;
	private volatile QueryDispatcherBase query;

	/**
	 * make instance
	 *
	 * @param userId           target user
	 * @param queryPropertyKey configProperties' property key
	 */
	public QueryFilter(String userId, String queryPropertyKey) {
		this.userId = userId;
		this.queryPropertyKey = queryPropertyKey;
		configProperties = ClientConfiguration.getInstance().getConfigProperties();
		configProperties.addPropertyChangedListener(this);
		initFilterQueries();
		stopDelay();
	}

	@Override
	public void disableDelay(QueryDispatcherBase delayer) {
		delayers.remove(delayer);
		if (delayers.isEmpty()) {
			stopDelay();
		}
	}

	@Override
	public void enableDelay(QueryDispatcherBase delayer) {
		delayers.add(delayer);
		startDelay();
	}

	@Override
	protected boolean filterStatus(Status status) {
		return query.filter(status);
	}

	@Override
	protected boolean filterUser(long userId) {
		return false;
	}

	@Override
	public String getTargetUserId() {
		return userId;
	}

	private void initFilterQueries() {
		String query = configProperties.getProperty(queryPropertyKey);
		if (query == null || query.trim().isEmpty()) {
			this.query = NullQueryDispatcher.getInstance();
		} else {
			try {
				this.query = QueryCompiler.getCompiledObject(query, this);
			} catch (IllegalSyntaxException e) {
				logger.warn("#initFilterQueries()", e);
				this.query = NullQueryDispatcher.getInstance();
			}
		}
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		if (name.equals(INIT_UI)) {
			query.init();
		}
		super.onClientMessage(name, arg);
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		if (isStarted) {
			if (!query.filter(message)) {
				child.onDirectMessage(message);
			}
		} else {
			filteringQueue.add(new DelayedOnDirectMessage(this, message));
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(queryPropertyKey)) {
			initFilterQueries();
		}
	}
}
