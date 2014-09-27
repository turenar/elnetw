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

/**
 * Depend Relation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ class Depend extends Relation {
	/**
	 * make instance
	 *
	 * @param name   target name
	 * @param source which depends on target
	 */
	public Depend(String name, TreeInitInfoBase source) {
		this(name, source, null);
	}

	/**
	 * make instance
	 *
	 * @param name       target name
	 * @param sourceInfo which depends on target
	 * @param targetInfo which is depended by source
	 */
	public Depend(String name, TreeInitInfoBase sourceInfo, TreeInitInfoBase targetInfo) {
		super(name, sourceInfo, targetInfo, true);
	}

	@Override
	protected DependedBy getOppositeRelation() {
		return new DependedBy(source.getName(), target, source);
	}

	@Override
	protected String getTypeString() {
		return "depend";
	}

	@Override
	protected boolean isAfterRelation() {
		return true;
	}

	@Override
	public boolean isResolved() {
		return target != null && target.isAllDependenciesResolved();
	}

	@Override
	public void update() {
	}
}
