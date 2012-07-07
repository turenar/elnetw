package jp.syuriken.snsw.twclient.filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import jp.syuriken.snsw.twclient.filter.func.OneOfFilterFunction;
import jp.syuriken.snsw.twclient.filter.func.OrFilterFunction;
import jp.syuriken.snsw.twclient.filter.prop.StandardIntProperties;

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
		//filterOperatorFactories.put("&&", AndFilterOperator.getFactory());
		//filterOperatorFactories.put("!", NotFilterOperator.getFactory());
		
		filterPropetyFactories = new HashMap<String, Constructor<? extends FilterProperty>>();
		Constructor<? extends FilterProperty> properties = StandardIntProperties.getFactory();
		filterPropetyFactories.put("userid", properties);
	}
	
	
	/**
	 * コンパイルされたクエリオブジェクトを取得する。
	 * 
	 * @param query クエリ
	 * @return コンパイル済みのオブジェクト。単にツリーを作って返すだけ
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	public static FilterDispatcherBase getCompiledObject(String query) throws IllegalSyntaxException {
		FilterCompiler filterCompiler = new FilterCompiler(query);
		return filterCompiler.compile();
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
	
	
	private transient String query;
	
	private transient TokenType queryTokenType;
	
	private transient TokenType queryNextTokenType;
	
	private transient int compilingIndex;
	
	private String queryToken;
	
	private boolean hasToken;
	
	
	/**
	 * インスタンスを生成する。
	 * 
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
	 * <p>一度使用すると、 {@link #reset()} を呼び出すまでこのクラスの状態を変更するような
	 * 操作はできなくなります。再利用する際は {@link #reset()} を呼び出してください。</p>
	 * @return コンパイル済みのオブジェクト。中身は単なるツリー。
	 * @throws IllegalSyntaxException 正しくない文法のクエリ 
	 */
	public FilterDispatcherBase compile() throws IllegalSyntaxException {
		String token = getToken();
		switch (queryNextTokenType) {
			case FUNC_NAME:
				return compileFunction(token);
			case PROPERTY_NAME:
				return compileProperty(token);
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
		
		getToken();
		if (queryNextTokenType != TokenType.FUNC_END) {
			ungetToken();
			while (getToken() != null) {
				if (queryNextTokenType == TokenType.PROPERTY_NAME) {
					argsList.add(compileProperty(queryToken));
				} else if (queryNextTokenType == TokenType.FUNC_NAME) {
					argsList.add(compileFunction(queryToken));
				} else {
					throwUnexpectedToken();
				}
				
				getToken();
				if (queryNextTokenType == TokenType.FUNC_END) {
					break;
				} else if (queryNextTokenType == TokenType.FUNC_ARG_SEPARATOR) {
					continue;
				} else {
					throwUnexpectedToken();
				}
			}
		}
		FilterDispatcherBase[] args = argsList.toArray(new FilterDispatcherBase[argsList.size()]);
		try {
			Constructor<? extends FilterFunction> factory = filterFunctionFactories.get(functionName);
			if (factory == null) {
				throw new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName + ">は見つかりません");
			} else {
				return filterFunctionFactories.get(functionName).newInstance(functionName, args);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: function<" + functionName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IllegalSyntaxException) {
				throw (IllegalSyntaxException) cause;
			} else {
				throw new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: " + functionName + "をインスタンス化中にエラーが発生しました",
						cause);
			}
		}
	}
	
	/**
	 * プロパティをコンパイルする。
	 * 
	 * @param propertyName プロパティ名 
	 * @return コンパイル済みのプロパティオブジェクト
	 * @throws IllegalSyntaxException 正しくない文法のクエリ
	 */
	private FilterDispatcherBase compileProperty(String propertyName) throws IllegalSyntaxException {
		String propertyOperator = getToken();
		String compareValue = null;
		if (queryNextTokenType != TokenType.PROPERTY_OPERATOR) {
			ungetToken();
			propertyOperator = null;
		} else {
			compareValue = getToken();
			if (queryNextTokenType == TokenType.SCALAR_STRING_START) {
				compareValue = getToken();
				if (queryNextTokenType != TokenType.SCALAR_STRING) {
					throwUnexpectedToken();
				}
				
				StringBuilder stringBuilder = new StringBuilder(getToken());
				int index = 0;
				int indexOf;
				while ((indexOf = stringBuilder.indexOf("\\", index)) != -1) {
					stringBuilder.deleteCharAt(indexOf);
					index = indexOf + 1;
				}
				compareValue = stringBuilder.toString();
				// check quote ended
				getToken();
				if (queryNextTokenType != TokenType.SCALAR_STRING_END) {
					throwUnexpectedToken();
				}
			} else if (queryNextTokenType != TokenType.SCALAR_INT) {
				throwUnexpectedToken();
			}
		}
		
		try {
			Constructor<? extends FilterProperty> factory = filterPropetyFactories.get(propertyName);
			if (factory == null) {
				throw new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName + ">は見つかりません");
			} else {
				return factory.newInstance(propertyName, propertyOperator, compareValue);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("フィルタのコンパイル中にエラーが発生しました: property<" + propertyName
					+ ">に関連付けられたConstructorは正しくありません", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IllegalSyntaxException) {
				throw (IllegalSyntaxException) cause;
			} else {
				throw new IllegalSyntaxException("フィルタのコンパイル中にエラーが発生しました: " + propertyName + "をインスタンス化中にエラーが発生しました",
						cause);
			}
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
		
		int length = query.length();
		int i = compilingIndex;
		for (; i < length; i++) {
			char charAt = query.charAt(i);
			if (isSpace(charAt)) { // skip space
				continue;
			} else {
				break;
			}
		}
		if (i >= length) { // no valid token
			return null;
		}
		int tokenStart = i;
		LOOP: for (; i < length; i++) {
			char charAt = query.charAt(i);
			switch (queryTokenType) {
				case FUNC_START:
					if (charAt == ')') {
						queryNextTokenType = TokenType.FUNC_END;
						i++;
						break LOOP;
					}
					// fall-through
				case DEFAULT:
				case FUNC_ARG_SEPARATOR:
					while (isNotEod(charAt) && (isAlphabet(charAt) || isNumeric(charAt) || (charAt == '_'))) {
						i++;// valid token
						charAt = charAt(i);
					}
					if (i == tokenStart) {
						throw new IllegalSyntaxException("FUNC_NAME", charAt, queryTokenType.toString());
					}
					int j = i;
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
					i++; // this char is already parsed
					break LOOP;
				case FUNC_NAME:
					if (charAt != '(') {
						throw new IllegalSyntaxException('(', charAt, "FUNC_NAME");
					}
					queryNextTokenType = TokenType.FUNC_START;
					i++;
					break LOOP;
				case PROPERTY_NAME:
					if (charAt == ')') {
						if (tokenStart == i) {
							queryNextTokenType = TokenType.FUNC_END;
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
						}
						i++;
						break LOOP;
					} else if (charAt == ',') {
						if (tokenStart == i) {
							queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
						}
						break LOOP;
					} else if (isNumeric(charAt) || isQuote(charAt)) {
						if (tokenStart == i) {
							throw new IllegalSyntaxException("PROP_OP", charAt, "PROPERTY_NAME");
						} else {
							queryNextTokenType = TokenType.PROPERTY_OPERATOR;
							break LOOP;
						}
					} else if (isAlphabet(charAt)) {
						if (tokenStart == i) {
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
						while (isNotEod(charAt) && isNumeric(query.charAt(++i))) {
							// valid token
						}
						queryNextTokenType = TokenType.SCALAR_INT;
						break LOOP;
					} else {
						if (isQuote(charAt)) {
							queryNextTokenType = TokenType.SCALAR_STRING_START;
						} else {
							if (charAt == ',') {
								queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
							} else if (charAt == ')') {
								queryNextTokenType = TokenType.FUNC_END;
							} else {
								throw new IllegalSyntaxException("SCALAR or TERMINATOR", charAt, "PROPERTY_OPERATOR");
							}
						}
						i++; // this char is already parsed
						break LOOP;
					}
				case SCALAR_INT:
				case SCALAR_STRING_END:
					if (charAt == ',') {
						queryNextTokenType = TokenType.FUNC_ARG_SEPARATOR;
					} else if (charAt == ')') {
						queryNextTokenType = TokenType.FUNC_END;
					} else {
						throw new IllegalSyntaxException("TERMINATOR", charAt, "SCALAR_INT");
					}
					i++; // this char is already parsed
					break LOOP;
				case SCALAR_STRING_START:
					while (isNotEod(charAt) && isQuote(charAt) == false) {
						if (charAt == '\\') {
							++i;
						}
						charAt = charAt(++i);
					}
					queryNextTokenType = TokenType.SCALAR_STRING;
					break LOOP;
				case SCALAR_STRING:
					if (isQuote(charAt) == false) {
						throw new IllegalSyntaxException('"', charAt, "SCALAR_STRING");
					}
					queryNextTokenType = TokenType.SCALAR_STRING_END;
					i++; // this char is already parsed
					break LOOP;
				default:
					throw new AssertionError("not catched enum");
			}
		}
		compilingIndex = i;
		queryToken = query.substring(tokenStart, i).trim();
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
		throw new IllegalSyntaxException("Unexpected token: \"" + queryToken + "\" (index:"
				+ (compilingIndex - queryToken.length()) + ")");
	}
	
	private final void ungetToken() {
		hasToken = true;
	}
}
