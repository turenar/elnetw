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

package jp.mydns.turenar.twclient.filter.query;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;

import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.tokenizer.FilterParser;
import jp.mydns.turenar.twclient.filter.tokenizer.FilterParserVisitor;
import jp.mydns.turenar.twclient.filter.tokenizer.ParseException;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenEndOfData;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunction;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunctionArgSeparator;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunctionLeftParenthesis;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenFunctionRightParenthesis;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenProperty;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenPropertyOperator;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenPropertyValue;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenQuery;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenStart;
import jp.mydns.turenar.twclient.filter.tokenizer.SimpleNode;
import jp.mydns.turenar.twclient.filter.tokenizer.TokenMgrError;

/**
 * フィルタをコンパイルするクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class QueryCompiler implements FilterParserVisitor {

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

	/** constructor ( QueryDispatcherBase[] ) */
	protected static final HashMap<String, QueryFunctionFactory> filterFunctionFactories = new HashMap<>();
	/** constructor ( String, String, String) */
	protected static final HashMap<String, QueryPropertyFactory> filterPropertyFactories = new HashMap<>();

	/**
	 * コンパイルされたクエリオブジェクトを取得する。
	 *
	 * @param query クエリ
	 *              @param controller query controller instance. some query requires user information.
	 *                                some query requires networking. so controller should be passed
	 * @return コンパイル済みのオブジェクト。単にツリーを作って返すだけ
	 * @throws jp.mydns.turenar.twclient.filter.IllegalSyntaxException 正しくない文法のクエリ
	 */
	public static QueryDispatcherBase getCompiledObject(String query, QueryController controller)
			throws IllegalSyntaxException {
		QueryCompiler queryCompiler = new QueryCompiler(controller);
		try {
			return (QueryDispatcherBase) tokenize(query).jjtAccept(queryCompiler, null);
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
	 * @return {@link jp.mydns.turenar.twclient.filter.query.QueryFunctionFactory}
	 */
	public static QueryFunctionFactory getFilterFunction(String functionName) {
		return filterFunctionFactories.get(functionName);
	}

	/**
	 * フィルタプロパティを追加する
	 *
	 * @param propertyName プロパティ名
	 * @return {@link jp.mydns.turenar.twclient.filter.query.QueryPropertyFactory}
	 */
	public static QueryPropertyFactory getFilterProperty(String propertyName) {
		return filterPropertyFactories.get(propertyName);
	}

	/**
	 * フィルタ関数を追加する
	 *
	 * @param functionName 関数名
	 * @param factory      {@link jp.mydns.turenar.twclient.filter.query.QueryFunctionFactory}
	 * @return 前関数名に結び付けられていたファクトリ。結び付けられていない場合はnull
	 */
	public static QueryFunctionFactory putFilterFunction(String functionName, QueryFunctionFactory factory) {
		return filterFunctionFactories.put(functionName, factory);
	}

	/**
	 * フィルタプロパティを追加する
	 *
	 * @param propertyName プロパティ名
	 * @param factory      {@link jp.mydns.turenar.twclient.filter.query.QueryPropertyFactory}
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

	private final QueryController controller;

	private QueryCompiler(QueryController controller) {
		this.controller = controller;
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		int childrenCount = node.jjtGetNumChildren();

		String functionName = ((String) node.jjtGetValue()).toLowerCase(Locale.ENGLISH);
		QueryFunctionFactory factory = getFilterFunction(functionName);

		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("func<" + functionName + ">は見つかりません。"));
		}
		// skip LeftParen, ArgSeparator, RightParen
		QueryDispatcherBase[] args = new QueryDispatcherBase[childrenCount >> 1];
		for (int i = 1; i < childrenCount; i += 2) {
			args[i >> 1] = (QueryDispatcherBase) node.jjtGetChild(i).jjtAccept(this, data);
		}

		try {
			return factory.getInstance(functionName, args);
		} catch (IllegalSyntaxException e) {
			throw new WrappedException(e);
		}
	}

	@Override
	public Object visit(QueryTokenFunctionArgSeparator node, Object data) {
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionLeftParenthesis node, Object data) {
		return null;
	}

	@Override
	public Object visit(QueryTokenFunctionRightParenthesis node, Object data) {
		return null;
	}

	@Override
	public Object visit(QueryTokenProperty node, Object data) {
		PropertyData propertyData = new PropertyData();

		String propertyName = ((String) node.jjtGetValue()).toLowerCase(Locale.ENGLISH);

		QueryPropertyFactory factory = getFilterProperty(propertyName);
		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("プロパティ<" + propertyName + ">は見つかりません。"));
		}

		//propertyData.name = propertyName;

		node.childrenAccept(this, propertyData);

		String propertyOperator = propertyData.operator;
		Object value = propertyData.value;
		try {
			return factory.getInstance(controller, propertyName, propertyOperator, value);
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
	public QueryDispatcherBase visit(QueryTokenQuery node, Object data) {
		return (QueryDispatcherBase) node.jjtGetChild(0).jjtAccept(this, data);
	}

	@Override
	public QueryDispatcherBase visit(QueryTokenStart node, Object data) {
		return (QueryDispatcherBase) node.jjtGetChild(0).jjtAccept(this, data);
	}

	@Override
	public Object visit(QueryTokenEndOfData node, Object data) {
		return null;
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		return null;
	}
}
