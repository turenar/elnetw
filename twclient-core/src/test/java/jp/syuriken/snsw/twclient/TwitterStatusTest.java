package jp.syuriken.snsw.twclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.internal.json.DataObjectFactoryUtil;
import twitter4j.json.DataObjectFactory;

/**
 * TwitterStatusのためのテスト
 * 
 * @author $Author$
 */
public class TwitterStatusTest {
	
	/*package*/static class TestObj {
		
		/*package*/String json;
		
		/*package*/List<String> entity = new ArrayList<String>();
		
		public Object text;
	}
	
	
	private static ArrayList<TestObj> tests;
	
	
	/**
	 * クラス初期化
	 */
	@BeforeClass
	public static void init() {
		Scanner scanner = new Scanner(TwitterStatusTest.class.getResourceAsStream("entity-test.txt"), "UTF-8");
		tests = new ArrayList<TestObj>();
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
	
	/**
	 * {@link TwitterStatus#TwitterStatus(twitter4j.Status)} のためのテスト・メソッド。
	 * @throws TwitterException json例外
	 */
	@Test
	public void testTwitterStatus() throws TwitterException {
		for (TestObj test : tests) {
			Status status = DataObjectFactory.createStatus(test.json);
			DataObjectFactoryUtil.registerJSONObject(status, test.json);
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
	}
}
