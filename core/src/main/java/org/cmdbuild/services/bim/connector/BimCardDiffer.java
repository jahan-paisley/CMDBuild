package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.SPACEGEOMETRY;
import static org.cmdbuild.bim.utils.BimConstants.SPACEHEIGHT;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.utils.bim.BimIdentifier;

public class BimCardDiffer implements CardDiffer {

	private final CardDiffer defaultCardDiffer;
	private final CMDataView dataView;

	private static final String SET_POSITION_FUNCTION = "_bim_set_coordinates";
	private static final String SET_PERIMETER_AND_HEIGHT_FUNCTION = "_bim_set_room_geometry";

	private BimCardDiffer(final CMDataView dataView, final LookupLogic lookupLogic, final BimDataView bimDataView) {
		this.defaultCardDiffer = new OptimizedDefaultCardDiffer(dataView, lookupLogic, bimDataView);
		this.dataView = dataView;
	}

	public static BimCardDiffer buildBimCardDiffer(final CMDataView dataView, final LookupLogic lookupLogic,
			BimDataView bimDataView) {
		return new BimCardDiffer(dataView, lookupLogic, bimDataView);
	}

	@Override
	public CMCard updateCard(final Entity sourceEntity, final CMCard oldCard) {
		final CMCard updatedCard = defaultCardDiffer.updateCard(sourceEntity, oldCard);
		final CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(sourceEntity.getTypeName()));
		final CMQueryResult queryResult = dataView.select(anyAttribute(bimClass))//
				.from(bimClass)//
				.where(condition(attribute(bimClass, GLOBALID_ATTRIBUTE), eq(sourceEntity.getKey()))).run();
		final CMCard bimCard = queryResult.getOnlyRow().getCard(bimClass);
		if (bimCard != null) {
			final boolean updateCoordinates = sourceEntity.getAttributeByName(COORDINATES).isValid();
			final boolean updateSpaceGeometry = sourceEntity.getAttributeByName(SPACEGEOMETRY).isValid()
					&& sourceEntity.getAttributeByName(SPACEHEIGHT).isValid();
			if (updateCoordinates) {
				final String coordinates = sourceEntity.getAttributeByName(COORDINATES).getValue();
				final CMFunction function = dataView.findFunctionByName(SET_POSITION_FUNCTION);
				final NameAlias f = NameAlias.as("f");
				dataView.select(anyAttribute(function, f)).from(call(function, //
						sourceEntity.getKey(), //
						sourceEntity.getTypeName(), //
						coordinates), f).run();
			} else if (updateSpaceGeometry) {
				final String polygon = sourceEntity.getAttributeByName(SPACEGEOMETRY).getValue();
				final String height = sourceEntity.getAttributeByName(SPACEHEIGHT).getValue();
				final CMFunction function = dataView.findFunctionByName(SET_PERIMETER_AND_HEIGHT_FUNCTION);
				final NameAlias f = NameAlias.as("f");
				dataView.select(anyAttribute(function, f)).from(call(function, //
						sourceEntity.getKey(), //
						sourceEntity.getTypeName(), //
						polygon, height), f).run();
			}
		}
		return updatedCard;
	}

	@Override
	public CMCard createCard(final Entity sourceEntity) {
		final CMCard newCard = defaultCardDiffer.createCard(sourceEntity);
		if (newCard != null) {
			final CMCard bimCard = createBimCard(newCard, sourceEntity);
			if (bimCard != null) {
				final boolean storeCoordinates = sourceEntity.getAttributeByName(COORDINATES).isValid();
				final boolean storeSpaceGeometry = sourceEntity.getAttributeByName(SPACEGEOMETRY).isValid()
						&& sourceEntity.getAttributeByName(SPACEHEIGHT).isValid();
				if (storeCoordinates) {
					final String coordinates = sourceEntity.getAttributeByName(COORDINATES).getValue();
					final CMFunction function = dataView.findFunctionByName(SET_POSITION_FUNCTION);
					final NameAlias f = NameAlias.as("f");
					dataView.select(anyAttribute(function, f)).from(call(function, //
							sourceEntity.getKey(), //
							sourceEntity.getTypeName(), //
							coordinates), f).run();
				} else if (storeSpaceGeometry) {
					final String polygon = sourceEntity.getAttributeByName(SPACEGEOMETRY).getValue();
					final String height = sourceEntity.getAttributeByName(SPACEHEIGHT).getValue();
					final CMFunction function = dataView.findFunctionByName(SET_PERIMETER_AND_HEIGHT_FUNCTION);
					final NameAlias f = NameAlias.as("f");
					dataView.select(anyAttribute(function, f)).from(call(function, //
							sourceEntity.getKey(), //
							sourceEntity.getTypeName(), //
							polygon, //
							height), f).run();
				}
			}
		}
		return newCard;
	}

	private CMCard createBimCard(final CMCard newCard, final Entity sourceEntity) {
		final String cmdbClassName = sourceEntity.getTypeName();
		final Long id = newCard.getId();
		final CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(cmdbClassName));
		final CMCardDefinition bimCard = dataView.createCardFor(bimClass);
		bimCard.set(GLOBALID_ATTRIBUTE, sourceEntity.getKey());
		bimCard.set(FK_COLUMN_NAME, id.toString());
		return bimCard.save();
	}

}
