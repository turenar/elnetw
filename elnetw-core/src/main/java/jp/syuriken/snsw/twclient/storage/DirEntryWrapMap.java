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

import java.util.AbstractMap;
import java.util.Set;

/**
 * DirEntry Wrapped Map
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
class DirEntryWrapMap<T> extends AbstractMap<String, T> {
	private final DirEntryImpl target;
	private final Converter<T> converter;

	public DirEntryWrapMap(DirEntryImpl target, Converter<T> converter) {
		this.target = target;
		this.converter = converter;
	}

	@Override
	public Set<Entry<String, T>> entrySet() {
		return new DirEntrySet<>(target, converter);
	}

	@Override
	public T put(String key, T value) {
		target.writeRaw(key, value);
		return null;
	}
}