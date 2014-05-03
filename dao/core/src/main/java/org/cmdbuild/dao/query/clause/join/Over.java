package org.cmdbuild.dao.query.clause.join;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;

public class Over {

	private final CMDomain domain;
	private final Alias alias;

	public static Over over(final CMDomain domain) {
		return over(domain, EntryTypeAlias.canonicalAlias(domain));
	}

	public static Over over(final CMDomain domain, final Alias alias) {
		return new Over(domain, alias);
	}

	private Over(final CMDomain domain, final Alias alias) {
		this.domain = domain;
		this.alias = alias;
	}

	public CMDomain getDomain() {
		return domain;
	}

	public Alias getAlias() {
		return alias;
	}

}
