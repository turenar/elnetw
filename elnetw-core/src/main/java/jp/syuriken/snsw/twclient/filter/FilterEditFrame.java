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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.filter.tokenizer.FilterParserVisitor;
import jp.syuriken.snsw.twclient.filter.tokenizer.Node;
import jp.syuriken.snsw.twclient.filter.tokenizer.ParseException;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenFunction;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenProperty;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyOperator;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenPropertyValue;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenQuery;
import jp.syuriken.snsw.twclient.filter.tokenizer.QueryTokenStart;
import jp.syuriken.snsw.twclient.filter.tokenizer.SimpleNode;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

/**
 * フィルタを編集するためのフレーム
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("serial")
public class FilterEditFrame extends JFrame implements WindowListener {

	/** クエリを見やすくするフォーマッタ */
	protected static class FilterQueryFormatter implements FilterParserVisitor {

		/** 関数の深さ */
		protected transient int queryDepth;
		/** 抽出設定かどうか */
		protected boolean isExtract;

		/** 抽出設定かどうかを取得する */
		public boolean isExtractFilter() {
			return isExtract;
		}

		@Override
		public Object visit(QueryTokenFunction node, Object data) {
			StringBuilder stringBuilder = (StringBuilder) data;

			queryDepth++;
			stringBuilder.append(node.jjtGetValue()).append("(\n");

			int childrenCount = node.jjtGetNumChildren();
			for (int i = 0; i < childrenCount; i++) {
				for (int d = 0; d < queryDepth; d++) {
					stringBuilder.append("  ");
				}
				node.jjtGetChild(i).jjtAccept(this, data);
				stringBuilder.append(",\n");
			}
			int len = stringBuilder.length();
			stringBuilder.deleteCharAt(len - 2); // remove "," before "\n"

			queryDepth--;
			for (int i = 0; i < queryDepth; i++) {
				stringBuilder.append("  ");
			}
			return stringBuilder.append(")");
		}

		@Override
		public Object visit(QueryTokenProperty node, Object data) {
			((StringBuilder) data).append(node.jjtGetValue());
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(QueryTokenPropertyOperator node, Object data) {
			return ((StringBuilder) data).append(' ').append(node.jjtGetValue());
		}

		@Override
		public Object visit(QueryTokenPropertyValue node, Object data) {
			return ((StringBuilder) data).append(' ').append(node.jjtGetValue());
		}

		@Override
		public Object visit(QueryTokenQuery node, Object data) {
			if (node.jjtGetNumChildren() >= 1) { // extract(..)を処理
				Node childNode = node.jjtGetChild(0);
				if (childNode instanceof QueryTokenFunction) {
					if ("extract".equals(((QueryTokenFunction) childNode).jjtGetValue())) {
						isExtract = true;
						// "extract("と")"の部分は表示しない。
						int argCount = childNode.jjtGetNumChildren();
						if (argCount == 0) { // argが指定されていないときは処理終わり
							return data;
						} else {
							((QueryTokenFunction) childNode).childrenAccept(this, data);
						}
					}
				}
			}
			// extract(..)がない→除外
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(QueryTokenStart node, Object data) {
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(SimpleNode node, Object data) {
			return null;
		}
	}

	/** スペースを削除するフィルタクエリビジター */
	protected static class FilterQueryNormalizer implements FilterParserVisitor {

		@Override
		public Object visit(QueryTokenFunction node, Object data) {
			StringBuilder stringBuilder = (StringBuilder) data;
			stringBuilder.append(node.jjtGetValue()).append('(');

			int count = node.jjtGetNumChildren();
			if (count >= 0) {
				for (int i = 0; i < count; i++) {
					node.jjtGetChild(i).jjtAccept(this, data);
					stringBuilder.append(',');
				}
				stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			}
			stringBuilder.append(')');
			return null;
		}

		@Override
		public Object visit(QueryTokenProperty node, Object data) {
			((StringBuilder) data).append(node.jjtGetValue());
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(QueryTokenPropertyOperator node, Object data) {
			return ((StringBuilder) data).append(node.jjtGetValue());
		}

		@Override
		public Object visit(QueryTokenPropertyValue node, Object data) {
			return ((StringBuilder) data).append(node.jjtGetValue());
		}

		@Override
		public Object visit(QueryTokenQuery node, Object data) {
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(QueryTokenStart node, Object data) {
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(SimpleNode node, Object data) {
			return null;
		}
	}

	private String propertyKey;
	private ClientProperties properties;
	private JTextArea filterEditTextArea;
	private JRadioButton excludeOptionButton;
	private JRadioButton extractOptionButton;

	/**
	 * インスタンスを生成する。
	 *
	 * @param displayString 表示名
	 * @param propertyKey   プロパティキー
	 */
	public FilterEditFrame(ClientConfiguration configuration, String displayString, String propertyKey) {
		this.propertyKey = propertyKey;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		properties = configuration.getConfigProperties();
		initComponents(displayString);
	}

	private JRadioButton getComponentExcludeOption() {
		if (excludeOptionButton == null) {
			excludeOptionButton = new JRadioButton("除外");
		}
		return excludeOptionButton;
	}

	private JRadioButton getComponentExtractOption() {
		if (extractOptionButton == null) {
			extractOptionButton = new JRadioButton("抽出");
		}
		return extractOptionButton;
	}

	private JTextArea getComponentFilterEditTextArea() {
		if (filterEditTextArea == null) {
			filterEditTextArea = new JTextArea();
		}
		return filterEditTextArea;
	}

	private void initComponents(String displayString) {
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(getComponentExcludeOption());
		buttonGroup.add(getComponentExtractOption());

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING) //
				.addGroup(LEADING,
						layout.createSequentialGroup() //
								.addComponent(getComponentExcludeOption()) //
								.addGap(0, 16, Short.MAX_VALUE) //
								.addComponent(
										getComponentExtractOption())) //
				.addComponent(getComponentFilterEditTextArea(), LEADING));
		layout.setVerticalGroup(layout.createSequentialGroup() //
				.addGroup(
						layout.createParallelGroup(LEADING) //
								.addComponent(getComponentExcludeOption(), LEADING, PREFERRED_SIZE, PREFERRED_SIZE,
										PREFERRED_SIZE) //
								.addComponent(getComponentExtractOption(),
										TRAILING, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)) //
				.addComponent(getComponentFilterEditTextArea(), 16, DEFAULT_SIZE, Short.MAX_VALUE));
		addWindowListener(this);
		setTitle("フィルタの編集 (" + displayString + ": " + propertyKey + ")");

		String filterQuery = properties.getProperty(propertyKey);
		if (filterQuery != null) {
			StringBuilder stringBuilder = new StringBuilder(filterQuery.length());
			try {
				QueryTokenStart tokenStart = FilterCompiler.tokenize(filterQuery);
				FilterQueryFormatter queryFormatter = new FilterQueryFormatter();
				tokenStart.jjtAccept(queryFormatter, stringBuilder);
				if (queryFormatter.isExtractFilter()) {
					getComponentExtractOption().setSelected(true);
				} else {
					getComponentExcludeOption().setSelected(true);
				}
			} catch (ParseException e) {
				stringBuilder.append(filterQuery).append("\n\n/* クエリのパース中にエラー: ").append(e.getLocalizedMessage())
						.append("\n*/");
			}
			getComponentFilterEditTextArea().setText(stringBuilder.toString());
		}

		pack();
		setSize(400, 400);
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		setExtendedState(NORMAL);
		try {
			QueryTokenStart tokenStart = FilterCompiler.tokenize(getComponentFilterEditTextArea().getText());
			StringBuilder stringBuilder = new StringBuilder();

			tokenStart.jjtAccept(new FilterQueryNormalizer(), stringBuilder);

			if (getComponentExtractOption().isSelected()) {
				stringBuilder.insert(0, "extract(");
				stringBuilder.append(')');
			}
			properties.setProperty(propertyKey, stringBuilder.toString());
			dispose();
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, "正しくない文法のクエリです。\n" + ex.getLocalizedMessage());
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
}
