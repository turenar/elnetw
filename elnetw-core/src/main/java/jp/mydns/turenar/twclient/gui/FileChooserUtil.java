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

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to use native File Chooser
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public abstract class FileChooserUtil {
	private static class FileChooserDelegateImpl extends FileChooserUtil {

		private final JFileChooser jFileChooser;

		public FileChooserDelegateImpl() {
			jFileChooser = new JFileChooser();
		}

		@Override
		public void addFilter(String description, String... extension) {
			jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(description, extension));
		}

		@Override
		public File openDialog(boolean save, Component parent) {
			int result;
			if (save) {
				result = jFileChooser.showSaveDialog(parent);
			} else {
				result = jFileChooser.showOpenDialog(parent);
			}
			return result == JFileChooser.APPROVE_OPTION ? jFileChooser.getSelectedFile() : null;
		}

		@Override
		public void setTitle(String title) {
			jFileChooser.setDialogTitle(title);
		}
	}

	private static class ZenityFileChooserImpl extends FileChooserUtil {

		private static final Logger logger = LoggerFactory.getLogger(ZenityFileChooserImpl.class);
		private final ArrayList<String> extraArgs;
		private String title;

		public ZenityFileChooserImpl() {
			extraArgs = new ArrayList<>();
		}

		@Override
		public void addFilter(String description, String... extensions) {
			StringBuilder stringBuilder = new StringBuilder("--file-filter=").append(description).append(" |");
			for (String ext : extensions) {
				stringBuilder.append(" *.").append(ext);
			}
			extraArgs.add(stringBuilder.toString());
		}

		private String getDefaultTitle(boolean save) {
			return save ? "Save" : "Open";
		}

		@Override
		public File openDialog(boolean save, Component parent) {
			ArrayList<String> args = new ArrayList<>();
			args.add("zenity");
			args.add("--file-selection");
			args.add("--title=" + (title == null ? getDefaultTitle(save) : title));
			if (save) {
				args.add("--save");
				args.add("--confirm-overwrite");
			}
			args.addAll(extraArgs);
			args.add("--file-filter=All Files (*.*) | *");
			try {
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				Process process = processBuilder.start();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String pathname = bufferedReader.readLine();
				return (pathname == null || pathname.equals("null")) ? null : new File(pathname);
			} catch (IOException e) {
				logger.error("Failed to launch zenity", e);
				return null;
			}
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}
	}

	/**
	 * create instance
	 *
	 * @return instance of FileChooserUtil
	 */
	public static FileChooserUtil newInstance() {
		String os = System.getProperty("os.name");
		if (os.contains("nix") || os.contains("nux")) {
			return new ZenityFileChooserImpl();
		} else {
			return new FileChooserDelegateImpl();
		}
	}

	/**
	 * add filter
	 *
	 * @param description description of filter
	 * @param extension   file extension such as jpg, gif, png.
	 */
	public abstract void addFilter(String description, String... extension);

	/**
	 * open file chooser
	 *
	 * @param save   save mode?
	 * @param parent parent component. some implementations ignore this.
	 * @return chosen file or null
	 */
	public abstract File openDialog(boolean save, Component parent);

	/**
	 * set title for dialog
	 *
	 * @param title title
	 */
	public abstract void setTitle(String title);
}
