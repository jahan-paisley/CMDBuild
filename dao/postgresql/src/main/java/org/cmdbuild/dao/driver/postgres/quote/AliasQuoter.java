package org.cmdbuild.dao.driver.postgres.quote;

import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter.DomainIdentifier;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.AliasVisitor;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;

public class AliasQuoter implements Quoter {

	public static final String NAMESPACE_LOCALNAME_SEPARATOR = "_";

	public static String quote(final Alias alias) {
		return new AliasQuoter(alias).quote();
	}

	private final Alias alias;

	public AliasQuoter(final Alias alias) {
		this.alias = alias;
	}

	@Override
	public String quote() {
		final StringBuilder quoted = new StringBuilder();
		alias.accept(new AliasVisitor() {

			@Override
			public void visit(final EntryTypeAlias alias) {
				final StringBuilder entryTypeName = new StringBuilder();

				CMIdentifier identifier = alias.getEntryType().getIdentifier();
				if (alias.getEntryType() instanceof CMDomain) {
					identifier = new DomainIdentifier(identifier);
				}

				if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
					entryTypeName.append(identifier.getNameSpace()).append(NAMESPACE_LOCALNAME_SEPARATOR);
				}
				entryTypeName.append(identifier.getLocalName());
				quoted.append(IdentQuoter.quote(entryTypeName.toString()));
			}

			@Override
			public void visit(final NameAlias alias) {
				quoted.append(IdentQuoter.quote(alias.getName()));
			}
		});
		return quoted.toString();
	}

}
