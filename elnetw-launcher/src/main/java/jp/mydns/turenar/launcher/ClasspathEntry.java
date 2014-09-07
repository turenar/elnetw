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

package jp.mydns.turenar.launcher;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import jp.mydns.turenar.lib.VersionComparator;

/**
 * Classpath Entry
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ClasspathEntry {


	private final String libraryName;
	private String libraryVersion;
	private URL libraryUrl;

	/**
	 * Make instance
	 *
	 * @param file jar's file
	 */
	public ClasspathEntry(File file) {
		// Valid Jar Filename:
		//   {artifactId}-{version}.jar
		// artifactId MUST start with alphabets
		// version MUST start with numeric
		String libraryFilename = file.getName();
		int extensionPos = libraryFilename.lastIndexOf('.');
		String libraryIdentifier;
		if (extensionPos == -1) {
			libraryIdentifier = libraryFilename;
		} else {
			libraryIdentifier = libraryFilename.substring(0, extensionPos);
		}
		int len = libraryIdentifier.length();
		int pos = 0;
		String libraryName = null;
		String version = null;
		while ((pos = libraryIdentifier.indexOf('-', pos + 1)) != -1) {
			// regex:"-(.)"
			if (pos + 1 >= len) {
				break; // libraryIdentifier does not have $1
			}

			char tokenStartChar = libraryIdentifier.charAt(pos + 1);

			// $1 is numeric
			if (tokenStartChar >= '0' && tokenStartChar <= '9') {
				libraryName = libraryIdentifier.substring(0, pos);
				version = libraryIdentifier.substring(pos + 1);
				break;
			}
		}

		// version is not found
		if (libraryName == null) {
			libraryName = libraryIdentifier;
			version = "0";
		}
		this.libraryName = libraryName;
		this.libraryVersion = version;
		try {
			this.libraryUrl = file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new AssertionError();
		}
	}

	/**
	 * get library name
	 *
	 * @return library name
	 */
	public String getLibraryName() {
		return libraryName;
	}

	/**
	 * get library url
	 *
	 * @return library url
	 */
	public URL getLibraryUrl() {
		return libraryUrl;
	}

	/**
	 * get library version
	 *
	 * @return library version
	 */
	public String getLibraryVersion() {
		return libraryVersion;
	}

	/**
	 * Compare with another, and if another has larger version than or equal to this update version and url.
	 *
	 * @param another another instance
	 */
	public void update(ClasspathEntry another) {
		if (VersionComparator.compareVersion(libraryVersion, another.getLibraryVersion()) <= 0) {
			libraryVersion = another.getLibraryVersion();
			libraryUrl = another.getLibraryUrl();
		}
	}
}
