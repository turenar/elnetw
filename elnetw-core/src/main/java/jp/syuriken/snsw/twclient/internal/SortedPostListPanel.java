package jp.syuriken.snsw.twclient.internal;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.StatusPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日時でソートするポストリスト。
 *
 * <p>
 * 普通の {@linkplain JPanel} では、コンポーネントの格納にArrayListを使用してるため、
 * 内部コンポーネントが増えてくると配列のコピーに時間がかかる可能性があります。
 * このクラスでは、二層の JPanel を使用することにより、配列のコピーはできるだけ抑えられます。
 * </p>
 * <p>
 * このクラスは、Twitterの、次のような性質を元にして作られています。
 * </p>
 * <ul>
 * <li>必ずしも時系列に沿ってツイートが送信されるわけではない (次の同じAPIを使った取得では、
 * 前回の取得の一番新しいツイートより古いツイートを取得する可能性がある)
 * </li>
 * <li>通常のTL表示は新→古であり、単純に末尾に追加する (→配列の拡張時のコピーだけで対処が可能) わけにはいかない</li>
 * </ul>
 * <p>
 * 具体的な処理は以下のとおりとなります。 (leafSize=2, maxSize=2)
 * </p>
 * <p>
 * {@link #add(LinkedList)}を呼び出します。このとき、引数は次のようなLinkedList
 * <pre>
 * {14:00, 15:00}
 * </pre>
 * です。すると、
 * <pre>
 * firstBranch -&gt; [<ins>&lt;-15:00</ins>, <ins>&lt;-14:00</ins>]
 * branches -&gt; []
 * </pre>
 * のように処理されます。そのうえでもう一度 {@link #add(LinkedList)}を呼び出してみます。引数は、
 * <pre>
 * {12:00, 18:00, 14:30}
 * </pre>
 * の値を持つLinkedListです。すると、
 * <pre>
 * firstBranch -&gt; [<ins>&lt;-18:00</ins>, 15:00, <ins>&lt;-14:30</ins>, 14:00, <ins>&lt;-12:00</ins>]
 * branches -&gt; []
 * </pre>
 * となりますが、sizeがleafSize * 2 (ここでは4)を超えるため、firstBranchが分割されます。
 * <pre>
 * firstBranch -&gt; [18:00, 15:00, 14:30, <del>14:00, 12:00</del>]
 * branches -&gt; [ <ins>[14:00, 12:00]</ins> ]
 * </pre>
 * このようになります。そして、もう一度 {@link #add(LinkedList)}を呼び出してみます。引数は、
 * <pre>
 * {19:00}
 * </pre>
 * の値を持つLinkedListです。すると、分割後が
 * <pre>
 * firstBranch -&gt; [<ins>&lt;-19:00</ins>, 18:00, <del>15:00, 14:30</del>]
 * branches -&gt; [ <ins>[15:00, 14:30]</ins> [14:00, 12:00] ]
 * </pre>
 * となります。ここで、sizeがleafSize * 2 + maxSize (ここでは6)を超えるため、branchesからいくつかの要素が削除されます。
 * <pre>
 * firstBranch -&gt; [19:00, 18:00, 15:00, 14:30]
 * branches -&gt; [ [15:00, 14:30] <del>[14:00, 12:00]</del> ]
 * </pre>
 * </p>
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SortedPostListPanel extends JPanel {
	private static class Bucket {
		public static Bucket make(LinkedList<StatusPanel> list) {
			int index = 0;
			int size = list.size() < 64 ? list.size() : 64;
			StatusPanel[] array = new StatusPanel[size];
			while (!list.isEmpty()) {
				array[index++] = list.pollFirst();
				if (index >= size) {
					break;
				}
			}
			Arrays.sort(array, ComponentComparator.SINGLETON);
			return new Bucket(array);
		}

		private final StatusPanel[] bucket;
		private int nextIndex;

		public Bucket(StatusPanel[] bucket) {
			this.bucket = bucket;
		}

		public boolean isEmpty() {
			return nextIndex >= bucket.length;
		}

		public StatusPanel peek() {
			return isEmpty() ? null : bucket[nextIndex];
		}

		public StatusPanel poll() {
			return isEmpty() ? null : bucket[nextIndex++];
		}

		public int size() {
			return bucket.length;
		}
	}

	/**
	 * コンポーネントを比較する
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected static final class ComponentComparator implements Comparator<StatusPanel> {

		/** ユニークインスタンス */
		public static final ComponentComparator SINGLETON = new ComponentComparator();

		private ComponentComparator() {
		}

		@Override
		public int compare(StatusPanel o1, StatusPanel o2) {
			return -(compareDate((StatusPanel) o1, (StatusPanel) o2));
		}
	}

	private static final long serialVersionUID = -2699588004179912235L;
	private static final Logger logger = LoggerFactory.getLogger(SortedPostListPanel.class);

	private static int binarySearch(JComponent panel, StatusPanel key, int start) {
		int low = start;
		int high = panel.getComponentCount() - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			StatusPanel midVal = (StatusPanel) panel.getComponent(mid);
			int cmp = midVal.compareTo(key);

			if (cmp > 0) {
				low = mid + 1;
			} else if (cmp < 0) {
				high = mid - 1;
			} else {
				throw new IllegalArgumentException("duplicate id"); // key found
			}
		}
		return low;  // key not found
	}

	/**
	 * {@link StatusPanel} を日時で比較する。
	 *
	 * @param a 比較する側
	 * @param b 比較される側
	 * @return a.compareTo(b)
	 */
	protected static int compareDate(StatusPanel a, StatusPanel b) {
		if (b == null) {
			return 1;
		} else {
			return a.compareTo(b);
		}
	}

	private static int getProperty(String key) {
		return ClientConfiguration.getInstance().getConfigProperties().getInteger(key);
	}

	private final int leafSize;
	private final int maxContainSize;
	private LinkedList<JPanel> branches;
	private int size;

	/** インスタンスを生成する。 */
	public SortedPostListPanel() {
		this(getProperty("gui.postlist.leaf_size"), getProperty("gui.postlist.max_size"));
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param leafSize 二層目のJPanelの期待サイズ (このサイズより大きくなる可能性があります)
	 * @param maxSize  このクラスが格納する要素数
	 */
	public SortedPostListPanel(int leafSize, int maxSize) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.leafSize = leafSize;
		maxContainSize = maxSize;
		branches = new LinkedList<>();
	}

	@Deprecated
	@Override
	public Component add(Component comp) {
		if (comp instanceof StatusPanel) {
			LinkedList<StatusPanel> list = new LinkedList<>();
			list.add((StatusPanel) comp);
			add(list);
			return comp;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Deprecated
	@SuppressWarnings("deprecated")
	@Override
	public Component add(Component comp, int index) {
		return this.add(comp);
	}

	@Deprecated
	@SuppressWarnings("deprecated")
	@Override
	public void add(Component comp, Object constraints) {
		this.add(comp);
	}

	@Deprecated
	@SuppressWarnings("deprecated")
	@Override
	public void add(Component comp, Object constraints, int index) {
		this.add(comp);
	}

	/**
	 * valuesの内容をこのパネルに追加する
	 *
	 * @param values StatusPanelのLinkedList
	 */
	public synchronized int add(LinkedList<StatusPanel> values) {
		if (values.size() == 0) {
			return 0;
		}

		Bucket bucket;
		synchronized (values) {
			bucket = Bucket.make(values);
		}
		long t = System.currentTimeMillis();

		for (ListIterator<JPanel> listIterator = branches.listIterator(); listIterator.hasNext(); ) {
			if (bucket.isEmpty()) {
				break;
			}
			JPanel branch = listIterator.next();
			addPanelIntoBranch(bucket, branch, listIterator, false);
		}
		if (!bucket.isEmpty()) { // all are added into last
			JPanel branch;
			if (branches.isEmpty()) { // first branch
				branch = createPanel();
				branches.add(branch);
				super.add(branch);
			} else {
				branch = branches.getLast();
			}
			addPanelIntoBranch(bucket, branch, branches.listIterator(branches.size()), true);
		}
		if (!bucket.isEmpty()) {
			throw new AssertionError();
		}

		tryRelease();
		if (logger.isTraceEnabled()) {
			logger.trace("add {}items: {}ms", bucket.size(), System.currentTimeMillis() - t);
			assertSequence();
		}
		return bucket.size();
	}

	/**
	 * {@link #add(LinkedList)}の糖衣構文
	 *
	 * @param panel パネル
	 */
	public void add(StatusPanel panel) {
		LinkedList<StatusPanel> list = new LinkedList<>();
		list.add(panel);
		add(list);
	}

	@Override
	@SuppressWarnings("deprecated")
	public Component add(String name, Component comp) {
		return this.add(comp);
	}

	private void addPanelIntoBranch(Bucket values, JPanel branch,
			ListIterator<JPanel> listIteratorOfBranches, boolean addAll) {
		Component lastOfBranch = addAll ? null : branch.getComponent(branch.getComponentCount() - 1);
		if (compareDate(values.peek(), (StatusPanel) lastOfBranch) >= 0) {
			// binarySearch+insert is usually faster than mergeSort+clear
			synchronized (branch.getTreeLock()) {
				int insertPos = 0; // values is sorted, I skip before inserted element
				do {
					StatusPanel panel = values.poll();
					insertPos = binarySearch(branch, panel, insertPos);
					// I already values.first should be added into branch
					branch.add(panel, insertPos);
					size++;
				} while (!values.isEmpty() && compareDate(values.peek(), (StatusPanel) lastOfBranch) >= 0);
				int componentCount = branch.getComponentCount();

				boolean panelAppendFlag = componentCount > (leafSize << 1);
				if (panelAppendFlag) {
					int min = componentCount >>> 1;
					int len = componentCount - min;
					Component[] array = new Component[len];
					JPanel panel = createPanel();
					for (int i = len - 1; i >= 0; i--) {
						int j = min + i;
						array[i] = branch.getComponent(j);
						branch.remove(j);
					}
					for (int i = 0; i < len; i++) {
						panel.add(array[i]);
					}
					int indexOfAddedPanel = listIteratorOfBranches.nextIndex();
					listIteratorOfBranches.add(panel);
					super.add(panel, indexOfAddedPanel);
				}
			}
		}
	}

	private void assertSequence() {
		StatusPanel previous = null;
		for (JPanel panel : branches) {
			for (Component comp : panel.getComponents()) {
				StatusPanel status = (StatusPanel) comp;
				if (previous != null) {
					if (compareDate(previous, status) <= 0) {
						throw new AssertionError();
					}
				}
				previous = status;
			}
		}
	}

	protected JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		return panel;
	}

	/**
	 * 親コンポーネント(={@link SortedPostListPanel}) に対する絶対位置を取得する。
	 * 指定されたコンポーネントがこのパネルに追加されていない場合の動作は保証されません。
	 *
	 * @param panel 調べるコンポーネント
	 * @return 絶対位置情報
	 */
	public synchronized Rectangle getBoundsOf(StatusPanel panel) {
		Rectangle bounds = panel.getBounds();
		for (JPanel branch : branches) {
			int componentCount = branch.getComponentCount();
			StatusPanel lastComponent = (StatusPanel) branch.getComponent(componentCount - 1);
			if (compareDate(panel, lastComponent) >= 0) {
				Rectangle branchBounds = branch.getBounds();
				// bounds.x += branchBounds.x;
				bounds.y += branchBounds.y;
				break;
			}
		}
		return bounds;
	}

	@Override
	public StatusPanel getComponentAt(int x, int y) {
		JPanel componentAt = (JPanel) super.getComponentAt(x, y);
		Point bounds = componentAt.getLocation();
		return (StatusPanel) componentAt.getComponentAt(x - bounds.x, y - bounds.y);
	}

	/**
	 * valueをこのパネルから削除する。
	 *
	 * @param value 削除するStatusPanel
	 * @return 削除したかどうか
	 */
	public synchronized boolean remove(StatusPanel value) {
		for (JPanel container : branches) {
			if (compareDate((StatusPanel) container.getComponent(container.getComponentCount() - 1), value) < 0) {
				container.remove(value);
				size--;
				return true;
			}
		}
		return false;
	}

	/**
	 * 最初のコンポーネントをフォーカスする
	 *
	 * @return フォーカス変更が失敗すると保証されるとき false; 成功すると思われるときは true
	 */
	public synchronized boolean requestFocusFirstComponent() {
		Component panel;
		panel = branches.getFirst().getComponent(0);
		return panel != null && panel.requestFocusInWindow();
	}

	/**
	 * 指定されたパネルの次のパネルにフォーカスを当てる
	 *
	 * @param panel パネル
	 * @return フォーカスが成功しそうかどうか
	 */
	public synchronized boolean requestFocusNextOf(StatusPanel panel) {
		for (JPanel branch : branches) {
			if (compareDate(panel, (StatusPanel) branch.getComponent(0)) > 0) {
				return branch.getComponent(0).requestFocusInWindow();
			}
			if (compareDate(panel, (StatusPanel) branch.getComponent(branch.getComponentCount() - 1)) <= 0) {
				continue;
			}
			Component[] components = branch.getComponents();
			for (int i = 0; i < components.length - 1; i++) {
				StatusPanel statusPanel = (StatusPanel) components[i];
				if (compareDate(panel, statusPanel) == 0) {
					return components[i + 1].requestFocusInWindow();
				}
			}
		}
		return false;
	}

	/**
	 * 指定されたパネルの前のパネルにフォーカスを当てる
	 *
	 * @param panel パネル
	 * @return フォーカスが成功しそうかどうか
	 */
	public synchronized boolean requestFocusPreviousOf(StatusPanel panel) {
		for (ListIterator<JPanel> iterator = branches.listIterator(branches.size()); iterator.hasPrevious(); ) {
			JPanel previous = iterator.previous();
			if (compareDate(panel, (StatusPanel) previous.getComponent(previous.getComponentCount() - 1)) < 0) {
				return previous.getComponent(previous.getComponentCount() - 1).requestFocusInWindow();
			}
			if (compareDate(panel, (StatusPanel) previous.getComponent(0)) >= 0) {
				continue;
			}
			Component[] components = previous.getComponents();
			for (int i = components.length - 1; i > 0; i--) {
				StatusPanel statusPanel = (StatusPanel) components[i];
				if (compareDate(panel, statusPanel) == 0) {
					return components[i - 1].requestFocusInWindow();
				}
			}
		}
		return false;
	}

	@Override
	public synchronized String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SortedPostListPanel{leaf=").append(leafSize).append(",max=").append(maxContainSize)
				.append(",size=").append(size);
		stringBuilder.append("}[");
		for (JPanel container : branches) {
			stringBuilder.append(container.getComponentCount()).append(", ");
		}
		stringBuilder.setLength(stringBuilder.length() - 2);
		stringBuilder.append("]");

		return stringBuilder.toString();
	}

	/** いくつかの要素を開放する。開放しない時もある。 */
	private synchronized void tryRelease() {
		while (branches.size() > 1 && size > maxContainSize + leafSize) {
			remove(getComponentCount() - 1);
			size -= branches.removeLast().getComponentCount();
		}
	}
}
