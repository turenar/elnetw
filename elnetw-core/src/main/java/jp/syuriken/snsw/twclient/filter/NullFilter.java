package jp.syuriken.snsw.twclient.filter;

import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * 何もしないフィルタ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class NullFilter implements FilterDispatcherBase {

	private static final FilterDispatcherBase instance = new NullFilter();


	/**
	 * 唯一インスタンスを取得する。
	 *
	 * @return インスタンス
	 */
	public static FilterDispatcherBase getInstance() {
		return instance;
	}

	private NullFilter() {
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return false;
	}

	@Override
	public boolean filter(Status status) {
		return false;
	}
}
