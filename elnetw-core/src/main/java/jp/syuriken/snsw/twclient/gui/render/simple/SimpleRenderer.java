package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.CacheManager;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;
import jp.syuriken.snsw.twclient.ImageCacher;
import jp.syuriken.snsw.twclient.TwitterStatus;
import jp.syuriken.snsw.twclient.gui.ImageResource;
import jp.syuriken.snsw.twclient.gui.TabRenderer;
import jp.syuriken.snsw.twclient.gui.render.RenderObject;
import jp.syuriken.snsw.twclient.gui.render.RenderTarget;
import jp.syuriken.snsw.twclient.gui.render.RendererFocusEvent;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/08/31
 * Time: 18:48
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SimpleRenderer implements TabRenderer {
	private static final Logger logger = LoggerFactory.getLogger(SimpleRenderer.class);
	private final long actualUserId;
	private final RenderTarget renderTarget;
	private final JPopupMenu popupMenu;
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

	public String getUserId() {
		return userId;
	}

	public ClientConfiguration getConfiguration() {
		return configuration;
	}

	public ClientProperties getConfigProperties() {
		return configProperties;
	}

	public SimpleRenderer(String userId, RenderTarget target, ActionListener actionListener) {
		this.userId = userId;
		configuration = ClientConfiguration.getInstance();
		this.actualUserId = configuration.getMessageBus().getActualUser(userId);
		this.renderTarget = target;
		configProperties = configuration.getConfigProperties();
		this.popupMenu = generatePopupMenu(actionListener);

		cacheManager = configuration.getCacheManager();
		imageCacher = configuration.getImageCacher();
		uiFont = configProperties.getFont("gui.font.ui");
		defaultFont = configProperties.getFont("gui.font.default");
		fontMetrics = new JLabel().getFontMetrics(defaultFont);
		int str12width = fontMetrics.stringWidth("0123456789abc");
		fontHeight = fontMetrics.getHeight();
		int height = Math.max(18, fontHeight);
		linePanelSizeOfSentBy = new Dimension(str12width, height);
		iconSize = new Dimension(64, height);
	}

	public void fireFocusEvent(FocusEvent e, RenderObject renderObject) {
		renderTarget.focusGained(new RendererFocusEvent(e, renderObject));
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

	protected Font getDefaultFont() {
		return defaultFont;
	}

	protected int getFontHeight() {
		return fontHeight;
	}

	protected FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	protected Dimension getIconSize() {
		return iconSize;
	}

	public ImageCacher getImageCacher() {
		return imageCacher;
	}

	protected Dimension getLinePanelSizeOfSentBy() {
		return linePanelSizeOfSentBy;
	}

	protected JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	protected RenderTarget getTarget() {
		return renderTarget;
	}

	protected Font getUiFont() {
		return uiFont;
	}

	@Override
	public void onBlock(User source, User blockedUser) {

	}

	@Override
	public void onChangeAccount(boolean forWrite) {
		renderTarget.addStatus(new MiscRenderObject(this, null)
				.setBackgroundColor(Color.LIGHT_GRAY)
				.setForegroundColor(Color.BLACK)
				.setCreatedByText(ClientConfiguration.APPLICATION_NAME)
				.setCreatedBy(
						forWrite ? "!core.change.account!write" : "!core.change.account!read")
				.setText(forWrite ? "書き込み用アカウントを変更しました。" : "読み込み用アカウントを変更しました。"));
	}

	@Override
	public void onCleanUp() {
	}

	@Override
	public void onClientMessage(String name, Object arg) {
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
				.setCreatedByText(user.getScreenName(), MiscRenderObject.getCreatedByLongText(cachedStatus))
				.setCreatedBy(user.getScreenName())
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
	public void onException(Exception ex) {
		renderTarget.addStatus(new ExceptionRenderObject(this, ex));
	}

	@Override
	public void onFavorite(User source, User target, Status favoritedStatus) {
		if (target.getId() == actualUserId) {
			renderTarget.addStatus(new MiscRenderObject(this, new Object[]{"fav", source, target, favoritedStatus})
					.setBackgroundColor(Color.GRAY)
					.setForegroundColor(Color.YELLOW)
					.setCreatedBy(source.getScreenName())
					.setIcon(source)
					.setUniqId(
							"!fav/" + source.getScreenName() + "/" + target.getScreenName() + "/" + favoritedStatus.getId())
					.setText("ふぁぼられました: \"" + favoritedStatus.getText() + "\""));
		}
		if (source.getId() == actualUserId) {
			Status cachedStatus = cacheManager.getCachedStatus(favoritedStatus.getId());
			if (cachedStatus instanceof TwitterStatus) {
				((TwitterStatus) cachedStatus).update(favoritedStatus);
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
					.setCreatedBy(source.getScreenName())
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
		renderTarget.addStatus(new StatusRenderObject(actualUserId, status, this));
	}

	@Override
	public void onDisplayRequirement() {
		if (configProperties.getBoolean("core.elnetw.danger_zone") &&
				configProperties.getBoolean("gui.danger.displayReq.disable")) {
			return;
		}

		renderTarget.addStatus(new MiscRenderObject(this, "DisplayRequirements")
				.setBackgroundColor(Color.DARK_GRAY)
				.setForegroundColor(Color.WHITE)
				.setCreatedBy("elnetw")
				.setText("All data is from twitter")
				.setIcon(ImageResource.getImgTwitterLogo())
				.setUniqId("misc/displayRequirements")
				.setDate(0x7fffffff_ffffffffL));
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
					.setIcon(source)
					.setText("ふぁぼやめられました: \"" + unfavoritedStatus.getText() + "\"")
					.setUniqId(
							"!unfav/" + source.getScreenName() + "/" + target.getScreenName() + "/" + unfavoritedStatus.getId()));
		} else if (source.getId() == actualUserId) {
			Status status = cacheManager.getCachedStatus(unfavoritedStatus.getId());
			if (status instanceof TwitterStatus) {
				((TwitterStatus) status).update(unfavoritedStatus);
			}
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
}
