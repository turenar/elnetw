package jp.syuriken.snsw.twclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * twclient関係のバージョン情報を格納するクラス。
 * 
 * @author $Author$
 */
public final class VersionInfo {
	
	private static final Logger logger = LoggerFactory.getLogger(VersionInfo.class);
	
	/** バージョン情報が格納されたファイルのResourceBundle */
	public static final Properties VERSION_INFO_RESOURCE;
	static {
		InputStream stream =
				VersionInfo.class.getClassLoader().getResourceAsStream(
						"jp/syuriken/snsw/twclient/twclient-core-version.properties");
		
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
	 * ビルドのもととなったsvn revisionを返す
	 * 
	 * @return revision
	 */
	public static String getCommittedRevision() {
		return getString("svn.committedRevision");
	}
	
	private static String getString(String key) {
		return VERSION_INFO_RESOURCE.getProperty(key);
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
			if (revision.equals(UNKNOWN_STRING)) {
				return version;
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(version).append("-rev").append(getCommittedRevision());
				return sb.toString();
			}
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