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

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.mydns.turenar.twclient.Utility;
import jp.mydns.turenar.twclient.gui.render.RenderPanel;
import jp.mydns.turenar.twclient.intent.Intent;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import jp.mydns.turenar.twclient.intent.PopupMenuDispatcher;
import jp.mydns.turenar.twclient.internal.IntentActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * popup menu generator
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PopupMenuGenerator implements PopupMenuDispatcher, PopupMenuListener {
	private class SubMenuDispatcher implements PopupMenuDispatcher {

		private final JMenu parent;

		public SubMenuDispatcher(JComponent owner, String title) {
			parent = new JMenu();
			Utility.setMnemonic(parent, title);
			owner.add(parent);
		}

		@Override
		public void addMenu(JMenuItem menu, IntentArguments intent) {
			if (menu == null || intent == null) {
				throw new NullPointerException();
			}
			menu.addActionListener(new IntentActionListener(intent));
			parent.add(menu);
		}

		@Override
		public PopupMenuDispatcher createSubMenu(String text) {
			return new SubMenuDispatcher(parent, text);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(PopupMenuGenerator.class);
	private final SimpleRenderer renderer;
	private final JPopupMenu popupMenu;
	private Stack<JComponent> stack = new Stack<>();

	/**
	 * create instance
	 *
	 * @param renderer renderer
	 */
	public PopupMenuGenerator(SimpleRenderer renderer) {
		this.renderer = renderer;
		popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(this);
	}

	@Override
	public void addMenu(JMenuItem menu, IntentArguments intent) {
		if (menu == null || intent == null) {
			throw new NullPointerException();
		}
		menu.addActionListener(new IntentActionListener(intent));
		stack.peek().add(menu);
	}

	@Override
	public PopupMenuDispatcher createSubMenu(String text) {
		return new SubMenuDispatcher(stack.peek(), text);
	}

	/**
	 * menuTypeからポップアップメニューを作成する
	 *
	 * @param menuType メニュータイプ
	 */
	protected void generatePopupMenu(String menuType) {
		popupMenu.removeAll();
		stack.clear();

		stack.push(popupMenu);
		String popupMenuStr = renderer.getConfigProperties().getProperty("gui.menu.popup." + menuType);

		Pattern pattern = Pattern.compile("([^;{}]+)([{}]?)");
		Matcher matcher = pattern.matcher(popupMenuStr);

		while (matcher.find()) {
			String commandName = matcher.group(1).trim();
			String subMenuStart = matcher.group(2);
			if (commandName.isEmpty()) {
				continue;
			}
			if (subMenuStart.equals("{")) {
				JMenu jMenu = new JMenu(commandName);
				stack.peek().add(jMenu);
				stack.push(jMenu);
			} else {
				IntentArguments intentArguments = getIntentArguments(commandName);
				Intent handler = renderer.getConfiguration().getIntent(intentArguments);
				if (handler == null) {
					logger.warn("intent {} is not found.", commandName);
				} else {
					handler.createJMenuItem(this, intentArguments);
				}
				if (subMenuStart.equals("}")) {
					stack.pop();
				}
			}
		}
		stack.pop();
		if (!stack.isEmpty()) {
			throw new AssertionError();
		}
	}

	/**
	 * Create IntentArguments
	 *
	 * @param actionCommand (name)[!(key)[=(value)][, ...]]
	 * @return IntentArguments
	 */
	protected IntentArguments getIntentArguments(String actionCommand) {
		IntentArguments intentArguments = Utility.getIntentArguments(actionCommand);
		intentArguments.putExtra(Intent.INTENT_ARG_NAME_SELECTING_POST_DATA, renderer.getFocusOwner());
		return intentArguments;
	}

	/**
	 * get popup menu
	 *
	 * @return popup
	 */
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		JPopupMenu source = (JPopupMenu) e.getSource();
		RenderPanel invoker = (RenderPanel) (source.getInvoker());
		AbstractRenderObject renderObject = (AbstractRenderObject) invoker.getRenderObject();
		renderObject.popupMenuWillBecomeVisible(e);
	}
}
