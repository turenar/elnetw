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

import jp.mydns.turenar.twclient.init.InitCondition;
import jp.mydns.turenar.twclient.init.InitializeException;

/**
 * Init Condition Implementation for {@link jp.mydns.turenar.twclient.init.tree.TreeInitializeService}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
/*package*/ class TreeInitConditionImpl implements InitCondition {
	private final TreeInitInfoBase initializerInfo;
	private final boolean initializingPhase;
	private InitializeException failException;

	/**
	 * インスタンス生成
	 *
	 * @param info                initializer情報
	 * @param isInitializingPhase 起動フェーズかどうか
	 */
	protected TreeInitConditionImpl(TreeInitInfoBase info, boolean isInitializingPhase) {
		this.initializerInfo = info;
		initializingPhase = isInitializingPhase;
	}

	@Override
	public void clearFailStatus() {
		failException = null;
	}

	/**
	 * 例外を取得する
	 *
	 * @return 例外
	 */
	protected InitializeException getException() {
		return failException;
	}

	@Override
	public boolean isFastUninit() {
		return TreeInitializeService.instance.isFastUninit();
	}

	@Override
	public boolean isInitializingPhase() {
		return initializingPhase;
	}

	@Override
	public boolean isSetFailStatus() {
		return failException != null;
	}

	@Override
	public boolean isSlowUninit() {
		return !isFastUninit();
	}

	@Override
	public void setFailStatus(Throwable cause, String reason, int exitCode) {
		failException = new InitializeException(initializerInfo, cause, reason, exitCode);
	}

	@Override
	public void setFailStatus(String reason, int exitCode) {
		failException = new InitializeException(initializerInfo, reason, exitCode);
	}
}
