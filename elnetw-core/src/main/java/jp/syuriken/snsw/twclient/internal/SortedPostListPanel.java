package jp.syuriken.snsw.twclient.internal;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
 * branches -&gt; [[<ins>&lt;-15:00</ins>, <ins>&lt;-14:00</ins>]]
 * </pre>
 * のように処理されます。そのうえでもう一度 {@link #add(LinkedList)}を呼び出してみます。引数は、
 * <pre>
 * {12:00, 18:00, 14:30}
 * </pre>
 * の値を持つLinkedListです。すると、
 * <pre>
 * branches -&gt; [[<ins>&lt;-18:00</ins>, 15:00, <ins>&lt;-14:30</ins>, 14:00, <ins>&lt;-12:00</ins>]]
 * </pre>
 * となりますが、sizeがleafSize * 2 (ここでは4)を超えるため、firstBranchが分割されます。
 * <pre>
 * branches -&gt; [[18:00, 15:00, 14:30, <del>14:00, 12:00</del>] <ins>[14:00, 12:00]</ins>]
 * </pre>
 * このようになります。そして、もう一度 {@link #add(LinkedList)}を呼び出してみます。引数は、
 * <pre>
 * {19:00}
 * </pre>
 * の値を持つLinkedListです。すると、分割後が
 * <pre>
 * branches -&gt; [[<ins>&lt;-19:00</ins>, 18:00, <del>15:00, 14:30</del>] <ins>[15:00, 14:30]</ins> [14:00, 12:00]]
 * </pre>
 * となります。ここで、sizeがleafSize * 2 + maxSize (ここでは6)を超えるため、branchesからいくつかの要素が削除されます。
 * <pre>
 * branches -&gt; [[19:00, 18:00, 15:00, 14:30] [15:00, 14:30] <del>[14:00, 12:00]</del>]
 * </pre>
 * </p>
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class SortedPostListPanel extends JPanel implements PropertyChangeListener {
	private static class Bucket {
		private static final Logger logger = LoggerFactory.getLogger(Bucket.class);
		private StatusPanel[] bucket;
		private int startIndex;
		private int nextIndex;
		private int len;

		public Bucket(int maxSize) {
			this.bucket = new StatusPanel[maxSize];
		}

		public boolean isEmpty() {
			return nextIndex >= len;
		}

		public void make(LinkedList<StatusPanel> list, int maxSize) {
			int remainedSize = len - nextIndex;
			if (remainedSize > (maxSize >>> 1)) {
				logger.trace("skip make bucket: {}", remainedSize);
				startIndex = nextIndex;
				return;
			}

			int newSize;
			synchronized (list) {
				newSize = remainedSize + list.size();
				// In this case, remainedSize must be smaller than (new) maxSize
				if (newSize > maxSize) {
					newSize = maxSize;
				}
				if (newSize > bucket.length) {
					StatusPanel[] newBucket = new StatusPanel[maxSize];
					System.arraycopy(bucket, nextIndex, newBucket, 0, remainedSize);
					bucket = newBucket;
				} else if (remainedSize > 0) {
					System.arraycopy(bucket, nextIndex, bucket, 0, remainedSize);
				}

				int index = remainedSize;
				while (!list.isEmpty()) {
					bucket[index++] = list.pollFirst();
					if (index >= newSize) {
						break;
					}
				}
				logger.trace("{}+{}/{}", remainedSize, newSize - remainedSize, bucket.length);
			}
			Arrays.sort(bucket, 0, newSize, ComponentComparator.SINGLETON);
			startIndex = nextIndex = 0;
			len = newSize;
		}

		public StatusPanel peek() {
			return isEmpty() ? null : bucket[nextIndex];
		}

		public StatusPanel poll() {
			return isEmpty() ? null : bucket[nextIndex++];
		}

		public int processedSize() {
			return nextIndex - startIndex;
		}

		public int size() {
			return len - startIndex;
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
			return -compareDate(o1, o2);
		}
	}

	private static final long serialVersionUID = -6160168801034111076L;
	private static final Logger logger = LoggerFactory.getLogger(SortedPostListPanel.class);
	private static final String PROPERTY_LEAF_SIZE = "gui.splp.leaf_size";
	private static final String PROPERTY_MAX_SIZE = "gui.splp.max_size";
	private static final String PROPERTY_LIMIT_ELAPSED_TIME = "gui.splp.limit_elapsed_time";
	private static final String PROPERTY_BUCKET_SIZE = "gui.splp.bucket_size";

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

	private static boolean checkOverTime(long limitTime) {
		return System.currentTimeMillis() > limitTime;
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

	private int leafSize;
	private int maxContainSize;
	private LinkedList<JPanel> branches;
	private int size;
	private long limitElapsedTime;
	private Bucket bucket;
	private int bucketMaxSize;

	/** インスタンスを生成する。 */
	public SortedPostListPanel() {
		this(getProperty(PROPERTY_LEAF_SIZE), getProperty(PROPERTY_MAX_SIZE));
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
		ClientConfiguration.getInstance().getConfigProperties().addPropertyChangedListener(this);
		limitElapsedTime = getProperty(PROPERTY_LIMIT_ELAPSED_TIME);
		bucketMaxSize = getProperty(PROPERTY_BUCKET_SIZE);
		bucket = new Bucket(bucketMaxSize);
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

		Bucket bucket = this.bucket;
		long limitTime = System.currentTimeMillis() + limitElapsedTime;
		bucket.make(values, bucketMaxSize);

		for (ListIterator<JPanel> listIterator = branches.listIterator(); listIterator.hasNext(); ) {
			JPanel branch = listIterator.next();
			addPanelIntoBranch(bucket, branch, listIterator, limitTime, false);
			if (bucket.isEmpty() || checkOverTime(limitTime)) {
				break;
			}
		}
		if (!(bucket.isEmpty() || checkOverTime(limitTime))) { // all are added into last
			JPanel branch;
			if (branches.isEmpty()) { // first branch
				branch = createPanel();
				branches.add(branch);
				super.add(branch);
			} else {
				branch = branches.getLast();
			}
			addPanelIntoBranch(bucket, branch, branches.listIterator(branches.size()), limitTime, true);
		}

		tryRelease();
		if (logger.isTraceEnabled()) {
			logger.trace("took {}ms: {}/{}", System.currentTimeMillis() + limitElapsedTime - limitTime,
					bucket.processedSize(), bucket.size());
			assertSequence();
		}
		validate();
		return bucket.processedSize();
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
	@Deprecated
	@SuppressWarnings("deprecated")
	public Component add(String name, Component comp) {
		return this.add(comp);
	}

	private void addPanelIntoBranch(Bucket values, JPanel branch, ListIterator<JPanel> listIteratorOfBranches,
			long limitTime, boolean addAll) {
		StatusPanel lastOfBranch = addAll ? null : (StatusPanel) branch.getComponent(branch.getComponentCount() - 1);
		if (compareDate(values.peek(), lastOfBranch) >= 0) {
			// binarySearch+insert is usually faster than mergeSort+clear
			synchronized (branch.getTreeLock()) {
				int insertPos = 0; // values is sorted, I skip before inserted element
				do {
					StatusPanel panel = values.poll();
					insertPos = binarySearch(branch, panel, insertPos);
					// I already values.first should be added into branch
					branch.add(panel, insertPos);
					size++;
				} while (!(values.isEmpty() || checkOverTime(limitTime)) &&
						compareDate(values.peek(), lastOfBranch) >= 0);
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
		if (panel == null) {
			throw new NullPointerException();
		}

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
	public StatusPanel getComponentAt(Point p) {
		return getComponentAt(p.x, p.y);
	}

	@Override
	public StatusPanel getComponentAt(int x, int y) {
		JPanel parentPanel = (JPanel) super.getComponentAt(x, y);
		if (parentPanel == this) {
			parentPanel = branches.peekFirst();
			if (parentPanel == null) {
				return null;
			}
		}
		Point bounds = parentPanel.getLocation();
		Component componentAt = parentPanel.getComponentAt(x - bounds.x, y - bounds.y);
		if (!(componentAt instanceof StatusPanel)) {
			componentAt = parentPanel.getComponent(0);
		}
		return (StatusPanel) componentAt;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
			case PROPERTY_BUCKET_SIZE:
				bucketMaxSize = getProperty(PROPERTY_BUCKET_SIZE);
				break;
			case PROPERTY_LEAF_SIZE:
				leafSize = getProperty(PROPERTY_LEAF_SIZE);
				break;
			case PROPERTY_LIMIT_ELAPSED_TIME:
				limitElapsedTime = getProperty(PROPERTY_LIMIT_ELAPSED_TIME);
				break;
			case PROPERTY_MAX_SIZE:
				maxContainSize = getProperty(PROPERTY_MAX_SIZE);
				break;
		}
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
