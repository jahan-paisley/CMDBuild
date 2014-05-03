package org.cmdbuild.cql.facade;

import static com.google.common.collect.Iterables.isEmpty;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.EntryTypeAlias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;
import org.cmdbuild.cql.CQLBuilderListener.FieldValueType;
import org.cmdbuild.cql.compiler.impl.DomainDeclarationImpl;
import org.cmdbuild.cql.compiler.impl.DomainObjectsReferenceImpl;
import org.cmdbuild.cql.compiler.impl.FieldImpl;
import org.cmdbuild.cql.compiler.impl.FieldSelectImpl;
import org.cmdbuild.cql.compiler.impl.GroupImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.compiler.impl.SelectImpl;
import org.cmdbuild.cql.compiler.impl.WhereImpl;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.FieldSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;
import org.cmdbuild.cql.compiler.select.SelectItem;
import org.cmdbuild.cql.compiler.where.DomainObjectsReference;
import org.cmdbuild.cql.compiler.where.Field.FieldValue;
import org.cmdbuild.cql.compiler.where.Group;
import org.cmdbuild.cql.compiler.where.WhereElement;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId.LookupOperatorTree;
import org.cmdbuild.cql.compiler.where.fieldid.SimpleFieldId;
import org.cmdbuild.cql.sqlbuilder.attribute.CMFakeAttribute;
import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.FalseWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.google.common.collect.Lists;

public class CQLAnalyzer {

	public static interface Callback {

		void from(CMClass source);

		void distinct();

		void leftJoin(CMClass target, Alias alias, Over over);

		void join(CMClass target, Alias alias, Over over);

		void where(WhereClause clause);

	}

	private static class JoinElement {

		public static class Builder implements org.cmdbuild.common.Builder<JoinElement> {

			private String domain;
			private Alias domainAlias;
			private String destination;
			private Alias alias;
			private boolean left;

			@Override
			public JoinElement build() {
				return new JoinElement(this);
			}

			public Builder domainName(final String domain) {
				this.domain = domain;
				return this;
			}

			public Builder domainAlias(final Alias domainAlias) {
				this.domainAlias = domainAlias;
				return this;
			}

			public Builder destinationName(final String destination) {
				this.destination = destination;
				return this;
			}

			public Builder destinationAlias(final Alias alias) {
				this.alias = alias;
				return this;
			}

			public Builder isLeft(final boolean left) {
				this.left = left;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		public final String domain;
		public final Alias domainAlias;
		public final String destination;
		public final Alias alias;
		public final boolean left;

		private JoinElement(final Builder builder) {
			this.domain = builder.domain;
			this.domainAlias = builder.domainAlias;
			this.destination = builder.destination;
			this.alias = builder.alias;
			this.left = builder.left;
		}

	}

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(CQLAnalyzer.class.getName());

	public static void analyze(final QueryImpl q, final Map<String, Object> vars, final Callback callback) {
		new CQLAnalyzer(q, vars, callback).analyze();
	}

	private final DataSource dataSource = applicationContext().getBean(DataSource.class);
	private final CMDataView dataView = applicationContext().getBean(DBDataView.class);
	private final LookupStore lookupStore = applicationContext().getBean(LookupStore.class);

	private final QueryImpl query;
	private final Map<String, Object> vars;
	private final Callback callback;

	private CMClass fromClass;
	private final List<WhereClause> whereClauses;
	private final List<JoinElement> joinElements;

	public CQLAnalyzer(final QueryImpl query, final Map<String, Object> vars, final Callback callback) {
		this.query = query;
		this.vars = vars;
		this.callback = callback;
		this.whereClauses = Lists.newArrayList();
		this.joinElements = Lists.newArrayList();
	}

	public void analyze() {
		init();
		callback();
	}

