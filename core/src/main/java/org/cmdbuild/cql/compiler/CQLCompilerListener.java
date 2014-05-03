package org.cmdbuild.cql.compiler;

import java.util.Stack;

import org.cmdbuild.cql.CQLBuilderAdaptor;
import org.cmdbuild.cql.compiler.factory.AbstractElementFactory;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.from.FromElement;
import org.cmdbuild.cql.compiler.select.FieldSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;
import org.cmdbuild.cql.compiler.where.DomainMetaReference;
import org.cmdbuild.cql.compiler.where.DomainObjectsReference;
import org.cmdbuild.cql.compiler.where.Field;
import org.cmdbuild.cql.compiler.where.Group;
import org.cmdbuild.cql.compiler.where.WhereElement;
import org.cmdbuild.cql.compiler.where.fieldid.ComplexFieldId;
import org.cmdbuild.cql.compiler.where.fieldid.FieldId;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId;
import org.cmdbuild.cql.compiler.where.fieldid.SimpleFieldId;

/**
 * Used to manage the creation of a Query object.<br>
 * Basically, used in conjunction with the CQLCompilerBuilder, is a Builder for
 * Query objects.
 */
public class CQLCompilerListener extends CQLBuilderAdaptor {

	/**
	 * used to generate custom AS for, e.g., domains defined in the WHERE
	 * statement
	 */
	int customAsCounter = 0;

	private String getNextCustomAs(final String name) {
		return name + (++customAsCounter);
	}

	/**
	 * Hold the parser state for the current Expression, needed for nested
	 * expressions.
	 */
	private class QueryScope {
		Query query;
		Stack<DomainDeclaration> domains = new Stack<DomainDeclaration>();

		DomainDeclaration curDomainDecl() {
			if (domains.isEmpty()) {
				return null;
			}
			return domains.peek();
		}

		WhereType curDomainWhereType;
		boolean curDomainIsNot;

		@SuppressWarnings("unchecked")
		SelectElement curSelectElement;

		Stack<WhereElement> wheres = new Stack<WhereElement>();
		Field curField;
	}

	AbstractElementFactory factory;
	Query root = null;

	public Query getRootQuery() {
		return root;
	}

	//
	Stack<QueryScope> scope = new Stack<QueryScope>();

	QueryScope cur() {
		return scope.peek();
	}

	void push() {
		scope.push(new QueryScope());
	}

	void pop() {
		scope.pop();
	}

	Query curQuery() {
		return cur().query;
	}

	WhereElement curWhere() {
		return cur().wheres.peek();
	}

	public void setFactory(final AbstractElementFactory factory) {
		this.factory = factory;
	}

	@Override
	public void startExpression() {
		push();
		cur().query = factory.createQuery();
		if (root == null) {
			root = cur().query;
		}
	}

	@Override
	public void endExpression() {
		pop();
	}

	@Override
	public void startFrom(final boolean history) {
		final From f = factory.createFrom(curQuery());
		f.setHistory(history);
	}

	@Override
	public void startSelect() {
		factory.createSelect(curQuery());
	}

	@Override
	public void addFromClass(final String className, final String classAs) {
		final ClassDeclaration cdecl = factory.createClassDeclaration(curQuery().getFrom());
		cdecl.setName(className);
		cdecl.setAs(classAs);
	}

	@Override
	public void startFromDomain(final String scopeReference, final String domainName, final String domainAs,
			final DomainDirection direction) {
		startFromDomain(scopeReference, domainName, -1, domainAs, direction);
	}

	@Override
	public void startFromDomain(final String scopeReference, final int domainId, final String domainAs,
			final DomainDirection direction) {
		startFromDomain(scopeReference, null, domainId, domainAs, direction);
	}

	@Override
	public void endFromDomain() {
		cur().domains.pop();
	}

