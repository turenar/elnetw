package jp.syuriken.snsw.lib;

import org.junit.Test;

import static junit.framework.Assert.*;


/** Test for {@link jp.syuriken.snsw.lib.VersionComparator} */
public class VersionComparatorTest {
	private static void assertEqualsVersion(String a, String b) {
		assertEquals(0, VersionComparator.compareVersion(a, b));
	}

	private static void assertGreaterVersion(String a, String b) {
		assertTrue(VersionComparator.compareVersion(a, b) > 0);
	}

	private static void assertLessVersion(String a, String b) {
		assertTrue(VersionComparator.compareVersion(a, b) < 0);
	}

	private void assertThrownError(String a, String b) {
		try {
			VersionComparator.compareVersion(a, b);
			fail();
		} catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	public void testCompareVersion1Simple() throws Exception {
		assertEqualsVersion("0", "0");
		assertEqualsVersion("1", "1");
		assertLessVersion("0", "1");
		assertGreaterVersion("1", "0");
		assertEqualsVersion("0.0", "0.0");
		assertEqualsVersion("0.1", "0.1");
		assertLessVersion("0.0", "0.1");
		assertGreaterVersion("0.4", "0.1");
	}

	@Test
	public void testCompareVersion2Snapshot() throws Exception {
		assertEqualsVersion("0-SNAPSHOT", "0-SNAPSHOT");
		assertLessVersion("0-SNAPSHOT", "0");
		assertGreaterVersion("1-SNAPSHOT", "0");
	}

	@Test
	public void testCompareVersion3Standard() throws Exception {
		assertEqualsVersion("0.0.1", "0.0.1");
		assertLessVersion("0", "0.0");
		assertLessVersion("0.0", "0.0.1");
		assertLessVersion("0.0.1", "0.0.2");
		assertGreaterVersion("0.1", "0.0.2");
		assertGreaterVersion("0.1-rc4", "0.1-beta3");
		assertGreaterVersion("0.1-beta3", "0.1-alpha1");
		assertLessVersion("0.1-alpha1", "0.1-alpha2");
		assertLessVersion("0.1-2patch0", "0.1-11patch0");
	}

	@Test
	public void testCompareVersion4Complexer() throws Exception {
		assertGreaterVersion("9100h+patch13+2012805-4~ppa6", "9100h+patch13+2012805-4~ppa5");
		assertLessVersion("20110227-0.1~ppa4", "20110227-0.2~ppa1.4");
		assertLessVersion("0.b037c59-4~ppa4", "0.b037c59-4~ppa5");
		assertLessVersion("0.0.2-alpha3-44-g179465e", "0.0.2");
		assertGreaterVersion("0.0.2-alpha3-44-g179465e", "v0.0.2-alpha3-43-g9d7dbaa");
		assertGreaterVersion("1.2.0.20091014-1~ppa1~ja2", "1.2.0.20091014-1~ppa1~ja1");
		assertGreaterVersion("1.2.0.20091014-1~ppa1~ja1", "1.2.0.20091014-1~ppa1~ga1");
		assertGreaterVersion("1.2.0.20091014-1~ppa1~ja1", "1.2.0.20091014-1~ppa0~zz1");
		assertLessVersion("1.2.0.20091014-1~ppa1~ja1", "1.2.0.20091014-1~ppa1+ja1");
		assertLessVersion("1.2.0.20091014-1~ppa1~ja1", "1.2.0.20091014_1~ppa1~ja1");
		assertGreaterVersion("1.2.0.20091014_1~ppa1~ja1", "1.2.0-20091014-1~ppa1~ja1");
		assertLessVersion("1.2.0.20091014-1~ppa1+ja1", "1.2.0.20091014.1~ppa1~ja1");
	}

	@Test
	public void testCompareVersion5Illegal() {
		assertThrownError("0..1", "0.1");
		assertThrownError("0.", "0.1");
	}
}
