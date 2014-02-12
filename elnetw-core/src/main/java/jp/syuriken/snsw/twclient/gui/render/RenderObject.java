package jp.syuriken.snsw.twclient.gui.render;

import java.util.Date;

/**
 * rendered object.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface RenderObject {
	/**
	 * get base object
	 *
	 * @return base object. ex. Status, DirectMessage, String[evName, ...]
	 */
	Object getBasedObject();

	/**
	 * rendered panel.
	 *
	 * For implementations: this value should be cache
	 *
	 * @return panel
	 */
	RenderPanel getComponent();

	/**
	 * get name base object is created by.
	 *
	 * @return Twitter screen name, application, etc.
	 */
	String getCreatedBy();

	/**
	 * get date when base object os created.
	 *
	 * DON'T MODIFY THIS DATE OBJECT. YOU MUSTN'T EDIT ANYWAY.
	 *
	 * @return created date
	 */
	Date getDate();

	/**
	 * get uniq id. must not make collision.
	 *
	 * This should read from final field.
	 *
	 * @return unique id
	 */
	String getUniqId();

	/**
	 * handle event
	 *
	 * @param name event name
	 * @param arg  event argument
	 */
	void onEvent(String name, Object arg);
}
