package org.cmdbuild.auth;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.Login.LoginType;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.AnonymousUser;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.AuthenticatedUserImpl;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.dao.Const.Role;
import org.cmdbuild.dao.Const.User;
import org.cmdbuild.dao.Const.UserRole;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.UserDTO;

import com.google.common.collect.Lists;

public class DefaultAuthenticationService implements AuthenticationService {

	private static final String ID = "Id";

	public interface Configuration {
		/**
		 * Returns the names of the authenticators that should be activated, or
		 * null if all authenticators should be activated.
		 * 
		 * @return active authenticators or null
		 */
		Set<String> getActiveAuthenticators();

		/**
		 * Return the names of the service users. They can only log in with
		 * password callback and can impersonate other users. Null means that
		 * there is no service user defined.
		 * 
		 * @return a list of service users or null
		 */
		Set<String> getServiceUsers();

		/**
		 * Return the names of the privileged service users. Every operation
		 * performed will be stored with a special label with this format "
		 * {@code system/$USER}", where "{@code $USER}" is the name of the
		 * impersonated user.
		 * 
		 * @return a list of privileged service users or null.
		 */
		Set<String> getPrivilegedServiceUsers();
	}

	private interface FetchCallback {

		void foundUser(AuthenticatedUser authUser);
	}

	private static final UserStore DUMB_STORE = new UserStore() {

		@Override
		public OperationUser getUser() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setUser(final OperationUser user) {
			throw new UnsupportedOperationException();
		}
	};

	private PasswordAuthenticator[] passwordAuthenticators;
	private ClientRequestAuthenticator[] clientRequestAuthenticators;
	private UserFetcher[] userFetchers;
	private GroupFetcher groupFetcher;
	private UserStore userStore;
	private final CMDataView view;

	private final Set<String> serviceUsers;
	private final Set<String> authenticatorNames;

	public DefaultAuthenticationService(final CMDataView dataView) {
		this(new Configuration() {

			@Override
			public Set<String> getActiveAuthenticators() {
				return null;
			}

			@Override
			public Set<String> getServiceUsers() {
				return null;
			}

			@Override
			public Set<String> getPrivilegedServiceUsers() {
				return null;
			}
		}, dataView);
	}

	public DefaultAuthenticationService(final Configuration conf, final CMDataView dataView) {
		Validate.notNull(conf);
		this.serviceUsers = conf.getServiceUsers();
		this.authenticatorNames = conf.getActiveAuthenticators();
		passwordAuthenticators = new PasswordAuthenticator[0];
		clientRequestAuthenticators = new ClientRequestAuthenticator[0];
		userFetchers = new UserFetcher[0];
		userStore = DUMB_STORE;
		view = dataView;
	}

	@Override
	public void setPasswordAuthenticators(final PasswordAuthenticator... passwordAuthenticators) {
		Validate.noNullElements(passwordAuthenticators);
		this.passwordAuthenticators = passwordAuthenticators;
	}

	@Override
	public void setClientRequestAuthenticators(final ClientRequestAuthenticator... clientRequestAuthenticators) {
		Validate.noNullElements(clientRequestAuthenticators);
		this.clientRequestAuthenticators = clientRequestAuthenticators;
	}

	@Override
	public void setUserFetchers(final UserFetcher... userFetchers) {
		Validate.noNullElements(userFetchers);
		this.userFetchers = userFetchers;
	}

	@Override
	public void setGroupFetcher(final GroupFetcher groupFetcher) {
		Validate.notNull(groupFetcher);
		this.groupFetcher = groupFetcher;
	}

	@Override
	public void setUserStore(final UserStore userStore) {
		Validate.notNull(userStore);
		this.userStore = userStore;
	}

	private boolean isInactive(final CMAuthenticator authenticator) {
		return authenticatorNames != null && !authenticatorNames.contains(authenticator.getName());
	}

	private boolean isServiceUser(final CMUser user) {
		return isServiceUser(user.getUsername());
	}

	private boolean isServiceUser(final Login login) {
		return (login.getType() == LoginType.USERNAME) && isServiceUser(login.getValue());
	}

	private boolean isServiceUser(final String username) {
		return serviceUsers != null && serviceUsers.contains(username);
	}

