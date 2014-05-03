package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.AnyAttribute;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Holds the information about which attribute to query for every alias and
 * entry type of that alias. Also it is used to keep a mapping between the alias
 * attributes and the position in the select clause.
 */
public class ColumnMapper implements LoggingSupport {

	public static class EntryTypeAttribute {

		public final String name;
		public final Alias alias;
		public final Integer index;
		public final SqlType sqlType;
		public final String sqlTypeString;

		private transient final String toString;

		/*
		 * Usable within this class only!
		 */
		private EntryTypeAttribute( //
				final String name, //
				final Alias alias, //
				final Integer index, //
				final SqlType sqlType, //
				final String sqlTypeString //
		) {
			this.name = name;
			this.alias = alias;
			this.index = index;
			this.sqlType = sqlType;
			this.sqlTypeString = sqlTypeString;

			this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		@Override
		public String toString() {
			return toString;
		}
	}

	/**
	 * Stores all {@link EntryTypeAttribute} by {@link CMEntryType}.
	 */
	private static class AliasAttributes {

		private final Map<CMEntryType, List<EntryTypeAttribute>> map;

		public AliasAttributes(final Iterable<? extends CMEntryType> entryTypes) {
			map = newHashMap();
			for (final CMEntryType entryType : entryTypes) {
				map.put(entryType, Lists.<EntryTypeAttribute> newArrayList());
			}
		}

		/*
		 * Adds the attribute to the specified type
		 */
		public void addAttribute( //
				final String attributeName, //
				final Alias attributeAlias, //
				final Integer index, //
				final CMEntryType type //
		) {
			new CMEntryTypeVisitor() {

				private final String sqlTypeString = sqlTypeString(type, attributeName);
				private final SqlType sqlType = sqlType(type, attributeName);

				@Override
				public void visit(final CMClass type) {
					final List<CMEntryType> entryTypes = Lists.newArrayList();
					entryTypes.add(type);
					for (final CMClass descendant : type.getDescendants()) {
						entryTypes.add(descendant);
					}
					add(entryTypes);
				}

				@Override
				public void visit(final CMDomain type) {
					addWithMissingAttributesAlso(Arrays.asList(type));
				}

				@Override
				public void visit(final CMFunctionCall type) {
					add(Arrays.asList(type));
				}

				private void add(final Iterable<? extends CMEntryType> types) {
					for (final CMEntryType type : types) {
						final EntryTypeAttribute eta = new EntryTypeAttribute(attributeName, attributeAlias, index,
								sqlType, sqlTypeString);
						if (map.containsKey(type)) {
							map.get(type).add(eta);
						}
					}
				}

				private void addWithMissingAttributesAlso(final Iterable<? extends CMEntryType> types) {
					for (final CMEntryType type : types) {
						for (final CMEntryType currentType : map.keySet()) {
							final String currentName = (attributeAlias == null || currentType.equals(type)) ? attributeName
									: null;
							final EntryTypeAttribute eta = new EntryTypeAttribute(currentName, attributeAlias, index,
									sqlType, sqlTypeString);
							map.get(currentType).add(eta);
						}
					}
				}

				public void addFor(final CMEntryType type) {
					type.accept(this);
				}

				private SqlType sqlType(final CMEntryType type, final String attributeName) {
					final CMAttributeType<?> attributeType = safeAttributeTypeFor(type, attributeName);
					return SqlType.getSqlType(attributeType);
				}

				private String sqlTypeString(final CMEntryType type, final String attributeName) {
					final CMAttributeType<?> attributeType = safeAttributeTypeFor(type, attributeName);
					return SqlType.getSqlTypeString(attributeType);
				}

				private CMAttributeType<?> safeAttributeTypeFor(final CMEntryType type, final String attributeName) {
					final CMAttributeType<?> attributeType;
					if (type != null) {
						final CMAttribute attribute = type.getAttribute(attributeName);
						attributeType = (attribute != null) ? attribute.getType() : UndefinedAttributeType.undefined();
					} else {
						attributeType = UndefinedAttributeType.undefined();
					}
					return attributeType;
				}

			}.addFor(type);
		}

