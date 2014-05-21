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

package jp.syuriken.snsw.twclient.gui;

import java.awt.Font;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.VersionInfo;
import jp.syuriken.snsw.twclient.jni.JavaGnome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * バージョン情報を表示するクラス。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@SuppressWarnings("serial")
public class VersionInfoFrame extends JFrame {

	/**
	 * ライブラリ情報を格納するクラス
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public static class LibraryInfo {

		/** ライブラリ名 */
		protected String name;
		/** ライブラリ情報。ライセンスなど */
		protected String info;

		/**
		 * インスタンスを生成する。
		 *
		 * @param name ライブラリ名
		 * @param info ライブラリ情報
		 */
		public LibraryInfo(String name, String info) {
			this.name = name;
			this.info = info;
		}

		/**
		 * ライブラリ情報を取得する。
		 *
		 * @return ライセンスなど
		 */
		public String getInfo() {
			return info;
		}

		/**
		 * ライブラリ名を取得する
		 *
		 * @return ライブラリ名。
		 */
		public String getName() {
			return name;
		}
	}

	private static Logger logger = LoggerFactory.getLogger(VersionInfoFrame.class);

	private static String getData(String resourceName) {
		BufferedReader bufferedReader = null;
		try {
			InputStream stream = VersionInfoFrame.class.getClassLoader().getResourceAsStream(resourceName);
			if (stream == null) {
				logger.error("Error reading resource, Class#getResourceAsStream returned null");
				return "Error reading resource, Class#getResourceAsStream returned null";
			}
			bufferedReader = new BufferedReader(new InputStreamReader(stream, ClientConfiguration.UTF8_CHARSET));
			char[] buf = new char[0x10000];
			int len;
			StringBuilder stringBuilder = new StringBuilder();
			while ((len = bufferedReader.read(buf)) != -1) {
				stringBuilder.append(buf, 0, len);
			}
			return stringBuilder.toString();
		} catch (IOException e) {
			logger.error("Error reading resource", e);
			return "Error reading resource";
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.warn("Failed close reader", e);
				}
			}
		}
	}

	private List<LibraryInfo> libraryInfoList = new ArrayList<>();
	private JSplitPane splitPane;
	private JScrollPane libraryListScrollPane;
	private JScrollPane infoTextScrollPane;
	private JTextArea infoTextPane;
	private JList<String> libraryList;

	/** インスタンスを生成する */
	public VersionInfoFrame() {
		initLibraryInfos();
		initComponents();
	}

	/**
	 * ライブラリ情報を追加する。
	 *
	 * <p>TODO: 外部からの追加対応</p>
	 *
	 * @param libraryInfo ライブラリ情報
	 * @return 追加されたかどうか。
	 */
	public boolean addLibraryInfo(LibraryInfo libraryInfo) {
		return libraryInfoList.add(libraryInfo);
	}

	private JTextComponent getInfoTextPane() {
		if (infoTextPane == null) {
			infoTextPane = new JTextArea();
			infoTextPane.setEditable(false);
			infoTextPane.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
			infoTextPane.setLineWrap(true);
			infoTextPane.setWrapStyleWord(true);
			infoTextPane.setText(libraryInfoList.get(0).getInfo());
		}
		return infoTextPane;
	}

	private JScrollPane getInfoTextScrollPane() {
		if (infoTextScrollPane == null) {
			infoTextScrollPane = new JScrollPane();
			JPanel jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
			jPanel.add(getInfoTextPane());
			infoTextScrollPane.setViewportView(jPanel);
		}
		return infoTextScrollPane;
	}

	private JList<String> getLibraryList() {
		if (libraryList == null) {
			libraryList = new JList<>();
			DefaultListModel<String> defaultListModel = new DefaultListModel<>();
			for (LibraryInfo libraryName : libraryInfoList) {
				defaultListModel.addElement(libraryName.getName());
			}
			libraryList.setModel(defaultListModel);
			libraryList.setSelectedIndex(0);
			libraryList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					updateLibraryInfo(e);
				}
			});
		}
		return libraryList;
	}

	private JScrollPane getLibraryListScrollPane() {
		if (libraryListScrollPane == null) {
			libraryListScrollPane = new JScrollPane();
			libraryListScrollPane.setViewportView(getLibraryList());
			libraryListScrollPane.getVerticalScrollBar().setUnitIncrement(
					ClientConfiguration.getInstance().getConfigProperties().getInteger(
							ClientConfiguration.PROPERTY_LIST_SCROLL)
			);
		}
		return libraryListScrollPane;
	}

	private JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(getLibraryListScrollPane());
			splitPane.setBottomComponent(getInfoTextScrollPane());
		}
		return splitPane;
	}

	private void initComponents() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		add(getSplitPane());
		pack();
		setSize(600, 450);
		getSplitPane().setDividerLocation(150);
	}

	private void initLibraryInfos() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("elnetw (エルナト): version")
				.append(VersionInfo.getUniqueVersion())
				.append("\nTwitter Client for hitobasira")
				.append("\n\n開発元: Turenai Project (@ture7)")
				.append("\n配布元: ").append(VersionInfo.getSupportUrl())
				.append("\n\nThis software included library:\n - twitter4j (")
				.append(twitter4j.Version.getVersion())
				.append(")\n   - json\n - logback\n   - slf4j\n - twitter-text\n - java-gnome (optional");

		JavaGnome javaGnome = JavaGnome.getInstance();
		if (javaGnome.isFound()) {
			stringBuilder.append(";api=")
					.append(javaGnome.getApiVersion())
					.append(";version=")
					.append(javaGnome.getVersion());
			if (javaGnome.isDisabled()) {
				stringBuilder.append(";disabled");
			}
		} else {
			stringBuilder.append(";missing");
		}
		stringBuilder.append(')');

		addLibraryInfo(new LibraryInfo("version", stringBuilder.toString()));
		addLibraryInfo(new LibraryInfo("elnetw", getData("elnetw.txt")));
		addLibraryInfo(new LibraryInfo("twitter4j", getData("twitter4j.txt")));
		addLibraryInfo(new LibraryInfo("json.org", getData("json.txt")));
		addLibraryInfo(new LibraryInfo("slf4j", getData("slf4j.txt")));
		addLibraryInfo(new LibraryInfo("logback", getData("logback.txt")));
		addLibraryInfo(new LibraryInfo("twitter-text", getData("twitter-text.txt")));
		addLibraryInfo(new LibraryInfo("java-gnome", getData("java-gnome.txt")));
	}

	/**
	 * 右側を更新する
	 *
	 * @param e イベント (ignore)
	 */
	protected void updateLibraryInfo(ListSelectionEvent e) {
		JList<String> list = getLibraryList();
		String selectedValue = list.getSelectedValue();
		for (LibraryInfo libraryInfo : libraryInfoList) {
			if (libraryInfo.name.equals(selectedValue)) {
				getInfoTextPane().setText(libraryInfo.getInfo());
				getInfoTextScrollPane().getViewport().setViewPosition(new Point(0, 0));
				getInfoTextScrollPane().getViewport().validate();
				break;
			}
		}
	}
}
