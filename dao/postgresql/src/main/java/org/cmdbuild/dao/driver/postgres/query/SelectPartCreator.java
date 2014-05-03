package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;

public class SelectPartCreator extends PartCreator {

	private static final String SELECT = "SELECT";
	private static final String DISTINCT_ON = "DISTINCT ON";
	public static final String ATTRIBUTES_SEPARATOR = ", ";
	private static final String LF = "\n";

	private final QuerySpecs querySpecs;
	private final SelectAttributesExpressions selectAttributesExpressions;

	public SelectPartCreator(final QuerySpecs querySpecs, final ColumnMapper columnMapper,
			final SelectAttributesExpressions selectAttributesExpressions) {
		this.querySpecs = querySpecs;
		this.selectAttributesExpressions = selectAttributesExpressions;

		final FromClause fromClause = querySpecs.getFromClause();
		fromClause.getType().accept(new CMEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				final Alias alias = fromClause.getAlias();
				addToSelect(alias, IdClass);
				addToSelect(alias, Id);
				addToSelect(alias, User);
				addToSelect(alias, BeginDate);
				if (fromClause.isHistory()) {
					/**
					 * aliases for join clauses are not added here (e.g. the
					 * EndDate attribute is not present in a referenced table /
					 * lookup table when there is one or more direct join)
					 */
					if (alias.toString().equals(fromClause.getType().getName())) {
						addToSelect(alias, EndDate);
					}
				}
			}

			@Override
			public void visit(final CMDomain type) {
				throw new IllegalArgumentException("domains should not be used in the from clauses");
			}

			@Override
			public void visit(final CMFunctionCall type) {
				// functions has no system attributes
			}

		});

		for (final JoinClause joinClause : querySpecs.getJoins()) {
			final Alias targetAlias = joinClause.getTargetAlias();
			addToSelect(targetAlias, IdClass);
			addToSelect(targetAlias, Id);
			addToSelect(targetAlias, User);
			addToSelect(targetAlias, BeginDate);
			if (fromClause.isHistory()) {
				/**
				 * aliases for join clauses are not added here (e.g. the EndDate
				 * attribute is not present in a referenced table / lookup table
				 * when there is one or more direct join)
				 */
				if (targetAlias.toString().equals(fromClause.getType().getName())) {
					addToSelect(targetAlias, EndDate);
				}
			}

			final Alias domainAlias = joinClause.getDomainAlias();
			addToSelect(domainAlias, DomainId);
			addToSelect(domainAlias, DomainQuerySource);
			addToSelect(domainAlias, Id);
			addToSelect(domainAlias, User);
			addToSelect(domainAlias, BeginDate);
			addToSelect(domainAlias, EndDate);
			addToSelect(domainAlias, DomainId1);
			addToSelect(domainAlias, DomainId2);
		}

		sb.append(SELECT) //
				.append(distinct()) //
				.append(LF) //
				.append(join(selectAttributesExpressions.getExpressions().iterator(), ATTRIBUTES_SEPARATOR));
	}

	private void addToSelect(final Alias typeAlias, final SystemAttributes systemAttribute) {
		selectAttributesExpressions.add( //
				typeAlias, //
				systemAttribute.getDBName(), //
				systemAttribute.getCastSuffix(), //
				as(nameForSystemAttribute(typeAlias, systemAttribute)));
	}

	private String distinct() {
		return querySpecs.distinct() ? //
		format(" %s (%s) ", //
				DISTINCT_ON, //
				AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id)))) //
				: EMPTY;
	}

}