		public Iterable<EntryTypeAttribute> getAttributes(final CMEntryType type) {
			final Iterable<EntryTypeAttribute> entryTypeAttributes = map.get(type);
			sqlLogger.trace("getting all attributes for type '{}': {}", //
					type.getName(), Iterables.toString(entryTypeAttributes));
			return entryTypeAttributes;
		}

		public Iterable<CMEntryType> getEntryTypes() {
			return map.keySet();
		}

		@Override
		public String toString() {
			return map.toString();
		}

	}

	/**
	 * Stores {@link AliasAttributes} by {@link Alias}.
	 */
	private static class AliasStore {

		private final Map<Alias, AliasAttributes> map;

		public AliasStore() {
			map = newHashMap();
		}

		public void addAlias(final Alias alias, final Iterable<? extends CMEntryType> entryTypes) {
			map.put(alias, new AliasAttributes(entryTypes));
		}

		public AliasAttributes getAliasAttributes(final Alias alias) {
			return map.get(alias);
		}

		public Set<Alias> getAliases() {
			return map.keySet();
		}

		@Override
		public String toString() {
			return map.toString();
		}

	}

	private static final Function<QueryDomain, CMEntryType> TO_DOMAIN = new Function<QueryDomain, CMEntryType>() {

		@Override
		public CMEntryType apply(final QueryDomain input) {
			return input.getDomain();
		}

	};

	private static final Function<Entry<CMClass, WhereClause>, CMClass> TO_CLASS = new Function<Entry<CMClass, WhereClause>, CMClass>() {
		@Override
		public CMClass apply(final Entry<CMClass, WhereClause> input) {
			return input.getKey();
		}
	};

	private final AliasStore cardSourceAliases = new AliasStore();
	private final AliasStore functionCallAliases = new AliasStore();
	private final AliasStore domainAliases = new AliasStore();
	private final List<String> externalReferenceAliases = Lists.newArrayList();

	private final SelectAttributesHolder selectAttributesHolder;

	private Integer currentIndex;

	public ColumnMapper(final QuerySpecs query, final SelectAttributesHolder holder) {
		this.selectAttributesHolder = holder;
		this.currentIndex = 0;
		fillAliases(query);
	}

