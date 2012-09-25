package jp.syuriken.snsw.twclient.filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

/**
 * フィルタをコンパイルするクラス。
 * 
 * @author $Author$
 */
public class FilterCompiler {
	
	/** データの末尾を示すchar */
	public static final char EOD_CHAR = '\uffff';
	
	/** constructor ( FilterDispatcherBase ) */
	protected static HashMap<String, Constructor<? extends FilterFunction>> filterFunctionFactories;
	
	/** constructor ( String, String, String) */
	protected static HashMap<String, Constructor<? extends FilterProperty>> filterPropetyFactories;
	
	static {
		filterFunctionFactories = new HashMap<String, Constructor<? extends FilterFunction>>();
		filterFunctionFactories.put("or", OrFilterFunction.getFactory());
		filterFunctionFactories.put("exactly_one_of", OneOfFilterFunction.getFactory());
		filterFunctionFactories.put("and", AndFilterFunction.getFactory());
		filterFunctionFactories.put("not", NotFilterFunction.getFactory());
		filterFunctionFactories.put("inrt", InRetweetFilterFunction.getFactory());
		
		filterPropetyFactories = new HashMap<String, Constructor<? extends FilterProperty>>();
		Constructor<? extends FilterProperty> properties;
		properties = StandardIntProperties.getFactory();
		filterPropetyFactories.put("userid", properties);
		filterPropetyFactories.put("in_reply_to_userid", properties);
		filterPropetyFactories.put("rtcount", properties);
		properties = StandardBooleanProperties.getFactory();
		filterPropetyFactories.put("retweeted", properties);
		filterPropetyFactories.put("mine", properties);
		properties = StandardStringProperties.getFactory();
		filterPropetyFactories.put("user", properties);
		filterPropetyFactories.put("text", properties);
	}
	
	
	/**
	 * コンパイルされたクエリオブジェクトを取得する。
	 * @param query クエリ
	 * @return コンパイル済みのオブジェクト。単にツリーを作って返すだけ
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public static FilterDispatcherBase getCompiledObject(String query) throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(query);
		return filterCompiler.compile();
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
		return filterPropetyFactories.get(propertyName);
	}
	
	private static final boolean isAlphabet(char charAt) {
		return (charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z');
	}
	
	/**
	 * データの最後ではないことを調べる。
	 * 
	 * @param charAt 文字
	 * @return データが最後の文字であるかどうか。
	 */
	public static final boolean isNotEod(char charAt) {
		return charAt != EOD_CHAR;
	}
	
	private static final boolean isNumeric(char charAt) {
		return charAt >= '0' && charAt <= '9';
	}
	
	private static final boolean isQuote(char charAt) {
		return charAt == '"';
	}
	
