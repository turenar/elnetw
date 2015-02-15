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

package jp.mydns.turenar.twclient.intent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import jp.mydns.turenar.twclient.gui.ImageViewerFrame;

/**
 * Open Image
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com
 */
public class OpenImageIntent implements Intent {
	@Override
	public void createJMenuItem(PopupMenuDispatcher dispatcher, IntentArguments args) {
	}

	@Override
	public void handleAction(IntentArguments args) {
		Boolean possiblySensitive = args.getExtraObj("possiblySensitive", Boolean.class, Boolean.FALSE);
		Object urls = args.getExtra("urls");
		if (urls instanceof Iterable) {
			Iterable<?> urlList = (Iterable<?>) urls;
			for (Object url : urlList) {
				showFrame(toUrls(url), possiblySensitive);
			}
			return;
		} else if (urls != null) {
			throw new IllegalArgumentException("arg `urls' must be Iterable");
		}
		Object urlObject = args.getExtra("url");
		URL[] url;
		if (urlObject != null) {
			url = toUrls(urlObject);
		} else {
			throw new IllegalArgumentException("arg `url' or `urls' is missing");
		}
		showFrame(url, possiblySensitive);
	}

	private void showFrame(URL[] url, Boolean possiblySensitive) {
		new ImageViewerFrame(possiblySensitive, url).setVisible(true);
	}

	private URL[] toUrls(Object url) {
		try {
			if (url instanceof URL[]) {
				return (URL[]) url;
			} else if (url instanceof String[]) {
				return Arrays.stream((String[]) url)
						.map(urlString -> {
							try {
								return new URL(urlString);
							} catch (MalformedURLException e) {
								throw new IllegalArgumentException(e);
							}
						})
						.toArray(URL[]::new);
			} else if (url instanceof URL) {
				return new URL[] {(URL) url};
			} else if (url instanceof String) {
				return new URL[] {new URL((String) url)};
			} else {
				throw new IllegalArgumentException("url must be URL or String");
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
