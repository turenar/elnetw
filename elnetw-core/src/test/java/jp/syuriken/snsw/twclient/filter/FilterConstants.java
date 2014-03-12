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

package jp.syuriken.snsw.twclient.filter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import static org.junit.Assert.*;

/**
 * フィルタ・テスト用の定数等を格納したクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class FilterConstants implements FilterDispatcherBase {

	private static class BooleanFilterDispatcher implements FilterDispatcherBase {

		private boolean bool;


		public BooleanFilterDispatcher(boolean bool) {
			this.bool = bool;
		}

		@Override
		public boolean filter(DirectMessage directMessage) {
			return bool;
		}

		@Override
		public boolean filter(Status status) {
			return bool;
		}
	}


	/** 常にfalseを返す {@link FilterDispatcherBase} */
	public static final FilterDispatcherBase FALSE_DISPATCHER = new BooleanFilterDispatcher(false);

	/** 常にtrueを返す {@link FilterDispatcherBase} */
	public static final FilterDispatcherBase TRUE_DISPATCHER = new BooleanFilterDispatcher(true);

	/** 自分自身を格納した {@link FilterDispatcherBase} の配列 */
	protected final FilterDispatcherBase[] thisDispatcher = new FilterDispatcherBase[] {
			this
	};

	/** 最後にフィルタしようとしたオブジェクト */
	protected Object lastFilteringObject;

	/**
	 * &#64;twit4j による 2012年7月10日14:35 投稿。
	 * 「Tue Jul 10 14:35:33 JST 2012 test」
	 */
	public static final Status STATUS_1;

	/**
	 * &#64;ture7s による 2012年7月11日18:01JST 投稿
	 * 「てす &#64;ture7 &#64;ture7s」
	 */
	public static final Status STATUS_2;

	/**
	 * &#64;ture7s による 2012年7月11日18:02JST 投稿
	 * 「&#64;ture7s てす ‪#てす‬ http://example.com」
	 */
	public static final Status STATUS_3;

	/** API Documentに記述されているリツイートされたStatusのテスト用データ */
	public static final Status STATUS_4;

	/** {@link #STATUS_2}とほとんど同じだが、verifiedおよびprotectedがtrueになってる実在しないデータ */
	public static final Status STATUS_5;

	/** API Documentに記述されているダイレクトメッセージのテスト用データ */
	public static final DirectMessage DM_1;

	static {
		try {
			STATUS_1 = loadStatus("222564698627907584.json");
			STATUS_2 = loadStatus("222978973305544704.json");
			STATUS_3 = loadStatus("222979122341752833.json");
			STATUS_4 = loadStatus("21947795900469248.json");
			STATUS_5 = loadStatus("st_test.json");
			DM_1 = loadDirectMessage("dm.json");
		} catch (TwitterException | IOException e) {
			throw new AssertionError(e);
		}
	}


	/**
	 * ユーティリティーメソッド。
	 * {@link #FALSE_DISPATCHER} ないし {@link #TRUE_DISPATCHER} を格納した配列を返す
	 *
	 * @param bools bool配列
	 * @return {@link FilterDispatcherBase}配列
	 */
	protected static FilterDispatcherBase[] getDispatchers(boolean... bools) {
		FilterDispatcherBase[] dispatchers = new FilterDispatcherBase[bools.length];
		for (int i = 0; i < bools.length; i++) {
			dispatchers[i] = bools[i] ? TRUE_DISPATCHER : FALSE_DISPATCHER;
		}
		return dispatchers;
	}

	private static DirectMessage loadDirectMessage(String fileName) throws TwitterException, IOException {
		return DataObjectFactory.createDirectMessage(loadFromFile(fileName));
	}

	private static String loadFromFile(String fileName) throws IOException {
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(FilterConstants.class.getResourceAsStream("/tweets/" + fileName));
			byte[] buf = new byte[65536];
			int len = stream.read(buf);
			return new String(buf, 0, len, Charset.forName("utf8"));
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new AssertionError(e);
				}
			}
		}
	}

	private static Status loadStatus(String fileName) throws TwitterException, IOException {
		return DataObjectFactory.createStatus(loadFromFile(fileName));
	}

	/**
	 * 最後にフィルタしようとしたオブジェクトが正しいかどうかを確認する。
	 *
	 * @param expected 予期していたオブジェクト
	 */
	protected void assertFilteringObject(Object expected) {
		assertEquals(expected, lastFilteringObject);
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		lastFilteringObject = directMessage;
		return false;
	}

	@Override
	public boolean filter(Status status) {
		lastFilteringObject = status;
		return false;
	}
}
