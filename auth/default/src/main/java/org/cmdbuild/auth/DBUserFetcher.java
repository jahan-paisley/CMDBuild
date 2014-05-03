package org.cmdbuild.auth;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.UserImpl;
import org.cmdbuild.auth.user.UserImpl.UserImplBuilder;
import org.cmdbuild.dao.Const.Role;
import org.cmdbuild.dao.Const.User;
import org.cmdbuild.dao.Const.UserRole;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.NullOnErrorOfGetCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Implements user, group and privilege management on top of the DAO layer
 */
public abstract class DBUserFetcher implements UserFetcher {

	private static final String ROLE_NAME_COLUMN = org.cmdbuild.common.Constants.CODE_ATTRIBUTE;

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final CMDataView view;

	protected DBUserFetcher(final CMDataView view) {
		Validate.notNull(view);
		this.view = view;
	}

	@Override
	public CMUser fetchUser(final Login login) {
		final CMCard userCard = fetchUserCard(login);
		return (userCard == null) ? null : buildUserFromCard(userCard);
	}

	@Override
	public CMUser fetchUserById(final Long userId) {
		final CMQueryRow row = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.where(condition(attribute(userClass(), userClass().getKeyAttributeName()), eq(userId))) //
				.run() //
				.getOnlyRow();
		return buildUserFromCard(row.getCard(userClass()));
	}

