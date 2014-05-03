package org.cmdbuild.logic.data.access;

import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_DIRECTION;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ANY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.cql.facade.CQLAnalyzer.Callback;
import org.cmdbuild.cql.facade.CQLFacade;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.ExternalReferenceAliasHandler;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonAttributeFilterBuilder;
import org.cmdbuild.logic.mapping.json.JsonFullTextQueryBuilder;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.cmdbuild.logic.validation.json.JsonFilterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class QuerySpecsBuilderFiller {

	private static final String DEFAULT_SORTING_ATTRIBUTE_NAME = "Description";

	private final CMDataView dataView;
	private final QueryOptions queryOptions;

	private CMClass sourceClass;

	public QuerySpecsBuilderFiller(final CMDataView dataView, final QueryOptions queryOptions, final String className) {
		this.dataView = dataView;
		this.queryOptions = queryOptions;
		this.sourceClass = dataView.findClass(className);
	}

	public CMClass getSourceClass() {
		return sourceClass;
	}

	public QuerySpecsBuilder create() {
		final Mapper<JSONArray, List<QueryAliasAttribute>> attributeSubsetMapper = new JsonAttributeSubsetMapper(
				sourceClass);
		final List<QueryAliasAttribute> attributeSubsetForSelect = attributeSubsetMapper.map(queryOptions
				.getAttributes());
		final QuerySpecsBuilder querySpecsBuilder = newQuerySpecsBuilder(attributeSubsetForSelect, sourceClass) //
				.from(sourceClass);
		try {
			fillQuerySpecsBuilderWithFilterOptions(querySpecsBuilder);
		} catch (final JSONException ex) {
			Log.CMDBUILD.error("Bad filter. The filter is {} ", queryOptions.getFilter().toString());
		}
		querySpecsBuilder //
				.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());
		addSortingOptions(querySpecsBuilder, sourceClass);
		return querySpecsBuilder;
	}

	private QuerySpecsBuilder newQuerySpecsBuilder(final List<QueryAliasAttribute> attributeSubsetForSelect,
			final CMEntryType entryType) {
		if (attributeSubsetForSelect.isEmpty()) {
			return dataView.select(anyAttribute(entryType));
		}
		final Object[] attributesArray = new QueryAliasAttribute[attributeSubsetForSelect.size()];
		attributeSubsetForSelect.toArray(attributesArray);
		return dataView.select(attributesArray);
	}

	private void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder, final CMClass sourceClass) {
		final SorterMapper sorterMapper = new JsonSorterMapper(sourceClass, queryOptions.getSorters());
		final List<OrderByClause> clauses = sorterMapper.deserialize();
		Validate.notNull(sourceClass, "null source class");
		if (clauses.isEmpty()) {
			if (sourceClass.getAttribute(DEFAULT_SORTING_ATTRIBUTE_NAME) != null) {
				querySpecsBuilder.orderBy(attribute(sourceClass, DEFAULT_SORTING_ATTRIBUTE_NAME), Direction.ASC);
			}
		} else {
			for (final OrderByClause clause : clauses) {
				final Object attributeAlias = getAttributeAliasFromOrderClause( //
						sourceClass, //
						clause //
				);
				querySpecsBuilder.orderBy(attributeAlias, clause.getDirection());
			}
		}
	}

	/**
	 * Sorting by lookup, reference and foreign key attributes must add column
	 * the description of the relative card/lookup
	 * 
	 * @param sourceClass
	 * @param clause
	 * @return the alias to use to sort
	 */
	private Object getAttributeAliasFromOrderClause( //
			final CMClass sourceClass, //
			final OrderByClause clause //
	) {

		final QueryAliasAttribute queryAttribute = clause.getAttribute();
		final String attributeName = queryAttribute.getName();
		final String entryTypeAlias = queryAttribute.getEntryTypeAlias().toString();
		final CMAttribute cmAttribute = sourceClass.getAttribute(attributeName);
		final CMAttributeType<?> cmAttributeType = cmAttribute.getType();

		Object attributeAlias = clause.getAttribute();
		String pattern = null;
		if (cmAttributeType instanceof LookupAttributeType) {
			pattern = new ExternalReferenceAliasHandler(entryTypeAlias, cmAttribute).forQuery();
		} else if (cmAttributeType instanceof ReferenceAttributeType) {
			final String referencedClassName = getReferencedClassName(cmAttributeType);
			/*
			 * if no referenced class name is found return only the attribute
			 * name
			 */
			if (!"".equals(referencedClassName)) {
				pattern = new ExternalReferenceAliasHandler(entryTypeAlias, cmAttribute).forQuery();
			}
		} else if (cmAttributeType instanceof ForeignKeyAttributeType) {
			pattern = new ExternalReferenceAliasHandler(entryTypeAlias, cmAttribute).forQuery();
		}

		if (pattern != null) {
			attributeAlias = QueryAliasAttribute.attribute( //
					NameAlias.as(pattern), //
					ExternalReferenceAliasHandler.EXTERNAL_ATTRIBUTE);
		}

		return attributeAlias;
	}

	private String getReferencedClassName(final CMAttributeType<?> cmAttributeType) {
		String referencedClassName = "";
		final String domainName = ((ReferenceAttributeType) cmAttributeType).getDomainName();
		final CMDomain domain = dataView.findDomain(domainName);
		if (CARDINALITY_1N.value().equals(domain.getCardinality())) {
			referencedClassName = domain.getClass1().getName();
		} else if (CARDINALITY_N1.value().equals(domain.getCardinality())) {
			referencedClassName = domain.getClass2().getName();
		}

		return referencedClassName;
	}

	/**
	 * TODO: split into different private methods
	 */
	private void fillQuerySpecsBuilderWithFilterOptions(final QuerySpecsBuilder querySpecsBuilder) throws JSONException {
		final List<WhereClause> whereClauses = Lists.newArrayList();
		final JSONObject filterObject = queryOptions.getFilter();
		new JsonFilterValidator(queryOptions.getFilter()).validate();

		// CQL filter
		if (filterObject.has(CQL_KEY)) {
			Log.CMDBUILD.info("Filter is a CQL filter");
			final String cql = filterObject.getString(CQL_KEY);
			final Map<String, Object> context = queryOptions.getParameters();
			CQLFacade.compileAndAnalyze(cql, context, new Callback() {

				@Override
				public void from(final CMClass source) {
					sourceClass = source;
					querySpecsBuilder.select(anyAttribute(source)) //
							.from(source);
				}

				@Override
				public void distinct() {
					querySpecsBuilder.distinct();
				}

				@Override
				public void leftJoin(final CMClass target, final Alias alias, final Over over) {
					querySpecsBuilder.leftJoin(target, alias, over);
				}

				@Override
				public void join(final CMClass target, final Alias alias, final Over over) {
					querySpecsBuilder.join(target, alias, over);
				}

				@Override
				public void where(final WhereClause clause) {
					whereClauses.add(clause);
					querySpecsBuilder.where(clause);
				}

			});
		}

		// full text query on attributes of the source class
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			final JsonFullTextQueryBuilder jsonFullTextQueryBuilder = new JsonFullTextQueryBuilder(
					filterObject.getString(FULL_TEXT_QUERY_KEY), sourceClass);
			whereClauses.add(jsonFullTextQueryBuilder.build());
		}

		if (filterObject.has(CQL_KEY)) {
			querySpecsBuilder.where(isEmpty(whereClauses) ? trueWhereClause() : and(whereClauses));
			return;
		}

		// filter on attributes of the source class
		if (filterObject.has(ATTRIBUTE_KEY)) {
			final JsonAttributeFilterBuilder attributeFilterBuilder = new JsonAttributeFilterBuilder(
					filterObject.getJSONObject(ATTRIBUTE_KEY), sourceClass, dataView);
			whereClauses.add(attributeFilterBuilder.build());
		}

		// filter on relations
		if (filterObject.has(RELATION_KEY)) {
			querySpecsBuilder.distinct();
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {

				final JSONObject condition = conditions.getJSONObject(i);
				final String domainName = condition.getString(RELATION_DOMAIN_KEY);
				final String sourceString = condition.getString(RELATION_DOMAIN_DIRECTION);
				final CMDomain domain = dataView.findDomain(domainName);
				final String destinationName = condition.getString(RELATION_DESTINATION_KEY);
				final CMClass destinationClass = dataView.findClass(destinationName);
				final boolean left = condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE);
				final Alias destinationAlias = NameAlias.as(String.format("DST-%s-%s", destinationName,
						randomNumeric(10)));
				final Alias domainAlias = NameAlias.as(String.format("DOM-%s-%s", domainName, randomNumeric(10)));

				if (left) {
					querySpecsBuilder.leftJoin(destinationClass, destinationAlias, over(domain, domainAlias),
							getSourceFrom(sourceString));
				} else {
					querySpecsBuilder.join(destinationClass, destinationAlias, over(domain, domainAlias),
							getSourceFrom(sourceString));
				}

				final String conditionType = condition.getString(RELATION_TYPE_KEY);

				if (conditionType.equals(RELATION_TYPE_ONEOF)) {
					final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);
					final List<Long> oneOfIds = Lists.newArrayList();

					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						oneOfIds.add(card.getLong(RELATION_CARD_ID_KEY));
					}
					whereClauses.add( //
							condition( //
									attribute(destinationAlias, Id.getDBName()), //
									in(oneOfIds.toArray())));

				} else if (conditionType.equals(RELATION_TYPE_NOONE)) {
					whereClauses.add( //
							condition( //
									attribute(destinationAlias, Id.getDBName()), //
									isNull()));
				} else if (conditionType.equals(RELATION_TYPE_ANY)) {
					/**
					 * Should be empty. WhereClauses not added because I can
					 * detect if a card is in relation with ANY card, using only
					 * the JOIN clause
					 */
				}

			}
		}
		if (!whereClauses.isEmpty()) {
			querySpecsBuilder.where(and(whereClauses));
		} else {
			querySpecsBuilder.where(trueWhereClause());
		}
	}

	private Source getSourceFrom(final String source) {
		return Source._1.name().equals(source) ? Source._1 : Source._2;
	}

}