	private static final boolean isSpace(char charAt) {
		return charAt == ' ' || charAt == '\t' || charAt == '\n';
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
				FilterCompiler filterCompiler = new FilterCompiler(query);
				while (filterCompiler.nextToken() != null) {
					String queryToken = filterCompiler.getQueryToken();
					TokenType tokenType = filterCompiler.getNextTokenType();
					System.out.printf("'%s': %s%n", queryToken, tokenType.toString());
				}
				
				filterCompiler.reset(query);
				filterCompiler.compile();
			} catch (IllegalSyntaxException e) {
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
	 * @param constructor ({@link String}, {@link String}, {@link String})コンストラクタ
	 * @return 前関数名に結び付けられていたコンストラクタ。結び付けられていない場合はnull
	 */
	public static Constructor<? extends FilterProperty> putFilterProperty(String propertyName,
			Constructor<? extends FilterProperty> constructor) {
		return filterPropetyFactories.put(propertyName, constructor);
	}
	
	
	private transient String query;
	
	private transient TokenType queryTokenType;
	
	private transient TokenType queryNextTokenType;
	
	private transient int compilingIndex;
	
	private String queryToken;
	
	private boolean hasToken;
	
	
	/**
	 * インスタンスを生成する。
	 * @param query クエリ
	 */
	public FilterCompiler(String query) {
		reset(query);
	}
	
	private final char charAt(int i) {
		return i < query.length() ? query.charAt(i) : EOD_CHAR;
	}
	
	/**
	 * コンパイルする。
	 * 
	 * <p>この関数では処理前に {@link #reset()} を呼び出します。</p>
	 * @return コンパイル済みのオブジェクト。中身は単なるツリー。
	 * @throws IllegalSyntaxException 正しくない文法のクエリ 
	 */
	public FilterDispatcherBase compile() throws IllegalSyntaxException {
		reset();
		
		String token = getToken();
		switch (queryNextTokenType) {
			case FUNC_NAME:
				try {
					return compileFunction(token);
				} catch (IllegalSyntaxException e) {
					e.setQuery(query);
					if (e.getTokenType() == IllegalSyntaxException.ID_FUNC_NAME && e.getTokenPosition() < 0) {
						e.setTokenPostion(0);
					}
					throw e;
				}
			case PROPERTY_NAME:
				try {
					return compileProperty(token);
				} catch (IllegalSyntaxException e) {
					e.setQuery(query);
					if (e.getTokenType() == IllegalSyntaxException.ID_PROPERTY_NAME) {
						e.setTokenPostion(0);
					}
					throw e;
				}
			default:
				throwUnexpectedToken();
				return null; // orphaned
		}
	}
	
	/**
	 * 関数をコンパイルする。
	 * 
	 * @param functionName 関数名
	 * @return 関数オブジェクト
	 * @throws IllegalSyntaxException 正しくない文法のクエリ 
	 */
	private FilterDispatcherBase compileFunction(String functionName) throws IllegalSyntaxException {
		getToken();
		if (queryNextTokenType != TokenType.FUNC_START) {
			throwUnexpectedToken();
		}
		
		ArrayList<FilterDispatcherBase> argsList = new ArrayList<FilterDispatcherBase>();
		ArrayList<Integer> argsTokenIndexList = new ArrayList<Integer>();
		
		argsTokenIndexList.add(compilingIndex);
		getToken();
		if (queryNextTokenType != TokenType.FUNC_END) {
			do {
				try {
					if (queryNextTokenType == TokenType.PROPERTY_NAME) {
						argsList.add(compileProperty(queryToken));
					} else if (queryNextTokenType == TokenType.FUNC_NAME) {
						argsList.add(compileFunction(queryToken));
					} else {
						throwUnexpectedToken();
					}
				} catch (IllegalSyntaxException e) {
					int tokenType = e.getTokenType();
					if (e.getTokenPosition() < 0
							&& (tokenType == IllegalSyntaxException.ID_PROPERTY_NAME || tokenType == IllegalSyntaxException.ID_FUNC_NAME)) {
						e.setTokenPostion(argsTokenIndexList.get(argsTokenIndexList.size() - 1));
					}
					throw e;
				}
				argsTokenIndexList.add(compilingIndex);
				
				getToken();
				if (queryNextTokenType == TokenType.FUNC_END) {
					break;
				} else if (queryNextTokenType == TokenType.FUNC_ARG_SEPARATOR) {
					continue;
				} else {
					throwUnexpectedToken();
				}
				argsTokenIndexList.add(compilingIndex);
			} while (getToken() != null);
		}
		FilterDispatcherBase[] args = argsList.toArray(new FilterDispatcherBase[argsList.size()]);
		try {
			Constructor<? extends FilterFunction> factory = filterFunctionFactories.get(functionName);
			if (factory == null) {
				throw new IllegalSyntaxException(IllegalSyntaxException.ID_FUNC_NAME,
						"フィルタのコンパイル中にエラーが発生しました: function<" + functionName + ">は見つかりません");
			} else {
				try {
					return factory.newInstance(functionName, args);
				} catch (InvocationTargetException e) {
					Throwable cause = e.getCause();
					if (cause instanceof IllegalSyntaxException) {
						throw (IllegalSyntaxException) cause;
					} else {
						throw new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: " + functionName
								+ "をインスタンス化中にエラーが発生しました", cause);
					}
				}
			}
		} catch (IllegalSyntaxException e) {
			int tokenType = e.getTokenType();
			if (tokenType > 0) {
				e.setTokenPostion(argsTokenIndexList.get(tokenType - 1));
			} else if (tokenType == IllegalSyntaxException.ID_FUNC_ARGS) {
				e.setTokenPostion(argsTokenIndexList.get(0));
			}
			throw e;
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
	
	/**
	 * プロパティをコンパイルする。
	 * 
	 * @param propertyName プロパティ名 
	 * @return コンパイル済みのプロパティオブジェクト
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	private FilterDispatcherBase compileProperty(String propertyName) throws IllegalSyntaxException
	
	{
		String propertyOperator;
		int propertyOperatorPostion = compilingIndex;
		String compareValue = null;
		int compareValuePostion;
		getToken();
		if (queryNextTokenType != TokenType.PROPERTY_OPERATOR) {
			ungetToken();
			propertyOperator = null;
			compareValuePostion = propertyOperatorPostion;
		} else {
			propertyOperator = queryToken;
			compareValuePostion = compilingIndex;
			
			getToken();
			if (queryNextTokenType == TokenType.SCALAR_STRING) {
				StringBuilder stringBuilder = new StringBuilder(queryToken.substring(1, queryToken.length() - 1));
				int index = 0;
				int indexOf;
				while ((indexOf = stringBuilder.indexOf("\\", index)) != -1) {
					stringBuilder.deleteCharAt(indexOf);
					index = indexOf + 1;
				}
				compareValue = stringBuilder.toString();
			} else if (queryNextTokenType != TokenType.SCALAR_INT) {
				if (queryNextTokenType == TokenType.FUNC_ARG_SEPARATOR || queryNextTokenType == TokenType.FUNC_END
						|| queryNextTokenType == TokenType.EOD) {
					ungetToken();
				} else {
					throwUnexpectedToken();
				}
			}
		}
		
		try {
			Constructor<? extends FilterProperty> factory = filterPropetyFactories.get(propertyName);
			if (factory == null) {
				throw new IllegalSyntaxException(IllegalSyntaxException.ID_PROPERTY_NAME,
						"フィルタのコンパイル中にエラーが発生しました: property<" + propertyName + ">は見つかりません");
			} else {
				try {
					return factory.newInstance(propertyName, propertyOperator, compareValue);
				} catch (InvocationTargetException e) {
					Throwable cause = e.getCause();
					if (cause instanceof IllegalSyntaxException) {
						throw (IllegalSyntaxException) cause;
					} else {
						throw new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: " + propertyName
								+ "をインスタンス化中にエラーが発生しました", cause);
					}
				}
			}
		} catch (IllegalSyntaxException e) {
			int tokenType = e.getTokenType();
			if (tokenType == IllegalSyntaxException.ID_PROPERTY_OPERATOR) {
				e.setTokenPostion(propertyOperatorPostion);
			} else if (tokenType == IllegalSyntaxException.ID_PROPERTY_VALUE) {
				e.setTokenPostion(compareValuePostion);
			}
			throw e;
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
	
	/**
	 * {@link #nextToken()} により取得したトークンの次の文字のインデックス。
	 * 
	 * @return 次の文字のインデックス
	 */
	public int getCompilingIndex() {
		return compilingIndex;
	}
	
	/**
	 * {@link #nextToken()} により取得したトークンのタイプを取得する。
	 * 
	 * @return トークンタイプ
	 */
	public TokenType getNextTokenType() {
		return queryNextTokenType;
	}
	
	/**
	 * 現在コンパイル中のクエリを取得する。
	 * 
	 * @return クエリ
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * {@link #nextToken()} により取得したトークンを再取得する。
	 * 
	 * @return トークン
	 */
	public String getQueryToken() {
		return queryToken;
	}
	
	private final String getToken() throws IllegalSyntaxException {
		if (hasToken) {
			hasToken = false;
			return queryToken;
		} else {
			return nextToken();
		}
	}
	
	/**
	 * 次のトークンを取得する。
	 * 
	 * @return トークン
	 * @throws IllegalSyntaxException トークン処理中にエラー
	 */
	public String nextToken() throws IllegalSyntaxException {
		queryTokenType = queryNextTokenType;
		queryNextTokenType = TokenType.UNEXPECTED;
		char charAt = EOD_CHAR;
		int length = query.length();
		int index = compilingIndex;
		for (; index < length; index++) {
			charAt = query.charAt(index);
			if (isSpace(charAt)) { // skip space
				continue;
			} else {
				break;
			}
		}
		
		if (index >= length) { // no valid token
			queryNextTokenType = TokenType.EOD;
			queryToken = null;
			return null;
		}
		int tokenStart = index;
		LOOP: for (; index < length; index++) {
			charAt = query.charAt(index);
			switch (queryTokenType) {
				case FUNC_START:
					if (charAt == ')') {
						queryNextTokenType = TokenType.FUNC_END;
						index++;
						break LOOP;
					}
					// fall-through
				case DEFAULT:
				case FUNC_ARG_SEPARATOR:
					while (isNotEod(charAt) && (isAlphabet(charAt) || isNumeric(charAt) || (charAt == '_'))) {
						index++;// valid token
						charAt = charAt(index);
					}
					if (index == tokenStart) {
						throw new IllegalSyntaxException("FUNC_NAME", charAt, queryTokenType.toString());
					}
					int j = index;
					while (isNotEod(charAt) && isSpace(charAt)) {
						j++;
						charAt = charAt(j);
					}
					if (charAt == '(') {
						queryNextTokenType = TokenType.FUNC_NAME;
					} else {
						queryNextTokenType = TokenType.PROPERTY_NAME;
					}
					break LOOP;
				case FUNC_END:
					if (charAt == ',') {
						queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
					} else if (charAt == ')') {
						queryNextTokenType = TokenType.FUNC_END;
					} else {
						throw new IllegalSyntaxException("TERMINATOR", charAt, "FUNC_END");
					}
					index++; // this char is already parsed
					break LOOP;
				case FUNC_NAME:
					if (charAt != '(') {
						throw new IllegalSyntaxException('(', charAt, "FUNC_NAME");
					}
					queryNextTokenType = TokenType.FUNC_START;
					index++;
					break LOOP;
				case PROPERTY_NAME:
					if (charAt == ')') {
						if (tokenStart == index) {
							queryNextTokenType = TokenType.FUNC_END;
							index++;
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
						}
						break LOOP;
					} else if (charAt == ',') {
						if (tokenStart == index) {
							queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
							index++;
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
						}
						break LOOP;
					} else if (isNumeric(charAt) || isQuote(charAt)) {
						if (tokenStart == index) {
							throw new IllegalSyntaxException("PROP_OP", charAt, "PROPERTY_NAME");
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
							break LOOP;
						}
					} else if (isAlphabet(charAt)) {
						if (tokenStart == index) {
							throw new IllegalSyntaxException("PROP_OP or TERMINATOR", charAt, "PROPERTY_NAME");
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
							break LOOP;
						}
					} else {
						queryNextTokenType = TokenType.PROPERTY_OPERATOR; // valid token
					}
					break;
				case PROPERTY_OPERATOR:
					if (isNumeric(charAt)) {
						while (isNotEod(charAt) && isNumeric(charAt(++index))) {
							// valid token
						}
						queryNextTokenType = TokenType.SCALAR_INT;
						break LOOP;
					} else {
						if (isQuote(charAt)) {
							charAt = charAt(++index);
							while (isNotEod(charAt) && isQuote(charAt) == false) {
								if (charAt == '\\') {
									++index;
								}
								charAt = charAt(++index);
							}
							queryNextTokenType = TokenType.SCALAR_STRING;
							index++; // '"' is already parsed
							break LOOP;
						} else {
							if (charAt == ',') {
								queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
							} else if (charAt == ')') {
								queryNextTokenType = TokenType.FUNC_END;
							} else {
								throw new IllegalSyntaxException("SCALAR or TERMINATOR", charAt, "PROPERTY_OPERATOR");
							}
						}
						index++; // this char is already parsed
						break LOOP;
					}
				case SCALAR_INT:
				case SCALAR_STRING:
					if (charAt == ',') {
						queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
					} else if (charAt == ')') {
						queryNextTokenType = TokenType.FUNC_END;
					} else {
						throw new IllegalSyntaxException("TERMINATOR", charAt, "SCALAR_INT");
					}
					index++; // this char is already parsed
					break LOOP;
				default:
					throw new AssertionError("not catched enum");
			}
		}
		compilingIndex = index;
		queryToken = query.substring(tokenStart, index).trim();
		return queryToken;
	}
	
	/**
	 * リセットする。
	 * 
	 * @see #reset(String)
	 */
	public void reset() {
		reset(query);
	}
	
	/**
	 * 指定されたクエリでリセットする。
	 * 
	 * @param query クエリ
	 * @see #reset()
	 */
	public void reset(String query) {
		this.query = query;
		queryTokenType = TokenType.DEFAULT;
		queryNextTokenType = TokenType.DEFAULT;
		compilingIndex = 0;
		queryToken = null;
		hasToken = false;
	}
	
	private void throwUnexpectedToken() throws IllegalSyntaxException {
		StringBuilder stringBuilder = new StringBuilder("Unexpected token: ");
		if (queryToken == null) {
			stringBuilder.append("null");
		} else {
			stringBuilder.append('"').append(queryToken).append('"');
		}
		stringBuilder.append(" (").append(queryNextTokenType).append(')');
		
		IllegalSyntaxException exception = new IllegalSyntaxException(stringBuilder.toString());
		exception.setQuery(query);
		exception.setTokenPostion(queryToken == null ? compilingIndex : (compilingIndex - queryToken.length()));
		throw exception;
	}
	
	private final void ungetToken() {
		hasToken = true;
	}
}
