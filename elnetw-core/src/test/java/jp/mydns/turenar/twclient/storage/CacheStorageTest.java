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

package jp.mydns.turenar.twclient.storage;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class CacheStorageTest {
	@Test
	public void testIntList() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeList("/temp", Integer.MAX_VALUE, 0L); // 0L is converted into int
		assertThat(storage.readIntList("/temp"),
				is(CoreMatchers.<List<Integer>>allOf(
						hasItems(Integer.MAX_VALUE, 0),
						hasSize(2))));
	}

	@Test
	public void testIntListWithConvertableType() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeList("/temp", "1", 2, 3L);
		assertThat(storage.readIntList("/temp"),
				is(CoreMatchers.<List<Integer>>allOf(
						hasItems(1, 2, 3),
						hasSize(3))));
	}

	@Test(expected = Exception.class)
	public void testIntListWithIllegalType() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeList("/temp", "hoge", 2, 3L);
		assertThat(storage.readIntList("/temp"),
				is(CoreMatchers.<List<Integer>>allOf(
						hasItems(1, 2, 3),
						hasSize(3))));
	}

	@Test
	public void testIntMap() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.newMap("hoge");
		Map<String, Integer> map = storage.readIntMap("hoge");
		assertTrue(map.isEmpty());
		storage.writeString("/hoge/abc", "3");
		assertThat(map, hasEntry("abc", 3));
		assertEquals(1, map.size());
		map.put("fuga", 7);
		assertThat(map, allOf(hasEntry("abc", 3), hasEntry("fuga", 7)));
		assertEquals(2, map.size());
	}

	@Test
	public void testLongList() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeList("/temp", Long.MAX_VALUE, 0L);
		assertThat(storage.readLongList("/temp"),
				is(CoreMatchers.<List<Long>>allOf(
						hasItems(Long.MAX_VALUE, 0L),
						hasSize(2))));
	}

	@Test
	public void testLongMap() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.newMap("hoge");
		Map<String, Long> map = storage.readLongMap("hoge");
		assertTrue(map.isEmpty());
		storage.writeString("/hoge/abc", "3");
		assertThat(map, hasEntry("abc", 3L));
		assertEquals(1, map.size());
		map.put("fuga", 7L);
		assertThat(map, allOf(hasEntry("abc", 3L), hasEntry("fuga", 7L)));
		assertEquals(2, map.size());
	}

	@Test
	public void testMkdir() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/mkdir");
		storage.writeString("/mkdir/temp", "dead");
		assertEquals("dead", storage.readString("/mkdir/temp"));
		assertEquals("dead", storage.readString("mkdir/temp"));
	}

	@Test
	public void testMkdirExistedDir() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/mkdir");
		storage.mkdir("/mkdir");
	}

	@Test
	public void testMkdirRecursively() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/recursive/mkdir/test", true);
		storage.writeString("/recursive/mkdir/test/temp", "TEST");
		assertEquals("TEST", storage.readString("/recursive/mkdir/test/temp"));
	}

	@Test(expected = NoSuchElementException.class)
	public void testMkdirWithNotExistedParent() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/not/existed/dir");
	}

	@Test(expected = NoSuchElementException.class)
	public void testNotExistedDirEntry() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.readString("/temp/hoge");
	}

	@Test
	public void testParent() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertNull(storage.getParent());
		assertEquals(storage.getRoot(), storage.mkdir("/temp").getParent());
	}

	@Test
	public void testReadInt() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeInt("/temp", 123);
		assertEquals(123, storage.readInt("/temp"));
		assertEquals(123, storage.readInt("temp"));
	}

	@Test(expected = JSONException.class)
	public void testReadIntWithNoSuchElement() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.readInt("/temp");
	}

	@Test
	public void testReadList() throws Exception {
		DirEntry storage = new CacheStorage();
		assertNull(storage.readList("/temp"));
		assertNull(storage.readList("temp"));
		storage.writeList("/temp", "dead", 4);
		assertEquals("dead", storage.readList("/temp").get(0));
		assertEquals(4, storage.readList("/temp").get(1));
	}

	@Test
	public void testReadLong() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeLong("/temp", Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, storage.readLong("/temp"));
		assertEquals(Long.MAX_VALUE, storage.readLong("temp"));
		assertEquals(Long.MIN_VALUE, storage.readLong("fuga", Long.MIN_VALUE));
	}

	@Test(expected = JSONException.class)
	public void testReadLongWithNoSuchElement() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.readLong("/temp");
	}

	@Test
	public void testReadString() throws Exception {
		DirEntry storage = new CacheStorage();
		assertNull(storage.readString("/temp"));
		assertNull(storage.readString("temp"));
		storage.writeString("/temp", "dead");
		assertEquals("dead", storage.readString("/temp"));
		assertEquals("dead", storage.readString("temp"));
	}

	@Test
	public void testRemove() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeString("/temp", "test");
		storage.remove("/temp");
	}

	@Test(expected = Exception.class)
	public void testRemoveDir() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/temp");
		storage.remove("/temp");
	}

	@Test
	public void testRmdir() throws Exception {
		DirEntry storage = new CacheStorage();
		assertFalse(storage.rmdir("/test"));
		storage.mkdir("/test");
		assertTrue(storage.rmdir("/test"));
		assertFalse(storage.rmdir("/test"));
	}

	@Test
	public void testRmdirNotRecursively() throws Exception {
		DirEntry storage = new CacheStorage();
		assertFalse(storage.rmdir("/test"));
		storage.mkdir("/test/chunk", true);
		assertFalse(storage.rmdir("/test"));
	}

	@Test
	public void testRmdirRecursively() throws Exception {
		DirEntry storage = new CacheStorage();
		assertFalse(storage.rmdir("/test/chunk", true));
		storage.mkdir("/test/chunk", true);
		assertFalse(storage.rmdir("/test"));
		assertTrue(storage.rmdir("/test", true));
		assertFalse(storage.rmdir("/test", true));
	}

	@Test
	public void testRoot() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertEquals(storage.getRoot(), storage.getRoot().getRoot());
		assertEquals(storage.getRoot(), storage.mkdir("/temp").getRoot());
	}

	@Test
	public void testSize() throws Exception {
		DirEntry storage = new CacheStorage();
		assertEquals(0, storage.size());
		storage.writeString("hoge", "fuga");
		assertEquals(1, storage.size());
		storage.mkdir("fuga");
		assertEquals(2, storage.size());
		storage.writeString("hoge", "fuga");
		assertEquals(2, storage.size());
		storage.remove("hoge");
		assertEquals(1, storage.size());
	}

	@Test
	public void testStringList() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeList("/temp", "hoge", "fuga", "null");
		assertThat(storage.readStringList("/temp"),
				is(CoreMatchers.<List<String>>allOf(
						hasItems("hoge", "fuga", "null"),
						hasSize(3))));
	}

	@Test
	public void testStringListWithIllegalType() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.writeList("/temp", "hoge", 4, "null");
		assertThat(storage.readStringList("/temp"),
				is(CoreMatchers.<List<String>>allOf(
						hasItems("hoge", "4", "null"),
						hasSize(3))));
	}

	@Test
	public void testStringMap() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.newMap("hoge");
		Map<String, String> map = storage.readStringMap("hoge");
		assertTrue(map.isEmpty());
		storage.writeString("/hoge/abc", "aiueo");
		assertThat(map, hasEntry("abc", "aiueo"));
		assertEquals(1, map.size());
		map.put("fuga", "abc");
		assertThat(map, allOf(hasEntry("abc", "aiueo"), hasEntry("fuga", "abc")));
		assertEquals(2, map.size());
	}

	@Test
	public void testToString() throws Exception {
		DirEntry storage = new CacheStorage();
		assertEquals(0, storage.size());
		storage.writeString("hoge", "fuga");
		storage.mkdir("fuga");
		storage.writeString("hoge", "fuga");
		storage.writeString("fuga", storage.toString());
	}

	@Test
	public void testWriteWithDoubleDots() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/temp");
		storage.writeString("/temp/../hoge", "with");
		assertEquals("with", storage.readString("/hoge"));
		assertEquals("with", storage.readString("/temp/../hoge"));
	}

	@Test
	public void testWriteWithMultiSlashes() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/temp");
		storage.writeString("//temp/../hoge", "with");
		assertEquals("with", storage.readString("//////hoge"));
		assertEquals("with", storage.readString("/temp////./////..////////hoge"));
	}

	@Test
	public void testWriteWithSingleDots() throws Exception {
		DirEntry storage = new CacheStorage();
		storage.mkdir("/temp");
		storage.writeString("/./hoge", "with");
		assertEquals("with", storage.readString("/hoge"));
		assertEquals("with", storage.readString("/temp/./../hoge"));
	}
}
