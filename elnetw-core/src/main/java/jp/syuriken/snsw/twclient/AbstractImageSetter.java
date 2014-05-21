package jp.syuriken.snsw.twclient;

import java.awt.Image;

import jp.syuriken.snsw.twclient.internal.ConnectionInfo;

/**
 * AbstractImageSetter: ImageSetter template.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractImageSetter implements ImageSetter {
	private ImageSetter next;

	@Override
	public synchronized void addSetter(ImageSetter next) {
		next.setNext(this.next);
		this.next = next;
	}

	@Override
	public ImageSetter next() {
		return next;
	}

	@Override
	public void onException(Exception e, ConnectionInfo connectionInfo) {
	}

	@Override
	public synchronized void setImageRecursively(Image image) {
		if (image == null) {
			return;
		}

		ImageSetter setter = this;
		do {
			setter.setImage(image);
		} while ((setter = setter.next()) != null);
	}

	@Override
	public void setNext(ImageSetter next) {
		this.next = next;
	}
}
