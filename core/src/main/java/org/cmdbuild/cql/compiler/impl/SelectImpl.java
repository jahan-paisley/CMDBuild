package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.Select;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.DomainMetaSelect;
import org.cmdbuild.cql.compiler.select.DomainObjectsSelect;
import org.cmdbuild.cql.compiler.select.FunctionSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;

@SuppressWarnings("unchecked")
public class SelectImpl extends CQLElementImpl implements Select {
	boolean isDefault = false;

	public void setDefault() {
		isDefault = true;
	}

	public boolean isDefault() {
		return isDefault;
	}

	boolean isAll = false;
	List<SelectElement> elements = new ArrayList<SelectElement>();

	@Override
	public void add(final FunctionSelect fun) {
		elements.add(fun);
	}

	@Override
	public void add(final ClassSelect classSelect) {
		elements.add(classSelect);
	}

	@Override
	public void add(final DomainMetaSelect domMeta) {
		elements.add(domMeta);
	}

	@Override
	public void add(final DomainObjectsSelect domObjs) {
		elements.add(domObjs);
	}

	@Override
	public ClassSelectImpl get(final ClassDeclaration classDecl) {
		for (final SelectElement el : elements) {
			if (el instanceof ClassSelectImpl) {
				if (((ClassSelectImpl) el).declaration.equals(classDecl)) {
					return (ClassSelectImpl) el;
				}
			}
		}
		return null;
	}

	@Override
	public DomainMetaSelectImpl getMeta(final DomainDeclaration domainDecl) {
		for (final SelectElement el : elements) {
			if (el instanceof DomainMetaSelectImpl) {
				if (((DomainMetaSelectImpl) el).declaration.equals(domainDecl)) {
					return (DomainMetaSelectImpl) el;
				}
			}
		}
		return null;
	}

	@Override
	public DomainObjectsSelectImpl getObjects(final DomainDeclaration domainDecl) {
		for (final SelectElement el : elements) {
			if (el instanceof DomainObjectsSelectImpl) {
				if (((DomainObjectsSelectImpl) el).declaration.equals(domainDecl)) {
					return (DomainObjectsSelectImpl) el;
				}
			}
		}
		return null;
	}

	@Override
	public DomainMetaSelectImpl getMetaOrCreate(final DomainDeclaration domainDecl) {
		DomainMetaSelectImpl out = getMeta(domainDecl);
		if (out == null) {
			out = (DomainMetaSelectImpl) this.factory.createDomainMetaSelect(this, domainDecl);
		}
		return out;
	}

	@Override
	public DomainObjectsSelectImpl getObjectsOrCreate(final DomainDeclaration domainDecl) {
		DomainObjectsSelectImpl out = getObjects(domainDecl);
		if (out == null) {
			out = (DomainObjectsSelectImpl) this.factory.createDomainObjectsSelect(this, domainDecl);
		}
		return out;
	}

	@Override
	public ClassSelectImpl getOrCreate(final ClassDeclaration classDecl) {
		ClassSelectImpl out = get(classDecl);
		if (out == null) {
			out = (ClassSelectImpl) this.factory.createClassSelect(this, classDecl);
		}
		return out;
	}

	@Override
	public boolean isSelectAll() {
		return isAll;
	}

	@Override
	public void setSelectAll() {
		isAll = true;
	}

	@Override
	public List<SelectElement> getElements() {
		return elements;
	}

}
