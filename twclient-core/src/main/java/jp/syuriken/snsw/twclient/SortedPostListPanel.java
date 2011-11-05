package jp.syuriken.snsw.twclient;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * TODO snsoftware
 * 
 * @author $Author$
 */
@SuppressWarnings("serial")
public class SortedPostListPanel extends JPanel {
	
	/**
	 * TODO snsoftware
	 * 
	 * @author $Author$
	 */
	private final static class ComponentComparator implements Comparator<Component> {
		
		public static final ComponentComparator SINGLETON = new ComponentComparator();
		
		
		private ComponentComparator() {
		}
		
		@Override
		public int compare(Component o1, Component o2) {
			return -(o1.getName().compareTo(o2.getName()));
		}
	}
	
	
	private LinkedList<Container> branches;
	
	private LinkedList<Container> firstBranch;
	
	private JPanel firstPanel;
	
	private int size;
	
	private final int leafSize;
	
	private final int maxContainSize;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 */
	public SortedPostListPanel(int leafSize, int maxSize) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.leafSize = leafSize;
		maxContainSize = maxSize;
		branches = new LinkedList<Container>();
		firstBranch = new LinkedList<Container>();
		firstPanel = new JPanel();
		firstPanel.setLayout(new BoxLayout(firstPanel, BoxLayout.Y_AXIS));
		add(firstPanel, 0);
	}
	
	public void add(LinkedList<Container> values) {
		size += values.size();
		invalidate();
		Collections.sort(values, ComponentComparator.SINGLETON);
		
		for (ListIterator<Container> iterator = firstBranch.listIterator(); iterator.hasNext();) {
			if (values.isEmpty()) {
				break;
			}
			Container value = values.peekFirst();
			Container branchValue = iterator.next();
			
			if (value.getName().compareTo(branchValue.getName()) <= 0) {
				continue;
			} else {
				firstPanel.add(values.peekFirst(), iterator.previousIndex());
				iterator.previous();
				iterator.add(values.pollFirst());
				
				continue;
			}
		}
		
		if (branches.isEmpty()) {
			while (values.isEmpty() == false) {
				firstBranch.addLast(values.peekFirst());
				firstPanel.add(values.pollFirst());
			}
		} else {
			for (ListIterator<Container> listIterator = branches.listIterator(); listIterator.hasNext();) {
				if (values.isEmpty()) {
					break;
				}
				Container branch = listIterator.next();
				Component lastOfBranch = branch.getComponent(branch.getComponentCount() - 1);
				if (values.peekFirst().getName().compareTo(lastOfBranch.getName()) > 0) {
					Component[] newBranch = Arrays.copyOf(branch.getComponents(), branch.getComponentCount() + 1);
					newBranch[newBranch.length - 1] = values.pollFirst();
					Arrays.sort(newBranch, ComponentComparator.SINGLETON);
					branch.removeAll();
					for (Component component : newBranch) {
						branch.add(component);
					}
				}
			}
		}
		splitFirstBranch();
		
		tryRelease();
		
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				firstPanel.validate();
				for (Container container : branches) {
					container.validate();
				}
			}
		});
		
		updateUI();
	}
	
	/**
	 * TODO snsoftware
	 * 
	 */
	private void splitFirstBranch() {
		while (firstBranch.size() > (leafSize << 1)) {
			ListIterator<Container> li = firstBranch.listIterator(firstBranch.size());
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			for (int i = leafSize - 1; i >= 0; i--) {
				panel.add(li.previous(), 0);
				li.remove();
			}
			branches.addFirst(panel);
			add(panel, 1);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("(Sorter)[ firstBranch=").append(firstBranch.toString());
		stringBuilder.append(",\t branches=[");
		for (Container elements : branches) {
			stringBuilder.append(Arrays.toString(elements.getComponents()));
			stringBuilder.append(", ");
		}
		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
		stringBuilder.append("] ]");
		return stringBuilder.toString();
	}
	
	/**
	 * TODO snsoftware
	 * 
	 */
	private void tryRelease() {
		while (size > maxContainSize + leafSize) {
			remove(getComponentCount() - 1);
		}
	}
}
