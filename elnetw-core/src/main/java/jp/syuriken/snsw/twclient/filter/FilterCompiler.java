package jp.syuriken.snsw.twclient.filter;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;

import jp.syuriken.snsw.twclient.ClientConfiguration;
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
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
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

	/** constructor ( FilterDispatcherBase ) */
	protected static final HashMap<String, Constructor<? extends FilterFunction>> filterFunctionFactories =
			new HashMap<String, Constructor<? extends FilterFunction>>();

	/** constructor ( String, String, String) */
	protected static final HashMap<String, Constructor<? extends FilterProperty>> filterPropertyFactories =
			new HashMap<String, Constructor<? extends FilterProperty>>();

	/**
	 * コンパイルされたクエリオブジェクトを取得する。
	 *
	 * @param configuration 設定
	 * @param query         クエリ
	 * @return コンパイル済みのオブジェクト。単にツリーを作って返すだけ
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public static FilterDispatcherBase getCompiledObject(ClientConfiguration configuration, String query)
			throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(configuration);
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
		Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name());
		System.out.print("> ");
		while (scanner.hasNextLine()) {
			String query = scanner.nextLine();
			try {
				FilterCompiler.tokenize(query).dump("");
				FilterCompiler.getCompiledObject(null, query);
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
	 * @param constructor  ({@link String}, {@link FilterDispatcherBase})コンストラクタ
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
	 * @param constructor  ({@link String}, {@link String}, {@link Object})コンストラクタ
	 * @return 前関数名に結び付けられていたコンストラクタ。結び付けられていない場合はnull
	 */
	public static Constructor<? extends FilterProperty> putFilterProperty(String propertyName,
			Constructor<? extends FilterProperty> constructor) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		if (parameterTypes[0] == ClientConfiguration.class && parameterTypes[1] == String.class
				&& parameterTypes[2] == String.class && parameterTypes[3] == Object.class) {
			return filterPropertyFactories.put(propertyName, constructor);
		} else {
			throw new IllegalArgumentException(
					"FilterProperty's constructor must be (ClientConfiguration, String, String, Object)");
		}
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


	private FilterCompiler(ClientConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Object visit(QueryTokenFunction node, Object data) {
		int childrenCount = node.jjtGetNumChildren();

		String functionName = (String) node.jjtGetValue();
		Constructor<? extends FilterFunction> factory = filterFunctionFactories.get(functionName);
		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">は見つかりません"));
		}

		FilterDispatcherBase[] args = new FilterDispatcherBase[childrenCount];
		for (int i = 0; i < childrenCount; i++) {
			args[i] = (FilterDispatcherBase) node.jjtGetChild(i).jjtAccept(this, data);
		}

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
	public Object visit(QueryTokenProperty node, Object data) {
		PropertyData propertyData = new PropertyData();

		String propertyName = (String) node.jjtGetValue();

		Constructor<? extends FilterProperty> factory = getFilterProperty(propertyName);
		if (factory == null) {
			throw new WrappedException(new IllegalSyntaxException("プロパティ<" + propertyName + ">は見つかりません。"));
		}

		//propertyData.name = propertyName;

		node.childrenAccept(this, propertyData);

		String propertyOperator = propertyData.operator;
		Object value = propertyData.value;
		try {
			return factory.newInstance(configuration, propertyName, propertyOperator, value);
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
			if (valueStr.equals("true")) {
				value = Boolean.TRUE;
			} else if (valueStr.equals("false")) {
				value = Boolean.FALSE;
			} else {
				value = Long.valueOf(valueStr);
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
