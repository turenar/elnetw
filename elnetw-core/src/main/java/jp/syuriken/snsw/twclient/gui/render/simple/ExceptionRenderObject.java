package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Color;
import java.util.Date;

import jp.syuriken.snsw.twclient.Utility;
import twitter4j.TwitterException;

import static jp.syuriken.snsw.twclient.ClientFrameApi.DO_NOTHING_WHEN_POINTED;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/09/21
 * Time: 19:25
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ExceptionRenderObject extends AbstractRenderObject {

	private final long date;
	private final Exception ex;

	public ExceptionRenderObject(SimpleRenderer simpleRenderer, Exception ex) {
		super(simpleRenderer);
		this.ex = ex;
		date = System.currentTimeMillis();
		foregroundColor = Color.RED;
		backgroundColor = Color.BLACK;
		componentSentBy.setText("!ERROR!");
		String exString;
		if (ex instanceof TwitterException) {
			TwitterException twex = (TwitterException) ex;
			if (twex.isCausedByNetworkIssue()) {
				exString = twex.getCause().toString();
			} else {
				exString = twex.getStatusCode() + ": " + twex.getErrorMessage();
			}
		} else {
			exString = ex.toString();
		}
		if (exString.length() > 256) {
			exString = new StringBuilder().append(exString, 0, 254).append("..").toString();
		}
		componentStatusText.setText(exString);
	}

	@Override
	public Object getBasedObject() {
		return ex;
	}

	@Override
	public String getCreatedBy() {
		return "!exception/" + ex.getClass().getName();
	}

	@Override
	public Date getDate() {
		return new Date(date);
	}

	@Override
	public String getUniqId() {
		return "!exception/" + ex.getClass().getName() + "/" + date;
	}

	@Override
	protected void initComponents() {
		Throwable handlingException = ex;
		StringBuilder stringBuilder = new StringBuilder().append(ex.getLocalizedMessage()).append("<br><br>");
		while (null != (handlingException = handlingException.getCause())) {
			stringBuilder.append("Caused by ").append(handlingException.toString()).append("<br>");
		}
		StringBuilder escaped = escapeHTML(stringBuilder);
		frameApi.clearTweetView();
		frameApi.setTweetViewText(escaped.toString(), null, DO_NOTHING_WHEN_POINTED);
		frameApi.setTweetViewCreatedAt(Utility.getDateFormat().format(date), null,
				DO_NOTHING_WHEN_POINTED);
		frameApi.setTweetViewCreatedBy(componentUserIcon.getIcon(), ex.getClass().getName(), null,
				DO_NOTHING_WHEN_POINTED);
	}

	@Override
	public void requestCopyToClipboard() {
	}
}
