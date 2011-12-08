package jp.syuriken.snsw.twclient;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
		size += values.size();
		
		Collections.sort(values, ComponentComparator.SINGLETON);
		
		for (ListIterator<StatusPanel> iterator = firstBranch.listIterator(); iterator.hasNext();) {
			if (values.isEmpty()) {
				break;
			}
			StatusPanel value = values.peekFirst();
			StatusPanel branchValue = iterator.next();
			
			if (compareDate(value, branchValue) <= 0) {
				continue;
			} else {
				firstPanel.add(values.peekFirst(), iterator.previousIndex());
				iterator.previous();
				iterator.add(values.pollFirst());
				firstPanel.invalidate();
				continue;
			}
		}
		if (branches.isEmpty()) {
			while (values.isEmpty() == false) {
				firstBranch.addLast(values.peekFirst());
				firstPanel.add(values.pollFirst());
			}
		} else {
			for (ListIterator<JPanel> listIterator = branches.listIterator(); listIterator.hasNext();) {
				if (values.isEmpty()) {
					break;
				}
				JPanel branch = listIterator.next();
				Component lastOfBranch = branch.getComponent(branch.getComponentCount() - 1);
				if (compareDate(values.peekFirst(), (StatusPanel) lastOfBranch) > 0) {
					Component[] newBranch = Arrays.copyOf(branch.getComponents(), branch.getComponentCount() + 1);
					newBranch[newBranch.length - 1] = values.pollFirst();
					Arrays.sort(newBranch, ComponentComparator.SINGLETON);
					branch.removeAll();
					for (Component component : newBranch) {
						branch.add(component);
					}
					branch.invalidate();
				}
			}
		}
		splitFirstBranch();
		
		tryRelease();
		updateUI();
	}
	
	@Override
	public Component add(String name, Component comp) {
		return this.add(comp);
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
				if (compareDate((StatusPanel) container.getComponent(container.getComponentCount()), value) < 0) {
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
