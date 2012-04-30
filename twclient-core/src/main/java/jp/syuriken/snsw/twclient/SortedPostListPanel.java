package jp.syuriken.snsw.twclient;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

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
 *   <li>必ずしも時系列に沿ってツイートが送信されるわけではない (次の同じAPIを使った取得では、
 *     前回の取得の一番新しいツイートより古いツイートを取得する可能性がある)
 *   </li>
 *   <li>通常のTL表示は新→古であり、単純に末尾に追加する (→配列の拡張時のコピーだけで対処が可能) わけにはいかない</li>
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
 * @author $Author$
 */
@SuppressWarnings("serial")
public class SortedPostListPanel extends JPanel {
	
	/**
	 * コンポーネントを比較する
	 * 
	 * @author $Author$
	 */
	public final static class ComponentComparator implements Comparator<Component> {
		
		/** ユニークインスタンス */
		public static final ComponentComparator SINGLETON = new ComponentComparator();
		
		
		private ComponentComparator() {
		}
		
		@Override
		public int compare(Component o1, Component o2) {
			if (o1 instanceof StatusPanel && o2 instanceof StatusPanel) {
				return -(compareDate((StatusPanel) o1, (StatusPanel) o2));
			} else {
				throw new IllegalArgumentException("SortedPostListPanelに追加できるコンポーネントはStatusPanelだけです。");
			}
		}
	}
	
	
	/**
	 * {@link StatusPanel} を日時で比較する。
	 * @param a 比較する側 
	 * @param b 比較される側
	 * @return a.compareTo(b)
	 */
	public static int compareDate(StatusPanel a, StatusPanel b) {
		return a.compareTo(b);
	}
	
	
	private LinkedList<JPanel> branches;
	
	private LinkedList<StatusPanel> firstBranch;
	
	private JPanel firstPanel;
	
	private int size;
	
	private final int leafSize;
	
	private final int maxContainSize;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
	public SortedPostListPanel() {
		this(3200, 50);
	}
	
