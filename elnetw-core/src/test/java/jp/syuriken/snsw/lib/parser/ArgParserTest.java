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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.syuriken.snsw.lib.parser;

import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for ArgParser
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgParserTest {

	private static <T> void assertArrayEmpty(T[] actual) {
		assertArrayEquals(new Object[]{}, actual);
	}

	private static void assertNoError(ParsedArguments arguments) {
		assertFalse(arguments.hasError());
	}

	private String[] a(String s) {
		return s.split(" ");
	}

	@Test
	public void testAddLongOpt1NoArgument() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--hoge", OptionType.NO_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasOpt("--hoge"));
		assertNull(arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[]{"fuga"}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testAddLongOpt1OptionalArgumentA() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--hoge", OptionType.OPTIONAL_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasOpt("--hoge"));
		assertEquals("fuga", arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[]{}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testAddLongOpt1OptionalArgumentB() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--hoge", OptionType.OPTIONAL_ARGUMENT);
		parser.addLongOpt("--fuga", OptionType.NO_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge --fuga"));
		assertTrue(arguments.hasOpt("--hoge"));
		assertNull(arguments.getOptArg("--hoge"));
		assertTrue(arguments.hasOpt("--fuga"));
		assertArrayEquals(new String[]{}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testAddLongOpt1RequiredArgumentA() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--hoge", OptionType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasOpt("--hoge"));
		assertEquals("fuga", arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[]{}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testAddLongOpt1RequiredArgumentB() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--hoge", OptionType.REQUIRED_ARGUMENT);
		parser.addLongOpt("--fuga", OptionType.NO_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge --fuga"));
		assertTrue(arguments.hasOpt("--hoge"));
		assertFalse(arguments.hasOpt("--fuga"));
		assertEquals("--fuga", arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[]{}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testAddLongOpt2IllegalArgument() throws Exception {
		ArgParser parser = new ArgParser();

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasError());
		assertEquals(1, arguments.getErrorCount());
		assertFalse(arguments.hasOpt("--hoge"));
		assertArrayEquals(a("--hoge fuga"), arguments.getProcessArguments());
	}

	@Test
	public void testAddShortOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--help", OptionType.OPTIONAL_ARGUMENT);
		parser.addLongOpt("--add-all", OptionType.NO_ARGUMENT);
		parser.addLongOpt("--call-as", OptionType.REQUIRED_ARGUMENT);
		parser.addShortOpt('h', "--help");
		parser.addShortOpt("A", "--add-all");
		parser.addShortOpt("-c", "--call-as");

		ParsedArguments arguments = parser.parse(a("-h -A -c Hiei 比叡"));
		assertTrue(arguments.hasOpt("--help"));
		assertNull(arguments.getOptArg("--help"));
		assertTrue(arguments.hasOpt("--add-all"));
		assertNull(arguments.getOptArg("--add-all"));
		assertTrue(arguments.hasOpt("--call-as"));
		assertEquals("Hiei", arguments.getOptArg("--call-as"));
		assertArrayEquals(a("比叡"), arguments.getProcessArguments());
		assertNoError(arguments);

		arguments = parser.parse(a("比叡 -hfull -AcHiei"));
		assertTrue(arguments.hasOpt("--help"));
		assertEquals("full", arguments.getOptArg("--help"));
		assertTrue(arguments.hasOpt("--add-all"));
		assertNull(arguments.getOptArg("--add-all"));
		assertTrue(arguments.hasOpt("--call-as"));
		assertEquals("Hiei", arguments.getOptArg("--call-as"));
		assertArrayEquals(a("比叡"), arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testAddLongOpt3Argument() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--hoge", OptionType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge=fuga"));
		assertTrue(arguments.hasOpt("--hoge"));
		assertEquals("fuga", arguments.getOptArg("--hoge"));
		assertArrayEmpty(arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void testParseMultipleArgsWithSingleOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--gender", OptionType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--gender male --gender female"));
		assertTrue(arguments.hasOpt("--gender"));
		assertEquals("female", arguments.getOptArg("--gender"));
		Iterator<OptionInfo> iterator = arguments.getOptInfo("--gender").iterator();
		assertTrue(iterator.hasNext());
		assertEquals("female", iterator.next().getArg());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testParseMultipleArgsWithMultipleOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--gender", OptionType.REQUIRED_ARGUMENT, true);

		ParsedArguments arguments = parser.parse(a("--gender male --gender female extreme"));
		assertTrue(arguments.hasOpt("--gender"));
		assertEquals("male", arguments.getOptArg("--gender"));
		Iterator<OptionInfo> iterator = arguments.getOptInfo("--gender").iterator();
		assertTrue(iterator.hasNext());
		assertEquals("male", iterator.next().getArg());
		assertTrue(iterator.hasNext());
		assertEquals("female", iterator.next().getArg());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testParseMultipleArgsWithMixedOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addLongOpt("--gender", OptionType.REQUIRED_ARGUMENT, true);
		parser.addShortOpt("-g", "--gender");

		ParsedArguments arguments = parser.parse(a("-g male --gender female extra-ordinary"));
		assertTrue(arguments.hasOpt("--gender"));
		assertEquals("male", arguments.getOptArg("--gender"));
		Iterator<OptionInfo> iterator = arguments.getOptInfo("--gender").iterator();
		assertTrue(iterator.hasNext());
		OptionInfo next = iterator.next();
		assertEquals("-g", next.getShortOptName());
		assertEquals("--gender", next.getLongOptName());
		assertEquals("male", next.getArg());
		assertTrue(iterator.hasNext());

		next = iterator.next();
		assertNull(next.getShortOptName());
		assertEquals("--gender", next.getLongOptName());
		assertEquals("female", next.getArg());
		assertFalse(iterator.hasNext());


		iterator = arguments.getOptionListIterator();
		assertTrue(iterator.hasNext());
		next = iterator.next();
		assertEquals("-g", next.getShortOptName());
		assertEquals("--gender", next.getLongOptName());
		assertEquals("male", next.getArg());

		assertTrue(iterator.hasNext());
		next = iterator.next();
		assertNull(next.getShortOptName());
		assertEquals("--gender", next.getLongOptName());
		assertEquals("female", next.getArg());

		assertTrue(iterator.hasNext());
		next = iterator.next();
		assertNull(next.getShortOptName());
		assertNull(next.getLongOptName());
		assertEquals("extra-ordinary", next.getArg());

		assertFalse(iterator.hasNext());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalShortOptChar() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addShortOpt('d', "--hoge");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalShortOptStringA() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addShortOpt("-d", "--hoge");
	}
}
