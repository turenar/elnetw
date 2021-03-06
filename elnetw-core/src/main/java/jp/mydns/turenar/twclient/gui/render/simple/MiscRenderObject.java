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

package jp.mydns.turenar.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import jp.mydns.turenar.twclient.gui.render.MessageRenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.User;

import static jp.mydns.turenar.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;

/**
 * Render object for misc events
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MiscRenderObject extends AbstractRenderObject {
	private static final Logger logger = LoggerFactory.getLogger(MiscRenderObject.class);

	/**
	 * create instance
	 *
	 * @param renderer renderer
	 * @param obj      base obj
	 * @return MiscRendererObject instance
	 */
	public static MiscRenderObject getInstance(SimpleRenderer renderer, MessageRenderBase obj) {
		MiscRenderObject renderObject = new MiscRenderObject(renderer, obj.getBasedObject());
		renderObject.setBackgroundColor(obj.getBackgroundColor());
		renderObject.setForegroundColor(obj.getForegroundColor());
		renderObject.setCreatedBy(obj.getCreatedBy());
		renderObject.setCreatedByText(obj.getCreatedBy(), obj.getLongCreatedBy());
		renderObject.setDate(obj.getDate().getTime());
		renderObject.setIcon(obj.getIcon());
		renderObject.setUniqId(obj.getUniqId());
		renderObject.setText(obj.getText());
		return renderObject;
	}

	private static String getUserCreatedByText(User user) {
		return "@" + user.getScreenName() + " (" + user.getName() + ")";
	}

	private final Object base;
	private String createdBy;
	private String longCreatedBy;
	private long date;
	private String uniqId;
	private String text;
	private String popupType = "default";

	/**
	 * instance
	 *
	 * @param renderer renderer
	 * @param base     based object
	 */
	public MiscRenderObject(SimpleRenderer renderer, Object base) {
		super(renderer);
		this.base = base;
		date = System.currentTimeMillis();
		uniqId = "!stub/" + date + "/" + ThreadLocalRandom.current().nextInt();
	}

	@Override
	public void focusGained(FocusEvent e) {
		super.focusGained(e);
		getFrameApi().setTweetViewCreatedAt(renderer.toDateString(getDate(), true), null,
				DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewCreatedBy(componentUserIcon.getIcon(), longCreatedBy, null,
				DO_NOTHING_WHEN_POINTED);
		getFrameApi().setTweetViewText(text, null, DO_NOTHING_WHEN_POINTED);
	}

	/**
	 * get background color
	 *
	 * @return bg color
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public Object getBasedObject() {
		return base;
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
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

	@Override
	protected String getPopupMenuType() {
		return popupType;
	}

	@Override
	public String getUniqId() {
		return uniqId;
	}

	@Override
	protected void initComponents() {
		componentUserIcon.setHorizontalAlignment(JLabel.CENTER);
		componentSentBy.setFont(renderer.getDefaultFont());
	}

	/**
	 * set background color
	 *
	 * @param backgroundColor background color
	 * @return this instance
	 */
	public MiscRenderObject setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	/**
	 * set created by from user
	 *
	 * @param user user
	 * @return this instance
	 */
	public MiscRenderObject setCreatedBy(User user) {
		return setCreatedBy(user.getScreenName())
				.setCreatedByText(user.getScreenName(), getUserCreatedByText(user));
	}

	/**
	 * set created-by name
	 *
	 * @param createdBy createdBy name
	 * @return this instance
	 */
	public MiscRenderObject setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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
	public MiscRenderObject setCreatedByText(String createdBy) {
		return setCreatedByText(createdBy, createdBy);
	}

	/**
	 * set created-by text
	 *
	 * @param createdBy     createdBy text
	 * @param longCreatedBy createdBy text (not omitted)
	 * @return this instance
	 */
	public MiscRenderObject setCreatedByText(String createdBy, String longCreatedBy) {
		this.longCreatedBy = longCreatedBy;
		componentSentBy.setText(getShortenString(createdBy, CREATED_BY_MAX_LEN));
		return this;
	}

	/**
	 * set published date
	 *
	 * @param date date
	 * @return this instance
	 */
	public MiscRenderObject setDate(long date) {
		this.date = date;
		return this;
	}

	/**
	 * set foreground color
	 *
	 * @param foregroundColor foreground color
	 * @return this instance
	 */
	public MiscRenderObject setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		return this;
	}

	/**
	 * set createdBy icon from user
	 *
	 * @param user user
	 * @return this instance
	 */
	public MiscRenderObject setIcon(User user) {
		try {
			renderer.getImageCacher().setImageIcon(componentUserIcon, user);
		} catch (InterruptedException e) {
			logger.warn("Interrupted", e);
			Thread.currentThread().interrupt();
		}
		return this;
	}

	/**
	 * set createdBy icon
	 *
	 * @param icon icon
	 * @return this instance
	 */
	public MiscRenderObject setIcon(ImageIcon icon) {
		componentUserIcon.setIcon(icon);
		return this;
	}

	/**
	 * set popoup menu type
	 *
	 * @param popupType popup type (default: 'default')
	 * @return this instance
	 */
	public MiscRenderObject setPopupMenuType(String popupType) {
		this.popupType = popupType;
		return this;
	}

	/**
	 * set text
	 *
	 * @param text text
	 * @return this instance
	 */
	public MiscRenderObject setText(String text) {
		this.text = text;
		componentStatusText.setText(getShortenString(text, TEXT_MAX_LEN));
		return this;
	}

	/**
	 * set unique identifier
	 *
	 * @param uniqId identifier
	 * @return this instance
	 */
	public MiscRenderObject setUniqId(String uniqId) {
		this.uniqId = uniqId;
		return this;
	}
}
