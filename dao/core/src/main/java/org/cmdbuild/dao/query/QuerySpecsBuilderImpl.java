package org.cmdbuild.dao.query;

import static org.cmdbuild.common.Constants.LOOKUP_CLASS_NAME;
import static org.cmdbuild.dao.entrytype.EntryTypeAnalyzer.inspect;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.dao.constants.Cardinality;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.EntryTypeAnalyzer;
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.clause.ClassHistory;
import org.cmdbuild.dao.query.clause.DomainHistory;
import org.cmdbuild.dao.query.clause.NamedAttribute;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.from.ClassFromClause;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.from.FunctionFromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@NotThreadSafe
// TODO split build and run
public class QuerySpecsBuilderImpl implements QuerySpecsBuilder {

	private static class AliasLibrary {

		private final Set<Alias> aliasSet;
		private CMEntryType fromType;
		private Alias fromAlias;

		AliasLibrary() {
			aliasSet = Sets.newHashSet();
		}

		public void addAlias(final Alias alias) {
			if (aliasSet.contains(alias)) {
				throw new IllegalArgumentException("Duplicate alias");
			}
			aliasSet.add(alias);
		}

		public void setFrom(final CMEntryType type, final Alias alias) {
			this.aliasSet.remove(this.fromAlias);
			addAlias(alias);
			this.fromType = type;
			this.fromAlias = alias;
		}

		public CMEntryType getFrom() {
			return fromType;
		}

		public Alias getFromAlias() {
			return fromAlias;
		}

		public void checkAlias(final Alias alias) {
			if (!aliasSet.contains(alias)) {
				throw new NoSuchElementException("Alias " + alias + " was not found");
			}
		}

		public boolean containsAlias(final Alias alias) {
			return aliasSet.contains(alias);
		}

		public Alias getDefaultAlias() {
			if (aliasSet.size() == 1) {
				return aliasSet.iterator().next();
			} else {
				throw new IllegalStateException("Unable to determine the default alias");
			}
		}
	}

	private static final Alias DEFAULT_ANYCLASS_ALIAS = NameAlias.as("_*");

	private List<QueryAttribute> attributes;
	private final List<JoinClause> joinClauses;
	private final List<DirectJoinClause> directJoinClauses;
	private final Map<QueryAttribute, OrderByClause.Direction> orderings;
	private WhereClause whereClause;
	private Long offset;
	private Long limit;
	private boolean distinct;
	private boolean numbered;
	private WhereClause conditionOnNumberedQuery;
	private boolean count;

	private final AliasLibrary aliases;

	private final CMDataView viewForBuild;
	private final CMDataView viewForRun;

	public QuerySpecsBuilderImpl(final CMDataView view) {
		this(view, view);
	}

	/**
	 * 
	 * @param viewForBuild
	 *            is a the data view for building the query. It must be a system
	 *            view because it must know all attributes, included those for
	 *            which the logged user does not have privileges
	 * @param viewForRun
	 *            is a data view for running the query. It must be a user data
	 *            view
	 */
	public QuerySpecsBuilderImpl(final CMDataView viewForBuild, final CMDataView viewForRun) {
		this.viewForBuild = viewForBuild;
		this.viewForRun = viewForRun;
		aliases = new AliasLibrary();
		select();
		_from(anyClass(), DEFAULT_ANYCLASS_ALIAS);
		joinClauses = Lists.newArrayList();
		directJoinClauses = Lists.newArrayList();
		orderings = Maps.newLinkedHashMap();
		whereClause = EmptyWhereClause.emptyWhereClause();
		conditionOnNumberedQuery = EmptyWhereClause.emptyWhereClause();
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		attributes = Lists.newArrayList();
		for (final Object a : attrDef) {
			attributes.add(attributeFrom(a));
		}
		return this;
	}

	@Override
	public QuerySpecsBuilder distinct() {
		distinct = true;
		return this;
	}

	@Override
	public QuerySpecsBuilder _from(final CMEntryType entryType, final Alias alias) {
		aliases.setFrom(entryType, alias);
		return this;
	}

	@Override
	public QuerySpecsBuilder from(final CMEntryType fromEntryType, final Alias fromAlias) {
		aliases.setFrom(transform(fromEntryType), fromAlias);
		return this;
	}

