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

package jp.mydns.turenar.twclient.gui.render;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;

import twitter4j.User;

/**
 * message render object.
 *
 * publish: {@link jp.mydns.turenar.twclient.gui.tab.TabRenderer}.onClientMessage(
 * {@link jp.mydns.turenar.twclient.ClientEventConstants}.RENDER_SHOW_OBJECT, this)
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MessageRenderBase {

	private static String getUserCreatedByText(User user) {
		return "@" + user.getScreenName() + " (" + user.getName() + ")";
	}

	private final Object base;
	private String createdById;
	private String createdBy;
	private String longCreatedBy;
	private long date;
	private String uniqId;
	private String text;
	private Color backgroundColor;
	private Color foregroundColor;
	private ImageIcon icon;

	public MessageRenderBase(Object base) {
		this.base = base;
		date = System.currentTimeMillis();
		uniqId = "!stub/" + date + "/" + ThreadLocalRandom.current().nextInt();
	}

	/**
	 * get background color
	 *
	 * @return bg color
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * get based object
	 *
	 * @return based object (nullable)
	 */
	public Object getBasedObject() {
		return base;
	}

	/**
	 * get created by
	 *
	 * @return created by
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * get created by ID (sometimes starts "!")
	 *
	 * @return created by ID
	 */
	public String getCreatedById() {
		return createdById;
	}

	/**
	 * get message creation date
	 *
	 * @return date
	 */
	public Date getDate() {
		return new Date(date);
	}

	/**
	 * get foreground color
	 *
	 * @return fg color
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}

	/**
	 * get icon
	 *
	 * @return created by icon
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * get created by for tweet pane
	 *
	 * @return created by for tweet pane
	 */
	public String getLongCreatedBy() {
		return longCreatedBy;
	}

	/**
	 * get message text
	 *
	 * @return text
	 */
	public String getText() {
		return text;
	}

	/**
	 * get unique id
	 *
	 * @return unique id
	 */
	public String getUniqId() {
		return uniqId;
	}

	/**
	 * set background color
	 *
	 * @param backgroundColor background color
	 * @return this instance
	 */
	public MessageRenderBase setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	/**
	 * set created by from user
	 *
	 * @param user user
	 * @return this instance
	 */
	public MessageRenderBase setCreatedById(User user) {
		return setCreatedById(user.getScreenName())
				.setCreatedByText(user.getScreenName(), getUserCreatedByText(user));
	}

	/**
	 * set created-by id
	 *
	 * @param createdBy createdById name
	 * @return this instance
	 */
	public MessageRenderBase setCreatedById(String createdBy) {
		this.createdById = createdBy;
		if (longCreatedBy == null) {
			setCreatedByText(createdBy);
		}
		return this;
	}

	/**
	 * set created-by text
	 *
	 * @param createdBy createdBy text
	 * @return this instance
	 */
	public MessageRenderBase setCreatedByText(String createdBy) {
		return setCreatedByText(createdBy, createdBy);
	}

	/**
	 * set created-by text
	 *
	 * @param createdBy     createdBy text
	 * @param longCreatedBy createdBy text (not omitted)
	 * @return this instance
	 */
	public MessageRenderBase setCreatedByText(String createdBy, String longCreatedBy) {
		this.createdBy = createdBy;
		this.longCreatedBy = longCreatedBy;
		return this;
	}

	/**
	 * set published date
	 *
	 * @param date date
	 * @return this instance
	 */
	public MessageRenderBase setDate(long date) {
		this.date = date;
		return this;
	}

	/**
	 * set foreground color
	 *
	 * @param foregroundColor foreground color
	 * @return this instance
	 */
	public MessageRenderBase setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		return this;
	}

	/**
	 * set createdById icon
	 *
	 * @param icon icon
	 * @return this instance
	 */
	public MessageRenderBase setIcon(ImageIcon icon) {
		this.icon = icon;
		return this;
	}

	/**
	 * set text
	 *
	 * @param text text
	 * @return this instance
	 */
	public MessageRenderBase setText(String text) {
		this.text = text;
		return this;
	}

	/**
	 * set unique identifier
	 *
	 * @param uniqId identifier
	 * @return this instance
	 */
	public MessageRenderBase setUniqId(String uniqId) {
		this.uniqId = uniqId;
		return this;
	}

}
