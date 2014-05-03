package org.cmdbuild.cql.compiler.impl;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.cql.CQLBuilderListener.DomainDirection;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.exception.ORMException;

@SuppressWarnings("unchecked")
public class DomainDeclarationImpl extends CQLElementImpl implements DomainDeclaration {
	String as;
	DomainDirection direction;

	int id = -1;
	String name;

	DomainDeclarationImpl subDomain = null;

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DomainDeclarationImpl)) {
			return false;
		}
		final DomainDeclarationImpl o = (DomainDeclarationImpl) obj;
		if (direction != o.direction) {
			return false;
		}
		if (name != null) {
			if (!name.equals(o.name)) {
				return false;
			}
		} else if (o.name != null) {
			return false;
		}
		if (id != o.id) {
			return false;
		}
		if (as != null) {
			if (!as.equals(o.as)) {
				return false;
			}
		} else if (o.as != null) {
			return false;
		}
		if (subDomain != null) {
			if (!subDomain.equals(o.subDomain)) {
				return false;
			}
		} else if (o.subDomain != null) {
			return false;
		}

		return true;
	}

	@Override
	public String getAs() {
		return as;
	}

	@Override
	public DomainDirection getDirection() {
		return direction;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DomainDeclarationImpl getSubdomain() {
		return subDomain;
	}

	@Override
	public DomainDeclarationImpl searchDomain(final String nameOrRef) {
		if (this.name != null && this.name.equals(nameOrRef)) {
			return this;
		}
		if (this.as != null && this.as.equals(nameOrRef)) {
			return this;
		}
		if (this.subDomain != null) {
			return this.subDomain.searchDomain(nameOrRef);
		}
		return null;
	}

	@Override
	public void setAs(final String domainAs) {
		this.as = domainAs;
	}

	@Override
	public void setDirection(final DomainDirection direction) {
		this.direction = direction;
	}

	@Override
	public void setId(final int domainId) {
		this.id = domainId;
	}

	@Override
	public void setName(final String domainName) {
		this.name = domainName;
	}

	@Override
	public void setSubdomain(final DomainDeclaration subdomain) {
		this.subDomain = (DomainDeclarationImpl) subdomain;
	}

	private CMDomain getIDomain(final CMDataView dataView) {
		final CMDataView _dataView = (dataView == null) ? applicationContext().getBean(DBDataView.class) : dataView;
		if (this.id > 0) {
			return _dataView.findDomain(Long.valueOf(id));
		} else {
			return _dataView.findDomain(name);
		}
	}

	private CMClass getEndClassTable() {
		return getClassTable(false, (CMDataView) null);
	}

	public CMClass getEndClassTable(final CMDataView dataView) {
		return getClassTable(false, dataView);
	}

	protected CMClass getClassTable(final boolean start, final CMDataView dataView) {
		final CMDomain domain = getIDomain(dataView);
		CMClass t = null;
		if (this.parent instanceof ClassDeclarationImpl) {
			final ClassDeclarationImpl p = parentAs();
			t = p.getClassTable(dataView);
		} else {
			final DomainDeclarationImpl p = parentAs();
			t = p.getEndClassTable(dataView);
		}

		if (!domain.getClass1().isAncestorOf(t) && !domain.getClass2().isAncestorOf(t)) {
			throw new RuntimeException("Table " + t.getName() + " not found for domain " + domain.getName());
		}

		if (start) {
			return t;
		}
		switch (direction) {
		case INVERSE:
			return domain.getClass1();
		default:
			try {
				if (domain.getClass1().isAncestorOf(t)) {
					return domain.getClass2();
				} else {
					return domain.getClass1();
				}
			} catch (final ORMException exc) {
				if (ORMException.ORMExceptionType.ORM_AMBIGUOUS_DIRECTION == exc.getExceptionType()) {
					return domain.getClass2();
				}
				throw exc;
			}
		}

	}

	public CMDomain getDirectedDomain(final CMDataView dataView) {
		return getIDomain(dataView);
	}

	public void check() {
		if (FactoryImpl.CmdbuildCheck) {
			getEndClassTable(); // this check the existence of the domain and
								// that the tables are consistent
			if (subDomain != null) {
				subDomain.check();
			}
		}
	}

}
