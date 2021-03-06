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

package jp.mydns.turenar.twclient.gui.tab;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientEventConstants;
import jp.mydns.turenar.twclient.ClientFrameApi;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.cache.ImageCacher;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.filter.MessageFilter;
import jp.mydns.turenar.twclient.filter.TeeFilter;
import jp.mydns.turenar.twclient.gui.render.RenderObject;
import jp.mydns.turenar.twclient.gui.render.RenderPanel;
import jp.mydns.turenar.twclient.gui.render.RenderTarget;
import jp.mydns.turenar.twclient.gui.render.RendererManager;
import jp.mydns.turenar.twclient.intent.Intent;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.internal.ScrollUtility;
import jp.mydns.turenar.twclient.internal.ScrollUtility.BoundsTranslator;
import jp.mydns.turenar.twclient.internal.SortedPostListPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * ツイート表示用のタブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class AbstractClientTab implements ClientTab, RenderTarget {
	/**
	 * 標準レンダラに移譲するレンダラ。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public abstract class DelegateRenderer extends ClientMessageAdapter implements TabRenderer {
		@Override
		public void addChild(MessageFilter filter) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public final MessageFilter clone() throws CloneNotSupportedException { // CS-IGNORE
			throw new CloneNotSupportedException();
		}

		private void focusUserNearestEntry(boolean prev) {
			if (selectingPost == null) {
				getSortedPostListPanel().requestFocusInWindow();
			} else {
				TreeSet<RenderPanel> usersPanels = getPanelsFromCreatedBy(
						selectingPost.getRenderObject().getCreatedBy(), false);
				if (usersPanels != null) {
					RenderPanel focusTo = prev ? usersPanels.lower(selectingPost) : usersPanels.higher(selectingPost);
					if (focusTo != null) {
						focusAndScroll(focusTo);
					}
				}
			}
		}

		@Override
		public MessageFilter getChild() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getUserId() {
			return accountId;
		}

		@Override
		public void onClientMessage(String name, Object arg) {
			switch (name) {
				case REQUEST_FOCUS_TAB_COMPONENT:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						selectingPost.requestFocusInWindow();
					}
					break;
				case REQUEST_FOCUS_NEXT_COMPONENT:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						getSortedPostListPanel().requestFocusNextOf(selectingPost);
						shouldBeScrollToPost = true;
					}
					break;
				case REQUEST_FOCUS_PREV_COMPONENT:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						getSortedPostListPanel().requestFocusPreviousOf(selectingPost);
						shouldBeScrollToPost = true;
					}
					break;
				case REQUEST_FOCUS_USER_PREV_COMPONENT:
					focusUserNearestEntry(true);
					break;
				case REQUEST_FOCUS_USER_NEXT_COMPONENT:
					focusUserNearestEntry(false);
					break;
				case REQUEST_FOCUS_FIRST_COMPONENT:
					getSortedPostListPanel().requestFocusFirstComponent();
					shouldBeScrollToPost = true;
					break;
				case REQUEST_FOCUS_LAST_COMPONENT:
					getSortedPostListPanel().requestFocusLastComponent();
					shouldBeScrollToPost = true;
					break;
				case REQUEST_FOCUS_WINDOW_FIRST_COMPONENT:
					getSortedPostListPanel().getComponentAt(0, getScrollPane().getViewport().getViewPosition().y)
							.requestFocusInWindow();
					break;
				case REQUEST_FOCUS_WINDOW_LAST_COMPONENT: {
					JViewport viewport = getScrollPane().getViewport();
					getSortedPostListPanel().getComponentAt(0, viewport.getViewPosition().y + viewport.getHeight())
							.requestFocusInWindow();
				}
				break;
				case REQUEST_SCROLL_AS_WINDOW_LAST:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						Rectangle bounds = getSortedPostListPanel().getBoundsOf(selectingPost);
						JViewport viewport = getScrollPane().getViewport();
						int x = viewport.getViewPosition().x;
						int y = bounds.y - (viewport.getHeight() - bounds.height);
						viewport.setViewPosition(new Point(x, y));
					}
					break;
				case REQUEST_FOCUS_IN_REPLY_TO:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status tag = (Status) renderObject.getBasedObject();
							RenderPanel renderPanel = statusMap.get(
									RendererManager.getStatusUniqId(tag.getInReplyToStatusId()));
							if (renderPanel != null) {
								inReplyToStack.push(selectingPost);
								focusAndScroll(renderPanel);
							}
						}
					}
					break;
				case REQUEST_FOCUS_BACK_REPLIED_BY:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						if (!inReplyToStack.isEmpty()) {
							focusAndScroll(inReplyToStack.pop());
						}
					}
					break;
				case OPEN_TIMELINE_IN_BROWSER:
					try {
						configuration.getUtility().openBrowser(getTwitterUrl());
					} catch (IOException | URISyntaxException e) {
						logger.warn("open browser failed", e);
					}
					break;
				default:
					if (selectingPost != null) {
						selectingPost.onEvent(name, arg);
					}
					actualRenderer.onClientMessage(name, arg);
			}
		}

		@Override
		public void onDisplayRequirement() {
			actualRenderer.onDisplayRequirement();
		}

		@Override
		public void setChild(MessageFilter child) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * PostListを更新する。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class PostListUpdater implements Runnable {

		@Override
		public void run() {
			final AbstractClientTab this$dct = AbstractClientTab.this; // Suppress Warning of FindBugs
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					LinkedList<RenderPanel> postListAddQueue = this$dct.postListAddQueue;
					SortedPostListPanel sortedPostListPanel = this$dct.getSortedPostListPanel();
					JScrollPane postListScrollPane = this$dct.getScrollPane();
					Point oldViewPosition = postListScrollPane.getViewport().getViewPosition();
					RenderPanel firstComponent = sortedPostListPanel.getComponentAt(oldViewPosition);
					Rectangle oldBounds = firstComponent == null ? null
							: sortedPostListPanel.getBoundsOf(firstComponent);

					synchronized (postListAddQueue) {
						sortedPostListPanel.add(postListAddQueue);
					}

					Point newViewPosition;
					if (firstComponent == null || oldViewPosition.y < this$dct.fontHeight) {
						newViewPosition = new Point(oldViewPosition.x, 0);
					} else {
						Rectangle newBounds = sortedPostListPanel.getBoundsOf(firstComponent);
						int y = newBounds.y - oldBounds.y + oldViewPosition.y;
						newViewPosition = new Point(oldViewPosition.x, y);
					}
					postListScrollPane.getViewport().setViewPosition(newViewPosition);
				}

			});
		}
	}

	/** uniqIdの衝突防止のために使用される乱数ジェネレーター。 */
	protected static final Random random = new Random();
	/** {@link jp.mydns.turenar.twclient.ClientConfiguration#getFrameApi()} */
	protected final ClientFrameApi frameApi;
	/** SortedPostListPanelインスタンス */
	protected final SortedPostListPanel sortedPostListPanel;
	/** 設定 */
	protected final ClientConfiguration configuration;
	/** {@link ClientConfiguration#getImageCacher()} */
	protected final ImageCacher imageCacher;
	/*package*/final Logger logger = LoggerFactory.getLogger(AbstractClientTab.class);
	/** {@link jp.mydns.turenar.twclient.conf.ClientProperties} */
	protected final ClientProperties configProperties;
	/**
	 * 他のタブと区別するためのユニークなID。
	 * これはフィルタの保存やシリアライズ処理などに使用されます。
	 */
	protected final String uniqId;
	/**
	 * アカウントID
	 */
	protected final String accountId;
	/** デフォルトフォント */
	public final Font defaultFont;
	/** UIフォント */
	public final Font uiFont;
	/** UI更新キュー */
	protected final LinkedList<RenderPanel> postListAddQueue = new LinkedList<>();
	/** [K=ユーザーID, V=ユーザーのツイートなど] */
	protected final HashMap<String, TreeSet<RenderPanel>> listItems = new HashMap<>();
	/** tab title label */
	protected JLabel titleLabel;
	/** inReplyTo呼び出しのスタック */
	protected Stack<RenderPanel> inReplyToStack = new Stack<>();
	/** 現在選択しているポスト */
	public RenderPanel selectingPost;
	/** 取得したフォントメトリックス (Default Font) */
	protected FontMetrics fontMetrics;
	/** フォントの高さ */
	protected int fontHeight;
	/** [K=ステータスID, V=ツイートなど] */
	protected HashMap<String, RenderPanel> statusMap = new HashMap<>();
	/** スクロールペーン */
	protected JScrollPane postListScrollPane;
	/** 慣性スクローラー */
	protected ScrollUtility scroller;
	/**
	 * {@link TeeFilter} インスタンスを格納する変数。
	 *
	 * <p>
	 * これは {@link #getActualRenderer()} を用いて最初の {@link #getRenderer()} 呼び出し時に
	 * 初期化されます (それまでは null です)。
	 *
	 * フィルタクエリの解析中にエラーが発生したときは、 {@link #getActualRenderer()} が代わりに
	 * 代入されます。
	 * </p>
	 */
	protected TabRenderer teeFilter;
	/**
	 * 実際に描画処理・移譲を行うタブレンダラ
	 */
	protected TabRenderer actualRenderer;
	/**
	 * 選択しているポストへスクロールするべきかどうか
	 */
	protected boolean shouldBeScrollToPost;
	private volatile boolean focusGained;
	private volatile boolean isDirty;
	private volatile String title;

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId account id
	 */
	protected AbstractClientTab(String accountId) {
		this.configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		imageCacher = configuration.getImageCacher();
		frameApi = configuration.getFrameApi();
		sortedPostListPanel = new SortedPostListPanel();
		this.accountId = accountId;
		uniqId = getTabId() + "_" + Integer.toHexString(random.nextInt());
		uiFont = configProperties.getFont(ClientConfiguration.PROPERTY_GUI_FONT_UI);
		defaultFont = configProperties.getFont(ClientConfiguration.PROPERTY_GUI_FONT_DEFAULT);
		init(configuration);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param tabId  ignored
	 * @param uniqId unique identifier
	 */
	protected AbstractClientTab(@SuppressWarnings("UnusedParameters") String tabId, String uniqId) {
		this.configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		imageCacher = configuration.getImageCacher();
		frameApi = configuration.getFrameApi();
		sortedPostListPanel = new SortedPostListPanel();
		this.uniqId = uniqId;
		uiFont = configProperties.getFont(ClientConfiguration.PROPERTY_GUI_FONT_UI);
		defaultFont = configProperties.getFont(ClientConfiguration.PROPERTY_GUI_FONT_DEFAULT);

		String propertyPrefix = getPropertyPrefix();
		String accountId = configProperties.getProperty(propertyPrefix + ".accountId");
		if (accountId == null) {
			accountId = "$reader";
		}
		this.accountId = accountId;

		init(configuration);
	}

	@Override
	public void addStatus(RenderObject renderObject) {
		synchronized (this) {
			if (statusMap.containsKey(renderObject.getUniqId())) {
				logger.debug("{} is already registered", renderObject.getUniqId());
				return; // already added
			} else {
				statusMap.put(renderObject.getUniqId(), renderObject.getComponent());
			}
			getPanelsFromCreatedBy(renderObject.getCreatedBy(), true).add(renderObject.getComponent());
		}
		synchronized (postListAddQueue) {
			RenderPanel component = renderObject.getComponent();
			postListAddQueue.add(component);
		}

		if (!(focusGained || isDirty)) {
			isDirty = true;
			runInDispatcherThread(new Runnable() {
				@Override
				public void run() {
					getTitleComponent().setText(getTitle() + "*");
				}
			});
		}
	}

	@Override
	public void close() {
		configProperties.removePrefixed(getPropertyPrefix());
	}

	/**
	 * create label for tab title
	 *
	 * @return label
	 */
	protected JLabel createTitleLabel() {
		JLabel label = new JLabel(getTitle());
		label.setIcon(getIcon());
		label.setComponentPopupMenu(createTitleLabelPopup());
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					configuration.focusFrameTab(AbstractClientTab.this);
				}
			}
		});
		return label;
	}

	/**
	 * create popup menu for tab title label
	 *
	 * @return popup menu
	 */
	protected JPopupMenu createTitleLabelPopup() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem closeMenu = new JMenuItem("閉じる");
		closeMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.removeFrameTab(AbstractClientTab.this);
			}
		});
		popupMenu.add(closeMenu);
		return popupMenu;
	}

	/**
	 * focus and set flag to scroll (not immediately scrolling)
	 *
	 * @param focusTo component to be focused
	 */
	protected void focusAndScroll(RenderPanel focusTo) {
		focusTo.requestFocusInWindow();
		shouldBeScrollToPost = true;
	}

	@Override
	public void focusGained(FocusEvent e) {
		selectingPost = (RenderPanel) e.getComponent();
		if (shouldBeScrollToPost) {
			shouldBeScrollToPost = false;
			scrollToSelectingPost();
		}
	}

	@Override
	public void focusGained() {
		focusGained = true;
		isDirty = false;
		runInDispatcherThread(new Runnable() {
			@Override
			public void run() {
				getTitleComponent().setText(getTitle());
			}
		});
	}

	@Override
	public void focusLost() {
		focusGained = false;
	}

	/**
	 * 実際に描画を行うレンダラ
	 *
	 * @return レンダラ
	 * @see #getRenderer()
	 */
	public TabRenderer getActualRenderer() {
		return actualRenderer;
	}

	/**
	 * {@link #getScrollPane()}の子コンポーネント
	 *
	 * @return {@link SortedPostListPanel}インスタンス
	 */
	protected JComponent getChildComponent() {
		return getSortedPostListPanel();
	}

	/**
	 * get default title. If user doesn't specify title, use this as title
	 *
	 * @return title
	 */
	protected abstract String getDefaultTitle();

	/**
	 * 描画処理をレンダラに行わせるクラスインスタンスを取得する
	 *
	 * @return インスタンス
	 */
	public abstract DelegateRenderer getDelegateRenderer();

	@Override
	public abstract Icon getIcon();

	/**
	 * Create IntentArguments
	 *
	 * @param actionCommand (name)[!(key)[=(value)][, ...]]
	 * @return IntentArguments
	 */
	protected IntentArguments getIntentArguments(String actionCommand) {
		IntentArguments intentArguments = Utility.getIntentArguments(actionCommand);

		if (selectingPost != null) {
			intentArguments.putExtra(Intent.INTENT_ARG_NAME_SELECTING_POST_DATA,
					selectingPost.getRenderObject());
		}
		return intentArguments;
	}

	private TreeSet<RenderPanel> getPanelsFromCreatedBy(String createdBy, boolean createFlag) {
		synchronized (listItems) {
			TreeSet<RenderPanel> panels = listItems.get(createdBy);
			if (createFlag && panels == null) {
				panels = new TreeSet<>(SortedPostListPanel.ComponentComparator.SINGLETON);
				listItems.put(createdBy, panels);
			}
			return panels;
		}
	}

	/**
	 * get the prefix of properties for this tab.
	 *
	 * @return prefix
	 */
	protected String getPropertyPrefix() {
		return "gui.tabs.data." + uniqId;
	}

	/**
	 * 描画する前にフィルタする、クラスを取得する。
	 *
	 * <p>フィルタクエリの解析中にエラーが発生したときは、 {@link #getActualRenderer()} が代わりに
	 * 返り値として使用されます。</p>
	 *
	 * @return TeeFilterインスタンス ({@link #teeFilter}変数)
	 * @see #getActualRenderer()
	 * @see #teeFilter
	 */
	@Override
	public TabRenderer getRenderer() {
		if (teeFilter == null) {
			teeFilter = new TeeFilter(uniqId, getDelegateRenderer());
			teeFilter.onClientMessage(ClientEventConstants.INIT_UI, null);
		}
		return teeFilter;
	}

	/**
	 * スクロールペーン。
	 *
	 * @return JScrollPane
	 */
	@SuppressWarnings("serial")
	protected JScrollPane getScrollPane() {
		if (postListScrollPane == null) {
			postListScrollPane = new JScrollPane() {

				@Override
				protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
					switch (ks.getKeyCode()) {
						case KeyEvent.VK_DOWN:
						case KeyEvent.VK_UP:
						case KeyEvent.VK_RIGHT:
						case KeyEvent.VK_LEFT:
							return false;
						default:
							return super.processKeyBinding(ks, e, condition, pressed);
					}
				}

				@Override
				protected void processKeyEvent(KeyEvent e) {
					logger.trace("jscrollpane#processKeyEvent: {}", e);
				}
			};
			postListScrollPane.getViewport().setView(getChildComponent());
			postListScrollPane.getVerticalScrollBar().setUnitIncrement(
					configProperties.getInteger(ClientConfiguration.PROPERTY_LIST_SCROLL));
			scroller = new ScrollUtility(postListScrollPane, new BoundsTranslator() {

				@Override
				public Rectangle translate(JComponent component) {
					if (!(component instanceof RenderPanel)) {
						throw new AssertionError();
					}
					return sortedPostListPanel.getBoundsOf((RenderPanel) component);
				}
			}, configuration.getConfigProperties().getBoolean("gui.scrool.momentumEnabled"));

		}
		return postListScrollPane;
	}

	/**
	 * SortedPostListPanelを取得する(レンダラ用)
	 *
	 * @return {@link SortedPostListPanel}インスタンス
	 */
	protected SortedPostListPanel getSortedPostListPanel() {
		return sortedPostListPanel;
	}

	/**
	 * ステータスを取得する。
	 *
	 * @param statusId ステータスID
	 * @return ステータスデータ
	 */
	public RenderObject getStatus(long statusId) {
		RenderPanel renderPanel = statusMap.get(RendererManager.getStatusUniqId(statusId));
		return renderPanel == null ? null : renderPanel.getRenderObject();
	}

	@Override
	public JComponent getTabComponent() {
		return getScrollPane();
	}

	@Override
	public String getTitle() {
		return title == null ? getDefaultTitle() : title;
	}

	@Override
	public JLabel getTitleComponent() {
		if (titleLabel == null) {
			titleLabel = createTitleLabel();
		}
		return titleLabel;
	}

	/**
	 * get url in twitter website
	 *
	 * @return url
	 */
	protected abstract String getTwitterUrl();

	@Override
	public String getUniqId() {
		return uniqId;
	}

	@Deprecated
	@Override
	public void handleAction(String command) {
		handleAction(getIntentArguments(command));
	}

	@Override
	public void handleAction(IntentArguments args) {
		args.putExtra(Intent.INTENT_ARG_NAME_SELECTING_POST_DATA,
				selectingPost == null ? null : selectingPost.getRenderObject());
		configuration.handleAction(args);
	}

	private void init(ClientConfiguration configuration) {
		fontMetrics = getSortedPostListPanel().getFontMetrics(frameApi.getDefaultFont());
		fontHeight = fontMetrics.getHeight();
		configuration.getTimer().scheduleWithFixedDelay(new PostListUpdater(),
				configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_POSTLIST_UPDATE),
				configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_POSTLIST_UPDATE),
				TimeUnit.MILLISECONDS);
		actualRenderer = RendererManager.get(accountId, this);
		title = configProperties.getProperty(getPropertyPrefix() + ".title");
	}

	/**
	 * <p>
	 * タイムラインの初期化。Display Requirements用にあります。
	 * 他にDisplay Requirementsに準拠できる方法があるのならばこのメソッドをオーバーライドして無効化しても構いません。
	 * </p><p>
	 * この関数は処理の都合から {@link #getTabComponent()} 呼び出し時に呼び出されます。
	 * </p>
	 */
	@Override
	public void initTimeline() {
		getActualRenderer().onDisplayRequirement();
	}

	/**
	 * IDが呼ばれたかどうかを判定する
	 *
	 * @param userMentionEntities エンティティ
	 * @return 呼ばれたかどうか
	 */
	protected boolean isMentioned(UserMentionEntity[] userMentionEntities) {
		return configuration.isMentioned(accountId, userMentionEntities);
	}

	/**
	 * ステータスを削除する
	 *
	 * @param renderObject ステータスデータ
	 */
	@Override
	public void removeStatus(RenderObject renderObject) {
		removeStatus(renderObject.getUniqId());
	}

	/**
	 * ステータスを削除する
	 *
	 * @param uniqId Unique ID
	 */
	@Override
	public void removeStatus(String uniqId) {
		RenderPanel panel = statusMap.remove(uniqId);
		if (panel != null) {
			getSortedPostListPanel().remove(panel);
		}
	}

	/**
	 * EDTで動かす
	 *
	 * @param runnable ジョブ
	 */
	protected void runInDispatcherThread(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			EventQueue.invokeLater(runnable);
		}
	}

	/**
	 * 選択しているポストへスクロールする
	 */
	protected void scrollToSelectingPost() {
		if (selectingPost == null) {
			return;
		}
		JViewport viewport = getScrollPane().getViewport();
		Rectangle viewRect = viewport.getViewRect();
		Rectangle compRectLocal = selectingPost.getBounds();
		Rectangle compRect = SwingUtilities.convertRectangle(selectingPost.getParent(),
				compRectLocal, sortedPostListPanel);
		if (viewRect.y > compRect.y) {
			viewRect.y = compRect.y;
		} else if (viewRect.y + viewRect.height < compRect.y + compRect.height) {
			// viewRect.y + viewRect.height = compRect.y + compRect.height
			viewRect.y = compRect.y + compRect.height - viewRect.height;
		}
		viewport.setViewPosition(new Point(viewRect.x, viewRect.y));
		viewport.validate();
	}

	@Override
	public void serialize() {
		String propertyPrefix = getPropertyPrefix();
		configProperties.put(propertyPrefix + ".accountId", accountId);
		configProperties.put(propertyPrefix + ".uniqId", uniqId);
		configProperties.put(propertyPrefix + ".tabId", getTabId());
		if (title != null) {
			configProperties.put(propertyPrefix + ".title", title);
		}
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * update title, icon
	 */
	protected void updateTab() {
		runInDispatcherThread(new Runnable() {
			@Override
			public void run() {
				getTitleComponent().setText(getTitle());
				getTitleComponent().setIcon(getIcon());
			}
		});
	}
}