	private void callback() {
		callback.from(fromClass);
		callback.where(isEmpty(whereClauses) ? trueWhereClause() : and(whereClauses));
		if (!joinElements.isEmpty()) {
			callback.distinct();
		}
		for (final JoinElement joinElement : joinElements) {
			final CMDomain domain = dataView.findDomain(joinElement.domain);
			final Alias domainAlias = (joinElement.domainAlias == null) ? canonicalAlias(domain)
					: joinElement.domainAlias;
			final CMClass clazz = dataView.findClass(joinElement.destination);
			final Alias targetAlias = (joinElement.alias == null) ? canonicalAlias(clazz) : joinElement.alias;
			if (joinElement.left) {
				callback.leftJoin(clazz, targetAlias, over(domain, domainAlias));
			} else {
				callback.join(clazz, targetAlias, over(domain, domainAlias));
			}
		}
	}

	private void init() {
		fromClass = query.getFrom().mainClass().getClassTable(dataView);

		final WhereImpl where = query.getWhere();
		for (final WhereElement element : where.getElements()) {
			handleWhereElement(element, fromClass);
		}

		final SelectImpl select = query.getSelect();
		if (!select.isDefault()) {
			for (@SuppressWarnings("rawtypes")
			final SelectElement selectElement : select.getElements()) {
				if (selectElement instanceof ClassSelect) {
					final ClassSelect classSelect = ClassSelect.class.cast(selectElement);
					for (final SelectItem item : classSelect.getElements()) {
						if (item instanceof FieldSelect) {
							FieldSelectImpl.class.cast(item);
							// ?
						} else {
							logger.warn(marker, "unsupported select item '{}'", selectElement.getClass()
									.getSimpleName());
						}
					}
				} else {
					logger.warn("unsupported select element '{}'", selectElement.getClass().getSimpleName());
				}
			}
		}
	}

	private void handleWhereElement(final WhereElement whereElement, final CMClass table) {
		if (whereElement instanceof FieldImpl) {
			logger.debug(marker, "adding field");
			handleField((FieldImpl) whereElement, table);
		} else if (whereElement instanceof DomainObjectsReference) {
			logger.debug(marker, "add domain objs");
			final DomainObjectsReferenceImpl domainObjectReference = DomainObjectsReferenceImpl.class
					.cast(whereElement);
			final DomainDeclarationImpl domainDeclaration = DomainDeclarationImpl.class.cast(domainObjectReference
					.getScope());
			final CMDomain domain = domainDeclaration.getDirectedDomain(dataView);
			final CMClass target = domain.getClass1().isAncestorOf(fromClass) ? domain.getClass2() : domain.getClass1();
			joinElements.add(JoinElement.newInstance() //
					.domainName(domain.getName()) //
					.domainAlias(NameAlias.as(domain.getName() + randomNumeric(10))) //
					.destinationName(target.getName()) //
					.isLeft(false) //
					.build());
			for (final WhereElement element : domainObjectReference.getElements()) {
				handleWhereElement(element, target);
			}
		} else if (whereElement instanceof GroupImpl) {
			logger.debug(marker, "add group");
			final Group group = Group.class.cast(whereElement);
			for (final WhereElement element : group.getElements()) {
				handleWhereElement(element, table);
			}
		} else {
			logger.warn(marker, "unsupported type '{}'", whereElement.getClass());
		}
	}

	private void handleField(final FieldImpl field, final CMClass table) {
		if (field.getId() instanceof SimpleFieldId) {
			handleSimpleField(SimpleFieldId.class.cast(field.getId()), field, table);
		} else if (field.getId() instanceof LookupFieldId) {
			handleLookupField((LookupFieldId) field.getId(), field, table);
		} else {
			throw new RuntimeException("Complex field ids are not supported!");
		}
	}

