package jp.syuriken.snsw.twclient;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * バージョン情報を表示するクラス。
 * 
 * @author $Author$
 */
public class VersionInfoFrame extends JFrame {
	
	/**
	 * ライブラリ情報を格納するクラス
	 * 
	 * @author $Author$
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
	
	
	/**
	 * ライブラリ情報を追加する。
	 * 
	 * @param libraryInfo ライブラリ情報
	 * @return 追加されたかどうか。
	 */
	public static boolean addLibraryInfo(LibraryInfo libraryInfo) {
		return libraryInfoList.add(libraryInfo);
	}
	
	private static final String getData(String resourceName) {
		BufferedReader bufferedReader = null;
		try {
			InputStream stream = VersionInfoFrame.class.getClassLoader().getResourceAsStream(resourceName);
			if (stream == null) {
				logger.error("Error reading resource, Class#getResourceAsStream returned null");
				return "Error reading resource, Class#getResourceAsStream returned null";
			}
			bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
			char[] buf = new char[0x10000];
			int len;
			StringBuffer stringBuffer = new StringBuffer();
			while ((len = bufferedReader.read(buf)) != -1) {
				stringBuffer.append(buf, 0, len);
			}
			return stringBuffer.toString();
		} catch (IOException e) {
			logger.error("Error reading resource", e);
			return "Error reading resource";
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private JSplitPane splitPane;
	
	private JScrollPane libraryListScrollPane;
	
	private JScrollPane infoTextScrollPane;
	
	private JTextArea infoTextPane;
	
	private JList libraryList;
	
	private static Logger logger = LoggerFactory.getLogger(VersionInfoFrame.class);
	
	private ClientConfiguration configuration;
	
	private static List<LibraryInfo> libraryInfoList = new ArrayList<LibraryInfo>();
	
	static {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("twclient-core: version");
		stringBuilder.append(VersionInfo.getUniqueVersion());
		stringBuilder.append("\n\nThis software included library of:\n - twitter4j (");
		stringBuilder.append(twitter4j.Version.getVersion());
		stringBuilder.append(")\n   - json\n - slf4j\n   - logback");
		
		addLibraryInfo(new LibraryInfo("version", stringBuilder.toString()));
		addLibraryInfo(new LibraryInfo("turetwcl", getData("turetwcl.txt")));
		addLibraryInfo(new LibraryInfo("slf4j", getData("slf4j.txt")));
		addLibraryInfo(new LibraryInfo("logback", getData("logback.txt")));
		addLibraryInfo(new LibraryInfo("twitter4j", getData("twitter4j.txt")));
		addLibraryInfo(new LibraryInfo("json.org", getData("json.txt")));
	}
	
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new VersionInfoFrame().setVisible(true);
	}
	
	/**
	 * インスタンスを生成する
	 * 
	 */
	public VersionInfoFrame() {
		initComponents();
	}
	
	private JTextComponent getInfoTextPane() {
		if (infoTextPane == null) {
			infoTextPane = new JTextArea();
			infoTextPane.setEditable(false);
			infoTextPane.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
			infoTextPane.setLineWrap(true);
			infoTextPane.setWrapStyleWord(true);
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
	
	private JList getLibraryList() {
		if (libraryList == null) {
			libraryList = new JList();
			DefaultListModel defaultListModel = new DefaultListModel();
			for (LibraryInfo libraryName : libraryInfoList) {
				defaultListModel.addElement(libraryName.getName());
			}
			libraryList.setModel(defaultListModel);
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
	
	/**
	 * 右側を更新する
	 * 
	 * @param e イベント (ignore)
	 */
	protected void updateLibraryInfo(ListSelectionEvent e) {
		JList list = getLibraryList();
		String selectedValue = (String) list.getSelectedValue();
		for (LibraryInfo libraryInfo : libraryInfoList) {
			if (Utility.equalString(libraryInfo.name, selectedValue)) {
				getInfoTextPane().setText(libraryInfo.getInfo());
				break;
			}
		}
	}
}
