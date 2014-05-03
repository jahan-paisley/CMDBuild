package org.cmdbuild.services.template.engine;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.guava.Functions.toAttribute;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class GroupUsersEmailEngine implements Engine {

	private static final String USER_CLASSNAME = "User";
	private static final String ROLE_CLASSNAME = "Role";
	private static final String USER_ROLE_DOMAIN = "UserRole";
	private static final String CODE_ATTRIBUTE = "Code";
	private static final String EMAIL_ATTRIBUTE = "Email";

	public static class Builder implements org.apache.commons.lang3.builder.Builder<GroupUsersEmailEngine> {

		private CMDataView dataView;
		private String separator;

		private Builder() {
			// use factory method
		}

		@Override
		public GroupUsersEmailEngine build() {
			validate();
			return new GroupUsersEmailEngine(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing data view");
			Validate.notBlank(separator, "invalid separator");
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder withSeparator(final String separator) {
			this.separator = separator;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMDataView dataView;
	private final String separator;

	private GroupUsersEmailEngine(final Builder builder) {
		this.dataView = builder.dataView;
		this.separator = builder.separator;
	}

	@Override
	public Object eval(final String expression) {
		final List<String> emails = Lists.newArrayList();
		final CMClass userClass = dataView.findClass(USER_CLASSNAME);
		Validate.notNull(userClass, "user class not visible");
		final CMClass roleClass = dataView.findClass(ROLE_CLASSNAME);
		Validate.notNull(roleClass, "role class not visible");
		final CMDomain userRoleDomain = dataView.findDomain(USER_ROLE_DOMAIN);
		Validate.notNull(userRoleDomain, "user-role domain not visible");
		final Iterable<CMQueryRow> rows = dataView
				.select(anyAttribute(roleClass), attribute(userClass, EMAIL_ATTRIBUTE)) //
				.from(roleClass) //
				.join(userClass, over(userRoleDomain)) //
				.where(condition(attribute(roleClass, CODE_ATTRIBUTE), eq(expression))) //
				.run();
		for (final CMQueryRow row : rows) {
			final CMCard card = row.getCard(userClass);
			final String email = card.get(EMAIL_ATTRIBUTE, String.class);
			if (isNotBlank(email)) {
				emails.add(email);
			}
		}
		return Joiner.on(separator) //
				.join(from(rows) //
						.transform(toCard(userClass)) //
						.transform(toAttribute(EMAIL_ATTRIBUTE, String.class)) //
						.filter(notNull()) //
				);
	}

}
