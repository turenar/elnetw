package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import twitter4j.User;

/**
 * Render object for misc events
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class MiscRenderObject extends AbstractRenderObject {
	public static final int TEXT_MAX_LEN = 255;
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

	public Color getForegroundColor() {
		return foregroundColor;
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

	@Override
	public void requestCopyToClipboard() {
	}

	public MiscRenderObject setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public MiscRenderObject setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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

	public MiscRenderObject setDate(long date) {
		this.date = date;
		return this;
	}

	public MiscRenderObject setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
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

	public MiscRenderObject setUniqId(String uniqId) {
		this.uniqId = uniqId;
		return this;
	}
}
