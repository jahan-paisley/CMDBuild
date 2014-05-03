package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.select.DomainMetaSelect;

public class DomainMetaSelectImpl extends SelectElementImpl<DomainDeclaration> implements DomainMetaSelect {

	public void check() {
		if (FactoryImpl.CmdbuildCheck) {
			// TODO: when the meta attributes for the domains will be ready,
			// this method should be implemented..
		}
	}
}
