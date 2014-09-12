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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ユーザー設定によりフィルタを行うフィルタクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class GlobalUserIdFilter extends AbstractMessageFilter implements PropertyChangeListener {

	/**
	 * property name for user ids
	 */
	public static final String PROPERTY_KEY_FILTER_IDS = "core.filter.user.ids";
	private final ClientConfiguration configuration;
	private final Logger logger = LoggerFactory.getLogger(GlobalUserIdFilter.class);
	private volatile LongHashSet filterIds;


	/**
	 * インスタンスを生成する。
	 */
	public GlobalUserIdFilter() {
		this.configuration = ClientConfiguration.getInstance();
		configuration.getConfigProperties().addPropertyChangedListener(this);
		filterIds = new LongHashSet();
		initFilterIds();
	}

	protected boolean filterUser(long userId) {
		return filterIds.contains(userId);
	}

	private void initFilterIds() {
		List<String> idsList = configuration.getConfigProperties().getList(PROPERTY_KEY_FILTER_IDS);
		LongHashSet filterIds = new LongHashSet();
		for (String userId : idsList) {
			try {
				filterIds.add(Long.parseLong(userId));
			} catch (NumberFormatException e) {
				logger.warn("filterIdsの読み込み中にエラー: {} は数値ではありません", userId);
			}

		}
		this.filterIds = filterIds;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PROPERTY_KEY_FILTER_IDS)) {
			initFilterIds();
		}
	}
}
