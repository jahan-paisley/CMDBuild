package org.cmdbuild.dao.query.clause;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingDomain;

public class AnyDomain extends ForwardingDomain {

	private static final String ANY_STRING = "*";

	private static final AnyDomain ANY_DOMAIN = new AnyDomain();
	private static final DBIdentifier ANY_IDENTIFIER = new DBIdentifier(ANY_STRING);

	public static CMDomain anyDomain() {
		return ANY_DOMAIN;
	}

	private AnyDomain() {
		super(UnsupportedProxyFactory.of(CMDomain.class).create());
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CMIdentifier getIdentifier() {
		return ANY_IDENTIFIER;
	}

	@Override
	public String getName() {
		return getIdentifier().getLocalName();
	}

	@Override
	public String toString() {
		return ANY_STRING;
	}

}
