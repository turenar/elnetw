package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * twclientのメインウィンドウ
 * @author snsoftware
 */
@SuppressWarnings("serial")
public class TwitterClientFrame extends javax.swing.JFrame {
	
	/**
	 * MenuItemのActionListenerの実装。
	 * 
	 * @author $Author$
	 */
	private final class ActionListenerImplementation implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			StatusData statusData;
			if (selectingPost == null) {
				statusData = null;
			} else {
				statusData = statusMap.get(selectingPost.getStatusData().id);
			}
			handleAction(e.getActionCommand(), statusData);
		}
	}
	
	/**
	 * ふぁぼる
	 * 
	 * @author $Author$
	 */
	public class FavoriteActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenuItem favMenuItem = new JMenuItem("ふぁぼる(F)", KeyEvent.VK_F);
			return favMenuItem;
		}
		
		@Override
		public void handleAction(String actionName, final StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				addJob(new Runnable() {
					
					@Override
					public void run() {
						boolean unfavorite = false;
						Status status = (Status) statusData.tag;
						if (status instanceof TwitterStatus && ((TwitterStatus) status).isFavorited()) {
							unfavorite = true;
						}
						try {
							if (unfavorite) {
								twitter.destroyFavorite(status.getId());
							} else {
								twitter.createFavorite(status.getId());
							}
						} catch (TwitterException e) {
							handleException(e);
						}
					}
				});
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
				if (statusData.tag instanceof TwitterStatus) {
					TwitterStatus tag = (TwitterStatus) statusData.tag;
					menuItem.setText(tag.isFavorited() ? "ふぁぼを解除する(F)" : "ふぁぼる(F)");
				} else {
					menuItem.setText("ふぁぼる(F)");
				}
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
	}
	
	/**
	 * ジョブワーカースレッド。
	 * 
	 * @author $Author$
	 */
	private class JobWorkerThread extends Thread {
		
		protected boolean isCanceled = false;
		
		private Object threadHolder = new Object();
		
		
		public JobWorkerThread(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			
			jobQueue.setJobWorkerThread(threadHolder, this);
			while (isCanceled == false) {
				Runnable job = jobQueue.getJob();
				if (job == null) {
					synchronized (threadHolder) {
						try {
							if (isCanceled == false) {
								threadHolder.wait();
							}
						} catch (InterruptedException e) {
							// do nothing
						}
					}
				} else {
					try {
						job.run();
					} catch (RuntimeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * メニューのプロパティエディタを開くためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class MenuPropertyEditorActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			return new JMenuItem("Reply", KeyEvent.VK_R);
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			PropertyEditorFrame propertyEditorFrame = new PropertyEditorFrame(configuration);
			propertyEditorFrame.setVisible(true);
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			// This is always enabled.
		}
		
	}
	
	/**
	 * 終了するためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class MenuQuitActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			setVisible(false);
			dispose();
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			// This is always enabled
		}
		
	}
	
	private class PostListMouseListner extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			e.getComponent().requestFocusInWindow();
			if (e.isPopupTrigger()) {
				selectingPost = (StatusPanel) e.getComponent();
			}
			if (e.getClickCount() == 2) {
				StatusPanel panel = ((StatusPanel) e.getComponent());
				long uniqId = panel.getStatusData().id;
				StatusData statusData = statusMap.get(uniqId);
				actionHandlerTable.get("reply").handleAction("reply", statusData, TwitterClientFrame.this);
			}
			
		}
	}
	
	/**
	 * QTするためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class QuoteTweetActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenuItem quoteMenuItem = new JMenuItem("引用(Q)", KeyEvent.VK_Q);
			return quoteMenuItem;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				inReplyToStatus = (Status) statusData.tag;
				postDataTextArea.setText(String.format(" QT @%s: %s", inReplyToStatus.getUser().getScreenName(),
						inReplyToStatus.getText()));
				postDataTextArea.requestFocusInWindow();
				postDataTextArea.select(0, 0);
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
		
	}
	
	/**
	 * ツイートを削除するためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class RemoveTweetActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenuItem deleteMenuItem = new JMenuItem("削除(D)...", KeyEvent.VK_D);
			return deleteMenuItem;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				final Status status = (Status) statusData.tag;
				boolean isTweetedByMe = ((Status) statusData.tag).getUser().getId() == getLoginUser().getId();
				if (isTweetedByMe) {
					JPanel panel = new JPanel();
					BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
					panel.setLayout(layout);
					panel.add(new JLabel("次のツイートを削除しますか？"));
					panel.add(Box.createVerticalStrut(15));
					panel.add(new JLabel(status.getText()));
					final JOptionPane pane =
							new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
					JDialog dialog = pane.createDialog(TwitterClientFrame.this, "確認");
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					pane.addPropertyChangeListener(new PropertyChangeListener() {
						
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
								if (Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
									jobQueue.addJob(new Runnable() {
										
										@Override
										public void run() {
											try {
												twitter.destroyStatus(status.getId());
											} catch (TwitterException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									});
								}
							}
						}
						
					});
					dialog.setVisible(true);
				}
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
				boolean isTweetedByMe = ((Status) statusData.tag).getUser().getId() == getLoginUser().getId();
				menuItem.setEnabled(isTweetedByMe);
			} else {
				menuItem.setEnabled(false);
			}
		}
	}
	
	/**
	 * リプライするためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class ReplyActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenuItem replyMenuItem = new JMenuItem("Reply", KeyEvent.VK_R);
			return replyMenuItem;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				String text = String.format("@%s ", statusData.sentBy.getName());
				postDataTextArea.setText(text);
				postDataTextArea.requestFocusInWindow();
				postDataTextArea.select(text.length(), text.length());
				inReplyToStatus = (Status) statusData.tag;
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
	}
	
	/**
	 * 公式リツイートするためのアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class RetweetActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenuItem retweetMenuItem = new JMenuItem("リツイート(T)", KeyEvent.VK_T);
			return retweetMenuItem;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				final Status retweetStatus = (Status) statusData.tag;
				jobQueue.addJob(new Runnable() {
					
					@Override
					public void run() {
						try {
							twitter.retweetStatus(retweetStatus.getId());
						} catch (TwitterException e) {
							handleException(e);
						}
					}
				});
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if (statusData.isSystemNotify() || (statusData.tag instanceof Status) == false) {
				menuItem.setEnabled(false);
			}
			if (statusData.tag instanceof Status) {
				Status status = (Status) statusData.tag;
				menuItem.setEnabled(status.getUser().getId() != getLoginUser().getId());
			}
		}
		
	}
	
	/**
	 * StatusPanelのポップアップメニューリスナ
	 * 
	 * @author $Author$
	 */
	public class TweetPopupMenuListner implements PopupMenuListener {
		
		@Override
		public void popupMenuCanceled(PopupMenuEvent arg0) {
		}
		
		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
		}
		
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			if (selectingPost == null) {
				getComponent(0).requestFocusInWindow();
			}
			JPopupMenu popupMenu = (JPopupMenu) arg0.getSource();
			Component[] components = popupMenu.getComponents();
			for (Component component : components) {
				JMenuItem menuItem = (JMenuItem) component;
				StatusData statusData = statusMap.get(selectingPost.getStatusData().id);
				if (statusData == null) {
					menuItem.setEnabled(false);
				} else {
					ActionHandler actionHandler = getActionHandler(menuItem.getActionCommand());
					if (actionHandler != null) {
						actionHandler.popupMenuWillBecomeVisible(menuItem, statusData);
					} else {
						System.err.println("ActionHandler is not found: " + menuItem.getActionCommand());
						menuItem.setEnabled(false);
					}
				}
			}
		}
		
	}
	
	/**
	 * PostListを更新する。
	 * 
	 * @author $Author$
	 */
	public class UpdatePostList extends TimerTask {
		
		@Override
		public void run() {
			EventQueue.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					synchronized (postListAddQueue) {
						int size = postListAddQueue.size();
						sortedPostListPanel.add(postListAddQueue);
						Point viewPosition = postListScrollPane.getViewport().getViewPosition();
						if (viewPosition.y < fontHeight + 4) {
							postListScrollPane.getViewport().setViewPosition(new Point(viewPosition.x, 0));
						} else {
							postListScrollPane.getViewport().setViewPosition(
									new Point(viewPosition.x, viewPosition.y + (fontHeight + 3) * size));
						}
					}
				}
			});
		}
	}
	
	/**
	 * ツイートに含まれるURLを開くアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class UrlActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenu openUrlMenu = new JMenu("ツイートのURLをブラウザで開く");
			return openUrlMenu;
		}
		
		@Override
		public void handleAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			String url = actionName.substring(actionName.indexOf('!') + 1);
			try {
				Utility.openBrowser(url);
			} catch (Exception e) {
				e.printStackTrace(); //TODO
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if (menuItem instanceof JMenu == false) {
				throw new AssertionError("UrlActionHandler#pMWBV transfered menuItem which is not instanceof JMenu");
			}
			JMenu menu = (JMenu) menuItem;
			if (statusData.isSystemNotify() == false && statusData.tag instanceof Status) {
				Status status = (Status) statusData.tag;
				menu.removeAll();
				
				URLEntity[] urlEntities = status.getURLEntities();
				if (urlEntities == null || urlEntities.length == 0) {
					menu.setEnabled(false);
				} else {
					for (URLEntity entity : status.getURLEntities()) {
						JMenuItem urlMenu = new JMenuItem();
						if (entity.getDisplayURL() == null) {
							urlMenu.setText(entity.getURL().toString());
						} else {
							urlMenu.setText(entity.getDisplayURL());
						}
						urlMenu.setActionCommand("url!" + entity.getURL().toString());
						for (ActionListener listener : menu.getActionListeners()) {
							urlMenu.addActionListener(listener);
						}
						menu.add(urlMenu);
					}
					menu.setEnabled(true);
				}
			} else {
				menu.setEnabled(false);
			}
		}
		
	}
	
	/**
	 * ユーザー情報を表示するアクションハンドラ
	 * 
	 * @author $Author$
	 */
	public class UserInfoViewActionHandler implements ActionHandler {
		
		@Override
		public JMenuItem createJMenuItem() {
			JMenuItem aboutMenuItem = new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
			return aboutMenuItem;
		}
		
		@Override
		public void handleAction(String actionName, final StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				addJob(Priority.MEDIUM, new Runnable() {
					
					@Override
					public void run() {
						Status status = (Status) statusData.tag;
						try {
							Utility.openBrowser("http://twitter.com/" + status.getUser().getScreenName());
						} catch (Exception e) {
							e.printStackTrace(); //TODO
						}
					}
				});
			}
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			if ((statusData.isSystemNotify() == false) && (statusData.tag instanceof Status)) {
				Status status = (Status) statusData.tag;
				menuItem.setText(MessageFormat.format("@{0} ({1}) について(A)", status.getUser().getScreenName(), status
					.getUser().getName()));
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
	}
	
	
	/** アプリケーション名 */
	private static final String APPLICATION_NAME = "Astarotte";
	
	private StatusPanel selectingPost;
	
	private Hashtable<String, ActionHandler> actionHandlerTable;
	
	private Status inReplyToStatus = null;
	
	private final JobQueue jobQueue = new JobQueue();
	
	private Thread jobWorkerThread;
	
	private JPanel editPanel;
	
	private JPanel postPanel;
	
	private JScrollPane postDataTextAreaScrollPane;
	
	//	private JPanel jPanel3;
	
	private JScrollPane postListScrollPane;
	
	private JSplitPane jSplitPane1;
	
	private JButton postActionButton;
	
	private JTextArea postDataTextArea;
	
	private TwitterStream stream;
	
	//	private JLabel statusLabel;
	
	// private JPanel postListPanel;
	private SortedPostListPanel sortedPostListPanel;
	
	private TreeMap<String, ArrayList<StatusData>> listItems;
	
	private TreeMap<Long, StatusData> statusMap;
	
	private Twitter twitter;
	
	/** デフォルトフォント: TODO from config */
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	/** UIフォント: TODO from config */
	public static final Font UI_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
	
	/** フォントの高さ */
	protected int fontHeight;
	
	private MouseListener postListMouseListnerSingleton = new PostListMouseListner();
	
	private static final Dimension ICON_SIZE = new Dimension(64, 18);
	
	private JPopupMenu tweetPopupMenu;
	
	private User loginUser;
	
	private LinkedList<StatusPanel> postListAddQueue = new LinkedList<StatusPanel>();
	
	private Timer timer;
	
	private UpdatePostList updatePostListDispatcher;
	
	private ClientProperties configProperties;
	
	private JMenuBar clientMenu;
	
	private final ClientConfiguration configuration;
	
	private JPanel tweetViewPanel;
	
	private JScrollPane tweetViewScrollPane;
	
	private JEditorPane tweetViewEditorPane;
	
	private JLabel tweetViewSourceLabel;
	
	private JLabel tweetViewDateLabel;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	private FontMetrics fontMetrics;
	
	private Dimension linePanelSizeOfSentBy;
	
	
	/** 
	 * Creates new form TwitterClientFrame 
	 * @param configuration 設定
	 */
	public TwitterClientFrame(ClientConfiguration configuration) {
		this.configuration = configuration;
		configProperties = configuration.getConfigProperties();
		timer = new Timer("timer");
		actionHandlerTable = new Hashtable<String, ActionHandler>();
		initActionHandlerTable(actionHandlerTable);
		listItems = new TreeMap<String, ArrayList<StatusData>>();
		statusMap = new TreeMap<Long, StatusData>();
		twitter = new TwitterFactory(configuration.getTwitterConfiguration()).getInstance();
		getLoginUser();
		getPopupMenu();
		initComponents();
		
		updatePostListDispatcher = new UpdatePostList();
		timer.schedule(updatePostListDispatcher, configProperties.getInteger("client.main.interval.list_update"),
				configProperties.getInteger("client.main.interval.list_update"));
		
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (postListAddQueue) {
					System.out.println(sortedPostListPanel.toString());
				}
			}
		}, 1000, 10000);
		
		stream = new TwitterStreamFactory(configuration.getTwitterConfiguration()).getInstance();
		stream.addConnectionLifeCycleListener(new ClientConnectionLifeCycleListner(this));
		stream.addListener(new ClientStreamListner(this));
		
		fontMetrics = getFontMetrics(DEFAULT_FONT);
		int str12width = fontMetrics.stringWidth("0123456789abc");
		linePanelSizeOfSentBy = new Dimension(str12width, fontHeight);
	}
	
	/**
	 * ジョブを追加する
	 * 
	 * @param priority 優先度
	 * @param job ジョブ
	 */
	public void addJob(Priority priority, Runnable job) {
		jobQueue.addJob(priority, job);
	}
	
	/**
	 * ジョブを追加する
	 * 
	 * @param job ジョブ
	 */
	public void addJob(Runnable job) {
		jobQueue.addJob(job);
	}
	
	/**
	 * リストにステータスを追加する。
	 * 
	 * @param originalStatus 元となるStatus
	 */
	public void addStatus(Status originalStatus) {
		synchronized (listItems) {
			if (statusMap.containsKey(originalStatus.getId())) {
				return; // It was already added.
			}
		}
		Status twitterStatus = new TwitterStatus(originalStatus);
		StatusData statusData = new StatusData(twitterStatus, originalStatus.getCreatedAt(), originalStatus.getId());
		
		Status status;
		if (originalStatus.isRetweet()) {
			status = originalStatus.getRetweetedStatus();
		} else {
			status = originalStatus;
		}
		User user = status.getUser();
		
		if (configProperties.getBoolean("client.main.match.id_strict_match")) {
			if (user.getId() == loginUser.getId()) {
				statusData.foregroundColor = Color.BLUE;
			}
		} else {
			if (user.getScreenName().startsWith(loginUser.getScreenName())) {
				statusData.foregroundColor = Color.BLUE;
			}
		}
		
		ImageIcon iconImage = new ImageIcon(status.getUser().getProfileImageURL());
		iconImage.setImageObserver(AnimationCanceledImageObserver.SINGLETON);
		JLabel icon = new JLabel(iconImage);
		statusData.image = icon;
		
		String screenName = user.getScreenName();
		if (screenName.length() > 11) {
			screenName = screenName.substring(0, 9) + "..";
		}
		JLabel sentBy = new JLabel(screenName);
		sentBy.setName(user.getScreenName());
		sentBy.setFont(DEFAULT_FONT);
		statusData.sentBy = sentBy;
		
		JLabel statusText = new JLabel(status.getText());
		statusData.data = statusText;
		
		statusData.popupMenu = tweetPopupMenu;
		
		if (originalStatus.isRetweet()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Retweeted by @").append(originalStatus.getUser().getScreenName());
			statusData.tooltip = stringBuilder.toString();
		}
		
		if (originalStatus.isRetweet()) {
			statusData.foregroundColor = Color.GREEN;
		} else {
			UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
			for (UserMentionEntity userMentionEntity : userMentionEntities) {
				boolean mentioned = false;
				if (configProperties.getBoolean("client.main.match.id_strict_match")) {
					if (userMentionEntity.getId() == getLoginUser().getId()) {
						mentioned = true;
					}
				} else {
					if (userMentionEntity.getScreenName().startsWith(loginUser.getScreenName())) {
						mentioned = true;
					}
				}
				if (mentioned) {
					statusData.foregroundColor = Color.RED;
					Utility.sendNotify(user.getName(), originalStatus.getText());
				}
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
		linePanel.setAlignmentX(LEFT_ALIGNMENT);
		statusData.image.setInheritsPopupMenu(true);
		statusData.image.setFocusable(true);
		statusData.image.setMinimumSize(ICON_SIZE);
		statusData.image.setMaximumSize(ICON_SIZE);
		linePanel.add(statusData.image);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.sentBy.setInheritsPopupMenu(true);
		statusData.sentBy.setFocusable(true);
		statusData.sentBy.setMinimumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setMaximumSize(linePanelSizeOfSentBy);
		statusData.sentBy.setFont(DEFAULT_FONT);
		linePanel.add(statusData.sentBy);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.data.setInheritsPopupMenu(true);
		statusData.data.setFocusable(true);
		statusData.data.setFont(DEFAULT_FONT);
		int dataWidth = fontMetrics.stringWidth(statusData.data.getText());
		
		linePanel.add(statusData.data);
		linePanel.setComponentPopupMenu(statusData.popupMenu);
		/* if (information.isSystemNotify()) {
			information.backgroundColor = Color.BLACK;
		} */
		linePanel.setForeground(statusData.foregroundColor);
		linePanel.setBackground(statusData.backgroundColor);
		Dimension minSize =
				new Dimension(ICON_SIZE.width + linePanelSizeOfSentBy.width + dataWidth + 3 * 2, fontHeight + 4);
		linePanel.setMinimumSize(minSize);
		linePanel.setPreferredSize(minSize);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontHeight + 4));
		linePanel.addMouseListener(postListMouseListnerSingleton);
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusGained(FocusEvent e) {
				focusGainOfLinePanel(e);
			}
		});
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);
		
		statusData.addStatusPanel(linePanel);
		synchronized (listItems) {
			statusMap.put(statusData.id, statusData);
			ArrayList<StatusData> list = listItems.get(statusData.sentBy.getName());
			if (list == null) {
				list = new ArrayList<StatusData>();
				listItems.put(statusData.sentBy.getName(), list);
			}
			list.add(statusData);
		}
		synchronized (postListAddQueue) {
			postListAddQueue.add(linePanel);
		}
		return linePanel;
	}
	
	/**
	 * リストにステータスを追加する。その後deltionDelayミリ秒後に該当するステータスを削除する。
	 * 
	 * @param statusData StatusDataインスタンス。
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
	 * @throws NumberFormatException 数値ではないプロパティ
	 */
	private void focusGainOfLinePanel(FocusEvent e) throws IllegalArgumentException, NumberFormatException {
		e.getComponent().setBackground(configProperties.getColor("client.main.color.list.focus"));
		if (selectingPost != null) {
			selectingPost.setBackground(statusMap.get(selectingPost.getStatusData().id).backgroundColor);
		}
		selectingPost = (StatusPanel) e.getComponent();
		
		JEditorPane editor = getTweetViewEditorPane();
		StatusData statusData = selectingPost.getStatusData();
		if (statusData.tag instanceof Status) {
			Status status = (Status) statusData.tag;
			status = status.isRetweet() ? status.getRetweetedStatus() : status;
			String originalStatusText = status.getText();
			StringBuilder stringBuilder = new StringBuilder(originalStatusText.length());
			HashtagEntity[] hashtagEntities = status.getHashtagEntities();
			URLEntity[] urlEntities = status.getURLEntities();
			UserMentionEntity[] mentionEntities = status.getUserMentionEntities();
			Object[] entities = new Object[hashtagEntities.length + urlEntities.length + mentionEntities.length];
			
			{
				int copyOffset = 0;
				System.arraycopy(hashtagEntities, 0, entities, copyOffset, hashtagEntities.length);
				copyOffset += hashtagEntities.length;
				System.arraycopy(urlEntities, 0, entities, copyOffset, urlEntities.length);
				copyOffset += urlEntities.length;
				System.arraycopy(mentionEntities, 0, entities, copyOffset, mentionEntities.length);
			}
			Arrays.sort(entities, new Comparator<Object>() {
				
				@Override
				public int compare(Object o1, Object o2) {
					return getStart(o1) - getStart(o2);
				}
				
				private int getStart(Object obj) {
					if (obj instanceof HashtagEntity) {
						return ((HashtagEntity) obj).getStart();
					} else if (obj instanceof URLEntity) {
						return ((URLEntity) obj).getStart();
					} else if (obj instanceof UserMentionEntity) {
						return ((UserMentionEntity) obj).getStart();
					} else {
						throw new AssertionError();
					}
				}
			});
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
					url = "http://command/hashtag!" + hashtagEntity.getText();
				} else if (entity instanceof URLEntity) {
					URLEntity urlEntity = (URLEntity) entity;
					start = urlEntity.getStart();
					end = urlEntity.getEnd();
					replaceText = urlEntity.getDisplayURL();
					url = urlEntity.getExpandedURL().toString();
				} else if (entity instanceof UserMentionEntity) {
					UserMentionEntity mentionEntity = (UserMentionEntity) entity;
					start = mentionEntity.getStart();
					end = mentionEntity.getEnd();
					replaceText = null;
					url = "http://command/userinfo!" + mentionEntity.getScreenName();
				} else {
					throw new AssertionError();
				}
				
				if (offset < start) {
					stringBuilder.append(originalStatusText.substring(offset, start));
				}
				stringBuilder.append("<a href=\"");
				stringBuilder.append(url);
				stringBuilder.append("\">");
				stringBuilder.append(replaceText == null ? originalStatusText.substring(start, end) : replaceText);
				stringBuilder.append("</a>");
				
				offset = end;
			}
			stringBuilder.append(originalStatusText.substring(offset));
			editor.setText(stringBuilder.toString());
			
			JLabel viewSourceLabel = getTweetViewSourceLabel();
			viewSourceLabel.setText(MessageFormat.format("@{0} ({1})", status.getUser().getScreenName(), status
				.getUser().getName()));
			viewSourceLabel.setToolTipText(MessageFormat.format("from {0}", status.getSource()));
			
			JLabel viewDateLabel = getTweetViewDateLabel();
			viewDateLabel.setText(dateFormat.format(status.getCreatedAt()));
		} else {
			editor.setText(statusData.data.getText());
			getTweetViewSourceLabel().setText(statusData.sentBy.getName());
			getTweetViewDateLabel().setText(dateFormat.format(statusData.date));
		}
	}
	
	/**
	 * アクションハンドラを取得する
	 * 
	 * @param name アクション名。!を含んでいても可
	 * @return アクションハンドラ
	 */
	private ActionHandler getActionHandler(String name) {
		int indexOf = name.indexOf('!');
		String commandName = indexOf < 0 ? name : name.substring(0, indexOf);
		ActionHandler actionHandler = actionHandlerTable.get(commandName);
		return actionHandler;
	}
	
	/**
	 * メニューバーを取得する。
	 * 
	 * @return メニューバー
	 */
	private JMenuBar getClientMenuBar() {
		if (clientMenu == null) {
			clientMenu = new JMenuBar();
			{
				JMenu applicationMenu = new JMenu("アプリケーション");
				JMenuItem quitMenuItem = new JMenuItem("終了(Q)", KeyEvent.VK_Q);
				quitMenuItem.setActionCommand("menu_quit");
				quitMenuItem.addActionListener(new ActionListenerImplementation());
				applicationMenu.add(quitMenuItem);
				clientMenu.add(applicationMenu);
			}
			{
				JMenu configMenu = new JMenu("設定");
				JMenuItem propertyEditorMenuItem = new JMenuItem("プロパティエディター(P)", KeyEvent.VK_P);
				propertyEditorMenuItem.setActionCommand("menu_propeditor");
				propertyEditorMenuItem.addActionListener(new ActionListenerImplementation());
				configMenu.add(propertyEditorMenuItem);
				clientMenu.add(configMenu);
			}
		}
		return clientMenu;
	}
	
	private JPanel getEditPanel() {
		if (editPanel == null) {
			editPanel = new JPanel();
			GroupLayout layout = new GroupLayout(editPanel);
			editPanel.setLayout(layout);
			layout.setHorizontalGroup( //
				layout
					.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(getPostPanel(), GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) //
					.addComponent(getTweetViewPanel()));
			layout.setVerticalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING) //
					.addGroup(
							layout.createSequentialGroup()
								.addComponent(getPostPanel(), 32, 64, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(getTweetViewPanel(), 64, Short.MAX_VALUE, GroupLayout.PREFERRED_SIZE)));
			
		}
		return editPanel;
	}
	
	/**
	 * 一時的な情報を追加するときに、この時間たったら削除してもいーよ的な時間を取得する。
	 * 若干重要度が高いときは *2 とかしてみよう！
	 * 
	 * @return 一時的な情報が生き残る時間
	 */
	public int getInfoSurviveTime() {
		return configProperties.getInteger("client.info.survive_time");
	}
	
	/**
	 * ログインしているユーザーを取得する。取得出来なかった場合nullの可能性あり。また、ブロックする可能性あり。
	 * 
	 * @return the loginUser
	 */
	public User getLoginUser() {
		if (loginUser == null) {
			try {
				loginUser = twitter.verifyCredentials();
			} catch (TwitterException e) {
				handleException(e);
			}
		}
		return loginUser;
	}
	
	private JPopupMenu getPopupMenu() {
		if (tweetPopupMenu == null) {
			ActionListener actionListner = new ActionListenerImplementation();
			JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.addPopupMenuListener(new TweetPopupMenuListner());
			
			String[] popupMenus = configProperties.getProperty("gui.menu.popup").split(" ");
			
			for (String actionCommand : popupMenus) {
				ActionHandler handler = getActionHandler(actionCommand);
				if (handler == null) {
					System.err.println("handler " + actionCommand + " is not found."); //TODO
				} else {
					JMenuItem menuItem = handler.createJMenuItem();
					menuItem.setActionCommand(actionCommand);
					menuItem.addActionListener(actionListner);
					popupMenu.add(menuItem);
				}
			}
			tweetPopupMenu = popupMenu;
		}
		return tweetPopupMenu;
	}
	
	private JButton getPostActionButton() {
		if (postActionButton == null) {
			postActionButton = new javax.swing.JButton();
			postActionButton.setText("投稿");
			postActionButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					postActionButtonMouseClicked(e);
				}
			});
			
		}
		return postActionButton;
	}
	
	private JTextArea getPostDataTextArea() {
		if (postDataTextArea == null) {
			postDataTextArea = new javax.swing.JTextArea();
			postDataTextArea.setColumns(20);
			postDataTextArea.setRows(3);
			postDataTextArea.setFont(UI_FONT);
			postDataTextArea.addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.isControlDown()) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							getPostActionButton().doClick();
						}
					}
				}
			});
		}
		return postDataTextArea;
	}
	
	private JScrollPane getPostDataTextAreaScrollPane() {
		if (postDataTextAreaScrollPane == null) {
			postDataTextAreaScrollPane = new JScrollPane();
			postDataTextAreaScrollPane.setViewportView(getPostDataTextArea());
		}
		return postDataTextAreaScrollPane;
	}
	
	private LinkedList<StatusPanel> getPostListAddQueue() {
		return postListAddQueue;
	}
	
	private JScrollPane getPostListScrollPane() {
		if (postListScrollPane == null) {
			postListScrollPane = new javax.swing.JScrollPane();
			postListScrollPane.getViewport().setView(getSortedPostListPanel());
			postListScrollPane.getVerticalScrollBar().setUnitIncrement(
					configProperties.getInteger("client.main.list.scroll"));
			
		}
		return postListScrollPane;
	}
	
	private JPanel getPostPanel() {
		if (postPanel == null) {
			postPanel = new JPanel();
			GroupLayout layout = new GroupLayout(postPanel);
			postPanel.setLayout(layout);
			layout.setHorizontalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING) //
					.addGroup(
							GroupLayout.Alignment.TRAILING, //
							layout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(getPostDataTextAreaScrollPane(), GroupLayout.DEFAULT_SIZE, 475,
										Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED) //
								.addComponent(getPostActionButton()).addGap(18, 18, 18)));
			layout.setVerticalGroup( //
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						layout
							.createSequentialGroup()
							.addGroup(
									layout
										.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addComponent(getPostActionButton())
										.addComponent(getPostDataTextAreaScrollPane(), 32, 64,
												GroupLayout.PREFERRED_SIZE)).addContainerGap(6, Short.MAX_VALUE)));
			
		}
		return postPanel;
	}
	
	/**
	 * ソート済みリストパネルを取得する。将来的にはマルチタブになる予定なのでこのメソッドは廃止される予定です。
	 * 
	 * @return the sortedPostListPanel
	 */
	public SortedPostListPanel getSortedPostListPanel() {
		if (sortedPostListPanel == null) {
			sortedPostListPanel =
					new SortedPostListPanel(configProperties.getInteger("client.main.list.split_size"),
							configProperties.getInteger("client.main.list.max_size"));
			
			sortedPostListPanel.setBackground(Color.WHITE);
		}
		return sortedPostListPanel;
	}
	
	private JSplitPane getSplitPane1() {
		if (jSplitPane1 == null) {
			jSplitPane1 = new JSplitPane();
			jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			jSplitPane1.setTopComponent(getEditPanel());
			jSplitPane1.setRightComponent(getPostListScrollPane());
		}
		return jSplitPane1;
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param statusId
	 * @return 
	 */
	public StatusData getStatus(long statusId) {
		synchronized (listItems) {
			return statusMap.get(statusId);
		}
	}
	
	/**
	 * タイマーを取得する。
	 * 
	 * @return タイマー
	 */
	public Timer getTimer() {
		return timer;
	}
	
	private JLabel getTweetViewDateLabel() {
		if (tweetViewDateLabel == null) {
			tweetViewDateLabel = new JLabel();
			tweetViewDateLabel.setText("2012/1/1 00:00:00");
		}
		return tweetViewDateLabel;
	}
	
	private JEditorPane getTweetViewEditorPane() {
		if (tweetViewEditorPane == null) {
			tweetViewEditorPane = new JEditorPane();
			tweetViewEditorPane.setEditable(false);
			tweetViewEditorPane.setContentType("text/html");
			tweetViewEditorPane.setFont(UI_FONT);
			tweetViewEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			tweetViewEditorPane.setText(APPLICATION_NAME + "へようこそ！<br><b>ゆっくりしていってね！</b>");
			tweetViewEditorPane.addHyperlinkListener(new HyperlinkListener() {
				
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						if (url.startsWith("http://command/")) {
							String command = url.substring("http://command/".length());
							handleAction(command, selectingPost == null ? null : selectingPost.getStatusData());
						} else {
							try {
								Utility.openBrowser(url);
							} catch (Exception e1) {
								e1.printStackTrace(); //TODO
							}
						}
					}
				}
			});
		}
		return tweetViewEditorPane;
	}
	
	private JPanel getTweetViewPanel() {
		if (tweetViewPanel == null) {
			tweetViewPanel = new JPanel();
			GroupLayout layout = new GroupLayout(tweetViewPanel);
			tweetViewPanel.setLayout(layout);
			layout.setVerticalGroup( //
				layout.createSequentialGroup() //
					.addGroup(layout.createParallelGroup(Alignment.LEADING) // 
						.addComponent(getTweetViewSourceLabel(), Alignment.LEADING) //
						.addComponent(getTweetViewDateLabel(), Alignment.LEADING)) //
					.addContainerGap().addComponent(getTweetViewScrollPane()));
			layout.setHorizontalGroup( //
				layout.createParallelGroup(Alignment.LEADING) //
					.addGroup(layout.createSequentialGroup() // 
						.addComponent(getTweetViewSourceLabel()) //
						.addPreferredGap(ComponentPlacement.RELATED).addContainerGap(8, Short.MAX_VALUE) //
						.addComponent(getTweetViewDateLabel())) //
					.addComponent(getTweetViewScrollPane()));
		}
		return tweetViewPanel;
	}
	
	private JScrollPane getTweetViewScrollPane() {
		if (tweetViewScrollPane == null) {
			tweetViewScrollPane = new JScrollPane();
			tweetViewScrollPane.getViewport().setView(getTweetViewEditorPane());
			tweetViewScrollPane.getVerticalScrollBar().setUnitIncrement(
					configProperties.getInteger("client.main.list.scroll"));
		}
		return tweetViewScrollPane;
	}
	
	private JLabel getTweetViewSourceLabel() {
		if (tweetViewSourceLabel == null) {
			tweetViewSourceLabel = new JLabel();
			tweetViewSourceLabel.setText("@twclient (仮の名前＠だれか名前考えて)");
			tweetViewSourceLabel.setToolTipText("from 暗黒の調和師");
		}
		return tweetViewSourceLabel;
	}
	
	/**
	* Actionをhandleする。
	* 
	* @param name Action名
	* @param statusData ステータス情報
	*/
	protected void handleAction(String name, StatusData statusData) {
		ActionHandler actionHandler = getActionHandler(name);
		if (actionHandler == null) {
			System.err.println("ActionHandler " + name + " is not found.");
		} else {
			actionHandler.handleAction(name, statusData, this);
		}
	}
	
	/**
	 * 例外を処理する。
	 * 
	 * @param ex 例外
	 */
	public void handleException(Exception ex) {
		if (ex instanceof TwitterException) {
			handleException((TwitterException) ex);
			//TODO
		} else {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 例外を処理する。
	 * @param e 例外
	 */
	public void handleException(TwitterException e) {
		Date date = new Date(System.currentTimeMillis() + 10000);
		StatusData information = new StatusData(e, date);
		information.foregroundColor = Color.RED;
		information.backgroundColor = Color.BLACK;
		information.image = new JLabel();
		information.sentBy = new JLabel("ERROR!");
		information.sentBy.setName("!sys.ex.TwitterException");
		String errorMessage = e.getErrorMessage();
		information.data =
				new JLabel(errorMessage == null ? e.getLocalizedMessage() : errorMessage + ": "
						+ postDataTextArea.getText());
		addStatus(information);
	}
	
	/**
	 * アクションハンドラーテーブルを初期化する。
	 * @param table アクションハンドラーテーブル
	 */
	private void initActionHandlerTable(Hashtable<String, ActionHandler> table) {
		table.put("reply", new ReplyActionHandler());
		table.put("qt", new QuoteTweetActionHandler());
		table.put("rt", new RetweetActionHandler());
		table.put("fav", new FavoriteActionHandler());
		table.put("remove", new RemoveTweetActionHandler());
		table.put("userinfo", new UserInfoViewActionHandler());
		table.put("url", new UrlActionHandler());
		table.put("menu_quit", new MenuQuitActionHandler());
		table.put("menu_propeditor", new MenuPropertyEditorActionHandler());
	}
	
	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		setTitle(APPLICATION_NAME);
		
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
				getSplitPane1(), GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(getSplitPane1(),
				GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE));
		
		fontHeight = getSortedPostListPanel().getFontMetrics(DEFAULT_FONT).getHeight();
		
		pack();
		
		setJMenuBar(getClientMenuBar());
		setSize(500, 500);
	}
	
	private void postActionButtonMouseClicked(final ActionEvent e) {
		if (postDataTextArea.getText().isEmpty() == false) {
			postActionButton.setEnabled(false);
			postDataTextArea.setEnabled(false);
			
			addJob(Priority.HIGH, new Runnable() {
				
				@Override
				public void run() {
					try {
						StatusUpdate statusUpdate = new StatusUpdate(postDataTextArea.getText());
						if (inReplyToStatus != null) {
							statusUpdate.setInReplyToStatusId(inReplyToStatus.getId());
						}
						twitter.updateStatus(statusUpdate);
						postDataTextArea.setText("");
						inReplyToStatus = null;
					} catch (TwitterException e) {
						handleException(e);
					} finally {
						try {
							EventQueue.invokeAndWait(new Runnable() {
								
								@Override
								public void run() {
									postActionButton.setEnabled(true);
									postDataTextArea.setEnabled(true);
								}
							});
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	/**
	 * ステータスを削除する。
	 * 
	 * @param statusData ステータスデータ
	 */
	public void removeStatus(final StatusData statusData) {
		try {
			for (StatusPanel panel : statusData.getStatusPanels()) {
				sortedPostListPanel.remove(panel);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ステータスを削除する。
	 * 
	 * @param statusData ステータスデータ
	 * @param delay 遅延 (ms)
	 */
	public void removeStatus(final StatusData statusData, int delay) {
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				removeStatus(statusData);
			}
		}, delay);
	}
	
	/**
	 * inReplyToStatusを付加する。
	 * 
	 * @param status ステータス
	 * @return 前設定されたinReplyToStatus
	 */
	public Status setInReplyToStatus(Status status) {
		Status previousInReplyToStatus = inReplyToStatus;
		inReplyToStatus = status;
		return previousInReplyToStatus;
	}
	
	/**
	 * 開始する
	 */
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				setVisible(true);
			}
		});
		stream.user();
		jobQueue.addJob(new Runnable() {
			
			@Override
			public void run() {
				ResponseList<Status> homeTimeline;
				try {
					Paging paging =
							new Paging().count(configProperties.getInteger("client.main.page.initial_timeline"));
					homeTimeline = twitter.getHomeTimeline(paging);
					for (Status status : homeTimeline) {
						addStatus(status);
					}
				} catch (TwitterException e) {
					handleException(e);
				}
			}
		});
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				jobQueue.addJob(new Runnable() {
					
					@Override
					public void run() {
						Paging paging = new Paging().count(configProperties.getInteger("client.main.page.timeline"));
						ResponseList<Status> timeline;
						try {
							timeline = twitter.getHomeTimeline(paging);
							for (Status status : timeline) {
								addStatus(status);
							}
						} catch (TwitterException e) {
							handleException(e);
						}
					}
				});
			}
		}, configProperties.getInteger("client.main.interval.timeline"),
				configProperties.getInteger("client.main.interval.timeline"));
		jobWorkerThread = new JobWorkerThread("jobworker");
		jobWorkerThread.start();
	}
}
