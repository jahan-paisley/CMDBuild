package org.cmdbuild.services.bim;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.data.converter.StorableProjectConverter.PROJECT_ID;
import static org.cmdbuild.data.converter.StorableProjectConverter.TABLE_NAME;
import static org.cmdbuild.services.bim.DefaultBimDataModelManager.DEFAULT_DOMAIN_SUFFIX;

import java.util.Iterator;
import java.util.List;

import org.cmdbuild.bim.mapper.DefaultAttribute;
import org.cmdbuild.bim.mapper.DefaultEntity;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;
import org.cmdbuild.utils.bim.BimIdentifier;

import com.google.common.collect.Lists;

public class DefaultBimDataView extends ForwardingDataView implements BimDataView {

	private static final String CONTAINER_ID = "container_id";
	public static final String CONTAINER_GUID = "container_globalid";
	public static final String SHAPE_OID = "shape_oid";

	public static final String X_COORD = "x";
	public static final String Y_COORD = "y";
	public static final String Z_COORD = "z";

	private static final String CARDDATA_FROM_GUID_FUNCTION = "_bim_carddata_from_globalid";
	private static final String CREATE_FUNCTION_CARDDATA_FOR_EXPORT_FUNCTION = "_bim_create_function_for_export";	
	private static final String CARDDATA_FOR_EXPORT_FUNCTION = "_bim_data_for_export";
	private static final String _BIM_UPDATE_COORDINATES = "_bim_update_coordinates";
	private static final String GENERATE_COORDINATES_FUNCTION = "_bim_generate_coordinates";
	private static final String STORE_BIMDATA_FUNCTION = "_bim_store_data";

	private static final String CLASSNAME = "ClassName";

	private final CMDataView dataView;

	public DefaultBimDataView(final CMDataView dataView) {
		super(dataView);
		this.dataView = dataView;
	}

	@Override
	public CMCard getCmCardFromGlobalId(final String globalId, final String className) {
		CMCard matchingCard = null;
		final BimCard bimCard = getBimDataFromGlobalid(globalId);
		if (bimCard != null) {
			bimCard.getId();
			final Long masterId = bimCard.getId();
			final CMClass theClass = dataView.findClass(className);
			final CMQueryResult result = dataView.select( //
					anyAttribute(theClass)) //
					.from(theClass).where(condition(attribute(theClass, ID_ATTRIBUTE), eq(masterId))) //
					.run();
			if (!result.isEmpty()) {
				final CMQueryRow row = result.getOnlyRow();
				matchingCard = row.getCard(theClass);
			}
		}
		return matchingCard;
	}

	@Override
	public List<CMCard> getCardsWithAttributeAndValue(final CMIdentifier classIdentifier, final Object attributeValue,
			final String attributeName) {
		final CMClass theClass = dataView.findClass(classIdentifier);
		CMQueryResult result = null;
		if (isBlank(attributeName)) {
			result = dataView.select(anyAttribute(theClass)) //
					.from(theClass) //
					.run();
		} else {
			result = dataView.select(anyAttribute(theClass)) //
					.from(theClass) //
					.where(condition(attribute(theClass, attributeName), eq(attributeValue))) //
					.run();
		}
		final List<CMCard> cards = Lists.newArrayList();
		for (final Iterator<CMQueryRow> it = result.iterator(); it.hasNext();) {
			final CMQueryRow row = it.next();
			cards.add(row.getCard(theClass));
		}
		return cards;
	}

	@Override
	public void moveObject(final String className, final String globalId, final List<Double> coordinates) {
		final String xcord = String.valueOf(coordinates.get(0));
		final String ycord = String.valueOf(coordinates.get(1));
		final String zcord = String.valueOf(coordinates.get(2));

		final CMFunction function = dataView.findFunctionByName(_BIM_UPDATE_COORDINATES);
		final NameAlias f = NameAlias.as("f");
		dataView.select(anyAttribute(function, f)).from(call(function, className, globalId, xcord, ycord, zcord), f)
				.run();
	}

