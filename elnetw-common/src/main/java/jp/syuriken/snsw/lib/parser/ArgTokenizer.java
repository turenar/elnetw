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

package jp.syuriken.snsw.lib.parser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Process Arguments Tokenizer
 *
 * <p>
 * このクラスが対応できるプロセス引数の形は以下のとおりです。(太字はオプション名)
 * </p>
 * <dl>
 * <dt>--<b>hoge</b>=fuga</dt><dd>=で結ばれた引数がある長いオプション (type a)</dd>
 * <dt>--<b>hoge</b>&nbsp;fuga</dt><dd>=で結ばれていないが引数がある長いオプション (type b)</dd>
 * <dt>--<b>hoge</b></dt><dd>引数のない長いオプション (type c)</dd>
 * <dt>-<b>abc</b></dt><dd><em>引数を必要としない</em>短いオプションが羅列されたオプション (type d-1)</dd>
 * <dt>-<b>o</b>ut</dt><dd><em>引数が指定できる</em>短いオプションに繋がって引数が指定されたオプション (type d-2)</dd>
 * <dt>-<b>o</b>&nbsp;arg</dt><dd>短いオプションと引数の区別がはっきりするオプション (type e)</dd>
 * <dt>-<b>o</b></dt><dd>単一の短いオプションが指定されたオプション (type f)</dd>
 * <dt>arg</dt><dd>オプションではないただの引数 (type g)</dd>
 * <dt>--</dt><dd>オプションの解析の終了</dd>
 * </dl>
 * <p>
 * <em>type a</em>から<em>type c</em>および<em>type e</em>から<em>type f</em>の引数の読み取りについては制限はありません。
 * {@link #next()}を呼び出し{@link #getOpt()}で読み取ります。
 * optが引数を必要とする場合は{@link #getArg()}を呼び出してください。
 * その引数が引数として正しいことを確認した時
 * <small>(オプションの引数は必須でなく、引数が明らかにオプションの形をしてい場合など)</small>
 * は、{@link #consumeArg()}を呼びださなくてはなりません。
 * </p>
 * <p>
 * <em>type d-1</em>は<em>type d-2</em>と同じようにパースされます。
 * これは{@linkplain ArgTokenizer}はどのオプション名が引数を必要としないのかを知らないためです。
 * {@link #consumeArg()}を呼び出さない限り、オプション引数
 * <small>(type d-2で言えば&quot;arg&quot;)</small>
 * は、続いてオプションとしてパースされます。
 * </p>
 * <p>
 * なお、ただの引数<em>(type g)</em>の場合、{@link #getOpt()}は{@code null}を返し{@link #getArg()}に引数が入ります。
 * {@link #consumeArg()}を呼び出す必要はありません。
 * </p>
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ArgTokenizer {
	private final String[] argv;
	private String opt;
	private String arg;
	private String savedArg;
	private boolean argParseFinished;
	private boolean shouldShiftArg;
	private boolean argConsumed;
	private boolean argParsed;
	private int argIndex = 0;

	@SuppressFBWarnings("EI_EXPOSE_REP2")
	public ArgTokenizer(String[] argv) {
		this.argv = argv;
	}

	/**
	 * 引数を使用したとマークする。
	 */
	public void consumeArg() {
		setArgConsumed(true);
	}

	/**
	 * 引数を取得する
	 *
	 * @return 引数
	 */
	public String getArg() {
		return arg;
	}

	/**
	 * オプション名を取得する
	 *
	 * @return オプション名
	 */
	public String getOpt() {
		return opt;
	}

	/**
	 * 次の引数を処理する
	 *
	 * @return 次の引数があるかどうか
	 */
	public boolean next() {
		if (argParsed) { // 最初のパースではない
			if (argConsumed) { // 引数が処理された
				savedArg = null; // 保存されたargは引数として処理された
				if (shouldShiftArg) { // オプションと引数は別々の要素として渡されているからここで引数分シフトする必要がある
					argIndex++;
				}
			}
			if (savedArg == null) {
				argIndex++; // 保存されたargがないとき/引数が処理されたときはオプションもシフトする
			}
		}
		String savedArg = this.savedArg;
		if (argIndex >= argv.length) { // すべてパースした
			opt = null;
			arg = null;
			return false;
		} else if (argParseFinished) { // "--"を処理した
			argParsed = false;
			opt = null;
			arg = argv[argIndex++];
			return true;
		}

		while (true) {
			String opt;
			String arg;
			String nextArg = argv[argIndex];
			boolean shouldShiftArg;
			if (savedArg != null) { // -abc or -a type (pass 2)
				opt = "-" + savedArg.substring(0, 1);
				savedArg = savedArg.substring(1);
				if (savedArg.isEmpty()) {
					arg = argIndex + 1 < argv.length ? argv[argIndex + 1] : null;
					savedArg = null;
					shouldShiftArg = true;
				} else {
					arg = savedArg;
					shouldShiftArg = false;
				}
			} else if (nextArg.equals("--")) {
				argParseFinished = true;
				opt = "--";
				arg = null;
				shouldShiftArg = false;
			} else if (nextArg.startsWith("--")) { // --hoge type
				int indexOf = nextArg.indexOf('=');
				if (indexOf >= 0) { // --hoge=fuga type
					opt = nextArg.substring(0, indexOf);
					arg = nextArg.substring(indexOf + 1);
					shouldShiftArg = false;
				} else { // --hoge type
					opt = nextArg;
					arg = argIndex + 1 < argv.length ? argv[argIndex + 1] : null;
					shouldShiftArg = true;
				}
			} else if (nextArg.startsWith("-")) { // -abc or -a type (pass 1)
				savedArg = nextArg.substring(1);
				continue;
			} else {
				opt = null;
				arg = nextArg;
				shouldShiftArg = false;
			}

			this.arg = arg;
			this.opt = opt;
			this.savedArg = savedArg;
			this.shouldShiftArg = shouldShiftArg;
			this.argConsumed = false;
			break;
		}
		argParsed = true;
		return true;
	}

	/**
	 * 引数が処理されたかどうかを指定する
	 *
	 * @param consumed 処理されたかどうか
	 */
	public void setArgConsumed(boolean consumed) {
		if (opt != null && arg != null) {
			argConsumed = consumed;
		}
	}
}
