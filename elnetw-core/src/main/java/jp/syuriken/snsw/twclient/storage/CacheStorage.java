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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jp.syuriken.snsw.twclient.ClientConfiguration.UTF8_CHARSET;

/**
 * cache storage
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class CacheStorage implements DirEntry {

	private static final Logger logger = LoggerFactory.getLogger(CacheStorage.class);
	/**
	 * database path
	 */
	protected final Path databasePath;
	/**
	 * real entry: json object
	 */
	protected final JSONObject root;
	/**
	 * root dir entry
	 */
	protected final DirEntryImpl rootDirEntry;

	/**
	 * instance
	 *
	 * @param databasePath database path
	 */
	public CacheStorage(Path databasePath) {
		this.databasePath = databasePath;
		JSONObject root = null;
		if (databasePath != null && Files.exists(databasePath)) {
			try {
				root = new JSONObject(new JSONTokener(Files.newBufferedReader(databasePath, UTF8_CHARSET)));
			} catch (IOException e) {
				logger.error("Failed loading cache db: {}", databasePath, e);
			}
		}
		if (root == null) {
			root = new JSONObject();
		}
		this.root = root;
		rootDirEntry = new DirEntryImpl("/", null, root);
	}

	/**
	 * instance
	 */
	public CacheStorage() {
		this(null);
	}

	@Override
	public DirEntry getDirEntry(String path) {
		return rootDirEntry.getDirEntry(path);
	}

	@Override
	public DirEntryImpl getParent() {
		return null;
	}

	@Override
	public String getPath() {
		return rootDirEntry.getPath();
	}

	@Override
	public DirEntryImpl getRoot() {
		return rootDirEntry;
	}

	@Override
	public boolean isDirEntry(String path) {
		return rootDirEntry.isDirEntry(path);
	}

	@Override
	public DirEntry mkdir(String path, boolean recursive) {
		return rootDirEntry.mkdir(path, recursive);
	}

	@Override
	public DirEntry mkdir(String path) {
		return rootDirEntry.mkdir(path);
	}

	@Override
	public int readInt(String path) {
		return rootDirEntry.readInt(path);
	}

	@Override
	public List<Integer> readIntList(String path) {
		return rootDirEntry.readIntList(path);
	}

	@Override
	public Map<String, Integer> readIntMap(String path) {
		return rootDirEntry.readIntMap(path);
	}

	@Override
	public List<Object> readList(String path) {
		return rootDirEntry.readList(path);
	}

	@Override
	public long readLong(String path) {
		return rootDirEntry.readLong(path);
	}

	@Override
	public long readLong(String path, long defaultValue) {
		return rootDirEntry.readLong(path, defaultValue);
	}

	@Override
	public List<Long> readLongList(String path) {
		return rootDirEntry.readLongList(path);
	}

	@Override
	public Map<String, Long> readLongMap(String path) {
		return rootDirEntry.readLongMap(path);
	}

	@Override
	public String readString(String path) {
		return rootDirEntry.readString(path);
	}

	@Override
	public List<String> readStringList(String path) {
		return rootDirEntry.readStringList(path);
	}

	@Override
	public Map<String, String> readStringMap(String path) {
		return rootDirEntry.readStringMap(path);
	}

	@Override
	public String realpath(String path) {
		return rootDirEntry.realpath(path);
	}

	@Override
	public boolean remove(String path) {
		return rootDirEntry.remove(path);
	}

	@Override
	public boolean rmdir(String path) {
		return rootDirEntry.rmdir(path);
	}

	@Override
	public boolean rmdir(String path, boolean recursive) {
		return rootDirEntry.rmdir(path, recursive);
	}

	@Override
	public int size() {
		return rootDirEntry.size();
	}

	/**
	 * store database
	 *
	 * @throws IOException error occurred
	 */
	public void store() throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(databasePath, UTF8_CHARSET)) {
			root.write(writer);
		}
	}

	@Override
	public Iterator<String> traverse() {
		return rootDirEntry.traverse();
	}

	@Override
	public void writeInt(String path, int value) {
		rootDirEntry.writeInt(path, value);
	}

	@Override
	public void writeList(String path, Object... elements) {
		rootDirEntry.writeList(path, elements);
	}

	@Override
	public void writeLong(String path, long value) {
		rootDirEntry.writeLong(path, value);
	}

	@Override
	public void writeString(String path, String data) {
		rootDirEntry.writeString(path, data);
	}
}
