package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_GT;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_IN;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_LIKE;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_LT;
import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_NULL;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.List;

import org.cmdbuild.common.Holder;
import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.EmptyArrayOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.FalseWhereClause;
import org.cmdbuild.dao.query.clause.where.FunctionWhereClause;
import org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.InOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.NotWhereClause;
import org.cmdbuild.dao.query.clause.where.NullOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.OperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.StringArrayOverlapOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;

public class WherePartCreator extends PartCreator implements WhereClauseVisitor {

	public static interface ActiveStatusChecker {

		boolean needsActiveStatus();

	}

	private static final String CAST_OPERATOR = "::";
	private static final String NO_CAST = null;

	private static final Holder<Object> VALUE_NOT_REQUIRED = null;

	private final QuerySpecs querySpecs;

	public WherePartCreator(final QuerySpecs querySpecs) {
		this(querySpecs, new ActiveStatusChecker() {
			@Override
			public boolean needsActiveStatus() {
				final FromClause fromClause = querySpecs.getFromClause();
				return fromClause.getType().holdsHistory() && !fromClause.isHistory();
			}
		});
	}

	public WherePartCreator(final QuerySpecs querySpecs, final ActiveStatusChecker activeStatusChecker) {
		super();
		this.querySpecs = querySpecs;
		querySpecs.getWhereClause().accept(this);
		if (activeStatusChecker.needsActiveStatus()) {
			and(attributeFilter(attribute(querySpecs.getFromClause().getAlias(), SystemAttributes.Status.getDBName()),
					null, OPERATOR_EQ, valuesOf(CardStatus.ACTIVE.value())));
		}
		excludeEntryTypes();
	}