	private void handleSimpleField(final SimpleFieldId simpleFieldId, final FieldImpl field, final CMClass table) {
		CMAttribute attribute = handleSystemAttributes(simpleFieldId.getId(), table);

		if (attribute == null) {
			attribute = table.getAttribute(simpleFieldId.getId());
		}

		final QueryAliasAttribute attributeForQuery = attribute(table, attribute.getName());
		final List<Object> values = values(field, table, attribute);

		WhereClause whereClause = null;
		if (!values.isEmpty()) {
			final Object value = values.get(0);
			switch (field.getOperator()) {
			case LTEQ:
				whereClause = or(condition(attributeForQuery, eq(value)), condition(attributeForQuery, lt(value)));
				break;
			case GTEQ:
				whereClause = or(condition(attributeForQuery, eq(value)), condition(attributeForQuery, gt(value)));
				break;
			case LT:
				whereClause = condition(attributeForQuery, lt(value));
				break;
			case GT:
				whereClause = condition(attributeForQuery, gt(value));
				break;
			case EQ:
				whereClause = condition(attributeForQuery, eq(value));
				break;
			case CONT:
				whereClause = condition(attributeForQuery, contains(value));
				break;
			case BGN:
				whereClause = condition(attributeForQuery, beginsWith(value));
				break;
			case END:
				whereClause = condition(attributeForQuery, endsWith(value));
				break;
			case BTW:
				whereClause = and(condition(attributeForQuery, gt(value)), condition(attributeForQuery, lt(values.get(1))));
				break;
			case IN:
				whereClause = condition(attributeForQuery, in(values.toArray()));
				break;
			case ISNULL:
				whereClause = condition(attributeForQuery, isNull());
				break;
			default:
				throw new IllegalArgumentException(format("invalid operator '%s'", field.getOperator()));
			}
		} else {
			switch (field.getOperator()) {
			case IN:
				whereClause = FalseWhereClause.falseWhereClause();
				break;
			default: // Do nothing
			}
		}

		if (whereClause != null) {
			whereClauses.add(field.isNot() ? not(whereClause) : whereClause);
		}

	}

	private CMAttribute handleSystemAttributes(final String attributeName, final CMEntryType entryType) {
		CMAttribute attribute = null;
		if (Const.SystemAttributes.Id.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new IntegerAttributeType(), false);
		} else if (Const.SystemAttributes.IdClass.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new IntegerAttributeType(), false);
		} else if (Const.SystemAttributes.BeginDate.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new DateAttributeType(), false);
		} else if (Const.SystemAttributes.Status.getDBName().equals(attributeName)) {
			attribute = new CMFakeAttribute(attributeName, entryType, new StringAttributeType(), false);
		}

