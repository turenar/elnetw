package jp.syuriken.snsw.twclient.net;

import java.util.Map;
import java.util.Properties;

import twitter4j.conf.Configuration;

/**
* Twitter Configuration Implementation
*
* @author Turenar (snswinhaiku dot lo at gmail dot com)
*/
public class TwitterConfigurationImpl implements Configuration {
	private final String name;

	public TwitterConfigurationImpl(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean isDalvik() {
		return false;
	}

	@Override
	public boolean isGAE() {
		return false;
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public String getUserAgent() {
		return null;
	}

	@Override
	public String getUser() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public Map<String, String> getRequestHeaders() {
		return null;
	}

	@Override
	public String getHttpProxyHost() {
		return null;
	}

	@Override
	public String getHttpProxyUser() {
		return null;
	}

	@Override
	public String getHttpProxyPassword() {
		return null;
	}

	@Override
	public int getHttpProxyPort() {
		return 0;
	}

	@Override
	public int getHttpConnectionTimeout() {
		return 0;
	}

	@Override
	public int getHttpReadTimeout() {
		return 0;
	}

	@Override
	public int getHttpStreamingReadTimeout() {
		return 0;
	}

	@Override
	public int getHttpRetryCount() {
		return 0;
	}

	@Override
	public int getHttpRetryIntervalSeconds() {
		return 0;
	}

	@Override
	public int getHttpMaxTotalConnections() {
		return 0;
	}

	@Override
	public int getHttpDefaultMaxPerRoute() {
		return 0;
	}

	@Override
	public String getOAuthConsumerKey() {
		return null;
	}

	@Override
	public String getOAuthConsumerSecret() {
		return null;
	}

	@Override
	public String getOAuthAccessToken() {
		return null;
	}

	@Override
	public String getOAuthAccessTokenSecret() {
		return null;
	}

	@Override
	public String getClientVersion() {
		return null;
	}

	@Override
	public String getClientURL() {
		return null;
	}

	@Override
	public String getRestBaseURL() {
		return null;
	}

	@Override
	public String getStreamBaseURL() {
		return null;
	}

	@Override
	public String getOAuthRequestTokenURL() {
		return null;
	}

	@Override
	public String getOAuthAuthorizationURL() {
		return null;
	}

	@Override
	public String getOAuthAccessTokenURL() {
		return null;
	}

	@Override
	public String getOAuthAuthenticationURL() {
		return null;
	}

	@Override
	public String getUserStreamBaseURL() {
		return null;
	}

	@Override
	public String getSiteStreamBaseURL() {
		return null;
	}

	@Override
	public boolean isIncludeMyRetweetEnabled() {
		return false;
	}

	@Override
	public boolean isJSONStoreEnabled() {
		return false;
	}

	@Override
	public boolean isMBeanEnabled() {
		return false;
	}

	@Override
	public boolean isUserStreamRepliesAllEnabled() {
		return false;
	}

	@Override
	public boolean isStallWarningsEnabled() {
		return false;
	}

	@Override
	public String getMediaProvider() {
		return null;
	}

	@Override
	public String getMediaProviderAPIKey() {
		return null;
	}

	@Override
	public Properties getMediaProviderParameters() {
		return null;
	}

	@Override
	public int getAsyncNumThreads() {
		return 0;
	}

	@Override
	public long getContributingTo() {
		return 0;
	}

	@Override
	public String getDispatcherImpl() {
		return null;
	}

	@Override
	public String getLoggerFactory() {
		return null;
	}

	@Override
	public boolean isPrettyDebugEnabled() {
		return false;
	}

	@Override
	public boolean isGZIPEnabled() {
		return false;
	}
}