	/**
	 * インスタンスを生成する。
	 * @param leafSize 二層目のJPanelの期待サイズ (このサイズより大きくなる可能性があります)
	 * @param maxSize このクラスが格納する要素数
	 * 
	 */
	public SortedPostListPanel(int leafSize, int maxSize) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.leafSize = leafSize;
		maxContainSize = maxSize;
		branches = new LinkedList<JPanel>();
		firstBranch = new LinkedList<StatusPanel>();
		firstPanel = new JPanel();
		firstPanel.setLayout(new BoxLayout(firstPanel, BoxLayout.Y_AXIS));
		super.add(firstPanel, 0);
	}
	
	@Deprecated
	@Override
	public Component add(Component comp) {
		if (comp instanceof StatusPanel) {
			LinkedList<StatusPanel> list = new LinkedList<StatusPanel>();
			list.add((StatusPanel) comp);
			add(comp);
			return comp;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	@Deprecated
	@Override
	public Component add(Component comp, int index) {
		return this.add(comp);
	}
	
	@Deprecated
	@Override
	public void add(Component comp, Object constraints) {
		this.add(comp);
	}
	
	@Override
	public void add(Component comp, Object constraints, int index) {
		this.add(comp);
	}
	
	/**
	 * valuesの内容をこのパネルに追加する
	 * 
	 * @param values StatusPanelのLinkedList
	 */
	public synchronized void add(LinkedList<StatusPanel> values) {
		if (values.size() == 0) {
			return;
		}
		
		Collections.sort(values, ComponentComparator.SINGLETON);
		
		for (ListIterator<StatusPanel> iterator = firstBranch.listIterator(); iterator.hasNext();) {
			if (values.isEmpty()) {
				break;
			}
			StatusPanel value = values.peekFirst();
			StatusPanel branchValue = iterator.next();
			
			if (compareDate(value, branchValue) < 0) {
				continue;
			} else {
				firstPanel.add(values.peekFirst(), iterator.previousIndex());
				iterator.previous();
				iterator.add(values.pollFirst());
				size++;
				firstPanel.invalidate();
				continue;
			}
		}
		if (branches.isEmpty()) {
			while (values.isEmpty() == false) {
				firstBranch.addLast(values.peekFirst());
				firstPanel.add(values.pollFirst());
				size++;
			}
		} else {
			for (ListIterator<JPanel> listIterator = branches.listIterator(); listIterator.hasNext();) {
				if (values.isEmpty()) {
					break;
				}
				JPanel branch = listIterator.next();
				Component lastOfBranch = branch.getComponent(branch.getComponentCount() - 1);
				while (compareDate(values.peekFirst(), (StatusPanel) lastOfBranch) >= 0) {
					Component[] newBranch = Arrays.copyOf(branch.getComponents(), branch.getComponentCount() + 1);
					newBranch[newBranch.length - 1] = values.pollFirst();
					Arrays.sort(newBranch, ComponentComparator.SINGLETON);
					branch.removeAll();
					for (Component component : newBranch) {
						branch.add(component);
					}
					branch.invalidate();
					size++;
				}
			}
			if (values.isEmpty() == false) { // all are added into last 
				JPanel branch = branches.getLast();
				Component[] newBranch =
						Arrays.copyOf(branch.getComponents(), branch.getComponentCount() + values.size());
				for (int i = 1; values.isEmpty() == false; i++) {
					newBranch[newBranch.length - i] = values.pollFirst();
					size++;
				}
				Arrays.sort(newBranch, ComponentComparator.SINGLETON);
				int branchSize = newBranch.length;
				int offset = 0;
				if (branchSize > (leafSize * 2)) {
					branches.removeLast(); // it is lastBranch
					remove(getComponentCount() - 1);
					while (branchSize > offset + (leafSize * 2)) {
						JPanel panel = new JPanel();
						panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
						for (int i = 0; i < leafSize; i++) {
							panel.add(newBranch[offset + i]);
						}
						offset += leafSize;
						branches.addLast(panel);
						super.add(panel);
					}
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					for (int i = offset; i < branchSize; i++) {
						panel.add(newBranch[i]);
					}
					branches.addLast(panel);
					super.add(panel);
				} else {
					branch.removeAll();
					for (Component component : newBranch) {
						branch.add(component);
					}
					branch.invalidate();
				}
			}
		}
		if (values.size() != 0) {
			throw new AssertionError();
		}
		splitFirstBranch();
		
		tryRelease();
		revalidate();
	}
	
	/**
	 * {@link #add(LinkedList)}の糖衣構文
	 * 
	 * @param panel パネル
	 */
	public void add(StatusPanel panel) {
		LinkedList<StatusPanel> list = new LinkedList<StatusPanel>();
		list.add(panel);
		add(list);
	}
	
	@Override
	public Component add(String name, Component comp) {
		return this.add(comp);
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
		if (compareDate(panel, firstBranch.peekLast()) >= 0) {
			Rectangle branchBounds = firstPanel.getBounds();
			bounds.y += branchBounds.y;
		} else {
			for (JPanel branch : branches) {
				StatusPanel lastComponent = (StatusPanel) branch.getComponent(branch.getComponentCount() - 1);
				if (compareDate(panel, lastComponent) >= 0) {
					Rectangle branchBounds = branch.getBounds();
					// bounds.x += branchBounds.x;
					bounds.y += branchBounds.y;
					break;
				}
			}
		}
		return bounds;
	}
	
	@Override
	public StatusPanel getComponentAt(int x, int y) {
		JPanel componentAt = (JPanel) super.getComponentAt(x, y);
		Point bounds = componentAt.getLocation();
		x -= bounds.x;
		y -= bounds.y;
		return (StatusPanel) componentAt.getComponentAt(x, y);
	}
	
	/**
	 * valueをこのパネルから削除する。
	 * 
	 * @param value 削除するStatusPanel
	 * @return 削除したかどうか
	 */
	public synchronized boolean remove(StatusPanel value) {
		if (compareDate(firstBranch.peekLast(), value) < 0) {
			firstBranch.remove(value);
			firstPanel.remove(value);
			firstPanel.invalidate();
			size--;
			return true;
		} else {
			for (JPanel container : branches) {
				if (compareDate((StatusPanel) container.getComponent(container.getComponentCount() - 1), value) < 0) {
					container.remove(value);
					container.invalidate();
					size--;
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 最初のコンポーネントをフォーカスする
	 * @return フォーカス変更が失敗すると保証されるとき false; 成功すると思われるときは true
	 */
	public synchronized boolean requestFocusFirstComponent() {
		StatusPanel panel = firstBranch.getFirst();
		if (panel == null) {
			panel = (StatusPanel) branches.getFirst().getComponent(0);
		}
		if (panel == null) {
			return false;
		}
		return panel.requestFocusInWindow();
	}
	
	/**
	 * 指定されたパネルの次のパネルにフォーカスを当てる
	 * 
	 * @param panel パネル
	 * @return フォーカスが成功しそうかどうか
	 */
	public synchronized boolean requestFocusNextOf(StatusPanel panel) {
		int comparison = compareDate(panel, firstBranch.getLast());
		
		if (comparison > 0) { // firstBranchのlastではない
			int indexOf = firstBranch.indexOf(panel);
			if (indexOf < 0) {
				return false; // not found
			}
			return firstBranch.get(indexOf + 1).requestFocusInWindow();
		} else {
			for (Iterator<JPanel> iterator = branches.listIterator(); iterator.hasNext();) {
				JPanel next = iterator.next();
				if (compareDate(panel, (StatusPanel) next.getComponent(0)) > 0) {
					return next.getComponent(0).requestFocusInWindow();
				}
				if (compareDate(panel, (StatusPanel) next.getComponent(next.getComponentCount() - 1)) <= 0) {
					continue;
				}
				Component[] components = next.getComponents();
				for (int i = 0; i < components.length - 1; i++) {
					StatusPanel statusPanel = (StatusPanel) components[i];
					if (compareDate(panel, statusPanel) == 0) {
						return components[i + 1].requestFocusInWindow();
					}
				}
			}
			return false;
		}
	}
	
	/**
	 * 指定されたパネルの前のパネルにフォーカスを当てる
	 * 
	 * @param panel パネル
	 * @return フォーカスが成功しそうかどうか
	 */
	public synchronized boolean requestFocusPreviousOf(StatusPanel panel) {
		for (ListIterator<JPanel> iterator = branches.listIterator(branches.size()); iterator.hasPrevious();) {
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
		if (compareDate(panel, firstBranch.peekLast()) < 0) { // panelがsecondBranchの最初
			return firstBranch.peekLast().requestFocusInWindow();
		}
		int indexOf = firstBranch.indexOf(panel);
		if (indexOf <= 0) { // not found OR first
			return false;
		}
		return firstBranch.get(indexOf - 1).requestFocusInWindow();
	}
	
	/**
	 * firstBranchを分割する。分割しない時もある。
	 */
	private synchronized void splitFirstBranch() {
		while (firstBranch.size() > (leafSize << 1)) {
			ListIterator<StatusPanel> li = firstBranch.listIterator(firstBranch.size());
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			for (int i = leafSize - 1; i >= 0; i--) {
				panel.add(li.previous(), 0);
				li.remove();
			}
			branches.addFirst(panel);
			super.add(panel, 1);
		}
	}
	
	@Override
	public synchronized String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SortedPostListPanel{leaf=").append(leafSize).append(",max=").append(maxContainSize)
			.append(",size=").append(size);
		stringBuilder.append("}[").append(firstBranch.size()).append(", ");
		for (JPanel container : branches) {
			stringBuilder.append(container.getComponentCount()).append(", ");
		}
		stringBuilder.setLength(stringBuilder.length() - 2);
		stringBuilder.append("]");
		
		return stringBuilder.toString();
	}
	
	/**
	 * いくつかの要素を開放する。開放しない時もある。
	 */
	private synchronized void tryRelease() {
		while (size > maxContainSize + leafSize) {
			remove(getComponentCount() - 1);
			size -= branches.removeLast().getComponentCount();
		}
	}
}
