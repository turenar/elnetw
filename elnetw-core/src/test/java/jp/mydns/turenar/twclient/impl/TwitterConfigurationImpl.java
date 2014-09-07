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

package jp.mydns.turenar.twclient.impl;

import java.util.Properties;

import twitter4j.HttpClientConfiguration;
import twitter4j.conf.Configuration;

/**
 * Twitter Configuration Implementation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterConfigurationImpl implements Configuration {
	private static final long serialVersionUID = 1616007835451341178L;
	private final String name;

	public TwitterConfigurationImpl(String name) {
		this.name = name;
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
	public HttpClientConfiguration getHttpClientConfiguration() {
		return null;
	}

	@Override
	public int getHttpStreamingReadTimeout() {
		return 0;
	}

	@Override
	public String getLoggerFactory() {
		return null;
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

	public String getName() {
		return name;
	}

	@Override
	public String getOAuth2AccessToken() {
		return null;
	}

	@Override
	public String getOAuth2InvalidateTokenURL() {
		return null;
	}

	@Override
	public String getOAuth2Scope() {
		return null;
	}

	@Override
	public String getOAuth2TokenType() {
		return null;
	}

	@Override
	public String getOAuth2TokenURL() {
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
	public String getOAuthAccessTokenURL() {
		return null;
	}

	@Override
	public String getOAuthAuthenticationURL() {
		return null;
	}

	@Override
	public String getOAuthAuthorizationURL() {
		return null;
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
	public String getOAuthRequestTokenURL() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getRestBaseURL() {
		return null;
	}

	@Override
	public String getSiteStreamBaseURL() {
		return null;
	}

	@Override
	public String getStreamBaseURL() {
		return null;
	}

	@Override
	public String getUploadBaseURL() {
		return null;
	}

	@Override
	public String getUser() {
		return null;
	}

	@Override
	public String getUserStreamBaseURL() {
		return null;
	}

	@Override
	public boolean isApplicationOnlyAuthEnabled() {
		return false;
	}

	@Override
	public boolean isDaemonEnabled() {
		return false;
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public boolean isIncludeEntitiesEnabled() {
		return false;
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
	public boolean isStallWarningsEnabled() {
		return false;
	}

	@Override
	public boolean isTrimUserEnabled() {
		return false;
	}

	@Override
	public boolean isUserStreamRepliesAllEnabled() {
		return false;
	}

	@Override
	public boolean isUserStreamWithFollowingsEnabled() {
		return false;
	}
}
