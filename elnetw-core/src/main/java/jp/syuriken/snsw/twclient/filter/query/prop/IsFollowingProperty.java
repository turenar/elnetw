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

package jp.syuriken.snsw.twclient.filter.query.prop;

import java.awt.Color;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientEventConstants;
import jp.syuriken.snsw.twclient.ClientMessageAdapter;
import jp.syuriken.snsw.twclient.bus.FollowingUsersChannel;
import jp.syuriken.snsw.twclient.filter.IllegalSyntaxException;
import jp.syuriken.snsw.twclient.filter.query.QueryController;
import jp.syuriken.snsw.twclient.gui.render.MessageRenderBase;
import twitter4j.DirectMessage;
import twitter4j.Status;

/**
 * IsFollowingProperty: provides is_following, which checks the user is following status/DM author.
 *
 * If the user is status/DM author, return true
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class IsFollowingProperty extends AbstractBoolArgProperty {
	protected class Dispatcher extends ClientMessageAdapter {
		@Override
		public void onClientMessage(String name, Object arg) {
			if (name.equals(FollowingUsersChannel.FOLLOWING_USERS_FETCHED)) {
				followingUsersQuery = (FollowingUsersChannel.FollowingUsersQuery) arg;
				controller.disableDelay(IsFollowingProperty.this);
				hideFetchingFollowing();
			}
		}
	}

	private static final String FILTER_FOLLOWING_USER_WAIT_MESSAGE = "!filter/query/IsFollowing/waitMessage";
	protected final QueryController controller;
	private volatile FollowingUsersChannel.FollowingUsersQuery followingUsersQuery;

	/**
	 * make instance
	 *
	 * @param controller query controller instance
	 * @param name       prop name
	 * @param operator   prop operator
	 * @param value      prop value
	 * @throws IllegalSyntaxException illegal arguments
	 */
	public IsFollowingProperty(QueryController controller, String name,
			String operator, Object value) throws IllegalSyntaxException {
		super(name, operator, value);
		this.controller = controller;
		ClientConfiguration.getInstance().getMessageBus()
				.establish(controller.getTargetUserId(), "users/following", new Dispatcher());
	}

	@Override
	protected boolean getPropertyValue(Status status) {
		return followingUsersQuery
				.isFollowing((status.isRetweet() ? status.getRetweetedStatus() : status).getUser().getId());
	}

	@Override
	protected boolean getPropertyValue(DirectMessage directMessage) {
		return followingUsersQuery.isFollowing(directMessage.getSenderId());
	}

	/**
	 * フォロー中ユーザーの取得中メッセージを非表示にする
	 */
	protected void hideFetchingFollowing() {
		controller.onClientMessage(ClientEventConstants.RENDER_DELETE_OBJECT, FILTER_FOLLOWING_USER_WAIT_MESSAGE);
	}

	@Override
	public void init() {
		controller.enableDelay(this);
		showFetchingFollowing();
	}

	/**
	 * フォロー中ユーザーの取得中メッセージを表示
	 */
	protected void showFetchingFollowing() {
		MessageRenderBase renderBase = new MessageRenderBase(null)
				.setBackgroundColor(Color.GRAY)
				.setCreatedById("!filter/query/IsFollowing")
				.setCreatedByText("is_following")
				.setText("フォロー中のユーザーを取得中です。しばらくお待ちください。")
				.setUniqId(FILTER_FOLLOWING_USER_WAIT_MESSAGE);
		controller.onClientMessage(ClientEventConstants.RENDER_SHOW_OBJECT, renderBase);
	}
}
