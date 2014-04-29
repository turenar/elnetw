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
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
