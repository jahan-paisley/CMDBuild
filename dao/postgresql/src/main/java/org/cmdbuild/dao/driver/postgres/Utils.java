package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.driver.postgres.quote.IdentQuoter;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.AliasVisitor;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;

public class Utils {

	private static final String SYSTEM_SEPARATOR = "_";
	private static final String USER_SEPARATOR = "#";

	private static final String QUOTE_ATTRIBUTE_FORMAT = "%s.%s";
	private static final String SYSTEM_ATTRIBUTE_NAME_FORMAT = SYSTEM_SEPARATOR + "%s" + SYSTEM_SEPARATOR + "%s";
	private static final String USER_ATTRIBUTE_NAME_FORMAT = "%s" + USER_SEPARATOR + "%s";

	private Utils() {
		// prevents instantiation
	}

	public static String quoteAttribute(final Alias tableAlias, final SystemAttributes attribute) {
		return quoteAttribute(tableAlias, attribute.getDBName());
	}

	public static String quoteAttribute(final Alias tableAlias, final String name) {
		return format(QUOTE_ATTRIBUTE_FORMAT, AliasQuoter.quote(tableAlias), IdentQuoter.quote(name));
	}

	public static String nameForSystemAttribute(final Alias alias, final SystemAttributes sa) {
		final StringBuilder entryTypeName = new StringBuilder();
		alias.accept(new AliasVisitor() {

			@Override
			public void visit(final EntryTypeAlias alias) {
				final CMIdentifier identifier = alias.getEntryType().getIdentifier();
				if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
					entryTypeName.append(identifier.getNameSpace()).append(AliasQuoter.NAMESPACE_LOCALNAME_SEPARATOR);
				}
				entryTypeName.append(identifier.getLocalName());
			}

			@Override
			public void visit(final NameAlias alias) {
				entryTypeName.append(alias.getName());
			}

		});
		return format(SYSTEM_ATTRIBUTE_NAME_FORMAT, entryTypeName.toString(), sa.getDBName());
	}

	public static String nameForUserAttribute(final Alias alias, final String name) {
		final StringBuilder entryTypeName = new StringBuilder();
		alias.accept(new AliasVisitor() {

			@Override
			public void visit(final EntryTypeAlias alias) {
				final CMIdentifier identifier = alias.getEntryType().getIdentifier();
				if (identifier.getNameSpace() != CMIdentifier.DEFAULT_NAMESPACE) {
					entryTypeName.append(identifier.getNameSpace()).append(AliasQuoter.NAMESPACE_LOCALNAME_SEPARATOR);
				}
				entryTypeName.append(identifier.getLocalName());
			}

			@Override
			public void visit(final NameAlias alias) {
				entryTypeName.append(alias.getName());
			}

		});
		return format(USER_ATTRIBUTE_NAME_FORMAT, entryTypeName.toString(), name);
	}

}
