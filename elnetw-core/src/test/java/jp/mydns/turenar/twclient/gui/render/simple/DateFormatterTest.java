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

package jp.mydns.turenar.twclient.gui.render.simple;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.mydns.turenar.twclient.ClientConfigurationTestImpl;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.i18n.LocalizationResourceProxy;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateFormatterTest {
	private static final int SEC = 1000;
	private static final int MINUTE = 60 * SEC;
	private static final int HOUR = 60 * MINUTE;
	private static final int DAY = 24 * HOUR;
	private final long BASE_TIME;

	public DateFormatterTest() {
		Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
		calendar.set(2014, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		BASE_TIME = calendar.getTimeInMillis();
	}

	private ClientConfigurationTestImpl getConf(String prop) {
		ClientConfigurationTestImpl configuration = new ClientConfigurationTestImpl();
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setProperty("gui.date.type", prop);
		configuration.setConfigProperties(clientProperties);
		return configuration;
	}

	@Test
	public void testMillisecond() throws Exception {
		ClientConfigurationTestImpl configuration = getConf("ms");
		configuration.setGlobalInstance();
		try {
			assertEquals("2014/01/01 00:00:00.000", toDateString(-10, SEC));
			assertEquals("2014/01/01 00:00:00.000", toDateString(+10, SEC));
			assertEquals("2014/01/01 00:00:00.000", toDateString(+10, MINUTE));
			assertEquals("2014/01/01 00:00:00.000", toDateString(+10, HOUR));
			assertEquals("2014/01/01 00:00:00.000", toDateString(+10, DAY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testMillisecondRelative() throws Exception {
		ClientConfigurationTestImpl configuration = getConf("ms+rel");
		configuration.setGlobalInstance();
		try {
			assertEquals("2014/01/01 00:00:00.000", toDateString(-10, SEC));
			assertEquals("10秒 (2014/01/01 00:00:00.000)", toDateString(+10, SEC));
			assertEquals("10分 (2014/01/01 00:00:00.000)", toDateString(+10, MINUTE));
			assertEquals("10時間 (2014/01/01 00:00:00.000)", toDateString(+10, HOUR));
			assertEquals("2014/01/01 00:00:00.000", toDateString(+10, DAY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testRelative() throws Exception {
		ClientConfigurationTestImpl configuration = getConf("rel");
		configuration.setGlobalInstance();
		try {
			assertEquals("未来", toDateString(-10, SEC));
			assertEquals("10秒", toDateString(+10, SEC));
			assertEquals("10分", toDateString(+10, MINUTE));
			assertEquals("10時間", toDateString(+10, HOUR));
			assertEquals("1月1日", toDateString(+10, DAY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testSecond() throws Exception {
		ClientConfigurationTestImpl configuration = getConf("sec");
		configuration.setGlobalInstance();
		try {
			assertEquals("2014/01/01 00:00:00", toDateString(-10, SEC));
			assertEquals("2014/01/01 00:00:00", toDateString(+10, SEC));
			assertEquals("2014/01/01 00:00:00", toDateString(+10, MINUTE));
			assertEquals("2014/01/01 00:00:00", toDateString(+10, HOUR));
			assertEquals("2014/01/01 00:00:00", toDateString(+10, DAY));
		} finally {
			configuration.clearGlobalInstance();
		}
	}

	@Test
	public void testSecondRelative() throws Exception {
		ClientConfigurationTestImpl configuration = getConf("sec+rel");
		configuration.setGlobalInstance();
		try {
			LocalizationResourceProxy.pushLocale(Locale.JAPAN);
			assertEquals("2014/01/01 00:00:00", toDateString(-10, SEC));
			assertEquals("10秒 (2014/01/01 00:00:00)", toDateString(+10, SEC));
			assertEquals("10分 (2014/01/01 00:00:00)", toDateString(+10, MINUTE));
			assertEquals("10時間 (2014/01/01 00:00:00)", toDateString(+10, HOUR));
			assertEquals("2014/01/01 00:00:00", toDateString(+10, DAY));
		} finally {
			LocalizationResourceProxy.popLocale();
			configuration.clearGlobalInstance();
		}
	}

	protected String toDateString(int time, int unit) {
		DateFormatter formatter = new DateFormatter();
		return formatter.toDateString(new Date(BASE_TIME), BASE_TIME + time * unit, false);
	}
}
