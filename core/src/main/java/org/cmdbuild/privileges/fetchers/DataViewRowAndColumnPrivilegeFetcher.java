package org.cmdbuild.privileges.fetchers;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContext.PrivilegedObjectMetadata;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class DataViewRowAndColumnPrivilegeFetcher implements RowAndColumnPrivilegeFetcher {

	private static final Logger logger = Log.PERSISTENCE;

	private static final Iterable<? extends WhereClause> EMPTY_WHERE_CLAUSES = Collections.emptyList();

	private final CMDataView dataView;
	private final PrivilegeContext privilegeContext;
	private final UserStore userStore;

	public DataViewRowAndColumnPrivilegeFetcher( //
			final CMDataView dataView, //
			final PrivilegeContext privilegeContext, //
			final UserStore userStore
	) {
		this.dataView = dataView;
		this.privilegeContext = privilegeContext;
		this.userStore = userStore;
	}

	/**
	 * FIXME: consider also filter on relations... bug on privileges on rows
	 * when relations are specified
	 */
	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType) {
		return fetchPrivilegeFiltersFor(entryType, entryType);
	}

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType,
			final CMEntryType entryTypeForClauses) {
		if (privilegeContext.hasAdministratorPrivileges() && entryType.isActive()) {
			return EMPTY_WHERE_CLAUSES;
		}
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		if (metadata == null) {
			return EMPTY_WHERE_CLAUSES;
		}
		final List<String> privilegeFilters = metadata.getFilters();
		final List<WhereClause> whereClauseFilters = Lists.newArrayList();
		for (final String privilegeFilter : privilegeFilters) {
			try {
				final Iterable<WhereClause> whereClauses = createWhereClausesFrom(privilegeFilter, entryTypeForClauses);
				if (!isEmpty(whereClauses)) {
					whereClauseFilters.add(and(whereClauses));
				}
			} catch (final JSONException e) {
				logger.warn("error creating where clause", e);
			}
		}
		return whereClauseFilters;
	}

	private Iterable<WhereClause> createWhereClausesFrom(final String privilegeFilter, final CMEntryType entryType)
			throws JSONException {
		final JSONObject jsonPrivilegeFilter = new JSONObject(privilegeFilter);
		return JsonFilterMapper.newInstance() //
				.withDataView(dataView) //
				.withEntryType(entryType) //
				.withFilterObject(jsonPrivilegeFilter) //
				.withOperationUser(userStore.getUser()) //
				.build() //
				.whereClauses();
	}

	/**
	 * If superUser return write privilege for all the attributes
	 * 
	 * If not superUser, looking for some attributes privilege definition, if
	 * there is no one return the attributes mode defined globally
	 */
	@Override
	public Map<String, String> fetchAttributesPrivilegesFor(final CMEntryType entryType) {

		final Map<String, String> groupLevelAttributePrivileges = getAttributePrivilegesMap(entryType);

		// initialize a map with the
		// mode set for attribute globally
		final Map<String, String> mergedAttributesPrivileges = new HashMap<String, String>();
		final Iterable<? extends CMAttribute> attributes = entryType.getAllAttributes();
		for (final CMAttribute attribute : attributes) {
			if (attribute.isActive()) {
				final String mode = attribute.getMode().name().toLowerCase();
				mergedAttributesPrivileges.put(attribute.getName(), mode);
			}
		}

		/*
		 * The super user has no added limitation for the attributes, so return
		 * the global attributes modes
		 */
		if (privilegeContext.hasAdministratorPrivileges()) {
			return mergedAttributesPrivileges;
		}

		// merge with the privileges set at group level
		for (final String attributeName : groupLevelAttributePrivileges.keySet()) {
			if (mergedAttributesPrivileges.containsKey(attributeName)) {
				mergedAttributesPrivileges.put( //
						attributeName, //
						groupLevelAttributePrivileges.get(attributeName) //
						);
			}
		}

		return mergedAttributesPrivileges;
	}

	private Map<String, String> getAttributePrivilegesMap(final CMEntryType entryType) {
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		final Map<String, String> attributePrivileges = new HashMap<String, String>();
		if (metadata != null) {
			for (final String privilege : metadata.getAttributesPrivileges()) {
				final String[] parts = privilege.split(":");
				attributePrivileges.put(parts[0], parts[1]);
			}
		}

		return attributePrivileges;
	}
}
