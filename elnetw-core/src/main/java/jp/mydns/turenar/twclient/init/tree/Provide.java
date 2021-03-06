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
 * provide relation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class Provide extends Before {
	/**
	 * make instance
	 *
	 * @param targetName target name. If target is null and targetName is unknown, we don't resolve target.
	 * @param source     relation source
	 * @param target     relation target. It can be null.
	 */
	public Provide(String targetName, TreeInitInfoBase source, TreeInitInfoBase target) {
		super(targetName, source, target, true);
	}

	@Override
	protected Relation getOppositeRelation() {
		return new ProvidedBy(source.getName(), target, source);
	}

	@Override
	protected String getTypeString() {
		return "provide";
	}
}
