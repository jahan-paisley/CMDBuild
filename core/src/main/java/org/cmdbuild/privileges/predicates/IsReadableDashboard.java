package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Predicate;

public class IsReadableDashboard implements Predicate<CMCard> {

	private final CMGroup group;
	private final CMDataView view;
	private static final String DASHBOARD_TABLE_NAME = "_Dashboards";
	private static final String DEFINITION_ATTRIBUTE_NAME = "Definition";

	public IsReadableDashboard(final CMDataView view, final CMGroup group) {
		this.group = group;
		this.view = view;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Object idElementObject = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE);
		if (idElementObject == null) {
			return false;
		}
		try {
			final Integer dashboardDefinitionId = (Integer) idElementObject;
			final CMCard dashboardCard = fetchDashboardCard(dashboardDefinitionId);
			final JSONObject jsonDashboardDefinition = new JSONObject(
					(String) dashboardCard.get(DEFINITION_ATTRIBUTE_NAME));
			final JSONArray groups = jsonDashboardDefinition.getJSONArray("groups");
			for (int i = 0; i < groups.length(); i++) {
				final String groupName = groups.getString(i);
				if (groupName.equals(group.getName())) {
					return true;
				}
			}
		} catch (final Exception ex) {
			return false;
		}
		return false;
	}

	private CMCard fetchDashboardCard(final Integer cardId) {
		final CMClass dashboardClass = view.findClass(DASHBOARD_TABLE_NAME);
		final CMQueryRow row = view.select(anyAttribute(dashboardClass)) //
				.from(dashboardClass) //
				.where(condition(attribute(dashboardClass, "Id"), eq(cardId))) //
				.run() //
				.getOnlyRow();
		return row.getCard(dashboardClass);
	}

}
