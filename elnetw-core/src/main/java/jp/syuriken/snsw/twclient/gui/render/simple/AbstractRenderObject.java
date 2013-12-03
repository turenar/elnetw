package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.gui.render.RenderPanel;
import jp.syuriken.snsw.twclient.gui.render.RenderTarget;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import twitter4j.Status;

/**
 * Template for Simple Renderer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractRenderObject implements RenderObject, KeyListener, FocusListener, MouseListener {
	/** ポストリストの間のパディング */
	/*package*/static final int PADDING_OF_POSTLIST = 1;
	/** クリップボード */
	protected static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	public static final int CREATED_BY_MAX_LEN = 11;

	/**
	 * HTMLEntityたちを表示できる文字 (&nbsp;等) に置き換える
	 *
	 * @param text テキスト
	 * @return {@link StringBuilder}
	 */
	protected static StringBuilder escapeHTML(CharSequence text) {
		return escapeHTML(text, new StringBuilder(text.length() * 2));
	}

	@Override
	public void onEvent(String name, Object arg) {
		// stub
	}

	/**
	 * HTMLEntityたちを表示できる文字 (&nbsp;等) に置き換える
	 *
	 * @param text     テキスト
	 * @param appendTo 追加先
	 * @return {@link StringBuilder}
	 */
	protected static StringBuilder escapeHTML(CharSequence text, StringBuilder appendTo) {
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			switch (c) {
				case '&':
					appendTo.append("&amp;");
					break;
				case '>':
					appendTo.append("&gt;");
					break;
				case '<':
					appendTo.append("&lt;");
					break;
				case '"':
					appendTo.append("&quot;");
					break;
				case '\'':
					appendTo.append("&#39;");
					break;
				case '\n':
					appendTo.append("<br>");
					break;
				case ' ':
					appendTo.append("&nbsp;");
					break;
				default:
					appendTo.append(c);
					break;
			}
		}
		return appendTo;
	}

	protected static String getCreatedByLongText(Status status) {
		return MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status.getUser().getName());
	}

	protected static void nl2br(StringBuffer stringBuffer) {
		int start = stringBuffer.length();
		int offset = start;
		int position;
		while ((position = stringBuffer.indexOf("\n", offset)) >= 0) {
			stringBuffer.replace(position, position + 1, "<br>");
			offset = position + 1;
		}
		offset = start;
		while ((position = stringBuffer.indexOf(" ", offset)) >= 0) {
			stringBuffer.replace(position, position + 1, "&nbsp;");
			offset = position + 1;
		}
		offset = start;
		while ((position = stringBuffer.indexOf("&amp;", offset)) >= 0) {
			stringBuffer.replace(position, position + 5, "&amp;amp;");
			offset = position + 9;
		}
	}

	protected final SimpleRenderer renderer;
	protected final JPopupMenu popupMenu;
	protected final ClientConfiguration configuration;
	protected final ClientProperties configProperties;
	protected final ImageCacher imageCacher;
	protected final RenderTarget target;
	protected final ClientFrameApi frameApi;
	protected RenderPanel linePanel;
	protected JLabel componentUserIcon = new JLabel();
	protected JLabel componentSentBy = new JLabel();
	protected JLabel componentStatusText = new JLabel();
	protected Color foregroundColor = Color.BLACK;
	protected Color backgroundColor = Color.WHITE;

	public AbstractRenderObject(SimpleRenderer renderer) {
		popupMenu = renderer.getPopupMenu();
		this.target = renderer.getTarget();
		configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		imageCacher = configuration.getImageCacher();
		this.renderer = renderer;
		frameApi = configuration.getFrameApi();
	}

	@Override
	public void focusGained(FocusEvent e) {
		renderer.fireFocusEvent(e, this);
	}

	@Override
	public void focusLost(FocusEvent e) {
	}

	@Override
	public abstract Object getBasedObject();

	@Override
	public RenderPanel getComponent() {
		if (linePanel == null) {
			initComponents();
			RenderPanel linePanel = new RenderPanel(this);
			BoxLayout layout = new BoxLayout(linePanel, BoxLayout.X_AXIS);
			linePanel.setLayout(layout);
			linePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			componentUserIcon.setInheritsPopupMenu(true);
			componentUserIcon.setFocusable(false);
			componentUserIcon.setMinimumSize(renderer.getIconSize());
			componentUserIcon.setMaximumSize(renderer.getIconSize());
			linePanel.add(componentUserIcon);
			linePanel.add(Box.createHorizontalStrut(3));
			componentSentBy.setInheritsPopupMenu(true);
			componentSentBy.setFocusable(false);
			componentSentBy.setMinimumSize(renderer.getLinePanelSizeOfSentBy());
			componentSentBy.setMaximumSize(renderer.getLinePanelSizeOfSentBy());
			componentSentBy.setFont(renderer.getDefaultFont());
			linePanel.add(componentSentBy);
			linePanel.add(Box.createHorizontalStrut(3));
			componentStatusText.setInheritsPopupMenu(true);
			componentStatusText.setFocusable(false);
			componentStatusText.setFont(renderer.getDefaultFont());
			int dataWidth = renderer.getFontMetrics().stringWidth(componentStatusText.getText());

			linePanel.add(componentStatusText);
			linePanel.setComponentPopupMenu(renderer.getPopupMenu());

			linePanel.setBackground(backgroundColor);
			int height = renderer.getIconSize().height + PADDING_OF_POSTLIST;
			Dimension minSize = new Dimension(
					renderer.getIconSize().width + renderer.getLinePanelSizeOfSentBy().width + dataWidth + 3 * 2,
					height);
			linePanel.setMinimumSize(minSize);
			linePanel.setPreferredSize(minSize);
			linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
			linePanel.setFocusable(true);
			linePanel.addMouseListener(this);
			linePanel.addFocusListener(this);
			linePanel.addKeyListener(this);
			componentUserIcon.setForeground(foregroundColor);
			componentSentBy.setForeground(foregroundColor);
			componentStatusText.setForeground(foregroundColor);
			this.linePanel = linePanel;
		}
		return linePanel;
	}

	@Override
	public abstract String getCreatedBy();

	@Override
	public abstract Date getDate();

	/**
	 * Create IntentArguments
	 *
	 * @param actionCommand (name)[!(key)[=(value)][, ...]]
	 * @return IntentArguments
	 */
	protected IntentArguments getIntentArguments(String actionCommand) {
		IntentArguments intentArguments = Utility.getIntentArguments(actionCommand);
		intentArguments.putExtra(ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA, this);
		return intentArguments;
	}

	protected String getShortenString(String string, int maxLen) {
		if (string.length() > maxLen) {
			string = string.substring(0, maxLen - 2) + "..";
		}
		return string;
	}

	@Override
	public abstract String getUniqId();

	protected abstract void initComponents();

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		e.getComponent().requestFocusInWindow();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public abstract void requestCopyToClipboard();
}