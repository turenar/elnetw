package jp.syuriken.snsw.twclient;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;

/** Data Collector from twitter stream */
public class BenchmarkDataCollector extends UserStreamAdapter {
	public static void main (String[] args) throws IOException {
		ClientProperties configProperties = new ClientProperties();
		configProperties.load(new FileInputStream("elnetw.cfg"));
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
				.setJSONStoreEnabled(true).setClientVersion(VersionInfo.getUniqueVersion())
				.setClientURL(VersionInfo.getSupportUrl())
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

	public BenchmarkDataCollector () throws IOException {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run () {
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
	public synchronized void onStatus (Status status) {
		String rawJSON = DataObjectFactory.getRawJSON(status);
		try {
			fileWriter.append(rawJSON);
			fileWriter.append('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("\r\u001b[0KWritten %d tweets: @%s", ++count, status.getUser().getScreenName());
	}
}
