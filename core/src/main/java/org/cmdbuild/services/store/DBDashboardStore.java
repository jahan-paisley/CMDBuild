package org.cmdbuild.services.store;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardObjectMapper;
import org.codehaus.jackson.map.ObjectMapper;

public class DBDashboardStore implements DashboardStore {

	public static final String DASHBOARD_TABLE = "_Dashboards";

	private static final String DEFINITION_ATTRIBUTE = "Definition";
	private static final ObjectMapper mapper = new DashboardObjectMapper();
	private static final ErrorMessageBuilder errors = new ErrorMessageBuilder();
	private final CMDataView view;

	public DBDashboardStore(final CMDataView view) {
		this.view = view;
	}

	@Override
	public Long create(final DashboardDefinition dashboard) {
		final String serializedDefinition = serializeDashboard(dashboard);
		final CMClass dashboardClass = view.findClass(DASHBOARD_TABLE);
		final CMCardDefinition cardDefinition = view.createCardFor(dashboardClass);
		final CMCard createdCard = cardDefinition.set(DEFINITION_ATTRIBUTE, serializedDefinition) //
				.save();
		return createdCard.getId();
	}

	@Override
	public DashboardDefinition read(final Long dashboardId) {
		final CMCard card = findDashboardCard(dashboardId);
		return cardToDashboardDefinition(card);
	}

	@Override
	public Map<Integer, DashboardDefinition> list() {
		final CMClass dashboardClass = view.findClass(DASHBOARD_TABLE);
		final Map<Integer, DashboardDefinition> out = new HashMap<Integer, DashboardDefinition>();
		final CMQueryResult result = view.select(anyAttribute(dashboardClass)) //
				.from(dashboardClass) //
				.run();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(dashboardClass);
			out.put(Integer.valueOf(card.getId().intValue()), cardToDashboardDefinition(card));
		}
		return out;
	}

	@Override
	public void update(final Long dashboardId, final DashboardDefinition dashboard) {
		final String serializedDefinition = serializeDashboard(dashboard);
		final CMCard card = findDashboardCard(dashboardId);
		final CMCardDefinition cardDefinition = view.update(card);
		cardDefinition.set(DEFINITION_ATTRIBUTE, serializedDefinition) //
				.save();
	}

	@Override
	public void delete(final Long dashboardId) {
		final CMCard card = findDashboardCard(dashboardId);
		view.delete(card);
	}

	private CMCard findDashboardCard(final Long dashboardId) {
		final CMClass dashboardClass = view.findClass(DASHBOARD_TABLE);
		final CMQueryRow row = view.select(anyAttribute(dashboardClass)) //
				.from(dashboardClass) //
				.where(condition(attribute(dashboardClass, "Id"), eq(dashboardId))) //
				.run().getOnlyRow();
		return row.getCard(dashboardClass);
	}

	private DashboardDefinition cardToDashboardDefinition(final CMCard card) {
		try {
			final String serializedDefinition = (String) card.get(DEFINITION_ATTRIBUTE);
			return mapper.readValue(serializedDefinition, DashboardDefinition.class);
		} catch (final Exception e) {
			throw new IllegalArgumentException(errors.decodingError());
		}
	}

	private String serializeDashboard(final DashboardDefinition dashboard) throws IllegalArgumentException {
		try {
			final String serializedDefinition = mapper.writeValueAsString(dashboard);
			return serializedDefinition;
		} catch (final Exception e) {
			throw new IllegalArgumentException(errors.encodingError());
		}
	}

	/*
	 * to avoid an useless errors hierarchy define this object that build the
	 * errors messages These are used also in the tests to ensure that a right
	 * message is provided by the exception
	 */
	public static class ErrorMessageBuilder {

		public String decodingError() {
			return "There ware some problems while trying to decode the dashboard";
		}

		public String encodingError() {
			return "There ware some problems while trying to encode the dashboard";
		}
	}
}
