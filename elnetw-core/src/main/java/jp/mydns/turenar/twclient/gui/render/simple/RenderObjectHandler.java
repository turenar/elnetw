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

package jp.mydns.turenar.twclient.gui.render.simple;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jp.mydns.turenar.twclient.ActionHandler;
import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.handler.IntentArguments;
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
