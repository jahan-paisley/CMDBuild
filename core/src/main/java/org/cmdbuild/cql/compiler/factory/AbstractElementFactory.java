package org.cmdbuild.cql.compiler.factory;

import org.cmdbuild.cql.compiler.CQLElement;
import org.cmdbuild.cql.compiler.From;
import org.cmdbuild.cql.compiler.GroupBy;
import org.cmdbuild.cql.compiler.Limit;
import org.cmdbuild.cql.compiler.Offset;
import org.cmdbuild.cql.compiler.OrderBy;
import org.cmdbuild.cql.compiler.Query;
import org.cmdbuild.cql.compiler.Select;
import org.cmdbuild.cql.compiler.Where;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.from.FromElement;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.DomainMetaSelect;
import org.cmdbuild.cql.compiler.select.DomainObjectsSelect;
import org.cmdbuild.cql.compiler.select.FieldSelect;
import org.cmdbuild.cql.compiler.select.FunctionSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;
import org.cmdbuild.cql.compiler.where.DomainMetaReference;
import org.cmdbuild.cql.compiler.where.DomainObjectsReference;
import org.cmdbuild.cql.compiler.where.Field;
import org.cmdbuild.cql.compiler.where.Group;
import org.cmdbuild.cql.compiler.where.WhereElement;

/**
 * Abstract Factory that is responsible to create and configure the CQL elements
 */
public abstract class AbstractElementFactory {

	public Query createQuery() {
		return createConf(Query.class, null);
	};

	public Query createQuery(final Field parent) {
		return createConf(Query.class, parent);
	};

	public From createFrom(final Query query) {
		final From out = createConf(From.class, query);
		query.setFrom(out);
		return out;
	};

	public Select createSelect(final Query query) {
		final Select out = createConf(Select.class, query);
		query.setSelect(out);
		return out;
	};

	public Where createWhere(final Query query) {
		final Where out = createConf(Where.class, query);
		query.setWhere(out);
		return out;
	};

	public OrderBy createOrderBy(final Query query) {
		final OrderBy out = createConf(OrderBy.class, query);
		query.setOrderBy(out);
		return out;
	};

	public GroupBy createGroupBy(final Query query) {
		final GroupBy out = createConf(GroupBy.class, query);
		query.setGroupBy(out);
		return out;
	};

	public Limit createLimit(final Query query) {
		final Limit out = createConf(Limit.class, query);
		query.setLimit(out);
		return out;
	};

	public Offset createOffset(final Query query) {
		final Offset out = createConf(Offset.class, query);
		query.setOffet(out);
		return out;
	};

	public ClassDeclaration createClassDeclaration(final From from) {
		final ClassDeclaration out = createConf(ClassDeclaration.class, from);
		from.add(out);
		return out;
	};

	public DomainDeclaration createDomainDeclaration(final FromElement parent) {
		final DomainDeclaration out = createConf(DomainDeclaration.class, parent);
		if (parent instanceof DomainDeclaration) {
			((DomainDeclaration) parent).setSubdomain(out);
		}
		return out;
	};

	public ClassSelect createClassSelect(final Select select, final ClassDeclaration cdecl) {
		final ClassSelect out = createConf(ClassSelect.class, select);
		out.setDeclaration(cdecl);
		select.add(out);
		return out;
	};

	public DomainMetaSelect createDomainMetaSelect(final Select select, final DomainDeclaration ddecl) {
		final DomainMetaSelect out = createConf(DomainMetaSelect.class, select);
		out.setDeclaration(ddecl);
		select.add(out);
		return out;
	};

	public DomainObjectsSelect createDomainObjectsSelect(final Select select, final DomainDeclaration ddecl) {
		final DomainObjectsSelect out = createConf(DomainObjectsSelect.class, select);
		out.setDeclaration(ddecl);
		select.add(out);
		return out;
	};

	public FunctionSelect createFunctionSelect(final Select select) {
		final FunctionSelect out = createConf(FunctionSelect.class, select);
		select.add(out);
		return out;
	};

	public FieldSelect createFieldSelect(final SelectElement<?> element) {
		final FieldSelect out = createConf(FieldSelect.class, element);
		element.add(out);
		return out;
	};

	public Group createGroup(final WhereElement parent) {
		final Group out = createConf(Group.class, parent);
		parent.add(out);
		return out;
	};

	public DomainMetaReference createDomainMetaReference(final WhereElement parent, final DomainDeclaration declaration) {
		final DomainMetaReference out = createConf(DomainMetaReference.class, parent, declaration);
		parent.add(out);
		return out;
	};

	public DomainObjectsReference createDomainObjectsReference(final WhereElement parent,
			final DomainDeclaration declaration) {
		final DomainObjectsReference out = createConf(DomainObjectsReference.class, parent, declaration);
		parent.add(out);
		return out;
	};

	public Field createField(final WhereElement parent) {
		final Field out = createConf(Field.class, parent);
		parent.add(out);
		return out;
	};

	protected <T extends CQLElement> T createConf(final Class<T> theClass, final CQLElement parent,
			final Object... args) {
		return configure(create(theClass, parent, args), parent);
	}

	protected <T extends CQLElement> T configure(final T elm, final CQLElement parent) {
		beforeConfigure(elm);
		elm.setParent(parent);
		elm.setElementFactory(this);
		afterConfigure(elm);
		return elm;
	}

	/**
	 * Here an element must be instantiated.
	 * 
	 * @param <T>
	 * @param elementClass
	 * @param parent
	 * @param args
	 * @return
	 */
	public abstract <T extends CQLElement> T create(Class<T> elementClass, CQLElement parent, Object... args);

	public abstract Select defaultSelect(Query q);

	public abstract Where defaultWhere(Query q);

	public abstract OrderBy defaultOrderBy(Query q);

	public abstract Limit defaultLimit(Query q);

	public abstract Offset defaultOffset(Query q);

	protected void beforeConfigure(final CQLElement element) {
	}

	protected void afterConfigure(final CQLElement element) {
	}
}
