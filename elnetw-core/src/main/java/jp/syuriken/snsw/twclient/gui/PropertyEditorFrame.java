package jp.syuriken.snsw.twclient.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.ClientProperties;

/**
 * プロパティーエディター。
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PropertyEditorFrame extends JFrame {

	/**
	 * テーブルモデル
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	private static final class DefaultTableModelExtension extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		Class<?>[] types = new Class<?>[]{
				String.class,
				String.class,
		};

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return types[columnIndex];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 1;
		}
	}

	/**
	 * セルレンダラ。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	@SuppressWarnings("serial")
	private final class DefaultTableCellRendererExtension extends DefaultTableCellRenderer {

		private final DefaultTableModelExtension model;

		/**
		 * インスタンスを生成する。
		 *
		 * @param tableModel テーブルモデル
		 */
		public DefaultTableCellRendererExtension(DefaultTableModelExtension tableModel) {
			model = tableModel;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			Object key;
			if (column == 0) {
				key = value;
			} else {
				key = model.getValueAt(table.convertRowIndexToModel(row), 0);
			}
			if (configuration.getConfigProperties().containsKey(key)) {
				setFont(getFont().deriveFont(Font.BOLD));
			}
			return this;
		}
	}

	private static final long serialVersionUID = 1L;

	/*
	public static void main(String[] args) throws IOException {
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		ClientProperties defaultProperties = new ClientProperties();
		defaultProperties.setProperty("default", "default?");
		defaultProperties.setProperty("int", "is the value");
		ClientProperties clientProperties = new ClientProperties(defaultProperties);
		clientProperties.setStoreFile(File.createTempFile("elnetw-test", ".prop"));
		clientProperties.setProperty("123", "456");
		clientProperties.setProperty("default", "entire");
		clientProperties.setProperty("abc", "def");
		clientConfiguration.setConfigProperties(clientProperties);
		PropertyEditorFrame frame = new PropertyEditorFrame(clientConfiguration);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	*/

	/** 設定 */
	protected final transient ClientConfiguration configuration;

	private JScrollPane paneListProperties;

	private JButton btnExit;

	private JButton btnDefault;

	private JTable tableProperties;

	/** テーブルモデル */
	protected DefaultTableModelExtension tableModel;

	/** インスタンスを生成する */
	public PropertyEditorFrame() {
		this.configuration = ClientConfiguration.getInstance();
		initComponents();
	}

	private void btnDefaultMouseMouseClicked(MouseEvent event) {
		String key =
				(String) tableModel.getValueAt(
						tableProperties.convertRowIndexToModel(tableProperties.getSelectedRow()), 0);
		String value =
				(String) tableModel.getValueAt(
						tableProperties.convertRowIndexToModel(tableProperties.getSelectedRow()), 1);

		int result =
				JOptionPane.showConfirmDialog(this, "\"" + key + "\"をデフォルトの値に復元してよろしいですか？\n\n現在の値: \"" + value + "\"",
						"元に戻す", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			configuration.getConfigProperties().remove(key);
			initTableModel();
		}
	}

	/**
	 * 終了ボタンを押した時のハンドラ。
	 *
	 * @param e イベント
	 */
	protected void btnExitMouseClicked(MouseEvent e) {
		setVisible(false);
		dispose();
	}

	private JButton getBtnDefault() {
		if (btnDefault == null) {
			btnDefault = new JButton();
			btnDefault.setText("元に戻す");
			btnDefault.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent event) {
					btnDefaultMouseMouseClicked(event);
				}
			});
		}
		return btnDefault;
	}

	private JButton getBtnExit() {
		if (btnExit == null) {
			btnExit = new JButton();
			btnExit.setText("閉じる(X)");
			btnExit.setMnemonic('X');
			btnExit.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					btnExitMouseClicked(e);
				}
			});
		}
		return btnExit;
	}

	private JScrollPane getPaneListProperties() {
		if (paneListProperties == null) {
			paneListProperties = new JScrollPane();
			paneListProperties.setViewportView(getTableProperties());
		}
		return paneListProperties;
	}

	private JTable getTableProperties() {
		if (tableProperties == null) {
			tableProperties = new JTable();
			tableProperties.setColumnSelectionAllowed(true);
			tableProperties.setColumnSelectionAllowed(false);
			tableProperties.getTableHeader().setReorderingAllowed(false);
			tableModel = new DefaultTableModelExtension();
			tableProperties.setModel(tableModel);
			tableModel.setColumnCount(0);
			tableModel.addColumn("name");
			tableModel.addColumn("value");
			tableModel.addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {
					if (e.getType() == TableModelEvent.UPDATE) {
						String key = (String) tableModel.getValueAt(e.getFirstRow(), 0);
						String value = (String) tableModel.getValueAt(e.getFirstRow(), 1);
						configuration.getConfigProperties().setProperty(key, value);
					}
				}
			});
			tableProperties.setDefaultRenderer(String.class, new DefaultTableCellRendererExtension(tableModel));
			TableRowSorter<DefaultTableModelExtension> sorter =
					new TableRowSorter<DefaultTableModelExtension>(tableModel);
			sorter.setSortable(1, false);
			List<SortKey> s = new ArrayList<SortKey>();
			s.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			sorter.setSortKeys(s);
			tableProperties.setRowSorter(sorter);
			tableProperties.setCellSelectionEnabled(true);
		}
		return tableProperties;
	}

	private void initComponents() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		GroupLayout layout = new GroupLayout(getContentPane());
		setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getPaneListProperties()).addContainerGap().addGap(4, 4, 4)
				.addGroup(layout.createParallelGroup(Alignment.LEADING).addGap(4, 4, 4)
						.addComponent(getBtnDefault(), Alignment.LEADING).addGap(4)
						.addComponent(getBtnExit(), Alignment.TRAILING).addGap(4, 4, 4))
				.addGap(4, 4, 4));
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(getPaneListProperties())
				.addGroup(Alignment.LEADING, layout.createSequentialGroup().addGap(18)
						.addComponent(getBtnDefault()).addGap(18).addContainerGap(8, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getBtnExit()).addGap(18)));
		/*		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(
		 * 			GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				jPanel2Layout.createSequentialGroup().addContainerGap()
					.addComponent(postDataTextAreaScrollPane, GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(postActionButton)
					.addGap(18, 18, 18)));
		 */
		setSize(320, 223);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				configuration.getConfigProperties().store();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				configuration.getConfigProperties().store();
			}
		});
	}

	private void initTableModel() {
		tableModel.setRowCount(0);

		ClientProperties configProperties = configuration.getConfigProperties();
		for (Enumeration<?> enumeration = configProperties.propertyNames(); enumeration.hasMoreElements(); ) {
			String key = (String) enumeration.nextElement();
			tableModel.addRow(new Object[]{
					key,
					configProperties.getProperty(key)
			});
		}
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b == true) {
			initTableModel();
		}
	}
}
