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

package jp.syuriken.snsw.twclient.filter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.filter.query.FilterDispatcherBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * Created with IntelliJ IDEA.
 * Date: 6/23/14
 * Time: 2:45 PM
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QueryFilter extends AbstractMessageFilter implements PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(QueryFilter.class);
	/**
	 * property name for global filter
	 */
	public static final String PROPERTY_KEY_FILTER_GLOBAL_QUERY = "core.filter._global";
	private final ClientConfiguration configuration;
	private final ClientProperties configProperties;
	private final String queryPropertyKey;
	private volatile FilterDispatcherBase query;

	public QueryFilter(String queryPropertyKey) {
		this.queryPropertyKey = queryPropertyKey;
		configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		configProperties.addPropertyChangedListener(this);
		initFilterQueries();
	}

	@Override
	protected boolean filterStatus(Status status) {
		return query.filter(status);
	}

	@Override
	protected boolean filterUser(long userId) {
		return false;
	}

	private void initFilterQueries() {
		String query = configProperties.getProperty(queryPropertyKey);
		if (query == null || query.trim().isEmpty()) {
			this.query = NullFilter.getInstance();
		} else {
			try {
				this.query = FilterCompiler.getCompiledObject(query);
			} catch (IllegalSyntaxException e) {
				logger.warn("#initFilterQueries()", e);
				this.query = NullFilter.getInstance();
			}
		}
	}

	@Override
	public void onDirectMessage(DirectMessage message) {
		if (!query.filter(message)) {
			child.onDirectMessage(message);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(queryPropertyKey)) {
			initFilterQueries();
		}
	}
}
