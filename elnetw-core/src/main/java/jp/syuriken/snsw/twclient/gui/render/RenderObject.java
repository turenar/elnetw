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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
