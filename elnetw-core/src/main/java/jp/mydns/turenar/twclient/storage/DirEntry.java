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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Virtual Directory Entry
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface DirEntry extends StorageEntry, Iterable<String> {
	/**
	 * get dir entry of path
	 *
	 * @param path path
	 * @return DirEntry instance
	 */
	DirEntry getDirEntry(String path);

	/**
	 * get entry for path
	 * @param path path
	 * @return if path is dir, return DirEntry. otherwise (even if path is not exist), return FileEntry
	 */
	StorageEntry getEntry(String path);

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
	 * create new map
	 *
	 * @param path path
	 * @return this
	 */
	DirEntry newMap(String path);

	/**
	 * normalize path such as ./, ../, //
	 *
	 * @param path path
	 * @return normalized path. if path starts with /, normalized path also contains preceding slash.
	 */
	String normalize(String path);

	/**
	 * read bool from path
	 *
	 * @param path path
	 * @return boolean
	 */
	boolean readBool(String path);

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
	 * @param path path
	 * @return String
	 */
	String readString(String path);

	/**
	 * read array of path
	 *
	 * @param path path
	 * @return array. if path has DirEntry, we throw Exception.
	 */
	String[] readStringArray(String path);

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
	 * @param path path
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
	 * return stream which contains all entry in this. sub directories are not included.
	 *
	 * @return stream
	 * @see #walk() to walk entries
	 */
	Stream<StorageEntry> stream();

	/**
	 * get Iterator of DirEntry
	 *
	 * @return all containing path
	 */
	Iterator<String> traverse();

	/**
	 * return stream which contains all entry in this and sub directories.
	 *
	 * @return stream
	 * @see #stream() no sub directories included
	 * @see #walk(int, int)
	 */
	default Stream<StorageEntry> walk() {
		return walk(0, Integer.MAX_VALUE);
	}

	/**
	 * return stream which contains all entry in this and sub directories.
	 *
	 * @param minDepth minimum depth. if 0, this entry is included. if 1, all entries but this are included.
	 *                 if 2, only such as /a/c, /a/c/d are included. and so on.
	 * @param maxDepth maximum depth. if 0, this entry is only included. if 1, don't traverse sub directories.
	 * @return stream
	 * @see #stream() no sub directories included
	 * @see #walk()
	 */
	Stream<StorageEntry> walk(int minDepth, int maxDepth);

	/**
	 * write bool to path
	 *
	 * @param path  path
	 * @param value value
	 * @return this
	 */
	DirEntry writeBool(String path, boolean value);

	/**
	 * write int to path
	 *
	 * @param path  path
	 * @param value value
	 * @return this
	 */
	DirEntry writeInt(String path, int value);

	/**
	 * write list to path
	 *
	 * @param path     path
	 * @param elements elements
	 * @return this
	 */
	DirEntry writeList(String path, Object... elements);

	/**
	 * write long to path
	 *
	 * @param path  path
	 * @param value value
	 * @return this
	 */
	DirEntry writeLong(String path, long value);

	/**
	 * write String to path
	 *
	 * @param path path
	 * @param data data
	 * @return this
	 */
	DirEntry writeString(String path, String data);

	/**
	 * write String[] to path
	 *
	 * @param path path
	 * @param data data
	 * @return this
	 */
	DirEntry writeStringArray(String path, String[] data);
}
