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
 * DependedBy Relation. It is the opposite of Depend.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ class DependedBy extends Relation {
	/**
	 * make instance
	 *
	 * @param name   target name
	 * @param source which depends on target
	 * @param target which is depended by source
	 */
	public DependedBy(String name, TreeInitInfoBase source, TreeInitInfoBase target) {
		super(name, source, target, true);
	}

	@Override
	protected Relation getOppositeRelation() {
		return null; // DependedBy is added as opposite of Depend relation
	}

	@Override
	protected String getTypeString() {
		return "dependedBy";
	}

	@Override
	public int getWeight() {
		return 0;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public void update() {
		target.update();
	}
}