		return attribute;
	}

	private void handleLookupField(final LookupFieldId fid, final FieldImpl field, final CMClass table) {
		final LookupOperatorTree node = fid.getTree();
		if (node.getOperator().equalsIgnoreCase("parent")) {
			Object value = 0;
			final FieldValue fieldValue = field.getValues().iterator().next();
			if (node.getAttributeName() == null) {
				if (fieldValue.getType() == FieldValueType.INT) {
					value = fieldValue.getValue();
				} else if (fieldValue.getType() == FieldValueType.STRING) {
					for (final Lookup lookupDto : lookupStore.list()) {
						if (lookupDto.description.equals(fieldValue.getValue().toString())) {
							value = lookupDto.getId();
						}
					}
				} else {
					try {
						final Field lookupDtoField = Lookup.class.getField(node.getAttributeName());
						for (final Lookup lookupDto : lookupStore.list()) {
							if (lookupDtoField.get(lookupDto).equals(fieldValue.getValue().toString())) {
								value = lookupDto.getId();
							}
						}
					} catch (final Exception e) {
						logger.error(marker, "error getting field");
					}
				}
				final CMAttribute attribute = table.getAttribute(node.getAttributeName());
				final Object __value = attribute.getType().convertValue(value);
				final QueryAliasAttribute attributeForQuery = attribute(fromClass, attribute.getName());
				final WhereClause whereClause = condition(attributeForQuery, eq(__value));
				whereClauses.add(field.isNot() ? not(whereClause) : whereClause);
			} else {
				throw new RuntimeException("unsupported lookup operator: " + node.getOperator());
			}
		}
	}

	private List<Object> values(final FieldImpl field, final CMClass table, final CMAttribute attribute) {
		final List<Object> values = Lists.newArrayList();
		for (final FieldValue v : field.getValues()) {
			convert(attribute, v, vars, new ConvertedCallback() {

				@Override
				public void addValue(final Object object) {
					logger.debug(marker, "converted value '{}'" + object);
					values.add(object);
				}

			});
		}

		if (!values.isEmpty()) {
			final Object firstValue = values.get(0);
			final String firstStringValue = (firstValue instanceof String) ? (String) firstValue : null;

			if (firstStringValue != null) {
				attribute.getType().accept(new NullAttributeTypeVisitor() {
					@Override
					public void visit(final LookupAttributeType attributeType) {
						if (field.getValues().iterator().next().getType() != FieldValueType.NATIVE) {
							try {
								attributeType.convertValue(firstStringValue);
							} catch (final Exception e) {
								values.clear();

								Lookup searchedLookup = null;
								final LookupType lookupType = LookupType.newInstance() //
										.withName(attributeType.getLookupTypeName()) //
										.build();

								for (final Lookup lookup : lookupStore.listForType(lookupType)) {
									if (lookup.description.equals(firstStringValue)) {
										searchedLookup = lookup;
										values.add(searchedLookup.getId());
										break;
									}
								}
							}
						}
					}

					@Override
					public void visit(final ReferenceAttributeType attributeType) {
						if (field.getValues().iterator().next().getType() != FieldValueType.NATIVE) {
							try {
								Integer.parseInt(firstStringValue);
							} catch (final NumberFormatException e) {
								final String domainName = attributeType.getDomainName();
								final CMDomain domain = dataView.findDomain(domainName);
								final CMClass target;
								if (domain.getClass1().isAncestorOf(table)) {
									target = domain.getClass2();
								} else {
									target = domain.getClass1();
								}

								final Alias destinationAlias = NameAlias.as(String.format("DST-%s-%s",
										target.getName(), randomNumeric(10)));

								whereClauses.add( //
										condition(attribute(destinationAlias, "Description"), eq(firstStringValue)));
								joinElements.add(JoinElement.newInstance() //
										.domainName(domainName) //
										.domainAlias(NameAlias.as(domain.getName() + randomNumeric(10))) //
										.destinationName(target.getName()) //
										.destinationAlias(destinationAlias) //
										.isLeft(true) //
										.build());
							}
						}
					}
				});
			}
		}
		final List<Object> convertedValues = Lists.newArrayList();
		for (final Object value : values) {
			convertedValues.add(attribute.getType().convertValue(value));
		}

		return convertedValues;
	}

	private interface ConvertedCallback {

		void addValue(Object object);

	}

	private void convert(final CMAttribute attribute, final FieldValue fieldValue, final Map<String, Object> context,
			final ConvertedCallback callback) {
		switch (fieldValue.getType()) {
		case BOOL:
		case DATE:
		case FLOAT:
		case INT:
		case STRING:
		case TIMESTAMP:
			callback.addValue(fieldValue.getValue().toString());
			break;
		case NATIVE:
			sqlQuery(fieldValue.getValue().toString(), callback);
			break;
		case INPUT:
			final FieldInputValue fieldInputValue = FieldInputValue.class.cast(fieldValue.getValue());
			final String variableName = fieldInputValue.getVariableName();
			final Object value = context.get(variableName);
			if (value instanceof java.util.Date) {
				callback.addValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) value));
			} else {
				callback.addValue(value.toString());
			}
			break;
		case SUBEXPR:
			throw new RuntimeException("subqueries are not supported");
		default:
			throw new RuntimeException("cannot convert value " + fieldValue.getType().name() + ": "
					+ fieldValue.getValue() + " to string!");
		}
	}

	private void sqlQuery(final String sql, final ConvertedCallback callback) {
		Log.SQL.debug(marker, "Execute nested SQL in CQL filter: {}", sql);
		new JdbcTemplate(dataSource).query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				callback.addValue(rs.getObject(1));
			}
		});
	}

}
