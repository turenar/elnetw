package jp.syuriken.snsw.twclient.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientMessageAdapter;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.filter.TeeFilter;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.gui.render.RenderPanel;
import jp.syuriken.snsw.twclient.gui.render.RenderTarget;
import jp.syuriken.snsw.twclient.gui.render.RendererManager;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import jp.syuriken.snsw.twclient.internal.ScrollUtility;
import jp.syuriken.snsw.twclient.internal.ScrollUtility.BoundsTranslator;
import jp.syuriken.snsw.twclient.internal.SortedPostListPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * ツイート表示用のタブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class DefaultClientTab implements ClientTab, RenderTarget {
	/**
	 * 標準レンダラに移譲するレンダラ。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public abstract class DelegateRenderer extends ClientMessageAdapter implements TabRenderer {
		/**
		 * この時間ぐらい情報を置いておけばいいんじゃないですか的な秒数を取得する
		 *
		 * @return ミリ秒
		 */
		protected int getInfoSurviveTime() {
			return frameApi.getInfoSurviveTime();
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
					}
					break;
				case REQUEST_FOCUS_PREV_COMPONENT:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						getSortedPostListPanel().requestFocusPreviousOf(selectingPost);
					}
					break;
				case REQUEST_FOCUS_USER_PREV_COMPONENT:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						ArrayList<RenderPanel> arrayList = listItems.get(
								selectingPost.getRenderObject().getCreatedBy());
						int indexOf = arrayList.indexOf(selectingPost);
						if (indexOf >= 0 && indexOf < arrayList.size() - 1) {
							arrayList.get(indexOf + 1).requestFocusInWindow();
						}
					}
					break;
				case REQUEST_FOCUS_USER_NEXT_COMPONENT:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						ArrayList<RenderPanel> arrayList = listItems.get(
								selectingPost.getRenderObject().getCreatedBy());
						int indexOf = arrayList.indexOf(selectingPost);
						if (indexOf > 0) {
							arrayList.get(indexOf - 1).requestFocusInWindow();
						}
					}
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
							RenderPanel renderPanel = statusMap.get(RendererManager.getStatusUniqId(tag.getId()));
							if (renderPanel != null) {
								inReplyToStack.push(selectingPost);
								renderPanel.requestFocusInWindow();
							}
						}
					}
					break;
				case REQUEST_FOCUS_BACK_REPLIED_BY:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						if (!inReplyToStack.isEmpty()) {
							inReplyToStack.pop().requestFocusInWindow();
						}
					}
					break;
				case REQUEST_COPY:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						renderObject.requestCopyToClipboard();
					}
					break;
				case REQUEST_COPY_URL:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							status = status.isRetweet() ? status.getRetweetedStatus() : status;
							String url =
									"http://twitter.com/" + status.getUser().getScreenName() + "/status/" +
											status.getId();
						/* TODO: StringSelection is not copied into gnome-terminal */
							StringSelection stringSelection = new StringSelection(url);
							clipboard.setContents(stringSelection, stringSelection);
						}
					}
					break;
				case REQUEST_COPY_USERID:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
					/* TODO: StringSelection is not copied into gnome-terminal */
							String screenName = ((Status) renderObject.getBasedObject()).getUser().getScreenName();
							StringSelection stringSelection = new StringSelection(screenName);
							clipboard.setContents(stringSelection, stringSelection);
						}
					}
					break;
				case REQUEST_BROWSER_USER_HOME:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							status = status.isRetweet() ? status.getRetweetedStatus() : status;
							String url = "http://twitter.com/" + status.getUser().getScreenName();
							utility.openBrowser(url);
						}
					}
					break;
				case REQUEST_BROWSER_STATUS:
				case REQUEST_BROWSER_PERMALINK:
				case EVENT_CLICKED_CREATED_AT:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							status = status.isRetweet() ? status.getRetweetedStatus() : status;
							String url =
									"http://twitter.com/" + status.getUser().getScreenName() + "/status/" +
											status.getId();
							utility.openBrowser(url);
						}
					}
					break;
				case REQUEST_BROWSER_IN_REPLY_TO:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							if (status.getInReplyToStatusId() != -1) {
								String url =
										"http://twitter.com/" + status.getInReplyToScreenName() + "/status/"
												+ status.getInReplyToStatusId();
								utility.openBrowser(url);
							}
						}
					}
					break;
				case REQUEST_BROWSER_OPENURLS:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							URLEntity[] urlEntities = status.getURLEntities();
							for (URLEntity urlEntity : urlEntities) {
								utility.openBrowser(urlEntity.getURL());
							}
						}
					}
					break;
				case EVENT_CLICKED_CREATED_BY:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							if (status.isRetweet()) {
								status = status.getRetweetedStatus();
							}
							handleAction(new IntentArguments("userinfo").putExtra("user", status.getUser()));
						}
					}
					break;
				case EVENT_CLICKED_OVERLAY_LABEL:
					if (selectingPost != null) {
						RenderObject renderObject = selectingPost.getRenderObject();
						if (renderObject.getBasedObject() instanceof Status) {
							Status status = (Status) renderObject.getBasedObject();
							if (status.isRetweet()) {
								handleAction(new IntentArguments("userinfo").putExtra("user", status.getUser()));
							}
						}
					}
					break;
				default:
					if (selectingPost != null) {
						selectingPost.onEvent(name, arg);
					}
			}
		}

		@Override
		public void onDisplayRequirement() {
			actualRenderer.onDisplayRequirement();
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
			final DefaultClientTab this$dct = DefaultClientTab.this; // Suppress Warning of FindBugs
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					LinkedList<RenderPanel> postListAddQueue = this$dct.postListAddQueue;
					SortedPostListPanel sortedPostListPanel = this$dct.getSortedPostListPanel();
					JScrollPane postListScrollPane = this$dct.postListScrollPane;
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

	/**
	 * RenderPanelのポップアップメニューリスナ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class TweetPopupMenuListener implements PopupMenuListener, ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String actionName = e.getActionCommand();
			IntentArguments intentArguments = getIntentArguments(actionName);
			configuration.handleAction(intentArguments);
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent arg0) {
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			if (selectingPost == null) {
				getSortedPostListPanel().requestFocusFirstComponent();
			}
			JPopupMenu popupMenu = (JPopupMenu) arg0.getSource();
			Component[] components = popupMenu.getComponents();
			for (Component component : components) {
				JMenuItem menuItem = (JMenuItem) component;
				RenderObject renderObject = selectingPost.getRenderObject();
				if (renderObject == null) {
					menuItem.setEnabled(false);
				} else {
					IntentArguments intentArguments = getIntentArguments(menuItem.getActionCommand());
					ActionHandler actionHandler = configuration.getActionHandler(intentArguments);
					if (actionHandler != null) {
						actionHandler.popupMenuWillBecomeVisible(menuItem, intentArguments);
					} else {
						logger.warn("ActionHandler is not found: {}", menuItem.getActionCommand());
						menuItem.setEnabled(false);
					}
				}
			}
		}
	}

	/** クリップボード */
	protected static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	/** uniqIdの衝突防止のために使用される乱数ジェネレーター。 */
	protected static final Random random = new Random();
	/** {@link jp.syuriken.snsw.twclient.ClientConfiguration#getFrameApi()} */
	protected final ClientFrameApi frameApi;
	/** SortedPostListPanelインスタンス */
	protected final SortedPostListPanel sortedPostListPanel;
	/** 設定 */
	protected final ClientConfiguration configuration;
	/** {@link ClientConfiguration#getImageCacher()} */
	protected final ImageCacher imageCacher;
	/*package*/final Logger logger = LoggerFactory.getLogger(DefaultClientTab.class);
	/** {@link jp.syuriken.snsw.twclient.ClientProperties} */
	protected final ClientProperties configProperties;
	/**
	 * 他のタブと区別するためのユニークなID。
	 * これはフィルタの保存や {@link #getSerializedData()} の保存などに使用されます。
	 */
	protected final String uniqId;
	protected final String accountId;
	/** デフォルトフォント */
	public final Font DEFAULT_FONT;
	/** UIフォント */
	public final Font UI_FONT;
	/** UI更新キュー */
	protected final LinkedList<RenderPanel> postListAddQueue = new LinkedList<>();
	/** inReplyTo呼び出しのスタック */
	protected Stack<RenderPanel> inReplyToStack = new Stack<>();
	/** 現在選択しているポスト */
	public RenderPanel selectingPost;
	/** 取得したフォントメトリックス (Default Font) */
	protected FontMetrics fontMetrics;
	/** フォントの高さ */
	protected int fontHeight;
	/** 送信元ラベルのサイズ */
	protected Dimension linePanelSizeOfSentBy;
	/** アイコンを表示するときのサイズ */
	protected Dimension iconSize;
	/** [K=ユーザーID, V=ユーザーのツイートなど] */
	protected TreeMap<String, ArrayList<RenderPanel>> listItems = new TreeMap<>();
	/** [K=ステータスID, V=ツイートなど] */
	protected TreeMap<String, RenderPanel> statusMap = new TreeMap<>();
	/** スクロールペーン */
	protected JScrollPane postListScrollPane;
	/** 慣性スクローラー */
	protected ScrollUtility scroller;
	/** {@link ClientConfiguration#getUtility()} */
	protected Utility utility;
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
	protected TweetPopupMenuListener tweetPopupMenuListener;
	protected TabRenderer actualRenderer;
	protected boolean shouldBeScrollToPost;

	/** インスタンスを生成する。 */
	protected DefaultClientTab() {
		this.configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		imageCacher = configuration.getImageCacher();
		frameApi = configuration.getFrameApi();
		utility = configuration.getUtility();
		sortedPostListPanel = new SortedPostListPanel();
		accountId = "$reader";
		uniqId = getTabId() + "_" + Integer.toHexString(random.nextInt());
		UI_FONT = configProperties.getFont("gui.font.ui");
		DEFAULT_FONT = configProperties.getFont("gui.font.default");
		init(configuration);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param serializedJson シリアル化されたJSON
	 * @throws JSONException JSONの形式が正しくないか必要とするキーが存在しない
	 */
	protected DefaultClientTab(JSONObject serializedJson) throws JSONException {
		this.configuration = ClientConfiguration.getInstance();
		configProperties = configuration.getConfigProperties();
		imageCacher = configuration.getImageCacher();
		frameApi = configuration.getFrameApi();
		utility = configuration.getUtility();
		sortedPostListPanel = new SortedPostListPanel();
		String accountId = serializedJson.getString("accountId");
		if (accountId == null) {
			accountId = "$reader";
		}
		this.accountId = accountId;
		uniqId = serializedJson.getString("uniqId");
		UI_FONT = configProperties.getFont("gui.font.ui");
		DEFAULT_FONT = configProperties.getFont("gui.font.default");
		init(configuration);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param serializedJson シリアル化されたJSON
	 * @throws JSONException JSONの復号化中に例外
	 */
	protected DefaultClientTab(String serializedJson) throws JSONException {
		this(new JSONObject(serializedJson));
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
		}
		synchronized (postListAddQueue) {
			RenderPanel component = renderObject.getComponent();
			postListAddQueue.add(component);
		}
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
		if (selectingPost != null) {
			selectingPost.requestFocusInWindow();
		}
	}

	/**
	 * 実際に描画するレンダラ。
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

	public abstract DelegateRenderer getDelegateRenderer();

	/**
	 * Create IntentArguments
	 *
	 * @param actionCommand (name)[!(key)[=(value)][, ...]]
	 * @return IntentArguments
	 */
	protected IntentArguments getIntentArguments(String actionCommand) {
		IntentArguments intentArguments = Utility.getIntentArguments(actionCommand);

		if (selectingPost != null) {
			intentArguments.putExtra(ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA,
					selectingPost.getRenderObject());
		}
		return intentArguments;
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
		}
		return postListScrollPane;
	}

	/** このクラスではJSONが返されます。 */
	@Override
	public String getSerializedData() {
		try {
			return new JSONObject().put("accountId", accountId).put("uniqId", getUniqId()).put("tabId", getTabId())
					.put("extended", getSerializedExtendedData()).toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 次回タブ復元時に必要なデータを取得する。
	 *
	 * <p>
	 * この関数によって返されたデータは、次回のタブ復元によるインスタンス作成時に
	 * JSONで、&quot;extended&quot;キーに格納されます。
	 * {@link JSONObject} や {@link JSONArray} なども使用出来ます。特に保存するデータがないときは
	 * {@link JSONObject#NULL} を使用してください。
	 * </p>
	 * <p>
	 * JSON例外を返すことができます。その他の例外は推奨されませんが {@link RuntimeException} などに
	 * ラップしてください。
	 * </p>
	 *
	 * @return 次回タブ復元時に必要なデータ
	 * @throws JSONException JSON例外
	 */
	protected abstract Object getSerializedExtendedData() throws JSONException;

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
		RenderPanel RenderPanel = statusMap.get(RendererManager.getStatusUniqId(statusId));
		return RenderPanel == null ? null : RenderPanel.getRenderObject();
	}

	@Override
	public JComponent getTabComponent() {
		return getScrollPane();
	}

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
		args.putExtra(ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA,
				selectingPost == null ? null : selectingPost.getRenderObject());
		configuration.handleAction(args);
	}

	private void init(ClientConfiguration configuration) {
		fontMetrics = getSortedPostListPanel().getFontMetrics(frameApi.getDefaultFont());
		int str12width = fontMetrics.stringWidth("0123456789abc");
		fontHeight = fontMetrics.getHeight();
		int height = Math.max(18, fontHeight);
		linePanelSizeOfSentBy = new Dimension(str12width, height);
		iconSize = new Dimension(64, height);
		configuration.getTimer().scheduleWithFixedDelay(new PostListUpdater(),
				configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_POSTLIST_UPDATE),
				configProperties.getInteger(ClientConfiguration.PROPERTY_INTERVAL_POSTLIST_UPDATE),
				TimeUnit.MILLISECONDS);
		tweetPopupMenuListener = new TweetPopupMenuListener();
		scroller = new ScrollUtility(getScrollPane(), new BoundsTranslator() {

			@Override
			public Rectangle translate(JComponent component) {
				if (!(component instanceof RenderPanel)) {
					throw new AssertionError();
				}
				return sortedPostListPanel.getBoundsOf((RenderPanel) component);
			}
		}, configuration.getConfigProperties().getBoolean("gui.scrool.momentumEnabled"));
		actualRenderer = RendererManager.get(accountId, this, tweetPopupMenuListener);
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
	public void removeStatus(RenderObject renderObject) {
		RenderPanel panel = statusMap.get(renderObject.getUniqId());
		if (panel != null) {
			getSortedPostListPanel().remove(panel);
		}
	}

	/**
	 * ステータスを削除する
	 *
	 * @param renderObject ステータスデータ
	 * @param delay        遅延ミリ秒
	 */
	public void removeStatus(final RenderObject renderObject, int delay) {
		configuration.getTimer().schedule(new Runnable() {

			@Override
			public void run() {
				removeStatus(renderObject);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	protected void runInDispatcherThread(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			EventQueue.invokeLater(runnable);
		}
	}

	protected void scrollToSelectingPost() {
		if (selectingPost == null) {
			return;
		}
		JViewport viewport = getScrollPane().getViewport();
		Rectangle viewRect = viewport.getViewRect();
		Rectangle compRectLocal = selectingPost.getBounds();
		Rectangle compRect = SwingUtilities.convertRectangle(selectingPost, compRectLocal, sortedPostListPanel);
		if (viewRect.y < compRect.y) {
			viewRect.y = compRect.y;
		} else if (viewRect.y + viewRect.height > compRect.y + compRect.height) {
			// viewRect.y + viewRect.height = compRect.y + compRect.height
			viewRect.y = compRect.y + compRect.height - viewRect.height;
		}
		viewport.setViewPosition(new Point(viewRect.x, viewRect.y));
		viewport.validate();
	}
}
