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

package jp.mydns.turenar.twclient.twitter;

import jp.mydns.turenar.twclient.storage.DirEntry;
import twitter4j.URLEntity;

/**
 * URLEntity implementation for JSON cache
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class URLEntityImpl implements URLEntity {
	private static final long serialVersionUID = -8010158472808922410L;

	/**
	 * read url entities from dir entry
	 *
	 * @param base base dir entry
	 * @return URLEntity[]
	 */
	protected static URLEntity[] getEntitiesFromDirEntry(DirEntry base) {
		int len = base.size();
		URLEntity[] entities = new URLEntity[len];
		for (int i = 0; i < len; i++) {
			entities[i] = new URLEntityImpl(base.getDirEntry(String.valueOf(i)));
		}
		return entities;
	}

	/**
	 * store url entities into dir entry
	 *
	 * @param base     base dir entry
	 * @param entities url entities
	 */
	protected static void write(DirEntry base, URLEntity[] entities) {
		for (int i = 0, entitiesLength = entities.length; i < entitiesLength; i++) {
			write(base.mkdir(String.valueOf(i)), entities[i]);
		}
	}

	/**
	 * store url entity into dir entry
	 *
	 * @param base   base dir entry
	 * @param entity url entity
	 */
	protected static void write(DirEntry base, URLEntity entity) {
		base.writeInt("start", entity.getStart())
				.writeInt("end", entity.getEnd())
				.writeString("display_url", entity.getDisplayURL())
				.writeString("expand_url", entity.getExpandedURL())
				.writeString("url", entity.getURL())
				.writeString("text", entity.getText());
	}

	private final int start;
	private final int end;
	private final String displayUrl;
	private final String expandedUrl;
	private final String url;
	private final String text;

	public URLEntityImpl(DirEntry base) {
		start = base.readInt("start");
		end = base.readInt("end");
		displayUrl = base.readString("display_url");
		expandedUrl = base.readString("expand_url");
		url = base.readString("url");
		text = base.readString("text");
	}

	@Override
	public String getDisplayURL() {
		return displayUrl;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public String getExpandedURL() {
		return expandedUrl;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getURL() {
		return url;
	}
}
