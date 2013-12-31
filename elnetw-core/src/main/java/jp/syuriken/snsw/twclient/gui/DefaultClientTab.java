package jp.syuriken.snsw.twclient.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientFrameApi;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.StatusData;
import jp.syuriken.snsw.twclient.StatusPanel;
import jp.syuriken.snsw.twclient.TwitterStatus;
import jp.syuriken.snsw.twclient.Utility;
import jp.syuriken.snsw.twclient.filter.TeeFilter;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import jp.syuriken.snsw.twclient.internal.ScrollUtility;
import jp.syuriken.snsw.twclient.internal.ScrollUtility.BoundsTranslator;
import jp.syuriken.snsw.twclient.internal.SortedPostListPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TweetEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;
import static jp.syuriken.snsw.twclient.ClientFrameApi.SET_FOREGROUND_COLOR_BLUE;
import static jp.syuriken.snsw.twclient.ClientFrameApi.UNDERLINE;

/**
 * ツイート表示用のタブ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class DefaultClientTab implements ClientTab {

	/** Entityの開始位置を比較する */
	private static final class EntityComparator implements Comparator<TweetEntity>, Serializable {

		private static final long serialVersionUID = 8166780199866981253L;

		@Override
		public int compare(TweetEntity o1, TweetEntity o2) {
			return o1.getStart() - o2.getStart();
		}
	}

	/**
	 * レンダラ。このクラスをextendすることによりリスト移動やステータスの受信はできるようになるかも。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public abstract class DefaultRenderer implements TabRenderer {

		/**
		 * この時間ぐらい情報を置いておけばいいんじゃないですか的な秒数を取得する
		 *
		 * @return ミリ秒
		 */
		protected int getInfoSurviveTime() {
			return frameApi.getInfoSurviveTime();
		}

		@Override
		public void onBlock(User source, User blockedUser) {
		}

		@Override
		public void onChangeAccount(boolean forWrite) {
		}

		@Override
		public void onCleanUp() {
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
						ArrayList<StatusPanel> arrayList = listItems.get(selectingPost.getStatusData().user);
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
						ArrayList<StatusPanel> arrayList = listItems.get(selectingPost.getStatusData().user);
						int indexOf = arrayList.indexOf(selectingPost);
						if (indexOf > 0) {
							arrayList.get(indexOf - 1).requestFocusInWindow();
						}
					}
					break;
				case REQUEST_FOCUS_FIRST_COMPONENT:
					getSortedPostListPanel().requestFocusFirstComponent();
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
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status tag = (Status) statusData.tag;
							inReplyToStack.push(selectingPost);
							StatusPanel statusPanel = statusMap.get(tag.getInReplyToStatusId());
							if (statusPanel != null) {
								statusPanel.requestFocusInWindow();
							}
						}
					}
					break;
				case REQUEST_FOCUS_BACK_REPLIED_BY:
					if (selectingPost == null) {
						getSortedPostListPanel().requestFocusInWindow();
					} else {
						if (inReplyToStack.isEmpty() == false) {
							inReplyToStack.pop().requestFocusInWindow();
						}
					}
					break;
				case REQUEST_COPY:
					if (selectingPost != null) {
						StatusData statusData = selectingPost.getStatusData();
						/* TODO: StringSelection is not copied into gnome-terminal */
						StringSelection stringSelection = new StringSelection(statusData.data.getText());
						clipboard.setContents(stringSelection, stringSelection);
					}
					break;
				case REQUEST_COPY_URL:
					if (selectingPost != null) {
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
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
						StatusData statusData = selectingPost.getStatusData();
					/* TODO: StringSelection is not copied into gnome-terminal */
						StringSelection stringSelection = new StringSelection(statusData.user);
						clipboard.setContents(stringSelection, stringSelection);
					}
					break;
				case REQUEST_BROWSER_USER_HOME:
					if (selectingPost != null) {
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
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
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
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
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
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
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
							URLEntity[] urlEntities = status.getURLEntities();
							for (URLEntity urlEntity : urlEntities) {
								utility.openBrowser(urlEntity.getURL());
							}
						}
					}
					break;
				case EVENT_CLICKED_CREATED_BY:
					if (selectingPost != null) {
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
							if (status.isRetweet()) {
								status = status.getRetweetedStatus();
							}
							handleAction(new IntentArguments("userinfo").putExtra("user", status.getUser()));
						}
					}
					break;
				case EVENT_CLICKED_OVERLAY_LABEL:
					if (selectingPost != null) {
						StatusData statusData = selectingPost.getStatusData();
						if (statusData.tag instanceof Status) {
							Status status = (Status) statusData.tag;
							if (status.isRetweet()) {
								handleAction(new IntentArguments("userinfo").putExtra("user", status.getUser()));
							}
						}
					}
					break;
				default:
					// do nothing
			}
		}

		@Override
		public void onConnect() {
		}

		@Override
		public void onDeletionNotice(long directMessageId, long userId) {
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {
		}

		@Override
		public void onDisconnect() {
		}

		@Override
		public void onException(Exception ex) {
		}

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
		}

		@Override
		public void onFollow(User source, User followedUser) {
		}

		@Override
		public void onFriendList(long[] friendIds) {
		}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {
		}

		@Override
		public void onStallWarning(StallWarning warning) {
		}

		@Override
		public void onStatus(Status originalStatus) {
			addStatus(originalStatus);
		}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		}

		@Override
		public void onUnblock(User source, User unblockedUser) {
		}

		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		}

		@Override
		public void onUserListCreation(User listOwner, UserList list) {
		}

		@Override
		public void onUserListDeletion(User listOwner, UserList list) {
		}

		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
		}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
		}

		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
		}

		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
		}

		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
		}

		@Override
		public void onUserProfileUpdate(User updatedUser) {
		}
	}

	/**
	 * ポストリストリスナ。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class PostListListener implements MouseListener, FocusListener, KeyListener {

		@Override
		public void focusGained(FocusEvent e) {
			// should scroll? if focus-window changed, i skip scrolling
			boolean scroll = (e.getOppositeComponent() == null && selectingPost != null);
			focusGainOfLinePanel(e);
			if (!scroll) {
				scroller.scrollTo(selectingPost);
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			logger.trace("{}", e);
			frameApi.handleShortcutKey("list", e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			frameApi.handleShortcutKey("list", e);
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().requestFocusInWindow();
			/* if (e.isPopupTrigger()) {
				selectingPost = (StatusPanel) e.getComponent();
			} */
			if (e.getClickCount() == 2) {
				handleAction(new IntentArguments("reply"));
			}
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
					LinkedList<StatusPanel> postListAddQueue = this$dct.postListAddQueue;
					synchronized (postListAddQueue) {
						int size = postListAddQueue.size();
						JScrollPane postListScrollPane = this$dct.postListScrollPane;

						postListScrollPane.invalidate();
						Point viewPosition = postListScrollPane.getViewport().getViewPosition();
						if (viewPosition.y < this$dct.fontHeight) {
							postListScrollPane.getViewport().setViewPosition(new Point(viewPosition.x, 0));
						} else {
							postListScrollPane.getViewport().setViewPosition(
									new Point(viewPosition.x, viewPosition.y
											+ (this$dct.iconSize.height + PADDING_OF_POSTLIST) * size));
						}
						this$dct.getSortedPostListPanel().add(postListAddQueue);
						postListScrollPane.validate();
					}
				}
			});
		}
	}

	/**
	 * StatusPanelのポップアップメニューリスナ
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
				StatusData statusData = selectingPost.getStatusData();
				if (statusData == null) {
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
	/** ポストリストの間のパディング */
	/*package*/static final int PADDING_OF_POSTLIST = 1;
	/** ふぁぼの星 (ふぁぼされていない時用) 32x32 */
	public static final ImageIcon IMG_FAV_OFF;
	/** ふぁぼの星 (ふぁぼされている時用) 32x32 */
	public static final ImageIcon IMG_FAV_ON;
	/** ふぁぼの星 (フォーカスが当たっている時用) 32x32 */
	public static final ImageIcon IMG_FAV_HOVER;
	private static final Dimension OPERATION_PANEL_SIZE = new Dimension(32, 32);
	/** Twitterのロゴ (青背景に白) */
	public static final ImageIcon IMG_TWITTER_LOGO;

	static {
		try {
			IMG_FAV_OFF = new ImageIcon(
					ImageIO.read(DefaultClientTab.class.getResource("/jp/syuriken/snsw/twclient/img/fav_off32.png")));
			IMG_FAV_ON = new ImageIcon(
					ImageIO.read(DefaultClientTab.class.getResource("/jp/syuriken/snsw/twclient/img/fav_on32.png")));
			IMG_FAV_HOVER = new ImageIcon(
					ImageIO.read(DefaultClientTab.class.getResource("/jp/syuriken/snsw/twclient/img/fav_hover32.png")));
		} catch (IOException e) {
			throw new AssertionError("必要なリソース img/fav_{off,on,hover}32.png が読み込めませんでした");
		}
		try {
			IMG_TWITTER_LOGO =
					new ImageIcon(ImageIO.read(DefaultClientTab.class
							.getResource("/com/twitter/twitter-bird-white-on-blue.png")));
		} catch (IOException e) {
			throw new AssertionError("必要なリソース Twitterのロゴ が読み込めませんでした");
		}
	}

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

	/** {@link jp.syuriken.snsw.twclient.ClientConfiguration#getFrameApi()} */
	protected final ClientFrameApi frameApi;
	/** SortedPostListPanelインスタンス */
	protected final SortedPostListPanel sortedPostListPanel;
	/** 設定 */
	protected final ClientConfiguration configuration;
	/** {@link ClientConfiguration#getImageCacher()} */
	protected final ImageCacher imageCacher;
	/** ポストリストリスナのシングルインスタンス */
	protected final PostListListener postListListenerSingleton = new PostListListener();
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
	/** inReplyTo呼び出しのスタック */
	protected Stack<StatusPanel> inReplyToStack = new Stack<StatusPanel>();
	/** 現在選択しているポスト */
	public StatusPanel selectingPost;
	/** 取得したフォントメトリックス (Default Font) */
	protected FontMetrics fontMetrics;
	/** フォントの高さ */
	protected int fontHeight;
	/** 送信元ラベルのサイズ */
	protected Dimension linePanelSizeOfSentBy;
	/** アイコンを表示するときのサイズ */
	protected Dimension iconSize;
	/** [K=ユーザーID, V=ユーザーのツイートなど] */
	protected TreeMap<String, ArrayList<StatusPanel>> listItems = new TreeMap<>();
	/** [K=ステータスID, V=ツイートなど] */
	protected TreeMap<Long, StatusPanel> statusMap = new TreeMap<>();
	/** スクロールペーン */
	protected JScrollPane postListScrollPane;
	/** UI更新キュー */
	protected LinkedList<StatusPanel> postListAddQueue = new LinkedList<>();
	/** ポップアップメニュー */
	protected JPopupMenu tweetPopupMenu;
	/** 慣性スクローラー */
	protected ScrollUtility scroller;
	/** {@link ClientConfiguration#getUtility()} */
	protected Utility utility;
	/** 操作用パネル */
	protected JPanel tweetViewOperationPanel;
	/** ふぁぼボタン */
	protected JLabel tweetViewFavoriteButton;
	/** リツイートボタン */
	protected JLabel tweetViewRetweetButton;
	/** リプライボタン */
	protected JLabel tweetViewReplyButton;
	/** その他用ボタン */
	protected JLabel tweetViewOtherButton;
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

	/**
	 * リストにステータスを追加する。
	 *
	 * @param originalStatus 元となるStatus
	 */
	public void addStatus(Status originalStatus) {
		Status twitterStatus = originalStatus instanceof TwitterStatus ? originalStatus
				: new TwitterStatus(originalStatus);
		StatusData statusData = new StatusData(twitterStatus, twitterStatus.getCreatedAt(), twitterStatus.getId());

		Status status;
		if (twitterStatus.isRetweet()) {
			status = twitterStatus.getRetweetedStatus();
		} else {
			status = twitterStatus;
		}
		User user = status.getUser();

		if (configProperties.getBoolean(ClientConfiguration.PROPERTY_ID_STRICT_MATCH)) {
			if (user.getId() == frameApi.getLoginUser().getId()) {
				statusData.foregroundColor = Color.BLUE;
			}
		} else {
			if (user.getScreenName().startsWith(frameApi.getLoginUser().getScreenName())) {
				statusData.foregroundColor = Color.BLUE;
			}
		}

		JLabel icon = new JLabel();
		imageCacher.setImageIcon(icon, status.getUser());
		icon.setHorizontalAlignment(JLabel.CENTER);
		statusData.image = icon;

		String screenName = user.getScreenName();
		statusData.user = screenName;
		if (screenName.length() > 11) {
			screenName = screenName.substring(0, 9) + "..";
		}
		JLabel sentBy = new JLabel(screenName);
		sentBy.setFont(DEFAULT_FONT);
		statusData.sentBy = sentBy;

		JLabel statusText = new JLabel(status.getText());
		statusData.data = statusText;

		statusData.popupMenu = tweetPopupMenu;

		if (twitterStatus.isRetweet()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Retweeted by @").append(twitterStatus.getUser().getScreenName());
			statusData.tooltip = stringBuilder.toString();
		}

		if (twitterStatus.isRetweet()) {
			statusData.foregroundColor = Color.GREEN;
		} else {
			UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
			if (isMentioned(userMentionEntities)) {
				statusData.foregroundColor = Color.RED;
			}
		}

		addStatus(statusData);
	}

	/**
	 * リストにステータスを追加する。
	 *
	 * @param statusData StatusDataインスタンス。
	 * @return 追加された StatusPanel
	 */
	public StatusPanel addStatus(StatusData statusData) {
		final StatusPanel linePanel = new StatusPanel(statusData);
		BoxLayout layout = new BoxLayout(linePanel, BoxLayout.X_AXIS);
		linePanel.setLayout(layout);
		linePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		statusData.image.setInheritsPopupMenu(true);
		statusData.image.setFocusable(false);
		statusData.image.setMinimumSize(iconSize);
		statusData.image.setMaximumSize(iconSize);
		linePanel.add(statusData.image);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.sentBy.setInheritsPopupMenu(true);
		statusData.sentBy.setFocusable(false);
		statusData.sentBy.setMinimumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setMaximumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setFont(frameApi.getDefaultFont());
		linePanel.add(statusData.sentBy);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.data.setInheritsPopupMenu(true);
		statusData.data.setFocusable(false);
		statusData.data.setFont(frameApi.getDefaultFont());
		int dataWidth = fontMetrics.stringWidth(statusData.data.getText());

		linePanel.add(statusData.data);
		linePanel.setComponentPopupMenu(statusData.popupMenu);
		/* if (information.isSystemNotify()) {
			information.backgroundColor = Color.BLACK;
		} */
		linePanel.setForeground(statusData.foregroundColor);
		linePanel.setBackground(statusData.backgroundColor);
		int height = iconSize.height + PADDING_OF_POSTLIST;
		Dimension minSize = new Dimension(iconSize.width + linePanelSizeOfSentBy.width + dataWidth + 3 * 2, height);
		linePanel.setMinimumSize(minSize);
		linePanel.setPreferredSize(minSize);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addMouseListener(postListListenerSingleton);
		linePanel.addFocusListener(postListListenerSingleton);
		linePanel.addKeyListener(postListListenerSingleton);
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);

		synchronized (listItems) {
			statusMap.put(statusData.id, linePanel);
			ArrayList<StatusPanel> list = listItems.get(statusData.user);
			if (list == null) {
				list = new ArrayList<StatusPanel>();
				listItems.put(statusData.user, list);
			}
			list.add(linePanel);
		}
		synchronized (postListAddQueue) {
			postListAddQueue.add(linePanel);
		}
		return linePanel;
	}

	/**
	 * リストにステータスを追加する。その後deltionDelayミリ秒後に該当するステータスを削除する。
	 *
	 * @param statusData    StatusDataインスタンス。
	 * @param deletionDelay 削除を予約する時間。ミリ秒
	 * @return 追加された (もしくはそのあと削除された) ステータス。
	 */
	public JPanel addStatus(StatusData statusData, int deletionDelay) {
		final StatusPanel status = addStatus(statusData);
		removeStatus(statusData, deletionDelay);
		return status;
	}

	/**
	 * ポストパネルがフォーカスを得た時のハンドラ
	 *
	 * @param e Focusイベント
	 * @throws IllegalArgumentException 正しくないプロパティ
	 * @throws NumberFormatException    数値ではないプロパティ
	 */
	protected void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException, NumberFormatException {
		if (selectingPost != null) {
			selectingPost.setBackground(selectingPost.getStatusData().backgroundColor);
		}
		selectingPost = (StatusPanel) e.getComponent();
		selectingPost.setBackground(Utility.blendColor(selectingPost.getStatusData().backgroundColor,
				configProperties.getColor(ClientConfiguration.PROPERTY_COLOR_FOCUS_LIST)));

		StatusData statusData = selectingPost.getStatusData();
		if (statusData.tag instanceof TwitterStatus) {
			TwitterStatus originalStatus = (TwitterStatus) statusData.tag;
			TwitterStatus status = originalStatus.isRetweet() ? originalStatus.getRetweetedStatus() : originalStatus;
			String text = status.getText();
			StringBuilder stringBuilder = new StringBuilder(text.length() * 2);

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
			int offset = 0;
			for (Object entity : entities) {
				int start;
				int end;
				String replaceText;
				String url;
				if (entity instanceof HashtagEntity) {
					HashtagEntity hashtagEntity = (HashtagEntity) entity;
					start = hashtagEntity.getStart();
					end = hashtagEntity.getEnd();
					replaceText = null;
					url = "http://command/hashtag!name=" + hashtagEntity.getText();
				} else if (entity instanceof URLEntity) {
					URLEntity urlEntity = (URLEntity) entity;
					if (urlEntity instanceof MediaEntity) {
						MediaEntity mediaEntity = (MediaEntity) urlEntity;
						url = "http://command/openimg!url=" + mediaEntity.getMediaURL();
					} else {
						url = urlEntity.getURL();
					}
					start = urlEntity.getStart();
					end = urlEntity.getEnd();
					replaceText = urlEntity.getDisplayURL();
				} else if (entity instanceof UserMentionEntity) {
					UserMentionEntity mentionEntity = (UserMentionEntity) entity;
					start = mentionEntity.getStart();
					end = mentionEntity.getEnd();
					replaceText = null;
					url = "http://command/userinfo!screenName=" + mentionEntity.getScreenName();
				} else {
					throw new AssertionError();
				}

				String insertText =
						new StringBuilder().append("<a href=\"").append(url).append("\">")
								.append(escapeHTML(replaceText == null ? text.substring(start, end) : replaceText))
								.append("</a>").toString();
				stringBuilder.append(escapeHTML(text.substring(offset, start)));
				stringBuilder.append(insertText);
				offset = end;
			}
			escapeHTML(text.substring(offset), stringBuilder);
			String tweetText = stringBuilder.toString();
			String createdBy;
			createdBy =
					MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status.getUser().getName());
			String source = status.getSource();
			int tagIndexOf = source.indexOf('>');
			int tagLastIndexOf = source.lastIndexOf('<');
			String createdAtToolTip =
					MessageFormat.format("from {0}",
							source.substring(tagIndexOf + 1, tagLastIndexOf == -1 ? source.length() : tagLastIndexOf));
			String createdAt = Utility.getDateString(status.getCreatedAt(), true);
			String overlayString;
			if (originalStatus.isRetweet()) {
				overlayString =
						"<html><span style='color:#33cc33;'>Retweeted by @" + originalStatus.getUser().getScreenName()
								+ " (" + originalStatus.getUser().getName() + ")</span>";
			} else {
				overlayString = null;
			}
			if (status.isFavorited()) {
				getTweetViewFavoriteButton().setIcon(IMG_FAV_ON);
			} else {
				getTweetViewFavoriteButton().setIcon(IMG_FAV_OFF);
			}
			Icon userProfileIcon = ((JLabel) statusData.image).getIcon();
			frameApi.clearTweetView();
			frameApi.setTweetViewCreatedAt(createdAt, createdAtToolTip, SET_FOREGROUND_COLOR_BLUE | UNDERLINE);
			frameApi.setTweetViewCreatedBy(userProfileIcon, createdBy, null, SET_FOREGROUND_COLOR_BLUE | UNDERLINE);
			frameApi.setTweetViewText(tweetText, overlayString, UNDERLINE);
			frameApi.setTweetViewOperationPanel(getTweetViewOperationPanel());
		} else if (statusData.tag instanceof Exception) {
			Exception ex = (Exception) statusData.tag;
			Throwable handlingException = ex;
			StringBuilder stringBuilder = new StringBuilder().append(ex.getLocalizedMessage()).append("<br><br>");
			while (null != (handlingException = handlingException.getCause())) {
				stringBuilder.append("Caused by ").append(handlingException.toString()).append("<br>");
			}
			StringBuilder escaped = escapeHTML(stringBuilder);
			frameApi.clearTweetView();
			frameApi.setTweetViewText(escaped.toString(), null, DO_NOTHING_WHEN_POINTED);
			frameApi.setTweetViewCreatedAt(Utility.getDateFormat().format(statusData.date), null,
					DO_NOTHING_WHEN_POINTED);
			frameApi.setTweetViewCreatedBy(((JLabel) statusData.image).getIcon(), ex.getClass().getName(), null,
					DO_NOTHING_WHEN_POINTED);
		} else {
			frameApi.clearTweetView();
			frameApi.setTweetViewText(statusData.data.getText(), null, DO_NOTHING_WHEN_POINTED);
			frameApi.setTweetViewCreatedAt(Utility.getDateFormat().format(statusData.date), null,
					DO_NOTHING_WHEN_POINTED);
			frameApi.setTweetViewCreatedBy(((JLabel) statusData.image).getIcon(), statusData.user, null,
					DO_NOTHING_WHEN_POINTED);
		}
	}

	@Override
	public void focusGained() {
		if (selectingPost != null) {
			selectingPost.requestFocusInWindow();
		}
	}

	protected JPopupMenu generatePopupMenu(ActionListener actionListener) {
		JPopupMenu popupMenu = new JPopupMenu();
		Container nowProcessingMenu = popupMenu;
		String[] popupMenus = configProperties.getProperty("gui.menu.popup").split(" ");

		for (String actionCommand : popupMenus) {
			if (actionCommand.trim().isEmpty()) {
				continue;
			} else if (actionCommand.startsWith("<") && actionCommand.endsWith(">")) {
				JMenu jMenu = new JMenu(actionCommand.substring(1, actionCommand.length() - 1));
				jMenu.setActionCommand("core!submenu");
				nowProcessingMenu = jMenu;
				popupMenu.add(nowProcessingMenu);
				continue;
			}
			ActionHandler handler = configuration.getActionHandler(new IntentArguments(actionCommand));
			if (handler == null) {
				logger.warn("handler {} is not found.", actionCommand); //TODO
			} else {
				JMenuItem menuItem = handler.createJMenuItem(new IntentArguments(actionCommand));
				menuItem.setActionCommand(actionCommand);
				menuItem.addActionListener(actionListener);
				if (nowProcessingMenu instanceof JPopupMenu) {
					((JPopupMenu) nowProcessingMenu).add(menuItem);
				} else {
					((JMenu) nowProcessingMenu).add(menuItem);
				}
			}
		}
		return popupMenu;
	}

	/**
	 * 実際に描画するレンダラ。
	 *
	 * @return レンダラ
	 * @see #getRenderer()
	 */
	public abstract TabRenderer getActualRenderer();

	/**
	 * {@link #getScrollPane()}の子コンポーネント
	 *
	 * @return {@link SortedPostListPanel}インスタンス
	 */
	protected JComponent getChildComponent() {
		return getSortedPostListPanel();
	}

	/**
	 * Create IntentArguments
	 *
	 * @param actionCommand (name)[!(key)[=(value)][, ...]]
	 * @return IntentArguments
	 */
	protected IntentArguments getIntentArguments(String actionCommand) {
		IntentArguments intentArguments = Utility.getIntentArguments(actionCommand);

		if (selectingPost != null) {
			intentArguments.putExtra(ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA, selectingPost.getStatusData());
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
			teeFilter = new TeeFilter(uniqId, getActualRenderer());
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
	public StatusData getStatus(long statusId) {
		StatusPanel statusPanel = statusMap.get(statusId);
		return statusPanel == null ? null : statusPanel.getStatusData();
	}

	@Override
	public JComponent getTabComponent() {
		return getScrollPane();
	}

	/**
	 * ツイートビューの隣に表示するふぁぼボタン
	 *
	 * @return ふぁぼボタン
	 */
	protected JLabel getTweetViewFavoriteButton() {
		if (tweetViewFavoriteButton == null) {
			tweetViewFavoriteButton = new JLabel(IMG_FAV_OFF, SwingConstants.CENTER);
			tweetViewFavoriteButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewFavoriteButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewFavoriteButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewFavoriteButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewFavoriteButton.addAncestorListener(new AncestorListener() {

				@Override
				public void ancestorAdded(AncestorEvent event) {
					ancestorMoved(event);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
					if (selectingPost != null && selectingPost.getStatusData().tag instanceof Status) {
						Status status = (Status) selectingPost.getStatusData().tag;
						if (status.isFavorited()) {
							tweetViewFavoriteButton.setIcon(IMG_FAV_ON);
						} else {
							tweetViewFavoriteButton.setIcon(IMG_FAV_OFF);
						}
					}
				}

				@Override
				public void ancestorRemoved(AncestorEvent event) {
					ancestorMoved(event);
				}
			});
			tweetViewFavoriteButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					mouseExited(e);
					handleAction("fav");
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					tweetViewFavoriteButton.setIcon(IMG_FAV_HOVER);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					Status status = (Status) selectingPost.getStatusData().tag;
					if (status.isFavorited()) {
						tweetViewFavoriteButton.setIcon(IMG_FAV_ON);
					} else {
						tweetViewFavoriteButton.setIcon(IMG_FAV_OFF);
					}
				}
			});
		}
		return tweetViewFavoriteButton;
	}

	/**
	 * ツイートビューの隣に表示するボタンの集まり
	 *
	 * @return ボタンの集まり
	 */
	protected JPanel getTweetViewOperationPanel() {
		if (tweetViewOperationPanel == null) {
			tweetViewOperationPanel = new JPanel();
			tweetViewOperationPanel.setPreferredSize(new Dimension(76, 76));
			tweetViewOperationPanel.setMinimumSize(new Dimension(76, 76));
			GroupLayout layout = new GroupLayout(tweetViewOperationPanel);
			layout.setHorizontalGroup(layout
					.createParallelGroup()
					.addGroup(
							layout.createSequentialGroup().addComponent(getTweetViewReplyButton(), 32, 32, 32)
									.addComponent(getTweetViewRetweetButton(), 32, 32, 32))
					.addGroup(
							layout.createSequentialGroup().addComponent(getTweetViewFavoriteButton(), 32, 32, 32)
									.addComponent(getTweetViewOtherButton(), 32, 32, 32)));
			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addGroup(
							layout.createParallelGroup().addComponent(getTweetViewReplyButton(), 32, 32, 32)
									.addComponent(getTweetViewRetweetButton(), 32, 32, 32))
					.addGroup(
							layout.createParallelGroup().addComponent(getTweetViewFavoriteButton(), 32, 32, 32)
									.addComponent(getTweetViewOtherButton(), 32, 32, 32)));
		}
		return tweetViewOperationPanel;
	}

	/**
	 * ツイートビューの隣に表示するその他用ボタン
	 *
	 * @return その他用ボタン
	 */
	protected JLabel getTweetViewOtherButton() {
		if (tweetViewOtherButton == null) {
			tweetViewOtherButton = new JLabel("？", SwingConstants.CENTER);
			tweetViewOtherButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewOtherButton.setToolTipText("ユーザー情報を見る");
			tweetViewOtherButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewOtherButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewOtherButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewOtherButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					handleAction("userinfo");
				}
			});
		}
		return tweetViewOtherButton;
	}

	/**
	 * ツイートビューの隣に表示するリプライボタン
	 *
	 * @return リプライボタン
	 */
	protected JLabel getTweetViewReplyButton() {
		if (tweetViewReplyButton == null) {
			tweetViewReplyButton = new JLabel("Re", SwingConstants.CENTER);
			tweetViewReplyButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewReplyButton.setToolTipText("@返信を行う");
			tweetViewReplyButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewReplyButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewReplyButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewReplyButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					handleAction("reply");
				}
			});
		}
		return tweetViewReplyButton;
	}

	/**
	 * ツイートビューの隣に表示するリツイートボタン
	 *
	 * @return リツイートボタン
	 */
	protected JLabel getTweetViewRetweetButton() {
		if (tweetViewRetweetButton == null) {
			tweetViewRetweetButton = new JLabel("RT", SwingConstants.CENTER);
			tweetViewRetweetButton.setBorder(new LineBorder(Color.GRAY, 1));
			tweetViewRetweetButton.setToolTipText("公式リツイート");
			tweetViewRetweetButton.setMinimumSize(OPERATION_PANEL_SIZE);
			tweetViewRetweetButton.setMaximumSize(OPERATION_PANEL_SIZE);
			tweetViewRetweetButton.setPreferredSize(OPERATION_PANEL_SIZE);
			tweetViewRetweetButton.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					handleAction("rt");
				}
			});
		}
		return tweetViewRetweetButton;
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
				selectingPost == null ? null : selectingPost.getStatusData());
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
		TweetPopupMenuListener tweetPopupMenuListener = new TweetPopupMenuListener();
		tweetPopupMenu = generatePopupMenu(tweetPopupMenuListener);
		tweetPopupMenu.addPopupMenuListener(tweetPopupMenuListener);
		scroller = new ScrollUtility(getScrollPane(), new BoundsTranslator() {

			@Override
			public Rectangle translate(JComponent component) {
				if (component instanceof StatusPanel == false) {
					throw new AssertionError();
				}
				return sortedPostListPanel.getBoundsOf((StatusPanel) component);
			}
		}, configuration.getConfigProperties().getBoolean("gui.scroll.momentumEnabled"));
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
		// for Display Requirements
		StatusData statusData = new StatusData(null, new Date(0x7fffffffffffffffL));
		statusData.user = "!twitter";
		statusData.backgroundColor = Color.DARK_GRAY;
		statusData.foregroundColor = Color.WHITE;
		statusData.image = new JLabel(IMG_TWITTER_LOGO);
		statusData.sentBy = new JLabel();
		statusData.data = new JLabel("All data is from twitter...");
		addStatus(statusData);
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
	 * @param statusData ステータスデータ
	 */
	public void removeStatus(StatusData statusData) {
		StatusPanel panel = statusMap.get(statusData.id);
		if (panel != null) {
			getSortedPostListPanel().remove(panel);
		}
	}

	/**
	 * ステータスを削除する
	 *
	 * @param statusData ステータスデータ
	 * @param delay      遅延ミリ秒
	 */
	public void removeStatus(final StatusData statusData, int delay) {
		configuration.getTimer().schedule(new Runnable() {

			@Override
			public void run() {
				removeStatus(statusData);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}
}
