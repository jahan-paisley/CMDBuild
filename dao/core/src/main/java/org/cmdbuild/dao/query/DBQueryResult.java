package org.cmdbuild.dao.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.jcip.annotations.NotThreadSafe;

/*
 * Mutable classes used by the driver implementations
 */
@NotThreadSafe
public class DBQueryResult implements CMQueryResult {

	private final Collection<CMQueryRow> rows;
	private int totalSize;

	public DBQueryResult() {
		rows = new ArrayList<CMQueryRow>();
		totalSize = 0;
	}

	public void add(final CMQueryRow row) {
		rows.add(row);
	}

	@Override
	public Iterator<CMQueryRow> iterator() {
		return rows.iterator();
	}

	@Override
	public int size() {
		return rows.size();
	}

	@Override
	public boolean isEmpty() {
		return rows.isEmpty();
	}

	@Override
	public int totalSize() {
		return totalSize;
	}

	public void setTotalSize(final int size) {
		this.totalSize = size;
	}

	@Override
	public CMQueryRow getOnlyRow() throws NoSuchElementException {
		final Iterator<CMQueryRow> i = iterator();
		final CMQueryRow row = i.next();
		if (i.hasNext()) {
			throw new NoSuchElementException("More than one row returned");
		}
		return row;
	}

}
