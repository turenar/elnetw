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
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONObject;

import static org.json.JSONObject.NULL;

/**
 * Dir Entry implementation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DirEntryImpl implements DirEntry {
	/**
	 * cast for generic
	 *
	 * @param o   object
	 * @param <T> cast target
	 * @return object
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T cast(Object o) {
		return (T) o;
	}

	private final String path;
	private final DirEntryImpl parent;
	private final JSONObject jsonObject;

	/**
	 * instance
	 *
	 * @param path     path
	 * @param parent   parent DirEntry
	 * @param dirEntry real object
	 */
	public DirEntryImpl(String path, DirEntryImpl parent, JSONObject dirEntry) {
		this.path = path;
		this.parent = parent;
		this.jsonObject = dirEntry;
	}

	private String basename(String path) {
		int indexOf = path.lastIndexOf('/');
		return indexOf < 0 ? path : path.substring(indexOf + 1);
	}

	@Override
	public boolean exists(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, false);
		String basename = basename(path);
		return parentDirectory != null && parentDirectory.jsonObject.has(basename);
	}

	@Override
	public DirEntryImpl getDirEntry(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		JSONObject jsonObject = parentDirectory.jsonObject.optJSONObject(basename);
		return jsonObject == null ? null : new DirEntryImpl(realpath(path), parentDirectory, jsonObject);
	}

	@Override
	public DirEntryImpl getParent() {
		return parent;
	}

	@Override
	public String getPath() {
		return path;
	}

	/**
	 * get raw object from path
	 *
	 * @param path path
	 * @return raw object
	 */
	protected Object getRaw(String path) {
		return jsonObject.get(path);
	}

	@Override
	public DirEntryImpl getRoot() {
		DirEntryImpl element = this;
		while (element.parent != null) {
			element = element.parent;
		}
		return element;
	}

	@Override
	public boolean isDirEntry(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, false);
		String basename = basename(path);
		return parentDirectory != null && parentDirectory.jsonObject.optJSONObject(basename) != null;
	}

	@Override
	public DirEntryImpl mkdir(String path, boolean recursive) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, recursive, true);
		String basename = basename(path);
		if (!parentDirectory.jsonObject.has(basename)) {
			parentDirectory.jsonObject.put(basename, new JSONObject());
		}
		return new DirEntryImpl(path, parentDirectory, parentDirectory.jsonObject.getJSONObject(basename));


	}

	@Override
	public DirEntryImpl mkdir(String path) {
		return mkdir(path, false);
	}

	@Override
	public DirEntry newMap(String path) {
		mkdir(path);
		return this;
	}

	@Override
	public boolean readBool(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return parentDirectory.jsonObject.getBoolean(basename);
	}

	/**
	 * read directory
	 *
	 * @return iterator
	 */
	@SuppressWarnings("unchecked")
	protected Iterator<String> readDir() {
		return (Iterator<String>) jsonObject.keys();
	}

	@Override
	public int readInt(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return parentDirectory.jsonObject.getInt(basename);
	}

	@Override
	public List<Integer> readIntList(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return wrapList(parentDirectory.jsonObject.optJSONArray(basename), new IntegerConverter());
	}

	@Override
	public Map<String, Integer> readIntMap(String path) {
		return new DirEntryWrapMap<>(getDirEntry(path), new IntegerConverter());
	}

	@Override
	public List<Object> readList(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return wrapList(parentDirectory.jsonObject.optJSONArray(basename), new ObjectConverter());
	}

	@Override
	public long readLong(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return parentDirectory.jsonObject.getLong(basename);
	}

	@Override
	public long readLong(String path, long defaultValue) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return parentDirectory.jsonObject.optLong(basename, defaultValue);
	}

	@Override
	public List<Long> readLongList(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return wrapList(parentDirectory.jsonObject.optJSONArray(basename), new LongConverter());
	}

	@Override
	public Map<String, Long> readLongMap(String path) {
		return new DirEntryWrapMap<>(getDirEntry(path), new LongConverter());
	}

	@Override
	public String readString(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return parentDirectory.jsonObject.optString(basename, null);
	}

	@Override
	public List<String> readStringList(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		return wrapList(parentDirectory.jsonObject.optJSONArray(basename), new StringConverter());
	}

	@Override
	public Map<String, String> readStringMap(String path) {
		return new DirEntryWrapMap<>(getDirEntry(path), new StringConverter());
	}

	@Override
	public String realpath(String path) {
		return this.path + "/" + path;
	}

	@Override
	public boolean remove(String path) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		Object obj = parentDirectory.jsonObject.get(basename);
		if (obj instanceof JSONObject) {
			throw new UnsupportedOperationException(basename + " is dir entry");
		} else {
			return parentDirectory.jsonObject.remove(basename) != null;
		}
	}

	@Override
	public boolean rmdir(String path) {
		return rmdir(path, false);
	}

	@Override
	public boolean rmdir(String path, boolean recursive) {
		DirEntryImpl parentDirectory;
		parentDirectory = traverseDirEntry(this, path, false, !recursive);

		if (parentDirectory == null) {
			return false;
		}

		String basename = basename(path);
		Object obj = parentDirectory.jsonObject.opt(basename);
		if (obj instanceof JSONObject) {
			if (recursive || ((JSONObject) obj).length() == 0) {
				parentDirectory.jsonObject.remove(basename);
				return true;
			} else {
				return false;
			}
		/*} else if (obj == null) {
			throw new NoSuchElementException(path + ": not found (" + basename + ")");*/
		} else {
			return false;
		}
	}

	@Override
	public int size() {
		return jsonObject.length();
	}

	@Override
	public String toString() {
		return "DirEntryImpl{path=" + path + "}";
	}

	@Override
	public Iterator<String> traverse() {
		return cast(jsonObject.keys());
	}

	/**
	 * traverse DirEntry
	 *
	 * @param base          base DirEntry
	 * @param path          path
	 * @param mkdir         create missing dir?
	 * @param noEntryReport report ENOENT?
	 * @return DirEntryImpl
	 */
	protected DirEntryImpl traverseDirEntry(DirEntryImpl base, String path, boolean mkdir, boolean noEntryReport) {
		DirEntryImpl dirEntry;
		if (path.startsWith("/")) {
			dirEntry = getRoot();
		} else {
			dirEntry = base;
		}
		int indexOf = -1; // indexOf + 1 == 0
		do {
			int nextIndexOf = path.indexOf('/', indexOf + 1);
			if (nextIndexOf < 0) {
				break;
			}
			String pathElement = path.substring(indexOf + 1, nextIndexOf);
			if (pathElement.isEmpty() || pathElement.equals(".")) {
				// do nothing
			} else if (pathElement.equals("..")) {
				dirEntry = dirEntry.getParent();
			} else {
				if (!dirEntry.isDirEntry(pathElement)) {
					if (mkdir) {
						dirEntry = dirEntry.mkdir(pathElement, false);
					} else if (noEntryReport) {
						throw new NoSuchElementException(path + ": not found (" + pathElement + ")");
					} else {
						return null;
					}
				} else {
					dirEntry = dirEntry.getDirEntry(pathElement);
				}
			}
			indexOf = nextIndexOf;
		} while (true /*indexOf >= 0*/);
		return dirEntry;
	}

	private <T> List<T> wrapList(JSONArray jsonArray, Converter<T> converter) {
		return jsonArray == null ? null : new ArrayWrapList<>(jsonArray, converter);
	}

	@Override
	public DirEntry writeBool(String path, boolean value) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		parentDirectory.jsonObject.put(basename, value);
		return this;
	}

	@Override
	public DirEntry writeInt(String path, int value) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		parentDirectory.jsonObject.put(basename, value);
		return this;
	}

	@Override
	public DirEntry writeList(String path, Object... elements) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		JSONArray jsonArray = new JSONArray(elements);
		parentDirectory.jsonObject.put(basename, jsonArray);
		return this;
	}

	@Override
	public DirEntry writeLong(String path, long value) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		parentDirectory.jsonObject.put(basename, value);
		return this;
	}

	protected <T> void writeRaw(String key, T value) {
		jsonObject.put(key, value);
	}

	@Override
	public DirEntry writeString(String path, String data) {
		DirEntryImpl parentDirectory = traverseDirEntry(this, path, false, true);
		String basename = basename(path);
		parentDirectory.jsonObject.put(basename, data == null ? NULL : data);
		return this;
	}
}
