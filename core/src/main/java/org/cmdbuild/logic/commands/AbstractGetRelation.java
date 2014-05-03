package org.cmdbuild.logic.commands;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.data.Card;
import org.joda.time.DateTime;

public class AbstractGetRelation {

	protected static abstract class GetRelationResponse {

		protected final void addRelation(final QueryRelation relation, final CMCard destination) {
			if ((relation != null) && (destination != null)) {
				doAddRelation(new RelationInfo(relation, destination));
			}
		}

		protected abstract void doAddRelation(RelationInfo relationInfo);

	}

	// TODO Change Code, Description, Id with something meaningful
	protected static final String ID = org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
	protected static final String CODE = org.cmdbuild.dao.driver.postgres.Const.CODE_ATTRIBUTE;
	protected static final String DESCRIPTION = org.cmdbuild.dao.driver.postgres.Const.DESCRIPTION_ATTRIBUTE;

	protected static final Alias DOM_ALIAS = NameAlias.as("DOM");
	protected static final Alias DST_ALIAS = NameAlias.as("DST");

	protected CMDataView view;

	public AbstractGetRelation(final CMDataView view) {
		this.view = view;
	}

	protected QuerySpecsBuilder getRelationQuery(final Card src, final CMDomain domain) {
		return getRelationQuerySpecsBuilder(src, domain, null);
	}

	protected QuerySpecsBuilder getRelationQuerySpecsBuilder(final Card src, final CMDomain domain,
			final WhereClause whereClause) {
		final CMClass srcCardType = getCardType(src);
		WhereClause clause = trueWhereClause();
		if ((src.getId() != null) && (src.getId() > 0L)) {
			if (whereClause == null || whereClause instanceof EmptyWhereClause) {
				clause = condition(attribute(srcCardType, ID), eq(src.getId()));
			} else {
				clause = and(condition(attribute(srcCardType, ID), eq(src.getId())), whereClause);
			}
		}
		return view.select(anyAttribute(DOM_ALIAS), attribute(DST_ALIAS, CODE), attribute(DST_ALIAS, DESCRIPTION)) //
				.from(srcCardType) //
				.join(anyClass(), as(DST_ALIAS), over(domain, as(DOM_ALIAS))) //
				.where(clause) //
				.orderBy(attribute(DST_ALIAS, DESCRIPTION), Direction.ASC);
	}

	protected QuerySpecsBuilder getRelationQuery(final CMClass sourceType, final CMDomain domain) {
		return view.select(anyAttribute(DOM_ALIAS), attribute(DST_ALIAS, CODE), attribute(DST_ALIAS, DESCRIPTION))
				.from(sourceType).join(anyClass(), as(DST_ALIAS), over(domain, as(DOM_ALIAS)))
				.orderBy(attribute(DST_ALIAS, DESCRIPTION), OrderByClause.Direction.ASC);
	}

	protected CMClass getCardType(final Card src) {
		final CMClass type = view.findClass(src.getClassName());
		Validate.notNull(type);
		return type;
	}

	public static class RelationInfo {

		private final QueryRelation rel;
		private final CMCard dst;

		protected RelationInfo(final QueryRelation rel, final CMCard dst) {
			this.rel = rel;
			this.dst = dst;
		}

		public String getTargetDescription() {
			return ObjectUtils.toString(dst.get(DESCRIPTION));
		}

		public String getTargetCode() {
			return ObjectUtils.toString(dst.get(CODE));
		}

		public CMCard getTargetCard() {
			return dst;
		}

		public Long getTargetId() {
			return dst.getId();
		}

		public CMClass getTargetType() {
			return dst.getType();
		}

		public Long getRelationId() {
			return rel.getRelation().getId();
		}

		public DateTime getRelationBeginDate() {
			return rel.getRelation().getBeginDate();
		}

		public DateTime getRelationEndDate() {
			return rel.getRelation().getEndDate();
		}

		public Iterable<Map.Entry<String, Object>> getRelationAttributes() {
			return rel.getRelation().getValues();
		}

		public CMRelation getRelation() {
			return rel.getRelation();
		}

		public QueryDomain getQueryDomain() {
			return rel.getQueryDomain();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((rel == null) ? 0 : rel.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final RelationInfo other = (RelationInfo) obj;
			if (rel == null) {
				if (other.rel != null) {
					return false;
				}
			} else if (!rel.getRelation().getId().equals(other.rel.getRelation().getId())) {
				return false;
			}
			return true;
		}

	}
}
