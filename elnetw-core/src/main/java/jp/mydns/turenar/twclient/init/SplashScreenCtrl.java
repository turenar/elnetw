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

package jp.mydns.turenar.twclient.init;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

/**
 * Splash Screen Controller
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SplashScreenCtrl {
	private static final int SS_PROGRESS_X = 2;
	private static final int SS_PROGRESS_Y = 120;
	private static final int SS_PROGRESS_WIDTH = 422;
	private static final int SS_PROGRESS_HEIGHT = 20;
	private static final int SS_WORKING_X = 4;
	private static final int SS_WORKING_Y = 130;
	private static final int PERCENTAGE = 100;
	private static SplashScreen splashScreen;

	static {
		splashScreen = SplashScreen.getSplashScreen();
	}

	private static int progress;

	/**
	 * set progress
	 *
	 * @param working working/worked count
	 * @param total   total work count
	 */
	public static void setProgress(int working, int total) {
		progress = working * PERCENTAGE / total;
	}

	/**
	 * show string to splash screen
	 *
	 * @param s str
	 */
	public static void setString(String s) {
		if (splashScreen == null) {
			return;
		}
		if (splashScreen.isVisible()) {
			Graphics2D graphics = splashScreen.createGraphics();
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.fillRect(SS_PROGRESS_X, SS_PROGRESS_Y, SS_PROGRESS_WIDTH, SS_PROGRESS_HEIGHT);
			graphics.setColor(Color.GREEN.brighter());
			graphics.fillRect(SS_PROGRESS_X, SS_PROGRESS_Y, SS_PROGRESS_WIDTH * progress / PERCENTAGE, SS_PROGRESS_HEIGHT);
			graphics.setColor(Color.BLACK);
			graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
			graphics.drawString(s, SS_WORKING_X, SS_WORKING_Y);
			splashScreen.update();
		}
	}

	private SplashScreenCtrl() {
	}
}
