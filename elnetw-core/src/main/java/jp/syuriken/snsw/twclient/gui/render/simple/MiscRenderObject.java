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

package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import jp.syuriken.snsw.twclient.Utility;
import twitter4j.User;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;

/**
 * Render object for misc events
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MiscRenderObject extends AbstractRenderObject {
	private static String getUserCreatedByText(User user) {
		return "@" + user.getScreenName() + " (" + user.getName() + ")";
	}

	private final Object base;
	private String createdBy;
	private String longCreatedBy;
	private long date;
	private String uniqId;
	private String text;

	public MiscRenderObject(SimpleRenderer renderer, Object base) {
		super(renderer);
		this.base = base;
		date = System.currentTimeMillis();
		uniqId = "!stub/" + date + "/" + ThreadLocalRandom.current().nextInt();
	}

	@Override
	public void focusGained(FocusEvent e) {
		super.focusGained(e);
		getFrameApi().setTweetViewCreatedAt(Utility.getDateString(getDate(), true), null,
				DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedBy(componentUserIcon.getIcon(), longCreatedBy, null,
				DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewText(text, null, DO_NOTHING_WHEN_POINTED);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public MiscRenderObject setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	@Override
	public Object getBasedObject() {
		return base;
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	public MiscRenderObject setCreatedBy(User user) {
		return setCreatedBy(user.getScreenName())
				.setCreatedByText(user.getScreenName(), getUserCreatedByText(user));
	}

	@Override
	public Date getDate() {
		return new Date(date);
	}

	public MiscRenderObject setDate(long date) {
		this.date = date;
		return this;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public MiscRenderObject setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		return this;
	}

	@Override
	public String getUniqId() {
		return uniqId;
	}

	public MiscRenderObject setUniqId(String uniqId) {
		this.uniqId = uniqId;
		return this;
	}

	@Override
	protected void initComponents() {
		componentUserIcon.setHorizontalAlignment(JLabel.CENTER);
		componentSentBy.setFont(renderer.getDefaultFont());
	}

	public MiscRenderObject setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		if (longCreatedBy == null) {
			setCreatedByText(createdBy);
		}
		return this;
	}

	public MiscRenderObject setCreatedByText(String createdBy) {
		return setCreatedByText(createdBy, createdBy);
	}

	public MiscRenderObject setCreatedByText(String createdBy, String longCreatedBy) {
		this.longCreatedBy = longCreatedBy;
		componentSentBy.setText(getShortenString(createdBy, CREATED_BY_MAX_LEN));
		return this;
	}

	public MiscRenderObject setIcon(User user) {
		renderer.getImageCacher().setImageIcon(componentUserIcon, user);
		return this;
	}

	public MiscRenderObject setIcon(ImageIcon icon) {
		componentUserIcon.setIcon(icon);
		return this;
	}

	public MiscRenderObject setText(String text) {
		this.text = text;
		componentStatusText.setText(getShortenString(text, TEXT_MAX_LEN));
		return this;
	}
}