	private void addDirectJoinClausesForLookup(final Iterable<CMAttribute> lookupAttributes, //
			final CMEntryType entryType, //
			final Alias entryTypeAlias) {
		final CMClass lookupClass = viewForBuild.findClass(LOOKUP_CLASS_NAME);
		for (final CMAttribute attribute : lookupAttributes) {
			final Alias lookupClassAlias = NameAlias.as(new ExternalReferenceAliasHandler(entryType, attribute)
					.forQuery());
			if (!aliases.containsAlias(lookupClassAlias)) {
				aliases.addAlias(lookupClassAlias);
			}
			final DirectJoinClause lookupJoinClause = DirectJoinClause.newInstance() //
					.leftJoin(lookupClass) //
					.as(lookupClassAlias) //
					.on(attribute(lookupClassAlias, "Id")) //
					.equalsTo(attribute(entryTypeAlias, attribute.getName())) //
					.build();
			directJoinClauses.add(lookupJoinClause);
		}
	}

	private void addDirectJoinClausesForReference(final Iterable<CMAttribute> referenceAttributes, //
			final CMEntryType entryType, //
			final Alias entryTypeAlias) {
		for (final CMAttribute attribute : referenceAttributes) {
			final ReferenceAttributeType attributeType = (ReferenceAttributeType) attribute.getType();
			final CMDomain domain = viewForBuild.findDomain(attributeType.getDomainName());
			final CMClass referencedClass;
			if (domain.getCardinality().equals(Cardinality.CARDINALITY_1N.value())) {
				referencedClass = viewForBuild.findClass(domain.getClass1().getName());
			} else { // CARDINALITY_N1
				referencedClass = viewForBuild.findClass(domain.getClass2().getName());
			}

			final Alias referencedClassAlias = NameAlias.as(new ExternalReferenceAliasHandler(entryType, attribute)
					.forQuery());
			if (!aliases.containsAlias(referencedClassAlias)) {
				aliases.addAlias(referencedClassAlias);
			}
			final DirectJoinClause lookupJoinClause = DirectJoinClause.newInstance() //
					.leftJoin(referencedClass) //
					.as(referencedClassAlias) //
					.on(attribute(referencedClassAlias, "Id")) //
					.equalsTo(attribute(entryTypeAlias, attribute.getName())) //
					.build();
			directJoinClauses.add(lookupJoinClause);
		}
	}

	private void addDirectJoinClausesForForeignKey(final Iterable<CMAttribute> foreignKeyAttributes, //
			final CMEntryType entryType, //
			final Alias entryTypeAlias) {

		for (final CMAttribute attribute : foreignKeyAttributes) {
			final ForeignKeyAttributeType attributeType = (ForeignKeyAttributeType) attribute.getType();
			final CMClass referencedClass = viewForBuild.findClass(attributeType.getForeignKeyDestinationClassName());
			final Alias referencedClassAlias = NameAlias.as(new ExternalReferenceAliasHandler(entryType, attribute)
					.forQuery());
			if (!aliases.containsAlias(referencedClassAlias)) {
				aliases.addAlias(referencedClassAlias);
			}
			final DirectJoinClause foreignKeyJoinClause = DirectJoinClause.newInstance() //
					.leftJoin(referencedClass) //
					.as(referencedClassAlias) //
					.on(attribute(referencedClassAlias, "Id")) //
					.equalsTo(attribute(entryTypeAlias, attribute.getName())) //
					.build();
			directJoinClauses.add(foreignKeyJoinClause);
		}
	}

