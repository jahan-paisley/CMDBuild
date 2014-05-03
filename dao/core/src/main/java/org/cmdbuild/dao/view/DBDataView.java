package org.cmdbuild.dao.view;

import static java.lang.String.format;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.collect.Lists;

public class DBDataView extends AbstractDataView {

	public static interface DBClassDefinition extends CMClassDefinition {

		@Override
		public DBClass getParent();

	}

	public static interface DBAttributeDefinition extends CMAttributeDefinition {

		@Override
		DBEntryType getOwner();

	}

	public static interface DBDomainDefinition extends CMDomainDefinition {

		@Override
		public DBClass getClass1();

		@Override
		public DBClass getClass2();

	}

	private static final Iterable<? extends WhereClause> TRUE_ONLY_WHERE_CLAUSES = Arrays.asList(trueWhereClause());

	private final DBDriver driver;

	public DBDataView(final DBDriver driver) {
		this.driver = driver;
	}

	@Override
	public DBClass findClass(final Long id) {
		return driver.findClass(id);
	}

	@Override
	public DBClass findClass(final String name) {
		return driver.findClass(name);
	}

	@Override
	public DBClass findClass(final CMIdentifier identifier) {
		return driver.findClass(identifier.getLocalName(), identifier.getNameSpace());
	}

	@Override
	public Iterable<DBClass> findClasses() {
		return driver.findAllClasses();
	}

	@Override
	public DBClass create(final CMClassDefinition definition) {
		return driver.createClass(adaptDefinition(definition));
	}

	@Override
	public DBClass update(final CMClassDefinition definition) {
		return driver.updateClass(adaptDefinition(definition));
	}

	private DBClassDefinition adaptDefinition(final CMClassDefinition definition) {
		return new DBClassDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return definition.getIdentifier();
			}

			@Override
			public Long getId() {
				return definition.getId();
			}

			@Override
			public String getDescription() {
				return definition.getDescription();
			}

			@Override
			public DBClass getParent() {
				return cmToDbClass(definition.getParent());
			}

			@Override
			public boolean isSuperClass() {
				return definition.isSuperClass();
			}

			@Override
			public boolean isActive() {
				return definition.isActive();
			}

			@Override
			public boolean isHoldingHistory() {
				return definition.isHoldingHistory();
			}

			@Override
			public boolean isUserStoppable() {
				return definition.isUserStoppable();
			}

