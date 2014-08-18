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
import twitter4j.EntitySupport;
import twitter4j.URLEntity;

/**
 * query property for url
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class HasUrlProperty extends AbstractEntitySupportProperty<URLEntity> {

	/**
	 * instance
	 *
	 * @param name     property name
	 * @param operator op string
	 * @param value    op value
	 * @throws jp.syuriken.snsw.twclient.filter.IllegalSyntaxException illegal or missing operator or value
	 */
	public HasUrlProperty(String name, String operator, Object value) throws IllegalSyntaxException {
		super(name, operator, value);
	}

	@Override
	protected URLEntity[] getEntities(EntitySupport entitySupport) {
		return entitySupport.getURLEntities();
	}

	@Override
	protected String getTextOfEntity(URLEntity entity) {
		return entity.getExpandedURL();
	}
}
