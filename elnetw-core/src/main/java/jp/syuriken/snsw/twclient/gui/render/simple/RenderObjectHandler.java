package jp.syuriken.snsw.twclient.gui.render.simple;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jp.syuriken.snsw.twclient.ActionHandler;
import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.handler.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "core!*" アクションハンドラ
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class RenderObjectHandler implements ActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(RenderObjectHandler.class);

	@Override
	public JMenuItem createJMenuItem(IntentArguments args) {
		return null;
	}

	@Override
	public void handleAction(IntentArguments args) {
	}

	@Override
	public void popupMenuWillBecomeVisible(JMenuItem menuItem, IntentArguments args) {
		String arg = args.getExtraObj("_arg", String.class);
		if ("submenu".equals(arg)) {
			if (!(menuItem instanceof JMenu)) {
				logger.error("\"<elnetw>.gui.render.simple.RenderObjectHandler!submenu\" argued menuItem not as JMenu");
				throw new AssertionError();
			}
			Component[] subItems = ((JMenu) menuItem).getMenuComponents();
			for (Component subItem : subItems) {
				if (subItem instanceof JMenuItem) {
					JMenuItem subMenuItem = (JMenuItem) subItem;
					String actionCommand = subMenuItem.getActionCommand();
					AbstractRenderObject renderObject = args.getExtraObj(INTENT_ARG_NAME_SELECTING_POST_DATA,
							AbstractRenderObject.class);
					IntentArguments intentArguments = renderObject.getIntentArguments(actionCommand);
					ClientConfiguration.getInstance().getActionHandler(intentArguments).popupMenuWillBecomeVisible(
							subMenuItem,
							intentArguments);
				}
			}
		}
	}
}
