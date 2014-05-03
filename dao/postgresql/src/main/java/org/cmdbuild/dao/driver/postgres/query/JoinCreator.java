package org.cmdbuild.dao.driver.postgres.query;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Status;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.driver.postgres.query.SelectPartCreator.ATTRIBUTES_SEPARATOR;
import static org.cmdbuild.dao.driver.postgres.quote.SystemAttributeQuoter.quote;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;
import java.util.Map.Entry;

import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeHistoryQuoter;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.driver.postgres.quote.IdentQuoter;
import org.cmdbuild.dao.driver.postgres.quote.Quoter;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.EmptyQuerySpecs;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.collect.Lists;

public class JoinCreator extends PartCreator {

	private enum DataQueryType {

		HISTORIC {

			@Override
			Quoter quoterFor(final CMEntryType entryType) {
				return new EntryTypeHistoryQuoter(entryType);
			}

			@Override
			String quotedEndDateAttribute() {
				return quote(EndDate);
			}

		},
		CURRENT {

			@Override
			Quoter quoterFor(final CMEntryType entryType) {
				return new EntryTypeQuoter(entryType);
			}

			@Override
			String quotedEndDateAttribute() {
				return "NULL";
			}

		};

		abstract Quoter quoterFor(CMEntryType entryType);

		abstract String quotedEndDateAttribute();

	}

	private abstract class UnionCreator<T> {

		private final Iterable<T> typeSet;
		protected final Alias typeAlias;
		private final boolean includeHistoryTable;

		UnionCreator(final Iterable<T> typeSet, final Alias typeAlias, final boolean includeHistoryTable) {
			this.typeSet = typeSet;
			this.typeAlias = typeAlias;
			this.includeHistoryTable = includeHistoryTable;
		}

		public void append() {
			sb.append("(");
			boolean first = true;
			for (final T type : typeSet) {
				if (includeHistoryTable) {
					appendTableSelect(type, DataQueryType.HISTORIC, first);
					first = false;
				}
				appendTableSelect(type, DataQueryType.CURRENT, first);
				first = false;
			}
			sb.append(")");
		}

		private void appendTableSelect(final T type, final DataQueryType dataQueryType, final boolean first) {
			final CMEntryType entryType = getEntryType(type);
			final String quotedTableName = dataQueryType.quoterFor(entryType).quote();
			if (!first) {
				sb.append(" UNION ALL ");
			}
			sb.append("SELECT ");
			appendSystemAttributes(type, dataQueryType, first);
			appendUserAttributes(type, first);
			sb.append(" FROM ONLY ").append(quotedTableName);
			appendWhere(type, dataQueryType);
		}

		protected void appendWhere(final T type, final DataQueryType dataQueryType) {
			final WherePartCreator wherePartCreator = new WherePartCreator(new EmptyQuerySpecs() {

				@Override
				public FromClause getFromClause() {
					return new FromClause() {

						@Override
						public boolean isHistory() {
							return false;
						}

						@Override
						public CMEntryType getType() {
							return getEntryType(type);
						}

						@Override
						public Alias getAlias() {
							return typeAlias;
						}

						@Override
						public EntryTypeStatus getStatus(final CMEntryType entryType) {
							return new EntryTypeStatus() {

								@Override
								public boolean isActive() {
									return true;
								}

								@Override
								public boolean isAccessible() {
									return true;
								}

							};
						}

					};
				}

				@Override
				public WhereClause getWhereClause() {
					return whereClauseFor(type, dataQueryType);
				}
			}, new WherePartCreator.ActiveStatusChecker() {
				@Override
				public boolean needsActiveStatus() {
					return false;
				}
			});
			sb.append(" ").append(wherePartCreator.getPart());
			JoinCreator.this.param(wherePartCreator.getParams());
		}

		protected abstract WhereClause whereClauseFor(T type, DataQueryType dataQueryType);

		protected abstract void appendSystemAttributes(T type, final DataQueryType dataQueryType, boolean first);

		void appendUserAttributes(final T type, final boolean first) {
			final List<String> userAttributes = Lists.newArrayList();
			final CMEntryType entryType = getEntryType(type);
			for (final EntryTypeAttribute eta : columnMapper.getAttributes(typeAlias, entryType)) {
				final StringBuilder sb = new StringBuilder();
				final boolean nullValue = (eta.name == null);
				if (nullValue) {
					sb.append(Const.NULL);
				} else {
					sb.append(IdentQuoter.quote(eta.name));
				}
				if (first) {
					if (nullValue) {
						// Null values need an explicit cast
						sb.append("::").append(eta.sqlTypeString);
					}

				}

				/*
				 * should be removed completely, but it's still needed for
				 * domain attributes that can have a special alias used for
				 * avoid name conflicts
				 */
				if ((entryType instanceof CMDomain) && (eta.alias != null)) {
					sb.append(" AS ").append(AliasQuoter.quote(eta.alias));
				}

				userAttributes.add(sb.toString());
			}
			if (userAttributes.size() > 0) {
				sb.append(ATTRIBUTES_SEPARATOR);
			}
			sb.append(join(userAttributes, ATTRIBUTES_SEPARATOR));
		}

		abstract protected CMEntryType getEntryType(T type);

		protected final StringBuilder appendColumnAndAliasIfFirst(final Object attribute, final String alias,
				final boolean isFirst) {
			// TODO boolean is not checked
			sb.append(attribute).append(" AS ").append(alias);
			return sb;
		}
	}

	private final Alias fromAlias;
	private final ColumnMapper columnMapper;