	@Override
	public AuthenticatedUser authenticate(final Login login, final String password) {
		if (isServiceUser(login)) {
			return ANONYMOUS_USER;
		}
		for (final PasswordAuthenticator passwordAuthenticator : passwordAuthenticators) {
			if (isInactive(passwordAuthenticator)) {
				continue;
			}
			final boolean isUserAuthenticated = passwordAuthenticator.checkPassword(login, password);
			if (isUserAuthenticated) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						final PasswordChanger passwordChanger = passwordAuthenticator.getPasswordChanger(login);
						authUser.setPasswordChanger(passwordChanger);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	@Override
	public AuthenticatedUser authenticate(final Login login, final PasswordCallback passwordCallback) {
		for (final PasswordAuthenticator pa : passwordAuthenticators) {
			if (isInactive(pa)) {
				continue;
			}
			final String pass = pa.fetchUnencryptedPassword(login);
			if (pass != null) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						final PasswordChanger passwordChanger = pa.getPasswordChanger(login);
						authUser.setPasswordChanger(passwordChanger);
						passwordCallback.setPassword(pass);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	@Override
	public ClientAuthenticatorResponse authenticate(final ClientRequest request) {
		for (final ClientRequestAuthenticator cra : clientRequestAuthenticators) {
			if (isInactive(cra)) {
				continue;
			}
			final ClientRequestAuthenticator.Response response = cra.authenticate(request);
			if (response != null) {
				AuthenticatedUser authUser = new AnonymousUser();
				if (response.getLogin() != null) {
					authUser = fetchAuthenticatedUser(response.getLogin(), new FetchCallback() {
						@Override
						public void foundUser(final AuthenticatedUser authUser) {
							// empty for now
						}
					});
				}
				return new ClientAuthenticatorResponse(authUser, response.getRedirectUrl());
			}
		}
		return ClientAuthenticatorResponse.EMTPY_RESPONSE;
	}

	@Override
	public OperationUser impersonate(final Login login) {
		final OperationUser operationUser = userStore.getUser();
		if (operationUser.hasAdministratorPrivileges() || isServiceUser(operationUser.getAuthenticatedUser())) {
			final CMUser user = fetchUser(login);
			operationUser.impersonate(user);
			return operationUser;
		}
		return operationUser;
	}

	@Override
	public OperationUser getOperationUser() {
		return userStore.getUser();
	}

	@Override
	public CMUser fetchUserByUsername(final String username) {
		final Login login = Login.newInstance(username);
		return fetchUser(login);
	}

	private AuthenticatedUser fetchAuthenticatedUser(final Login login, final FetchCallback callback) {
		AuthenticatedUser authUser = ANONYMOUS_USER;
		final CMUser user = fetchUser(login);
		if (user != null) {
			authUser = AuthenticatedUserImpl.newInstance(user);
			callback.foundUser(authUser);
		}
		return authUser;
	}

	private CMUser fetchUser(final Login login) {
		CMUser user = null;
		for (final UserFetcher uf : userFetchers) {
			user = uf.fetchUser(login);
			if (user != null) {
				break;
			}
		}
		return user;
	}

	@Override
	public List<CMUser> fetchUsersByGroupId(final Long groupId) {
		List<CMUser> users = Lists.newArrayList();
		for (final UserFetcher userFetcher : userFetchers) {
			users = userFetcher.fetchUsersFromGroupId(groupId);
			if (!users.isEmpty()) {
				break;
			}
		}
		return users;
	}

	@Override
	public List<Long> fetchUserIdsByGroupId(final Long groupId) {
		List<Long> users = Lists.newArrayList();
		for (final UserFetcher userFetcher : userFetchers) {
			users = userFetcher.fetchUserIdsFromGroupId(groupId);
			if (!users.isEmpty()) {
				break;
			}
		}
		return users;
	}

	@Override
	public CMUser fetchUserById(final Long userId) {
		CMUser user = null;
		for (final UserFetcher userFetcher : userFetchers) {
			user = userFetcher.fetchUserById(userId);
			if (user != null) {
				break;
			}
		}
		return user;
	}

	@Override
	public CMUser createUser(final UserDTO userDTO) {
		final Digester digester = new Base64Digester();
		final CMCard createdUserCard = view.createCardFor(userClass()) //
				.set(User.DESCRIPTION, userDTO.getDescription()) //
				.set(User.USERNAME, userDTO.getUsername()) //
				.set(User.PASSWORD, digester.encrypt(userDTO.getPassword())) //
				.set(User.EMAIL, userDTO.getEmail()) //
				.set(User.ACTIVE, userDTO.isActive()) //
				.save();
		return fetchUserById(createdUserCard.getId());
	}

	@Override
	public CMUser updateUser(final UserDTO userDTO) {
		final Digester digester = new Base64Digester();
		final CMCard userCard = fetchUserCardWithId(userDTO.getUserId());
		final CMCardDefinition cardToBeUpdated = view.update(userCard) //
				.set(User.ACTIVE, userDTO.isActive());
		if (userDTO.getDescription() != null) {
			cardToBeUpdated.set(User.DESCRIPTION, userDTO.getDescription());
		}
		if (userDTO.getEmail() != null) {
			cardToBeUpdated.set(User.EMAIL, userDTO.getEmail());
		}
		if (userDTO.getPassword() != null) {
			cardToBeUpdated.set(User.PASSWORD, digester.encrypt(userDTO.getPassword()));
		}
		cardToBeUpdated.save();

		final Long defaultGroupId = userDTO.getDefaultGroupId();
		final List<CMRelationDefinition> relationsInUpdateOrder = Lists.newArrayList();
		for (final CMRelation relation : fetchRelationsForUserWithId(userDTO.getUserId())) {
			final boolean isActualDefaultGroup = isTrue(relation.get(UserRole.DEFAULT_GROUP, Boolean.class));
			final boolean isNewDefaultGroup = relation.getCard2Id().equals(defaultGroupId);
			final CMRelationDefinition definition = view.update(relation) //
					/*
					 * TODO implement within dao layer
					 * 
					 * at the moment queried relations doesn't have card1 and
					 * card2, so we must set them until it will be fixed
					 */
					.setCard1(cardId1Of(relation)) //
					.setCard2(cardId2Of(relation)) //
					.set(UserRole.DEFAULT_GROUP, isNewDefaultGroup);
			if (isActualDefaultGroup) {
				relationsInUpdateOrder.add(0, definition);
			} else {
				relationsInUpdateOrder.add(definition);
			}
		}
		for (final CMRelationDefinition relation : relationsInUpdateOrder) {
			relation.update();
		}

		return fetchUserById(userDTO.getUserId());
	}

	private CMCard cardId1Of(final CMRelation relation) {
		final CMClass target = view.findClass("Class");
		return view.select(anyAttribute(target)) //
				.from(target) //
				.where(condition(attribute(target, ID), eq(relation.getCard1Id()))) //
				.run() //
				.getOnlyRow() //
				.getCard(target);
	}

	private CMCard cardId2Of(final CMRelation relation) {
		final CMClass target = view.findClass("Class");
		return view.select(anyAttribute(target)) //
				.from(target) //
				.where(condition(attribute(target, ID), eq(relation.getCard2Id()))) //
				.run() //
				.getOnlyRow() //
				.getCard(target);
	}

	private CMCard fetchUserCardWithId(final Long userId) throws NoSuchElementException {
		final Alias userClassAlias = EntryTypeAlias.canonicalAlias(userClass());
		final CMQueryRow userRow = view.select(attribute(userClassAlias, User.USERNAME), //
				attribute(userClassAlias, User.DESCRIPTION), //
				attribute(userClassAlias, User.PASSWORD)) //
				.from(userClass(), as(userClassAlias)) //
				.where(condition(attribute(userClassAlias, ID), eq(userId))) //
				.run().getOnlyRow();
		final CMCard userCard = userRow.getCard(userClassAlias);
		return userCard;
	}

	private List<CMRelation> fetchRelationsForUserWithId(final Long userId) {
		final CMQueryResult result = view
				.select(anyAttribute(userGroupDomain()), attribute(roleClass(), roleClass().getCodeAttributeName())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(condition(attribute(userClass(), ID), eq(userId))) //
				.run();
		final List<CMRelation> relations = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMRelation relation = row.getRelation(userGroupDomain()).getRelation();
			relations.add(relation);
		}
		return relations;
	}

	@Override
	public Iterable<CMGroup> fetchAllGroups() {
		return groupFetcher.fetchAllGroups();
	}

	@Override
	public List<CMUser> fetchAllUsers() {
		for (final UserFetcher uf : userFetchers) {
			return uf.fetchAllUsers();
		}
		return Lists.newArrayList();
	}

	@Override
	public CMGroup fetchGroupWithId(final Long groupId) {
		return groupFetcher.fetchGroupWithId(groupId);
	}

	@Override
	public CMGroup fetchGroupWithName(final String groupName) {
		return groupFetcher.fetchGroupWithName(groupName);
	}

	@Override
	public CMGroup changeGroupStatusTo(final Long groupId, final boolean isActive) {
		return groupFetcher.changeGroupStatusTo(groupId, isActive);
	}

	@Override
	public CMUser enableUserWithId(final Long userId) {
		final UserDTO userDTO = UserDTO.newInstance() //
				.withUserId(userId) //
				.withActiveStatus(true) //
				.build();
		return updateUser(userDTO);
	}

	@Override
	public CMUser disableUserWithId(final Long userId) {
		final UserDTO userDTO = UserDTO.newInstance() //
				.withUserId(userId) //
				.withActiveStatus(false) //
				.build();
		return updateUser(userDTO);
	}

	@Override
	public CMGroup createGroup(final GroupDTO groupDTO) {
		final CMCard createdGroupCard = view.createCardFor(roleClass()) //
				.set(Role.CODE, groupDTO.getName()) //
				.set(Role.DESCRIPTION, groupDTO.getDescription()) //
				.set(Role.EMAIL, groupDTO.getEmail()) //
				.set(Role.ACTIVE, groupDTO.isActive()) //
				.set(Role.STARTING_CLASS, groupDTO.getStartingClassId()) //
				.set(Role.ADMINISTRATOR, groupDTO.isAdministrator()) //
				.set(Role.RESTRICTED_ADINISTRATOR, groupDTO.isRestrictedAdministrator()) //
				.save();
		return groupFetcher.fetchGroupWithId(createdGroupCard.getId());
	}

	@Override
	public CMGroup updateGroup(final GroupDTO groupDTO) {
		final CMCard groupCard = fetchGroupCardWithId(groupDTO.getGroupId());
		final CMCardDefinition groupToUpdate = view.update(groupCard) //
				.set(Role.ACTIVE, groupDTO.isActive()) //
				.set(Role.ADMINISTRATOR, groupDTO.isAdministrator()) //
				.set(Role.RESTRICTED_ADINISTRATOR, groupDTO.isRestrictedAdministrator());
		if (groupDTO.getDescription() != null) {
			groupToUpdate.set(Role.DESCRIPTION, groupDTO.getDescription());
		}
		if (groupDTO.getEmail() != null) {
			groupToUpdate.set(Role.EMAIL, groupDTO.getEmail());
		}
		if (groupDTO.getStartingClassId() != null) {
			groupToUpdate.set(Role.STARTING_CLASS, groupDTO.getStartingClassId());
		}
		final CMCard createdGroupCard = groupToUpdate.save();
		return fetchGroupWithId(createdGroupCard.getId());
	}

	@Override
	public CMGroup setGroupActive(final Long groupId, final boolean active) {
		final CMCard groupCard = fetchGroupCardWithId(groupId);
		final CMCard updatedGroupCard = view.update(groupCard) //
				.set(Role.ACTIVE, active) //
				.save();
		return fetchGroupWithId(updatedGroupCard.getId());
	}

	private CMCard fetchGroupCardWithId(final Long groupId) throws NoSuchElementException {
		final Alias groupClassAlias = EntryTypeAlias.canonicalAlias(roleClass());
		final CMQueryRow userRow = view.select(anyAttribute(groupClassAlias)) //
				.from(roleClass(), as(groupClassAlias)) //
				.where(condition(attribute(groupClassAlias, ID), eq(groupId))) //
				.run().getOnlyRow();
		final CMCard groupCard = userRow.getCard(groupClassAlias);
		return groupCard;
	}

	private CMClass userClass() {
		return view.findClass("User");
	}

	private CMClass roleClass() {
		return view.findClass("Role");
	}

	private CMDomain userGroupDomain() {
		return view.findDomain("UserRole");
	}

}
