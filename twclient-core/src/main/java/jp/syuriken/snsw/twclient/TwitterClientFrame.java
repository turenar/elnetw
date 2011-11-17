package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.syuriken.snsw.twclient.JobQueue.Priority;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 *
 * @author snsoftware
 */
public class TwitterClientFrame extends javax.swing.JFrame {
	
	/**
	 * TODO snsoftware
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
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class FavoriteActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, final StatusData statusData, TwitterClientFrame frameInstance) {
			if (statusData.tag instanceof Status) {
				addJob(new Runnable() {
					
					@Override
					public void run() {
						Status status = (Status) statusData.tag;
						try {
							twitter.createFavorite(status.getId());
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
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
	}
	
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
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class MenuPropertyEditorActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
			PropertyEditorFrame propertyEditorFrame = new PropertyEditorFrame(configuration);
			propertyEditorFrame.setVisible(true);
		}
		
		@Override
		public void popupMenuWillBecomeVisible(JMenuItem menuItem, StatusData statusData) {
			// This is always enabled.
		}
		
	}
	
	/**
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class MenuQuitActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
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
				actionHandlerTable.get("reply").dispatchAction("reply", statusData, TwitterClientFrame.this);
			}
			
		}
	}
	
	/**
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class QuoteTweetActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
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
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class RemoveTweetActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
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
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class ReplyActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
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
	 * TODO snsoftware
	 * 
	 * @author $Author$
	 */
	public class RetweetActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, StatusData statusData, TwitterClientFrame frameInstance) {
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
		}
		
	}
	
	/**
		 * TODO snsoftware
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
					ActionHandler actionHandler = actionHandlerTable.get(menuItem.getActionCommand());
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
		 * TODO snsoftware
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
						sortedPostListPanel.add(postListAddQueue);
					}
				}
			});
		}
	}
	
	/**
		 * TODO snsoftware
		 * 
		 * @author $Author$
		 */
	public class UserInfoViewActionDispatcher implements ActionHandler {
		
		@Override
		public void dispatchAction(String actionName, final StatusData statusData, TwitterClientFrame frameInstance) {
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
				menuItem.setText(status.getUser().getName() + " について(A)");
				menuItem.setEnabled(true);
			} else {
				menuItem.setEnabled(false);
			}
		}
		
	}
	
	
	/** TODO snsoftware */
	private static final String APPLICATION_NAME = "Astarotte";
	
	private StatusPanel selectingPost;
	
	private Hashtable<String, ActionHandler> actionHandlerTable;
	
	private Status inReplyToStatus = null;
	
	private final JobQueue jobQueue = new JobQueue();
	
	private Thread jobWorkerThread;
	
	private JPanel jPanel1;
	
	private JPanel jPanel2;
	
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
	
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	public int fontHeight;
	
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
	
	
	/** Creates new form TwitterClientFrame 
	 * @param configuration */
	public TwitterClientFrame(ClientConfiguration configuration) {
		this.configuration = configuration;
		configProperties = configuration.getConfigProperties();
		timer = new Timer("timer");
		actionHandlerTable = new Hashtable<String, ActionHandler>();
		actionHandlerTable.put("reply", new ReplyActionDispatcher());
		actionHandlerTable.put("qt", new QuoteTweetActionDispatcher());
		actionHandlerTable.put("rt", new RetweetActionDispatcher());
		actionHandlerTable.put("fav", new FavoriteActionDispatcher());
		actionHandlerTable.put("remove", new RemoveTweetActionDispatcher());
		actionHandlerTable.put("userinfo", new UserInfoViewActionDispatcher());
		actionHandlerTable.put("menu_quit", new MenuQuitActionDispatcher());
		actionHandlerTable.put("menu_propeditor", new MenuPropertyEditorActionDispatcher());
		listItems = new TreeMap<String, ArrayList<StatusData>>();
		statusMap = new TreeMap<Long, StatusData>();
		twitter = new TwitterFactory(configuration.getTwitterConfiguration()).getInstance();
		try {
			loginUser = twitter.verifyCredentials();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initPopupMenu();
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
	}
	
	public void addJob(Priority priority, Runnable job) {
		jobQueue.addJob(priority, job);
	}
	
	public void addJob(Runnable job) {
		jobQueue.addJob(job);
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param originalStatus
	 */
	public void addStatus(Status originalStatus) {
		if (statusMap.containsKey(originalStatus.getId())) {
			return; // It was already added.
		}
		StatusData statusData = new StatusData(originalStatus, originalStatus.getCreatedAt(), originalStatus.getId());
		Status status;
		if (originalStatus.isRetweet()) {
			status = originalStatus.getRetweetedStatus();
		} else {
			status = originalStatus;
		}
		User user = status.getUser();
		
		if (user.getId() == loginUser.getId()) {
			statusData.foregroundColor = Color.BLUE;
		}
		
		ImageIcon iconImage = new ImageIcon(status.getUser().getProfileImageURL());
		iconImage.setImageObserver(new ImageObserver() {
			
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				return false; // Animation GIF is disabled.
			}
		});
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
				if (userMentionEntity.getId() == getLoginUser().getId()) {
					statusData.foregroundColor = Color.RED;
					sendNotify(status.getUser().getName(), status.getText());
				}
			}
		}
		
		addStatus(statusData);
	}
	
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
		FontMetrics fontMetrics = statusData.sentBy.getFontMetrics(DEFAULT_FONT);
		int str10width = fontMetrics.stringWidth("0123456789abc");
		statusData.sentBy.setMinimumSize(new Dimension(str10width, fontHeight));
		statusData.sentBy.setMaximumSize(new Dimension(str10width, fontHeight));
		statusData.sentBy.setFont(DEFAULT_FONT);
		linePanel.add(statusData.sentBy);
		linePanel.add(Box.createHorizontalStrut(3));
		statusData.data.setInheritsPopupMenu(true);
		statusData.data.setFocusable(true);
		statusData.data.setFont(DEFAULT_FONT);
		linePanel.add(statusData.data);
		linePanel.setComponentPopupMenu(statusData.popupMenu);
		/* if (information.isSystemNotify()) {
			information.backgroundColor = Color.BLACK;
		} */
		linePanel.setForeground(statusData.foregroundColor);
		linePanel.setBackground(statusData.backgroundColor);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontHeight + 4));
		linePanel.addMouseListener(postListMouseListnerSingleton);
		linePanel.setFocusable(true);
		linePanel.setToolTipText(statusData.tooltip);
		linePanel.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusGained(FocusEvent e) {
				e.getComponent().setBackground(configProperties.getColor("client.main.color.list.focus"));
				if (selectingPost != null) {
					selectingPost.setBackground(statusMap.get(selectingPost.getStatusData().id).backgroundColor);
				}
				selectingPost = (StatusPanel) e.getComponent();
			}
			
			@Override
			public void focusLost(FocusEvent e) {
			}
		});
		statusData.image.setForeground(statusData.foregroundColor);
		statusData.sentBy.setForeground(statusData.foregroundColor);
		statusData.data.setForeground(statusData.foregroundColor);
		
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
	
	public JPanel addStatus(StatusData statusData, int deletionDelay) {
		final JPanel status = addStatus(statusData);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				sortedPostListPanel.remove(status);
			}
		}, deletionDelay);
		return status;
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @return
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
	
	/**
	 * TODO snsoftware
	 * 
	 * @return the loginUser
	 */
	public User getLoginUser() {
		return loginUser;
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @return the sortedPostListPanel
	 */
	public SortedPostListPanel getSortedPostListPanel() {
		return sortedPostListPanel;
	}
	
	public Timer getTimer() {
		return timer;
	}
	
	/**
	* TODO snsoftware
	* 
	* @param name
	*/
	protected void handleAction(String name, StatusData statusData) {
		ActionHandler actionHandler = actionHandlerTable.get(name);
		actionHandler.dispatchAction(name, statusData, this);
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param ex
	 */
	public void handleException(Exception ex) {
		if (ex instanceof TwitterException) {
			handleException((TwitterException) ex);
			//TODO			
		}
	}
	
	/**
	 * TODO snsoftware
	 * 
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
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		setTitle(APPLICATION_NAME);
		
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		postDataTextAreaScrollPane = new javax.swing.JScrollPane();
		postDataTextArea = new javax.swing.JTextArea();
		postActionButton = new javax.swing.JButton();
		postListScrollPane = new javax.swing.JScrollPane();
		sortedPostListPanel =
				new SortedPostListPanel(configProperties.getInteger("client.main.list.split_size"),
						configProperties.getInteger("client.main.list.max_size"));
		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		
		postDataTextArea.setColumns(20);
		postDataTextArea.setRows(5);
		postDataTextArea.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isControlDown()) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						postActionButton.doClick();
					}
				}
			}
		});
		postDataTextAreaScrollPane.setViewportView(postDataTextArea);
		
		postActionButton.setText("投稿");
		postActionButton.setName("postActionButton"); // NOI18N
		postActionButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				postActionButtonMouseClicked(e);
			}
		});
		
		GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				jPanel2Layout.createSequentialGroup().addContainerGap()
					.addComponent(postDataTextAreaScrollPane, GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(postActionButton)
					.addGap(18, 18, 18)));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jPanel2Layout
					.createSequentialGroup()
					.addGroup(
							jPanel2Layout
								.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(postActionButton)
								.addComponent(postDataTextAreaScrollPane, GroupLayout.PREFERRED_SIZE, 80,
										GroupLayout.PREFERRED_SIZE)).addContainerGap(6, Short.MAX_VALUE)));
		
//		statusLabel.setText("(Information Area)");
		
//		GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
//		jPanel3.setLayout(jPanel3Layout);
//		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
//				statusLabel, GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
//		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
//				statusLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		
//		statusLabel.setText(APPLICATION_NAME);
		
		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//			.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(jPanel2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
					Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jPanel1Layout.createSequentialGroup()
					.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
//					.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			));
		
		sortedPostListPanel.setBackground(Color.WHITE);
		postListScrollPane.getViewport().setView(sortedPostListPanel);
		
		jSplitPane1.setTopComponent(jPanel1);
		jSplitPane1.setRightComponent(postListScrollPane);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSplitPane1,
				GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSplitPane1,
				GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE));
		
		fontHeight = sortedPostListPanel.getFontMetrics(DEFAULT_FONT).getHeight();
		
		pack();
		
		setJMenuBar(getClientMenuBar());
		setSize(500, 500);
	}
	
	private void initPopupMenu() {
		ActionListener actionListner = new ActionListenerImplementation();
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new TweetPopupMenuListner());
		
		JMenuItem replyMenuItem = new JMenuItem("Reply", KeyEvent.VK_R);
		replyMenuItem.setActionCommand("reply");
		replyMenuItem.addActionListener(actionListner);
		popupMenu.add(replyMenuItem);
		JMenuItem quoteMenuItem = new JMenuItem("引用(Q)", KeyEvent.VK_Q);
		quoteMenuItem.setActionCommand("qt");
		quoteMenuItem.addActionListener(actionListner);
		popupMenu.add(quoteMenuItem);
		JMenuItem retweetMenuItem = new JMenuItem("リツイート(T)", KeyEvent.VK_T);
		retweetMenuItem.setActionCommand("rt");
		retweetMenuItem.addActionListener(actionListner);
		popupMenu.add(retweetMenuItem);
		JMenuItem favMenuItem = new JMenuItem("ふぁぼる(F)", KeyEvent.VK_F);
		favMenuItem.setActionCommand("fav");
		favMenuItem.addActionListener(actionListner);
		popupMenu.add(favMenuItem);
		JMenuItem deleteMenuItem = new JMenuItem("削除(D)...", KeyEvent.VK_D);
		deleteMenuItem.setActionCommand("remove");
		deleteMenuItem.addActionListener(actionListner);
		popupMenu.add(deleteMenuItem);
		JMenuItem aboutMenuItem = new JMenuItem("ユーザーについて(A)...", KeyEvent.VK_A);
		aboutMenuItem.setActionCommand("userinfo");
		aboutMenuItem.addActionListener(actionListner);
		popupMenu.add(aboutMenuItem);
		tweetPopupMenu = popupMenu;
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
	 * TODO snsoftware
	 * 
	 * @param text
	 */
	private void sendNotify(String summary, String text) {
		try {
			if (Runtime.getRuntime().exec(new String[] {
				"which",
				"notify-send"
			}).waitFor() == 0) {
				Runtime.getRuntime().exec(new String[] {
					"notify-send",
					summary,
					text
				});
			}
		} catch (InterruptedException e) {
			e.printStackTrace(); //TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*public void setStatusBar(final String status) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				statusLabel.setText(status);
			}
		});
	}*/
	
	public Status setInReplyToStatus(Status status) {
		Status previousInReplyToStatus = inReplyToStatus;
		inReplyToStatus = status;
		return previousInReplyToStatus;
	}
	
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
