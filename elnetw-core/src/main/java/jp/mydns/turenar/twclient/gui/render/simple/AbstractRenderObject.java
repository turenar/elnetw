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

package jp.mydns.turenar.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientEventConstants;
import jp.mydns.turenar.twclient.ClientFrameApi;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.cache.ImageCacher;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.gui.ShortcutKeyManager;
import jp.mydns.turenar.twclient.gui.render.RenderObject;
import jp.mydns.turenar.twclient.gui.render.RenderPanel;
import jp.mydns.turenar.twclient.gui.render.RenderTarget;
import jp.mydns.turenar.twclient.intent.Intent;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.EntitySupport;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TweetEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Template for Simple Renderer
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractRenderObject implements RenderObject,
		FocusListener, MouseListener, ClientEventConstants, PopupMenuListener {
	/** Entityの開始位置を比較する */
	private static final class EntityComparator implements Comparator<TweetEntity>, Serializable {
		private static final long serialVersionUID = -6063590113086378960L;

		@Override
		public int compare(TweetEntity o1, TweetEntity o2) {
			int diff = o1.getStart() - o2.getStart();
			if (diff == 0) {
				// prior MediaEntity to URLEntity
				diff = (o1 instanceof MediaEntity ? -1 : 0) - (o2 instanceof MediaEntity ? -1 : 0);
			}
			return diff;
		}
	}

	/** ポストリストの間のパディング */
	/*package*/static final int PADDING_OF_POSTLIST = 1;
	/** クリップボード */
	protected static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	/**
	 * リストに表示するcreatedByのテキスト最大長
	 */
	public static final int CREATED_BY_MAX_LEN = 11;
	/**
	 * リストに表示するテキスト最大長
	 */
	public static final int TEXT_MAX_LEN = 255;
	private static final Logger logger = LoggerFactory.getLogger(AbstractRenderObject.class);

	/**
	 * HTMLEntityたちを表示できる文字 (&nbsp;等) に置き換える
	 *
	 * @param text テキスト
	 * @return {@link StringBuilder}
	 */
	protected static StringBuilder escapeHTML(CharSequence text) {
		return escapeHTML(text, new StringBuilder(text.length() * 2));
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

	/**
	 * get createdBy long text
	 *
	 * @param status status
	 * @return @_screenName_ (_userName_)
	 */
	protected static String getCreatedByLongText(Status status) {
		return MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status.getUser().getName());
	}

	/**
	 * entityたちを全部ソート
	 *
	 * @param status エンティティ対応クラスインスタンス
	 * @return エンティティ配列 (start昇順)
	 */
	protected static TweetEntity[] sortEntities(EntitySupport status) {
		int entitiesLen;
		HashtagEntity[] hashtagEntities = status.getHashtagEntities();
		entitiesLen = hashtagEntities == null ? 0 : hashtagEntities.length;
		URLEntity[] urlEntities = status.getURLEntities();
		entitiesLen += urlEntities == null ? 0 : urlEntities.length;
		MediaEntity[] mediaEntities = status.getMediaEntities();
		entitiesLen += mediaEntities == null ? 0 : mediaEntities.length;
		UserMentionEntity[] mentionEntities = status.getUserMentionEntities();
		entitiesLen += mentionEntities == null ? 0 : mentionEntities.length;
		TweetEntity[] entities = new TweetEntity[entitiesLen];

		if (entitiesLen != 0) {
			int copyOffset = 0;
			if (hashtagEntities != null) {
				System.arraycopy(hashtagEntities, 0, entities, copyOffset, hashtagEntities.length);
				copyOffset += hashtagEntities.length;
			}
			if (urlEntities != null) {
				System.arraycopy(urlEntities, 0, entities, copyOffset, urlEntities.length);
				copyOffset += urlEntities.length;
			}
			if (mediaEntities != null) {
				System.arraycopy(mediaEntities, 0, entities, copyOffset, mediaEntities.length);
				copyOffset += mediaEntities.length;
			}
			if (mentionEntities != null) {
				System.arraycopy(mentionEntities, 0, entities, copyOffset, mentionEntities.length);
			}
		}
		Arrays.sort(entities, new EntityComparator());
		return entities;
	}

	/**
	 * レンターターゲット
	 */
	protected final RenderTarget target;
	/**
	 * レンダラ
	 */
	protected SimpleRenderer renderer;
	/**
	 * 右クリックで表示するポップアップメニュー
	 */
	protected JPopupMenu popupMenu;
	/**
	 * コンポーネント
	 */
	protected RenderPanel linePanel;
	/**
	 * ユーザーアイコンコンポーネント
	 */
	protected JLabel componentUserIcon = new JLabel();
	/**
	 * 作成者コンポーネント
	 */
	protected JLabel componentSentBy = new JLabel();
	/**
	 * テキストコンポーネント
	 */
	protected JLabel componentStatusText = new JLabel();
	/**
	 * 前景色
	 */
	protected Color foregroundColor = Color.BLACK;
	/**
	 * 背景色
	 */
	protected Color backgroundColor = Color.WHITE;

	/**
	 * インスタンスの生成
	 *
	 * @param renderer レンダラ
	 */
	public AbstractRenderObject(SimpleRenderer renderer) {
		this.target = renderer.getTarget();
		this.renderer = renderer;
	}

	@Override
	public void focusGained(FocusEvent e) {
		getFrameApi().clearTweetView();
		if (renderer.getFocusOwner() != null) {
			AbstractRenderObject lostFocusObject = renderer.getFocusOwner();
			lostFocusObject.linePanel.setBackground(lostFocusObject.backgroundColor);
		}
		renderer.setFocusOwner(this);
		linePanel.setBackground(Utility.blendColor(backgroundColor,
				getConfigProperties().getColor(ClientConfiguration.PROPERTY_COLOR_FOCUS_LIST)));
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
			popupMenu = renderer.getPopupMenu();
			linePanel.setComponentPopupMenu(popupMenu);
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
			ShortcutKeyManager.setKeyMap("list", linePanel);
			componentUserIcon.setForeground(foregroundColor);
			componentSentBy.setForeground(foregroundColor);
			componentStatusText.setForeground(foregroundColor);
			this.linePanel = linePanel;
		}
		return linePanel;
	}

	/**
	 * get {@link jp.mydns.turenar.twclient.conf.ClientProperties} instance
	 *
	 * @return getRenderer().getConfigProperties()
	 */
	public ClientProperties getConfigProperties() {
		return renderer.getConfigProperties();
	}

	/**
	 * get {@link jp.mydns.turenar.twclient.ClientConfiguration} instance
	 *
	 * @return getRenderer().getConfiguration()
	 */
	public ClientConfiguration getConfiguration() {
		return renderer.getConfiguration();
	}

	@Override
	public abstract String getCreatedBy();

	@Override
	public abstract Date getDate();

	/**
	 * get ClientFrameApi instance
	 *
	 * @return getConfiguration().getFrameApi()
	 */
	public ClientFrameApi getFrameApi() {
		return renderer.getConfiguration().getFrameApi();
	}

	/**
	 * get ImageCacher instance
	 *
	 * @return getRenderer().getImageCacher()
	 */
	protected ImageCacher getImageCacher() {
		return renderer.getImageCacher();
	}

	/**
	 * Create IntentArguments
	 *
	 * @param actionCommand (name)[!(key)[=(value)][, ...]]
	 * @return IntentArguments
	 */
	protected IntentArguments getIntentArguments(String actionCommand) {
		IntentArguments intentArguments = Utility.getIntentArguments(actionCommand);
		intentArguments.putExtra(Intent.INTENT_ARG_NAME_SELECTING_POST_DATA, this);
		return intentArguments;
	}

	/**
	 * popup menu type
	 *
	 * @return ポップアップメニュータイプ
	 */
	protected String getPopupMenuType() {
		return "default";
	}

	/**
	 * 最後に..を付けて長いテキストを省略する
	 *
	 * @param string テキスト
	 * @param maxLen 最大長
	 * @return maxLen以下の文字列
	 */
	protected String getShortenString(String string, int maxLen) {
		if (string.length() > maxLen) {
			string = string.substring(0, maxLen - 2) + "..";
		}
		return string;
	}

	@Override
	public abstract String getUniqId();

	/**
	 * コンポーネントの初期化。初めて{@link #getComponent()}が呼び出された時に呼び出される。
	 * componentSentBy, componentStatusText, componentUserIconを初期化しよう。
	 */
	protected abstract void initComponents();


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
	public void onEvent(String name, Object arg) {
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		getComponent().requestFocusInWindow();
		this.focusGained(new FocusEvent(getComponent(), FocusEvent.FOCUS_GAINED, true));
		logger.info("popupMenuWillBecomeVisible");
		renderer.generatePopupMenu(getPopupMenuType());
	}
}