	private void startFromDomain(final String scopeReference, final String domainName, final int domainId,
			final String domainAs, final DomainDirection direction) {
		DomainDeclaration domDecl;
		if (scopeReference == null) {
			if (cur().curDomainDecl() == null) {
				domDecl = factory.createDomainDeclaration(curQuery().getFrom().mainClass());
			} else {
				domDecl = factory.createDomainDeclaration(cur().curDomainDecl());
				cur().curDomainDecl().setSubdomain(domDecl);
			}
		} else {
			FromElement parent = curQuery().getFrom().searchClass(scopeReference);
			if (parent == null) {
				parent = curQuery().getFrom().searchDomain(scopeReference);
			}
			if (parent == null) {
				throw new RuntimeException("Scope reference " + scopeReference + " for domain " + domainName + ":"
						+ domainId + " as " + domainAs + " was not found!");
			}
			domDecl = factory.createDomainDeclaration(parent);
		}
		if (domainName != null) {
			domDecl.setName(domainName);
		} else {
			domDecl.setId(domainId);
		}
		domDecl.setDirection(direction);
		domDecl.setAs(domainAs);
		cur().domains.add(domDecl);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void startSelectFromClass(final String classNameOrReference) {
		final ClassDeclaration cdecl = curQuery().getFrom().searchClass(classNameOrReference);
		if (cdecl == null) {
			throw new RuntimeException("Class declaration for " + classNameOrReference + " was not found!");
		}
		cur().curSelectElement = curQuery().getSelect().getOrCreate(cdecl);
		cur().curSelectElement.setDeclaration(cdecl);
	}

	@Override
	public void endSelectFromClass() {
		cur().curSelectElement = null;
	}

	@Override
	public void startSelectFromDomain(final String domainNameOrReference) {
		final DomainDeclaration decl = curQuery().getFrom().searchDomain(domainNameOrReference);
		if (decl == null) {
			throw new RuntimeException("Domain declaration for " + domainNameOrReference + " was not found!");
		}
		cur().domains.add(decl);
	}

	@Override
	public void endSelectFromDomain() {
		cur().domains.pop();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void startSelectFromDomainMeta() {
		cur().curSelectElement = curQuery().getSelect().getMetaOrCreate(cur().curDomainDecl());
		cur().curSelectElement.setDeclaration(cur().curDomainDecl());
	}

	@Override
	public void endSelectFromDomainMeta() {
		cur().curSelectElement = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void startSelectFromDomainObjects() {
		cur().curSelectElement = curQuery().getSelect().getObjectsOrCreate(cur().curDomainDecl());
		cur().curSelectElement.setDeclaration(cur().curDomainDecl());
	}

	@Override
	public void endSelectFromDomainObjects() {
		cur().curSelectElement = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addSelectAttribute(final String attributeName, final String attributeAs,
			final String classNameOrReference) {
		FieldSelect fsel = null;
		if (cur().curSelectElement != null) {
			fsel = factory.createFieldSelect(cur().curSelectElement);
		} else {
			ClassDeclaration cdecl;
			if (classNameOrReference != null) {
				cdecl = curQuery().getFrom().searchClass(classNameOrReference);
			} else {
				cdecl = curQuery().getFrom().mainClass();
			}
			if (cdecl == null) {
				throw new RuntimeException("Class declaration for " + classNameOrReference + " was not found!");
			}
			final SelectElement selElm = curQuery().getSelect().getOrCreate(cdecl);
			fsel = factory.createFieldSelect(selElm);
		}
		fsel.setName(attributeName);
		fsel.setAs(attributeAs);
	}

	@Override
	public void startSelectFunction(final String functionName, final String functionAs) {
		cur().curSelectElement = factory.createFunctionSelect(curQuery().getSelect());
	}

	@Override
	public void endSelectFunction() {
		cur().curSelectElement = null;
	}

	@Override
	public void startWhere() {
		factory.createWhere(curQuery());
		cur().wheres.add(curQuery().getWhere());
	}

	@Override
	public void startGroup(final WhereType type, final boolean isNot) {
		final Group group = factory.createGroup(curWhere());
		group.setType(type);
		group.setIsNot(isNot);
		cur().wheres.push(group);
	}

	@Override
	public void endGroup() {
		cur().wheres.pop();
	}

	@Override
	public void startDomain(final WhereType type, final String scopeReference, final int domainId,
			final DomainDirection direction, final boolean isNot) {
		cur().curDomainIsNot = isNot;
		cur().curDomainWhereType = type;
		startFromDomain(scopeReference, null, domainId, getNextCustomAs(domainId + ""), direction);
	}

	@Override
	public void startDomain(final WhereType type, final String scopeReference, final String domainName,
			final DomainDirection direction, final boolean isNot) {
		cur().curDomainIsNot = isNot;
		cur().curDomainWhereType = type;
		startFromDomain(scopeReference, domainName, -1, getNextCustomAs(domainName), direction);
	}

	@Override
	public void endDomain() {
		cur().domains.pop();
	}

	@Override
	public void endDomainRef() {
		cur().domains.pop();
	}

	@Override
	public void startDomainRef(final WhereType type, final String domainReference, final boolean isNot) {
		cur().domains.add(curQuery().getFrom().searchDomain(domainReference));
		cur().curDomainIsNot = isNot;
		cur().curDomainWhereType = type;
	}

	@Override
	public void startDomainMeta() {
		final DomainMetaReference dommeta = factory.createDomainMetaReference(curWhere(), cur().curDomainDecl());
		dommeta.setScope(cur().curDomainDecl());
		dommeta.setIsNot(cur().curDomainIsNot);
		dommeta.setType(cur().curDomainWhereType);
		cur().wheres.push(dommeta);
	}

	@Override
	public void endDomainMeta() {
		cur().wheres.pop();
	}

	@Override
	public void startDomainObjects() {
		final DomainObjectsReference domobjs = factory.createDomainObjectsReference(curWhere(), cur().curDomainDecl());
		domobjs.setScope(cur().curDomainDecl());
		domobjs.setIsNot(cur().curDomainIsNot);
		domobjs.setType(cur().curDomainWhereType);
		cur().wheres.push(domobjs);
	}

	@Override
	public void endDomainObjects() {
		cur().wheres.pop();
	}

	private FromElement searchFrom(final String classOrDomainNameOrRef) {
		FromElement from = null;
		if (classOrDomainNameOrRef == null) {
			from = curQuery().getFrom().mainClass();
		} else {
			from = curQuery().getFrom().searchClass(classOrDomainNameOrRef);
			if (from == null) {
				from = curQuery().getFrom().searchDomain(classOrDomainNameOrRef);
			}
			if (from == null) {
				throw new RuntimeException("Cannot find declaration for " + classOrDomainNameOrRef);
			}
		}
		return from;
	}

	private void createField(final FieldId id, final WhereType type, final boolean isNot,
			final String classOrDomainNameOrRef, final FieldOperator operator) {
		final FromElement from = searchFrom(classOrDomainNameOrRef);
		final Field field = factory.createField(curWhere());
		field.setScope(from);
		field.setId(id);
		field.setOperator(operator);
		field.setIsNot(isNot);
		field.setType(type);
		cur().curField = field;
	}

	@Override
	public void startSimpleField(final WhereType type, final boolean isNot, final String classOrDomainNameOrRef,
			final String fieldId, final FieldOperator operator) {
		final FromElement from = searchFrom(classOrDomainNameOrRef);
		createField(new SimpleFieldId(fieldId, from), type, isNot, classOrDomainNameOrRef, operator);
	}

	@Override
	public void startComplexField(final WhereType type, final boolean isNot, final String classOrDomainNameOrRef,
			final String[] fieldPath, final FieldOperator operator) {
		final FromElement from = searchFrom(classOrDomainNameOrRef);
		createField(new ComplexFieldId(fieldPath, from), type, isNot, classOrDomainNameOrRef, operator);
	}

	@Override
	public void startLookupField(final WhereType type, final boolean isNot, final String classOrDomainNameOrRef,
			final String fieldId, final LookupOperator[] lookupPath, final FieldOperator operator) {
		final FromElement from = searchFrom(classOrDomainNameOrRef);
		createField(new LookupFieldId(fieldId, lookupPath, from), type, isNot, classOrDomainNameOrRef, operator);
	}

	@Override
	public void endField() {
		cur().curField = null;
	}

	@Override
	public void startValue(final FieldValueType type) {
		cur().curField.nextValueType(type);
	}

	@Override
	public void value(final Object o) {
		cur().curField.nextValue(o);
		if (o instanceof FieldInputValue) {
			curQuery().addVariable((FieldInputValue) o);
		}
	}

	@Override
	public void defaultWhere() {
		factory.defaultWhere(cur().query);
	}

	@Override
	public void defaultLimit() {
		factory.defaultLimit(cur().query);
	}

	@Override
	public void defaultSelect() {
		factory.defaultSelect(cur().query);
	}

	@Override
	public void defaultOffset() {
		factory.defaultOffset(cur().query);
	}

	@Override
	public void defaultOrderBy() {
		factory.defaultOrderBy(cur().query);
	}

	@Override
	public void setLimit(final FieldInputValue limit) {
		factory.createLimit(curQuery()).setLimit(limit);
	}

	@Override
	public void setLimit(final int limit) {
		factory.createLimit(curQuery()).setLimit(limit);
	}

	@Override
	public void setOffset(final FieldInputValue offset) {
		factory.createOffset(curQuery()).setOffset(offset);
	}

	@Override
	public void setOffset(final int offset) {
		factory.createOffset(curQuery()).setOffset(offset);
	}

}
