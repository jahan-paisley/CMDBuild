package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Set;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.auth.UserGroup;
import org.cmdbuild.services.auth.UserInfo;

import com.google.common.base.Function;

public class AuthenticationLogicHelper implements SoapLogicHelper {

	private final OperationUser operationUser;
	private final CMDataView dataView;
	private final AuthenticationStore authenticationStore;

	public AuthenticationLogicHelper(final OperationUser operationUser, final CMDataView dataView,
			final AuthenticationStore authenticationStore) {
		this.operationUser = operationUser;
		this.dataView = dataView;
		this.authenticationStore = authenticationStore;
	}

	public UserInfo getUserInfo() {
		final AuthenticatedUser authenticatedUser = operationUser.getAuthenticatedUser();
		final UserInfo userInfo = new UserInfo();
		userInfo.setUsername(authenticatedUser.getUsername());
		userInfo.setGroups(groups(authenticatedUser.getGroupNames()));
		userInfo.setUserType(authenticationStore.getType());
		return userInfo;
	}

	private Set<UserGroup> groups(final Iterable<String> names) {
		final CMClass roleClass = dataView.findClass("Role");
		final Iterable<CMQueryRow> userGroupsRows = dataView.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, CODE_ATTRIBUTE), //
						in((Object[]) from(names).toArray(String.class)))) //
				.run();
		return from(userGroupsRows) //
				.transform(new Function<CMQueryRow, UserGroup>() {
					@Override
					public UserGroup apply(final CMQueryRow input) {
						final CMCard card = input.getCard(roleClass);
						final UserGroup userGroup = new UserGroup();
						userGroup.setName((String) card.getCode());
						userGroup.setDescription((String) card.getDescription());
						return userGroup;
					}
				}) //
				.toSet();
	}

}
