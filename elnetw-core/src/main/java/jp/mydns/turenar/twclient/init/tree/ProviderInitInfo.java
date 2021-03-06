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

import jp.mydns.turenar.twclient.init.InitializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * init info for provider
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ProviderInitInfo extends VirtualInitInfo {
	private static final Logger logger = LoggerFactory.getLogger(ProviderInitInfo.class);

	/**
	 * create instance
	 *
	 * @param name name
	 */
	public ProviderInitInfo(String name) {
		super(name);
	}

	@Override
	public void invoke() throws InitializeException {
		logger.trace(" {} weight={}", this, weight);
		isInitialized = true;
	}

	@Override
	public boolean isAllDependenciesResolved() {
		if (allDependenciesResolved) {
			return true;
		}

		for (Relation dependency : dependencies) {
			if (dependency instanceof ProvidedBy && dependency.isResolved()) {
				allDependenciesResolved = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return name + "(provider)";
	}
}
