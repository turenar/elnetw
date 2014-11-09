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

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.conf.PropertyUpdateEvent;
import jp.mydns.turenar.twclient.conf.PropertyUpdateListener;

import static jp.mydns.turenar.twclient.Utility.DAY2MS;
import static jp.mydns.turenar.twclient.Utility.HOUR2MS;
import static jp.mydns.turenar.twclient.Utility.MINUTE2MS;
import static jp.mydns.turenar.twclient.Utility.SEC2MS;
import static jp.mydns.turenar.twclient.i18n.LocalizationResource.trb;
import static jp.mydns.turenar.twclient.i18n.LocalizationResource.trbc;
import static jp.mydns.turenar.twclient.i18n.LocalizationResource.trc;

/**
 * date formatter for SimpleRenderer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class DateFormatter implements PropertyUpdateListener {
	/**
	 * thread local storage of simple date format
	 */
	protected static class SimpleDateFormatThreadLocal extends ThreadLocal<SimpleDateFormat> {
		/**
		 * is this instance millisecond?
		 */
		protected boolean isMillisecond;

		/**
		 * make instance
		 *
		 * @param isMillisecond show time with millisecond?
		 */
		public SimpleDateFormatThreadLocal(boolean isMillisecond) {
			this.isMillisecond = isMillisecond;
		}

		@Override
		protected SimpleDateFormat initialValue() {
			return isMillisecond
					? new SimpleDateFormat(trc("date format", "yyyy-MM-dd HH:mm:ss.SSS"))
					: new SimpleDateFormat(trc("date format", "yyyy-MM-dd HH:mm:ss"));
		}
	}

	/**
	 * show relative time?
	 */
	protected static final int RELATIVE = 1;
	/**
	 * show absolute time without milliseconds?
	 */
	protected static final int SECOND = 2;
	/**
	 * show absolute time with milliseconds?
	 */
	protected static final int MILLISECOND = 4;
	/**
	 * show absolute time?
	 */
	protected static final int ABSOLUTE_BITMASK = SECOND | MILLISECOND;

	/**
	 * check bit
	 *
	 * @param haystack format
	 * @param needle   bit mask
	 * @return bit enabled?
	 */
	protected static boolean checkBit(int haystack, int needle) {
		return (haystack & needle) != 0;
	}

	/**
	 * format type
	 */
	protected volatile int format;
	/**
	 * {@link #dateFormatThreadLocal} is milliseconds instance?
	 */
	protected boolean dateFormatIsMillisecond;
	/**
	 * real formatter thread local storage.
	 */
	protected volatile ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new SimpleDateFormatThreadLocal(false);

	public DateFormatter() {
		ClientProperties configProperties = ClientConfiguration.getInstance().getConfigProperties();
		configProperties.addPropertyUpdatedListener(this);
		setFormat(configProperties.getProperty("gui.date.type"));
	}

	/**
	 * get formatter for formatting
	 *
	 * @param format format
	 * @return formatter
	 */
	protected synchronized SimpleDateFormat getDateFormatter(int format) {
		boolean milliSec = checkBit(format, MILLISECOND);
		if (dateFormatIsMillisecond != milliSec) {
			dateFormatThreadLocal = new SimpleDateFormatThreadLocal(milliSec);
			dateFormatIsMillisecond = milliSec;
		}
		return dateFormatThreadLocal.get();
	}

	@Override
	public void propertyUpdate(PropertyUpdateEvent evt) {
		if (evt.getPropertyName().equals("gui.date.type")) {
			setFormat(evt.getNewValue());
		}
	}

	/**
	 * set format from property value
	 *
	 * @param property property
	 */
	protected synchronized void setFormat(String property) {
		int format = 0;
		if (property.contains("rel")) {
			format |= RELATIVE;
		}
		if (property.contains("sec")) {
			format |= SECOND;
		} else if (property.contains("ms")) {
			format |= MILLISECOND;
		}
		this.format = format;
	}

	/**
	 * get string for date
	 *
	 * @param date created at
	 * @param html insert html?
	 * @return string for date
	 */
	public String toDateString(Date date, boolean html) {
		return toDateString(date, System.currentTimeMillis(), html);
	}

	/**
	 * get string for date
	 *
	 * @param date    created at
	 * @param nowTime now time (for debugging)
	 * @param html    insert html?
	 * @return string for date
	 */
	protected String toDateString(Date date, long nowTime, boolean html) {
		long timeDiff = nowTime - date.getTime();
		StringBuilder stringBuilder = new StringBuilder();
		int format = this.format;
		if (html) {
			stringBuilder.append("<html>");
		}
		boolean absolute = checkBit(format, ABSOLUTE_BITMASK);
		boolean relative = checkBit(format, RELATIVE);
		if (relative) {
			if (timeDiff < 0) {
				if (!absolute) {
					trb(stringBuilder, "future");
				}
			} else if (timeDiff < MINUTE2MS) {
				trbc(stringBuilder, "date short format", "%ds", timeDiff / SEC2MS);
			} else if (timeDiff < HOUR2MS) {
				trbc(stringBuilder, "date short format", "%dm", timeDiff / MINUTE2MS);
			} else if (timeDiff < DAY2MS) {
				trbc(stringBuilder, "date short format", "%dh", timeDiff / HOUR2MS);
			} else if (!absolute) {
				trbc(stringBuilder, "date short format", "%1$tb %1$td", date);
			}

			if (absolute && timeDiff >= 0 && timeDiff < DAY2MS) {
				stringBuilder.append(" (");
			}
		}
		if (absolute) {
			stringBuilder.append(getDateFormatter(format).format(date));

			if (relative && timeDiff >= 0 && timeDiff < DAY2MS) {
				stringBuilder.append(')');
			}
		}
		return stringBuilder.toString();
	}
}
