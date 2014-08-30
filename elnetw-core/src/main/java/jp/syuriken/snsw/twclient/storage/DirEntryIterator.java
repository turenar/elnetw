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

import java.util.Iterator;
import java.util.Map;

/**
 * DirEntry Iterator
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
class DirEntryIterator<T> implements Iterator<Map.Entry<String, T>> {

	private class EntryImpl implements Map.Entry<String, T> {
		private final String path;
		private final Converter<T> converter;

		public EntryImpl(String path, Converter<T> converter) {
			this.path = path;
			this.converter = converter;
		}

		@Override
		public String getKey() {
			return path;
		}

		@Override
		public T getValue() {
			return converter.convert(target.getRaw(path));
		}

		@Override
		public T setValue(T value) {
			return null;
		}
	}

	protected final DirEntryImpl target;
	protected final Converter<T> converter;
	protected final Iterator<String> keys;


	public DirEntryIterator(DirEntryImpl target, Converter<T> converter) {
		this.target = target;
		this.converter = converter;
		keys = target.readDir();
	}

	@Override
	public boolean hasNext() {
		return keys.hasNext();
	}

	@Override
	public Map.Entry<String, T> next() {
		String nextKey = keys.next();
		return new EntryImpl(nextKey, converter);
	}

	@Override
	public void remove() {
		keys.remove();
	}
}
