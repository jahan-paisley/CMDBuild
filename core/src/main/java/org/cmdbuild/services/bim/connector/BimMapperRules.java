package org.cmdbuild.services.bim.connector;

import static org.cmdbuild.bim.utils.BimConstants.FK_COLUMN_NAME;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.utils.bim.BimIdentifier;

@Deprecated
// FIXME use BimDataView instead of this class.
public class BimMapperRules extends DefaultMapperRules {

	public static final BimMapperRules INSTANCE = new BimMapperRules();

	private BimMapperRules() {
	}

	public MapperRules getInstance() {
		return INSTANCE;
	}

	@Override
	public CMCard fetchCardWithKey(String key, String className, CMDataView dataView) {
		CMCard matchingCard = null;
		Long masterId = findIdFromKey(key, className, dataView);
		CMClass theClass = dataView.findClass(className);
		CMQueryResult result = dataView.select( //
				anyAttribute(theClass)) //
				.from(theClass)
				.where(condition(attribute(theClass, ID_ATTRIBUTE), eq(masterId))) //
				.run();
		if (!result.isEmpty()) {
			CMQueryRow row = result.getOnlyRow();
			matchingCard = row.getCard(theClass);
		}
		return matchingCard;
	}

	@Override
	public Long findIdFromKey(String value, String className, CMDataView dataView) {
		Long referencedId = null;
		CMClass theClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		Alias CLASS_ALIAS = EntryTypeAlias.canonicalAlias(theClass);
		CMQueryResult result = dataView.select( //
				anyAttribute(CLASS_ALIAS)) //
				.from(theClass)
				//
				.where(condition(attribute(CLASS_ALIAS, GLOBALID_ATTRIBUTE), eq(value))) //
				.run();
		if (!result.isEmpty()) {
			CMCard card = result.getOnlyRow().getCard(CLASS_ALIAS);
			if (card.get(FK_COLUMN_NAME) != null) {
				IdAndDescription reference = (IdAndDescription) card.get(FK_COLUMN_NAME);
				referencedId = reference.getId();
			}

		}
		return referencedId;
	}

	public String fetchKeyFromId(Long id, String className, CMDataView dataView) {
		CMClass bimClass = dataView.findClass(BimIdentifier.newIdentifier().withName(className));
		CMQueryResult result = dataView.select( //
				attribute(bimClass, GLOBALID_ATTRIBUTE)) //
				.from(bimClass) //
				.where(condition(attribute(bimClass, FK_COLUMN_NAME), eq(id))) //
				.run();
		CMCard card = result.getOnlyRow().getCard(bimClass);
		return card.get(GLOBALID_ATTRIBUTE).toString();
	}

	public long convertKeyToId(String key, String className, CMDataView dataView) {
		long id = -1;
		CMCard card = fetchCardWithKey(key, className, dataView);
		if (card != null) {
			id = card.getId();
		}
		return id;
	}

}
