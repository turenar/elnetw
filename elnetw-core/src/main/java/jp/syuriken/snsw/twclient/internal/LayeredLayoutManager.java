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

package jp.syuriken.snsw.twclient.internal;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Layered Layout Manager
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class LayeredLayoutManager implements LayoutManager2 {

	private static final double DOUBLE_EQUAL_THRESHOLD = .0000001;
	protected final Dimension greatMaximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	/**
	 * minimum size for component
	 */
	protected Dimension minimumSize;
	/**
	 * preferred size for component
	 */
	protected Dimension preferredSize;
	/**
	 * maximum size for component
	 */
	protected Dimension maximumSize;

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		invalidateLayout();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		/*if (tweetViewTextLayeredPane != comp.getParent()) {
			throw new IllegalArgumentException("parent is already setted");
		}*/
		invalidateLayout();
	}

	private void calculateLayout(Container parent) {
		if (minimumSize != null && preferredSize != null && maximumSize != null) {
			return;
		}
		int minw = 0;
		int minh = 0;
		int prefw = 0;
		int prefh = 0;
		int maxw = 0;
		int maxh = 0;
		int count = parent.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component component = parent.getComponent(i);
			Dimension size = component.getMinimumSize();
			minw = max(minw, size.width);
			minh = max(minh, size.height);
			size = component.getPreferredSize();
			prefw = max(prefw, size.width);
			prefh = max(prefh, size.height);
			size = component.getMaximumSize();
			maxw = max(maxw, size.width);
			maxh = max(maxh, size.height);
		}
		minimumSize = new Dimension(minw, minh);
		preferredSize = new Dimension(prefw, prefh);
		maximumSize = new Dimension(maxh, maxw);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	private void invalidateLayout() {
		minimumSize = null;
		preferredSize = null;
		maximumSize = null;
	}

	@Override
	public void invalidateLayout(Container target) {
		invalidateLayout();
	}

	@Override
	public void layoutContainer(Container parent) {
		final Insets insets = parent.getInsets();
		final Dimension size = parent.getSize();
		final int width = size.width - insets.left - insets.right;
		final int height = size.height - insets.top - insets.bottom;
		final int count = parent.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component comp = parent.getComponent(i);
			Dimension prefSize = comp.getPreferredSize();
			Dimension minSize = comp.getMinimumSize();
			int compw;
			int x;
			int comph;
			int y;

			if (abs(comp.getAlignmentX() - Component.CENTER_ALIGNMENT) < DOUBLE_EQUAL_THRESHOLD) {
				compw = width;
				x = 0;
			} else {
				compw =
						width < prefSize.width ? ((width > minSize.width) ? width : minSize.width)
								: prefSize.width;
				x = (int) ((width - compw) * comp.getAlignmentX());
			}
			if (abs(comp.getAlignmentY() - Component.CENTER_ALIGNMENT) < DOUBLE_EQUAL_THRESHOLD) {
				comph = height;
				y = 0;
			} else {
				comph =
						height < prefSize.height ? ((height > minSize.height) ? height : minSize.height)
								: prefSize.height;
				y = (int) ((height - comph) * comp.getAlignmentY());
			}
			comp.setBounds(x, y, compw, comph);
		}
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		/*calculateLayout(target);
		return maximumSize;*/
		return greatMaximumSize;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		calculateLayout(parent);
		return minimumSize;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		calculateLayout(parent);
		return preferredSize;
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		invalidateLayout();
	}
}
