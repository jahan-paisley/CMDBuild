package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.base.Predicates.containsPattern;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.cmdbuild.common.utils.guava.Functions.trim;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowNumber;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowsCount;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;
import static org.cmdbuild.dao.query.clause.where.EmptyWhereClause.emptyWhereClause;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.NullOperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

public class QueryCreator {

	private static class SpecialFeaturesChecker extends NullEntryTypeVisitor {

		private boolean addDefaultOrderings;
		private boolean reduceSelectCountArguments;

		/**
		 * @return {@code true} if default orderings must be added,
		 *         {@code false} otherwise ({@code true} is default).
		 */
		public boolean addDefaultOrderings(final CMEntryType type) {
			addDefaultOrderings = true;
			type.accept(this);
			return addDefaultOrderings;
		}

		/**
		 * @return {@code true} if default attributes for count must be reduces,
		 *         {@code false} otherwise ({@code true} is default).
		 */
		public boolean reduceSelectCountArguments(final CMEntryType type) {
			reduceSelectCountArguments = true;
			type.accept(this);
			return reduceSelectCountArguments;
		}

		@Override
		public void visit(final CMFunctionCall type) {
			addDefaultOrderings = false;
			reduceSelectCountArguments = false;
		}

	}

	private static Function<String, Predicate<CharSequence>> toContainsPatternPredicate = new Function<String, Predicate<CharSequence>>() {

		@Override
		public Predicate<CharSequence> apply(final String input) {
			return containsPattern(input);
		}

	};

	private static final String ATTRIBUTES_SEPARATOR = ",";

	private static final String SPACE = " ";

	private static final String PARTS_SEPARATOR = SPACE + LINE_SEPARATOR;
	private static final String ORDER_BY = "ORDER BY";
	private static final String ORDER_BY_ATTRIBUTE_EXPRESSION = "%s %s";

	private static final SpecialFeaturesChecker specialFeaturesChecker = new SpecialFeaturesChecker();

	private final StringBuilder sb;
	private final QuerySpecs querySpecs;
	private final List<Object> params;

