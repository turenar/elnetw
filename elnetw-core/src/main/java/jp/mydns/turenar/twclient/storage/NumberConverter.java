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

package jp.mydns.turenar.twclient.storage;

/**
 * Number converter for generic instance
 *
 * @param <T> ex. Long, Integer
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class NumberConverter<T extends Number> implements Converter<T> {
	/**
	 * check long value is in range?
	 *
	 * @param l long value
	 * @return l can be shown as T
	 */
	protected abstract boolean checkRange(long l);

	@Override
	public T convert(Object obj) {
		if (instanceOf(obj)) {
			return DirEntryImpl.cast(obj);
		} else if (obj instanceof Number) {
			long l = ((Number) obj).longValue();
			if (checkRange(l)) {
				return getNumber(l);
			} else {
				throw new ClassCastException(l + " cannot convert into int");
			}
		} else if (obj instanceof String) {
			return getNumber((String) obj);
		} else {
			return getNumber(obj.toString());
		}
	}

	/**
	 * get number instance from string
	 *
	 * @param obj string instance
	 * @return Number instance
	 */
	protected abstract T getNumber(String obj);

	/**
	 * get number instance from long
	 *
	 * @param l long
	 * @return Number instance
	 */
	protected abstract T getNumber(long l);

	/**
	 * check if obj is T?
	 *
	 * @param obj obj
	 * @return obj instanceof T
	 */
	protected abstract boolean instanceOf(Object obj);
}
