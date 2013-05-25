package jp.syuriken.snsw.twclient.handler;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.StatusData;
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
		StatusData statusData = arguments.getExtraObj(FavoriteActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA,
				StatusData.class);
		Status status = null;

		if (statusData == null) {
			status = arguments.getExtraObj("status", Status.class);
		} else {
			Object tag = statusData.tag;
			if (tag instanceof Status) {
				status = (Status) tag;
			}
		}

		return status;
	}

	protected void throwIllegalArgument() {
		throw new IllegalArgumentException(
				"Specify arg `status`(Status) or `" + ActionHandler.INTENT_ARG_NAME_SELECTING_POST_DATA
						+ "`(StatusPanel)");
	}
}
