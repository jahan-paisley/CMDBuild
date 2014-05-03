package org.cmdbuild.logic.auth;

import static com.google.common.collect.Iterables.getFirst;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationService.ClientAuthenticatorResponse;
import org.cmdbuild.auth.AuthenticationService.PasswordCallback;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOCreationValidator;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOUpdateValidator;
import org.cmdbuild.logic.auth.UserDTO.UserDTOCreationValidator;
import org.cmdbuild.logic.auth.UserDTO.UserDTOUpdateValidator;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class DefaultAuthenticationLogic implements AuthenticationLogic {

	private static class DefaultGroupInfo implements GroupInfo {

		private final Long id;
		private final String name;
		private final String description;

		public DefaultGroupInfo(final Long id, final String name, final String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Long getId() {
			return id;
		}

	}

	private static class DefaultResponse implements Response {

		private boolean success = false;
		private String reason = null;
		private Collection<GroupInfo> groups = null;

		private DefaultResponse(final boolean success, final String reason, final Collection<GroupInfo> groups) {
			this.success = success;
			this.reason = reason;
			this.groups = groups;
		}

		public static Response newInstance(final boolean success, final String reason,
				final Collection<GroupInfo> groups) {
			return new DefaultResponse(success, reason, groups);
		}

		@Override
		public boolean isSuccess() {
			return success;
		}

		@Override
		public String getReason() {
			return reason;
		}

		@Override
		public Collection<GroupInfo> getGroupsInfo() {
			return groups;
		}

	}

	private static final String USER_GROUP_DOMAIN_NAME = "UserRole";

	private final AuthenticationService authService;
	private final PrivilegeContextFactory privilegeContextFactory;
	private final CMDataView view;
	private final UserStore userStore;

	public DefaultAuthenticationLogic( //
			final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			final CMDataView dataView, //
			final UserStore userStore //
	) {
		this.authService = authenticationService;
		this.privilegeContextFactory = privilegeContextFactory;
		this.view = dataView;
		this.userStore = userStore;
	}

	@Override
	public Response login(final LoginDTO loginDTO) {
		logger.info("trying to login user {} with group {}", loginDTO.getLoginString(), loginDTO.getLoginGroupName());
		logger.trace("login information '{}'", loginDTO);
		final AuthenticatedUser authUser;
		final OperationUser actualOperationUser = userStore.getUser();
		if (!actualOperationUser.getAuthenticatedUser().isAnonymous() && !actualOperationUser.isValid()) {
			/*
			 * header authentication in progress, only group selection is
			 * missing
			 */
			authUser = userStore.getUser().getAuthenticatedUser();
		} else if (loginDTO.isPasswordRequired()) {
			final Login login = Login.newInstance(loginDTO.getLoginString());
			authUser = authService.authenticate(login, loginDTO.getPassword());
		} else {
			final Login login = Login.newInstance(loginDTO.getLoginString());
			authUser = authService.authenticate(login, new PasswordCallback() {
				@Override
				public void setPassword(final String password) {
					// nothing to do
				}
			});
		}

		final boolean userNotAuthenticated = authUser.isAnonymous();
		if (userNotAuthenticated) {
			logger.error("Login failed");
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}

		final String groupName = loginDTO.getLoginGroupName();
		PrivilegeContext privilegeCtx = null;
		if (groupName == null) {
			final CMGroup guessedGroup = guessPreferredGroup(authUser);
			if (guessedGroup == null) {
				logger.error("The user does not have a default group and belongs to multiple groups");
				final List<GroupInfo> groupsForLogin = Lists.newArrayList();
				for (final String name : authUser.getGroupNames()) {
					groupsForLogin.add(getGroupInfoForGroup(name));
				}
				final OperationUser operationUser = new OperationUser(authUser, new NullPrivilegeContext(),
						new NullGroup());
				loginDTO.getUserStore().setUser(operationUser);
				return DefaultResponse.newInstance(false, AuthExceptionType.AUTH_MULTIPLE_GROUPS.toString(),
						groupsForLogin);
			} else if (authUser.getGroupNames().size() == 1) {
				privilegeCtx = buildPrivilegeContext(guessedGroup);
			} else { // the user has a default group
				final Set<String> groupNames = authUser.getGroupNames();
				final CMGroup[] groupsArray = new CMGroup[groupNames.size()];
				int i = 0;
				for (final String name : groupNames) {
					groupsArray[i] = getGroupWithName(name);
					i++;
				}
				privilegeCtx = buildPrivilegeContext(groupsArray);
			}
			final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, guessedGroup);
			loginDTO.getUserStore().setUser(operationUser);
			return buildSuccessfulResponse();
		} else {
			final String selectedGroupName;
			if (authUser.getGroupNames().contains(groupName)) {
				selectedGroupName = groupName;
			} else {
				final String defaultGroupName = authUser.getDefaultGroupName();
				selectedGroupName = (defaultGroupName == null) ? getFirst(authUser.getGroupNames(), groupName)
						: defaultGroupName;
			}
			final CMGroup selectedGroup = getGroupWithName(selectedGroupName);
			privilegeCtx = buildPrivilegeContext(selectedGroup);
			final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, selectedGroup);
			loginDTO.getUserStore().setUser(operationUser);
			return buildSuccessfulResponse();
		}
	}

	@Override
	public ClientAuthenticationResponse login(final ClientAuthenticationRequest request) {
		logger.info("trying to login with no username or password");
		final ClientAuthenticatorResponse response = authService.authenticate(request);
		final AuthenticatedUser authenticatedUser = response.getUser();
		final boolean isValidUser = !authenticatedUser.isAnonymous();
		final boolean hasOneGroupOnly = (authenticatedUser.getGroupNames().size() == 1);
		final boolean hasDefaultGroup = (authenticatedUser.getDefaultGroupName() != null);
		logger.debug("user is valid: {}", isValidUser);
		logger.debug("user has one group only: {}", hasOneGroupOnly);
		logger.debug("user default group: {}", hasDefaultGroup);
		if (isValidUser) {
			if (hasOneGroupOnly || hasDefaultGroup) {
				final String groupName = (hasDefaultGroup) ? authenticatedUser.getDefaultGroupName()
						: authenticatedUser.getGroupNames().iterator().next();
				final CMGroup group = getGroupWithName(groupName);
				final PrivilegeContext privilegeContext = buildPrivilegeContext(group);
				final OperationUser operationUser = new OperationUser(authenticatedUser, privilegeContext, group);
				request.getUserStore().setUser(operationUser);
			} else if (!hasOneGroupOnly) {
				final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(),
						new NullGroup());
				request.getUserStore().setUser(operationUser);
			}
		}
		return new ClientAuthenticationResponse() {

			@Override
			public String getRedirectUrl() {
				return isValidUser ? null : response.getRedirectUrl();
			}

		};
	}

	/**
	 * Gets the default group (if any) or the only one. If no default group has
	 * been found and more than one group is present, {@code null} is returned.
	 */
	private CMGroup guessPreferredGroup(final CMUser user) {
		String guessedGroupName = user.getDefaultGroupName();
		if (guessedGroupName == null) {
			guessedGroupName = getTheFirstAndOnlyGroupName(user);
		}

		if (guessedGroupName != null) {
			return getGroupWithName(guessedGroupName);
		}
		return null;
	}

	private String getTheFirstAndOnlyGroupName(final CMUser user) {
		String firstGroupName = null;
		final Iterator<String> groupNames = user.getGroupNames().iterator();
		if (groupNames.hasNext()) {
			firstGroupName = groupNames.next();
			if (groupNames.hasNext()) {
				firstGroupName = null;
			}
		}
		return firstGroupName;
	}

	private PrivilegeContext buildPrivilegeContext(final CMGroup... groups) {
		return privilegeContextFactory.buildPrivilegeContext(groups);
	}

	private Response buildSuccessfulResponse() {
		return DefaultResponse.newInstance(true, null, null);
	}

	@Override
	public DefaultGroupInfo getGroupInfoForGroup(final String groupName) {
		final CMClass roleClass = view.findClass("Role");
		final CMQueryRow row = view.select(attribute(roleClass, "Description")) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Code"), eq(groupName))) //
				.run().getOnlyRow();
		final String description = (String) row.getCard(roleClass).get("Description");
		final Long roleId = row.getCard(roleClass).getId();
		final DefaultGroupInfo groupInfo = new DefaultGroupInfo(roleId, groupName, description);
		return groupInfo;
	}

	@Override
	public List<CMUser> getUsersForGroupWithId(final Long groupId) {
		return authService.fetchUsersByGroupId(groupId);
	}

	@Override
	public List<Long> getUserIdsForGroupWithId(final Long groupId) {
		return authService.fetchUserIdsByGroupId(groupId);
	}

	@Override
	public Iterable<String> getGroupNamesForUserWithId(final Long userId) {
		final CMUser user = authService.fetchUserById(userId);
		return user.getGroupNames();
	}

	@Override
	public Iterable<String> getGroupNamesForUserWithUsername(final String loginString) {
		final CMUser user = authService.fetchUserByUsername(loginString);
		if (user != null) {
			return user.getGroupNames();
		}
		return Lists.newArrayList();
	}

	@Override
	public CMUser getUserWithId(final Long userId) {
		return authService.fetchUserById(userId);
	}

	@Override
	@Transactional
	public CMUser createUser(final UserDTO userDTO) {
		final ModelValidator<UserDTO> validator = new UserDTOCreationValidator();
		if (!validator.validate(userDTO)) {
			throw ORMExceptionType.ORM_CANT_CREATE_USER.createException();
		}
		if (!existsUserWithUsername(userDTO.getUsername())) {
			return authService.createUser(userDTO);
		} else {
			throw ORMExceptionType.ORM_DUPLICATE_USER.createException();
		}
	}

	private boolean existsUserWithUsername(final String username) {
		final CMUser user = authService.fetchUserByUsername(username);
		return user != null;
	}

	@Override
	@Transactional
	public CMUser updateUser(final UserDTO userDTO) {
		final ModelValidator<UserDTO> validator = new UserDTOUpdateValidator();
		if (!validator.validate(userDTO)) {
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		}
		final CMUser updatedUser = authService.updateUser(userDTO);
		return updatedUser;
	}

	@Override
	public CMGroup getGroupWithId(final Long groupId) {
		return authService.fetchGroupWithId(groupId);
	}

	@Override
	public CMGroup getGroupWithName(final String groupName) {
		return authService.fetchGroupWithName(groupName);
	}

	@Override
	public CMGroup changeGroupStatusTo(final Long groupId, final boolean isActive) {
		return authService.changeGroupStatusTo(groupId, isActive);
	}

	@Override
	public Iterable<CMGroup> getAllGroups() {
		return authService.fetchAllGroups();
	}

	@Override
	public List<CMUser> getAllUsers() {
		return authService.fetchAllUsers();
	}

	@Override
	public CMUser enableUserWithId(final Long userId) {
		return authService.enableUserWithId(userId);
	}

	@Override
	public CMUser disableUserWithId(final Long userId) {
		return authService.disableUserWithId(userId);
	}

	@Override
	public CMGroup createGroup(final GroupDTO groupDTO) {
		final ModelValidator<GroupDTO> validator = new GroupDTOCreationValidator();
		if (!validator.validate(groupDTO)) {
			throw ORMExceptionType.ORM_CANT_CREATE_GROUP.createException();
		}

		// the restricted administrator could not create
		// a new group with administrator privileges
		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		if (userGroup.isRestrictedAdmin() && groupDTO.isAdministrator()) {

			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}

		final String groupName = groupDTO.getName();
		if (!existsGroupWithName(groupName)) {
			return authService.createGroup(groupDTO);
		} else {
			throw ORMExceptionType.ORM_DUPLICATE_GROUP.createException();
		}
	}

	private boolean existsGroupWithName(final String groupName) {
		final CMGroup group = authService.fetchGroupWithName(groupName);
		if (group instanceof NullGroup) {
			return false;
		}
		return true;
	}

	@Override
	public CMGroup updateGroup(final GroupDTO groupDTO) {
		final ModelValidator<GroupDTO> validator = new GroupDTOUpdateValidator();

		if (!validator.validate(groupDTO)) {
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		}

		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		final CMGroup groupToUpdate = authService.fetchGroupWithId(groupDTO.getGroupId());

		// the restricted administrator could update only non administrator
		// groups or other restricted groups. In any case it could not set them
		// as full administration.
		if (userGroup.isRestrictedAdmin()) {
			if (groupToUpdate.isAdmin() && !groupToUpdate.isRestrictedAdmin()) {
				throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
			} else if (groupDTO.isAdministrator()) {
				throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
			}
		}

		final CMGroup updatedGroup = authService.updateGroup(groupDTO);
		return updatedGroup;
	}

	@Override
	public CMGroup setGroupActive(final Long groupId, final boolean active) {
		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		final CMGroup groupToUpdate = authService.fetchGroupWithId(groupId);

		// A group could not activate/deactivate itself
		if (userGroup.getId().equals(groupToUpdate.getId())) {
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}

		// The restricted administrator could
		// activate/deactivate only non administrator groups
		checkRestrictedAdminOverFullAdmin(groupToUpdate.getId());

		return authService.setGroupActive(groupId, active);
	}

	@Override
	@Transactional
	public void addUserToGroup(final Long userId, final Long groupId) {
		/*
		 * a restricted administrator could not add a user to a full
		 * administrator group
		 */
		checkRestrictedAdminOverFullAdmin(groupId);

		final CMDomain userRoleDomain = view.findDomain(USER_GROUP_DOMAIN_NAME);
		final CMRelationDefinition relationDefinition = view.createRelationFor(userRoleDomain);
		relationDefinition.setCard1(fetchUserCardWithId(userId));
		relationDefinition.setCard2(fetchRoleCardWithId(groupId));
		relationDefinition.save();
	}

	private void checkRestrictedAdminOverFullAdmin(final Long groupId) {
		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		final CMGroup groupToUpdate = authService.fetchGroupWithId(groupId);
		if (userGroup.isRestrictedAdmin() && groupToUpdate.isAdmin() && !groupToUpdate.isRestrictedAdmin()) {

			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
	}

	private CMGroup getCurrentlyLoggedUserGroup() {
		final OperationUser operationUser = userStore.getUser();
		return operationUser.getPreferredGroup();
	}

	private CMCard fetchUserCardWithId(final Long userId) {
		final CMClass userClass = view.findClass("User");
		final CMQueryRow userRow = view.select(anyAttribute(userClass)) //
				.from(userClass) //
				.where(condition(QueryAliasAttribute.attribute(userClass, "Id"), eq(userId))) //
				.run().getOnlyRow();
		return userRow.getCard(userClass);
	}

	private CMCard fetchRoleCardWithId(final Long groupId) {
		final CMClass roleClass = view.findClass("Role");
		final CMQueryRow groupRow = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(QueryAliasAttribute.attribute(roleClass, "Id"), eq(groupId))) //
				.run().getOnlyRow();
		return groupRow.getCard(roleClass);
	}

	@Override
	@Transactional
	public void removeUserFromGroup(final Long userId, final Long groupId) {
		/*
		 * a restricted administrator could not remove a user from a full
		 * administrator group
		 */
		checkRestrictedAdminOverFullAdmin(groupId);

		final CMDomain userRoleDomain = view.findDomain("UserRole");
		final CMClass roleClass = view.findClass("Role");
		final CMClass userClass = view.findClass("User");

		final CMQueryRow row = view.select(attribute(userClass, "Username")) //
				.from(userClass) //
				.join(roleClass, over(userRoleDomain)) //
				.where(and(condition(attribute(userClass, "Id"), eq(userId)), //
						condition(attribute(roleClass, "Id"), eq(groupId)))) //
				.run().getOnlyRow();

		final CMRelation relationToBeRemoved = row.getRelation(userRoleDomain).getRelation();
		final CMRelationDefinition relationDefinition = view.update(relationToBeRemoved);
		relationDefinition.delete();
	}

}
