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

package jp.mydns.turenar.twclient.filter.delayed;

import java.net.URI;
import java.util.Date;

import jp.mydns.turenar.twclient.impl.ClientMessageHandler;
import jp.mydns.turenar.twclient.internal.NullStatus;
import jp.mydns.turenar.twclient.internal.NullUser;
import org.junit.Test;
import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;

public class DelayedFilterTest {
	private static class DelayedFilterImpl extends DelayedFilter {

		@Override
		protected boolean filterUser(long userId) {
			return false;
		}
	}

	private static class NullDeletionNotice implements StatusDeletionNotice {
		public static final NullDeletionNotice INSTANCE = new NullDeletionNotice();

		@Override
		public int compareTo(StatusDeletionNotice o) {
			return 0;
		}

		@Override
		public long getStatusId() {
			return 0;
		}

		@Override
		public long getUserId() {
			return 0;
		}
	}

	private static class NullDirectMessage implements DirectMessage {
		public static final DirectMessage INSTANCE = new NullDirectMessage();

		@Override
		public int getAccessLevel() {
			return 0;
		}

		@Override
		public Date getCreatedAt() {
			return null;
		}

		@Override
		public MediaEntity[] getExtendedMediaEntities() {
			return new MediaEntity[0];
		}

		@Override
		public HashtagEntity[] getHashtagEntities() {
			return new HashtagEntity[0];
		}

		@Override
		public long getId() {
			return 0;
		}

		@Override
		public MediaEntity[] getMediaEntities() {
			return new MediaEntity[0];
		}

		@Override
		public RateLimitStatus getRateLimitStatus() {
			return null;
		}

		@Override
		public User getRecipient() {
			return NullUser.INSTANCE;
		}

		@Override
		public long getRecipientId() {
			return 0;
		}

		@Override
		public String getRecipientScreenName() {
			return null;
		}

		@Override
		public User getSender() {
			return NullUser.INSTANCE;
		}

		@Override
		public long getSenderId() {
			return 0;
		}

		@Override
		public String getSenderScreenName() {
			return null;
		}

		@Override
		public SymbolEntity[] getSymbolEntities() {
			return new SymbolEntity[0];
		}

		@Override
		public String getText() {
			return null;
		}

		@Override
		public URLEntity[] getURLEntities() {
			return new URLEntity[0];
		}

		@Override
		public UserMentionEntity[] getUserMentionEntities() {
			return new UserMentionEntity[0];
		}
	}

	private static class NullStatusImpl extends NullStatus {
		public static final Status INSTANCE = new NullStatusImpl();

		@Override
		public User getUser() {
			return NullUser.INSTANCE;
		}
	}

	private static class NullUserList implements UserList {
		public static final NullUserList INSTANCE = new NullUserList();

		@Override
		public int compareTo(UserList o) {
			return 0;
		}

		@Override
		public int getAccessLevel() {
			return 0;
		}

		@Override
		public Date getCreatedAt() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getFullName() {
			return null;
		}

		@Override
		public long getId() {
			return 0;
		}

		@Override
		public int getMemberCount() {
			return 0;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public RateLimitStatus getRateLimitStatus() {
			return null;
		}

		@Override
		public String getSlug() {
			return null;
		}

		@Override
		public int getSubscriberCount() {
			return 0;
		}

		@Override
		public URI getURI() {
			return null;
		}

		@Override
		public User getUser() {
			return null;
		}

		@Override
		public boolean isFollowing() {
			return false;
		}

		@Override
		public boolean isPublic() {
			return false;
		}
	}

	@Test
	public void testClone() throws Exception {

	}

