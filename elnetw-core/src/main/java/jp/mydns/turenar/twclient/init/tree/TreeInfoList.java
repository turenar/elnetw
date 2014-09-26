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

package jp.mydns.turenar.twclient.init.tree;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * List which contains TreeInitInfo.
 *
 * <p>
 * This class should be called as follow statement.
 * </p>
 * <p>
 * Phase Init: You should call {@link #add(TreeInitInfoBase)}. If you want to retrieve next info to init,
 * You can call {@link #resort()} and {@link #next()}
 * </p>
 * <p>
 * Phase UnInit: You can call {@link #prev()}. You must not call {@link #add(TreeInitInfoBase)} and {@link #next()}.
 * {@link #resort()} have no effect.
 * </p>
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ class TreeInfoList extends AbstractList<TreeInitInfoBase> implements RandomAccess {
	private TreeInitInfoBase[] objects;
	private int tableSize;
	private int size;
	private int finishedIndex;

	/**
	 * make instance
	 */
	public TreeInfoList() {
		tableSize = 16;
		objects = new TreeInitInfoBase[tableSize];
	}

	@Override
	public boolean add(TreeInitInfoBase treeInitInfo) {
		if (tableSize == 0) {
			throw new IllegalStateException(); // prev called
		}
		if (contains(treeInitInfo)) {
			return false;
		}
		if (size >= tableSize) {
			TreeInitInfoBase[] oldTable = objects;
			objects = new TreeInitInfoBase[tableSize <<= 1];
			System.arraycopy(oldTable, 0, objects, 0, size);
		}
		objects[size++] = treeInitInfo;
		return true;
	}

	@Override
	public TreeInitInfoBase get(int index) {
		if (tableSize == 0) {
			throw new IllegalStateException();
		} else if (index < 0 || index >= size) {
			throw new NoSuchElementException();
		}
		return objects[index];
	}

	/**
	 * get next info to init which all dependencies are resolved.
	 *
	 * @return info or null
	 */
	public TreeInitInfoBase next() {
		if (finishedIndex >= size) {
			return null;
		}
		TreeInitInfoBase next = get(finishedIndex);
		if (next.isAllDependenciesResolved()) {
			finishedIndex++;
			return next;
		} else {
			return null;
		}
	}

	/**
	 * get info to uninit
	 *
	 * @return info or null
	 */
	public TreeInitInfoBase prev() {
		if (finishedIndex <= 0) {
			return null;
		}
		tableSize = 0;
		return objects[--finishedIndex];
	}

	/**
	 * sort all info which doesn't not retrieved. This function affects {@link #next()} but {@link #prev()}
	 */
	public void resort() {
		Arrays.sort(objects, finishedIndex, size);
	}

	@Override
	public int size() {
		return size;
	}
}
