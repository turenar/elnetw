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
 * Virtual Directory Entry
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface FileEntry extends StorageEntry {
	/**
	 * read bool from path
	 *
	 * @return boolean
	 */
	boolean readBool();

	/**
	 * read int from path
	 *
	 * @return int
	 */
	int readInt();

	/**
	 * read long from path
	 *
	 * @return long
	 */
	long readLong();

	/**
	 * read long from path
	 *
	 * @param defaultValue value if path is missing
	 * @return long
	 */
	long readLong(long defaultValue);

	/**
	 * read string from path
	 *
	 * @return String
	 */
	String readString();

	/**
	 * remove path from database
	 *
	 * @return if it exists
	 */
	boolean remove();

	/**
	 * write bool to path
	 *
	 * @param value value
	 * @return this
	 */
	FileEntry writeBool(boolean value);

	/**
	 * write int to path
	 *
	 * @param value value
	 * @return this
	 */
	FileEntry writeInt(int value);

	/**
	 * write long to path
	 *
	 * @param value value
	 * @return this
	 */
	FileEntry writeLong(long value);

	/**
	 * write String to path
	 *
	 * @param data data
	 * @return this
	 */
	FileEntry writeString(String data);
}
