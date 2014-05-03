package org.cmdbuild.logic.mapping;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface FilterMapper {

	/**
	 * @deprecated to be removed
	 */
	@Deprecated
	final class JoinElement {

		public final String domain;
		public final String source;
		public final String destination;
		public final boolean left;

		private JoinElement(final String domain, final String source, final String destination, final boolean left) {
			this.domain = domain;
			this.source = source;
			this.destination = destination;
			this.left = left;
		}

		public static JoinElement newInstance(final String domain, final String source, final String destination,
				final boolean left) {
			return new JoinElement(domain, source, destination, left);
		}

	}

	CMEntryType entryType();

	Iterable<WhereClause> whereClauses();

	Iterable<JoinElement> joinElements();

}
