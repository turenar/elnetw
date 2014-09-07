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

package jp.mydns.turenar.twclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * elnetw関係のバージョン情報を格納するクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class VersionInfo {
	private static final Logger logger = LoggerFactory.getLogger(VersionInfo.class);
	/** バージョン情報が格納されたファイルのResourceBundle */
	public static final Properties VERSION_INFO_RESOURCE;

	static {
		InputStream stream =
				VersionInfo.class.getClassLoader().getResourceAsStream(
						"jp/mydns/turenar/twclient/elnetw-core-version.properties");

		VERSION_INFO_RESOURCE = new Properties();
		try {
			VERSION_INFO_RESOURCE.load(stream);
		} catch (IOException e) {
			logger.error("バージョン情報が取得できません", e);
		}
	}

	/** 情報が取得できなかった時の文字列 */
	public static final String UNKNOWN_STRING = "<unknown>";

	/**
	 * Artifact IDを返す
	 *
	 * @return Artifact ID
	 */
	public static String getArtifactId() {
		return getString("pom.artifactId");
	}

	/**
	 * Artifact Nameを返す
	 *
	 * @return ArtifactName
	 */
	public static String getArtifactName() {
		return getString("pom.name");
	}

	/**
	 * return code name
	 *
	 * @return code name
	 */
	public static String getCodeName() {
		return getString("elnetw.codeName");
	}

	/**
	 * ビルドのもととなったgit revisionを返す
	 *
	 * @return revision
	 */
	public static String getCommittedRevision() {
		return getString("git.build-commit-abbrev");
	}

	/**
	 * ビルドのもととなったgit described versionを返す
	 *
	 * @return described version
	 */
	public static String getDescribedVersion() {
		return getString("pom.uniqueVersion");
	}

	/**
	 * get major version (different major version will return different code name)
	 * @return major version
	 */
	public static String getMajorVersion() {
		return getString("elnetw.majorVersion");
	}

	/**
	 * return short code name
	 *
	 * @return code name
	 */
	public static String getShortCodeName() {
		return getString("elnetw.codeNameShort");
	}

	private static String getString(String key) {
		return VERSION_INFO_RESOURCE.getProperty(key, UNKNOWN_STRING);
	}

	/**
	 * サポートURLを返す
	 *
	 * @return サポートURL
	 */
	public static String getSupportUrl() {
		return getString("pom.url");
	}

	/**
	 * 一意なバージョンを返す。
	 *
	 * @return 一意なバージョン
	 */
	public static String getUniqueVersion() {
		String version = getVersion();
		if (version.equals(UNKNOWN_STRING)) {
			return getCommittedRevision();
		} else if (isSnapshot(version)) {
			return getDescribedVersion();
		}
		return version;
	}

	/**
	 * pom.xml に記述されたバージョンを返す
	 *
	 * @return バージョン
	 */
	public static String getVersion() {
		return VERSION_INFO_RESOURCE.getProperty("pom.version");
	}

	/**
	 * is snapshot runnning?
	 *
	 * @return if snapshot, true
	 */
	public static boolean isSnapshot() {
		return isSnapshot(getVersion());
	}

	/**
	 * is snapshot instance runnning?
	 *
	 * @param version version
	 * @return if snapshot, true
	 */
	protected static boolean isSnapshot(String version) {
		return version.endsWith("-SNAPSHOT");
	}

	private VersionInfo() {
	}
}
