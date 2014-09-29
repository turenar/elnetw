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

package jp.mydns.turenar.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.FocusEvent;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import jp.mydns.turenar.lib.primitive.LongHashSet;
import jp.mydns.turenar.twclient.CacheManager;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.cache.ImageCacher;
import jp.mydns.turenar.twclient.conf.ClientProperties;
import jp.mydns.turenar.twclient.filter.MessageFilter;
import jp.mydns.turenar.twclient.gui.ImageResource;
import jp.mydns.turenar.twclient.gui.render.MessageRenderBase;
import jp.mydns.turenar.twclient.gui.render.RenderObject;
import jp.mydns.turenar.twclient.gui.render.RenderTarget;
import jp.mydns.turenar.twclient.gui.render.RendererFocusEvent;
import jp.mydns.turenar.twclient.gui.tab.TabRenderer;
import jp.mydns.turenar.twclient.twitter.TwitterStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

import static jp.mydns.turenar.twclient.ClientConfiguration.APPLICATION_NAME;

/**
 * default renderer. no skin, no decoration...
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SimpleRenderer implements TabRenderer {
	private static final Logger logger = LoggerFactory.getLogger(SimpleRenderer.class);
	private static final long DISPLAY_REQUIREMENT_PUBLISHED_DATE = 0x7fffffff_ffffffffL;
	/**
	 * minimum height for panel
	 */
	public static final int MIN_PANEL_HEIGHT = 18;
	/**
	 * icon width
	 */
	public static final int ICON_WIDTH = 64;
	private final RenderTarget renderTarget;
	private final Font uiFont;
	private final Font defaultFont;
	private final FontMetrics fontMetrics;
	private final Dimension linePanelSizeOfSentBy;
	private final int fontHeight;
	private final Dimension iconSize;
	private final CacheManager cacheManager;
	private final ImageCacher imageCacher;
	private final ClientConfiguration configuration;
	private final ClientProperties configProperties;
	private final String userId;
	private volatile long actualUserId;
	private AbstractRenderObject focusOwner;
	private LongHashSet statusSet = new LongHashSet();
	private DateFormatter dateFormatter = new DateFormatter();
	private PopupMenuGenerator popupMenuGenerator = new PopupMenuGenerator(this);

	/**
	 * init
	 *
	 * @param userId user id (virtual or real)
	 * @param target render target
	 */
	public SimpleRenderer(String userId, RenderTarget target) {
		this.userId = userId;
		configuration = ClientConfiguration.getInstance();
		getActualUserId();
		this.renderTarget = target;
		configProperties = configuration.getConfigProperties();
		configuration.getMessageBus().establish(userId, "core", this);

		cacheManager = configuration.getCacheManager();
		imageCacher = configuration.getImageCacher();
		uiFont = configProperties.getFont("gui.font.ui");
		defaultFont = configProperties.getFont("gui.font.default");
		fontMetrics = new JLabel().getFontMetrics(defaultFont);
		int str12width = fontMetrics.stringWidth("0123456789abc");
		fontHeight = fontMetrics.getHeight();
		int height = Math.max(MIN_PANEL_HEIGHT, fontHeight);
		linePanelSizeOfSentBy = new Dimension(str12width, height);
		iconSize = new Dimension(ICON_WIDTH, height);
	}

	@Override
	public void addChild(MessageFilter filter) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public final SimpleRenderer clone() throws CloneNotSupportedException { // CS-IGNORE
		throw new CloneNotSupportedException();
	}

	/**
	 * fire focus event
	 *
	 * @param e            event
	 * @param renderObject render object
	 */
	public void fireFocusEvent(FocusEvent e, RenderObject renderObject) {
		renderTarget.focusGained(new RendererFocusEvent(e, renderObject));
	}

	/**
	 * generate popup menu from popup menu kind
	 *
	 * @param popupMenuKind kind of popup menu. for example, 'status', 'default'
	 */
	public void generatePopupMenu(String popupMenuKind) {
		popupMenuGenerator.generatePopupMenu(popupMenuKind);
	}

	private long getActualUserId() {
		actualUserId = configuration.getMessageBus().getActualUser(userId);
		return actualUserId;
	}

	@Override
	public MessageFilter getChild() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * get ClientProperties instance
	 *
	 * @return ClientProperties
	 */
	public ClientProperties getConfigProperties() {
		return configProperties;
	}

	/**
	 * get configuration
	 *
	 * @return configuration
	 */
	public ClientConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * get default font
	 *
	 * @return default font
	 */
	protected Font getDefaultFont() {
		return defaultFont;
	}

	/**
	 * get focus owner of objects which this instance created
	 *
	 * @return focus owner
	 */
	public AbstractRenderObject getFocusOwner() {
		return focusOwner;
	}

	/**
	 * get font height
	 *
	 * @return font height
	 */
	protected int getFontHeight() {
		return fontHeight;
	}

	/**
	 * get font metrics
	 *
	 * @return font metrics
	 */
	protected FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	/**
	 * get icon size
	 *
	 * @return icon size
	 */
	protected Dimension getIconSize() {
		return iconSize;
	}

	/**
	 * get image cacher
	 *
	 * @return image cacher
	 */
	public ImageCacher getImageCacher() {
		return imageCacher;
	}

	/**
	 * get sentBy panel size
	 *
	 * @return sentBy panel size
	 */
	protected Dimension getLinePanelSizeOfSentBy() {
		return linePanelSizeOfSentBy;
	}

	/**
	 * get popup menu. You must call {@link #generatePopupMenu(String)}
	 * in {@link AbstractRenderObject#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)}
	 *
	 * @return popup menu
	 */
	public JPopupMenu getPopupMenu() {
		return popupMenuGenerator.getPopupMenu();
	}

	/**
	 * get render target
	 *
	 * @return render target
	 */
	protected RenderTarget getTarget() {
		return renderTarget;
	}

	/**
	 * get ui font
	 *
	 * @return ui font
	 */
	protected Font getUiFont() {
		return uiFont;
	}

	/**
	 * get user id (virtual or real)
	 *
	 * @return user id
	 */
	public String getUserId() {
		return userId;
	}

	@Override
	public void onBlock(User source, User blockedUser) {

	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		renderTarget.addStatus(new MiscRenderObject(this, null)
				.setBackgroundColor(Color.LIGHT_GRAY)
				.setForegroundColor(Color.BLACK)
				.setCreatedByText(APPLICATION_NAME)
				.setCreatedBy(forWrite ? "!core.change.account!write" : "!core.change.account!read")
				.setText(forWrite ? "書き込み用アカウントを変更しました。" : "読み込み用アカウントを変更しました。"));
	}

	@Override
	public void onCleanUp() {
	}

	@Override
	public void onClientMessage(String name, Object arg) {
		switch (name) {
			case TabRenderer.READER_ACCOUNT_CHANGED:
			case TabRenderer.WRITER_ACCOUNT_CHANGED:
				getActualUserId();
				break;
			case TabRenderer.RENDER_SHOW_OBJECT:
				renderTarget.addStatus(MiscRenderObject.getInstance(this, (MessageRenderBase) arg));
				break;
			case TabRenderer.RENDER_DELETE_OBJECT:
				renderTarget.removeStatus((String) arg);
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
		Status cachedStatus = cacheManager.getCachedStatus(statusDeletionNotice.getStatusId());

		if (cachedStatus == null) {
			return;
		}
		User user = cachedStatus.getUser();

		renderTarget.addStatus(new MiscRenderObject(this, statusDeletionNotice)
				.setBackgroundColor(Color.LIGHT_GRAY)
				.setForegroundColor(Color.RED)
				.setCreatedBy(user)
				.setUniqId("!twdel/" + statusDeletionNotice.getStatusId())
				.setText("DELETED: " + cachedStatus.getText())
				.setIcon(user));
	}

	@Override
	public void onDirectMessage(DirectMessage directMessage) {
		renderTarget.addStatus(new DirectMessageRenderObject(this, directMessage));
	}

	@Override
	public void onDisconnect() {
	}

	@Override
	public void onDisplayRequirement() {
		if (configProperties.getBoolean("core.elnetw.danger_zone")
				&& configProperties.getBoolean("gui.danger.displayReq.disable")) {
			return;
		}

		renderTarget.addStatus(new MiscRenderObject(this, "DisplayRequirements")
				.setBackgroundColor(Color.DARK_GRAY)
				.setForegroundColor(Color.WHITE)
				.setCreatedBy("!display_reuirements")
				.setCreatedByText("", APPLICATION_NAME)
				.setText("All data is from twitter")
				.setIcon(ImageResource.getImgTwitterLogo())
				.setUniqId("misc/displayRequirements")
				.setDate(DISPLAY_REQUIREMENT_PUBLISHED_DATE)
				.setPopupMenuType("dispReq"));
	}

	@Override
	public void onException(Exception ex) {
		renderTarget.addStatus(new ExceptionRenderObject(this, ex));
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (target.getId() == actualUserId) {
			renderTarget.addStatus(new MiscRenderObject(this, new Object[]{"fav", source, target, favoritedStatus})
					.setBackgroundColor(Color.GRAY)
					.setForegroundColor(Color.YELLOW)
					.setCreatedBy(source)
					.setIcon(source)
					.setUniqId(
							"!fav/" + source.getScreenName() + "/" + target.getScreenName() + "/" + favoritedStatus.getId())
					.setText("ふぁぼられました: \"" + favoritedStatus.getText() + "\""));
		}
		if (source.getId() == actualUserId) {
			TwitterStatus cachedStatus = cacheManager.getCachedStatus(favoritedStatus.getId());
			if (cachedStatus != null) {
				cachedStatus.update(favoritedStatus);
			}
		}
	}

	@Override
	public void onFollow(User source, User followedUser) {
		if (followedUser.getId() == actualUserId) {
			renderTarget.addStatus(new MiscRenderObject(this, new Object[]{"follow", source, followedUser})
					.setBackgroundColor(Color.GRAY)
					.setForegroundColor(Color.YELLOW)
					.setIcon(source)
					.setCreatedBy(source)
					.setUniqId("!follow/" + source.getScreenName() + "/" + followedUser.getScreenName())
					.setText("@" + followedUser.getScreenName() + " にフォローされました"));
		}
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
	public void onStatus(Status status) {
		if (statusSet.add(status.getId())) {
			renderTarget.addStatus(new StatusRenderObject(actualUserId, status, this));
		}
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	}

	@Override
	public void onUnblock(User source, User unblockedUser) {
	}

	@Override
	public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
		if (logger.isTraceEnabled()) {
			logger.trace("onUnFavorite: source={}, target={}, unfavoritedStatus={}", source, target, unfavoritedStatus);
		}
		if (target.getId() == actualUserId) {
			renderTarget.addStatus(new MiscRenderObject(this, new Object[]{"unfav", source, target, unfavoritedStatus})
					.setBackgroundColor(Color.GRAY)
					.setForegroundColor(Color.LIGHT_GRAY)
					.setCreatedBy(source)
					.setIcon(source)
					.setText("ふぁぼやめられました: \"" + unfavoritedStatus.getText() + "\"")
					.setUniqId(
							"!unfav/" + source.getScreenName() + "/" + target.getScreenName()
									+ "/" + unfavoritedStatus.getId()));
		} else if (source.getId() == actualUserId) {
			TwitterStatus status = cacheManager.getCachedStatus(unfavoritedStatus.getId());
			if (status != null) {
				status.update(unfavoritedStatus);
			}
		}
	}

	@Override
	public void onUnfollow(User source, User unfollowedUser) {
		if (unfollowedUser.getId() == actualUserId) {
			renderTarget.addStatus(new MiscRenderObject(this, new Object[]{"unfollow", source, unfollowedUser})
					.setBackgroundColor(Color.GRAY)
					.setForegroundColor(Color.YELLOW)
					.setIcon(source)
					.setCreatedBy(source)
					.setUniqId("!unfollow/" + source.getScreenName() + "/" + unfollowedUser.getScreenName())
					.setText("@" + unfollowedUser.getScreenName() + " にフォロー解除されました"));
		}
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

	@Override
	public void setChild(MessageFilter child) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * set focus owner
	 *
	 * @param focusOwner focus owner
	 */
	public void setFocusOwner(AbstractRenderObject focusOwner) {
		this.focusOwner = focusOwner;
	}

	/**
	 * converts createdAt into string
	 *
	 * @param createdAt create at
	 * @param html      insert html tag?
	 * @return date string
	 */
	public String toDateString(Date createdAt, boolean html) {
		return dateFormatter.toDateString(createdAt, html);
	}
}
