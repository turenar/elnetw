package jp.syuriken.snsw.twclient;

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
						"jp/syuriken/snsw/twclient/elnetw-core-version.properties");

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
		return getString("git.build-version");
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
		String revision = getCommittedRevision();
		if (version.equals(UNKNOWN_STRING)) {
			return revision;
		} else if (version.endsWith("-SNAPSHOT")) {
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

	private VersionInfo() {
	}
}
