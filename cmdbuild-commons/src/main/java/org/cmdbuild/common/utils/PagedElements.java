package org.cmdbuild.common.utils;

import java.util.Iterator;

public class PagedElements<T> implements Iterable<T> {

	private final Iterable<T> elements;
	private final int totalSize;

	public PagedElements(final Iterable<T> elements, final int totalSize) {
		this.totalSize = totalSize;
		this.elements = elements;
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	public Iterable<T> elements() {
		return elements;
	}

	public int totalSize() {
		return totalSize;
	}

}
