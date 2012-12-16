package jp.syuriken.snsw.twclient.filter;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;

import jp.syuriken.snsw.twclient.filter.func.AndFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.InRetweetFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.NotFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.OneOfFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.OrFilterFunction;
import jp.syuriken.snsw.twclient.filter.prop.StandardBooleanProperties;
import jp.syuriken.snsw.twclient.filter.prop.StandardIntProperties;
import jp.syuriken.snsw.twclient.filter.prop.StandardStringProperties;
import jp.syuriken.snsw.twclient.filter.tokenizer.FilterParser;
import jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserVisitor;
import jp.syuriken.snsw.twclient.filter.tokenizer.ParseException;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenFunction;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenFunctionName;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenProperty;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyName;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyOperator;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyValue;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenQuery;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenStart;
import jp.syuriken.snsw.twclient.filter.tokenizer.SimpleNode;
import jp.syuriken.snsw.twclient.filter.tokenizer.TokenMgrError;

/**
 * フィルタをコンパイルするクラス。
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
public class FilterCompiler implements FilterParserVisitor {

	/**
	 * 関数のデータを格納するクラス
	 */
	protected static class FunctionData {

		/** ファクトリ */
		protected Constructor<? extends FilterFunction> factory;

		/** 名前 */
		protected String name;
	}

	/**
	 * プロパティのデータを格納するクラス
	 */
	protected static class PropertyData {

		/** ファクトリ */
		protected Constructor<? extends FilterProperty> factory;

		/** 名前 */
		protected String name;

		/** 演算子 */
		protected String operator;

		/** 値 */
		protected Object value;
	}

	/**
	 * 例外をラップする
	 */
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


	/** constructor ( FilterDispatcherBase ) */
	protected static HashMap<String, Constructor<? extends FilterFunction>> filterFunctionFactories;

	/** constructor ( String, String, String) */
	protected static HashMap<String, Constructor<? extends FilterProperty>> filterPropertyFactories;

	static {
		HashMap<String, Constructor<? extends FilterFunction>> ffMap =
				new HashMap<String, Constructor<? extends FilterFunction>>();
		FilterCompiler.filterFunctionFactories = ffMap;
		putFilterFunction("or", OrFilterFunction.getFactory());
		putFilterFunction("exactly_one_of", OneOfFilterFunction.getFactory());
		putFilterFunction("and", AndFilterFunction.getFactory());
		putFilterFunction("not", NotFilterFunction.getFactory());
		putFilterFunction("inrt", InRetweetFilterFunction.getFactory());

		HashMap<String, Constructor<? extends FilterProperty>> pfMap =
				new HashMap<String, Constructor<? extends FilterProperty>>();
		FilterCompiler.filterPropertyFactories = pfMap;
		Constructor<? extends FilterProperty> properties;
		properties = StandardIntProperties.getFactory();
		putFilterProperty("userid", properties);
		putFilterProperty("in_reply_to_userid", properties);
		putFilterProperty("rtcount", properties);
		putFilterProperty("timediff", properties);
		properties = StandardBooleanProperties.getFactory();
		putFilterProperty("retweeted", properties);
		putFilterProperty("mine", properties);
		putFilterProperty("protected", properties);
		putFilterProperty("verified", properties);
		putFilterProperty("status", properties);
		putFilterProperty("dm", properties);
		properties = StandardStringProperties.getFactory();
		putFilterProperty("user", properties);
		putFilterProperty("text", properties);
		putFilterProperty("client", properties);
	}


	/**
	 * コンパイルされたクエリオブジェクトを取得する。
	 * @param query クエリ
	 * @return コンパイル済みのオブジェクト。単にツリーを作って返すだけ
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public static FilterDispatcherBase getCompiledObject(String query) throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler();
		try {
			return (FilterDispatcherBase) tokenize(query).jjtAccept(filterCompiler, null);
		} catch (TokenMgrError e) {
			throw new IllegalSyntaxException(e.getLocalizedMessage(), e);
		} catch (WrappedException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IllegalSyntaxException) {
				throw (IllegalSyntaxException) cause;
			} else {
				throw e;
			}
		} catch (ParseException e) {
			throw new IllegalSyntaxException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * 指定した名前のフィルタ関数を取得する
	 *
	 * @param functionName 関数名
	 * @return {@link FilterFunction} の ({@link String}, {@link FilterDispatcherBase}) コンストラクタ
	 */
	public static Constructor<? extends FilterFunction> getFilterFunction(String functionName) {
		return filterFunctionFactories.get(functionName);
	}

	/**
	 * フィルタプロパティを追加する
	 *
	 * @param propertyName プロパティ名
	 * @return {@link FilterProperty} の ({@link String}, {@link String}, {@link String})コンストラクタ
	 */
	public static Constructor<? extends FilterProperty> getFilterProperty(String propertyName) {
		return filterPropertyFactories.get(propertyName);
	}

	/**
	 * テスト用インタラクティブコンソール
	 *
	 * @param args argv
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("> ");
		while (scanner.hasNextLine()) {
			String query = scanner.nextLine();
			try {
				FilterCompiler.tokenize(query).dump("");
				FilterCompiler.getCompiledObject(query);
			} catch (IllegalSyntaxException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.print("> ");
		}
	}

	/**
	 * フィルタ関数を追加する
	 *
	 * @param functionName 関数名
	 * @param constructor ({@link String}, {@link FilterDispatcherBase})コンストラクタ
	 * @return 前関数名に結び付けられていたコンストラクタ。結び付けられていない場合はnull
	 */
	public static Constructor<? extends FilterFunction> putFilterFunction(String functionName,
			Constructor<? extends FilterFunction> constructor) {
		return filterFunctionFactories.put(functionName, constructor);
	}

	/**
	 * フィルタプロパティを追加する
	 *
	 * @param propertyName プロパティ名
	 * @param constructor ({@link String}, {@link String}, {@link Object})コンストラクタ
	 * @return 前関数名に結び付けられていたコンストラクタ。結び付けられていない場合はnull
	 */
	public static Constructor<? extends FilterProperty> putFilterProperty(String propertyName,
			Constructor<? extends FilterProperty> constructor) {
		return filterPropertyFactories.put(propertyName, constructor);
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

	private FilterCompiler() {
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		int childrenCount = node.jjtGetNumChildren();
		FunctionData functionData = new FunctionData();
		node.jjtGetChild(0).jjtAccept(this, functionData);
		FilterDispatcherBase[] args = new FilterDispatcherBase[childrenCount - 1];
		for (int i = 1; i < childrenCount; i++) {
			args[i - 1] = (FilterDispatcherBase) node.jjtGetChild(i).jjtAccept(this, data);
		}

		String functionName = functionData.name;
		Constructor<? extends FilterFunction> factory = functionData.factory;
		try {
			return factory.newInstance(functionName, args);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			throw new WrappedException(cause);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">に関連付けられたConstructorは正しくありません", e);
		}

	}

	@Override
	public Object visit(QueryTokenFunctionName node, Object data) {
		String name = (String) node.jjtGetValue();
		Constructor<? extends FilterFunction> factory = filterFunctionFactories.get(name);
		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: function<" + name
					+ ">は見つかりません"));
		}
		FunctionData functionData = (FunctionData) data;
		functionData.name = name;
		functionData.factory = factory;
		return functionData;
	}

	@Override
	public Object visit(QueryTokenProperty node, Object data) {
		PropertyData propertyData = new PropertyData();
		node.childrenAccept(this, propertyData);

		String propertyName = propertyData.name;
		String propertyOperator = propertyData.operator;
		Object value = propertyData.value;
		Constructor<? extends FilterProperty> factory = propertyData.factory;
		try {
			return factory.newInstance(propertyName, propertyOperator, value);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			throw new WrappedException(cause);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName
					+ ">に関連付けられたConstructorは正しくありません", e);
		}
	}

	@Override
	public Object visit(QueryTokenPropertyName node, Object data) {
		String name = (String) node.jjtGetValue();
		Constructor<? extends FilterProperty> factory = getFilterProperty(name);
		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("プロパティ<" + name + ">は見つかりません。"));
		}

		PropertyData propertyData = (PropertyData) data;
		propertyData.name = name;
		propertyData.factory = factory;
		return factory;
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
		String value = (String) node.jjtGetValue();
		if (value.startsWith("\"")) {
			StringBuilder str = new StringBuilder(value.substring(1, value.length() - 1));
			int index = 0;
			while ((index = str.indexOf("\\", index)) != -1) {
				str.deleteCharAt(index);
				index += 2;
			}
			value = str.toString();
			propertyData.value = value;
			return value;
		} else { // Integer or Long
			Long longValue = Long.valueOf(value);
			propertyData.value = longValue;
			return longValue;
		}
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
