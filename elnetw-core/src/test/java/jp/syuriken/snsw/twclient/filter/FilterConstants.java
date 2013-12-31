package jp.syuriken.snsw.twclient.filter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import static junit.framework.Assert.assertEquals;

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
		} catch (TwitterException e) {
			throw new AssertionError(e);
		} catch (IOException e) {
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
		DirectMessage directMessage = DataObjectFactory.createDirectMessage(loadFromFile(fileName));
		return directMessage;
	}

	private static String loadFromFile(String fileName) throws IOException {
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(FilterConstants.class.getResourceAsStream("/tweets/" + fileName));
			byte[] buf = new byte[65536];
			int len = stream.read(buf);
			String str = new String(buf, 0, len, Charset.forName("utf8"));
			return str;
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
		Status status = DataObjectFactory.createStatus(loadFromFile(fileName));
		return status;
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
