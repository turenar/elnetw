package jp.syuriken.snsw.twclient.internal;

import java.io.File;
import java.io.IOException;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.MessageNotifier;
import jp.syuriken.snsw.twclient.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * notify-sendを使用して通知を送信するクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class NotifySendMessageNotifier implements MessageNotifier {
	private static final Logger logger = LoggerFactory.getLogger(NotifySendMessageNotifier.class);

	public static boolean checkUsable(ClientConfiguration configuration) {
		if (Utility.getOstype() == Utility.OSType.OTHER) {
			try {
				if (Runtime.getRuntime().exec(new String[] {
						"which",
						"notify-send"
				}).waitFor() == 0) {
					return true;
				}
			} catch (InterruptedException e) {
				// do nothing
			} catch (IOException e) {
				logger.warn("#detectNotifier: whichの呼び出しに失敗", e);
			}
		}
		return false;
	}

	public NotifySendMessageNotifier(ClientConfiguration configuration) {
	}

	@Override
	public void sendNotify(String summary, String text, File imageFile) throws IOException {
		if (imageFile == null) {
			Runtime.getRuntime().exec(new String[] {
					"notify-send",
					summary,
					text
			});
		} else {
			Runtime.getRuntime().exec(new String[] {
					"notify-send",
					"-i",
					imageFile.getPath(),
					summary,
					text
			});
		}
	}
}
