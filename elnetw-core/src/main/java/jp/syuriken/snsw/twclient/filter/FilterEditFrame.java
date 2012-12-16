package jp.syuriken.snsw.twclient.filter;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
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

/**
 * フィルタを編集するためのフレーム
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings("serial")
public class FilterEditFrame extends JFrame implements WindowListener {

	/**
	 * クエリを見やすくするフォーマッタ
	 *
	 */
	protected static class FilterQueryFormatter implements FilterParserVisitor {

		/** 関数の深さ */
		protected transient int queryDepth;


		@Override
		public Object visit(QueryTokenFunction node, Object data) {
			StringBuilder stringBuilder = (StringBuilder) data;

			int childrenCount = node.jjtGetNumChildren();
			node.jjtGetChild(0).jjtAccept(this, data);

			queryDepth++;
			stringBuilder.append("(\n");

			for (int i = 1; i < childrenCount; i++) {
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
		public Object visit(QueryTokenFunctionName node, Object data) {
			return ((StringBuilder) data).append(node.jjtGetValue());
		}

		@Override
		public Object visit(QueryTokenProperty node, Object data) {
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(QueryTokenPropertyName node, Object data) {
			return ((StringBuilder) data).append(node.jjtGetValue());

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

	/**
	 * スペースを削除するフィルタクエリビジター
	 */
	protected static class FilterQueryNormalizer implements FilterParserVisitor {

		@Override
		public Object visit(QueryTokenFunction node, Object data) {
			StringBuilder stringBuilder = (StringBuilder) data;
			QueryTokenFunctionName name = (QueryTokenFunctionName) node.jjtGetChild(0);
			stringBuilder.append(name.jjtGetValue()).append('(');

			int count = node.jjtGetNumChildren();
			for (int i = 1; i < count; i++) {
				node.jjtGetChild(i).jjtAccept(this, data);
				stringBuilder.append(',');
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append(')');
			return null;
		}

		@Override
		public Object visit(QueryTokenFunctionName node, Object data) {
			return ((StringBuilder) data).append(node.jjtGetValue());
		}

		@Override
		public Object visit(QueryTokenProperty node, Object data) {
			return node.childrenAccept(this, data);
		}

		@Override
		public Object visit(QueryTokenPropertyName node, Object data) {
			return ((StringBuilder) data).append(node.jjtGetValue());
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


	/**
	 * インスタンスを生成する。
	 *
	 * @param displayString 表示名
	 * @param propertyKey プロパティキー
	 */
	public FilterEditFrame(String displayString, String propertyKey) {
		this.propertyKey = propertyKey;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		properties = ClientConfiguration.getInstance().getConfigProperties();
		initComponents(displayString);
	}

	private JTextArea getComponentFilterEditTextArea() {
		if (filterEditTextArea == null) {
			filterEditTextArea = new JTextArea();

			String filterQuery = properties.getProperty(propertyKey);
			if (filterQuery != null) {
				StringBuilder stringBuilder = new StringBuilder(filterQuery.length());
				try {
					QueryTokenStart filterCompiler = FilterCompiler.tokenize(filterQuery);
					filterCompiler.jjtAccept(new FilterQueryFormatter(), stringBuilder);
				} catch (ParseException e) {
					stringBuilder.append(filterQuery).append("\n\n/* クエリのパース中にエラー: ").append(e.getLocalizedMessage())
						.append("\n*/");
				}
				filterEditTextArea.setText(stringBuilder.toString());
			}
		}
		return filterEditTextArea;
	}

	private void initComponents(String displayString) {
		GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(getComponentFilterEditTextArea()));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(getComponentFilterEditTextArea()));
		addWindowListener(this);
		setTitle("フィルタの編集 (" + displayString + ": " + propertyKey + ")");
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
