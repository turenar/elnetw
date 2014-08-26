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

package jp.syuriken.snsw.twclient.storage;

import java.util.AbstractList;

import org.json.JSONArray;

/**
 * json array wrapped as list
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
class ArrayWrapList<T> extends AbstractList<T> {
	private final JSONArray jsonArray;
	private final Converter<T> converter;

	public ArrayWrapList(JSONArray jsonArray, Converter<T> converter) {
		this.jsonArray = jsonArray;
		this.converter = converter;
	}

	@Override
	public void add(int index, T element) {
		jsonArray.put(index, element);
	}

	@Override
	public T get(int index) {
		return converter.convert(jsonArray.get(index));
	}

	@Override
	public T remove(int index) {
		return converter.convert(jsonArray.remove(index));
	}

	@Override
	public T set(int index, T element) {
		Object removed = jsonArray.remove(index);
		jsonArray.put(index, element);
		return converter.convert(removed);
	}

	@Override
	public int size() {
		return jsonArray.length();
	}
}
