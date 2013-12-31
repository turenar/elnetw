package jp.syuriken.snsw.twclient.internal;

import java.awt.GraphicsEnvironment;
import java.awt.TrayIcon;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue;
import jp.syuriken.snsw.twclient.MessageNotifier;
import jp.syuriken.snsw.twclient.ParallelRunnable;

/**
 * TrayIconを使用して通知する。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TrayIconMessageNotifier implements MessageNotifier, ParallelRunnable {

	public static boolean checkUsable(ClientConfiguration configuration) {
		return !GraphicsEnvironment.isHeadless();
	}

	private final ClientConfiguration configuration;

	private TrayIcon trayIcon;

	private LinkedList<Object[]> queue = new LinkedList<Object[]>();

	private long lastNotified;

	/**
	 * インスタンスを生成する。
	 *
	 * @param configuration 設定
	 */
	public TrayIconMessageNotifier(ClientConfiguration configuration) {
		this.configuration = configuration;
		trayIcon = configuration.getTrayIcon();
	}

	@Override
	public void run() {
		synchronized (queue) {
			long tempTime = lastNotified + 5000; //TODO 5000 from configure
			if (tempTime > System.currentTimeMillis()) {

				configuration.getTimer().schedule(new Runnable() {

					@Override
					public void run() {
						TrayIconMessageNotifier.this.run();
					}
				}, tempTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
				return;
			}
			Object[] arr = queue.poll();
			if (arr == null) {
				return;
			}
			String summary = (String) arr[0];
			String text = (String) arr[1];
			trayIcon.displayMessage(summary, text, TrayIcon.MessageType.INFO);
			lastNotified = System.currentTimeMillis();
			if (queue.size() > 0) {
				configuration.addJob(JobQueue.Priority.LOW, this);
			}
		}
	}

	@Override
	public void sendNotify(String summary, String text, File imageFile) {
		synchronized (queue) {
			queue.add(new Object[] {
					summary,
					text
			/*,imageFile*/});
			if (queue.size() == 1) {
				configuration.addJob(JobQueue.Priority.LOW, this);
			}
		}
	}
}
