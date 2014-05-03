package org.cmdbuild.dao.query.clause;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;

public class AnyAttribute extends QueryAliasAttribute {

	private static final String ANY = "*";

	private AnyAttribute(final Alias entryTypeAlias) {
		super(entryTypeAlias, ANY);
	}

	public static AnyAttribute anyAttribute(final CMEntryType entryType) {
		return anyAttribute(EntryTypeAlias.canonicalAlias(entryType));
	}

	public static AnyAttribute anyAttribute(final String entryTypeName) {
		return anyAttribute(NameAlias.as(entryTypeName));
	}

	public static AnyAttribute anyAttribute(final Alias entryTypeAlias) {
		return new AnyAttribute(entryTypeAlias);
	}

	/*
	 * TODO: Should be replaced by anyAttribute(f) when it works
	 */
	public static Object[] anyAttribute(final CMFunction function, final Alias f) {
		final List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		for (final CMFunction.CMFunctionParameter p : function.getOutputParameters()) {
			attributes.add(attribute(f, p.getName()));
		}

		return attributes.toArray(new Object[attributes.size()]);
	}
}
