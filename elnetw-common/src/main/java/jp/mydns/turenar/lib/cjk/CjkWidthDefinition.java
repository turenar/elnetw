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



package jp.mydns.turenar.lib.cjk;

import java.util.Arrays;
import java.util.Comparator;

public class CjkWidthDefinition {
	private static class CharacterRange {
		int first, last;
		EastAsianWidth width;

		public CharacterRange(int first, int last, EastAsianWidth width) {
			this.first = first;
			this.last = last;
			this.width = width;
		}

		public CharacterRange() {
		}
	}

	private static class CodepointBinarySearchComparator implements Comparator<CharacterRange> {
		public static final CodepointBinarySearchComparator INSTANCE = new CodepointBinarySearchComparator();

		@Override
		public int compare(CharacterRange left, CharacterRange right) {
			// Assumes right is [x, x+1)
			if (left.last <= right.first) {
				return -1;
			} else if (left.first >= right.last) {
				return 1;
			} else {
				return 0;
			}
		}

	}

	private static enum EastAsianWidth {
		WIDE_CHAR,
		FULL_WIDTH,
		AMBIGUOUS
	}

	private static final CharacterRange[] widthDefinition = {
			new CharacterRange(0x00A1, 0x00A2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00A4, 0x00A5, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00A7, 0x00A9, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00AA, 0x00AB, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00AD, 0x00AF, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00B0, 0x00B5, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00B6, 0x00BB, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00BC, 0x00C0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00C6, 0x00C7, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00D0, 0x00D1, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00D7, 0x00D9, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00DE, 0x00E2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00E6, 0x00E7, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00E8, 0x00EB, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00EC, 0x00EE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00F0, 0x00F1, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00F2, 0x00F4, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00F7, 0x00FB, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00FC, 0x00FD, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x00FE, 0x00FF, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0101, 0x0102, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0111, 0x0112, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0113, 0x0114, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x011B, 0x011C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0126, 0x0128, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x012B, 0x012C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0131, 0x0134, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0138, 0x0139, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x013F, 0x0143, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0144, 0x0145, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0148, 0x014C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x014D, 0x014E, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0152, 0x0154, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0166, 0x0168, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x016B, 0x016C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01CE, 0x01CF, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01D0, 0x01D1, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01D2, 0x01D3, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01D4, 0x01D5, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01D6, 0x01D7, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01D8, 0x01D9, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01DA, 0x01DB, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x01DC, 0x01DD, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0251, 0x0252, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0261, 0x0262, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02C4, 0x02C5, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02C7, 0x02C8, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02C9, 0x02CC, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02CD, 0x02CE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02D0, 0x02D1, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02D8, 0x02DC, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02DD, 0x02DE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x02DF, 0x02E0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0300, 0x0370, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0391, 0x03A2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x03A3, 0x03AA, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x03B1, 0x03C2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x03C3, 0x03CA, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0401, 0x0402, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0410, 0x0450, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x0451, 0x0452, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x1100, 0x1160, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x11A3, 0x11A8, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x11FA, 0x1200, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x2010, 0x2011, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2013, 0x2017, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2018, 0x201A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x201C, 0x201E, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2020, 0x2023, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2024, 0x2028, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2030, 0x2031, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2032, 0x2034, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2035, 0x2036, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x203B, 0x203C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x203E, 0x203F, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2074, 0x2075, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x207F, 0x2080, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2081, 0x2085, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x20AC, 0x20AD, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2103, 0x2104, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2105, 0x2106, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2109, 0x210A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2113, 0x2114, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2116, 0x2117, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2121, 0x2123, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2126, 0x2127, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x212B, 0x212C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2153, 0x2155, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x215B, 0x215F, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2160, 0x216C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2170, 0x217A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2189, 0x218A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2190, 0x219A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x21B8, 0x21BA, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x21D2, 0x21D3, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x21D4, 0x21D5, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x21E7, 0x21E8, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2200, 0x2201, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2202, 0x2204, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2207, 0x2209, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x220B, 0x220C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x220F, 0x2210, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2211, 0x2212, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2215, 0x2216, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x221A, 0x221B, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x221D, 0x2221, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2223, 0x2224, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2225, 0x2226, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2227, 0x222D, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x222E, 0x222F, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2234, 0x2238, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x223C, 0x223E, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2248, 0x2249, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x224C, 0x224D, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2252, 0x2253, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2260, 0x2262, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2264, 0x2268, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x226A, 0x226C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x226E, 0x2270, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2282, 0x2284, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2286, 0x2288, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2295, 0x2296, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2299, 0x229A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x22A5, 0x22A6, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x22BF, 0x22C0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2312, 0x2313, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2329, 0x232B, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x2460, 0x24EA, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x24EB, 0x254C, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2550, 0x2574, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2580, 0x2590, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2592, 0x2596, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25A0, 0x25A2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25A3, 0x25AA, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25B2, 0x25B4, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25B6, 0x25B8, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25BC, 0x25BE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25C0, 0x25C2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25C6, 0x25C9, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25CB, 0x25CC, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25CE, 0x25D2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25E2, 0x25E6, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x25EF, 0x25F0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2605, 0x2607, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2609, 0x260A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x260E, 0x2610, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2614, 0x2616, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x261C, 0x261D, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x261E, 0x261F, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2640, 0x2641, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2642, 0x2643, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2660, 0x2662, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2663, 0x2666, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2667, 0x266B, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x266C, 0x266E, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x266F, 0x2670, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x269E, 0x26A0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x26BE, 0x26C0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x26C4, 0x26CE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x26CF, 0x26E2, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x26E3, 0x26E4, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x26E8, 0x2700, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x273D, 0x273E, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2757, 0x2758, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2776, 0x2780, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2B55, 0x2B5A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x2E80, 0x2E9A, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x2E9B, 0x2EF4, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x2F00, 0x2FD6, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x2FF0, 0x2FFC, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3000, 0x3001, EastAsianWidth.FULL_WIDTH),
			new CharacterRange(0x3001, 0x303F, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3041, 0x3097, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3099, 0x3100, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3105, 0x312E, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3131, 0x318F, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3190, 0x31BB, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x31C0, 0x31E4, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x31F0, 0x321F, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3220, 0x3248, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3248, 0x3250, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x3250, 0x32FF, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x3300, 0x4DC0, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x4E00, 0xA48D, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xA490, 0xA4C7, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xA960, 0xA97D, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xAC00, 0xD7A4, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xD7B0, 0xD7C7, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xD7CB, 0xD7FC, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xE000, 0xF900, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0xF900, 0xFB00, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xFE00, 0xFE10, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0xFE10, 0xFE1A, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xFE30, 0xFE53, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xFE54, 0xFE67, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xFE68, 0xFE6C, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xFF01, 0xFF61, EastAsianWidth.FULL_WIDTH),
			new CharacterRange(0xFFE0, 0xFFE7, EastAsianWidth.FULL_WIDTH),
			new CharacterRange(0xFFFD, 0xFFFE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x1B000, 0x1B002, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x1F100, 0x1F10B, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x1F110, 0x1F12E, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x1F130, 0x1F16A, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x1F170, 0x1F19B, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x1F200, 0x1F203, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x1F210, 0x1F23B, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x1F240, 0x1F249, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x1F250, 0x1F252, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x20000, 0x2F740, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x2B740, 0x2FFFE, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0x30000, 0x3FFFE, EastAsianWidth.WIDE_CHAR),
			new CharacterRange(0xE0100, 0xE01F0, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0xF0000, 0xFFFFE, EastAsianWidth.AMBIGUOUS),
			new CharacterRange(0x100000, 0x10FFFE, EastAsianWidth.AMBIGUOUS)};

	/**
	 * calculate text width in terminal
	 *
	 * @param text               text
	 * @param ambiguousIsDoubled if assume Ambiguous Width is 2
	 * @return text width
	 */
	public static int width(String text, boolean ambiguousIsDoubled) {
		int len = text.length();
		int cnt = 0;
		for (int i = 0, cp; i < len; i += Character.charCount(cp)) {
			cp = text.codePointAt(i);
			cnt += width(cp, ambiguousIsDoubled);
		}
		return cnt;
	}

	/**
	 * calculate text width in terminal
	 *
	 * @param codepoint          unicode codepoint
	 * @param ambiguousIsDoubled if assume Ambiguous Width is 2
	 * @return text width
	 */
	public static int width(int codepoint, boolean ambiguousIsDoubled) {
		CharacterRange key = new CharacterRange();
		key.first = codepoint;
		key.last = codepoint + 1;
		int indexOf = Arrays.binarySearch(widthDefinition, key, CodepointBinarySearchComparator.INSTANCE);
		if (indexOf >= 0
				&& (widthDefinition[indexOf].width != EastAsianWidth.AMBIGUOUS || ambiguousIsDoubled)) {
			return 2;
		} else {
			return 1;
		}
	}

	private CjkWidthDefinition() {
	}
}
