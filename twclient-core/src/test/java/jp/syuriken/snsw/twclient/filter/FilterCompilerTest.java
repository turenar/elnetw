package jp.syuriken.snsw.twclient.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * {@link FilterCompiler} のためのテスト・クラス
 * 
 * @author $Author$
 */
public class FilterCompilerTest {
	
	private void assertNoValidToken(FilterCompiler compiler) throws IllegalSyntaxException {
		assertNull(compiler.nextToken());
	}
	
	private void assertToken(FilterCompiler compiler, String token, TokenType tokenType) throws IllegalSyntaxException {
		compiler.nextToken();
		assertEquals(compiler.getNextTokenType(), tokenType);
		assertEquals(token, compiler.getQueryToken());
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken1WithPropertyNameOnly() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(" hoge ");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken2WithPropertyOperator() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler("hoge:");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ":", TokenType.PROPERTY_OPERATOR);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler(" \t    \nhoge \n\t: ");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ":", TokenType.PROPERTY_OPERATOR);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken3WithPropertyComparedWithString() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler("hoge:\"aaaa\"");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ":", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "\"", TokenType.SCALAR_STRING_START);
		assertToken(filterCompiler, "aaaa", TokenType.SCALAR_STRING);
		assertToken(filterCompiler, "\"", TokenType.SCALAR_STRING_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler("hoge:\"\"");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ":", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "\"", TokenType.SCALAR_STRING_START);
		assertToken(filterCompiler, "", TokenType.SCALAR_STRING);
		assertToken(filterCompiler, "\"", TokenType.SCALAR_STRING_END);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken4WithPropertyComparedWithInt() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler("hoge:1234");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ":", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "1234", TokenType.SCALAR_INT);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler("hoge== 1234");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "==", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "1234", TokenType.SCALAR_INT);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler("hoge != 1234");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "!=", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "1234", TokenType.SCALAR_INT);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler(" hoge <= 1234 ");
		assertToken(filterCompiler, "hoge", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "<=", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "1234", TokenType.SCALAR_INT);
		assertNoValidToken(filterCompiler);
		
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken5WithFunction() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(" hoge (  ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken6WithDeepFunction() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(" hoge ( fuga ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler(" hoge ( fuga ?) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler(" hoge ( fuga == 1234) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "==", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "1234", TokenType.SCALAR_INT);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler(" hoge ( hoge ( ) ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#nextToken()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testNextToken7WithFunctionSeparator() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(" hoge ( fuga?, fuga ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler = new FilterCompiler(" hoge ( fuga?, hoge(fuga\n==9876, fuga\t??  ) ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "==", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "9876", TokenType.SCALAR_INT);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "??", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#reset()} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testReset() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(" hoge ( fuga?, fuga ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler.reset();
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
	}
	
	/**
	 * {@link jp.syuriken.snsw.twclient.filter.FilterCompiler#reset(java.lang.String)} のためのテスト・メソッド。
	 * @throws IllegalSyntaxException エラー
	 */
	@Test
	public void testResetString() throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(" hoge ( fuga?, fuga ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
		
		filterCompiler.reset(" hoge ( fuga?, hoge(fuga\n==9876, fuga\t??  ) ) ");
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "?", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "hoge", TokenType.FUNC_NAME);
		assertToken(filterCompiler, "(", TokenType.FUNC_START);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "==", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, "9876", TokenType.SCALAR_INT);
		assertToken(filterCompiler, ",", TokenType.FUNC_ARG_SEPARATOR);
		assertToken(filterCompiler, "fuga", TokenType.PROPERTY_NAME);
		assertToken(filterCompiler, "??", TokenType.PROPERTY_OPERATOR);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertToken(filterCompiler, ")", TokenType.FUNC_END);
		assertNoValidToken(filterCompiler);
	}
	
}
