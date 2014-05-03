package org.cmdbuild.dao.driver.postgres.quote;

import static org.cmdbuild.dao.driver.postgres.Const.HISTORY_SUFFIX;

import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter.DomainIdentifier;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public class EntryTypeHistoryQuoter extends AbstractEntryTypeQuoter implements CMEntryTypeVisitor {

	public static String quote(final CMEntryType type) {
		return new EntryTypeHistoryQuoter(type).quote();
	}

	private String quotedTypeName;

	public EntryTypeHistoryQuoter(final CMEntryType entryType) {
		super(entryType);
	}

	@Override
	public String quote() {
		entryType.accept(this);
		return quotedTypeName;
	}

	private static class HistoryIdentifier implements CMIdentifier {

		private final CMIdentifier inner;

		public HistoryIdentifier(final CMEntryType entryType) {
			this(entryType.getIdentifier());
		}

		public HistoryIdentifier(final CMIdentifier identifier) {
			this.inner = identifier;
		}

		@Override
		public String getLocalName() {
			return inner.getLocalName() + HISTORY_SUFFIX;
		}

		@Override
		public String getNameSpace() {
			return inner.getNameSpace();
		}

	}

	@Override
	public void visit(final CMClass entryType) {
		quotedTypeName = quoteClassOrDomain(new HistoryIdentifier(entryType));
	}

	@Override
	public void visit(final CMDomain entryType) {
		quotedTypeName = quoteClassOrDomain(new HistoryIdentifier(new DomainIdentifier(entryType)));
	}

	@Override
	public void visit(final CMFunctionCall entryType) {
		throw new UnsupportedOperationException("Cannot specify history for functions");
	}

}
