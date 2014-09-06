package jp.syuriken.snsw.twclient.internal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.handler.IntentArguments;

/**
 * actionPerformedでIntentArgumentをhandleするActionListener
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class IntentActionListener implements ActionListener {
	private IntentArguments intentArguments;

	public IntentActionListener(String intentName) {
		intentArguments = new IntentArguments(intentName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/* StatusData statusData;
		 if (selectingPost == null) {
		statusData = null;
		} else {
		statusData = statusMap.get(selectingPost.getRenderObject().id);
		} */
		ClientConfiguration.getInstance().handleAction(intentArguments);
	}

	/**
	 * put extra message into intent argument
	 *
	 * @param name name
	 * @param arg  argument
	 * @return this instance
	 */
	public IntentActionListener putExtra(String name, Object arg) {
		intentArguments.putExtra(name, arg);
		return this;
	}
}
