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

import java.lang.reflect.Method;
import java.util.HashSet;

import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InitInfo for phase
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ class PhaseInitInfo extends TreeInitInfoBase {

	private static final Logger logger = LoggerFactory.getLogger(PhaseInitInfo.class);

	/**
	 * get info name from phase
	 *
	 * @param phase phase name
	 * @return info name (::phase:&lt;phase&gt;)
	 */
	public static String getNameFromPhase(String phase) {
		return "::phase:".concat(phase);
	}

	private final String phase;
	private boolean isResolved;

	/**
	 * create instance
	 *
	 * @param phase phase name
	 */
	public PhaseInitInfo(String phase) {
		super(getNameFromPhase(phase));
		this.phase = phase;
		weight = TreeInitializeService.UNRESOLVED_WEIGHT;
	}

	@Override
	public Initializer getAnnotation() {
		return null;
	}

	@Override
	public Method getInitializer() {
		return null;
	}

	@Override
	public String getPhase() {
		return phase;
	}

	@Override
	protected int getWeight(HashSet<String> set) {
		if (!set.add(name) || isResolved) {
			return 0;
		} else {
			return TreeInitializeService.UNRESOLVED_WEIGHT;
		}
	}

	@Override
	public boolean isAllDependenciesResolved() {
		return isResolved;
	}

	/**
	 * resolve dependants
	 */
	protected void resolve() {
		if (!isResolved) {
			isResolved = true;
			weight = 0;
			for (Relation dependency : dependencies) {
				dependency.update();
			}
		}
	}

	@Override
	public void run() {
		logger.info("Entering {} phase", phase);
	}

	@Override
	public String toString() {
		return (isAllDependenciesResolved() ? "" : "<NotResolved>") + "PhaseInitInfo{phase=" + phase + "}";
	}

	@Override
	public void uninit(boolean fastUninit) throws InitializeException {
	}
}
