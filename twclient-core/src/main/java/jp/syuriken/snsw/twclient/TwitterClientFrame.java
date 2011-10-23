/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TwitterClientFrame.java
 *
 * Created on 2011/10/20, 18:07:06
 */
package jp.syuriken.snsw.twclient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Label;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;

/**
 *
 * @author snsoftware
 */
public class TwitterClientFrame extends javax.swing.JFrame {
	
	private class JobWorkerThread extends Thread {
		
		protected boolean isCanceled = false;
		
		
		public JobWorkerThread(String name) {
			super(name);
		}
		
		@Override
		public void run() {
			while (isCanceled == false) {
				synchronized (jobQueue) {
					Runnable job = jobQueue.pollFirst();
					if (job == null) {
						try {
							jobQueue.wait();
						} catch (InterruptedException e) {
							// do nothing
						}
					} else {
						job.run();
					}
				}
			}
		}
	}
	
	
	private final java.util.LinkedList<String> infoList = new java.util.LinkedList<String>();
	
	private final java.util.LinkedList<Runnable> jobQueue = new java.util.LinkedList<Runnable>();
	
	private Thread jobWorkerThread;
	
	private JPanel jPanel1;
	
	private JPanel jPanel2;
	
	private JPanel jPanel3;
	
	private JScrollPane postDataTextAreaScrollPane;
	
	private JScrollPane postListScrollPane;
	
	private JSplitPane jSplitPane1;
	
	private JButton postActionButton;
	
	private JTextArea postDataTextArea;
	
	private JLabel statusLabel;
	
	private TwitterStream stream;
	
	private JPanel postListPanel;
	
	private Label statusBar;
	
	private HashMap<String, ArrayList<StatusData>> listItems;
	
	private Twitter twitter;
	
	public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	public int fontHeight;
	
	private static final Dimension ICON_SIZE = new Dimension(64, 18);
	
	
	/** Creates new form TwitterClientFrame 
	 * @param configuration */
	public TwitterClientFrame(Configuration configuration) {
		twitter = new TwitterFactory(configuration).getInstance();
		initComponents();
		
		stream = new TwitterStreamFactory(configuration).getInstance();
		stream.addConnectionLifeCycleListener(new ClientConnectionLifeCycleListner(this));
		stream.addListener(new ClientStreamListner(this));
		listItems = new HashMap<String, ArrayList<StatusData>>();
	}
	
	public void addJob(Runnable job) {
		synchronized (jobQueue) {
			jobQueue.add(job);
			jobQueue.notifyAll();
		}
	}
	
	/**
	 * TODO snsoftware
	 * 
	 * @param originalStatus
	 */
	public void addStatus(Status originalStatus) {
		StatusData statusData = new StatusData();
		Status status;
		if (originalStatus.isRetweet()) {
			status = originalStatus.getRetweetedStatus();
			statusData.foregroundColor = Color.GREEN;
		} else {
			status = originalStatus;
		}
		
		JLabel icon = new JLabel(new ImageIcon(status.getUser().getProfileImageURL()));
		icon.setMinimumSize(ICON_SIZE);
		icon.setMaximumSize(ICON_SIZE);
		statusData.image = icon;
		
		String screenName = status.getUser().getScreenName();
		if (originalStatus.isRetweet()) {
			screenName = "(RT)" + screenName;
		}
		if (screenName.length() > 13) {
			screenName = screenName.substring(0, 10) + "...";
		}
		JLabel sentBy = new JLabel(screenName);
		sentBy.setFont(DEFAULT_FONT);
		FontMetrics fontMetrics = sentBy.getFontMetrics(DEFAULT_FONT);
		int str10width = fontMetrics.stringWidth("0123456789abc");
		int fontHeight = fontMetrics.getHeight();
		sentBy.setMinimumSize(new Dimension(str10width, fontHeight));
		sentBy.setMaximumSize(new Dimension(str10width, fontHeight));
		statusData.sentBy = sentBy;
		
		JLabel statusText = new JLabel(status.getText());
		statusText.setFont(DEFAULT_FONT);
		statusData.data = statusText;
		
		addStatus(statusData);
	}
	
