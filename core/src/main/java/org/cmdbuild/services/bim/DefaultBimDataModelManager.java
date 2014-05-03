package org.cmdbuild.services.bim;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.*;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.StorableProjectConverter;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.cmdbuild.model.data.EntryType.TableType;
import org.cmdbuild.services.bim.BimPersistence.PersistenceProject;
import org.cmdbuild.utils.bim.BimIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.google.common.collect.Lists;

public class DefaultBimDataModelManager implements BimDataModelManager {

	public static final String PROJECTID = "ProjectId";
	private final CMDataView dataView;
	private final DataDefinitionLogic dataDefinitionLogic;
	private final JdbcTemplate jdbcTemplate;
	private static final String CREATE_ATTRIBUTE_TEMPLATE = "SELECT cm_create_class_attribute('%s','%s','%s',%s,%s,%s,'%s')";
	private static final String CHECK_ATTRIBUTE_EXISTENCE = "SELECT cm_attribute_exists('%s','%s','%s')";

	public static final String FK_COLUMN_NAME = "Master";
	public static final String BIM_SCHEMA = "bim";
	public static final String DEFAULT_DOMAIN_SUFFIX = StorableProjectConverter.TABLE_NAME;

	// "Export" layers has a position
	private static final String BIM_TABLE_POSITION_ATTRIBUTE_COMMENT_TEMPLATE = "STATUS: active|BASEDSP: false|CLASSORDER: 0|DESCR: Position|GROUP: |INDEX: -1|MODE: write|FIELDMODE: write|NOTNULL: false|UNIQUE: false";

	// "Container" layers has a perimeter and a height
	private static final String BIM_TABLE_PERIMETER_ATTRIBUTE_COMMENT_TEMPLATE = "STATUS: active|BASEDSP: false|CLASSORDER: 0|DESCR: Perimeter|GROUP: |INDEX: -1|MODE: write|FIELDMODE: write|NOTNULL: false|UNIQUE: false";

