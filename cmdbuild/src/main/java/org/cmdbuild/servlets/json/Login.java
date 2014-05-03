package org.cmdbuild.servlets.json;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Collection;
import java.util.Collections;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends JSONBaseWithSpringContext {

	private AuthenticationLogic authLogic;

	@JSONExported
	@Unauthorized
	public JSONObject login( //
			final JSONObject serializer, //
			@Parameter(value = "username", required = false) final String loginString, //
			@Parameter(value = "password", required = false) final String password, //
			@Parameter(value = "role", required = false) final String groupName //
	) throws JSONException {
		authLogic = authLogic();
		final Response response = authLogic.login(LoginDTO.newInstance() //
				.withLoginString(loginString)//
				.withPassword(password)//
				.withGroupName(groupName)//
				.withUserStore(userStore()) //
				.build());
		return serializeResponse(response, serializer);
	}

	private JSONObject serializeResponse(final Response response, final JSONObject serializer) {
		try {
			serializer.put("success", response.isSuccess());
			if (response.getReason() != null) {
				serializer.put("reason", response.getReason());
			}
			if (response.getGroupsInfo() != null) {
				serializer.put("groups", serializeForLogin(response.getGroupsInfo()));
			}
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing login response", e);
		}
		return serializer;
	}

	private static JSONArray serializeForLogin(final Collection<GroupInfo> groups) throws JSONException {
		final JSONArray jsonGroups = new JSONArray();
		for (final GroupInfo group : groups) {
			final JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("name", group.getName());
			jsonGroup.put("description", group.getDescription());
			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

	/*
	 * DON'T REMOVE, used within index.jsp
	 */
	public static JSONArray serializeGroupForLogin(final Iterable<String> groupNames) throws JSONException {
		final JSONArray jsonGroups = new JSONArray();
		final CMDataView dataView = applicationContext().getBean(DBDataView.class);
		final Iterable<PrivilegeFetcherFactory> factories = Collections.emptyList();
		final GroupFetcher groupFetcher = new DBGroupFetcher(dataView, factories);
		for (final String groupName : groupNames) {
			final CMGroup group = groupFetcher.fetchGroupWithName(groupName);
			final JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("name", groupName);
			jsonGroup.put("description", group.getDescription());
			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

}
