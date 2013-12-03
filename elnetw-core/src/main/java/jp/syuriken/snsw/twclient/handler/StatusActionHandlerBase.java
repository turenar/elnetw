package jp.syuriken.snsw.twclient.handler;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.gui.render.RenderPanel;
import twitter4j.Status;

public abstract class StatusActionHandlerBase implements ActionHandler {

	protected final ClientConfiguration configuration;

	public StatusActionHandlerBase() {
		configuration = ClientConfiguration.getInstance();
	}

	protected long getLoginUserId() {
		return Long.parseLong(configuration.getAccountIdForRead());
	}

	protected Status getStatus(IntentArguments arguments) throws IllegalArgumentException {
		RenderPanel renderPanel = arguments.getExtraObj(FavoriteActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA,
				RenderPanel.class);
		Status status = null;

		if (renderPanel == null) {
			status = arguments.getExtraObj("status", Status.class);
		} else {
			Object tag = renderPanel.getRenderObject().getBasedObject();
			if (tag instanceof Status) {
				status = (Status) tag;
			}
		}

		return status;
	}

	protected void throwIllegalArgument() {
		throw new IllegalArgumentException(
				"Specify arg `status`(Status) or `" + ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA
						+ "`(RenderPanel)");
	}
}
