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

import java.util.ArrayList;

import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.InitializerInfo;

/**
 * The base class for init info of TreeInitializeService
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ abstract class TreeInitInfoBase implements InitializerInfo, Comparable<TreeInitInfoBase> {
	/**
	 * info name
	 */
	protected final String name;
	/**
	 * weight
	 */
	protected int weight = TreeInitializeService.UNRESOLVED_WEIGHT;
	/**
	 * dependencies
	 */
	protected ArrayList<Relation> dependencies = new ArrayList<>();
	/**
	 * is initialized?
	 */
	protected boolean isInitialized;

	/**
	 * if false, check all dependencies resolved
	 */
	private boolean allDependenciesResolved;

	/**
	 * create instance
	 *
	 * @param name info name
	 */
	public TreeInitInfoBase(String name) {
		this.name = name;
	}

	/**
	 * add dependency
	 *
	 * @param relation relation
	 */
	public void addDependency(Relation relation) {
		dependencies.add(relation);
		allDependenciesResolved = false;
		update();
	}

	@Override
	public int compareTo(TreeInitInfoBase o) {
		return weight - o.weight;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * get weight
	 *
	 * @return weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * check is all dependencies resolved
	 *
	 * @return all resolved?
	 */
	public boolean isAllDependenciesResolved() {
		if (allDependenciesResolved) {
			return true;
		}

		for (Relation dependency : dependencies) {
			if (!dependency.isResolved()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * is initialized?
	 *
	 * @return initialized?
	 */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * skip this initializer
	 */
	public void provide() {
	}

	/**
	 * run initializer method
	 *
	 * @throws InitializeException exception occurred
	 */
	public abstract void run() throws InitializeException;

	/**
	 * uninit
	 *
	 * @param fastUninit uninit as fast as possible?
	 * @throws InitializeException exception occurred
	 */
	public abstract void uninit(boolean fastUninit) throws InitializeException;

	/**
	 * update weight and dependency
	 */
	protected void update() {
		int weight = 1;
		for (Relation dependency : dependencies) {
			weight += dependency.getWeight();
		}
		this.weight = weight;
		for (Relation dependency : dependencies) {
			dependency.update();
		}
	}
}