	@Override
	public Entity getCardDataForExport(final Long id, final String className, final String containerAttributeName,
			final String containerClassName, final String shapeOid, final String ifcType) {
		Entity cardToExport = Entity.NULL_ENTITY;
		final CMFunction createFunction = dataView.findFunctionByName(CREATE_FUNCTION_CARDDATA_FOR_EXPORT_FUNCTION);
		final NameAlias cf = NameAlias.as("cf");
		dataView.select(anyAttribute(createFunction, cf)).from(call(//
				createFunction), cf) //
				.run();

		CMFunction function = dataView.findFunctionByName(CARDDATA_FOR_EXPORT_FUNCTION);
		NameAlias f = NameAlias.as("f");
		CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(//
				function, //
				id, //
				className, //
				containerAttributeName, //
				containerClassName), f) //
				.run();

		if (queryResult.isEmpty()) {
			System.out.println("No bim data found for card " + id);
		}
		final CMQueryRow row = queryResult.getOnlyRow();
		if (!isValid(row, f)) {
			return cardToExport;
		}
		final String code = String.class.cast(row.getValueSet(f).get(CODE_ATTRIBUTE));
		final String description = String.class.cast(row.getValueSet(f).get(DESCRIPTION_ATTRIBUTE));
		String globalId = String.class.cast(row.getValueSet(f).get(GLOBALID_ATTRIBUTE));
		final Integer containerIdAsInt = Integer.class.cast(row.getValueSet(f).get(CONTAINER_ID));
		final Long containerId = new Long(containerIdAsInt.longValue());
		final String containerGlobalId = String.class.cast(row.getValueSet(f).get(CONTAINER_GUID));
		String xCoord = String.class.cast(row.getValueSet(f).get(X_ATTRIBUTE));
		String yCoord = String.class.cast(row.getValueSet(f).get(Y_ATTRIBUTE));
		String zCoord = String.class.cast(row.getValueSet(f).get(Z_ATTRIBUTE));

		final double x = xCoord != null && !xCoord.isEmpty() ? Double.parseDouble(xCoord) : 0;
		final double y = yCoord != null && !yCoord.isEmpty() ? Double.parseDouble(yCoord) : 0;
		final double z = zCoord != null && !zCoord.isEmpty() ? Double.parseDouble(zCoord) : 0;

		if (globalId == null || globalId.isEmpty()) {
			globalId = randomAlphanumeric(22);
		}
		if (x == 0 && y == 0 && z == 0) {
			function = dataView.findFunctionByName(GENERATE_COORDINATES_FUNCTION);
			f = NameAlias.as("f");
			queryResult = dataView.select(anyAttribute(function, f))
					.from(call(function, containerId, containerClassName), f).run();
			if (queryResult.isEmpty()) {
				System.out.println("No coordinates generated for card " + id);
			}
			final CMQueryRow rowCoordinates = queryResult.getOnlyRow();

			xCoord = String.class.cast(rowCoordinates.getValueSet(f).get(X_ATTRIBUTE));
			yCoord = String.class.cast(rowCoordinates.getValueSet(f).get(Y_ATTRIBUTE));
			zCoord = String.class.cast(rowCoordinates.getValueSet(f).get(Z_ATTRIBUTE));

			function = dataView.findFunctionByName(STORE_BIMDATA_FUNCTION);
			f = NameAlias.as("f");
			queryResult = dataView.select(anyAttribute(function, f))
					.from(call(function, id, className, globalId, xCoord, yCoord, zCoord), f).run();
		}