	public void addStatus(StatusData information) {
		final JPanel linePanel = new JPanel();
		BoxLayout layout = new BoxLayout(linePanel, BoxLayout.X_AXIS);
		linePanel.setLayout(layout);
		linePanel.setAlignmentX(LEFT_ALIGNMENT);
		linePanel.add(information.image);
		linePanel.add(Box.createHorizontalStrut(3));
		linePanel.add(information.sentBy);
		linePanel.add(Box.createHorizontalStrut(16));
		linePanel.add(information.data);
		linePanel.setComponentPopupMenu(information.popupMenu);
		linePanel.setForeground(information.foregroundColor);
		linePanel.setBackground(information.backgroundColor);
		linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fontHeight + 4));
		
		synchronized (listItems) {
			ArrayList<StatusData> list = listItems.get(information.sentBy.getText());
			if (list == null) {
				list = new ArrayList<StatusData>();
				listItems.put(information.sentBy.getText(), list);
			}
			list.add(information);
		}
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				postListPanel.add(linePanel);
				validate();
			}
		});
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		setTitle("Twclient");
		
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		postDataTextAreaScrollPane = new javax.swing.JScrollPane();
		postDataTextArea = new javax.swing.JTextArea();
		postActionButton = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		statusLabel = new javax.swing.JLabel();
		postListScrollPane = new javax.swing.JScrollPane();
		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		
		postDataTextArea.setColumns(20);
		postDataTextArea.setRows(5);
		postDataTextAreaScrollPane.setViewportView(postDataTextArea);
		
		postActionButton.setText("投稿");
		postActionButton.setName("postActionButton"); // NOI18N
		postActionButton.addMouseListener(new java.awt.event.MouseAdapter() {
			
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				postActionButtonMouseClicked(evt);
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
		
		statusLabel.setText("(Information Area)");
		
		GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
				statusLabel, GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
				statusLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		
		statusLabel.setText("");
		
		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout
			.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(jPanel2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
					Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jPanel1Layout.createSequentialGroup()
					.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
		postListPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(postListPanel, BoxLayout.Y_AXIS);
		postListPanel.setLayout(boxLayout);
		postListPanel.setBackground(Color.WHITE);
		postListScrollPane.getViewport().setView(postListPanel);
		
		jSplitPane1.setTopComponent(jPanel1);
		jSplitPane1.setRightComponent(postListScrollPane);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSplitPane1,
				GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSplitPane1,
				GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE));
		
		statusBar = new Label("twclient initialized.");
		getContentPane().add(statusBar);
		
		fontHeight = postListPanel.getFontMetrics(DEFAULT_FONT).getHeight();
		
		pack();
	}
	
	private void postActionButtonMouseClicked(final java.awt.event.MouseEvent evt) {
		if (postDataTextArea.getText().isEmpty() == false) {
			addJob(new Runnable() {
				
				@Override
				public void run() {
					try {
						EventQueue.invokeAndWait(new Runnable() {
							
							@Override
							public void run() {
								postActionButton.setEnabled(false);
								postDataTextArea.setEnabled(false);
							}
						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						twitter.updateStatus(postDataTextArea.getText());
						postDataTextArea.setText("");
					} catch (TwitterException e) {
						// TODO
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
	
	public void setStatusBar(final String status) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				statusLabel.setText(status);
			}
		});
	}
	
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				setVisible(true);
			}
		});
		stream.user();
		jobQueue.add(new Runnable() {
			
			@Override
			public void run() {
				ResponseList<Status> homeTimeline;
				try {
					homeTimeline = twitter.getHomeTimeline();
				} catch (TwitterException e) {
					return; //TODO
				}
				for (Status status : homeTimeline) {
					addStatus(status);
				}
			}
		});
		jobWorkerThread = new JobWorkerThread("jobworker");
		jobWorkerThread.start();
	}
}
