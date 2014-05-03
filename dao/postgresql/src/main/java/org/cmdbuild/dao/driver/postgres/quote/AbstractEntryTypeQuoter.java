package org.cmdbuild.dao.driver.postgres.quote;

import static java.lang.String.format;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public abstract class AbstractEntryTypeQuoter implements Quoter {

	final String NAMESPACE_LOCALNAME_SEPARATOR = ".";

	protected final CMEntryType entryType;

	public AbstractEntryTypeQuoter(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	protected String quoteClassOrDomain(final CMIdentifier identifier) {
		final String quotedTypeName;
		if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
			quotedTypeName = format("%s%s%s", //
					IdentQuoter.quote(identifier.getNameSpace()), //
					NAMESPACE_LOCALNAME_SEPARATOR, //
					IdentQuoter.quote(identifier.getLocalName()));
		} else {
			quotedTypeName = IdentQuoter.quote(identifier.getLocalName());
		}
		return quotedTypeName;
	}

}