	private SelectAttributesExpressions selectAttributesExpressions;
	private ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs querySpecs) {
		this.sb = new StringBuilder();
		this.querySpecs = querySpecs;
		this.params = newArrayList();
		buildQuery();
	}

	private void buildQuery() {
		selectAttributesExpressions = new SelectAttributesExpressions();
		columnMapper = new ColumnMapper(querySpecs, selectAttributesExpressions);
		columnMapper.addAllAttributes(querySpecs.getAttributes());

		appendSelect();
		appendFrom();
		appendDirectJoin();
		appendJoin();
		appendWhere();
		appendNumberingAndOrder();
		appendLimitAndOffset();
		appendConditionOnNumberedQuery();
	}

	private void appendSelect() {
		appendPart(new SelectPartCreator(querySpecs, columnMapper, selectAttributesExpressions));
	}

	private void appendFrom() {
		appendPart(new FromPartCreator(querySpecs));
	}

	private void appendDirectJoin() {
		appendPart(new DirectJoinPartCreator(querySpecs));
	}

	private void appendJoin() {
		appendPart(new JoinCreator(querySpecs.getFromClause().getAlias(), querySpecs.getJoins(), columnMapper));
	}

	private void appendWhere() {
		appendPart(new WherePartCreator(querySpecs));
	}

	private void appendNumberingAndOrder() {
		final String actual = sb.toString();
		sb.setLength(0);

		final List<String> selectAttributes = newArrayList();

		// any attribute of default query
		selectAttributes.add("*");

		// count
		if (querySpecs.count()) {
			final String actualForCount;
			if (specialFeaturesChecker.reduceSelectCountArguments(querySpecs.getFromClause().getType())) {
				final Pattern pattern = Pattern.compile( //
						"SELECT[\\s]+" + // mandatory/fix
								"(DISTINCT[\\s]+ON[\\s]+\\((.+)\\))?" + // optional/group
								"([\\s\\w\".,#:-]+)" + // mandatory/group
								"FROM[\\s]+" + // mandatory/fix
								"(ONLY[\\s]+)?" + // optional/group
								"([\\S]+)" + // mandatory/group
								"([\\s]+AS[\\s]+([\\S]+))?"); // optional/groups
				final Matcher matcher = pattern.matcher(actual);
				if (matcher.find()) {
					final Iterable<String> distinctAttributes = from( //
							Splitter.on(ATTRIBUTES_SEPARATOR) //
									.split(defaultString(matcher.group(2)))) //
							.transform(trim());
					final String tableName = matcher.group(5);
					final String alias = matcher.group(7);
					final String tableOrAlias = defaultString(alias, tableName);
					final MatchResult matchResult = matcher.toMatchResult();
					final int start = matchResult.start(3);
					final int end = matchResult.end(3);
					final Iterable<String> actualAttributes = from( //
							Splitter.on(ATTRIBUTES_SEPARATOR) //
									.split(actual.substring(start, end))) //
							.transform(trim()) //
							.filter(allIfEmpty(distinctAttributes)) //
							.filter(or(from(distinctAttributes) //
									.transform(toContainsPatternPredicate)));
					final String newAttributes = isEmpty(actualAttributes) ? format(" %s.\"Id\" ", tableOrAlias)
							: Joiner.on(ATTRIBUTES_SEPARATOR) //
									.join(actualAttributes);
					actualForCount = actual.substring(0, start) + newAttributes + actual.substring(end);
				} else {
					actualForCount = actual;
				}
			} else {
				actualForCount = actual;
			}
			selectAttributes.add(format("(SELECT count(*) FROM (%s) AS main) AS %s", //
					actualForCount, //
					nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowsCount)));

			// parameters must be doubled
			params.addAll(params);
		}

		final String orderByAttributesExpression = join(expressionsForOrdering(),
				SelectPartCreator.ATTRIBUTES_SEPARATOR);

		/*
		 * row number (if possible)
		 * 
		 * uses row_number feature for ordering
		 */
		if (querySpecs.numbered() && !orderByAttributesExpression.isEmpty()) {
			selectAttributes.add(format("row_number() OVER (%s %s) AS %s", //
					ORDER_BY, //
					orderByAttributesExpression, //
					nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowNumber)));
		}

		sb.append(format("SELECT %s FROM (%s) AS main", //
				join(selectAttributes, SelectPartCreator.ATTRIBUTES_SEPARATOR), //
				actual));

		/*
		 * uses row_number feature for ordering
		 */
		if (!querySpecs.numbered() && !orderByAttributesExpression.isEmpty()) {
			sb.append(LINE_SEPARATOR).append(ORDER_BY).append(SPACE).append(orderByAttributesExpression);
		}

		if (querySpecs.numbered()) {
			appendConditionOnNumberedQuery();
		}
	}

	private Predicate<String> allIfEmpty(final Iterable<String> elements) {
		return new Predicate<String>() {

			@Override
			public boolean apply(final String input) {
				return !isEmpty(elements);
			}

		};
	}

	/**
	 * Returns a list of all ordering expression in the format where each
	 * expression has the format: <br>
	 * <br>
	 * {@code "attribute [ASC|DESC]}" <br>
	 * <br>
	 * If no attributes are specified then {@code Id} is added (if possible).
	 * 
	 * @return a list of ordering expressions (if any).
	 */
	private List<String> expressionsForOrdering() {
		final List<String> expressions = newArrayList();
		for (final OrderByClause clause : querySpecs.getOrderByClauses()) {
			final QueryAliasAttribute attribute = clause.getAttribute();
			expressions.add(format(ORDER_BY_ATTRIBUTE_EXPRESSION, //
					AliasQuoter.quote(as(nameForUserAttribute(attribute.getEntryTypeAlias(), attribute.getName()))), //
					clause.getDirection()));
		}
		if (specialFeaturesChecker.addDefaultOrderings(querySpecs.getFromClause().getType())) {
			expressions.add(AliasQuoter.quote(as(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), Id))));
		}

		return expressions;
	}

	private void appendConditionOnNumberedQuery() {
		final WhereClause whereClause = (querySpecs.getConditionOnNumberedQuery() == null) ? emptyWhereClause()
				: querySpecs.getConditionOnNumberedQuery();
		whereClause.accept(new NullWhereClauseVisitor() {
			@Override
			public void visit(final SimpleWhereClause whereClause) {
				whereClause.getOperator().accept(new NullOperatorAndValueVisitor() {
					@Override
					public void visit(final EqualsOperatorAndValue operatorAndValue) {
						final QueryAliasAttribute attribute = whereClause.getAttribute();
						final String quotedName = AliasQuoter.quote(as(nameForSystemAttribute(
								attribute.getEntryTypeAlias(), Id)));
						final String actual = sb.toString();
						sb.setLength(0);
						sb.append(format("SELECT * FROM (%s) AS numbered WHERE %s = %s", //
								actual, //
								quotedName, //
								operatorAndValue.getValue()));
					}
				});
			}
		});
	}

	private void appendLimitAndOffset() {
		appendPart(new LimitPartCreator(querySpecs));
		appendPart(new OffsetPartCreator(querySpecs));
	}

	private void appendPart(final PartCreator partCreator) {
		final String part = partCreator.getPart();
		if (isNotEmpty(part)) {
			sb.append(PARTS_SEPARATOR).append(part);
			params.addAll(partCreator.getParams());
		}
	}

	public String getQuery() {
		return sb.toString();
	}

	public Object[] getParams() {
		return params.toArray();
	}

	public ColumnMapper getColumnMapper() {
		return columnMapper;
	}

}
