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

package jp.syuriken.snsw.twclient.filter.query.prop;

import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.query.QueryOperator;
import jp.syuriken.snsw.twclient.filter.query.QueryProperty;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * abstract query property for boolean argument
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractBoolArgProperty implements QueryProperty {
	/**
	 * operator
	 */
	protected final QueryOperator operatorType;
	/**
	 * operator value
	 */
	protected final boolean value;

	/**
	 * instance
	 *
	 * @param name     name
	 * @param operator op string
	 * @param value    op value
	 * @throws IllegalSyntaxException missing or illegal operator or value
	 */
	public AbstractBoolArgProperty(String name, String operator, Object value) throws IllegalSyntaxException {
		// operator 処理
		if (operator == null) {
			operatorType = QueryOperator.IS;
		} else {
			operatorType = QueryOperator.compileOperatorBool(operator);
		}
		if (value == null || value instanceof Boolean) {
			// value 処理
			if (!(operatorType == QueryOperator.IS || operatorType == QueryOperator.IS_NOT)) {
				if (value == null) {
					throw new IllegalSyntaxException("[" + name + "] 比較値が必要です");
				} else {
					this.value = (Boolean) value;
				}
			} else {
				this.value = false; // init because this field is final
			}
		} else {
			throw new IllegalSyntaxException("[" + name + "] 値がbool型ではありません");
		}
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return operatorType.compare(getPropertyValue(directMessage), value);
	}

	@Override
	public boolean filter(Status status) {
		return operatorType.compare(getPropertyValue(status), value);
	}

	/**
	 * property value
	 *
	 * @param status status
	 * @return value
	 */
	protected abstract boolean getPropertyValue(Status status);

	/**
	 * property value
	 *
	 * @param directMessage direct message
	 * @return value
	 */
	protected abstract boolean getPropertyValue(DirectMessage directMessage);
}
