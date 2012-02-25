package jp.syuriken.snsw.twclient.filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ImageCacher;

import org.junit.Test;

/**
 * RootFilter のためのテスト
 * 
 * @author $Author$
 */
public class RootFilterTest {
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.RootFilter#onStatus(twitter4j.Status)} のためのテスト・メソッド。
	 */
	@Test
	public void testOnStatus() {
		ClientConfiguration configuration = new ClientConfiguration() {
			
			@Override
			public ImageCacher getImageCacher() {
				return new TestImageCacher(this);
			}
		};
		RootFilter rootFilter = new RootFilter(configuration);
		assertNotNull(rootFilter.onStatus(new TestStatus(0)));
		assertNotNull(rootFilter.onStatus(new TestStatus(1)));
		assertNull(rootFilter.onStatus(new TestStatus(0)));
	}
}

class TestImageCacher extends ImageCacher {
	
	public TestImageCacher(ClientConfiguration configuration) {
		super(configuration);
	}
}
