package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.From;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;

@SuppressWarnings("unchecked")
public class FromImpl extends CQLElementImpl implements From {

	boolean history = false;
	List<ClassDeclarationImpl> declarations = new ArrayList<ClassDeclarationImpl>();

	@Override
	public void setHistory(final boolean history) {
		this.history = history;
	}

	@Override
	public boolean isHistory() {
		return history;
	}

	@Override
	public void add(final ClassDeclaration classDecl) {
		declarations.add((ClassDeclarationImpl) classDecl);
	}

	@Override
	public ClassDeclarationImpl mainClass() {
		return declarations.get(0);
	}

	@Override
	public ClassDeclarationImpl searchClass(final String nameOrRef) {
		for (final ClassDeclarationImpl c : declarations) {
			if (c.isClass(nameOrRef)) {
				return c;
			}
		}
		return null;
	}

	@Override
	public DomainDeclarationImpl searchDomain(final String nameOrRef) {
		for (final ClassDeclarationImpl c : declarations) {
			final DomainDeclarationImpl out = c.searchDomain(nameOrRef);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	public void check() {
		for (final ClassDeclarationImpl c : declarations) {
			c.check();
		}
	}
}
