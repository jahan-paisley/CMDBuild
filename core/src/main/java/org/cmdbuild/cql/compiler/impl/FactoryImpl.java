package org.cmdbuild.cql.compiler.impl;

import org.cmdbuild.cql.compiler.CQLElement;
import org.cmdbuild.cql.compiler.From;
import org.cmdbuild.cql.compiler.GroupBy;
import org.cmdbuild.cql.compiler.Limit;
import org.cmdbuild.cql.compiler.Offset;
import org.cmdbuild.cql.compiler.OrderBy;
import org.cmdbuild.cql.compiler.Query;
import org.cmdbuild.cql.compiler.Select;
import org.cmdbuild.cql.compiler.Where;
import org.cmdbuild.cql.compiler.factory.AbstractElementFactory;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.DomainMetaSelect;
import org.cmdbuild.cql.compiler.select.DomainObjectsSelect;
import org.cmdbuild.cql.compiler.select.FieldSelect;
import org.cmdbuild.cql.compiler.select.FunctionSelect;
import org.cmdbuild.cql.compiler.where.DomainMetaReference;
import org.cmdbuild.cql.compiler.where.DomainObjectsReference;
import org.cmdbuild.cql.compiler.where.Field;
import org.cmdbuild.cql.compiler.where.Group;
import org.cmdbuild.logger.Log;

@SuppressWarnings("unchecked")
public class FactoryImpl extends AbstractElementFactory {

	public static boolean CmdbuildCheck = false;

	@Override
	public Limit defaultLimit(final Query q) {
		// no limit
		return null;
	}

	@Override
	public Offset defaultOffset(final Query q) {
		((QueryImpl) q).setOffset(0);
		return null;
	}

	@Override
	public OrderBy defaultOrderBy(final Query q) {
		final OrderByImpl orderby = (OrderByImpl) this.createOrderBy(q);
		orderby.setDefault();
		return orderby;
	}

	@Override
	public Select defaultSelect(final Query q) {
		final SelectImpl sel = (SelectImpl) this.createSelect(q);
		sel.setDefault();
		return sel;
	}

	@Override
	public Where defaultWhere(final Query q) {
		final WhereImpl where = (WhereImpl) this.createWhere(q);
		where.setDefault();
		return where;
	}

	@Override
	public <T extends CQLElement> T create(final Class<T> c, final CQLElement parent, final Object... args) {

		if (is(c, Query.class)) {
			return (T) new QueryImpl();
		}
		if (is(c, From.class)) {
			return (T) new FromImpl();
		}
		if (is(c, Select.class)) {
			return (T) new SelectImpl();
		}
		if (is(c, Where.class)) {
			return (T) new WhereImpl();
		}
		if (is(c, GroupBy.class)) {
			return (T) new GroupByImpl();
		}
		if (is(c, OrderBy.class)) {
			return (T) new OrderByImpl();
		}
		if (is(c, Limit.class)) {
			return (T) parent;
		}
		if (is(c, Offset.class)) {
			return (T) parent;
		}
		if (is(c, ClassDeclaration.class)) {
			return (T) new ClassDeclarationImpl();
		}
		if (is(c, DomainDeclaration.class)) {
			return (T) new DomainDeclarationImpl();
		}
		if (is(c, ClassSelect.class)) {
			return (T) new ClassSelectImpl();
		}
		if (is(c, DomainMetaSelect.class)) {
			return (T) new DomainMetaSelectImpl();
		}
		if (is(c, DomainObjectsSelect.class)) {
			return (T) new DomainObjectsSelectImpl();
		}
		if (is(c, FieldSelect.class)) {
			return (T) new FieldSelectImpl();
		}
		if (is(c, FunctionSelect.class)) {
			return (T) new FunctionSelectImpl();
		}
		if (is(c, Field.class)) {
			return (T) new FieldImpl();
		}
		if (is(c, DomainObjectsReference.class)) {
			return (T) new DomainObjectsReferenceImpl();
		}
		if (is(c, DomainMetaReference.class)) {
			return (T) new DomainMetaReferenceImpl();
		}
		if (is(c, Group.class)) {
			return (T) new GroupImpl();
		}

		Log.CMDBUILD.debug("CQL Default element impl for class: " + c.getCanonicalName());
		return (T) new CQLElementImpl();
	}

	private boolean is(final Class a, final Class b) {
		return a == b;
	}

}
