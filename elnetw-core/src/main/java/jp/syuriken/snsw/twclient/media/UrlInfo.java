package jp.syuriken.snsw.twclient.media;

/**
 * Url Info
 */
public class UrlInfo {
	private final String resolvedUrl;
	private final boolean isMediaFile;
	private boolean shouldRecursive;

	public UrlInfo(String url) {
		this(url, false, false);
	}

	/**
	 * インスタンスの作成
	 *
	 * @param resolvedUrl     解決済みURL。null不可
	 * @param shouldRecursive もう一度resolveするべきかどうか。bit.ly等はtrueであるべき。逆にjpg等はfalseであるのが望ましい。
	 * @param isMediaFile     画像ファイルかどうか。
	 */
	public UrlInfo(String resolvedUrl, boolean shouldRecursive, boolean isMediaFile) {
		this.resolvedUrl = resolvedUrl;
		this.shouldRecursive = shouldRecursive;
		this.isMediaFile = isMediaFile;
	}

	/**
	 * 解決済みURLを返す。
	 *
	 * @return 解決済みURL。検索に使ったURLと同一でも泣かない。
	 */
	public String getResolvedUrl() {
		return resolvedUrl;
	}

	/**
	 * 画像ファイルかどうかを返す。
	 *
	 * @return 画像ファイルかどうか。
	 */
	public boolean isMediaFile() {
		return isMediaFile;
	}

	/**
	 * もう一度resolveするべきかどうかを返す。
	 *
	 * @return trueの場合、{@link UrlResolverManager}は内部でもう一度{@link UrlResolverManager#getUrl(String)}を呼び出す。
	 */
	public boolean shouldRecursive() {
		return shouldRecursive;
	}

	@Override
	public String toString() {
		return "UrlInfo {resolvedUrl=" + resolvedUrl + ",shouldRecursive=" + shouldRecursive + ",isMediaFile=" + isMediaFile + "}";
	}
}
