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
 * This class shows relation such as depend, after, before, etc.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ abstract class Relation {
	/**
	 * target name
	 */
	protected final String targetName;
	/**
	 * relation source
	 */
	protected final TreeInitInfoBase source;
	/**
	 * relation target. It can be null.
	 */
	protected TreeInitInfoBase target;

	/**
	 * make instance
	 *
	 * @param targetName               target name. If target is null and targetName is unknown, we don't resolve target.
	 * @param source                   relation source
	 * @param target                   relation target. It can be null.
	 * @param registerOppositeRelation should register opposite relation?
	 */
	public Relation(String targetName, TreeInitInfoBase source, TreeInitInfoBase target,
			boolean registerOppositeRelation) {
		this.targetName = targetName;
		this.source = source;
		target = target == null ? TreeInitializeService.instance.infoMap.get(targetName) : target;
		this.target = target;
		if (target == null) {
			TreeInitializeService.instance.unresolvedRelations.add(this);
		}
		if (registerOppositeRelation && !(target == null || source == target)) {
			registerOppositeRelation();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Relation)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		Relation another = (Relation) obj;
		return (source == another.source) && (targetName.equals(another.targetName));
	}

	protected abstract Relation getOppositeRelation();

	/**
	 * get target info
	 *
	 * @return target info. it can be null.
	 */
	public TreeInitInfoBase getTargetInfo() {
		return target;
	}

	/**
	 * target name
	 *
	 * @return target name
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * get string which shows what relation is.
	 *
	 * @return what relation is.
	 */
	protected abstract String getTypeString();

	/**
	 * get weight. The larger weight, the later initializer is invoked
	 *
	 * @return weight
	 */
	public abstract int getWeight();

	/**
	 * is target resolved?
	 *
	 * @return resolved status
	 */
	public abstract boolean isResolved();

	protected void registerOppositeRelation() {
		Relation oppositeRelation = getOppositeRelation();
		if (!(oppositeRelation == null || target.hasDependency(source.getName()))) {
			target.addDependency(oppositeRelation);
			target.update();
		}
	}

	@Override
	public String toString() {
		return getTypeString() + ": " + source.getName() + " -> " + targetName;
	}

	/**
	 * try to resolve relation
	 *
	 * @return resolved newly?
	 */
	protected boolean tryResolve() {
		if (target == null) {
			target = TreeInitializeService.instance.infoMap.get(targetName);
			if (target != null) {
				update();
				if (!(target == null || source == target)) {
					registerOppositeRelation();
				}
				return true;
			}
		}
		return false;
	}

	public abstract void update();
}
