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
import java.util.HashSet;

import jp.mydns.turenar.twclient.ParallelRunnable;
import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.InitializerInfo;

/**
 * The base class for init info of TreeInitializeService
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ abstract class TreeInitInfoBase implements InitializerInfo, Comparable<TreeInitInfoBase>, ParallelRunnable {
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
	protected boolean allDependenciesResolved;
	private InitializeException exception;
	private boolean allDependenciesInitialized;

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
		allDependenciesInitialized = false;
	}

	@Override
	public int compareTo(TreeInitInfoBase o) {
		return weight - o.weight;
	}

	public InitializeException getException() {
		return exception;
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
	 * get weight recursively
	 *
	 * @param set dependency check
	 * @return weight
	 */
	protected int getWeight(HashSet<String> set) {
		int weight = set.add(name) ? 1 : 0;
		for (Relation dependency : dependencies) {
			weight += dependency.getWeight(set);
		}
		return weight;
	}

	/**
	 * check if has dependency
	 *
	 * @param targetName target name
	 * @return have dependency?
	 */
	protected boolean hasDependency(String targetName) {
		for (Relation relation : dependencies) {
			if (relation.getTargetName().equals(targetName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * run initializer method
	 *
	 * @throws InitializeException exception occurred
	 */
	public abstract void invoke() throws InitializeException;

	protected boolean isAllDependenciesInitialized() {
		if (allDependenciesInitialized) {
			return true;
		}

		for (Relation dependency : dependencies) {
			if (!dependency.isInitialized()) {
				return false;
			}
		}
		allDependenciesInitialized = true;
		return true;
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
		allDependenciesResolved = true;
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

	@Override
	public void run() {
		try {
			invoke();
		} catch (InitializeException e) {
			setException(e);
		} catch (RuntimeException e) {
			setException(new InitializeException(e));
		}
		TreeInitializeService.instance.finish(this);
	}

	public void setException(InitializeException exception) {
		this.exception = exception;
	}

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
		weight = getWeight(new HashSet<String>());
	}
}
