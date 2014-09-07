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

package jp.mydns.turenar.twclient.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jp.mydns.turenar.twclient.Utility;

/**
 * JPanelに背景画像を描画する
 */
public class BackgroundImagePanel extends JPanel {
	/**
	 * 背景画像の上に見やすいように塗る色
	 */
	public static final Color IMAGE_OVERLAY_COLOR = new Color(0f, 0f, 0f, 0.3f);
	private static final long serialVersionUID = -3176015401705848989L;
	// 描画する画像
	private BufferedImage image;

	/**
	 * インスタンスの生成
	 *
	 * @param image 背景画像
	 */
	public BackgroundImagePanel(BufferedImage image) {
		this.image = image;
	}

	/**
	 * インスタンスの生成
	 */
	public BackgroundImagePanel() {
		this(null);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();

		Graphics2D g2D = (Graphics2D) g.create();
		if (image != null) {
			double imageWidth = image.getWidth();
			double imageHeight = image.getHeight();

			// 画像がコンポーネントの何倍の大きさか計算
			double sx = panelWidth / imageWidth;
			double sy = panelHeight / imageHeight;

			// スケーリング
			AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
			g2D.drawImage(image, af, this);


		}
		g2D.setColor(IMAGE_OVERLAY_COLOR);
		g2D.fillRect(0, 0, panelWidth, panelHeight);
		g2D.dispose();
	}

	/**
	 * 背景画像を設定する
	 *
	 * @param image 画像
	 * @throws InterruptedException 読み込まれる前に割り込まれた
	 */
	public void setBackgroundImage(Image image) throws InterruptedException {
		this.image = Utility.createBufferedImage(image, new MediaTracker(this));
	}
}
