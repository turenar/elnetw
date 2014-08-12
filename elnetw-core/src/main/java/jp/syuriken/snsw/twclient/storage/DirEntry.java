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
import java.util.List;
import java.util.Map;

/**
 * Virtual Directory Entry
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface DirEntry {
	/**
	 * get dir entry of path
	 *
	 * @param path path
	 * @return DirEntry instance
	 */
	DirEntry getDirEntry(String path);

	/**
	 * get parent DirEntry
	 *
	 * @return DirEntry
	 */
	DirEntry getParent();

	/**
	 * get path of this
	 *
	 * @return path
	 */
	String getPath();

	/**
	 * get root of this
	 *
	 * @return root DirEntry
	 */
	DirEntry getRoot();

	/**
	 * check if path is DirEntry
	 *
	 * @param path path
	 * @return isDirEntry?
	 */
	boolean isDirEntry(String path);

	/**
	 * create DirEntry
	 *
	 * @param path      path
	 * @param recursive if true, create missing DirEntry, too.
	 * @return DirEntry
	 */
	DirEntry mkdir(String path, boolean recursive);

	/**
	 * create DirEntry not recursively
	 *
	 * @param path path
	 * @return DirEntry
	 */
	DirEntry mkdir(String path);

	/**
	 * read int from path
	 *
	 * @param path path
	 * @return int
	 */
	int readInt(String path);

	/**
	 * read List&lt;Integer&gt; from path
	 *
	 * @param path path
	 * @return List&lt;Integer&gt;
	 */
	List<Integer> readIntList(String path);

	/**
	 * read Map&lt;Integer&gt; from path
	 *
	 * @param path path
	 * @return Map&lt;Integer&gt;
	 */
	Map<String, Integer> readIntMap(String path);

	/**
	 * read List&lt;Object&gt; from path
	 *
	 * @param path path
	 * @return List&lt;Object&gt;. it may have DirEntry
	 */
	List<Object> readList(String path);

	/**
	 * read long from path
	 *
	 * @param path path
	 * @return long
	 */
	long readLong(String path);

	/**
	 * read long from path
	 *
	 * @param path         path
	 * @param defaultValue value if path is missing
	 * @return long
	 */
	long readLong(String path, long defaultValue);

	/**
	 * read List&lt;Long&gt; from path
	 *
	 * @param path path
	 * @return List&lt;Long&gt;
	 */
	List<Long> readLongList(String path);

	/**
	 * read Map&lt;Long&gt; from path
	 *
	 * @param path path
	 * @return Map&lt;Long&gt;
	 */
	Map<String, Long> readLongMap(String path);

	/**
	 * read string from path
	 *
	 * @param path
	 * @return
	 */
	String readString(String path);

	/**
	 * read List&lt;String&gt; from path
	 *
	 * @param path path
	 * @return List&lt;String&gt;
	 */
	List<String> readStringList(String path);

	/**
	 * read Map&lt;String&gt; from database
	 *
	 * @param path path
	 * @return Map&lt;String&gt;
	 */
	Map<String, String> readStringMap(String path);

	/**
	 * get real path
	 *
	 * @param path path
	 * @return real path
	 */
	String realpath(String path);

	/**
	 * remove path from database
	 *
	 * @return if it exists
	 */
	boolean remove(String path);

	/**
	 * remove DirEntry
	 *
	 * @param path path
	 * @return if path exists and is DirEntry
	 */
	boolean rmdir(String path);

	/**
	 * remove DirEntry
	 *
	 * @param path      path
	 * @param recursive if true, remove existing DirEntry, too.
	 * @return if path exists and is DirEntry
	 */
	boolean rmdir(String path, boolean recursive);

	/**
	 * get DirEntry size
	 *
	 * @return the number of elements
	 */
	int size();

	/**
	 * get Iterator of DirEntry
	 *
	 * @return all containing path
	 */
	Iterator<String> traverse();

	/**
	 * write int to path
	 *
	 * @param path  path
	 * @param value value
	 */
	void writeInt(String path, int value);

	/**
	 * write list to path
	 *
	 * @param path     path
	 * @param elements elements
	 */
	void writeList(String path, Object... elements);

	/**
	 * write long to path
	 *
	 * @param path  path
	 * @param value value
	 */
	void writeLong(String path, long value);

	/**
	 * write String to path
	 *
	 * @param path path
	 * @param data data
	 */
	void writeString(String path, String data);
}
