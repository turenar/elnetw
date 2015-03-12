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

package jp.mydns.turenar.twclient.filter.query.prop;

import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.query.QueryOperator;
import jp.mydns.turenar.twclient.filter.query.QueryProperty;
import twitter4j.DirectMessage;
import twitter4j.EntitySupport;
import twitter4j.Status;
import twitter4j.TweetEntity;

/**
 * template for entity filtering
 *
 * @param <T> TweetEntity subclass
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractEntitySupportProperty<T extends TweetEntity> implements QueryProperty {
	/**
	 * operator
	 */
	protected final QueryOperator operatorType;
	/**
	 * value
	 */
	protected final Object value;

	/**
	 * make instance
	 *
	 * @param name     property name
	 * @param operator operator string
	 * @param value    value object
	 * @throws IllegalSyntaxException illegal name/value
	 */
	public AbstractEntitySupportProperty(String name, String operator, Object value) throws IllegalSyntaxException {
		// operator 処理
		if (operator == null) {
			throw new IllegalSyntaxException("[" + name + "] string演算子が必要です");
		}

		operatorType = QueryOperator.compileOperatorString(operator);
		if (operatorType == null) {
			throw new IllegalSyntaxException("[" + name + "] 正しくないstring演算子です: " + operator);
		}
		// value 処理
		if (value instanceof String) {
			this.value = QueryOperator.compileValueString((String) value);
		} else {
			throw new IllegalSyntaxException("[" + name + "] 正しくないstring値です: " + operator);
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return filterEntity(directMessage);
	}

	@Override
	public boolean filter(Status status) {
		return filterEntity(status);
	}

	/**
	 * filter entity by entity support
	 *
	 * @param entitySupport entity support instance
	 * @return should be filter?
	 */
	public boolean filterEntity(EntitySupport entitySupport) {
		T[] entities = getEntities(entitySupport);
		if (entities != null) {
			for (T entity : entities) {
				if (operatorType.compare(getTextOfEntity(entity), value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * get entities to filter
	 *
	 * @param entitySupport entity support instance
	 * @return entities to filter.
	 */
	protected abstract T[] getEntities(EntitySupport entitySupport);

	/**
	 * get text of entity for filtering
	 *
	 * @param entity entity
	 * @return text
	 */
	protected String getTextOfEntity(T entity) {
		return entity.getText();
	}

	@Override
	public void init() {
	}
}

