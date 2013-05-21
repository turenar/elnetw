package jp.syuriken.snsw.twclient.internal;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import static java.lang.Math.abs;

/**
 * y軸方向の慣性スクロールを実現させるためのクラス
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ScrollUtility {

	/**
	 * 孫コンポーネントの子コンポーネントにおける相対位置を親コンポーネントにおける絶対位置に変換するためのクラス。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public interface BoundsTranslator {

		/**
		 * 孫コンポーネントの子コンポーネントにおける相対位置を親コンポーネントにおける絶対位置に変換する
		 *
		 * @param component コンポーネント
		 * @return 絶対位置情報
		 */
		Rectangle translate(JComponent component);
	}

	private static final int MAX_SPEED = 100;

	private final JScrollPane scrollPane;

	private final BoundsTranslator translator;

	/*package*/ int deltaY;

	/*package*/ Timer scrollTimer;

	/*package*/ JComponent target;

	private boolean momemtumEnabled;

	/**
	 * インスタンスを生成する。
	 *
	 * @param scrollPane      スクロールペーン
	 * @param translator      位置情報を変換するクラス
	 * @param momemtumEnabled 慣性スクロールするかどうか
	 */
	public ScrollUtility(final JScrollPane scrollPane, BoundsTranslator translator, boolean momemtumEnabled) {
		this.scrollPane = scrollPane;
		this.translator = translator;
		this.momemtumEnabled = momemtumEnabled;
		if (momemtumEnabled) {
			scrollTimer = new Timer(10, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (isInside(target)) {
						deltaY = 0;
						scrollTimer.stop();
						return;
					}
					Point viewPosition = scrollPane.getViewport().getViewPosition();
					int vectorY = getVectorY(viewPosition);
					momemtumTranslate(vectorY);
					viewPosition.translate(0, deltaY);
					scrollPane.getViewport().setViewPosition(viewPosition);
				}
			});
		}
	}

	/**
	 * 指定したコンポーネントを表示するためのy距離を取得する
	 *
	 * @param viewPosition スクロールペーンの表示位置
	 * @return y距離
	 */
	protected int getVectorY(Point viewPosition) {
		Rectangle bounds = translator.translate(target);
		int vectorY;
		if (viewPosition.y > bounds.y) {
			vectorY = (bounds.y) - (viewPosition.y);
		} else {
			vectorY = (bounds.y + bounds.height) - (viewPosition.y + scrollPane.getViewport().getHeight());
		}
		return vectorY;
	}

	/**
	 * 指定されたコンポーネントが表示位置の中にあるかどうかを調べる
	 *
	 * @param component 調べるコンポーネント
	 * @return 表示位置の中にあるかどうか。
	 */
	/*package*/boolean isInside(JComponent component) {
		Point viewPosition = scrollPane.getViewport().getViewPosition();
		Rectangle bounds = translator.translate(component);
		return (viewPosition.y <= bounds.y && viewPosition.y + scrollPane.getViewport().getHeight() >= bounds.y
				+ bounds.height);
	}

	private void momemtumTranslate(int vectorY) {
		if (abs(vectorY) < 5) {
			deltaY = vectorY;
		} else if (abs(deltaY) * 2 > abs(vectorY)) { // will overscroll
			deltaY = deltaY / 2;
		} else {
			if (abs(vectorY) > MAX_SPEED) {
				vectorY = vectorY > 0 ? MAX_SPEED : -MAX_SPEED;
			}
			deltaY = (deltaY + vectorY) / 2;
			if (abs(deltaY) > MAX_SPEED) {
				deltaY = deltaY > 0 ? MAX_SPEED : -MAX_SPEED;
			}
		}
	}

	/**
	 * 指定されたコンポーネントが表示できるようにスクロールする。非同期で行われます。
	 *
	 * @param target スクロールの基準
	 * @return スクロールされるかどうか。
	 */
	public boolean scrollTo(final JComponent target) {
		if (momemtumEnabled) {
			this.target = target;
			scrollTimer.stop();
			if (isInside(target)) {
				return false;
			}
			scrollTimer.start();
			return true;
		} else {
			this.target = target;
			if (isInside(target)) {
				return false;
			}
			Point viewPosition = scrollPane.getViewport().getViewPosition();
			scrollPane.getViewport().setViewPosition(new Point(0, viewPosition.y + getVectorY(viewPosition)));
			return true;
		}
	}
}
