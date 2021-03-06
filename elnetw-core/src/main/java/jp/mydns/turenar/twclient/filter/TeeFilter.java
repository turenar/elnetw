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

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.filter.delayed.QueryFilter;
import jp.mydns.turenar.twclient.gui.tab.TabRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.mydns.turenar.twclient.filter.delayed.QueryFilter.PROPERTY_KEY_FILTER_GLOBAL_QUERY;

/**
 * 入力 → {@link jp.mydns.turenar.twclient.filter.query.QueryDispatcherBase} → {@link TabRenderer} とするユーティリティークラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TeeFilter extends AbstractMessageFilter implements TabRenderer {
	private static final Logger logger = LoggerFactory.getLogger(TeeFilter.class);
	private final String queryPropertyKey;
	private TabRenderer renderer;
	private ClientConfiguration configuration;

	/**
	 * インスタンスを生成する。グローバルフィルタを使用する。
	 *
	 * @param uniqId      ユニークなID
	 * @param tabRenderer 移譲先レンダラ
	 */
	public TeeFilter(String uniqId, TabRenderer tabRenderer) {
		this(uniqId, tabRenderer, true);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param uniqId          ユニークなID
	 * @param tabRenderer     移譲先レンダラ
	 * @param useGlobalFilter グローバルフィルタを使用するかどうか
	 */
	public TeeFilter(String uniqId, TabRenderer tabRenderer, boolean useGlobalFilter) {
		configuration = ClientConfiguration.getInstance();
		renderer = tabRenderer;
		queryPropertyKey = getQueryPropertyKey(uniqId);

		if (useGlobalFilter) {
			try {
				addChild(configuration.getFilters());
				addChild(new QueryFilter(tabRenderer.getUserId(), PROPERTY_KEY_FILTER_GLOBAL_QUERY));
			} catch (CloneNotSupportedException e) {
				logger.error("failed to filter.clone()", e);
			}
		}
		addChild(new QueryFilter(tabRenderer.getUserId(), queryPropertyKey));
		addChild(tabRenderer);
	}

	@Override
	public final TeeFilter clone() throws CloneNotSupportedException { // CS-IGNORE
		throw new CloneNotSupportedException("TeeFilter doesn't support #clone()");
	}

	@Override
	protected boolean filterUser(long userId) {
		return false;
	}

	/**
	 * get query property key for tab
	 *
	 * @param uniqId unique id
	 * @return query property key
	 */
	public String getQueryPropertyKey(String uniqId) {
		return "gui.tabs.data." + uniqId + ".filter.query";
	}

	@Override
	public String getUserId() {
		return renderer.getUserId();
	}

	@Override
	public void onDisplayRequirement() {
		renderer.onDisplayRequirement();
	}
}
