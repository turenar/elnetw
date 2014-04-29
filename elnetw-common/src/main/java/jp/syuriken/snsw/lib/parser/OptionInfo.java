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

package jp.syuriken.snsw.lib.parser;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * store option info
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class OptionInfo implements Iterable<OptionInfo> {
	/**
	 * イテレータ
	 */
	protected class InfoIterator implements Iterator<OptionInfo> {
		private OptionInfo info;
		private boolean isNextThis;

		public InfoIterator() {
			info = OptionInfo.this;
			isNextThis = true;
		}

		@Override
		public boolean hasNext() {
			return isNextThis || !(info == null || info.next == null);
		}

		@Override
		public OptionInfo next() {
			if (info == null) {
				throw new NoSuchElementException();
			} else if (isNextThis) {
				isNextThis = false;
			} else {
				info = info.next;
			}
			return info;
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	private String shortOptName;
	private String longOptName;
	private String arg;
	protected OptionInfo next;
	protected OptionInfo last = this;

	/**
	 * インスタンスを作成する
	 *
	 * @param shortOptName 短いオプション名 (null可)
	 * @param longOptName  長いオプション名
	 * @param arg          引数
	 */
	public OptionInfo(String shortOptName, String longOptName, String arg) {
		this.shortOptName = shortOptName;
		this.longOptName = longOptName;
		this.arg = arg;
	}

	/**
	 * 末尾に追加する。
	 *
	 * @param info 次に指定されたオプションの情報
	 */
	protected void add(OptionInfo info) {
		last.next = info;
		last = info;
	}

	/**
	 * 引数を取得する
	 *
	 * @return 引数
	 */
	public String getArg() {
		return arg;
	}

	/**
	 * 長いオプション名を取得する。
	 *
	 * @return 長いオプション名
	 */
	public String getLongOptName() {
		return longOptName;
	}

	/**
	 * 短いオプション名を取得する。
	 *
	 * @return 短いオプション名
	 */
	public String getShortOptName() {
		return shortOptName;
	}

	/**
	 * OptionInfoのイテレータを取得する。
	 *
	 * @return OptionInfo
	 */
	public Iterator<OptionInfo> iterator() {
		return new InfoIterator();
	}

	/**
	 * 次のOptionInfoを取得する
	 *
	 * @return 次のOptionInfo
	 */
	public OptionInfo next() {
		return next;
	}

	/**
	 * このインスタンスの内容を更新する
	 *
	 * @param shortOptName 短いオプション名 (null可)
	 * @param longOptName  長いオプション名
	 * @param arg          引数
	 */
	protected void update(String shortOptName, String longOptName, String arg) {
		this.shortOptName = shortOptName;
		this.longOptName = longOptName;
		this.arg = arg;
	}
}
