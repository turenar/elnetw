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

package jp.mydns.turenar.twclient.filter.query.prop;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientMessageAdapter;
import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.channel.FollowingUsersChannel;
import jp.mydns.turenar.twclient.filter.IllegalSyntaxException;
import jp.mydns.turenar.twclient.filter.query.QueryController;
import jp.mydns.turenar.twclient.filter.query.QueryOperator;
import jp.mydns.turenar.twclient.filter.query.QueryProperty;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserList;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * リストに入っているかどかを用いてフィルタする
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class InListProperty implements QueryProperty {
	private class ListMemberDispatcher extends ClientMessageAdapter {
		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
			userIdsFollowedByList.add(addedMember.getId());
		}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
			userIdsFollowedByList.remove(deletedMember.getId());
		}

		@Override
		public void onClientMessage(String name, Object arg) {
			if (name.equals(FollowingUsersChannel.FOLLOWING_USERS_FETCHED)) {
				controller.disableDelay(InListProperty.this);
			}
		}
	}

	private final QueryController controller;

	/** 設定 */
	protected ClientConfiguration configuration;
	private boolean isEqual;
	/** (:&lt;listId&gt;|&lt;listName&gt;|@&lt;owner&gt;/&lt;listName&gt;) */
	protected String listIdentifier;
	/** リストでフォローされているユーザーIDの配列 (ソート済み) */
	protected LongHashSet userIdsFollowedByList = new LongHashSet();

	/**
	 * インスタンスを生成する。
	 *
	 * @param operatorStr 演算子
	 * @param value       値 (文字列)
	 * @throws IllegalSyntaxException 正しくないarg
	 */
	public InListProperty(QueryController controller,String operatorStr, Object value)
			throws IllegalSyntaxException {
		this.controller = controller;
		this.configuration = ClientConfiguration.getInstance();
		if (operatorStr == null || value == null) {
			throw new IllegalSyntaxException(tr("[in_list] Could not omit operator and value"));
		}
		if (!(value instanceof String)) {
			throw new IllegalSyntaxException(tr("[in_list] value must be String"));
		}
		isEqual = QueryOperator.compileOperatorString(operatorStr) == QueryOperator.EQ;

		listIdentifier = value.toString();

		controller.enableDelay(this);
		configuration.getMessageBus().establish(MessageBus.READER_ACCOUNT_ID, "lists/members?"+value,
				new ListMemberDispatcher());
	}

	@Override
	public boolean filter(DirectMessage directMessage) {
		return isEqual == (userIdsFollowedByList.contains(directMessage.getSenderId())
				|| userIdsFollowedByList.contains(directMessage.getRecipientId()));
	}

	@Override
	public boolean filter(Status status) {
		return isEqual == userIdsFollowedByList.contains(status.getUser().getId());
	}

	@Override
	public void init() {
	}
}
