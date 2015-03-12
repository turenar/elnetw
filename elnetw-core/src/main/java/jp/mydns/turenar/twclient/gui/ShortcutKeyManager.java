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

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import jp.mydns.turenar.twclient.ClientConfiguration;
import jp.mydns.turenar.twclient.ClientFrameApi;
import jp.mydns.turenar.twclient.intent.IntentArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ShortcutKey Manager. This class provides friendly interface for shortcut key handling
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public final class ShortcutKeyManager {
	private static final class ShortcutActionListener extends AbstractAction {
		private static final Logger logger = LoggerFactory.getLogger(ShortcutKeyManager.class);
		private final IntentArguments intent;
		private ClientFrameApi frameApi;

		public ShortcutActionListener(IntentArguments intent) {
			this.intent = intent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (frameApi == null) {
				frameApi = ClientConfiguration.getInstance().getFrameApi();
			}
			logger.debug("Shortcutkey invoke: {}", intent);
			frameApi.getSelectingTab().handleAction(intent.clone());
		}
	}

	private static String defaultComponentName;
	private static HashMap<String, InputMap> inputMapHashMap = new HashMap<>();
	private static HashMap<String, ActionMap> actionMapHashMap = new HashMap<>();
	private static HashMap<IntentArguments, KeyStroke> intentStrokeMap = new HashMap<>();

	/**
	 * add shortcutkey
	 *
	 * @param component component type
	 * @param stroke    key stroke
	 * @param intent    invoker
	 */
	public static void addShortcutKey(String component, KeyStroke stroke, IntentArguments intent) {
		InputMap inputMap = getInputMap(component);
		ActionMap actionMap = getActionMap(component);
		actionMap.put(intent, new ShortcutActionListener(intent));
		inputMap.put(stroke, intent);
		intentStrokeMap.put(intent, stroke);
	}

	private static ActionMap getActionMap(String component) {
		ActionMap actionMap = actionMapHashMap.get(component);
		if (actionMap == null) {
			actionMap = new ActionMap();
			actionMapHashMap.put(component, actionMap);
			if (!(defaultComponentName == null || component.equals(defaultComponentName))) {
				actionMap.setParent(getActionMap(defaultComponentName));
			}
		}

		return actionMap;
	}

	private static InputMap getInputMap(String component) {
		InputMap inputMap = inputMapHashMap.get(component);
		if (inputMap == null) {
			inputMap = new InputMap();
			inputMapHashMap.put(component, inputMap);
			if (!(defaultComponentName == null || component.equals(defaultComponentName))) {
				inputMap.setParent(getInputMap(defaultComponentName));
			}
		}
		return inputMap;
	}

	/**
	 * get KeyStroke instance from intent arguments
	 *
	 * @param intent intent
	 * @return key stroke. if not found, return null
	 */
	public static KeyStroke getKeyStrokeFromIntent(IntentArguments intent) {
		return intentStrokeMap.get(intent);
	}

	/**
	 * set default map name. This reflects only newly created map
	 *
	 * @param component default component type
	 */
	public static void setDefaultMap(String component) {
		defaultComponentName = component;
	}

	/**
	 * set key map for specified component
	 *
	 * @param componentType component type
	 * @param component     target component
	 */
	public static void setKeyMap(String componentType, JComponent component) {
		InputMap baseInputMap = getInputMap(componentType);
		InputMap inputMap = component.getInputMap();
		for (KeyStroke keyStroke : baseInputMap.allKeys()) {
			inputMap.put(keyStroke, baseInputMap.get(keyStroke));
		}
		ActionMap baseActionMap = getActionMap(componentType);
		ActionMap actionMap = component.getActionMap();
		for (Object actionKey : baseActionMap.allKeys()) {
			actionMap.put(actionKey, baseActionMap.get(actionKey));
		}
	}

	private ShortcutKeyManager() {
	}
}
