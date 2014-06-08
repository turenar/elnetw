package jp.syuriken.snsw.twclient;

import org.junit.Assert;

/**
 * Save AssertionError for external threads
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class AssertionHandler {
	private Throwable throwable;

	public void assertEquals(int expected, int actual) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (Throwable t) {
			set(t);
		}
	}

	public void check() throws Throwable {
		if (throwable != null) {
			throw throwable;
		}
	}

	public void set(Throwable t) {
		if (this.throwable == null) {
			this.throwable = t;
		}
	}
}
