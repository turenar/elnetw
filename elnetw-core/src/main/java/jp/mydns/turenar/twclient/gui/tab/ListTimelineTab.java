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

import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.channel.FilterStreamChannel;
import jp.mydns.turenar.twclient.bus.channel.ListTimelineChannel;
import jp.mydns.turenar.twclient.gui.render.RenderTarget;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserList;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement;

/**
 * list tab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ListTimelineTab extends AbstractClientTab implements RenderTarget {
	private static final String TAB_ID = "list";
	public static final int SEARCH_FIELD_MIN_SIZE = 128;
	private JPanel tabComponent;
	private JPanel componentSearchPanel;
	private JLabel componentSearchLabel;
	private JTextField componentSearchArea;
	private JButton componentUpdateButton;
	private long listId = -1;
	private String listOwner;
	private String slug;
	private volatile UserList listInfo;
	private volatile Set<User> listMemberSet;
	private DelegateRenderer renderer = new DelegateRenderer() {
		@Override
		public void onClientMessage(String name, Object arg) {
			if (name.equals(ListTimelineChannel.LIST_MEMBERS_MESSAGE_ID)) {
				listMemberSet = Utility.uncheckedCast(arg);
				updateStream();
			}
		}

		@Override
		public void onException(Exception ex) {
			actualRenderer.onException(ex);
		}

		@Override
		public void onStatus(Status status) {
			actualRenderer.onStatus(status);
		}

		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
			updateStream();
		}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
			updateStream();
		}

		@Override
		public void onUserListUpdate(User listOwner, UserList list) {
			listInfo = list;
			updateTab();
		}
	};
	private String lastEstablishedStreamPath;

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId account id
	 * @param listId    list unique id
	 */
	public ListTimelineTab(String accountId, long listId) {
		super(accountId);
		this.listId = listId;
		establishChannel();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId account id
	 * @param listOwner the owner of list
	 * @param slug      the name of list
	 */
	public ListTimelineTab(String accountId, String listOwner, String slug) {
		super(accountId);
		this.listOwner = listOwner;
		this.slug = slug;
		establishChannel();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param uniqId unique identifier
	 */
	public ListTimelineTab(String uniqId) {
		super(TAB_ID, uniqId);
		listId = configProperties.getLong(getPropertyPrefix() + ".listId", -1);
		listOwner = configProperties.getProperty(getPropertyPrefix() + ".listOwner");
		slug = configProperties.getProperty(getPropertyPrefix() + ".slug");
		establishChannel();
	}

	/**
	 * establish list timeline
	 */
	protected void establishChannel() {
		if (listOwner == null) {
			ListTimelineChannel.establish(accountId, getRenderer(), listId);
		} else {
			ListTimelineChannel.establish(accountId, getRenderer(), listOwner, slug);
		}
	}

	private JTextField getComponentSearchField() {
		if (componentSearchArea == null) {
			componentSearchArea = new JTextField(listOwner);
		}
		return componentSearchArea;
	}

	private JLabel getComponentSearchLabel() {
		if (componentSearchLabel == null) {
			componentSearchLabel = new JLabel("クエリ:");
		}
		return componentSearchLabel;
	}

	private JPanel getComponentSearchPanel() {
		if (componentSearchPanel == null) {
			componentSearchPanel = new JPanel();
			GroupLayout layout = new GroupLayout(componentSearchPanel);
			componentSearchPanel.setLayout(layout);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(2)
					.addGroup(layout.createBaselineGroup(true, true)
							.addComponent(getComponentSearchLabel())
							.addComponent(getComponentSearchField())
							.addComponent(getComponentUpdateButton()))
					.addGap(2));
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(2)
					.addComponent(getComponentSearchLabel())
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getComponentSearchField(), SEARCH_FIELD_MIN_SIZE, DEFAULT_SIZE, DEFAULT_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getComponentUpdateButton())
					.addGap(2));
		}
		return componentSearchPanel;
	}

	private JButton getComponentUpdateButton() {
		if (componentUpdateButton == null) {
			componentUpdateButton = new JButton("検索");
		}
		return componentUpdateButton;
	}

	@Override
	public DelegateRenderer getDelegateRenderer() {
		return renderer;
	}

	@Override
	public Icon getIcon() {
		return null; // TODO
	}

	@Override
	public JComponent getTabComponent() {
		if (tabComponent == null) {
			tabComponent = new JPanel();
			GroupLayout layout = new GroupLayout(tabComponent);
			tabComponent.setLayout(layout);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getComponentSearchPanel(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
					.addComponent(getScrollPane()));

			layout.setHorizontalGroup(layout.createParallelGroup()
					.addComponent(getComponentSearchPanel(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
					.addComponent(getScrollPane()));
		}
		return tabComponent;
	}

	@Override
	public String getTabId() {
		return TAB_ID;
	}

	@Override
	public String getTitle() {
		if (listOwner != null) {
			return "@" + listOwner + "/" + slug;
		} else if (listInfo != null) {
			return listInfo.getFullName();
		} else {
			return "リスト";
		}
	}

	@Override
	public String getToolTip() {
		return "リスト";
	}

	@Override
	public void serialize() {
		super.serialize();
		if (listOwner == null) {
			configProperties.setLong(getPropertyPrefix() + ".listId", listId);
		} else {
			configProperties.setProperty(getPropertyPrefix() + ".listOwner", listOwner);
			configProperties.setProperty(getPropertyPrefix() + ".slug", slug);
		}
	}

	private synchronized void updateStream() {
		MessageBus messageBus = configuration.getMessageBus();
		if (lastEstablishedStreamPath != null) {
			messageBus.dissolve(accountId, lastEstablishedStreamPath, getRenderer());
			logger.debug("Disconnect bus {}", lastEstablishedStreamPath);
		}

		long[] listMembers;
		synchronized (listMemberSet) {
			listMembers = new long[listMemberSet.size()];
			int index = 0;
			for (User user : listMemberSet) {
				listMembers[index++] = user.getId();
			}
		}
		FilterQuery filterQuery = new FilterQuery(listMembers);
		String channelPath = FilterStreamChannel.getChannelPath(filterQuery);
		messageBus.establish(accountId, channelPath, getRenderer());
		lastEstablishedStreamPath = channelPath;
		logger.debug("Connect bus {}", channelPath);
		logger.trace("  IDs: {}", listMembers);
	}
}