	private void fillAliases(final QuerySpecs querySpecs) {
		sqlLogger.trace("filling aliases");
		querySpecs.getFromClause().getType().accept(new CMEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				final List<CMClass> classes = Lists.newArrayList(type.getDescendants());
				classes.add(type);

				addClasses(querySpecs.getFromClause().getAlias(), classes);
				for (final JoinClause joinClause : querySpecs.getJoins()) {
					addDomainAlias(joinClause.getDomainAlias(), joinClause.getQueryDomains());
					addClasses(joinClause.getTargetAlias(), from(joinClause.getTargets()).transform(TO_CLASS));
				}
				for (final DirectJoinClause directJoinClause : querySpecs.getDirectJoins()) {
					final List<CMClass> classesToJoin = Lists.newArrayList();
					if (directJoinClause.getTargetClass() != null) {
						classesToJoin.add(directJoinClause.getTargetClass());
					}
					addClasses(directJoinClause.getTargetClassAlias(), classesToJoin);
					externalReferenceAliases.add(directJoinClause.getTargetClassAlias().toString());
				}
			}

			private void addClasses(final Alias alias, final Iterable<? extends CMClass> classes) {
				add(cardSourceAliases, alias, classes);
			}

			private void addDomainAlias(final Alias alias, final Iterable<QueryDomain> queryDomains) {
				add(domainAliases, alias, newHashSet(transform(queryDomains, TO_DOMAIN)));
			}

			@Override
			public void visit(final CMDomain type) {
				throw new IllegalArgumentException("domain is an illegal 'from' type");
			}

			@Override
			public void visit(final CMFunctionCall type) {
				add(functionCallAliases, querySpecs.getFromClause().getAlias(), newArrayList(type));
			}

			private void add(final AliasStore store, final Alias alias, final Iterable<? extends CMEntryType> entryTypes) {
				sqlLogger.trace("adding '{}' for alias '{}'", namesOfEntryTypes(entryTypes), alias);
				store.addAlias(alias, entryTypes);
			}

		});
	}

	public List<String> getExternalReferenceAliases() {
		return externalReferenceAliases;
	}

	public Iterable<Alias> getClassAliases() {
		return cardSourceAliases.getAliases();
	}

	public Iterable<Alias> getDomainAliases() {
		return domainAliases.getAliases();
	}

	public Iterable<Alias> getFunctionCallAliases() {
		return functionCallAliases.getAliases();
	}

	public Iterable<EntryTypeAttribute> getAttributes(final Alias alias, final CMEntryType type) {
		return aliasAttributesFor(alias).getAttributes(type);
	}

	public void addAllAttributes(final Iterable<? extends QueryAliasAttribute> attributes) {
		for (final QueryAliasAttribute a : attributes) {
			addAttribute(a);
		}
	}

	private void addAttribute(final QueryAliasAttribute queryAttribute) {
		sqlLogger.trace("adding attribute '{}' to alias '{}'", queryAttribute.getName(),
				queryAttribute.getEntryTypeAlias());

		final Alias attributeEntryTypeAlias = queryAttribute.getEntryTypeAlias();
		final AliasAttributes aliasAttributes = aliasAttributesFor(attributeEntryTypeAlias);
		if (queryAttribute instanceof AnyAttribute) {
			sqlLogger.trace("any attribute required");
			final Iterable<CMEntryType> entryTypes = entryTypesOf(aliasAttributes);
			final CMEntryType rootEntryType = rootOf(entryTypes);
			for (final CMEntryType entryType : entryTypes) {
				sqlLogger.trace("adding attributes for type '{}'", entryType.getIdentifier().getLocalName());
				final Alias entryTypeAlias = new CMEntryTypeVisitor() {

					private Alias alias;

					@Override
					public void visit(final CMClass type) {
						alias = attributeEntryTypeAlias;
					}

					@Override
					public void visit(final CMDomain type) {
						alias = attributeEntryTypeAlias;
					}

					@Override
					public void visit(final CMFunctionCall type) {
						alias = EntryTypeAlias.canonicalAlias(type);
					}

					public Alias typeAlias() {
						entryType.accept(this);
						return alias;
					}

				}.typeAlias();

				for (final CMAttribute attribute : entryType.getAllAttributes()) {
					sqlLogger.trace("adding attribute '{}'", attribute.getName());

					if (attribute.isInherited()) {
						if (!entryType.getIdentifier().equals(rootEntryType.getIdentifier())) {
							continue;
						}
					}

					final String attributeName = attribute.getName();

					new CMEntryTypeVisitor() {

						private Alias alias;

						@Override
						public void visit(final CMClass type) {
							alias = as(nameForUserAttribute(entryTypeAlias, attributeName));
							selectAttributesHolder.add(entryTypeAlias, attributeName, sqlCastFor(attribute), alias);
						}

						@Override
						public void visit(final CMDomain type) {
							/**
							 * The alias is updated. Bug fix for domains that
							 * have an attribute with the same name
							 */
							alias = as(nameForUserAttribute(entryTypeAlias, attributeName + "##" + currentIndex));
							selectAttributesHolder.add(sqlCastFor(attribute), alias);
						}

						@Override
						public void visit(final CMFunctionCall type) {
							alias = as(nameForUserAttribute(entryTypeAlias, attributeName));
							selectAttributesHolder.add(entryTypeAlias, attributeName, sqlCastFor(attribute), alias);
						}

						public void execute(final CMEntryType entryType) {
							entryType.accept(this);
							aliasAttributes.addAttribute(attributeName, alias, ++currentIndex, entryType);
						}

					}.execute(entryType);

				}
			}
		} else {
			final String attributeName = queryAttribute.getName();
			final int index = ++currentIndex;
			for (final CMEntryType entryType : aliasAttributes.getEntryTypes()) {
				aliasAttributes.addAttribute(attributeName, null, index, entryType);
			}
			final CMEntryType type = rootOf(aliasAttributes.getEntryTypes());
			final Alias attributeAlias = as(nameForUserAttribute(attributeEntryTypeAlias, attributeName));
			selectAttributesHolder.add(attributeEntryTypeAlias, attributeName,
					sqlCastFor(type.getAttribute(attributeName)), attributeAlias);
		}
	}

	private Iterable<CMEntryType> entryTypesOf(final AliasAttributes aliasAttributes) {
		final Iterable<CMEntryType> entryTypes = aliasAttributes.getEntryTypes();
		return new CMEntryTypeVisitor() {

			private Iterable<CMEntryType> resultEntryTypes;

			public Iterable<CMEntryType> entryTypes() {
				assert entryTypes.iterator().hasNext() : "at least one element expected";
				entryTypes.iterator().next().accept(this);
				return resultEntryTypes;
			}

			@Override
			public void visit(final CMClass type) {
				resultEntryTypes = Arrays.asList(rootOf(entryTypes));
			}

			@Override
			public void visit(final CMDomain type) {
				resultEntryTypes = entryTypes;
			}

			@Override
			public void visit(final CMFunctionCall type) {
				// should be one only
				resultEntryTypes = entryTypes;
			}

		}.entryTypes();
	}

	private CMEntryType rootOf(final Iterable<CMEntryType> entryTypes) {
		CMEntryType root = null;
		for (final CMEntryType entryType : entryTypes) {
			root = new CMEntryTypeVisitor() {

				private CMEntryType anchestor;

				@Override
				public void visit(final CMClass type) {
					if (type.isAncestorOf(CMClass.class.cast(anchestor))) {
						anchestor = type;
					}
				}

				@Override
				public void visit(final CMDomain type) {
					/*
					 * domain hierarchies are not supported, we are just
					 * returning the first one
					 */
				}

				@Override
				public void visit(final CMFunctionCall type) {
					throw new IllegalArgumentException("function hierarchies are not supported");
				}

				public CMEntryType anchestorOf(final CMEntryType entryType1, final CMEntryType entryType2) {
					if (entryType1 == null) {
						anchestor = entryType2;
					} else if (entryType2 == null) {
						anchestor = entryType1;
					} else {
						anchestor = entryType1;
						entryType2.accept(this);
					}
					return anchestor;
				}

			}.anchestorOf(root, entryType);
		}
		return root;
	}

	private String sqlCastFor(final CMAttribute attribute) {
		return SqlType.getSqlType(attribute.getType()).sqlCast();
	}

	private AliasAttributes aliasAttributesFor(final Alias alias) {
		sqlLogger.trace("getting '{}' for alias '{}'...", AliasAttributes.class, alias);
		AliasAttributes out;
		sqlLogger.trace("... is a class!");
		out = cardSourceAliases.getAliasAttributes(alias);
		if (out == null) {
			sqlLogger.trace("... no is a domain!");
			out = domainAliases.getAliasAttributes(alias);
		}
		if (out == null) {
			sqlLogger.trace("... no is a function!");
			out = functionCallAliases.getAliasAttributes(alias);
		}
		return out;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("Classes", cardSourceAliases) //
				.append("Domains", domainAliases) //
				.append("Functions", functionCallAliases) //
				.toString();
	}

	private static Iterable<String> namesOfEntryTypes(final Iterable<? extends CMEntryType> aliasClasses) {
		return transform(aliasClasses, new Function<CMEntryType, String>() {

			@Override
			public String apply(final CMEntryType input) {
				return input.getName();
			}

		});
	}

}