		final DefaultEntity cardWithBimData = DefaultEntity.withTypeAndKey(EMPTY, globalId);
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(ID_ATTRIBUTE, id.toString()));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(BASE_CLASS_NAME, className));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(CODE_ATTRIBUTE, code));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(DESCRIPTION_ATTRIBUTE, description));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(GLOBALID_ATTRIBUTE, globalId));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(X_ATTRIBUTE, xCoord));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(Y_ATTRIBUTE, yCoord));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(Z_ATTRIBUTE, zCoord));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(CONTAINER_ID, String.valueOf(containerId)));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(CONTAINER_GUID, containerGlobalId));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(SHAPE_OID, shapeOid));
		cardWithBimData.addAttribute(DefaultAttribute.withNameAndValue(IFC_TYPE, ifcType));
		cardToExport = cardWithBimData;

		return cardToExport;

	}

	private boolean isValid(final CMQueryRow row, final NameAlias f) {
		final Object containerReference = row.getValueSet(f).get(CONTAINER_ID);
		if (containerReference == null) {
			return false;
		}
		final String containerGlobalId = String.class.cast(row.getValueSet(f).get(CONTAINER_GUID));
		if (isBlank(containerGlobalId)) {
			return false;
		}
		return true;
	}

	@Override
	public BimCard getBimDataFromGlobalid(final String globalId) {
		final CMFunction function = dataView.findFunctionByName(CARDDATA_FROM_GUID_FUNCTION);
		final NameAlias f = NameAlias.as("f");
		final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)).from(call(function, globalId), f)
				.run();
		if (queryResult.isEmpty()) {
			System.out.println("No matching card found for globalid " + globalId);
		}

		final BimCard bimCard = new BimCard();
		final CMQueryRow row = queryResult.getOnlyRow();
		final Integer rowIdInt = (Integer) row.getValueSet(f).get(ID_ATTRIBUTE);
		final Integer rowIdClassInt = (Integer) row.getValueSet(f).get(CLASS_ID_ATTRIBUTE);
		final String className = (String) row.getValueSet(f).get(CLASSNAME);
		final String description = String.class.cast(row.getValueSet(f).get(DESCRIPTION_ATTRIBUTE));
		if (rowIdInt != null && rowIdClassInt != null) {
			final Long rowId = new Long(rowIdInt.longValue());
			final Long rowIdClass = new Long(rowIdClassInt.longValue());
			bimCard.setGlobalId(globalId);
			bimCard.setId(rowId);
			bimCard.setClassId(rowIdClass);
			bimCard.setCardDescription(description);
			bimCard.setClassName(className);
		}
		return bimCard;
	}

	public static class BimCard {
		private Long id;
		private Long classId;
		private String className;
		private String cardDescription;
		private String globalId;

		public BimCard() {
		}

		public BimCard( //
				final Long id, //
				final Long classId, //
				final String cardDescription, //
				final String className //
		) {
			this.id = id;
			this.classId = classId;
			this.cardDescription = cardDescription;
			this.className = className;
		}

		public boolean isValid() {
			return id != null && classId != null;
		}

		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		public Long getClassId() {
			return classId;
		}

		public void setClassId(final Long classId) {
			this.classId = classId;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(final String className) {
			this.className = className;
		}

		public String getCardDescription() {
			return cardDescription;
		}

		public void setCardDescription(final String cardDescription) {
			this.cardDescription = cardDescription;
		}

		public String getGlobalId() {
			return globalId;
		}

		public void setGlobalId(final String globalId) {
			this.globalId = globalId;
		}
	}

	@Override
	public Long getRootId(final Long cardId, final String className, final String referenceRootName) {
		final CMClass theClass = dataView.findClass(className);
		final CMQueryResult result = dataView.select( //
				attribute(theClass, referenceRootName)) //
				.from(theClass).where(condition(attribute(theClass, ID_ATTRIBUTE), eq(cardId))) //
				.run();
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(theClass);
		final IdAndDescription reference = IdAndDescription.class.cast(card.get(referenceRootName));
		return reference.getId();
	}

	@Override
	public Long getIdFromGlobalId(final String globalId, final String className) {
		Long id = null;
		final BimCard cardData = getBimDataFromGlobalid(globalId);
		if (cardData != null) {
			id = cardData.getId();
		}
		return id;
	}

	@Override
	public List<BimCard> getBimCardsWithGivenValueOfRootReferenceAttribute(final String className, final Long rootId,
			final String rootReferenceName) {
		final List<BimCard> result = Lists.newArrayList();
		final CMClass theClass = dataView.findClass(className);
		final Long idClass = theClass.getId();
		final List<CMCard> baseCards = getCardsWithAttributeAndValue(DBIdentifier.fromName(className), rootId,
				rootReferenceName);
		for (final CMCard card : baseCards) {
			final List<CMCard> bimCards = getCardsWithAttributeAndValue(
					BimIdentifier.newIdentifier().withName(className), card.getId(), FK_COLUMN_NAME);
			final Long cmid = card.getId();
			final String description = card.get(DESCRIPTION_ATTRIBUTE).toString();
			if (!bimCards.isEmpty()) {
				final CMCard bimCard = bimCards.get(0);
				final String globalid = bimCard.get(GLOBALID_ATTRIBUTE).toString();
				final BimCard resultCard = new BimCard(cmid, idClass, description, className);
				resultCard.setGlobalId(globalid);
				result.add(resultCard);
			}
		}
		return result;
	}

	@Override
	public BimCard getBimCardFromRootId(final String className, final Long rootCardId) {
		BimCard result = null;
		final CMClass theClass = dataView.findClass(className);
		final Long idClass = theClass.getId();

		final List<CMCard> baseCards = getCardsWithAttributeAndValue(DBIdentifier.fromName(className), rootCardId,
				ID_ATTRIBUTE);
		if (baseCards.size() > 1) {
			throw new BimError("More than one row with id " + rootCardId);
		}
		for (final CMCard card : baseCards) {
			final List<CMCard> bimCards = getCardsWithAttributeAndValue(
					BimIdentifier.newIdentifier().withName(className), card.getId(), FK_COLUMN_NAME);
			final Long cmid = card.getId();
			final String description = card.get(DESCRIPTION_ATTRIBUTE).toString();
			if (!bimCards.isEmpty()) {
				final CMCard bimCard = bimCards.get(0);
				final String globalid = bimCard.get(GLOBALID_ATTRIBUTE).toString();
				final BimCard resultCard = new BimCard(cmid, idClass, description, className);
				resultCard.setGlobalId(globalid);
				result = resultCard;
			}
		}
		return result;
	}

	@Override
	public CMCard fetchCard(final String className, final Long id) {
		final CMClass theClass = dataView.findClass(className);
		final CMQueryResult result = dataView.select(anyAttribute(theClass)).from(theClass)
				.where(condition(attribute(theClass, ID_ATTRIBUTE), eq(id))) //
				.run();
		final CMQueryRow row = result.getOnlyRow();
		final CMCard card = row.getCard(theClass);
		return card;
	}

	@Override
	public Long getProjectCardIdFromRootCard(final Long rootId, final String rootClassName) {
		Long projectId = (long) -1;

		final Alias DOM_ALIAS = NameAlias.as("DOM");
		final Alias DST_ALIAS = NameAlias.as("DST");
		final CMClass rootClass = dataView.findClass(DBIdentifier.fromName(rootClassName));

		final String domainName = rootClassName + DEFAULT_DOMAIN_SUFFIX;
		final CMDomain domain = dataView.findDomain(domainName);

		final WhereClause clause = condition(attribute(rootClass, ID_ATTRIBUTE), eq(rootId));
		final Over overClause = over(domain, as(DOM_ALIAS));

		final CMQueryResult result = dataView.select(anyAttribute(DOM_ALIAS)) //
				.from(rootClass) //
				.join(anyClass(), as(DST_ALIAS), overClause) //
				.where(clause) //
				.run();

		for (final CMQueryRow row : result) {
			final QueryRelation relation = row.getRelation(DOM_ALIAS);
			if (relation != null) {
				final CMRelation relation2 = relation.getRelation();
				projectId = Long.class.cast(relation2.getCard2Id());
			}
		}
		return projectId;
	}

	@Override
	public Long getRootCardIdFromProjectId(final String projectId, final String rootClassName) {
		Long rootId = (long) -1;

		final Alias DOM_ALIAS = NameAlias.as("DOM");
		final Alias DST_ALIAS = NameAlias.as("DST");
		final CMClass projectClass = dataView.findClass(DBIdentifier.fromName(TABLE_NAME));

		final String domainName = rootClassName + DEFAULT_DOMAIN_SUFFIX;
		final CMDomain domain = dataView.findDomain(domainName);

		final WhereClause clause = condition(attribute(projectClass, PROJECT_ID), eq(projectId));
		final Over overClause = over(domain, as(DOM_ALIAS));

		final CMQueryResult result = dataView.select(anyAttribute(DOM_ALIAS)) //
				.from(projectClass) //
				.join(anyClass(), as(DST_ALIAS), overClause) //
				.where(clause) //
				.run();

		for (final CMQueryRow row : result) {
			final QueryRelation relation = row.getRelation(DOM_ALIAS);
			if (relation != null) {
				final CMRelation relation2 = relation.getRelation();
				rootId = Long.class.cast(relation2.getCard2Id());
			}
		}
		return rootId;
	}

}