	@Override
	public List<CMUser> fetchUsersFromGroupId(final Long groupId) {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(condition(attribute(roleClass(), roleClass().getKeyAttributeName()), eq(groupId))) //
				.run();

		final List<CMUser> usersForSpecifiedGroup = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			final CMUser user = buildUserFromCard(userCard);
			usersForSpecifiedGroup.add(user);
		}
		return usersForSpecifiedGroup;

	}

	@Override
	public List<Long> fetchUserIdsFromGroupId(final Long groupId) {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(condition(attribute(roleClass(), roleClass().getKeyAttributeName()), eq(groupId))) //
				.run();

		final List<Long> userIdsForSpecifiedGroup = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			userIdsForSpecifiedGroup.add(userCard.getId());
		}
		return userIdsForSpecifiedGroup;

	}

	private CMUser buildUserFromCard(final CMCard _userCard) {
		final CMCard userCard = NullOnErrorOfGetCard.of(_userCard);
		final Long userId = userCard.getId();
		final String username = userCard.get(userNameAttribute(), String.class);
		final String email = userCard.get(userEmailAttribute(), String.class);
		final String userDescription = userCard.get(userDescriptionAttribute(), String.class);
		final String defaultGroupName = fetchDefaultGroupNameForUser(username);
		final UserImplBuilder userBuilder = UserImpl.newInstanceBuilder() //
				.withId(userId) //
				.withUsername(defaultString(username)) //
				.withEmail(defaultString(email)) //
				.withDescription(defaultString(userDescription)) //
				.withDefaultGroupName(defaultGroupName); //

		final List<String> userGroups = fetchGroupNamesForUser(userId);
		for (final String groupName : userGroups) {
			userBuilder.withGroupName(groupName);
			addGroupDescription(userBuilder, groupName);
		}
		userBuilder.withActiveStatus(isActive(userCard));
		return userBuilder.build();
	}

	/**
	 * @param userBuilder
	 * @param groupName
	 */
	private void addGroupDescription( //
			final UserImplBuilder userBuilder, //
			final String groupName //
	) {
		try {
			final CMCard roleCard = view.select(anyAttribute(roleClass())) //
					.from(roleClass()) //
					.where(condition(attribute(roleClass(), ROLE_NAME_COLUMN), eq(groupName))) //
					.run() //
					.getOnlyRow() //
					.getCard(roleClass());

			final Object roleDescription = roleCard.getDescription();
			if (roleDescription != null) {
				userBuilder.withGroupDescription(roleDescription.toString());
			}
		} catch (final Exception e) {
			logger.debug("Error reading description of group " + groupName);
		}
	}

	protected boolean isActive(final CMCard userCard) {
		return userCard.get(User.ACTIVE, Boolean.class);
	}

	private String fetchDefaultGroupNameForUser(final String username) {
		String defaultGroupName = null;
		if (allowsDefaultGroup()) {
			final CMQueryResult result = view
					.select(attribute(userClass(), userNameAttribute()),
							attribute(userGroupDomain(), UserRole.DEFAULT_GROUP),
							attribute(roleClass(), roleClass().getCodeAttributeName())) //
					.from(userClass()) //
					.join(roleClass(), over(userGroupDomain())) //
					.where(condition(attribute(userClass(), userNameAttribute()), //
							eq(username))) //
					.run();

			for (final CMQueryRow row : result) {
				final CMCard group = row.getCard(roleClass());
				final CMRelation relation = row.getRelation(userGroupDomain()).getRelation();
				final String groupName = (String) group.getCode();
				final Object isDefaultGroup = relation.get(UserRole.DEFAULT_GROUP);
				if (isDefaultGroup != null) {
					if ((Boolean) isDefaultGroup) {
						defaultGroupName = groupName;
					}
				}
			}
		}
		return defaultGroupName;
	}

	protected boolean allowsDefaultGroup() {
		return true;
	}

	protected CMCard fetchUserCard(final Login login) throws NoSuchElementException {
		final Alias userClassAlias = EntryTypeAlias.canonicalAlias(userClass());
		final CMQueryResult queryResult = view.select(anyAttribute(userClass())) //
				.from(userClass(), as(userClassAlias)) //
				.where(and( //
						activeCondition(userClassAlias), //
						condition(attribute(userClassAlias, loginAttributeName(login)), //
								eq(login.getValue())))) //
				.run();
		final CMCard userCard;
		if (queryResult.size() == 1) {
			userCard = queryResult.getOnlyRow().getCard(userClassAlias);
		} else {
			userCard = null;
		}
		return userCard;
	}

	protected WhereClause activeCondition(final Alias userClassAlias) {
		return condition(attribute(userClassAlias, User.ACTIVE), eq(true));
	}

	private List<String> fetchGroupNamesForUser(final Long userId) {
		final List<String> groupNames = new ArrayList<String>();
		final Alias groupClassAlias = EntryTypeAlias.canonicalAlias(roleClass());
		final Alias userClassAlias = EntryTypeAlias.canonicalAlias(userClass());
		final CMQueryResult userGroupsRows = view.select(attribute(groupClassAlias, Role.CODE)) //
				.from(roleClass()) //
				.join(userClass(), as(userClassAlias), over(userGroupDomain())) //
				.where(and( //
						condition(attribute(roleClass(), Role.ACTIVE), //
								eq(true)), //
						condition(attribute(userClass(), userIdAttribute()), //
								eq(userId)))) //
				.run();
		for (final CMQueryRow row : userGroupsRows) {
			final CMCard groupCard = row.getCard(groupClassAlias);
			groupNames.add((String) groupCard.getCode());
		}
		return groupNames;
	}

	@Override
	public List<CMUser> fetchAllUsers() {
		final CMQueryResult result = view.select(anyAttribute(userClass())) //
				.from(userClass()) //
				.run();
		final List<CMUser> allUsers = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard userCard = row.getCard(userClass());
			final CMUser user = buildUserFromCard(userCard);
			allUsers.add(user);
		}
		return allUsers;
	}

	/*
	 * Methods to shade class and attribute names. They should be detected by
	 * metadatas, but for now we stick to what the DBA has decided.
	 */

	protected abstract CMClass userClass();

	protected abstract CMClass roleClass();

	protected abstract String userEmailAttribute();

	protected abstract String userNameAttribute();

	protected abstract String userDescriptionAttribute();

	protected abstract String userPasswordAttribute();

	protected abstract String userIdAttribute();

	protected abstract CMDomain userGroupDomain();

	protected String loginAttributeName(final Login login) {
		switch (login.getType()) {
		case USERNAME:
			return userNameAttribute();
		case EMAIL:
			return userEmailAttribute();
		default:
			throw new IllegalArgumentException("Unsupported login type");
		}
	}

}
