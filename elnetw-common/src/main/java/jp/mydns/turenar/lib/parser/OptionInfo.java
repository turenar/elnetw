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

package jp.mydns.turenar.lib.parser;

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
	protected static class InfoIterator implements Iterator<OptionInfo> {
		private OptionInfo next;

		/**
		 * create instance
		 *
		 * @param first First Element
		 */
		public InfoIterator(OptionInfo first) {
			next = first;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public OptionInfo next() {
			OptionInfo ret = next;
			if (next == null) {
				throw new NoSuchElementException();
			}
			next = next.next;
			return ret;
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	private OptionConfig config;
	private String optName;
	private String arg;
	/**
	 * 次のOptionInfo
	 */
	protected OptionInfo next;
	/**
	 * 最後のOptionInfo。コードを見る通り、先頭ではないInfoにaddすると死にます。
	 */
	protected OptionInfo last = this;

	/**
	 * インスタンスを作成する
	 *
	 * @param optName オプション名
	 * @param arg     引数
	 * @param config  OptionConfigインスタンス
	 */
	public OptionInfo(String optName, String arg, OptionConfig config) {
		this.optName = optName;
		this.arg = arg;
		this.config = config;
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
	 * OptionConfigを取得する。
	 *
	 * @return OptionConfig
	 */
	public OptionConfig getConfig() {
		return config;
	}

	/**
	 * グループ名を取得するユーティリティメソッド
	 *
	 * @return グループ名。オプションではなくプロセス引数なら、nullが返ります。
	 */
	public String getGroup() {
		return config == null ? null : config.group();
	}

	/**
	 * オプション名を取得する。
	 *
	 * @return オプション名
	 */
	public String getOptName() {
		return optName;
	}

	/**
	 * OptionInfoのイテレータを取得する。
	 *
	 * @return OptionInfo
	 */
	public Iterator<OptionInfo> iterator() {
		return new InfoIterator(this);
	}

	/**
	 * 次のOptionInfoを取得する
	 *
	 * @return 次のOptionInfo
	 */
	public OptionInfo next() {
		return next;
	}

	@Override
	public String toString() {
		return optName
				+ (arg == null ? "" : " " + arg)
				+ (next == null ? "" : " " + next.toString());
	}
}
