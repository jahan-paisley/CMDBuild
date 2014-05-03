package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CONFIGURATION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.POSITION;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;
import static org.cmdbuild.servlets.json.ComunicationConstants.TEMPLATE;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.GetFiltersResponse;
import org.cmdbuild.servlets.json.serializers.FilterSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Filter extends JSONBaseWithSpringContext {

	/**
	 * Retrieves only users' filters (it does not fetches filters defined for
	 * groups)
	 * 
	 * @param start
	 *            is the offset (used for pagination)
	 * @param limit
	 *            is the max number of rows for each page (used for pagination)
	 * @return
	 * @throws JSONException
	 * @throws CMDBException
	 */
	@JSONExported
	public JSONObject read( //
			final @Parameter(value = CLASS_NAME) String className, //
			final @Parameter(value = START) int start, //
			final @Parameter(value = LIMIT) int limit //
	) throws JSONException, CMDBException {
		final GetFiltersResponse userFilters = filterStore().getAllUserFilters(className, start, limit);
		return FilterSerializer.toClient(userFilters);
	}

	/**
	 * Retrieves only groups filters
	 * 
	 * @param start
	 *            is the offset (used for pagination)
	 * @param limit
	 *            is the max number of rows for each page (used for pagination)
	 * @return
	 * @throws JSONException
	 * @throws CMDBException
	 */
	@JSONExported
	public JSONObject readAllGroupFilters( //
			@Parameter(value = START) final int start, //
			@Parameter(value = LIMIT) final int limit //
	) throws JSONException, CMDBException {
		final GetFiltersResponse response = filterStore().fetchAllGroupsFilters(start, limit);
		return FilterSerializer.toClient(response);
	}

	/**
	 * Retrieves, for the currently logged user, all filters (group and user
	 * filters) that are referred to the className
	 * 
	 * @param className
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject readForUser( //
			@Parameter(value = CLASS_NAME) final String className //
	) throws JSONException {
		final GetFiltersResponse userFilters = filterStore().getFiltersForCurrentlyLoggedUser(className);
		return FilterSerializer.toClient(userFilters);
	}

	@JSONExported
	public JSONObject create( //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = CONFIGURATION) final JSONObject configuration, //
			@Parameter(value = TEMPLATE, required = false) final boolean template //
	) throws JSONException, CMDBException {
		final FilterStore.Filter filter = filterStore().create(
				FilterSerializer.toServerForCreation(name, className, description, configuration, template));
		return FilterSerializer.toClient(filter, FILTER);
	}

	@JSONExported
	public void update( //
			@Parameter(value = ID) final Long id, //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = CONFIGURATION) final JSONObject configuration //
	) throws JSONException, CMDBException {
		filterStore().update(FilterSerializer.toServerForUpdate(id, name, className, description, configuration));
	}

	@JSONExported
	public void delete( //
			@Parameter(value = ID) final Long id //
	) throws JSONException, CMDBException {
		filterStore().delete(FilterDTO.newFilter().withId(id).build());
	}

	@JSONExported
	public JSONObject position( //
			@Parameter(value = ID) final Long id //
	) throws JSONException, CMDBException {
		final Long position = filterStore().getPosition(FilterDTO.newFilter().withId(id).build());
		final JSONObject out = new JSONObject();
		out.put(POSITION, position);
		return out;
	}

}
