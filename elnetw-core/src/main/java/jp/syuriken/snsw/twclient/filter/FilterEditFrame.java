package jp.syuriken.snsw.twclient.filter;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * フィルタを編集するためのフレーム
 *
 * @author Turenar <snswinhaiku dot lo at gmail dot com>
 */
@SuppressWarnings("serial")
public class FilterEditFrame extends JFrame implements WindowListener {

	private static Logger logger = LoggerFactory.getLogger(FilterEditFrame.class);

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
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		properties = ClientConfiguration.getInstance().getConfigProperties();
		initComponents(displayString);
	}

	private JTextArea getComponentFilterEditTextArea() {
		if (filterEditTextArea == null) {
			filterEditTextArea = new JTextArea();

			String filterQuery = properties.getProperty(propertyKey);
			if (filterQuery != null) {
				FilterCompiler filterCompiler = new FilterCompiler(filterQuery);
				StringBuilder stringBuilder = new StringBuilder(filterQuery.length());
				int depth = 0;
				try {
					while (filterCompiler.nextToken() != null) {
						String token = filterCompiler.getQueryToken();
						TokenType tokenType = filterCompiler.getNextTokenType();
						switch (tokenType) {
							case PROPERTY_OPERATOR:
							case SCALAR_INT:
							case SCALAR_STRING:
								stringBuilder.append(' ');
								//$FALL-THROUGH$
							case DEFAULT:
							case FUNC_NAME:
							case PROPERTY_NAME:
								stringBuilder.append(token);
								break;
							case FUNC_START:
								depth++;
								//$FALL-THROUGH$
							case FUNC_ARG_SEPARATOR:
								stringBuilder.append(token);
								stringBuilder.append('\n');
								for (int i = 0; i < depth; i++) {
									stringBuilder.append("  ");
								}
								break;
							case FUNC_END:
								depth--;
								stringBuilder.append('\n');
								for (int i = 0; i < depth; i++) {
									stringBuilder.append("  ");
								}
								stringBuilder.append(token);
								break;
							case EOD:
								throw new IllegalSyntaxException("got EOD as tokenType");
							case UNEXPECTED:
								throw new IllegalSyntaxException("got UNEXPECTED as tokenType");
						}
					}
				} catch (IllegalSyntaxException e) {
					logger.warn("フィルタの文字解析中にエラー", e);
					stringBuilder.append('\n').append(filterQuery, filterCompiler.getCompilingIndex(),
							filterQuery.length());
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
		FilterCompiler filterCompiler = new FilterCompiler(getComponentFilterEditTextArea().getText());
		StringBuilder stringBuilder = new StringBuilder();
		String token;
		setExtendedState(NORMAL);
		try {
			while ((token = filterCompiler.nextToken()) != null) {
				if (filterCompiler.getNextTokenType() == TokenType.UNEXPECTED) {
					throw new IllegalSyntaxException("got UNEXPECTED as token");
				}
				stringBuilder.append(token);
			}
			properties.setProperty(propertyKey, stringBuilder.toString());
			dispose();
		} catch (IllegalSyntaxException ex) {
			logger.error("保存中にエラー: 正しくない文法のクエリです", ex);
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
