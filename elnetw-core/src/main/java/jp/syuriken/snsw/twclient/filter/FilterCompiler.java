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

package jp.syuriken.snsw.twclient.filter;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.filter.query.FilterDispatcherBase;
import jp.syuriken.snsw.twclient.filter.query.QueryFunctionFactory;
import jp.syuriken.snsw.twclient.filter.query.QueryPropertyFactory;
import jp.syuriken.snsw.twclient.filter.tokenizer.FilterParser;
import jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserVisitor;
import jp.syuriken.snsw.twclient.filter.tokenizer.ParseException;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenFunction;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenProperty;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyOperator;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyValue;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenQuery;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenStart;
import jp.syuriken.snsw.twclient.filter.tokenizer.SimpleNode;
import jp.syuriken.snsw.twclient.filter.tokenizer.TokenMgrError;

/**
 * フィルタをコンパイルするクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FilterCompiler implements FilterParserVisitor {

	/** プロパティのデータを格納するクラス */
	protected static class PropertyData {

		// /** 名前 */
		// protected String name;

		/** 演算子 */
		protected String operator;
		/** 値 */
		protected Object value;
	}

	/** 例外をラップする */
	@SuppressWarnings("serial")
	public static class WrappedException extends RuntimeException {

		/**
		 * インスタンスを生成する。
		 *
		 * @param exception 例外
		 */
		public WrappedException(Throwable exception) {
			super(exception);
		}
	}

	/** constructor ( FilterDispatcherBase[] ) */
	protected static final HashMap<String, QueryFunctionFactory> filterFunctionFactories = new HashMap<>();
	/** constructor ( String, String, String) */
	protected static final HashMap<String, QueryPropertyFactory> filterPropertyFactories = new HashMap<>();

	/**
	 * コンパイルされたクエリオブジェクトを取得する。
	 *
	 * @param query クエリ
	 * @return コンパイル済みのオブジェクト。単にツリーを作って返すだけ
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public static FilterDispatcherBase getCompiledObject(String query)
			throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler();
		try {
			return (FilterDispatcherBase) tokenize(query).jjtAccept(filterCompiler, null);
		} catch (TokenMgrError | ParseException e) {
			throw new IllegalSyntaxException(e.getLocalizedMessage(), e);
		} catch (WrappedException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IllegalSyntaxException) {
				throw (IllegalSyntaxException) cause;
			} else {
				throw e;
			}
		}
	}

	/**
	 * 指定した名前のフィルタ関数を取得する
	 *
	 * @param functionName 関数名
	 * @return {@link jp.syuriken.snsw.twclient.filter.query.QueryFunctionFactory}
	 */
	public static QueryFunctionFactory getFilterFunction(String functionName) {
		return filterFunctionFactories.get(functionName);
	}

	/**
	 * フィルタプロパティを追加する
	 *
	 * @param propertyName プロパティ名
	 * @return {@link jp.syuriken.snsw.twclient.filter.query.QueryPropertyFactory}
	 */
	public static QueryPropertyFactory getFilterProperty(String propertyName) {
		return filterPropertyFactories.get(propertyName);
	}

	/**
	 * テスト用インタラクティブコンソール
	 *
	 * @param args argv
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name());
		System.out.print("> ");
		while (scanner.hasNextLine()) {
			String query = scanner.nextLine();
			try {
				FilterCompiler.tokenize(query).dump("");
				FilterCompiler.getCompiledObject(query);
			} catch (IllegalSyntaxException | ParseException e) {
				e.printStackTrace();
			}
			System.out.print("> ");
		}
	}

	/**
	 * フィルタ関数を追加する
	 *
	 * @param functionName 関数名
	 * @param factory      {@link jp.syuriken.snsw.twclient.filter.query.QueryFunctionFactory}
	 * @return 前関数名に結び付けられていたファクトリ。結び付けられていない場合はnull
	 */
	public static QueryFunctionFactory putFilterFunction(String functionName, QueryFunctionFactory factory) {
		return filterFunctionFactories.put(functionName, factory);
	}

	/**
	 * フィルタプロパティを追加する
	 *
	 * @param propertyName プロパティ名
	 * @param factory      {@link jp.syuriken.snsw.twclient.filter.query.QueryPropertyFactory}
	 * @return 前関数名に結び付けられていたファクトリ。結び付けられていない場合はnull
	 */
	public static QueryPropertyFactory putFilterProperty(String propertyName, QueryPropertyFactory factory) {
		return filterPropertyFactories.put(propertyName, factory);
	}

	/**
	 * トークン化する
	 *
	 * @param query クエリ文字列
	 * @return トークン
	 * @throws ParseException パース中にエラー
	 */
	public static QueryTokenStart tokenize(String query) throws ParseException {
		FilterParser filterParser = new FilterParser(new StringReader(query));
		return filterParser.Start();
	}

	private ClientConfiguration configuration;


	private FilterCompiler() {
		this.configuration = ClientConfiguration.getInstance();
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		int childrenCount = node.jjtGetNumChildren();

		String functionName = (String) node.jjtGetValue();
		QueryFunctionFactory factory = filterFunctionFactories.get(functionName);

		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("<" + functionName + ">は見つかりません。"));
		}
		FilterDispatcherBase[] args = new FilterDispatcherBase[childrenCount];
		for (int i = 0; i < childrenCount; i++) {
			args[i] = (FilterDispatcherBase) node.jjtGetChild(i).jjtAccept(this, data);
		}

		try {
			return factory.getInstance(functionName, args);
		} catch (IllegalSyntaxException e) {
			throw new WrappedException(e);
		}
	}

	@Override
	public Object visit(QueryTokenProperty node, Object data) {
		PropertyData propertyData = new PropertyData();

		String propertyName = (String) node.jjtGetValue();

		QueryPropertyFactory factory = getFilterProperty(propertyName);
		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("プロパティ<" + propertyName + ">は見つかりません。"));
		}

		//propertyData.name = propertyName;

		node.childrenAccept(this, propertyData);

		String propertyOperator = propertyData.operator;
		Object value = propertyData.value;
		try {
			return factory.getInstance(propertyName, propertyOperator, value);
		} catch (IllegalSyntaxException e) {
			throw new WrappedException(e);
		}
	}

	@Override
	public String visit(QueryTokenPropertyOperator node, Object data) {
		PropertyData propertyData = (PropertyData) data;
		String operator = (String) node.jjtGetValue();
		propertyData.operator = operator;
		return operator;
	}

	@Override
	public Object visit(QueryTokenPropertyValue node, Object data) {
		PropertyData propertyData = (PropertyData) data;
		String valueStr = (String) node.jjtGetValue();
		Object value;
		if (valueStr.startsWith("\"")) {
			StringBuilder str = new StringBuilder(valueStr.substring(1, valueStr.length() - 1));
			int index = 0;
			while ((index = str.indexOf("\\", index)) != -1) {
				str.deleteCharAt(index);
				index += 2;
			}
			value = str.toString();
		} else { // Boolean or Long
			switch (valueStr) {
				case "true":
					value = Boolean.TRUE;
					break;
				case "false":
					value = Boolean.FALSE;
					break;
				default:
					value = Long.valueOf(valueStr);
					break;
			}
		}
		propertyData.value = value;
		return value;
	}

	@Override
	public FilterDispatcherBase visit(QueryTokenQuery node, Object data) {
		return (FilterDispatcherBase) node.jjtGetChild(0).jjtAccept(this, data);
	}

	@Override
	public FilterDispatcherBase visit(QueryTokenStart node, Object data) {
		return (FilterDispatcherBase) node.jjtGetChild(0).jjtAccept(this, data);
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return null;
	}
}
