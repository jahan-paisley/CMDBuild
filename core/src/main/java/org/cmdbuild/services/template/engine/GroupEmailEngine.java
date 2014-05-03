package org.cmdbuild.services.template.engine;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;

public class GroupEmailEngine implements Engine {

	private static final String ROLE_CLASSNAME = "Role";
	private static final String CODE_ATTRIBUTE = "Code";
	private static final String EMAIL_ATTRIBUTE = "Email";

	public static class Builder implements org.apache.commons.lang3.builder.Builder<GroupEmailEngine> {

		private CMDataView dataView;

		private Builder() {
			// use factory method
		}

		@Override
		public GroupEmailEngine build() {
			validate();
			return new GroupEmailEngine(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing data view");
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMDataView dataView;

	private GroupEmailEngine(final Builder builder) {
		this.dataView = builder.dataView;
	}

	@Override
	public Object eval(final String expression) {
		final CMClass roleClass = dataView.findClass(ROLE_CLASSNAME);
		Validate.notNull(roleClass, "role class not visible");
		final CMCard card = dataView.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, CODE_ATTRIBUTE), eq(expression))) //
				.run() //
				.getOnlyRow() //
				.getCard(roleClass);
		return card.get(EMAIL_ATTRIBUTE, String.class);
	}

}