	public JoinCreator(final Alias fromAlias, final Iterable<JoinClause> joinClauses, final ColumnMapper columnMapper) {
		this.fromAlias = fromAlias;
		this.columnMapper = columnMapper;
		for (final JoinClause joinClause : joinClauses) {
			appendJoinWithDomainAndTarget(joinClause);
		}
	}

	private void appendJoinWithDomainAndTarget(final JoinClause joinClause) {
		if (joinClause.hasQueryDomains() && joinClause.hasTargets()) {
			appendDomainJoin(joinClause);
			appendTargetJoin(joinClause);
		}
	}

	private void appendDomainJoin(final JoinClause joinClause) {
		if (joinClause.isLeft()) {
			sb.append("LEFT ");
		}
		sb.append("JOIN ");
		appendDomainUnion(joinClause);
		sb.append(" AS ").append(AliasQuoter.quote(joinClause.getDomainAlias())).append(" ON ")
				.append(quoteAttribute(fromAlias, Id)).append(OPERATOR_EQ)
				.append(quoteAttribute(joinClause.getDomainAlias(), DomainId1));
	}

	private void appendDomainUnion(final JoinClause joinClause) {
		final boolean includeHistoryTable = joinClause.isDomainHistory();
		new UnionCreator<QueryDomain>(joinClause.getQueryDomains(), joinClause.getDomainAlias(), includeHistoryTable) {

			@Override
			protected void appendSystemAttributes(final QueryDomain queryDomain, final DataQueryType dataQueryType,
					final boolean first) {
				final String endDateField = dataQueryType.quotedEndDateAttribute();
				sb.append(quote(Id)) //
						.append(ATTRIBUTES_SEPARATOR) //
						.append(quote(DomainId)) //
						.append(ATTRIBUTES_SEPARATOR);
				appendColumnAndAliasIfFirst(param(queryDomain.getQuerySource()), quote(DomainQuerySource), first) //
						.append(ATTRIBUTES_SEPARATOR);
				if (queryDomain.getDirection()) {
					sb.append(quote(DomainId1)) //
							.append(ATTRIBUTES_SEPARATOR) //
							.append(quote(DomainId2));
				} else {
					appendColumnAndAliasIfFirst(quote(DomainId2), quote(DomainId1), first) //
							.append(ATTRIBUTES_SEPARATOR);
					appendColumnAndAliasIfFirst(quote(DomainId1), quote(DomainId2), first);
				}
				sb.append(ATTRIBUTES_SEPARATOR) //
						.append(quote(User)) //
						.append(ATTRIBUTES_SEPARATOR) //
						.append(quote(BeginDate)) //
						.append(ATTRIBUTES_SEPARATOR);
				appendColumnAndAliasIfFirst(endDateField, quote(EndDate), first);
			}

			@Override
			protected CMEntryType getEntryType(final QueryDomain queryDomain) {
				return queryDomain.getDomain();
			}

			@Override
			protected WhereClause whereClauseFor(final QueryDomain type, final DataQueryType dataQueryType) {
				final WhereClause whereClause;
				if (dataQueryType == DataQueryType.CURRENT) {
					whereClause = condition(attribute(type.getDomain(), Status.getDBName()),
							eq(CardStatus.ACTIVE.value()));
				} else {
					whereClause = trueWhereClause();
				}
				return whereClause;
			}

		}.append();
	}

	private void appendTargetJoin(final JoinClause joinClause) {
		if (joinClause.isLeft()) {
			sb.append(" LEFT ");
		}
		sb.append(" JOIN ");
		appendClassUnion(joinClause);
		sb.append(" AS ").append(AliasQuoter.quote(joinClause.getTargetAlias())).append(" ON ")
				.append(quoteAttribute(joinClause.getDomainAlias(), DomainId2)).append(OPERATOR_EQ)
				.append(quoteAttribute(joinClause.getTargetAlias(), Id));
	}

	private void appendClassUnion(final JoinClause joinClause) {
		final boolean includeStatusCheck = !joinClause.isDomainHistory();
		final boolean includeHistoryTable = false;
		new UnionCreator<Entry<CMClass, WhereClause>>(joinClause.getTargets(), joinClause.getTargetAlias(),
				includeHistoryTable) {

			@Override
			protected void appendSystemAttributes(final Entry<CMClass, WhereClause> type,
					final DataQueryType dataQueryType, final boolean first) {
				sb.append(join(asList( //
						quote(Id), //
						quote(IdClass), //
						quote(User), //
						quote(BeginDate), //
						"NULL AS " + quote(EndDate)), //
						ATTRIBUTES_SEPARATOR));
			}

			@Override
			protected CMEntryType getEntryType(final Entry<CMClass, WhereClause> type) {
				return type.getKey();
			}

			@Override
			protected void appendWhere(final Entry<CMClass, WhereClause> targets, final DataQueryType dataQueryType) {
				super.appendWhere(targets, includeStatusCheck ? DataQueryType.CURRENT : dataQueryType);
			}

			@Override
			protected WhereClause whereClauseFor(final Entry<CMClass, WhereClause> type,
					final DataQueryType dataQueryType) {
				final CMClass clazz = type.getKey();
				final WhereClause whereClause;
				if (clazz.holdsHistory() && dataQueryType == DataQueryType.CURRENT) {
					whereClause = and( //
							type.getValue(), //
							condition(attribute(clazz, Status.getDBName()), eq(CardStatus.ACTIVE.value())));
				} else {
					whereClause = type.getValue();
				}
				return whereClause;
			}

		}.append();
	}
}
