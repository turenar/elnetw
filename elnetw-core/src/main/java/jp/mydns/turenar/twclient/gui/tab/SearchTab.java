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

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jp.mydns.turenar.twclient.bus.channel.FilterStreamChannel;
import jp.mydns.turenar.twclient.gui.render.RenderTarget;
import twitter4j.FilterQuery;
import twitter4j.Status;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement;

/**
 * search tab
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SearchTab extends AbstractClientTab implements RenderTarget {
	private static final String TAB_ID = "search";
	private final String searchQuery;
	private DelegateRenderer renderer = new DelegateRenderer() {
		@Override
		public void onException(Exception ex) {
			actualRenderer.onException(ex);
		}

		@Override
		public void onStatus(Status status) {
			actualRenderer.onStatus(status);
		}
	};
	private JPanel tabComponent;
	private JPanel componentSearchPanel;
	private JLabel componentSearchLabel;
	private JTextField componentSearchArea;
	private JButton componentUpdateButton;

	/**
	 * インスタンスを生成する。
	 *
	 * @param accountId   account id
	 * @param searchQuery query
	 */
	public SearchTab(String accountId, String searchQuery) {
		super(accountId);
		this.searchQuery = searchQuery;
		establishChannel();
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param uniqId unique identifier
	 */
	public SearchTab(String uniqId) {
		super(TAB_ID, uniqId);
		searchQuery = configProperties.getProperty(getPropertyPrefix() + ".searchQuery");
		establishChannel();
	}

	protected void establishChannel() {
		configuration.getMessageBus().establish(accountId, "search?" + searchQuery, getRenderer());

		String[] streamTrackKeywords = searchQuery.split(" OR ");
		FilterQuery filterQuery = new FilterQuery().track(streamTrackKeywords);
		String channelPath = FilterStreamChannel.getChannelPath(filterQuery);
		configuration.getMessageBus().establish(accountId, channelPath, getRenderer());
	}

	private JTextField getComponentSearchField() {
		if (componentSearchArea == null) {
			componentSearchArea = new JTextField(searchQuery);
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
					.addComponent(getComponentSearchField(), 128, 256, DEFAULT_SIZE)
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
		return "検索";
	}

	@Override
	public String getToolTip() {
		return "検索";
	}

	@Override
	protected String getTwitterUrl() {
		try {
			return new URI("https", "twitter.com", "search", searchQuery, null).toASCIIString();
		} catch (URISyntaxException e) {
			throw new AssertionError();
		}
	}

	@Override
	public void serialize() {
		super.serialize();
		configProperties.setProperty(getPropertyPrefix() + ".searchQuery", searchQuery);
	}
}
