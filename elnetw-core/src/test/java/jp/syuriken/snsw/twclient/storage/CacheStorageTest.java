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

package jp.syuriken.snsw.twclient.storage;

import java.util.List;
import java.util.NoSuchElementException;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class CacheStorageTest {
	@Test
	public void testIntList() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeList("/temp", Integer.MAX_VALUE, 0L); // 0L is converted into int
		assertThat(storage.readIntList("/temp"),
				is(CoreMatchers.<List<Integer>>allOf(
						hasItems(Integer.MAX_VALUE, 0),
						hasSize(2))));
	}

	@Test
	public void testIntListWithConvertableType() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeList("/temp", "1", 2, 3L);
		assertThat(storage.readIntList("/temp"),
				is(CoreMatchers.<List<Integer>>allOf(
						hasItems(1, 2, 3),
						hasSize(3))));
	}

	@Test(expected = Exception.class)
	public void testIntListWithIllegalType() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeList("/temp", "hoge", 2, 3L);
		assertThat(storage.readIntList("/temp"),
				is(CoreMatchers.<List<Integer>>allOf(
						hasItems(1, 2, 3),
						hasSize(3))));
	}


	@Test
	public void testLongList() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeList("/temp", Long.MAX_VALUE, 0L);
		assertThat(storage.readLongList("/temp"),
				is(CoreMatchers.<List<Long>>allOf(
						hasItems(Long.MAX_VALUE, 0L),
						hasSize(2))));
	}

	@Test
	public void testMkdir() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.mkdir("/mkdir");
		storage.writeString("/mkdir/temp", "dead");
		assertEquals("dead", storage.readString("/mkdir/temp"));
		assertEquals("dead", storage.readString("/mkdir/temp"));
	}

	@Test
	public void testMkdirRecursively() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.mkdir("/recursive/mkdir/test", true);
		storage.writeString("/recursive/mkdir/test/temp", "TEST");
		assertEquals("TEST", storage.readString("/recursive/mkdir/test/temp"));
	}

	@Test(expected = NoSuchElementException.class)
	public void testMkdirWithNotExistedParent() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.mkdir("/not/existed/dir");
	}

	@Test(expected = NoSuchElementException.class)
	public void testNotExistedDirEntry() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.readString("/temp/hoge");
	}

	@Test
	public void testReadInt() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeInt("/temp", 123);
		assertEquals(123, storage.readInt("/temp"));
		assertEquals(123, storage.readInt("temp"));
	}

	@Test(expected = JSONException.class)
	public void testReadIntWithNoSuchElement() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.readInt("/temp");
	}

	@Test
	public void testReadList() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertNull(storage.readList("/temp"));
		assertNull(storage.readList("temp"));
		storage.writeList("/temp", "dead", 4);
		assertEquals("dead", storage.readList("/temp").get(0));
		assertEquals(4, storage.readList("/temp").get(1));
	}

	@Test
	public void testReadLong() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeLong("/temp", Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, storage.readLong("/temp"));
		assertEquals(Long.MAX_VALUE, storage.readLong("temp"));
	}

	@Test(expected = JSONException.class)
	public void testReadLongWithNoSuchElement() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.readLong("/temp");
	}

	@Test
	public void testReadString() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertNull(storage.readString("/temp"));
		assertNull(storage.readString("temp"));
		storage.writeString("/temp", "dead");
		assertEquals("dead", storage.readString("/temp"));
		assertEquals("dead", storage.readString("temp"));
	}

	@Test
	public void testRmdir() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertFalse(storage.rmdir("/test"));
		storage.mkdir("/test");
		assertTrue(storage.rmdir("/test"));
		assertFalse(storage.rmdir("/test"));
	}

	@Test
	public void testRmdirNotRecursively() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertFalse(storage.rmdir("/test"));
		storage.mkdir("/test/chunk", true);
		assertFalse(storage.rmdir("/test"));
	}

	@Test
	public void testRmdirRecursively() throws Exception {
		CacheStorage storage = new CacheStorage();
		assertFalse(storage.rmdir("/test/chunk", true));
		storage.mkdir("/test/chunk", true);
		assertFalse(storage.rmdir("/test"));
		assertTrue(storage.rmdir("/test", true));
		assertFalse(storage.rmdir("/test", true));
	}

	@Test
	public void testStringList() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeList("/temp", "hoge", "fuga", "null");
		assertThat(storage.readStringList("/temp"),
				is(CoreMatchers.<List<String>>allOf(
						hasItems("hoge", "fuga", "null"),
						hasSize(3))));
	}

	@Test
	public void testStringListWithIllegalType() throws Exception {
		CacheStorage storage = new CacheStorage();
		storage.writeList("/temp", "hoge", 4, "null");
		assertThat(storage.readStringList("/temp"),
				is(CoreMatchers.<List<String>>allOf(
						hasItems("hoge", "4", "null"),
						hasSize(3))));
	}
}