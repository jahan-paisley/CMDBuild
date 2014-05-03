package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.ALREADY_ASSOCIATED;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.CONFIRMATION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DEFAULT_GROUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DISABLE;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUP_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.IS_ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.NEW_PASSWORD;
import static org.cmdbuild.servlets.json.ComunicationConstants.OLD_PASSWORD;
import static org.cmdbuild.servlets.json.ComunicationConstants.PASSWORD;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRIVILEGE_MODE;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRIVILEGE_OBJ_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRIVILEGE_READ;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRIVILEGE_WRITE;
import static org.cmdbuild.servlets.json.ComunicationConstants.RESULT;
import static org.cmdbuild.servlets.json.ComunicationConstants.ROWS;
import static org.cmdbuild.servlets.json.ComunicationConstants.STARTING_CLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.UI_CONFIGURATION;
import static org.cmdbuild.servlets.json.ComunicationConstants.USERS;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_NAME;

import java.io.IOException;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOBuilder;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.logic.auth.UserDTO.UserDTOBuilder;
import org.cmdbuild.logic.privileges.PrivilegeInfo;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.model.profile.UIConfigurationObjectMapper;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.PrivilegeSerializer;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class ModSecurity extends JSONBaseWithSpringContext {

	private static final ObjectMapper mapper = new UIConfigurationObjectMapper();

	/*
	 * Group management
	 */

	@JSONExported
	public JSONObject getGroupList() throws JSONException, AuthException, ORMException {
		final Iterable<CMGroup> allGroups = authLogic().getAllGroups();
		final JSONObject out = new JSONObject();
		final JSONArray groups = new JSONArray();

		for (final CMGroup group : allGroups) {
			final JSONObject jsonGroup = Serializer.serialize(group);
			groups.put(jsonGroup);
		}

		out.put(GROUPS, groups);
		return out;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveGroup( //
			@Parameter(ID) final Long groupId, //
			@Parameter(value = NAME, required = false) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(EMAIL) final String email, //
			@Parameter(STARTING_CLASS) final Long startingClass, //
			@Parameter(IS_ACTIVE) final boolean isActive, //
			@Parameter(value = TYPE, required = false) final String groupType, //
			@Parameter(value = USERS, required = false) final String users //
	) throws JSONException, AuthException {
		final boolean newGroup = groupId <= -1;
		CMGroup createdOrUpdatedGroup = null;
		final GroupDTOBuilder builder = GroupDTO.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withEmail(email) //
				.withStartingClassId(startingClass) //
				.withActiveStatus(isActive);

		if (CMGroup.GroupType.admin.name().equals(groupType)) {
			builder.withAdminFlag(true); //
		} else if (CMGroup.GroupType.restrictedAdmin.name().equals(groupType)) {
			builder.withAdminFlag(true);
			builder.withRestrictedAdminFlag(true);
		}

		final AuthenticationLogic authLogic = authLogic();
		if (newGroup) {
			final GroupDTO groupDTO = builder.build();
			createdOrUpdatedGroup = authLogic.createGroup(groupDTO);
		} else {
			final GroupDTO groupDTO = builder.withGroupId(groupId).build();
			createdOrUpdatedGroup = authLogic.updateGroup(groupDTO);
		}

		final JSONObject out = new JSONObject();
		out.put(GROUP, Serializer.serialize(createdOrUpdatedGroup));
		return out;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup( //
			@Parameter(IS_ACTIVE) final boolean active, //
			@Parameter(GROUP_ID) final Long groupId) throws JSONException, AuthException {

		final CMGroup group = authLogic().setGroupActive(groupId, active);

		final JSONObject out = new JSONObject();
		out.put(GROUP, Serializer.serialize(group));
		return out;
	}

	@Admin
	@JSONExported
	public JSONObject getGroupUserList( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(ALREADY_ASSOCIATED) final boolean associated) throws JSONException {

		final JSONObject out = new JSONObject();
		final AuthenticationLogic authLogic = authLogic();
		final List<CMUser> associatedUsers = authLogic.getUsersForGroupWithId(groupId);

		if (!associated) {
			final List<CMUser> allUsers = authLogic.getAllUsers();
			final List<CMUser> notAssociatedUsers = Lists.newArrayList();
			for (final CMUser user : allUsers) {
				if (associatedUsers.contains(user)) {
					continue;
				}
				notAssociatedUsers.add(user);
			}
			out.put(USERS, Serializer.serializeUsers(notAssociatedUsers));
		} else {
			out.put(USERS, Serializer.serializeUsers(associatedUsers));
		}

		return out;
	}

	/**
	 * 
	 * @param users
	 *            a String of comma separated user identifiers. These are the id
	 *            of the users that belong to the group with id = groupId
	 * @param groupId
	 */
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList( //
			@Parameter(value = USERS, required = false) final String users, //
			@Parameter(GROUP_ID) final Long groupId) { //

		final List<Long> newUserIds = Lists.newArrayList();
		if (!users.isEmpty()) {
			final String[] splittedUserIds = users.split(",");
			for (final String userId : splittedUserIds) {
				newUserIds.add(Long.valueOf(userId));
			}
		}
		final AuthenticationLogic authLogic = authLogic();
		final List<Long> oldUserIds = authLogic.getUserIdsForGroupWithId(groupId);
		for (final Long userId : newUserIds) {
			if (!oldUserIds.contains(userId)) {
				authLogic.addUserToGroup(userId, groupId);
			}
		}
		for (final Long userId : oldUserIds) {
			if (!newUserIds.contains(userId)) {
				authLogic.removeUserFromGroup(userId, groupId);
			}
		}
	}

	/*
	 * UI configuration
	 */

	@JSONExported
	public JsonResponse getUIConfiguration() throws JSONException, AuthException, ORMException {
		final Long groupId = operationUser().getPreferredGroup().getId();
		final SecurityLogic securityLogic = securityLogic();
		final UIConfiguration uiConfiguration = securityLogic.fetchGroupUIConfiguration(groupId);
		return JsonResponse.success(uiConfiguration);
	}

	@Admin
	@JSONExported
	public JsonResponse getGroupUIConfiguration(@Parameter(ID) final Long groupId) throws JSONException, AuthException,
			ORMException {
		final SecurityLogic securityLogic = securityLogic();
		final UIConfiguration uiConfiguration = securityLogic.fetchGroupUIConfiguration(groupId);
		return JsonResponse.success(uiConfiguration);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUIConfiguration( //
			@Parameter(ID) final Long groupId, //
			@Parameter(UI_CONFIGURATION) final String jsonUIConfiguration //
	) throws JSONException, AuthException, JsonParseException, JsonMappingException, IOException {

		final SecurityLogic securityLogic = securityLogic();
		final UIConfiguration uiConfiguration = mapper.readValue(jsonUIConfiguration, UIConfiguration.class);
		securityLogic.saveGroupUIConfiguration(groupId, uiConfiguration);
	}

	/*
	 * Privileges
	 */

	@JSONExported
	public JSONObject getClassPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> classPrivilegesForGroup = securityLogic().fetchClassPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(classPrivilegesForGroup);
	}

	@JSONExported
	public JSONObject getViewPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> viewPrivilegesForGroup = securityLogic().fetchViewPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(viewPrivilegesForGroup);
	}

	@JSONExported
	public JSONObject getFilterPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> filterPrivilegesForGroup = securityLogic().fetchFilterPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(filterPrivilegesForGroup);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveClassPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode) throws JSONException, AuthException { //
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode);
		securityLogic().saveClassPrivilege(privilegeInfoToSave, true);
	}

	private SerializablePrivilege serializablePrivilege(final Long privilegedObjectId) {
		return new SerializablePrivilege() {

			@Override
			public Long getId() {
				return privilegedObjectId;
			}

			@Override
			public String getPrivilegeId() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getName() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getDescription() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveViewPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode) throws JSONException, AuthException {
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode);
		securityLogic().saveViewPrivilege(privilegeInfoToSave);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveFilterPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode) throws JSONException, AuthException {
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode);
		securityLogic().saveFilterPrivilege(privilegeInfoToSave);
	}

	private PrivilegeMode extractPrivilegeMode(final String privilegeMode) {
		PrivilegeMode mode = null;
		if (privilegeMode.equals(PRIVILEGE_WRITE)) {
			mode = PrivilegeMode.WRITE;
		} else if (privilegeMode.equals(PRIVILEGE_READ)) {
			mode = PrivilegeMode.READ;
		} else {
			mode = PrivilegeMode.NONE;
		}
		return mode;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void setRowAndColumnPrivileges( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = ATTRIBUTES, required = false) final JSONArray jsonAttributes //
		) throws JSONException, AuthException {

		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo( //
				groupId, //
				serializablePrivilege(privilegedObjectId), //
				null //
			);

		// from jsonArray to string array
		final int l = jsonAttributes == null ? 0 : jsonAttributes.length();
		final String[] attributes = l > 0 ? new String[l] : null;
		for (int i = 0; i < l; ++i) {
			attributes[i] = jsonAttributes.getString(i);
		}

		privilegeInfoToSave.setAttributesPrivileges(attributes);
		privilegeInfoToSave.setPrivilegeFilter(filter);

		securityLogic().saveClassPrivilege(privilegeInfoToSave, false);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void clearRowAndColumnPrivileges( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId) throws JSONException, AuthException {

	}

	/*
	 * User management
	 */

	@JSONExported
	public JSONObject getUserList() throws JSONException, AuthException {
		final List<CMUser> usersList = authLogic().getAllUsers();
		final JSONObject out = new JSONObject();
		out.put(ROWS, Serializer.serializeUsers(usersList));

		return out;
	}

	@JSONExported
	public void changePassword(@Parameter(NEW_PASSWORD) final String newPassword,
			@Parameter(OLD_PASSWORD) final String oldPassword) {
		final OperationUser currentLoggedUser = operationUser();
		final boolean passwordChanged = currentLoggedUser.getAuthenticatedUser().changePassword(oldPassword,
				newPassword);
		if (!passwordChanged) {
			throw AuthException.AuthExceptionType.AUTH_WRONG_PASSWORD.createException();
		}
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveUser( //
			@Parameter(USER_ID) final Long userId, //
			@Parameter(value = DESCRIPTION, required = false) final String description, //
			@Parameter(value = USER_NAME, required = false) final String username, //
			@Parameter(value = PASSWORD, required = false) final String password, //
			@Parameter(value = CONFIRMATION, required = false) final String confirmation, //
			@Parameter(value = EMAIL, required = false) final String email, //
			@Parameter(IS_ACTIVE) final boolean isActive, //
			@Parameter(DEFAULT_GROUP) final Long defaultGroupId //
	) throws JSONException, AuthException {
		// TODO: check if password and confirmation match
		final boolean newUser = userId <= -1;
		CMUser createdOrUpdatedUser = null;
		final UserDTOBuilder userDTOBuilder = UserDTO.newInstance() //
				.withDescription(description) //
				.withUsername(username) //
				.withPassword(password) //
				.withEmail(email) //
				.withDefaultGroupId(defaultGroupId) //
				.withActiveStatus(isActive);
		final AuthenticationLogic authLogic = authLogic();
		if (newUser) {
			final UserDTO userDTO = userDTOBuilder.build();
			createdOrUpdatedUser = authLogic.createUser(userDTO);
		} else {
			final UserDTO userDTO = userDTOBuilder.withUserId(userId).build();
			createdOrUpdatedUser = authLogic.updateUser(userDTO);
		}

		final JSONObject out = new JSONObject();
		out.put(ROWS, Serializer.serialize(createdOrUpdatedUser));
		return out;
	}

	/**
	 * 
	 * @param serializer
	 * @param userId
	 * @return the groups to which the current user belongs
	 * @throws JSONException
	 */

	@JSONExported
	public JSONObject getUserGroupList( //
			@Parameter(value = USER_ID) final Long userId) //
			throws JSONException {
		final AuthenticationLogic authLogic = authLogic();
		final CMUser user = authLogic.getUserWithId(userId);
		final List<GroupInfo> groupsForLogin = Lists.newArrayList();
		for (final String name : user.getGroupNames()) {
			groupsForLogin.add(authLogic.getGroupInfoForGroup(name));
		}
		final JSONArray jsonGroupList = Serializer.serializeGroupsForUser(user, groupsForLogin);

		final JSONObject out = new JSONObject();
		out.put(RESULT, jsonGroupList);
		return out;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject disableUser( //
			@Parameter(USER_ID) final Long userId, //
			@Parameter(DISABLE) final boolean disable) throws JSONException, AuthException {

		final AuthenticationLogic authLogic = authLogic();
		CMUser user;
		if (disable) {
			user = authLogic.disableUserWithId(userId);
		} else {
			user = authLogic.enableUserWithId(userId);
		}

		final JSONObject out = new JSONObject();
		out.put(ROWS, Serializer.serialize(user));
		return out;
	}

	@JSONExported
	public JsonResponse loadClassUiConfiguration( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(CLASS_ID) final Long classId
	) throws JSONException, AuthException {
//		if (classId == 227716)
//			return JsonResponse.success("{\"modify\": true, \"clone\": false, \"remove\": true, \"create\": false}");
//		if (classId == 228102)
//			return JsonResponse.success("{\"modify\": false, \"clone\": true, \"remove\": true, \"create\": true}");
//		if (classId == 230471)
//			return JsonResponse.success("{\"modify\": true, \"clone\": true, \"remove\": true, \"create\": true}");
//		if (classId == 228513)
//			return JsonResponse.success("{\"modify\": true, \"clone\": true, \"remove\": true, \"create\": true}");
		return JsonResponse.success("{}");
	}
	
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JsonResponse saveClassUiConfiguration( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(CLASS_ID) final Long classId, //
			@Parameter("create") final boolean create,
			@Parameter("modify") final boolean modify,
			@Parameter("clone") final boolean clone,
			@Parameter("remove") final boolean remove
			) throws JSONException, AuthException {

		return JsonResponse.success(null);
	}
}
