package org.cmdbuild.logic.data.access;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.alias.EntryTypeAlias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.HashMap;
import java.util.List;

import org.cmdbuild.common.Builder;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.FunctionCall;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;

import com.google.common.collect.Lists;

public class DataViewCardFetcher {

	/**
	 * @deprecated use QuerySpecsBuilder instead
	 */
	@Deprecated
	private static abstract class AbstractQuerySpecsBuilderBuilder implements Builder<QuerySpecsBuilder> {

		protected CMDataView dataView;
		protected CMDataView systemDataView;
		protected QueryOptions queryOptions;

		public AbstractQuerySpecsBuilderBuilder withDataView(final CMDataView value) {
			dataView = value;
			return this;
		}

		public AbstractQuerySpecsBuilderBuilder withSystemDataView(final CMDataView value) {
			systemDataView = value;
			return this;
		}

		public AbstractQuerySpecsBuilderBuilder withQueryOptions(final QueryOptions value) {
			queryOptions = value;
			return this;
		}

		protected void addJoinOptions(final QuerySpecsBuilder querySpecsBuilder, final QueryOptions options,
				final Iterable<FilterMapper.JoinElement> joinElements) {
			if (!isEmpty(joinElements)) {
				querySpecsBuilder.distinct();
			}
			for (final FilterMapper.JoinElement joinElement : joinElements) {
				final CMDomain domain = dataView.findDomain(joinElement.domain);
				final CMClass clazz = dataView.findClass(joinElement.destination);
				if (joinElement.left) {
					querySpecsBuilder.leftJoin(clazz, canonicalAlias(clazz), over(domain));
				} else {
					querySpecsBuilder.join(clazz, canonicalAlias(clazz), over(domain));
				}
			}
		}

		protected static void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder,
				final Iterable<OrderByClause> clauses) {
			for (final OrderByClause clause : clauses) {
				querySpecsBuilder.orderBy(clause.getAttribute(), clause.getDirection());
			}
		}

	}

	/**
	 * @deprecated use QuerySpecsBuilderFiller instead
	 */
	@Deprecated
	public static class SqlQuerySpecsBuilderBuilder extends AbstractQuerySpecsBuilderBuilder {

		private CMFunction fetchedFunction;
		private Alias functionAlias;

		@Override
		public QuerySpecsBuilder build() {
			final FunctionCall functionCall = FunctionCall.call(fetchedFunction, new HashMap<String, Object>());
			final FilterMapper filterMapper = JsonFilterMapper.newInstance() //
					.withDataView(dataView) //
					.withDataView(systemDataView) //
					.withEntryType(functionCall) //
					.withEntryTypeAlias(functionAlias) //
					.withFilterObject(queryOptions.getFilter()) //
					.build();
			final Iterable<WhereClause> whereClauses = filterMapper.whereClauses();
			final WhereClause whereClause = isEmpty(whereClauses) ? trueWhereClause() : and(whereClauses);
			final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();
			final QuerySpecsBuilder querySpecsBuilder = dataView //
					.select(anyAttribute(fetchedFunction, functionAlias)) //
					.from(functionCall, functionAlias) //
					.where(whereClause) //
					.limit(queryOptions.getLimit()) //
					.offset(queryOptions.getOffset());
			addJoinOptions(querySpecsBuilder, queryOptions, joinElements);
			addSortingOptions(querySpecsBuilder, queryOptions, functionCall, functionAlias);
			return querySpecsBuilder;
		}

		private void addSortingOptions( //
				final QuerySpecsBuilder querySpecsBuilder, //
				final QueryOptions options, //
				final FunctionCall functionCall, //
				final Alias alias) { //

			final SorterMapper sorterMapper = new JsonSorterMapper(functionCall, options.getSorters(), alias);
			final List<OrderByClause> clauses = sorterMapper.deserialize();

			addSortingOptions(querySpecsBuilder, clauses);
		}

		@Override
		public SqlQuerySpecsBuilderBuilder withDataView(final CMDataView value) {
			return (SqlQuerySpecsBuilderBuilder) super.withDataView(value);
		}

		@Override
		public SqlQuerySpecsBuilderBuilder withSystemDataView(final CMDataView value) {
			return (SqlQuerySpecsBuilderBuilder) super.withSystemDataView(value);
		}

		@Override
		public SqlQuerySpecsBuilderBuilder withQueryOptions(final QueryOptions value) {
			return (SqlQuerySpecsBuilderBuilder) super.withQueryOptions(value);
		}

		public SqlQuerySpecsBuilderBuilder withFunction(final CMFunction value) {
			fetchedFunction = value;
			return this;
		}

		public SqlQuerySpecsBuilderBuilder withAlias(final Alias value) {
			functionAlias = value;
			return this;
		}

	}

	public static class DataViewCardFetcherBuilder implements Builder<DataViewCardFetcher> {

		private CMDataView dataView;
		private String className;
		private QueryOptions queryOptions;

		public DataViewCardFetcherBuilder withDataView(final CMDataView value) {
			dataView = value;
			return this;
		}

		public DataViewCardFetcherBuilder withClassName(final String value) {
			className = value;
			return this;
		}

		public DataViewCardFetcherBuilder withQueryOptions(final QueryOptions value) {
			queryOptions = value;
			return this;
		}

		@Override
		public DataViewCardFetcher build() {
			return new DataViewCardFetcher(this);
		}

	}

	public static DataViewCardFetcherBuilder newInstance() {
		return new DataViewCardFetcherBuilder();
	}

	private final CMDataView dataView;
	private final String className;
	private final QueryOptions queryOptions;
	private final QuerySpecsBuilderFiller querySpecsBuilderFiller;

	private DataViewCardFetcher(final DataViewCardFetcherBuilder builder) {
		this.dataView = builder.dataView;
		this.className = builder.className;
		this.queryOptions = builder.queryOptions;
		querySpecsBuilderFiller = new QuerySpecsBuilderFiller(dataView, queryOptions, className);
	}

	public PagedElements<CMCard> fetch() {
		final CMQueryResult result = querySpecsBuilderFiller.create() //
				.count() //
				.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		final CMClass sourceClass = querySpecsBuilderFiller.getSourceClass();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(sourceClass);
			filteredCards.add(card);
		}
		return new PagedElements<CMCard>(filteredCards, result.totalSize());
	}

	public PagedElements<CMQueryRow> fetchNumbered(final WhereClause conditionOnNumberedQuery) {
		final QuerySpecsBuilder querySpecsBuilder = querySpecsBuilderFiller.create();
		querySpecsBuilder.numbered(conditionOnNumberedQuery);
		final CMQueryResult result = querySpecsBuilder.run();
		return new PagedElements<CMQueryRow>(result, result.size());
	}

}
