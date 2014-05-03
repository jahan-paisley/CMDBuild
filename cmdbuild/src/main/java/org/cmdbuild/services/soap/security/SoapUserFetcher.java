package org.cmdbuild.services.soap.security;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.DBUserFetcher;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.cache.CachingService.Cacheable;

public class SoapUserFetcher extends DBUserFetcher implements Cacheable {

	private static final String ORG_CMDBUILD_PORTLET_GROUP_DOMAIN = "org.cmdbuild.portlet.group.domain";
	private static final String ORG_CMDBUILD_PORTLET_USER_EMAIL = "org.cmdbuild.portlet.user.email";
	private static final String ORG_CMDBUILD_PORTLET_USER_USERNAME = "org.cmdbuild.portlet.user.username";
	private static final String ORG_CMDBUILD_PORTLET_USER_TABLE = "org.cmdbuild.portlet.user.table";

	private final AuthenticationStore userTypeStore;

	private boolean initialized = false;
	private String table;
	private String username;
	private String email;
	private String domain;

	public SoapUserFetcher(final CMDataView view, final AuthenticationStore userTypeStore) {
		super(view);
		this.userTypeStore = userTypeStore;
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			initialized = false;
		}
	}

	private void initialize() {
		synchronized (this) {
			if (!initialized) {
				table = null;
				username = null;
				email = null;
				domain = null;

				final CMClass metadataClass = view.findClass("Metadata");
				final CMQueryResult queryResult = view.select(anyAttribute(metadataClass)) //
						.from(metadataClass) //
						.where(or( //
								condition(attribute(metadataClass, DESCRIPTION_ATTRIBUTE), //
										eq(ORG_CMDBUILD_PORTLET_USER_TABLE)), //
								condition(attribute(metadataClass, DESCRIPTION_ATTRIBUTE), //
										eq(ORG_CMDBUILD_PORTLET_USER_USERNAME)), //
								condition(attribute(metadataClass, DESCRIPTION_ATTRIBUTE), //
										eq(ORG_CMDBUILD_PORTLET_USER_EMAIL)), //
								condition(attribute(metadataClass, DESCRIPTION_ATTRIBUTE), //
										eq(ORG_CMDBUILD_PORTLET_GROUP_DOMAIN)))) //
						.run();
				for (final CMQueryRow row : queryResult) {
					final CMCard card = row.getCard(metadataClass);
					final String description = card.get(DESCRIPTION_ATTRIBUTE, String.class);
					final String notes = card.get("Notes", String.class);
					if (ORG_CMDBUILD_PORTLET_USER_TABLE.equals(description)) {
						table = notes;
					} else if (ORG_CMDBUILD_PORTLET_USER_USERNAME.equals(description)) {
						username = notes;
					} else if (ORG_CMDBUILD_PORTLET_USER_EMAIL.equals(description)) {
						email = notes;
					} else if (ORG_CMDBUILD_PORTLET_GROUP_DOMAIN.equals(description)) {
						domain = notes;
					}
				}
			}
			initialized = true;
		}
	}

	@Override
	protected final CMClass userClass() {
		initialize();
		return view.findClass(table);
	}

	@Override
	protected final CMClass roleClass() {
		initialize();
		return view.findClass("Role");
	}

	@Override
	protected final String userEmailAttribute() {
		initialize();
		return email;
	}

	@Override
	protected final String userNameAttribute() {
		initialize();
		return username;
	}

	@Override
	protected final String userDescriptionAttribute() {
		initialize();
		return (username == null) ? email : username;
	}

	@Override
	protected final String userPasswordAttribute() {
		throw new UnsupportedOperationException("why here?");
	}

	@Override
	protected final String userIdAttribute() {
		initialize();
		return Const.SystemAttributes.Id.getDBName();
	}

	@Override
	protected CMDomain userGroupDomain() {
		initialize();
		return view.findDomain(domain);
	}

	@Override
	protected boolean allowsDefaultGroup() {
		return false;
	}

	@Override
	protected boolean isActive(final CMCard userCard) {
		return true;
	}

	@Override
	public CMUser fetchUser(final Login login) {
		final CMUser user = super.fetchUser(login);
		if (user != null) {
			userTypeStore.setType(UserType.DOMAIN);
		}
		return user;
	}

	@Override
	protected WhereClause activeCondition(final Alias userClassAlias) {
		return trueWhereClause();
	}

}
