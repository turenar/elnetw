package jp.syuriken.snsw.twclient.gui;

import jp.syuriken.snsw.twclient.ClientMessageListener;

/**
 * タブレンダラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface TabRenderer extends ClientMessageListener {
	/*public static final*/ String READER_ACCOUNT_CHANGED = "account reader changed";
	/*public static final*/ String WRITER_ACCOUNT_CHANGED = "account writer changed";

	/**
	 * render for display requirements
	 */
	void onDisplayRequirement();

	/**
	 * render account changed message.
	 *
	 * {@inheritDoc}
	 */
	@Override
	void onChangeAccount(boolean forWrite);
}
