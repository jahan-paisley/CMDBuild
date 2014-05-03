package org.cmdbuild.dao.query;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterators;

/*
 * Immutable interface to mask result object building
 */
public interface CMQueryResult extends Iterable<CMQueryRow> {

	final CMQueryResult EMPTY = new CMQueryResult() {

		@Override
		public Iterator<CMQueryRow> iterator() {
			return Iterators.emptyIterator();
		}

		@Override
		public CMQueryRow getOnlyRow() throws NoSuchElementException {
			throw new NoSuchElementException();
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public int totalSize() {
			return 0;
		}

	};

	int size();

	boolean isEmpty();

	int totalSize();

	/**
	 * Returns the first and only row in the result.
	 * 
	 * @return the first and only row in the result
	 * 
	 * @throws NoSuchElementException
	 *             if there is no unique element
	 */
	CMQueryRow getOnlyRow() throws NoSuchElementException;

}
