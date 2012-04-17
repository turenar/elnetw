package jp.syuriken.snsw.twclient.internal;

import static java.lang.Math.abs;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import jp.syuriken.snsw.twclient.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * y軸方向の慣性スクロールを実現させるためのクラス
 * 
 * @author $Author$
 */
public class MomemtumScroller {
	
	/**
	 * 孫コンポーネントの子コンポーネントにおける相対位置を親コンポーネントにおける絶対位置に変換するためのクラス。
	 * 
	 * @author $Author$
	 */
	public static interface BoundsTranslator {
		
		/**
		 * 孫コンポーネントの子コンポーネントにおける相対位置を親コンポーネントにおける絶対位置に変換する
		 * 
		 * @param component コンポーネント
		 * @return 絶対位置情報
		 */
		Rectangle translate(JComponent component);
	}
	
	
	private static final Logger logger = LoggerFactory.getLogger(MomemtumScroller.class);
	
	private final JScrollPane scrollPane;
	
	private int deltaY;
	
	private static final int MAX_SPEED = 100;
	
	private Timer scrollTimer;
	
	private JComponent target;
	
	private final BoundsTranslator translator;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param scrollPane スクロールペーン
	 * @param translator 位置情報を変換するクラス
	 */
	public MomemtumScroller(final JScrollPane scrollPane, BoundsTranslator translator) {
		this.scrollPane = scrollPane;
		this.translator = translator;
		scrollTimer = new Timer(10, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isInside(target)) {
					deltaY = 0;
					scrollTimer.stop();
					return;
				}
				Point viewPosition = scrollPane.getViewport().getViewPosition();
				Rectangle bounds = MomemtumScroller.this.translator.translate(target);
				int vectorY;
				if (viewPosition.y > bounds.y) {
					vectorY = (bounds.y) - (viewPosition.y);
				} else {
					vectorY = (bounds.y + bounds.height) - (viewPosition.y + scrollPane.getViewport().getHeight());
				}
				momemtumTranslate(vectorY);
				logger.debug("vp.y={}, bounds.y={}, vp.bottom={}, bounds.bottom={}", Utility.toArray(viewPosition.y,
						bounds.y, viewPosition.y + scrollPane.getViewport().getHeight(), bounds.y + bounds.height));
				logger.debug("vectorY={}, deltaY={}", vectorY, deltaY);
				viewPosition.translate(0, deltaY);
				scrollPane.getViewport().setViewPosition(viewPosition);
			}
		});
	}
	
	/**
	 * 指定されたコンポーネントが表示位置の中にあるかどうかを調べる
	 * 
	 * @param component 調べるコンポーネント
	 * @return 表示位置の中にあるかどうか。
	 */
	private boolean isInside(JComponent component) {
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
		this.target = target;
		scrollTimer.stop();
		if (isInside(target)) {
			return false;
		}
		scrollTimer.start();
		return true;
	}
}