	public DefaultBimDataModelManager(CMDataView dataView, DataDefinitionLogic dataDefinitionLogic,
			LookupLogic lookupLogic, DataSource dataSource) {
		this.dataView = dataView;
		this.dataDefinitionLogic = dataDefinitionLogic;

		// TODO remove this and use a store-procedure
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void createBimTableIfNeeded(String className) {
		CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		if (bimClass == null) {
			createBimTable(className);
		}
	}

	@Override
	public void addPositionFieldIfNeeded(String className) {

		final String checkAttributeExistenceQuery = String.format(CHECK_ATTRIBUTE_EXISTENCE, //
				BIM_SCHEMA, //
				className, //
				POSITION);

		ExistenceRowCallbackHandler callbackHandler = new ExistenceRowCallbackHandler();

		jdbcTemplate.query(checkAttributeExistenceQuery, callbackHandler);
		if (callbackHandler.doesExists()) {
			return;
		}

		final String positionAttributeQuery = String.format(CREATE_ATTRIBUTE_TEMPLATE, //
				"bim." + className, //
				POSITION, //
				"Geometry", //
				"null", //
				"false", //
				"false", //
				BIM_TABLE_POSITION_ATTRIBUTE_COMMENT_TEMPLATE //
				);

		jdbcTemplate.query(positionAttributeQuery, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
			}
		});
	}

	@Override
	public void addPerimeterAndHeightFieldsIfNeeded(String className) {

		final String checkAttributeExistenceQuery = String.format(CHECK_ATTRIBUTE_EXISTENCE, //
				BIM_SCHEMA, //
				className, //
				PERIMETER);

		ExistenceRowCallbackHandler callbackHandler = new ExistenceRowCallbackHandler();

		jdbcTemplate.query(checkAttributeExistenceQuery, callbackHandler);
		if (callbackHandler.doesExists()) {
			return;
		}

		final String perimeterAttributeQuery = String.format(CREATE_ATTRIBUTE_TEMPLATE, //
				"bim." + className, //
				PERIMETER, //
				"Geometry", //
				"null", //
				"false", //
				"false", //
				BIM_TABLE_PERIMETER_ATTRIBUTE_COMMENT_TEMPLATE //
				);

		jdbcTemplate.query(perimeterAttributeQuery, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
			}
		});

		AttributeBuilder attributeBuilder = Attribute.newAttribute() //
				.withName(HEIGHT) //
				.withType(Attribute.AttributeTypeBuilder.DOUBLE) //
				.thatIsUnique(false) //
				.thatIsMandatory(false) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA);

		Attribute attributeHeight = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeHeight);

	}

	@Override
	public void createBimDomainOnClass(String className) {
		CMClass theClass = dataView.findClass(className);
		CMClass projectClass = dataView.findClass(StorableProjectConverter.TABLE_NAME);
		DomainBuilder domainBuilder = Domain.newDomain() //
				.withName(className + DEFAULT_DOMAIN_SUFFIX) //
				.withIdClass1(theClass.getId()) //
				.withIdClass2(projectClass.getId()) //
				.withCardinality("N:1");

		Domain domain = domainBuilder.build();

		dataDefinitionLogic.create(domain);
	}

	@Override
	public void deleteBimDomainIfExists(String className) {
		dataDefinitionLogic.deleteDomainIfExists(className + DEFAULT_DOMAIN_SUFFIX);
	}

	private void createBimTable(String className) {
		ClassBuilder classBuilder = EntryType.newClass() //
				.withName(className) //
				.withNamespace(BIM_SCHEMA) //
				.withTableType(TableType.simpletable)//
				.thatIsSystem(true);
		dataDefinitionLogic.createOrUpdate(classBuilder.build());

		AttributeBuilder attributeBuilder = Attribute.newAttribute() //
				.withName(GLOBALID_ATTRIBUTE) //
				.withType(Attribute.AttributeTypeBuilder.STRING) //
				.withLength(22) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA);

		Attribute attributeGlobalId = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeGlobalId);

		attributeBuilder = Attribute.newAttribute() //
				.withName(FK_COLUMN_NAME) //
				.withType(Attribute.AttributeTypeBuilder.FOREIGNKEY) //
				.thatIsUnique(true) //
				.thatIsMandatory(true) //
				.withOwnerName(className) //
				.withOwnerNamespace(BIM_SCHEMA) //
				.withForeignKeyDestinationClassName(className);
		Attribute attributeMaster = attributeBuilder.build();
		dataDefinitionLogic.createOrUpdate(attributeMaster);
	}

	@Override
	public void bindProjectToCards(String projectCardId, String className, Iterable<String> cardsToBind) {
		CMClass projectsClass = dataView.findClass(StorableProjectConverter.TABLE_NAME);
		CMClass rootClass = dataView.findClass(className);

		CMDomain domain = dataView.findDomain(className + DEFAULT_DOMAIN_SUFFIX);

		removeOldRelations(domain, projectCardId);

		for (String cardId : cardsToBind) {
			CMRelationDefinition relationDefinition = dataView.createRelationFor(domain);

			CMCard projectCard = dataView.select(attribute(projectsClass, DESCRIPTION_ATTRIBUTE)) //
					.from(projectsClass)
					//
					.where(condition(attribute(projectsClass, ID_ATTRIBUTE), eq(Long.parseLong(projectCardId)))) //
					.run() //
					.getOnlyRow() //
					.getCard(projectsClass);

			CMCard rootCard = dataView.select(attribute(rootClass, DESCRIPTION_ATTRIBUTE)) //
					.from(rootClass)
					//
					.where(condition(attribute(rootClass, ID_ATTRIBUTE), eq(Long.parseLong(cardId)))) //
					.run() //
					.getOnlyRow() //
					.getCard(rootClass);

			relationDefinition.setCard1(rootCard);
			relationDefinition.setCard2(projectCard);
			relationDefinition.save();
		}
	}

	private void removeOldRelations(CMDomain domain, String projectId) {
		ArrayList<CMRelation> oldRelations = getAllRelationsForDomain(domain, projectId);
		for (CMRelation relation : oldRelations) {
			dataView.delete(relation);
		}
	}

	private ArrayList<CMRelation> getAllRelationsForDomain(CMDomain domain, String projectId) {
		ArrayList<CMRelation> oldRelations = Lists.newArrayList();

		CMClass projectClass = domain.getClass2();
		CMClass rootClass = domain.getClass1();

		Alias DOM_ALIAS = EntryTypeAlias.canonicalAlias(domain);
		Alias DST_ALIAS = EntryTypeAlias.canonicalAlias(projectClass);
		CMQueryResult result = dataView.select( //
				anyAttribute(DOM_ALIAS), attribute(DST_ALIAS, DESCRIPTION_ATTRIBUTE)) //
				.from(rootClass) //
				.join(anyClass(), as(DST_ALIAS), over(domain, as(DOM_ALIAS))) //
				.where(condition(attribute(DST_ALIAS, ID_ATTRIBUTE), eq(Long.parseLong(projectId))))//
				.run();

		for (java.util.Iterator<CMQueryRow> it = result.iterator(); it.hasNext();) {
			CMQueryRow row = it.next();
			QueryRelation queryRelation = row.getRelation(domain);
			CMRelation relation = queryRelation.getRelation();
			oldRelations.add(relation);
		}
		return oldRelations;
	}

	@Override
	public Iterable<String> readCardsBindedToProject(String projectId, String className) {
		ArrayList<CMRelation> relations = Lists.newArrayList();

		CMDomain domain = dataView.findDomain(className + DEFAULT_DOMAIN_SUFFIX);
		relations = getAllRelationsForDomain(domain, projectId);

		List<String> bindedCards = Lists.newArrayList();
		for (CMRelation relation : relations) {
			bindedCards.add(relation.getCard1Id().toString());
		}
		return bindedCards;
	}

	private class ExistenceRowCallbackHandler implements RowCallbackHandler {

		private boolean exists;

		public boolean doesExists() {
			return exists;
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			exists = rs.getBoolean("cm_attribute_exists");

		}
	};

	@Override
	@Deprecated
	public void updateCardsFromSource(List<Entity> source) throws Exception {
		throw new Exception("Do not use this, use directly to Mapper");
	}

	@Override
	public void saveCardBinding(PersistenceProject persistenceProject) {
		// TODO Auto-generated method stub

	}

}
