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

package jp.mydns.turenar.twclient.notifier;

import java.awt.GraphicsEnvironment;
import java.awt.TrayIcon;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.ParallelRunnable;

/**
 * TrayIconを使用して通知する。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TrayIconMessageNotifier implements MessageNotifier, ParallelRunnable {

	/**
	 * check is usable notifier for this environment and get notifier instance
	 *
	 * @return if usable, return valid instance. if not, return null
	 */
	public static TrayIconMessageNotifier getInstance() {
		return GraphicsEnvironment.isHeadless() ? null : new TrayIconMessageNotifier();
	}

	private final ClientConfiguration configuration;

	private TrayIcon trayIcon;

	private LinkedList<Object[]> queue = new LinkedList<>();

	private long lastNotified;

	/**
	 * インスタンスを生成する。
	 */
	private TrayIconMessageNotifier() {
		this.configuration = ClientConfiguration.getInstance();
		trayIcon = configuration.getTrayIcon();
	}

	@Override
	public void run() {
		synchronized (queue) {
			long tempTime = lastNotified + 5000; //TODO 5000 from configure
			if (tempTime > System.currentTimeMillis()) {

				configuration.getTimer().schedule(this,
						tempTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
