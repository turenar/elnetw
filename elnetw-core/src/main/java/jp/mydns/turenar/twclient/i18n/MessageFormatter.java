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

package jp.mydns.turenar.twclient.i18n;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Formatter;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.MissingFormatWidthException;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * MessageFormatter. This class provides like {@link java.util.Formatter}, but
 * formatter is reusable. It improves some performance
 */
public class MessageFormatter {
	private interface FormatFragment {
		int index();

		void print(Appendable target, Object arg) throws IOException;

		String toString();
	}

	private static class Conversion {
		// Byte, Short, Integer, Long, BigInteger
		// (and associated primitives due to autoboxing)
		/*package*/static final char DECIMAL_INTEGER = 'd';
		/*package*/static final char OCTAL_INTEGER = 'o';
		/*package*/static final char HEXADECIMAL_INTEGER = 'x';
		/*package*/static final char HEXADECIMAL_INTEGER_UPPER = 'X';

		// Float, Double, BigDecimal
		// (and associated primitives due to autoboxing)
		/*package*/static final char SCIENTIFIC = 'e';
		/*package*/static final char SCIENTIFIC_UPPER = 'E';
		/*package*/static final char GENERAL = 'g';
		/*package*/static final char GENERAL_UPPER = 'G';
		/*package*/static final char DECIMAL_FLOAT = 'f';
		/*package*/static final char HEXADECIMAL_FLOAT = 'a';
		/*package*/static final char HEXADECIMAL_FLOAT_UPPER = 'A';

		// Character, Byte, Short, Integer
		// (and associated primitives due to autoboxing)
		/*package*/static final char CHARACTER = 'c';
		/*package*/static final char CHARACTER_UPPER = 'C';

		// if (arg.TYPE != boolean) return boolean
		// if (arg != null) return true; else return false;
		/*package*/static final char BOOLEAN = 'b';
		/*package*/static final char BOOLEAN_UPPER = 'B';
		// arg.toString();
		/*package*/static final char STRING = 's';
		/*package*/static final char STRING_UPPER = 'S';
		// arg.hashCode()
		/*package*/static final char HASHCODE = 'h';
		/*package*/static final char HASHCODE_UPPER = 'H';

		/*package*/static final char LINE_SEPARATOR = 'n';
		/*package*/static final char PERCENT_SIGN = '%';