	/**
	 * Excludes disabled classes or not accessible classes (due to lack of
	 * privileges)
	 */
	private void excludeEntryTypes() {
		querySpecs.getFromClause().getType().accept(new CMEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				for (final CMClass cmClass : type.getLeaves()) {
					final FromClause.EntryTypeStatus status = querySpecs.getFromClause().getStatus(cmClass);
					if (!status.isAccessible() || !status.isActive()) {
						andNot(attributeFilter(
								attribute(querySpecs.getFromClause().getAlias(), SystemAttributes.IdClass.getDBName()),
								null, OPERATOR_EQ, valuesOf(cmClass.getId())));
					}
				}
			}

			@Override
			public void visit(final CMDomain type) {
				// nothing to do
			}

			@Override
			public void visit(final CMFunctionCall type) {
				// nothing to do
			}

		});
	}

	private WherePartCreator append(final String string) {
		if (sb.length() == 0) {
			sb.append("WHERE");
		}
		sb.append(" ").append(string);
		return this;
	}

	private void and(final String string) {
		if (sb.length() > 0) {
			append("AND");
		}
		append(string);
	}

	private void or(final String string) {
		if (sb.length() > 0) {
			append("OR");
		}
		append(string);
	}

	private void andNot(final String string) {
		if (sb.length() > 0) {
			append("AND NOT");
		}
		append(string);
	}

	@Override
	public void visit(final AndWhereClause whereClause) {
		append("(");
		// TODO do it better
		final List<? extends WhereClause> clauses = whereClause.getClauses();
		for (int i = 0; i < clauses.size(); i++) {
			if (i > 0) {
				and(" ");
			}
			clauses.get(i).accept(this);
		}
		append(")");
	}

	@Override
	public void visit(final SimpleWhereClause whereClause) {
		whereClause.getOperator().accept(new OperatorAndValueVisitor() {

			@Override
			public void visit(final EqualsOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_EQ,
						valueOf(operatorAndValue.getValue())));
			}

			@Override
			public void visit(final GreaterThanOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_GT,
						valueOf(operatorAndValue.getValue())));
			}

			@Override
			public void visit(final LessThanOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LT,
						valueOf(operatorAndValue.getValue())));
			}

			@Override
			public void visit(final ContainsOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LIKE,
						valueOf("%" + operatorAndValue.getValue() + "%")));
			}

			@Override
			public void visit(final BeginsWithOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LIKE,
						valueOf(operatorAndValue.getValue() + "%")));
			}

			@Override
			public void visit(final EndsWithOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_LIKE,
						valueOf("%" + operatorAndValue.getValue())));
			}

			@Override
			public void visit(final NullOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_NULL,
						VALUE_NOT_REQUIRED));
			}

			@Override
			public void visit(final InOperatorAndValue operatorAndValue) {
				append(attributeFilter(whereClause.getAttribute(), whereClause.getAttributeNameCast(), OPERATOR_IN,
						valuesOf(operatorAndValue.getValue())));
			}

			@Override
			public void visit(final StringArrayOverlapOperatorAndValue operatorAndValue) {
				final String template = " %s && string_to_array('%s',',')::varchar[] ";
				final QueryAliasAttribute attributeAlias = whereClause.getAttribute();
				final String quotedAttributeName = Utils.quoteAttribute(attributeAlias.getEntryTypeAlias(),
						attributeAlias.getName());

				append(String.format(template, quotedAttributeName, operatorAndValue.getValue()));
			}

			@Override
			public void visit(final EmptyArrayOperatorAndValue operatorAndValue) {
				final String template = " coalesce(array_length(%s, 1), 0) = 0 ";
				final QueryAliasAttribute attributeAlias = whereClause.getAttribute();
				final String quotedAttributeName = Utils.quoteAttribute(attributeAlias.getEntryTypeAlias(),
						attributeAlias.getName());

				append(String.format(template, quotedAttributeName));
			}

			private Holder<Object> valueOf(final Object value) {
				return WherePartCreator.this.valuesOf(value);
			}

		});
	}

	private Holder<Object> valuesOf(final Object value) {
		return new Holder<Object>() {
			@Override
			public Object get() {
				return value;
			}
		};
	}

	@Override
	public void visit(final OrWhereClause whereClause) {
		append("(");
		// TODO do it better
		final List<? extends WhereClause> clauses = whereClause.getClauses();
		for (int i = 0; i < clauses.size(); i++) {
			if (i > 0) {
				or(" ");
			}
			clauses.get(i).accept(this);
		}
		append(")");
	}

	@Override
	public void visit(final NotWhereClause whereClause) {
		append("NOT (");
		whereClause.getClauses().get(0).accept(this);
		append(")");
	}

	@Override
	public void visit(final EmptyWhereClause whereClause) {
		if (sb.length() != 0) {
			throw new IllegalArgumentException("Cannot use an empty clause along with other where clauses");
		}
	}

	@Override
	public void visit(final TrueWhereClause whereClause) {
		append(" TRUE ");
	}

	@Override
	public void visit(final FalseWhereClause whereClause) {
		append(" FALSE ");
	}

	@Override
	public void visit(final FunctionWhereClause whereClause) {
		append(format("%s %s (SELECT %s(?, ?, ?)) ", nameOf(whereClause.attribute), OPERATOR_IN, whereClause.name));
		param(whereClause.userId.intValue());
		param(whereClause.roleId.intValue());
		param(whereClause.entryType.getName());
	}

	private String attributeFilter(final QueryAliasAttribute attribute, final String attributeNameCast,
			final String operator, final Holder<Object> holder) {
		final String attributeName = nameOf(attribute, attributeNameCast);
		final String attributeCast = attributeCastOf(attribute, attributeNameCast);
		return format("%s %s %s", attributeName, operator,
				(holder != VALUE_NOT_REQUIRED) ? param(sqlValueOf(attribute, holder.get()), attributeCast) : EMPTY);
	}

	private String nameOf(final QueryAliasAttribute attribute) {
		return nameOf(attribute, NO_CAST);
	}

	private String nameOf(final QueryAliasAttribute attribute, final String attributeNameCast) {
		final boolean isAttributeNameCastSpecified = (attributeNameCast != NO_CAST);
		final String attributeName = Utils.quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
		return new StringBuilder(attributeName) //
				.append(isAttributeNameCastSpecified ? (CAST_OPERATOR + attributeNameCast) : EMPTY) //
				.toString();
	}

	private String attributeCastOf(final QueryAliasAttribute attribute, final String attributeNameCast) {
		final boolean isAttributeNameCastSpecified = (attributeNameCast != null);
		return isAttributeNameCastSpecified ? null : sqlTypeOf(attribute).sqlCast();
	}

	private Object sqlValueOf(final QueryAliasAttribute attribute, final Object value) {
		if (value instanceof IdAndDescription) {
			return IdAndDescription.class.cast(value).getId();
		}
		return sqlTypeOf(attribute).javaToSqlValue(value);
	}

	private SqlType sqlTypeOf(final QueryAliasAttribute attribute) {
		return SqlType.getSqlType(typeOf(attribute));
	}

	private CMAttributeType<?> typeOf(final QueryAliasAttribute attribute) {
		final CMAttribute _attribute = new CMEntryTypeVisitor() {

			private CMAttribute _attribute;

			public CMAttribute findAttribute(final CMEntryType type) {
				type.accept(this);
				return _attribute;
			}

			@Override
			public void visit(final CMClass type) {
				final String key = attribute.getName();
				_attribute = querySpecs.getFromClause().getType().getAttribute(key);
				if (_attribute == null) {
					/*
					 * attribute not found, probably it's a superclass so we
					 * search within all subclasses (leaves) hoping to find it:
					 * the first one is selected, keeping fingers crossed...
					 * 
					 * TODO the query generation must be implemented is a
					 * different way or the QueryAliasAttribute must keep an
					 * information of it's owner (entry type)
					 */
					for (final CMClass leaf : type.getLeaves()) {
						_attribute = leaf.getAttribute(key);
						if (_attribute != null) {
							break;
						}
					}
				}
			}

			@Override
			public void visit(final CMDomain type) {
				final String key = attribute.getName();
				_attribute = querySpecs.getFromClause().getType().getAttribute(key);
			}

			@Override
			public void visit(final CMFunctionCall type) {
				final String key = attribute.getName();
				_attribute = querySpecs.getFromClause().getType().getAttribute(key);
			}

		}.findAttribute(querySpecs.getFromClause().getType());
		return (_attribute == null) ? UndefinedAttributeType.undefined() : _attribute.getType();
	}
}