	@Test
	public void testOnBlock() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onBlock(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onBlock");
		handler.testNotCalled();
		filter.startDelay();
		filter.onBlock(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onBlock(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onBlock");
		handler.testCalled("onBlock");
		handler.testNotCalled();
	}

	@Test
	public void testOnDeletionNotice() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onDeletionNotice(NullDeletionNotice.INSTANCE);
		handler.testCalled("onDeletionNotice");
		handler.testNotCalled();
		filter.startDelay();
		filter.onDeletionNotice(NullDeletionNotice.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onDeletionNotice(NullDeletionNotice.INSTANCE);
		handler.testCalled("onDeletionNotice");
		handler.testCalled("onDeletionNotice");
		handler.testNotCalled();
	}

	@Test
	public void testOnDeletionNotice1() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onDeletionNotice(-1L, -1L);
		handler.testCalled("onDeletionNotice");
		handler.testNotCalled();
		filter.startDelay();
		filter.onDeletionNotice(-1L, -1L);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onDeletionNotice(-1L, -1L);
		handler.testCalled("onDeletionNotice");
		handler.testCalled("onDeletionNotice");
		handler.testNotCalled();
	}

	@Test
	public void testOnDirectMessage() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onDirectMessage(NullDirectMessage.INSTANCE);
		handler.testCalled("onDirectMessage");
		handler.testNotCalled();
		filter.startDelay();
		filter.onDirectMessage(NullDirectMessage.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onDirectMessage(NullDirectMessage.INSTANCE);
		handler.testCalled("onDirectMessage");
		handler.testCalled("onDirectMessage");
		handler.testNotCalled();
	}

	@Test
	public void testOnFavorite() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onFavorite(NullUser.INSTANCE, NullUser.INSTANCE, NullStatusImpl.INSTANCE);
		handler.testCalled("onFavorite");
		handler.testNotCalled();
		filter.startDelay();
		filter.onFavorite(NullUser.INSTANCE, NullUser.INSTANCE, NullStatusImpl.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onFavorite(NullUser.INSTANCE, NullUser.INSTANCE, NullStatusImpl.INSTANCE);
		handler.testCalled("onFavorite");
		handler.testCalled("onFavorite");
		handler.testNotCalled();
	}

	@Test
	public void testOnFollow() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onFollow(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onFollow");
		handler.testNotCalled();
		filter.startDelay();
		filter.onFollow(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onFollow(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onFollow");
		handler.testCalled("onFollow");
		handler.testNotCalled();
	}

	@Test
	public void testOnScrubGeo() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onScrubGeo(-1L, -1L);
		handler.testCalled("onScrubGeo");
		handler.testNotCalled();
		filter.startDelay();
		filter.onScrubGeo(-1L, -1L);
		handler.testCalled("onScrubGeo");
		handler.testNotCalled();
		filter.stopDelay();
		filter.onScrubGeo(-1L, -1L);
		handler.testCalled("onScrubGeo");
		handler.testNotCalled();
	}

	@Test
	public void testOnStatus() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onStatus(NullStatusImpl.INSTANCE);
		handler.testCalled("onStatus");
		handler.testNotCalled();
		filter.startDelay();
		filter.onStatus(NullStatusImpl.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onStatus(NullStatusImpl.INSTANCE);
		handler.testCalled("onStatus");
		handler.testCalled("onStatus");
		handler.testNotCalled();
	}

	@Test
	public void testOnUnblock() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUnblock(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onUnblock");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUnblock(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUnblock(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onUnblock");
		handler.testCalled("onUnblock");
		handler.testNotCalled();
	}

	@Test
	public void testOnUnfavorite() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUnfavorite(NullUser.INSTANCE, NullUser.INSTANCE, NullStatusImpl.INSTANCE);
		handler.testCalled("onUnfavorite");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUnfavorite(NullUser.INSTANCE, NullUser.INSTANCE, NullStatusImpl.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUnfavorite(NullUser.INSTANCE, NullUser.INSTANCE, NullStatusImpl.INSTANCE);
		handler.testCalled("onUnfavorite");
		handler.testCalled("onUnfavorite");
		handler.testNotCalled();
	}

	@Test
	public void testOnUnfollow() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUnfollow(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onUnfollow");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUnfollow(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUnfollow(NullUser.INSTANCE, NullUser.INSTANCE);
		handler.testCalled("onUnfollow");
		handler.testCalled("onUnfollow");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListCreation() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListCreation(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListCreation");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListCreation(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListCreation(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListCreation");
		handler.testCalled("onUserListCreation");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListDeletion() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListDeletion(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListDeletion");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListDeletion(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListDeletion(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListDeletion");
		handler.testCalled("onUserListDeletion");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListMemberAddition() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListMemberAddition(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListMemberAddition");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListMemberAddition(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListMemberAddition(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListMemberAddition");
		handler.testCalled("onUserListMemberAddition");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListMemberDeletion() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListMemberDeletion(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListMemberDeletion");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListMemberDeletion(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListMemberDeletion(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListMemberDeletion");
		handler.testCalled("onUserListMemberDeletion");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListSubscription() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListSubscription(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListSubscription");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListSubscription(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListSubscription(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListSubscription");
		handler.testCalled("onUserListSubscription");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListUnsubscription() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListUnsubscription(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListUnsubscription");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListUnsubscription(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListUnsubscription(NullUser.INSTANCE, NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListUnsubscription");
		handler.testCalled("onUserListUnsubscription");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserListUpdate() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserListUpdate(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListUpdate");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserListUpdate(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserListUpdate(NullUser.INSTANCE, NullUserList.INSTANCE);
		handler.testCalled("onUserListUpdate");
		handler.testCalled("onUserListUpdate");
		handler.testNotCalled();
	}

	@Test
	public void testOnUserProfileUpdate() throws Exception {
		DelayedFilter filter = new DelayedFilterImpl();
		ClientMessageHandler handler = new ClientMessageHandler();
		filter.addChild(handler);
		filter.stopDelay();
		filter.onUserProfileUpdate(NullUser.INSTANCE);
		handler.testCalled("onUserProfileUpdate");
		handler.testNotCalled();
		filter.startDelay();
		filter.onUserProfileUpdate(NullUser.INSTANCE);
		handler.testNotCalled();
		filter.stopDelay();
		filter.onUserProfileUpdate(NullUser.INSTANCE);
		handler.testCalled("onUserProfileUpdate");
		handler.testCalled("onUserProfileUpdate");
		handler.testNotCalled();
	}
}