			@Override
			public boolean isSystem() {
				return definition.isSystem();
			}
		};
	}

	@Override
	public DBAttribute createAttribute(final CMAttributeDefinition definition) {
		return driver.createAttribute(adaptDefinition(definition));
	}

	@Override
	public DBAttribute updateAttribute(final CMAttributeDefinition definition) {
		return driver.updateAttribute(adaptDefinition(definition));
	}

	private DBAttributeDefinition adaptDefinition(final CMAttributeDefinition definition) {
		return new DBAttributeDefinition() {

			@Override
			public String getName() {
				return definition.getName();
			}

			@Override
			public DBEntryType getOwner() {
				return cmToDbEntryType(definition.getOwner());
			}

			@Override
			public CMAttributeType<?> getType() {
				return definition.getType();
			}

			@Override
			public String getDescription() {
				return definition.getDescription();
			}

			@Override
			public String getDefaultValue() {
				return definition.getDefaultValue();
			}

			@Override
			public boolean isDisplayableInList() {
				return definition.isDisplayableInList();
			}

			@Override
			public boolean isMandatory() {
				return definition.isMandatory();
			}

			@Override
			public boolean isUnique() {
				return definition.isUnique();
			}

			@Override
			public boolean isActive() {
				return definition.isActive();
			}

			@Override
			public Mode getMode() {
				return definition.getMode();
			}

			@Override
			public int getIndex() {
				return definition.getIndex();
			}

			@Override
			public String getGroup() {
				return definition.getGroup();
			}

			@Override
			public int getClassOrder() {
				return definition.getClassOrder();
			}

			@Override
			public String getEditorType() {
				return definition.getEditorType();
			}

			@Override
			public String getFilter() {
				return definition.getFilter();
			}

			@Override
			public String getForeignKeyDestinationClassName() {
				return definition.getForeignKeyDestinationClassName();
			}

		};
	}

	@Override
	public void delete(final CMAttribute attribute) {
		driver.deleteAttribute(cmToDbAttribute(attribute));
	}

	@Override
	public Iterable<DBDomain> findDomains() {
		return driver.findAllDomains();
	}

	@Override
	public Iterable<DBDomain> findDomainsFor(final CMClass cmClass) {
		final List<DBDomain> domainsForClass = Lists.newArrayList();
		for (final DBDomain d : findDomains()) {
			if (d.getClass1().isAncestorOf(cmClass) || d.getClass2().isAncestorOf(cmClass)) {

				domainsForClass.add(d);
			}
		}
		return domainsForClass;
	}

	@Override
	public DBDomain findDomain(final Long id) {
		return driver.findDomain(id);
	}

	@Override
	public DBDomain findDomain(final String name) {
		return driver.findDomain(name);
	}
	
	@Override
	public DBDomain findDomain(final CMIdentifier identifier) {
		return driver.findDomain(identifier.getLocalName(), identifier.getNameSpace());
	}

	@Override
	public DBDomain create(final CMDomainDefinition definition) {
		return driver.createDomain(adaptDefinition(definition));
	}

	@Override
	public DBDomain update(final CMDomainDefinition definition) {
		return driver.updateDomain(adaptDefinition(definition));
	}

	private DBDomainDefinition adaptDefinition(final CMDomainDefinition definition) {
		return new DBDomainDefinition() {

			@Override
			public CMIdentifier getIdentifier() {
				return definition.getIdentifier();
			}

			@Override
			public Long getId() {
				return definition.getId();
			}

			@Override
			public DBClass getClass1() {
				return cmToDbClass(definition.getClass1());
			}

			@Override
			public DBClass getClass2() {
				return cmToDbClass(definition.getClass2());
			}

			@Override
			public String getDescription() {
				return definition.getDescription();
			}

			@Override
			public String getDirectDescription() {
				return definition.getDirectDescription();
			}

			@Override
			public String getInverseDescription() {
				return definition.getInverseDescription();
			}

			@Override
			public String getCardinality() {
				return definition.getCardinality();
			}

			@Override
			public boolean isMasterDetail() {
				return definition.isMasterDetail();
			}

			@Override
			public String getMasterDetailDescription() {
				return definition.getMasterDetailDescription();
			}

			@Override
			public boolean isActive() {
				return definition.isActive();
			}

		};
	}

	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return driver.findAllFunctions();
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return driver.findFunction(name);
	}

	@Override
	public void delete(final CMEntryType entryType) {
		if (entryType == null) {
			return;
		}
		entryType.accept(new CMEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				driver.deleteClass(cmToDbClass(type));
			}

			@Override
			public void visit(final CMDomain type) {
				driver.deleteDomain(cmToDbDomain(type));
			}

			@Override
			public void visit(final CMFunctionCall type) {
				throw new UnsupportedOperationException("function calls cannot be deleted");
			}

		});
	}

	@Override
	public DBCard createCardFor(final CMClass type) {
		final DBClass dbType = findClass(type.getId());
		return DBCard.newInstance(driver, dbType);
	}

	@Override
	public DBCard update(final CMCard card) {
		CMIdentifier identifier = card.getType().getIdentifier();
		final DBClass dbType = findClass(identifier);		
		final DBCard dbCard = DBCard.newInstance(driver, dbType, card.getId());
		for (final Entry<String, Object> entry : card.getAllValues()) {
			dbCard.set(entry.getKey(), entry.getValue());
		}
		return dbCard;
	}

	@Override
	public void delete(final CMCard card) {
		CMIdentifier identifier = card.getType().getIdentifier();
		final DBClass dbType = findClass(identifier);
		final DBCard dbCard = DBCard.newInstance(driver, dbType, card.getId());
		driver.delete(dbCard);
	}

	@Override
	public CMQueryResult executeQuery(final QuerySpecs querySpecs) {
		return driver.query(querySpecs);
	}

	private DBAttribute cmToDbAttribute(final CMAttribute attribute) {
		final DBAttribute dbAttribute;
		if (attribute == null) {
			dbAttribute = null;
		} else if (attribute instanceof DBClass) {
			dbAttribute = DBAttribute.class.cast(attribute);
		} else {
			final DBEntryType owner = cmToDbEntryType(attribute.getOwner());
			dbAttribute = owner.getAttribute(attribute.getName());
			assert dbAttribute != null;
		}
		return dbAttribute;
	}

	@Override
	public CMRelationDefinition createRelationFor(final CMDomain domain) {
		final DBDomain dom = driver.findDomain(domain.getId());
		return DBRelation.newInstance(driver, dom);
	}

	@Override
	public CMRelationDefinition update(final CMRelation relation) {
		if (relation instanceof DBRelation) {
			final DBRelation dbRelation = (DBRelation) relation;
			return DBRelation.newInstance(driver, dbRelation);
		}
		throw new IllegalArgumentException();
	}

	@Override
	public void delete(final CMRelation relation) {
		final DBDomain dbDomain = findDomain(relation.getType().getId());
		final DBRelation dbRelation = DBRelation.newInstance(driver, dbDomain, relation.getId());

		driver.delete(dbRelation);
	}

	@Override
	public void clear(final CMEntryType type) {
		driver.clear(cmToDbEntryType(type));
	}

	private DBEntryType cmToDbEntryType(final CMEntryType entryType) {
		return new CMEntryTypeVisitor() {

			private DBEntryType output;

			public DBEntryType convert(final CMEntryType entryType) {
				entryType.accept(this);
				return output;
			}

			@Override
			public void visit(final CMFunctionCall type) {
				throw new IllegalArgumentException(format("unexpected type '%s'", entryType.getClass()));
			}

			@Override
			public void visit(final CMDomain type) {
				output = cmToDbDomain(type);
			}

			@Override
			public void visit(final CMClass type) {
				output = cmToDbClass(type);
			}

		}.convert(entryType);
	}

	private DBClass cmToDbClass(final CMEntryType entryType) {
		final DBClass dbClass;
		if (entryType == null) {
			dbClass = null;
		} else if (entryType instanceof DBClass) {
			dbClass = DBClass.class.cast(entryType);
		} else {
			dbClass = findClass(entryType.getIdentifier());
			assert dbClass != null;
		}
		return dbClass;
	}

	private DBDomain cmToDbDomain(final CMEntryType entryType) {
		final DBDomain dbDomain;
		if (entryType == null) {
			dbDomain = null;
		} else if (entryType instanceof DBDomain) {
			dbDomain = DBDomain.class.cast(entryType);
		} else {
			dbDomain = findDomain(entryType.getIdentifier());
			assert dbDomain != null;
		}
		return dbDomain;
	}

	// Some method to retrieve system classes
	@Override
	public CMClass getActivityClass() {
		return findClass(SystemClass.ACTIVITY);
	}

	@Override
	public CMClass getReportClass() {
		return findClass(SystemClass.REPORT);
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter) {
		return TRUE_ONLY_WHERE_CLAUSES;
	}

}