		// Returns true iff the Conversion is applicable to character.
		/*package*/
		static boolean isCharacter(char c) {
			switch (c) {
				case CHARACTER:
				case CHARACTER_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion is a floating-point type.
		/*package*/
		static boolean isFloat(char c) {
			switch (c) {
				case SCIENTIFIC:
				case SCIENTIFIC_UPPER:
				case GENERAL:
				case GENERAL_UPPER:
				case DECIMAL_FLOAT:
				case HEXADECIMAL_FLOAT:
				case HEXADECIMAL_FLOAT_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion is applicable to all objects.
		/*package*/
		static boolean isGeneral(char c) {
			switch (c) {
				case BOOLEAN:
				case BOOLEAN_UPPER:
				case STRING:
				case STRING_UPPER:
				case HASHCODE:
				case HASHCODE_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion is an integer type.
		/*package*/
		static boolean isInteger(char c) {
			switch (c) {
				case DECIMAL_INTEGER:
				case OCTAL_INTEGER:
				case HEXADECIMAL_INTEGER:
				case HEXADECIMAL_INTEGER_UPPER:
					return true;
				default:
					return false;
			}
		}

		// Returns true iff the Conversion does not require an argument
		/*package*/
		static boolean isText(char c) {
			switch (c) {
				case LINE_SEPARATOR:
				case PERCENT_SIGN:
					return true;
				default:
					return false;
			}
		}

		/*package*/
		static boolean isValid(char c) {
			return isGeneral(c) || isInteger(c) || isFloat(c) || isText(c)
					|| c == 't' || isCharacter(c);
		}
	}

	private static class DateTime {
		/*package*/static final char HOUR_OF_DAY_0 = 'H'; // (00 - 23)
		/*package*/static final char HOUR_0 = 'I'; // (01 - 12)
		/*package*/static final char HOUR_OF_DAY = 'k'; // (0 - 23) -- like H
		/*package*/static final char HOUR = 'l'; // (1 - 12) -- like I
		/*package*/static final char MINUTE = 'M'; // (00 - 59)
		/*package*/static final char NANOSECOND = 'N'; // (000000000 - 999999999)
		/*package*/static final char MILLISECOND = 'L'; // (000 - 999)
		/*package*/static final char MILLISECOND_SINCE_EPOCH = 'Q'; // (0 - 99...?)
		/*package*/static final char AM_PM = 'p'; // (am or pm)
		/*package*/static final char SECONDS_SINCE_EPOCH = 's'; // (0 - 99...?)
		/*package*/static final char SECOND = 'S'; // (00 - 60 - leap second)
		/*package*/static final char TIME = 'T'; // (24 hour hh:mm:ss)
		/*package*/static final char ZONE_NUMERIC = 'z'; // (-1200 - +1200) - ls minus?
		/*package*/static final char ZONE = 'Z'; // (symbol)

		// Date
		/*package*/static final char NAME_OF_DAY_ABBREV = 'a'; // 'a'
		/*package*/static final char NAME_OF_DAY = 'A'; // 'A'
		/*package*/static final char NAME_OF_MONTH_ABBREV = 'b'; // 'b'
		/*package*/static final char NAME_OF_MONTH = 'B'; // 'B'
		/*package*/static final char CENTURY = 'C'; // (00 - 99)
		/*package*/static final char DAY_OF_MONTH_0 = 'd'; // (01 - 31)
		/*package*/static final char DAY_OF_MONTH = 'e'; // (1 - 31) -- like d
		/*package*/static final char NAME_OF_MONTH_ABBREV_X = 'h'; // -- same b
		/*package*/static final char DAY_OF_YEAR = 'j'; // (001 - 366)
		/*package*/static final char MONTH = 'm'; // (01 - 12)
		/*package*/static final char YEAR_2 = 'y'; // (00 - 99)
		/*package*/static final char YEAR_4 = 'Y'; // (0000 - 9999)

		// Composites
		/*package*/static final char TIME_12_HOUR = 'r'; // (hh:mm:ss [AP]M)
		/*package*/static final char TIME_24_HOUR = 'R'; // (hh:mm same as %H:%M)
		/*package*/static final char DATE_TIME = 'c'; // (Sat Nov 04 12:02:33 EST 1999)
		/*package*/static final char DATE = 'D'; // (mm/dd/yy)
		/*package*/static final char ISO_STANDARD_DATE = 'F'; // (%Y-%m-%d)

		/*package*/
		static boolean isValid(char c) {
			switch (c) {
				case HOUR_OF_DAY_0:
				case HOUR_0:
				case HOUR_OF_DAY:
				case HOUR:
				case MINUTE:
				case NANOSECOND:
				case MILLISECOND:
				case MILLISECOND_SINCE_EPOCH:
				case AM_PM:
				case SECONDS_SINCE_EPOCH:
				case SECOND:
				case TIME:
				case ZONE_NUMERIC:
				case ZONE:
					// Date
				case NAME_OF_DAY_ABBREV:
				case NAME_OF_DAY:
				case NAME_OF_MONTH_ABBREV:
				case NAME_OF_MONTH:
				case CENTURY:
				case DAY_OF_MONTH_0:
				case DAY_OF_MONTH:
				case NAME_OF_MONTH_ABBREV_X:
				case DAY_OF_YEAR:
				case MONTH:
				case YEAR_2:
				case YEAR_4:
					// Composites
				case TIME_12_HOUR:
				case TIME_24_HOUR:
				case DATE_TIME:
				case DATE:
				case ISO_STANDARD_DATE:
					return true;
				default:
					return false;
			}
		}
	}

	private static class Flags {
		/*package*/static final Flags NONE = new Flags(0);      // ''
		/*package*/static final Flags LEFT_JUSTIFY = new Flags(1);   // '-'
		/*package*/static final Flags UPPERCASE = new Flags(1 << 1);   // '^'
		/*package*/static final Flags ALTERNATE = new Flags(1 << 2);   // '#'
		// numerics
		/*package*/static final Flags PLUS = new Flags(1 << 3);   // '+'
		/*package*/static final Flags LEADING_SPACE = new Flags(1 << 4);   // ' '
		/*package*/static final Flags ZERO_PAD = new Flags(1 << 5);   // '0'
		/*package*/static final Flags GROUP = new Flags(1 << 6);   // ','
		/*package*/static final Flags PARENTHESES = new Flags(1 << 7);   // '('
		// indexing
		/*package*/static final Flags PREVIOUS = new Flags(1 << 8);   // '<'

		public static Flags parse(String s) {
			char[] charArray = s.toCharArray();
			Flags f = new Flags(0);
			for (char c : charArray) {
				Flags value = parse(c);
				if (f.contains(value)) {
					throw new DuplicateFormatFlagsException(value.toString());
				}
				f.add(value);
			}
			return f;
		}

		// parse those flags which may be provided by users
		private static Flags parse(char c) {
			switch (c) {
				case '-':
					return LEFT_JUSTIFY;
				case '#':
					return ALTERNATE;
				case '+':
					return PLUS;
				case ' ':
					return LEADING_SPACE;
				case '0':
					return ZERO_PAD;
				case ',':
					return GROUP;
				case '(':
					return PARENTHESES;
				case '<':
					return PREVIOUS;
				default:
					throw new UnknownFormatFlagsException(String.valueOf(c));
			}
		}

		// Returns a string representation of the current {@code Flags}.
		public static String toString(Flags f) {
			return f.toString();
		}

		private int flags;

		private Flags(int f) {
			flags = f;
		}

		private Flags add(Flags f) {
			flags |= f.valueOf();
			return this;
		}

		public boolean contains(Flags f) {
			return (flags & f.valueOf()) == f.valueOf();
		}

		public Flags dup() {
			return new Flags(flags);
		}

		public Flags remove(Flags f) {
			flags &= ~f.valueOf();
			return this;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (contains(LEFT_JUSTIFY)) {
				sb.append('-');
			}
			if (contains(UPPERCASE)) {
				sb.append('^');
			}
			if (contains(ALTERNATE)) {
				sb.append('#');
			}
			if (contains(PLUS)) {
				sb.append('+');
			}
			if (contains(LEADING_SPACE)) {
				sb.append(' ');
			}
			if (contains(ZERO_PAD)) {
				sb.append('0');
			}
			if (contains(GROUP)) {
				sb.append(',');
			}
			if (contains(PARENTHESES)) {
				sb.append('(');
			}
			if (contains(PREVIOUS)) {
				sb.append('<');
			}
			return sb.toString();
		}

		public int valueOf() {
			return flags;
		}
	}

	private class FixedString implements FormatFragment {
		private String string;

		FixedString(String string) {
			this.string = string;
		}

		@Override
		public int index() {
			return FIXED_STRING_INDEX;
		}

		@Override
		public void print(Appendable target, Object arg) throws IOException {
			target.append(string);
		}

		@Override
		public String toString() {
			return string;
		}
	}

	private class FormatSpecifier implements FormatFragment {
		private static final int HOUR_OF_HALF_DAY = 12;
		private static final int SECONDS_IN_MINUTE = 60;
		private static final int MSEC_IN_SECOND = 1000;
		private static final int YEARS_IN_CENTURY = 100;

		private final String rawFormat;
		@Nonnull
		private final Locale locale;
		private int index = PREVIOUS_INDEX;
		private Flags flag = Flags.NONE;
		private int width;
		private int precision;
		private boolean isDateConversion = false;
		private char conversionChar;

		/*package*/FormatSpecifier(Matcher m, Locale locale) {
			this.locale = locale;

			rawFormat = m.group(0);
			index(m.group(1));
			flags(m.group(2));
			width(m.group(3));
			precision(m.group(4));

			String tT = m.group(5);
			if (tT != null) {
				isDateConversion = true;
				if (tT.equals("T")) {
					flag.add(Flags.UPPERCASE);
				}
			}

			conversion(m.group(6));

			if (isDateConversion) {
				checkDateTime();
			} else if (Conversion.isGeneral(conversionChar)) {
				checkGeneral();
			} else if (Conversion.isCharacter(conversionChar)) {
				checkCharacter();
			} else if (Conversion.isInteger(conversionChar)) {
				checkInteger();
			} else if (Conversion.isFloat(conversionChar)) {
				checkFloat();
			} else if (Conversion.isText(conversionChar)) {
				checkText();
			} else {
				throw new UnknownFormatConversionException(String.valueOf(conversionChar));
			}
		}

		private int adjustWidth(int width, Flags f, boolean negative) {
			int newWidth = width;
			if (newWidth != -1 && negative && f.contains(Flags.PARENTHESES)) {
				newWidth--;
			}
			return newWidth;
		}

		private void checkBadFlags(Flags... badFlags) {
			for (Flags badFlag : badFlags) {
				if (flag.contains(badFlag)) {
					failMismatch(badFlag, conversionChar);
				}
			}
		}

		private void checkCharacter() {
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}
			checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
					Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
			// '-' requires a width
			if (width == -1 && flag.contains(Flags.LEFT_JUSTIFY)) {
				throw new MissingFormatWidthException(toString());
			}
		}

		private void checkDateTime() {
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}
			if (!DateTime.isValid(conversionChar)) {
				throw new UnknownFormatConversionException("t" + conversionChar);
			}
			checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
					Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
			// '-' requires a width
			if (width == -1 && flag.contains(Flags.LEFT_JUSTIFY)) {
				throw new MissingFormatWidthException(toString());
			}
		}

		private void checkFloat() {
			checkNumeric();
			if (conversionChar == Conversion.DECIMAL_FLOAT) {
				// do nothing
			} else if (conversionChar == Conversion.HEXADECIMAL_FLOAT) {
				checkBadFlags(Flags.PARENTHESES, Flags.GROUP);
			} else if (conversionChar == Conversion.SCIENTIFIC) {
				checkBadFlags(Flags.GROUP);
			} else if (conversionChar == Conversion.GENERAL) {
				checkBadFlags(Flags.ALTERNATE);
			}
		}

		private void checkGeneral() {
			if ((conversionChar == Conversion.BOOLEAN || conversionChar == Conversion.HASHCODE)
					&& flag.contains(Flags.ALTERNATE)) {
				failMismatch(Flags.ALTERNATE, conversionChar);
			}
			// '-' requires a width
			if (width == -1 && flag.contains(Flags.LEFT_JUSTIFY)) {
				throw new MissingFormatWidthException(toString());
			}
			checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD,
					Flags.GROUP, Flags.PARENTHESES);
		}

		private void checkInteger() {
			checkNumeric();
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}

			if (conversionChar == Conversion.DECIMAL_INTEGER) {
				checkBadFlags(Flags.ALTERNATE);
			} else {
				checkBadFlags(Flags.GROUP);
			}
		}

		private void checkNumeric() {
			if (width != -1 && width < 0) {
				throw new IllegalFormatWidthException(width);
			}

			if (precision != -1 && precision < 0) {
				throw new IllegalFormatPrecisionException(precision);
			}

			// '-' and '0' require a width
			if (width == -1
					&& (flag.contains(Flags.LEFT_JUSTIFY) || flag.contains(Flags.ZERO_PAD))) {
				throw new MissingFormatWidthException(toString());
			}

			// bad combination
			if ((flag.contains(Flags.PLUS) && flag.contains(Flags.LEADING_SPACE))
					|| (flag.contains(Flags.LEFT_JUSTIFY) && flag.contains(Flags.ZERO_PAD))) {
				throw new IllegalFormatFlagsException(flag.toString());
			}
		}

		private void checkText() {
			if (precision != -1) {
				throw new IllegalFormatPrecisionException(precision);
			}
			switch (conversionChar) {
				case Conversion.PERCENT_SIGN:
					if (flag.valueOf() != Flags.LEFT_JUSTIFY.valueOf()
							&& flag.valueOf() != Flags.NONE.valueOf()) {
						throw new IllegalFormatFlagsException(flag.toString());
					}
					// '-' requires a width
					if (width == -1 && flag.contains(Flags.LEFT_JUSTIFY)) {
						throw new MissingFormatWidthException(toString());
					}
					break;
				case Conversion.LINE_SEPARATOR:
					if (width != -1) {
						throw new IllegalFormatWidthException(width);
					}
					if (flag.valueOf() != Flags.NONE.valueOf()) {
						throw new IllegalFormatFlagsException(flag.toString());
					}
					break;
				default:
					throw new AssertionError();
			}
		}

		private char conversion(String s) {
			conversionChar = s.charAt(0);
			if (!isDateConversion) {
				if (!Conversion.isValid(conversionChar)) {
					throw new UnknownFormatConversionException(String.valueOf(conversionChar));
				}
				if (Character.isUpperCase(conversionChar)) {
					flag.add(Flags.UPPERCASE);
				}
				conversionChar = Character.toLowerCase(conversionChar);
				if (Conversion.isText(conversionChar)) {
					index = FIXED_STRING_INDEX;
				}
			}
			return conversionChar;
		}

		private void failConversion(char c, Object arg) {
			throw new IllegalFormatConversionException(c, arg.getClass());
		}

		private void failMismatch(Flags f, char c) {
			throw new FormatFlagsConversionMismatchException(f.toString(), c);
		}

		private Flags flags(String s) {
			flag = Flags.parse(s);
			if (flag.contains(Flags.PREVIOUS)) {
				index = PREVIOUS_INDEX;
			}
			return flag;
		}

		private StringBuilder formatCalendar(StringBuilder sb, Calendar t, char c) throws IOException { // CS-IGNORE
			if (sb == null) {
				sb = new StringBuilder();
			}
			switch (c) {
				case DateTime.HOUR_OF_DAY_0: // 'H' (00 - 23)
				case DateTime.HOUR_0:        // 'I' (01 - 12)
				case DateTime.HOUR_OF_DAY:   // 'k' (0 - 23) -- like H
				case DateTime.HOUR: { // 'l' (1 - 12) -- like I
					int i = t.get(Calendar.HOUR_OF_DAY);
					if (c == DateTime.HOUR_0 || c == DateTime.HOUR) {
						i = (i == 0 || i == HOUR_OF_HALF_DAY) ? HOUR_OF_HALF_DAY : i % HOUR_OF_HALF_DAY;
					}
					Flags flags = (c == DateTime.HOUR_OF_DAY_0 || c == DateTime.HOUR_0)
							? Flags.ZERO_PAD
							: Flags.NONE;
					sb.append(localizedMagnitude(null, i, flags, 2));
					break;
				}
				case DateTime.MINUTE: { // 'M' (00 - 59)
					int i = t.get(Calendar.MINUTE);
					sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2));
					break;
				}
				case DateTime.NANOSECOND: { // 'N' (000000000 - 999999999)
					int i = t.get(Calendar.MILLISECOND) * 1000000; // CS-IGNORE
					sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 9));
					break;
				}
				case DateTime.MILLISECOND: { // 'L' (000 - 999)
					int i = t.get(Calendar.MILLISECOND);
					sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 3));
					break;
				}
				case DateTime.MILLISECOND_SINCE_EPOCH: { // 'Q' (0 - 99...?)
					long i = t.getTimeInMillis();
					sb.append(localizedMagnitude(null, i, Flags.NONE, width));
					break;
				}
				case DateTime.AM_PM: { // 'p' (am or pm)
					// Calendar.AM = 0, Calendar.PM = 1, LocaleElements defines upper
					DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
					String[] amPm = dfs.getAmPmStrings();
					String s = amPm[t.get(Calendar.AM_PM)];
					sb.append(s.toLowerCase(locale));
					break;
				}
				case DateTime.SECONDS_SINCE_EPOCH: { // 's' (0 - 99...?)
					long i = t.getTimeInMillis() / 1000; // CS-IGNORE
					sb.append(localizedMagnitude(null, i, Flags.NONE, width));
					break;
				}
				case DateTime.SECOND: { // 'S' (00 - 60 - leap second)
					int i = t.get(Calendar.SECOND);
					sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2));
					break;
				}
				case DateTime.ZONE_NUMERIC: { // 'z' ({-|+}####) - ls minus?
					int i = t.get(Calendar.ZONE_OFFSET) + t.get(Calendar.DST_OFFSET);
					boolean negative = i < 0;
					sb.append(negative ? '-' : '+');
					if (negative) {
						i = -i;
					}
					int min = i / (SECONDS_IN_MINUTE * MSEC_IN_SECOND);
					// combine minute and hour into a single integer
					int offset = (min / SECONDS_IN_MINUTE) * 100 + (min % SECONDS_IN_MINUTE); // CS-IGNORE

					sb.append(localizedMagnitude(null, offset, Flags.ZERO_PAD, 4));
					break;
				}
				case DateTime.ZONE: { // 'Z' (symbol)
					TimeZone tz = t.getTimeZone();
					sb.append(tz.getDisplayName(t.get(Calendar.DST_OFFSET) != 0,
							TimeZone.SHORT));
					break;
				}

				// Date
				case DateTime.NAME_OF_DAY_ABBREV:     // 'a'
				case DateTime.NAME_OF_DAY: { // 'A'
					int i = t.get(Calendar.DAY_OF_WEEK);
					DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
					if (c == DateTime.NAME_OF_DAY) {
						sb.append(dfs.getWeekdays()[i]);
					} else {
						sb.append(dfs.getShortWeekdays()[i]);
					}
					break;
				}
				case DateTime.NAME_OF_MONTH_ABBREV:   // 'b'
				case DateTime.NAME_OF_MONTH_ABBREV_X: // 'h' -- same b
				case DateTime.NAME_OF_MONTH: { // 'B'
					int i = t.get(Calendar.MONTH);
					DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
					if (c == DateTime.NAME_OF_MONTH) {
						sb.append(dfs.getMonths()[i]);
					} else {
						sb.append(dfs.getShortMonths()[i]);
					}
					break;
				}
				case DateTime.CENTURY:                // 'C' (00 - 99)
				case DateTime.YEAR_2:                 // 'y' (00 - 99)
				case DateTime.YEAR_4: { // 'Y' (0000 - 9999)
					int i = t.get(Calendar.YEAR);
					int size = 2;
					switch (c) {
						case DateTime.CENTURY:
							i /= YEARS_IN_CENTURY;
							break;
						case DateTime.YEAR_2:
							i %= 100; // CS-IGNORE
							break;
						case DateTime.YEAR_4:
							size = 4;
							break;
						default:
							throw new AssertionError(); // parent case is wrong
					}
					Flags flags = Flags.ZERO_PAD;
					sb.append(localizedMagnitude(null, i, flags, size));
					break;
				}
				case DateTime.DAY_OF_MONTH_0:         // 'd' (01 - 31)
				case DateTime.DAY_OF_MONTH: { // 'e' (1 - 31) -- like d
					int i = t.get(Calendar.DATE);
					Flags flags = (c == DateTime.DAY_OF_MONTH_0) ? Flags.ZERO_PAD : Flags.NONE;
					sb.append(localizedMagnitude(null, i, flags, 2));
					break;
				}
				case DateTime.DAY_OF_YEAR: { // 'j' (001 - 366)
					int i = t.get(Calendar.DAY_OF_YEAR);
					sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 3));
					break;
				}
				case DateTime.MONTH: { // 'm' (01 - 12)
					int i = t.get(Calendar.MONTH) + 1;
					sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2));
					break;
				}

				// Composites
				case DateTime.TIME:         // 'T' (24 hour hh:mm:ss - %tH:%tM:%tS)
				case DateTime.TIME_24_HOUR: { // 'R' (hh:mm same as %H:%M)
					char sep = ':';
					formatCalendar(sb, t, DateTime.HOUR_OF_DAY_0).append(sep);
					formatCalendar(sb, t, DateTime.MINUTE);
					if (c == DateTime.TIME) {
						sb.append(sep);
						print(sb, t, DateTime.SECOND);
					}
					break;
				}
				case DateTime.TIME_12_HOUR: { // 'r' (hh:mm:ss [AP]M)
					char sep = ':';
					formatCalendar(sb, t, DateTime.HOUR_0).append(sep);
					formatCalendar(sb, t, DateTime.MINUTE).append(sep);
					formatCalendar(sb, t, DateTime.SECOND).append(' ');
					// this may be in wrong place for some locales
					StringBuilder tsb = new StringBuilder();
					print(tsb, t, DateTime.AM_PM);
					sb.append(tsb.toString().toUpperCase(locale));
					break;
				}
				case DateTime.DATE_TIME: { // 'c' (Sat Nov 04 12:02:33 EST 1999)
					char sep = ' ';
					formatCalendar(sb, t, DateTime.NAME_OF_DAY_ABBREV).append(sep);
					formatCalendar(sb, t, DateTime.NAME_OF_MONTH_ABBREV).append(sep);
					formatCalendar(sb, t, DateTime.DAY_OF_MONTH_0).append(sep);
					formatCalendar(sb, t, DateTime.TIME).append(sep);
					formatCalendar(sb, t, DateTime.ZONE).append(sep);
					formatCalendar(sb, t, DateTime.YEAR_4);
					break;
				}
				case DateTime.DATE: { // 'D' (mm/dd/yy)
					char sep = '/';
					formatCalendar(sb, t, DateTime.MONTH).append(sep);
					formatCalendar(sb, t, DateTime.DAY_OF_MONTH_0).append(sep);
					formatCalendar(sb, t, DateTime.YEAR_2);
					break;
				}
				case DateTime.ISO_STANDARD_DATE: { // 'F' (%Y-%m-%d)
					char sep = '-';
					formatCalendar(sb, t, DateTime.YEAR_4).append(sep);
					formatCalendar(sb, t, DateTime.MONTH).append(sep);
					formatCalendar(sb, t, DateTime.DAY_OF_MONTH_0);
					break;
				}
				default:
					throw new AssertionError();
			}
			return sb;
		}

		private char[] getCharArrayWithoutSign(long l) {
			if (l == Long.MIN_VALUE) {
				return "-9223372036854775808".toCharArray();
			}
			if (l < 0) {
				l = -l;
			}
			char[] chars = new char[stringSize(l)];
			int i = chars.length;
			do {
				long n = l / 10;
				long a = l - n * 10;
				l = n;
				chars[--i] = (char) (a + zero);
			} while (l != 0);
			return chars;
		}

		public String getRawFormat() {
			return rawFormat;
		}

		private int index(String s) {
			if (s != null) {
				try {
					index = Integer.parseInt(s.substring(0, s.length() - 1));
				} catch (NumberFormatException x) {
					// regex or overflow error?
					throw new RuntimeException("Illegal index", x);
				}
			} else {
				index = 0;
			}
			return index;
		}

		public int index() {
			return index;
		}

		private String justify(String s) {
			if (width == -1) {
				return s;
			}
			StringBuilder sb = new StringBuilder();
			boolean pad = flag.contains(Flags.LEFT_JUSTIFY);
			int sp = width - s.length();
			if (!pad) {
				for (int i = 0; i < sp; i++) {
					sb.append(' ');
				}
			}
			sb.append(s);
			if (pad) {
				for (int i = 0; i < sp; i++) {
					sb.append(' ');
				}
			}
			return sb.toString();
		}

		// negative := val < 0
		private StringBuilder leadingSign(StringBuilder sb, boolean negative) {
			if (!negative) {
				if (flag.contains(Flags.PLUS)) {
					sb.append('+');
				} else if (flag.contains(Flags.LEADING_SPACE)) {
					sb.append(' ');
				}
			} else {
				if (flag.contains(Flags.PARENTHESES)) {
					sb.append('(');
				} else {
					sb.append('-');
				}
			}
			return sb;
		}

		private StringBuilder localizedMagnitude(StringBuilder sb, long value, Flags f, int width) {
			char[] va = getCharArrayWithoutSign(value);
			return localizedMagnitude(sb, va, f, width);
		}

		private StringBuilder localizedMagnitude(StringBuilder sb, char[] value, Flags f, int width) {
			if (sb == null) {
				sb = new StringBuilder();
			}
			int begin = sb.length();

			char zero = MessageFormatter.this.zero;

			// determine localized grouping separator and size
			char grpSep = '\0';
			int grpSize = -1;
			char decSep = '\0';

			int len = value.length;
			int dot = len;
			for (int j = 0; j < len; j++) {
				if (value[j] == '.') {
					dot = j;
					break;
				}
			}

			if (dot < len) {
				decSep = decimalFormatSymbols.getDecimalSeparator();
			}

			if (f.contains(Flags.GROUP)) {
				grpSep = decimalFormatSymbols.getGroupingSeparator();
				DecimalFormat df = (DecimalFormat) NumberFormat.getIntegerInstance(locale);
				grpSize = df.getGroupingSize();
			}

			// localize the digits inserting group separators as necessary
			for (int j = 0; j < len; j++) {
				if (j == dot) {
					sb.append(decSep);
					// no more group separators after the decimal separator
					grpSep = '\0';
					continue;
				}

				char c = value[j];
				sb.append((char) ((c - '0') + zero));
				if (grpSep != '\0' && j != dot - 1 && ((dot - j) % grpSize == 1)) {
					sb.append(grpSep);
				}
			}

			// apply zero padding
			len = sb.length();
			if (width != -1 && f.contains(Flags.ZERO_PAD)) {
				for (int k = 0; k < width - len; k++) {
					sb.insert(begin, zero);
				}
			}

			return sb;
		}

		private int precision(String s) {
			precision = -1;
			if (s != null) {
				try {
					precision = Integer.parseInt(s);
					if (precision < 0) {
						throw new IllegalFormatPrecisionException(precision);
					}
				} catch (NumberFormatException x) {
					throw new RuntimeException("Illegal precision", x);
				}
			}
			return precision;
		}

		public void print(Appendable target, Object arg) throws IOException {
			if (isDateConversion) {
				printDateTime(target, arg);
				return;
			}
			switch (conversionChar) {
				case Conversion.DECIMAL_INTEGER:
				case Conversion.OCTAL_INTEGER:
				case Conversion.HEXADECIMAL_INTEGER:
					printInteger(target, arg);
					break;
				case Conversion.SCIENTIFIC:
				case Conversion.GENERAL:
				case Conversion.DECIMAL_FLOAT:
				case Conversion.HEXADECIMAL_FLOAT:
					printFloat(target, arg);
					break;
				case Conversion.CHARACTER:
				case Conversion.CHARACTER_UPPER:
					printCharacter(target, arg);
					break;
				case Conversion.BOOLEAN:
					printBoolean(target, arg);
					break;
				case Conversion.STRING:
					printString(target, arg);
					break;
				case Conversion.HASHCODE:
					printHashCode(target, arg);
					break;
				case Conversion.LINE_SEPARATOR:
					target.append(System.lineSeparator());
					break;
				case Conversion.PERCENT_SIGN:
					target.append('%');
					break;
				default:
					throw new AssertionError();
			}
		}

		private void print(Appendable target, String s) throws IOException {
			if (precision != -1 && precision < s.length()) {
				s = s.substring(0, precision);
			}
			if (flag.contains(Flags.UPPERCASE)) {
				s = s.toUpperCase();
			}
			target.append(justify(s));
		}

		private void print(Appendable target, byte value) throws IOException {
			long v = value;
			if (value < 0
					&& (conversionChar == Conversion.OCTAL_INTEGER
					|| conversionChar == Conversion.HEXADECIMAL_INTEGER)) {
				v += 1L << Byte.SIZE; // cut 0xffff... header
			}
			print(target, v);
		}

		private void print(Appendable target, short value) throws IOException {
			long v = value;
			if (value < 0
					&& (conversionChar == Conversion.OCTAL_INTEGER
					|| conversionChar == Conversion.HEXADECIMAL_INTEGER)) {
				v += 1L << Short.SIZE; // cut 0xffff... header
			}
			print(target, v);
		}

		private void print(Appendable target, int value) throws IOException {
			long v = value;
			if (value < 0
					&& (conversionChar == Conversion.OCTAL_INTEGER
					|| conversionChar == Conversion.HEXADECIMAL_INTEGER)) {
				v += 1L << Integer.SIZE; // cut 0xffff... header
			}
			print(target, v);
		}

		private void print(Appendable target, long value) throws IOException {
			StringBuilder sb = new StringBuilder();

			switch (conversionChar) {
				case Conversion.DECIMAL_INTEGER: {
					boolean negative = value < 0;
					char[] va = getCharArrayWithoutSign(value);

					// leading sign indicator
					leadingSign(sb, negative);

					// the value
					localizedMagnitude(sb, va, flag, adjustWidth(width, flag, negative));

					// trailing sign indicator
					trailingSign(sb, negative);
					break;
				}
				case Conversion.OCTAL_INTEGER: {
					checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE, Flags.PLUS);
					String s = Long.toOctalString(value);

					// apply ALTERNATE (radix indicator for octal) before ZERO_PAD
					if (flag.contains(Flags.ALTERNATE)) {
						sb.append('0');
					}
					if (flag.contains(Flags.ZERO_PAD)) {
						int len = flag.contains(Flags.ALTERNATE) ? s.length() + 1 : s.length();
						for (int i = 0; i < width - len; i++) {
							sb.append('0');
						}
					}
					sb.append(s);
					break;
				}
				case Conversion.HEXADECIMAL_INTEGER: {
					checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE, Flags.PLUS);
					String s = Long.toHexString(value);

					// apply ALTERNATE (radix indicator for hex) before ZERO_PAD
					if (flag.contains(Flags.ALTERNATE)) {
						sb.append(flag.contains(Flags.UPPERCASE) ? "0X" : "0x");
					}
					if (flag.contains(Flags.ZERO_PAD)) {
						int len = flag.contains(Flags.ALTERNATE) ? s.length() + 2 : s.length();
						for (int i = 0; i < width - len; i++) {
							sb.append('0');
						}
					}
					if (flag.contains(Flags.UPPERCASE)) {
						s = s.toUpperCase();
					}
					sb.append(s);
					break;
				}
				default:
					throw new AssertionError();
			}

			// justify based on width
			target.append(justify(sb.toString()));
		}

		private void print(Appendable target, float value) throws IOException {
			print(target, (double) value);
		}

		private void print(Appendable target, double value) throws IOException {
			StringBuilder sb = new StringBuilder();
			boolean negative = Double.compare(value, 0.0) == -1;

			if (Double.isNaN(value)) {
				sb.append(flag.contains(Flags.UPPERCASE) ? "NAN" : "NaN");
			} else {
				double v = Math.abs(value);

				// leading sign indicator
				leadingSign(sb, negative);

				// the value
				if (Double.isInfinite(v)) {
					sb.append(flag.contains(Flags.UPPERCASE) ? "INFINITY" : "Infinity");
				} else {
					new Formatter(sb).format(locale, getRawFormat(), v);
				}

				// trailing sign indicator
				trailingSign(sb, negative);
			}

			// justify based on width
			target.append(justify(sb.toString()));
		}

		private void print(Appendable target, Calendar t, char c) throws IOException {
			StringBuilder sb = new StringBuilder();
			formatCalendar(sb, t, c);

			// justify based on width
			String s = justify(sb.toString());
			if (flag.contains(Flags.UPPERCASE)) {
				s = s.toUpperCase();
			}

			target.append(s);
		}

		private void printBoolean(Appendable target, Object arg) throws IOException {
			String s;
			if (arg != null) {
				s = arg instanceof Boolean ? arg.toString() : Boolean.toString(true);
			} else {
				s = Boolean.toString(false);
			}
			print(target, s);
		}

		private void printCharacter(Appendable target, Object arg) throws IOException {
			if (arg == null) {
				print(target, "null");
				return;
			}
			String s = null;
			if (arg instanceof Character) {
				s = arg.toString();
			} else if (arg instanceof Byte) {
				byte i = (Byte) arg;
				if (Character.isValidCodePoint(i)) {
					s = new String(Character.toChars(i));
				} else {
					throw new IllegalFormatCodePointException(i);
				}
			} else if (arg instanceof Short) {
				short i = (Short) arg;
				if (Character.isValidCodePoint(i)) {
					s = new String(Character.toChars(i));
				} else {
					throw new IllegalFormatCodePointException(i);
				}
			} else if (arg instanceof Integer) {
				int i = (Integer) arg;
				if (Character.isValidCodePoint(i)) {
					s = new String(Character.toChars(i));
				} else {
					throw new IllegalFormatCodePointException(i);
				}
			} else {
				failConversion(conversionChar, arg);
			}
			print(target, s);
		}

		private void printDateTime(Appendable target, Object arg) throws IOException {
			if (arg == null) {
				print(target, "null");
				return;
			}
			Calendar cal = null;

			// Instead of Calendar.setLenient(true), perhaps we should
			// wrap the IllegalArgumentException that might be thrown?
			if (arg instanceof Long) {
				cal = Calendar.getInstance(locale);
				cal.setTimeInMillis((Long) arg);
			} else if (arg instanceof Date) {
				cal = Calendar.getInstance(locale);
				cal.setTime((Date) arg);
			} else if (arg instanceof Calendar) {
				cal = (Calendar) ((Calendar) arg).clone();
				cal.setLenient(true);
			} else {
				failConversion(conversionChar, arg);
			}
			// Use the provided locale so that invocations of
			// localizedMagnitude() use optimizations for null.
			print(target, cal, conversionChar);
		}

		private void printFloat(Appendable target, Object arg) throws IOException {
			if (arg == null) {
				print(target, "null");
			} else if (arg instanceof Float) {
				print(target, ((Float) arg).floatValue());
			} else if (arg instanceof Double) {
				print(target, ((Double) arg).doubleValue());
			} else {
				failConversion(conversionChar, arg);
			}
		}

		private void printHashCode(Appendable target, Object arg) throws IOException {
			String s = arg == null ? "null" : Integer.toHexString(arg.hashCode());
			print(target, s);
		}

		private void printInteger(Appendable target, Object arg) throws IOException {
			if (arg == null) {
				print(target, "null");
			} else if (arg instanceof Byte) {
				print(target, ((Byte) arg).byteValue());
			} else if (arg instanceof Short) {
				print(target, ((Short) arg).shortValue());
			} else if (arg instanceof Integer) {
				print(target, ((Integer) arg).intValue());
			} else if (arg instanceof Long) {
				print(target, ((Long) arg).longValue());
			} else {
				failConversion(conversionChar, arg);
			}
		}

		private void printString(Appendable target, Object arg) throws IOException {
			if (flag.contains(Flags.ALTERNATE)) {
				failMismatch(Flags.ALTERNATE, 's');
			}
			if (arg == null) {
				print(target, "null");
			} else {
				print(target, arg.toString());
			}
		}

		// Requires positive x
		private int stringSize(long x) {
			long p = 10;
			for (int i = 1; i < 19; i++) { // CS-IGNORE
				if (x < p) {
					return i;
				}
				p = 10 * p;
			}
			return 19; // CS-IGNORE
		}

		// -- Methods to support throwing exceptions --

		public String toString() {
			StringBuilder sb = new StringBuilder("%");
			// Flags.UPPERCASE is set internally for legal conversions.
			Flags dupFlag = flag.dup().remove(Flags.UPPERCASE);
			sb.append(dupFlag.toString());
			if (index > 0) {
				sb.append(index).append('$');
			}
			if (width != -1) {
				sb.append(width);
			}
			if (precision != -1) {
				sb.append('.').append(precision);
			}
			if (isDateConversion) {
				sb.append(flag.contains(Flags.UPPERCASE) ? 'T' : 't');
			}
			sb.append(flag.contains(Flags.UPPERCASE)
					? Character.toUpperCase(conversionChar) : conversionChar);
			return sb.toString();
		}

		// negative := val < 0
		private StringBuilder trailingSign(StringBuilder sb, boolean negative) {
			if (negative && flag.contains(Flags.PARENTHESES)) {
				sb.append(')');
			}
			return sb;
		}

		private int width(String s) {
			width = -1;
			if (s != null) {
				try {
					width = Integer.parseInt(s);
					if (width < 0) {
						throw new IllegalFormatWidthException(width);
					}
				} catch (NumberFormatException x) {
					// regex or overflow error?
					throw new RuntimeException("Illegal width", x);
				}
			}
			return width;
		}
	}

	// %[argument_index$][flags][width][.precision][t]conversion
	private static final String formatSpecifier
			= "%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(?:\\.(\\d+))?([tT])?([a-zA-Z%])";
	private static final int FIXED_STRING_INDEX = -2;
	private static final int PREVIOUS_INDEX = -1;
	private static Pattern formatPattern = Pattern.compile(formatSpecifier);

	private static void checkText(String s, int start, int end) {
		for (int i = start; i < end; i++) {
			// Any '%' found in the region starts an invalid format specifier.
			if (s.charAt(i) == '%') {
				char c = (i == end - 1) ? '%' : s.charAt(i + 1);
				throw new UnknownFormatConversionException(String.valueOf(c));
			}
		}
	}

	private final FormatFragment[] formatFragments;
	private final Locale locale;
	/*package*/ final char zero;
	/*package*/ final DecimalFormatSymbols decimalFormatSymbols;

	/**
	 * Constructs a new formatter.
	 *
	 * @param locale string locale
	 * @param format format specifier
	 */
	public MessageFormatter(Locale locale, String format) {
		if (locale == null || format == null) {
			throw new NullPointerException();
		}
		this.locale = locale;
		decimalFormatSymbols = DecimalFormatSymbols.getInstance(locale);
		this.zero = decimalFormatSymbols.getZeroDigit();
		this.formatFragments = parse(format);
	}

	/**
	 * Writes a formatted string to this object's destination using the
	 * specified format string and arguments.  The locale used is the one
	 * defined during the construction of this formatter.
	 *
	 * @param args Arguments referenced by the format specifiers in the format
	 *             string.  If there are more arguments than format specifiers, the
	 *             extra arguments are ignored.  The maximum number of arguments is
	 *             limited by the maximum dimension of a Java array as defined by
	 *             <cite>The Java&trade; Virtual Machine Specification</cite>.
	 * @return This formatter
	 * @throws java.util.IllegalFormatException If a format string contains an illegal syntax, a format
	 *                                          specifier that is incompatible with the given arguments,
	 *                                          insufficient arguments given the format string, or other
	 *                                          illegal conditions.  For specification of all possible
	 *                                          formatting errors, see the <a href="#detail">Details</a>
	 *                                          section of the formatter class specification.
	 */
	public String format(Object... args) {
		return format(new StringBuilder(), args).toString();
	}

	/**
	 * Writes a formatted string to this object's destination using the
	 * specified locale, format string, and arguments.
	 *
	 * @param args   Arguments referenced by the format specifiers in the format
	 *               string.  If there are more arguments than format specifiers, the
	 *               extra arguments are ignored.  The maximum number of arguments is
	 *               limited by the maximum dimension of a Java array as defined by
	 *               <cite>The Java&trade; Virtual Machine Specification</cite>.
	 * @param target {@link java.lang.StringBuilder} instance. This class appends formatted string into it.
	 * @return target
	 * @throws java.util.IllegalFormatException If a format string contains an illegal syntax, a format
	 *                                          specifier that is incompatible with the given arguments,
	 *                                          insufficient arguments given the format string, or other
	 *                                          illegal conditions.  For specification of all possible
	 *                                          formatting errors, see the <a href="#detail">Details</a>
	 *                                          section of the formatter class specification.
	 */
	public StringBuilder format(StringBuilder target, Object... args) {
		try {
			return (StringBuilder) format((Appendable) target, args);
		} catch (IOException e) {
			// never occurred
			throw new AssertionError(e);
		}
	}

	/**
	 * Writes a formatted string to this object's destination using the
	 * specified locale, format string, and arguments.
	 *
	 * @param args   Arguments referenced by the format specifiers in the format
	 *               string.  If there are more arguments than format specifiers, the
	 *               extra arguments are ignored.  The maximum number of arguments is
	 *               limited by the maximum dimension of a Java array as defined by
	 *               <cite>The Java&trade; Virtual Machine Specification</cite>.
	 * @param target {@link java.lang.Appendable} instance. This class appends formatted string into it.
	 * @param <T>    appendable instance (StringBuilder...)
	 * @return target
	 * @throws java.util.IllegalFormatException If a format string contains an illegal syntax, a format
	 *                                          specifier that is incompatible with the given arguments,
	 *                                          insufficient arguments given the format string, or other
	 *                                          illegal conditions.  For specification of all possible
	 *                                          formatting errors, see the <a href="#detail">Details</a>
	 *                                          section of the formatter class specification.
	 * @throws java.io.IOException              {@link Appendable#append(char)} failed.
	 */
	public <T extends Appendable> T format(T target, Object... args) throws IOException {
		// index of last argument referenced
		int lastIndex = -1;
		// last ordinary index
		int lastOrdinaryIndex = -1;

		FormatFragment[] fragments = formatFragments;
		for (FormatFragment fragment : fragments) {
			int index = fragment.index();
			switch (index) {
				case FIXED_STRING_INDEX:  // fixed string, "%n", or "%%"
					fragment.print(target, null);
					break;
				case PREVIOUS_INDEX:  // relative index
					if (lastIndex < 0 || (args != null && lastIndex > args.length - 1)) {
						throw new MissingFormatArgumentException(fragment.toString());
					}
					fragment.print(target, args == null ? null : args[lastIndex]);
					break;
				case 0:  // ordinary index
					lastOrdinaryIndex++;
					lastIndex = lastOrdinaryIndex;
					if (args != null && lastOrdinaryIndex > args.length - 1) {
						throw new MissingFormatArgumentException(fragment.toString());
					}
					fragment.print(target, args == null ? null : args[lastOrdinaryIndex]);
					break;
				default:  // explicit index
					lastIndex = index - 1;
					if (args != null && lastIndex > args.length - 1) {
						throw new MissingFormatArgumentException(fragment.toString());
					}
					fragment.print(target, args == null ? null : args[lastIndex]);
					break;
			}
		}
		return target;
	}

	/**
	 * Returns the locale set by the construction of this formatter.
	 *
	 * <p> The {@link #format(Object...) format} method
	 * for this object which has a locale argument does not change this value.
	 *
	 * @return {@code null} if no localization is applied, otherwise a
	 * locale
	 */
	public Locale locale() {
		return locale;
	}

	/**
	 * Finds format specifiers in the format string.
	 */
	private FormatFragment[] parse(String format) {
		ArrayList<FormatFragment> list = new ArrayList<>();
		Matcher m = formatPattern.matcher(format);
		for (int i = 0, len = format.length(); i < len; ) {
			if (m.find(i)) {
				// Anything between the start of the string and the beginning
				// of the format specifier is either fixed text or contains
				// an invalid format string.
				if (m.start() != i) {
					// Make sure we didn't miss any invalid format specifiers
					checkText(format, i, m.start());
					// Assume previous characters were fixed text
					list.add(new FixedString(format.substring(i, m.start())));
				}

				list.add(new FormatSpecifier(m, locale));
				i = m.end();
			} else {
				// No more valid format specifiers.  Check for possible invalid
				// format specifiers.
				checkText(format, i, len);
				// The rest of the string is fixed text
				list.add(new FixedString(format.substring(i)));
				break;
			}
		}
		return list.toArray(new FormatFragment[list.size()]);
	}
}
