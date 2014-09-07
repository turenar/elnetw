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

package jp.mydns.turenar.twclient.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientProperties;
import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.query.FilterQueryFormatter;
import jp.mydns.turenar.twclient.filter.query.FilterQueryNormalizer;
import jp.mydns.turenar.twclient.filter.query.QueryCompiler;
import jp.mydns.turenar.twclient.filter.query.QueryController;
import jp.mydns.turenar.twclient.filter.query.QueryDispatcherBase;
import jp.mydns.turenar.twclient.filter.tokenizer.ParseException;
import jp.mydns.turenar.twclient.filter.tokenizer.QueryTokenStart;

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
public class QueryEditFrame extends JFrame implements WindowListener {
	private class NullQueryController implements QueryController {
		@Override
		public void disableDelay(QueryDispatcherBase delayer) {
			// do nothing
		}

		@Override
		public void enableDelay(QueryDispatcherBase delayer) {
			// do nothing
		}

		@Override
		public String getTargetUserId() {
			return configuration.getAccountIdForRead();
		}

		@Override
		public void onClientMessage(String name, Object arg) {
			// do nothing
		}
	}

	public static final int DEFAULT_FRAME_SIZE = 400;
	private final ClientConfiguration configuration;

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
	public QueryEditFrame(String displayString, String propertyKey) {
		this.propertyKey = propertyKey;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		configuration = ClientConfiguration.getInstance();
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
								.addComponent(getComponentExtractOption())
				)
				.addComponent(getComponentFilterEditTextArea(), LEADING));
		layout.setVerticalGroup(layout.createSequentialGroup() //
				.addGroup(
						layout.createParallelGroup(LEADING) //
								.addComponent(getComponentExcludeOption(), LEADING, PREFERRED_SIZE, PREFERRED_SIZE,
										PREFERRED_SIZE) //
								.addComponent(getComponentExtractOption(),
										TRAILING, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				) //
				.addComponent(getComponentFilterEditTextArea(), 16, DEFAULT_SIZE, Short.MAX_VALUE));
		addWindowListener(this);
		setTitle("フィルタの編集 (" + displayString + ": " + propertyKey + ")");

		String filterQuery = properties.getProperty(propertyKey);
		if (filterQuery != null) {
			StringBuilder stringBuilder = new StringBuilder(filterQuery.length());
			try {
				QueryTokenStart tokenStart = QueryCompiler.tokenize(filterQuery);
				FilterQueryFormatter queryFormatter = new FilterQueryFormatter(stringBuilder);
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
		setSize(DEFAULT_FRAME_SIZE, DEFAULT_FRAME_SIZE);
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
			String queryText = getComponentFilterEditTextArea().getText().trim();
			String query;
			if (queryText.isEmpty()) {
				query = "";
			} else {
				QueryTokenStart tokenStart = QueryCompiler.tokenize(queryText);
				StringBuilder stringBuilder = new StringBuilder();

				tokenStart.jjtAccept(new FilterQueryNormalizer(stringBuilder), stringBuilder);

				if (getComponentExtractOption().isSelected()) {
					stringBuilder.insert(0, "extract(");
					stringBuilder.append(')');
				}
				query = stringBuilder.toString();
				// test compilable? (for regex test)
				QueryCompiler.getCompiledObject(query, new NullQueryController());
			}
			properties.setProperty(propertyKey, query);
			dispose();
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, "正しくない文法のクエリです。\n" + ex.getLocalizedMessage());
		} catch (IllegalSyntaxException ex) {
			JOptionPane.showMessageDialog(this,
					"フィルターとしてコンパイルできませんでした。\n" + ex.getLocalizedMessage());
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
