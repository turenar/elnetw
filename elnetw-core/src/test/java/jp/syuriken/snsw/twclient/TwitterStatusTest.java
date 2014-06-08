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

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jp.syuriken.snsw.twclient.twitter.TwitterStatus;
import jp.syuriken.snsw.twclient.twitter.TwitterUser;
import org.junit.BeforeClass;
import org.junit.Test;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import static org.junit.Assert.*;

/**
 * TwitterStatusのためのテスト
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TwitterStatusTest {

	private static final class ClientConfigurationExtension extends ClientConfigurationTestImpl {
		@Override
		public CacheManager getCacheManager() {
			return new CacheManager(this) {
				@Override
				public TwitterUser getCachedUser(long userId) {
					return null;
				}
			};
		}

		@Override
		public Twitter getTwitterForRead() {
			return null;
		}
	}

	/*package*/static class TestObj {

		/*package*/ String json;
		/*package*/ List<String> entity = new ArrayList<>();
		public Object text;
	}

	private static ArrayList<TestObj> tests;
	private static Method registerMethod;
	private static NoSuchMethodException registerMethodException;

	@SuppressWarnings("UnusedDeclaration") // called from reflection
	private static <T> T fakeRegisterJSONObject(T key, Object json) {
		throw new AssertionError(registerMethodException);
	}

	/** クラス初期化 */
	@BeforeClass
	public static void init() {
		Scanner scanner = new Scanner(TwitterStatusTest.class.getResourceAsStream("entity-test.txt"), "UTF-8");
		tests = new ArrayList<>();
		TestObj nowObj = null;
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.startsWith("!")) {
				continue; // line is comment
			}
			if (line.trim().isEmpty()) { // ignore empty line
				if (nowObj != null) {
					tests.add(nowObj);
					nowObj = null;
				}
				continue;
			}
			if (nowObj == null) {
				nowObj = new TestObj();
			}
			if (nowObj.json == null) {
				nowObj.json = line;
			} else if (nowObj.text == null) {
				nowObj.text = line;
			} else {
				nowObj.entity.add(line.trim());
			}
		}
		if (nowObj != null) {
			tests.add(nowObj);
		}
		try {
			registerMethod = TwitterObjectFactory.class
					.getDeclaredMethod("registerJSONObject", Object.class, Object.class);
			registerMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			try {
				// fail only if #registerJSONObject is called
				registerMethod = TwitterStatusTest.class
						.getDeclaredMethod("fakeRegisterJSONObject", Object.class, Object.class);
				registerMethodException = e;
			} catch (NoSuchMethodException e1) {
				// fail any case
				e1.addSuppressed(e);
				throw new AssertionError(e1);
			}

		}
	}

	private String getEntityText(String text, Object entity) {
		if (entity instanceof URLEntity) {
			URLEntity urlEntity = (URLEntity) entity;
			return text.substring(urlEntity.getStart(), urlEntity.getEnd()) + "=" + urlEntity.getDisplayURL();
		} else if (entity instanceof HashtagEntity) {
			return text.substring(((HashtagEntity) entity).getStart(), ((HashtagEntity) entity).getEnd());
		} else if (entity instanceof UserMentionEntity) {
			return text.substring(((UserMentionEntity) entity).getStart(), ((UserMentionEntity) entity).getEnd());
		} else {
			fail();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T registerJSONObject(T key, Object json) throws ReflectiveOperationException {
		if (registerMethod == null) {
			throw new AssertionError();
		} else {
			return (T) registerMethod.invoke(null, key, json);
		}
	}

	/**
	 * {@link jp.syuriken.snsw.twclient.twitter.TwitterStatus#TwitterStatus(Status)} のためのテスト・メソッド。
	 *
	 * @throws TwitterException json例外
	 * @throws IOException      IO例外
	 */
	@Test
	public void testTwitterStatus() throws Throwable {
		ClientConfigurationTestImpl configuration = new ClientConfigurationExtension();
		configuration.setGlobalInstance();
		try {
			ClientConfiguration.setInstance(configuration);
			ClientProperties clientProperties = new ClientProperties();
			clientProperties.load(new InputStreamReader(TwitterStatusTest.class.getResourceAsStream("config.properties"),
					"UTF-8"));
			configuration.setConfigProperties(clientProperties);
			for (TestObj test : tests) {
				Status status = registerJSONObject(TwitterObjectFactory.createStatus(test.json), test.json);
				status = new TwitterStatus(status);
				if (status.isRetweet()) {
					status = status.getRetweetedStatus();
				}
				assertEquals(test.text, status.getText());

				int i = 0;
				Object[] entities;
				entities = status.getHashtagEntities();
				if (entities != null) {
					for (Object entity : entities) {
						assertEquals(test.entity.get(i++), getEntityText(status.getText(), entity));
					}
				}
				entities = status.getUserMentionEntities();
				if (entities != null) {
					for (Object entity : entities) {
						assertEquals(test.entity.get(i++), getEntityText(status.getText(), entity));
					}
				}
				entities = status.getURLEntities();
				if (entities != null) {
					for (Object entity : entities) {
						assertEquals(test.entity.get(i++), getEntityText(status.getText(), entity));
					}
				}
				assertTrue(i == test.entity.size());
			}
		} finally {
			configuration.clearGlobalInstance();
		}
	}
}