	private void addSubclassesJoinClauses(final CMEntryType entryType, final Alias entryTypeAlias) {
		final Map<Alias, CMClass> descendantsByAlias = Maps.newHashMap();
		entryType.accept(new NullEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				for (final CMClass descendant : type.getDescendants()) {
					final Alias alias = EntryTypeAlias.canonicalAlias(descendant);
					if (!aliases.containsAlias(alias)) {
						aliases.addAlias(alias);
					}
					descendantsByAlias.put(alias, descendant);
				}
			}

		});
		whereClause.accept(new NullWhereClauseVisitor() {

			@Override
			public void visit(final AndWhereClause whereClause) {
				for (final WhereClause subWhereClause : whereClause.getClauses()) {
					subWhereClause.accept(this);
				}
			}

			@Override
			public void visit(final OrWhereClause whereClause) {
				for (final WhereClause subWhereClause : whereClause.getClauses()) {
					subWhereClause.accept(this);
				}
			}

			@Override
			public void visit(final SimpleWhereClause whereClause) {
				final QueryAliasAttribute attribute = whereClause.getAttribute();
				final Alias alias = attribute.getEntryTypeAlias();
				if (!aliases.containsAlias(alias)) {
					aliases.addAlias(alias);
				}
				if (descendantsByAlias.containsKey(alias)) {
					final CMClass type = descendantsByAlias.get(alias);
					final DirectJoinClause clause = DirectJoinClause.newInstance() //
							.leftJoin(type) //
							.as(alias) //
							.on(attribute(alias, "Id")) //
							.equalsTo(attribute(entryTypeAlias, "Id")) //
							.build();
					directJoinClauses.add(clause);
				}
			}

		});

	}

	@Override
	public QuerySpecsBuilder from(final CMClass cmClass) {
		return from(transform(cmClass), EntryTypeAlias.canonicalAlias(cmClass));
	}

	/*
	 * TODO: Consider more join levels (join with join tables)
	 */
	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Over overClause) {
		return join(joinClass, EntryTypeAlias.canonicalAlias(joinClass), overClause);
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		// from must be a class
		final CMClass fromClass = (CMClass) aliases.getFrom();
		final JoinClause joinClause = JoinClause.newJoinClause(viewForRun, viewForBuild, transform(fromClass))
				.withDomain(transform(overClause.getDomain()), overClause.getAlias()) //
				.withTarget(transform(joinClass), joinClassAlias) //
				.build();
		return join(joinClause, joinClassAlias, overClause);
	}

	@Override
	public QuerySpecsBuilder join(final CMClass joinClass, final Alias joinClassAlias, final Over overClause,
			final Source source) {
		// from must be a class
		final CMClass fromClass = (CMClass) aliases.getFrom();
		final JoinClause joinClause = JoinClause.newJoinClause(viewForRun, viewForBuild, transform(fromClass))
				.withDomain(new QueryDomain(transform(overClause.getDomain()), source), overClause.getAlias()) //
				.withTarget(transform(joinClass), joinClassAlias) //
				.build();
		return join(joinClause, joinClassAlias, overClause);
	}

	// TODO refactor to have a single join method
	@Override
	public QuerySpecsBuilder leftJoin(final CMClass joinClass, final Alias joinClassAlias, final Over overClause) {
		// from must be a class
		final CMClass fromClass = (CMClass) aliases.getFrom();
		final JoinClause join = JoinClause.newJoinClause(viewForRun, viewForBuild, fromClass)
				.withDomain(transform(overClause.getDomain()), overClause.getAlias()) //
				.withTarget(transform(joinClass), joinClassAlias) //
				.left() //
				.build();
		return join(join, joinClassAlias, overClause);
	}

	@Override
	public QuerySpecsBuilder leftJoin(final CMClass joinClass, final Alias joinClassAlias, final Over overClause,
			final Source source) {
		// from must be a class
		final CMClass fromClass = (CMClass) aliases.getFrom();
		final JoinClause join = JoinClause.newJoinClause(viewForRun, viewForBuild, fromClass)
				.withDomain(new QueryDomain(transform(overClause.getDomain()), source), overClause.getAlias()) //
				.withTarget(transform(joinClass), joinClassAlias) //
				.left() //
				.build();
		return join(join, joinClassAlias, overClause);
	}

	private QuerySpecsBuilder join(final JoinClause joinClause, final Alias joinClassAlias, final Over overClause) {
		joinClauses.add(joinClause);
		aliases.addAlias(joinClassAlias);
		aliases.addAlias(overClause.getAlias());
		return this;
	}

	@Override
	public QuerySpecsBuilder where(final WhereClause clause) {
		whereClause = (clause == null) ? trueWhereClause() : clause;
		return this;
	}

	@Override
	public QuerySpecsBuilder offset(final Number offset) {
		this.offset = offset.longValue();
		return this;
	}

	@Override
	public QuerySpecsBuilder limit(final Number limit) {
		this.limit = limit.longValue();
		return this;
	}

	@Override
	public QuerySpecsBuilder orderBy(final Object attribute, final Direction direction) {
		orderings.put(attributeFrom(attribute), direction);
		return this;
	}

	@Override
	public QuerySpecsBuilder numbered() {
		numbered = true;
		return this;
	}

	@Override
	public QuerySpecsBuilder numbered(final WhereClause whereClause) {
		numbered = true;
		conditionOnNumberedQuery = whereClause;
		return this;
	}

	@Override
	public QuerySpecsBuilder count() {
		count = true;
		return this;
	}

	@Override
	public QuerySpecs build() {
		final FromClause fromClause = createFromClause();

		final CMEntryType fromEntryType = fromClause.getType();
		final Alias fromAlias = fromClause.getAlias();
		final EntryTypeAnalyzer entryTypeAnalyzer = inspect(fromEntryType, viewForBuild);
		if (entryTypeAnalyzer.hasExternalReferences()) {
			addDirectJoinClausesForLookup(entryTypeAnalyzer.getLookupAttributes(), fromEntryType, fromAlias);
			addDirectJoinClausesForReference(entryTypeAnalyzer.getReferenceAttributes(), fromEntryType, fromAlias);
			addDirectJoinClausesForForeignKey(entryTypeAnalyzer.getForeignKeyAttributes(), fromEntryType, fromAlias);
		}
		addSubclassesJoinClauses(fromEntryType, fromAlias);

		final QuerySpecsImpl qs = QuerySpecsImpl.newInstance() //
				.fromClause(fromClause) //
				.distinct(distinct) //
				.numbered(numbered) //
				.conditionOnNumberedQuery(conditionOnNumberedQuery) //
				.count(count) //
				.build();

		for (final JoinClause joinClause : joinClauses) {
			if (!joinClause.hasTargets()) {
				return new EmptyQuerySpecs();
			}
			qs.addJoin(joinClause);
		}
		for (final DirectJoinClause directJoinClause : directJoinClauses) {
			qs.addDirectJoin(directJoinClause);
			final QueryAliasAttribute externalRefAttribute = attribute(directJoinClause.getTargetClassAlias(),
					ExternalReferenceAliasHandler.EXTERNAL_ATTRIBUTE);
			qs.addSelectAttribute(aliasAttributeFrom(externalRefAttribute));
		}
		for (final QueryAttribute qa : attributes) {
			qs.addSelectAttribute(aliasAttributeFrom(qa));
		}

		qs.setWhereClause(whereClause);
		qs.setOffset(offset);
		qs.setLimit(limit);
		for (final Entry<QueryAttribute, Direction> entry : orderings.entrySet()) {
			qs.addOrderByClause(new OrderByClause(aliasAttributeFrom(entry.getKey()), entry.getValue()));
		}
		return qs;
	}

	private FromClause createFromClause() {
		if (aliases.getFrom() instanceof CMFunctionCall) {
			return new FunctionFromClause(aliases.getFrom(), aliases.getFromAlias());
		} else {
			return new ClassFromClause(viewForRun, aliases.getFrom(), aliases.getFromAlias());
		}
	}

	/**
	 * Returns a {@link QueryAliasAttribute} from a {@link QueryAttribute} and
	 * checks if the alias of the {@link CMEntryType} is valid.
	 */
	private QueryAliasAttribute aliasAttributeFrom(final QueryAttribute queryAttribute) {
		QueryAliasAttribute queryAliasAttribute;
		// FIXME: Implement it with a QueryAttribute visitor
		if (queryAttribute instanceof NamedAttribute) {
			final Alias alias = aliasForNamedAttribute((NamedAttribute) queryAttribute);
			queryAliasAttribute = attribute(alias, queryAttribute.getName());
		} else if (queryAttribute instanceof QueryAliasAttribute) {
			queryAliasAttribute = (QueryAliasAttribute) queryAttribute;
		} else {
			throw new UnsupportedOperationException("Unsupported attribute class");
		}
		aliases.checkAlias(queryAliasAttribute.getEntryTypeAlias());
		return queryAliasAttribute;
	}

	private Alias aliasForNamedAttribute(final NamedAttribute na) {
		final String aliasName = na.getEntryTypeAliasName();
		if (aliasName == null) {
			return aliases.getDefaultAlias();
		} else {
			return as(aliasName);
		}
	}

	@Override
	public CMQueryResult run() {
		return viewForRun.executeQuery(build());
	}

	private QueryAttribute attributeFrom(final Object attribute) {
		QueryAttribute queryAttribute;
		if (attribute instanceof QueryAttribute) {
			queryAttribute = (QueryAttribute) attribute;
		} else if (attribute instanceof String) {
			queryAttribute = new NamedAttribute((String) attribute);
		} else {
			throw new IllegalArgumentException("invalid attribute");
		}
		return queryAttribute;
	}

	/*
	 * Object
	 */

	@Override
	public String toString() {
		return super.toString(); // TODO
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	private <T extends CMEntryType> T transform(final T entryType) {
		try {
			return new CMEntryTypeVisitor() {

				private T transformed;

				@Override
				public void visit(final CMClass type) {
					transformed = (T) viewForBuild.findClass(type.getId());
					if (type instanceof ClassHistory) {
						transformed = (T) ClassHistory.history((CMClass) transformed);
					}
				}

				@Override
				public void visit(final CMDomain type) {
					transformed = (T) viewForBuild.findDomain(type.getId());
					if (type instanceof DomainHistory) {
						transformed = (T) DomainHistory.history((CMDomain) transformed);
					}
				}

				@Override
				public void visit(final CMFunctionCall type) {
					// function does not need transformation
					transformed = entryType;
				}

				public T transform(final T entryType) {
					entryType.accept(this);
					return transformed;
				}

			}.transform(entryType);
		} catch (final Exception e) {
			return entryType;
		}
	}

}
