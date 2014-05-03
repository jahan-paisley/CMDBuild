package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public abstract class ForwardingDataView implements CMDataView {

	private final CMDataView delegate;

	protected ForwardingDataView(final CMDataView delegate) {
		this.delegate = delegate;
	}

	@Override
	public CMClass findClass(final Long id) {
		return delegate.findClass(id);
	}

	@Override
	public CMClass findClass(final String name) {
		return delegate.findClass(name);
	}

	@Override
	public CMClass findClass(final CMIdentifier identifier) {
		return delegate.findClass(identifier);
	}

	@Override
	public Iterable<? extends CMClass> findClasses() {
		return delegate.findClasses();
	}

	@Override
	public CMClass create(final CMClassDefinition definition) {
		return delegate.create(definition);
	}

	@Override
	public CMClass update(final CMClassDefinition definition) {
		return delegate.update(definition);
	}

	@Override
	public CMAttribute createAttribute(final CMAttributeDefinition definition) {
		return delegate.createAttribute(definition);
	}

	@Override
	public CMAttribute updateAttribute(final CMAttributeDefinition definition) {
		return delegate.updateAttribute(definition);
	}

	@Override
	public void delete(final CMAttribute attribute) {
		delegate.delete(attribute);
	}

	@Override
	public CMDomain findDomain(final Long id) {
		return delegate.findDomain(id);
	}

	@Override
	public CMDomain findDomain(final String name) {
		return delegate.findDomain(name);
	}
	
	@Override
	public CMDomain findDomain(final CMIdentifier identifier) {
		return delegate.findDomain(identifier);
	}

	@Override
	public Iterable<? extends CMDomain> findDomains() {
		return delegate.findDomains();
	}

	@Override
	public Iterable<? extends CMDomain> findDomainsFor(final CMClass type) {
		return delegate.findDomainsFor(type);
	}

	@Override
	public CMDomain create(final CMDomainDefinition definition) {
		return delegate.create(definition);
	}

	@Override
	public CMDomain update(final CMDomainDefinition definition) {
		return delegate.update(definition);
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return delegate.findFunctionByName(name);
	}

	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return delegate.findAllFunctions();
	}

	@Override
	public void delete(final CMEntryType entryType) {
		delegate.delete(entryType);
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		return delegate.createCardFor(type);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return delegate.update(card);
	}

	@Override
	public void delete(final CMCard card) {
		delegate.delete(card);
	}

	@Override
	public CMRelationDefinition createRelationFor(final CMDomain domain) {
		return delegate.createRelationFor(domain);
	}

	@Override
	public CMRelationDefinition update(final CMRelation relation) {
		return delegate.update(relation);
	}

	@Override
	public void delete(final CMRelation relation) {
		delegate.delete(relation);
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return delegate.select(attrDef);
	}

	@Override
	public CMQueryResult executeQuery(final QuerySpecs querySpecs) {
		return delegate.executeQuery(querySpecs);
	}

	@Override
	public void clear(final CMEntryType type) {
		delegate.clear(type);
	}

	@Override
	public CMClass getActivityClass() {
		return delegate.getActivityClass();
	}

	@Override
	public CMClass getReportClass() {
		return delegate.getReportClass();
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter) {
		return delegate.getAdditionalFiltersFor(classToFilter);
	}

}
