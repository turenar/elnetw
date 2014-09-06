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

package jp.syuriken.snsw.twclient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;

import twitter4j.Status;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/** Data Collector from twitter stream */
public class BenchmarkDataCollector extends UserStreamAdapter {
	public static void main(String[] args) throws IOException {
		ClientProperties configProperties = new ClientProperties();
		configProperties.load(Files.newBufferedReader(Paths.get("elnetw.cfg"), Charset.forName("UTF-8")));
		String accountId = configProperties.getProperty("twitter.oauth.access_token.default");
		String[] accessToken;
		try {
			String accessTokenString = configProperties.getPrivateString("twitter.oauth.access_token." + accountId,
					"X4b:mZ\"p4");
			accessToken = accessTokenString.split(":");
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}

		Configuration configuration = new ConfigurationBuilder()
				.setUserStreamRepliesAllEnabled(configProperties.getBoolean("twitter.stream.replies_all"))
				.setJSONStoreEnabled(true)
				.setOAuthAccessToken(accessToken[0])
				.setOAuthAccessTokenSecret(accessToken[1])
				.setOAuthConsumerKey(accessToken[2])
				.setOAuthConsumerSecret(accessToken[3])
				.build();
		TwitterStream stream = new TwitterStreamFactory(configuration).getInstance();
		final BenchmarkDataCollector benchmarkDataCollector = new BenchmarkDataCollector();
		stream.addListener(benchmarkDataCollector);
		stream.user();
	}

	private final FileWriter fileWriter;
	private final BufferedWriter bufferedWriter;
	private int count;

	public BenchmarkDataCollector() throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if (bufferedWriter != null) {
					try {
						bufferedWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fileWriter != null) {
					try {
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}));
		fileWriter = new FileWriter("benchmark.txt", true);
		bufferedWriter = new BufferedWriter(fileWriter);
	}

	@Override
	public synchronized void onStatus(Status status) {
		String rawJSON = TwitterObjectFactory.getRawJSON(status);
		try {
			fileWriter.append(rawJSON);
			fileWriter.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("\r\u001b[0KWritten %d tweets: @%s", ++count, status.getUser().getScreenName());
	}
}
