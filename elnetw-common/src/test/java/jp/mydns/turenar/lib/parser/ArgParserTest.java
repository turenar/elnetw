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

package jp.mydns.turenar.lib.parser;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for ArgParser
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgParserTest {

	private static <T> void assertArrayEmpty(T[] actual) {
		assertArrayEquals(new Object[] {}, actual);
	}

	private static void assertNoError(ParsedArguments arguments) {
		assertFalse(arguments.hasError());
	}

	private String[] a(String s) {
		return s.split(" ");
	}

	@Test
	public void test1AddLongOpt1NoArgument() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--hoge").argType(ArgumentType.NO_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasOptGroup("--hoge"));
		assertNull(arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[] {"fuga"}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void test1AddLongOpt1OptionalArgumentA() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--hoge").argType(ArgumentType.OPTIONAL_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasOptGroup("--hoge"));
		assertEquals("fuga", arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[] {}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void test1AddLongOpt1OptionalArgumentB() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--hoge").argType(ArgumentType.OPTIONAL_ARGUMENT);
		parser.addOption("--fuga").argType(ArgumentType.OPTIONAL_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge --fuga"));
		assertTrue(arguments.hasOptGroup("--hoge"));
		assertNull(arguments.getOptArg("--hoge"));
		assertTrue(arguments.hasOptGroup("--fuga"));
		assertArrayEquals(new String[] {}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void test1AddLongOpt1RequiredArgumentA() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--hoge").argType(ArgumentType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasOptGroup("--hoge"));
		assertEquals("fuga", arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[] {}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void test1AddLongOpt1RequiredArgumentB() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--hoge").argType(ArgumentType.REQUIRED_ARGUMENT);
		parser.addOption("--fuga").argType(ArgumentType.NO_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge --fuga"));
		assertTrue(arguments.hasOptGroup("--hoge"));
		assertFalse(arguments.hasOptGroup("--fuga"));
		assertEquals("--fuga", arguments.getOptArg("--hoge"));
		assertArrayEquals(new String[] {}, arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void test1AddLongOpt1WithoutDoubleHyphen() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("gender").argType(ArgumentType.REQUIRED_ARGUMENT);
		ParsedArguments arguments = parser.parse(a("--gender male"));
		assertTrue(arguments.hasOptGroup("--gender"));
		assertEquals("male", arguments.getOptArg("--gender"));
	}

	@Test
	public void test1AddLongOpt2IllegalArgument() throws Exception {
		ArgParser parser = new ArgParser();

		ParsedArguments arguments = parser.parse(a("--hoge fuga"));
		assertTrue(arguments.hasError());
		assertEquals(1, arguments.getErrorCount());
		assertFalse(arguments.hasOptGroup("--hoge"));
		assertArrayEquals(a("--hoge fuga"), arguments.getProcessArguments());
	}

	@Test
	public void test1AddLongOpt3Argument() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--hoge").argType(ArgumentType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--hoge=fuga"));
		assertTrue(arguments.hasOptGroup("--hoge"));
		assertEquals("fuga", arguments.getOptArg("--hoge"));
		assertArrayEmpty(arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test2AddShortOptWithIllegalName1() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--gender", "--gender");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test2AddShortOptWithIllegalName2() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("", "--gender");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test2AddShortOptWithIllegalName3() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("ge", "--gender");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test2LongOptIsNullWithShortOptChar() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption('d', null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test2LongOptIsNullWithShortOptString() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("-d", null);
	}

	@Test
	public void test2ParseWithUnknownOpt1() throws Exception {
		ArgParser parser = new ArgParser();
		assertFalse(parser.isIgnoreUnknownOption());
		ParsedArguments arguments = parser.parse(a("--gender male --gender female"));
		assertFalse(arguments.hasOptGroup("--gender"));
		assertNull(arguments.getOptArg("--gender"));
		assertTrue(arguments.hasError());
		assertEquals(arguments.getErrorCount(), arguments.getErrorMessages().length);
		assertEquals(4, arguments.getProcessArguments().length);
	}

	@Test
	public void test2ParseWithUnknownOpt2() throws Exception {
		ArgParser parser = new ArgParser();
		parser.setIgnoreUnknownOption(true);
		ParsedArguments arguments = parser.parse(a("--gender male --gender female"));
		assertFalse(arguments.hasOptGroup("--gender"));
		assertNull(arguments.getOptArg("--gender"));
		assertFalse(arguments.hasError());
		assertEquals(4, arguments.getProcessArguments().length);
	}

	@Test
	public void test2ParseWithoutRequiredArgument() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--gender").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true);

		ParsedArguments arguments = parser.parse(a("--gender"));
		assertTrue(arguments.hasOptGroup("--gender"));
		assertNull(arguments.getOptArg("--gender"));
		assertTrue(arguments.hasError());
		assertEquals(1, arguments.getErrorCount());
	}

	@Test
	public void test3AddShortOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption('h', "--help").argType(ArgumentType.OPTIONAL_ARGUMENT);
		parser.addOption("A", "--add-all").argType(ArgumentType.NO_ARGUMENT);
		parser.addOption("-c", "--call-as").argType(ArgumentType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("-h -A -c Hiei 比叡"));
		assertTrue(arguments.hasOptGroup("--help"));
		assertNull(arguments.getOptArg("--help"));
		assertTrue(arguments.hasOptGroup("--add-all"));
		assertNull(arguments.getOptArg("--add-all"));
		assertTrue(arguments.hasOptGroup("--call-as"));
		assertEquals("Hiei", arguments.getOptArg("--call-as"));
		assertArrayEquals(a("比叡"), arguments.getProcessArguments());
		assertNoError(arguments);

		arguments = parser.parse(a("比叡 -hfull -AcHiei"));
		assertTrue(arguments.hasOptGroup("--help"));
		assertEquals("full", arguments.getOptArg("--help"));
		assertTrue(arguments.hasOptGroup("--add-all"));
		assertNull(arguments.getOptArg("--add-all"));
		assertTrue(arguments.hasOptGroup("--call-as"));
		assertEquals("Hiei", arguments.getOptArg("--call-as"));
		assertArrayEquals(a("比叡"), arguments.getProcessArguments());
		assertNoError(arguments);
	}

	@Test
	public void test3ParseDoubleHyphen() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("-g", "--gender").argType(ArgumentType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("-g male --gender female test -- --gender undefined arg"));
		assertTrue(arguments.hasOptGroup("--gender"));
		assertEquals("female", arguments.getOptArg("--gender"));
		assertEquals(4, arguments.getProcessArguments().length);
		assertEquals("test", arguments.getProcessArgument());
		assertEquals("--gender", arguments.getProcessArgument(1));
		assertEquals("undefined", arguments.getProcessArgument(2));
		assertEquals("arg", arguments.getProcessArgument(3));
	}

	@Test
	public void test4Grouping1() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--quiet").argType(ArgumentType.NO_ARGUMENT);
		parser.addOption("--verbose").argType(ArgumentType.NO_ARGUMENT).group("--quiet");

		ParsedArguments arguments = parser.parse(a("--quiet --verbose hoge"));
		assertTrue(arguments.hasOptGroup("--quiet"));
		assertFalse(arguments.hasOptGroup("--verbose"));
		OptionInfo quietInfo = arguments.getOptGroup("--quiet");
		assertEquals("--quiet", quietInfo.getGroup());
		assertEquals("--verbose", quietInfo.getOptName());
		assertNull(quietInfo.getArg());
	}

	@Test
	public void test4Grouping2MultipleOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--quiet").argType(ArgumentType.NO_ARGUMENT);
		parser.addOption("-v", "--verbose").argType(ArgumentType.NO_ARGUMENT).multiple(true).group("--quiet");

		ParsedArguments arguments = parser.parse(a("--quiet --verbose -v hoge"));
		assertTrue(arguments.hasOptGroup("--quiet"));
		assertFalse(arguments.hasOptGroup("--verbose"));

		OptionInfo quietInfo = arguments.getOptGroup("--quiet");
		assertEquals("--quiet", quietInfo.getGroup());
		assertEquals("--verbose", quietInfo.getOptName());
		assertNull(quietInfo.getArg());

		quietInfo = quietInfo.next();
		assertEquals("--quiet", quietInfo.getGroup());
		assertEquals("-v", quietInfo.getOptName());
		assertNull(quietInfo.getArg());

	}

	@Test
	public void test4ParseMultipleArgsWithMixedOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("-g", "--gender").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true);

		ParsedArguments arguments = parser.parse(a("-g male --gender female extra-ordinary"));
		assertTrue(arguments.hasOptGroup("--gender"));
		assertEquals("male", arguments.getOptArg("--gender"));
		Iterator<OptionInfo> iterator = arguments.getOptGroup("--gender").iterator();
		assertTrue(iterator.hasNext());
		OptionInfo next = iterator.next();
		assertEquals("--gender", next.getGroup());
		assertEquals("-g", next.getOptName());
		assertEquals("male", next.getArg());
		assertTrue(iterator.hasNext());

		next = iterator.next();
		assertEquals("--gender", next.getGroup());
		assertEquals("--gender", next.getOptName());
		assertEquals("female", next.getArg());
		assertFalse(iterator.hasNext());


		iterator = arguments.getOptionListIterator();
		assertTrue(iterator.hasNext());
		next = iterator.next();
		assertEquals("--gender", next.getGroup());
		assertEquals("-g", next.getOptName());
		assertEquals("male", next.getArg());

		assertTrue(iterator.hasNext());
		next = iterator.next();
		assertEquals("--gender", next.getGroup());
		assertEquals("--gender", next.getOptName());
		assertEquals("female", next.getArg());

		assertTrue(iterator.hasNext());
		next = iterator.next();
		assertNull(next.getGroup());
		assertNull(next.getOptName());
		assertEquals("extra-ordinary", next.getArg());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void test4ParseMultipleArgsWithMultipleOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--gender").argType(ArgumentType.REQUIRED_ARGUMENT).multiple(true);

		ParsedArguments arguments = parser.parse(a("--gender male --gender female extreme"));
		assertTrue(arguments.hasOptGroup("--gender"));
		assertEquals("male", arguments.getOptArg("--gender"));
		Iterator<OptionInfo> iterator = arguments.getOptGroup("--gender").iterator();
		assertTrue(iterator.hasNext());
		assertEquals("male", iterator.next().getArg());
		assertTrue(iterator.hasNext());
		assertEquals("female", iterator.next().getArg());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test4ParseMultipleArgsWithSingleOpt() throws Exception {
		ArgParser parser = new ArgParser();
		parser.addOption("--gender").argType(ArgumentType.REQUIRED_ARGUMENT);

		ParsedArguments arguments = parser.parse(a("--gender male --gender female"));
		assertTrue(arguments.hasOptGroup("--gender"));
		assertEquals("female", arguments.getOptArg("--gender"));
		Iterator<OptionInfo> iterator = arguments.getOptGroup("--gender").iterator();
		assertTrue(iterator.hasNext());
		assertEquals("female", iterator.next().getArg());
		assertFalse(iterator.hasNext());
		iterator = arguments.getOptionListIterator();
		assertTrue(iterator.hasNext());
		assertEquals("male", iterator.next().getArg());
		assertTrue(iterator.hasNext());
		assertEquals("female", iterator.next().getArg());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test5ParseSortCommandOption() throws Exception {
		ArgParser parser = new ArgParser();
		// GNU Coreutils sort
		OptionGroup orderGroup = parser.addGroup("order");
		orderGroup.addOption('g', "--general-numeric-sort");
		orderGroup.addOption("-M", "--month-sort");
		orderGroup.addOption("-h", "--human-numeric-sort");
		orderGroup.addOption("-n", "--numeric-sort");
		orderGroup.addOption("--sort").argType(ArgumentType.REQUIRED_ARGUMENT);
		orderGroup.addOption("-R", "--random-sort");
		orderGroup.addOption("-V", "--version-sort");

		parser.addOption("-m", "--merge");
		parser.addOption("-S", "--buffer-size").argType(ArgumentType.REQUIRED_ARGUMENT);
		parser.addOption("-t", "--field-separator").argType(ArgumentType.REQUIRED_ARGUMENT);
		parser.addOption("-T", "--temporary-directory").argType(ArgumentType.REQUIRED_ARGUMENT);
		parser.addOption("-u", "--unique");

		ParsedArguments args = parser.parse(a("-gMhnRm --sort=random a.txt --buffer-size= "
				+ "c.txt -VVV -u -T/tmp d.txt -t , b.txt"));
		assertFalse(args.hasError());

		assertFalse(args.hasOpt("-g"));
		assertFalse(args.hasOpt("-M"));
		assertFalse(args.hasOpt("-h"));
		assertFalse(args.hasOpt("-n"));
		assertFalse(args.hasOpt("--sort"));
		assertFalse(args.hasOpt("-R"));
		assertTrue(args.hasOpt("-V"));
		assertTrue(args.hasOptGroup("order"));
		assertTrue(args.hasOptGroup(orderGroup));

		assertTrue(args.hasOpt("-m"));
		assertTrue(args.hasOpt("-S"));
		assertEquals("", args.getOptArg("--buffer-size"));
		assertTrue(args.hasOpt("-t"));
		assertEquals(",", args.getOptArg("-t"));
		assertTrue(args.hasOpt("-T"));
		assertEquals("/tmp", args.getOptArg("-T"));
		assertTrue(args.hasOpt("-u"));

		assertEquals(4, args.getProcessArgumentCount());
		assertArrayEquals(a("a.txt c.txt d.txt b.txt"), args.getProcessArguments());
		assertArrayEquals(a("a.txt c.txt d.txt b.txt"),
				StreamSupport.stream(Spliterators.spliterator(args.getProcessArgumentIterator(),
						args.getProcessArgumentCount(), 0), false).toArray());
	}
}
