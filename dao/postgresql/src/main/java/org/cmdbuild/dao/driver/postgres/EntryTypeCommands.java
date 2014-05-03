package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.driver.postgres.Const.DOMAIN_PREFIX;
import static org.cmdbuild.dao.driver.postgres.SqlType.createAttributeType;
import static org.cmdbuild.dao.driver.postgres.SqlType.getSqlTypeString;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromNameAndNamespace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBDomain.DomainMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.function.DBFunction.FunctionMetadata;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class EntryTypeCommands implements LoggingSupport {

	private static final String DEFAULT_SCHEMA = "public";

	private static final Pattern COMMENT_PATTERN = Pattern.compile("(([A-Z0-9]+): ([^|]*))*");

	private final DBDriver driver;
	private final JdbcTemplate jdbcTemplate;

	EntryTypeCommands(final DBDriver driver, final JdbcTemplate jdbcTemplate) {
		this.driver = driver;
		this.jdbcTemplate = jdbcTemplate;
	}

	/*
	 * Classes
	 */

	public List<DBClass> findAllClasses() {
		final ClassTreeBuilder classTreeBuilder = new ClassTreeBuilder();
		jdbcTemplate.query("SELECT table_id" //
				+ ", _cm_cmtable(table_id) AS table_name" //
				+ ", _cm_cmschema(table_id) as table_schema" //
				+ ", _cm_parent_id(table_id) AS parent_id" //
				+ ", _cm_comment_for_table_id(table_id) AS table_comment" //
				+ " FROM _cm_class_list() AS table_id"
				/*
				 * TODO configure usable schemas in another way
				 */
				+ " WHERE _cm_cmschema(table_id) IN (_cm_cmschema('\"Class\"'::regclass::oid), 'bim')",
				classTreeBuilder);

		return classTreeBuilder.getResult();
	}

	private class ClassTreeBuilder implements RowCallbackHandler {

		private class ClassAndParent {

			public final DBClass dbClass;
			public final Object parentId;

			public ClassAndParent(final DBClass dbClass, final Object parentId) {
				this.dbClass = dbClass;
				this.parentId = parentId;
			}

		}

		private final Map<Object, ClassAndParent> classMap = new HashMap<Object, ClassAndParent>();

		@Override
		public void processRow(final ResultSet rs) throws SQLException {
			final Long id = rs.getLong("table_id");
			final String name = rs.getString("table_name");
			final String namespace = schemaToNamespace(rs.getString("table_schema"));
			final Long parentId = (Long) rs.getObject("parent_id");
			final List<DBAttribute> attributes = userEntryTypeAttributesFor(id);
			final String comment = rs.getString("table_comment");
			final ClassMetadata meta = classCommentToMetadata(comment);
			final DBClass dbClass = DBClass.newClass() //
					.withIdentifier(fromNameAndNamespace(name, namespace)) //
					.withId(id) //
					.withAllMetadata(meta) //
					.withAllAttributes(attributes) //
					.build();
			classMap.put(id, new ClassAndParent(dbClass, parentId));
		}

		public List<DBClass> getResult() {
			return linkClasses();
		}

		private List<DBClass> linkClasses() {
			final List<DBClass> allClasses = new ArrayList<DBClass>();
			for (final ClassAndParent cap : classMap.values()) {
				final DBClass child = cap.dbClass;
				if (cap.parentId != null) {
					final DBClass parent = classMap.get(cap.parentId).dbClass;
					child.setParent(parent);
				}
				allClasses.add(child);
			}
			return allClasses;
		}
	}

	public DBClass createClass(final DBClassDefinition definition) {
		final CMClass parent = definition.getParent();
		final String parentName = (parent != null) ? nameFrom(parent.getIdentifier()) : null;
		final String classComment = commentFrom(definition);
		final CMIdentifier identifier = definition.getIdentifier();
		final String name = nameFrom(identifier);
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_create_class('%s', '%s', '%s');", name,
				parentName, classComment));
		final long id = jdbcTemplate.queryForInt( //
				"SELECT * FROM cm_create_class(?, ?, ?)", //
				new Object[] { name, parentName, classComment });
		final DBClass newClass = DBClass.newClass() //
				.withIdentifier(identifier) //
				.withId(id) //
				.withAllMetadata(classCommentToMetadata(classComment)) //
				.withAllAttributes(userEntryTypeAttributesFor(id)) //
				.build();
		newClass.setParent(definition.getParent());
		return newClass;
	}

	public DBClass updateClass(final DBClassDefinition definition) {
		final CMIdentifier identifier = definition.getIdentifier();
		final String name = nameFrom(identifier);
		final String comment = commentFrom(definition);
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_modify_class('%s', '%s');", name, comment));
		jdbcTemplate.queryForObject( //
				"SELECT * FROM cm_modify_class(?, ?)", //
				Object.class, //
				new Object[] { name, comment });
		final DBClass updatedClass = DBClass.newClass() //
				.withIdentifier(identifier) //
				.withId(definition.getId()) //
				.withAllMetadata(classCommentToMetadata(comment)) //
				.withAllAttributes(userEntryTypeAttributesFor(definition.getId())) //
				.build();
		updatedClass.setParent(definition.getParent());
		return updatedClass;
	}

	private String commentFrom(final DBClassDefinition definition) {
		return format("DESCR: %s|MODE: %s|STATUS: %s|SUPERCLASS: %b|TYPE: %s|USERSTOPPABLE: %s", //
				definition.getDescription(), //
				definition.isSystem() ? "reserved" : "write", //
				statusFrom(definition.isActive()), //
				definition.isSuperClass(), //
				typeFrom(definition.isHoldingHistory()), //
				definition.isUserStoppable());
	}

	private String statusFrom(final boolean active) {
		return active ? EntryTypeCommentMapper.STATUS_ACTIVE : EntryTypeCommentMapper.STATUS_NOACTIVE;
	}

	private String typeFrom(final boolean isHoldingHistory) {
		return CommentMappers.CLASS_COMMENT_MAPPER.getCommentValueFromMeta("TYPE", //
				Boolean.valueOf(isHoldingHistory).toString());
	}

	public void deleteClass(final DBClass dbClass) {
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_delete_class('%s');", nameFrom(dbClass)));
		jdbcTemplate.queryForObject("SELECT * FROM cm_delete_class(?)", Object.class,
				new Object[] { nameFrom(dbClass) });
		dbClass.setParent(null);
	}

	public DBAttribute createAttribute(final DBAttributeDefinition definition) {
		final DBEntryType owner = definition.getOwner();
		final String comment = commentFrom(definition);
		final String domainPrefixForLog = owner instanceof DBDomain ? "Map_" : EMPTY;
		final String logString = definition.getDefaultValue() != null ? "SELECT * FROM cm_create_attribute('\"%s%s\"'::regclass,'%s','%s','%s',%b,%b,'%s');"
				: "SELECT * FROM cm_create_attribute('\"%s%s\"'::regclass,'%s','%s',%s,%b,%b,'%s');";
		dataDefinitionSqlLogger.info(String.format(logString, //
				domainPrefixForLog, //
				owner.getName(), //
				definition.getName(), //
				getSqlTypeString(definition.getType()), //
				definition.getDefaultValue(), //
				definition.isMandatory(), //
				definition.isUnique(), //
				comment));
		jdbcTemplate.queryForObject( //
				"SELECT * FROM cm_create_attribute(?,?,?,?,?,?,?)", //
				Object.class, //
				new Object[] { //
				owner.getId(), //
						definition.getName(), //
						getSqlTypeString(definition.getType()), //
						definition.getDefaultValue(), //
						definition.isMandatory(), //
						definition.isUnique(), //
						comment //
				});
		final AttributeMetadata attributeMetadata = attributeCommentToMetadata(comment);
		attributeMetadata.put(AttributeMetadata.DEFAULT, definition.getDefaultValue());
		attributeMetadata.put(AttributeMetadata.MANDATORY, "" + definition.isMandatory());
		attributeMetadata.put(AttributeMetadata.UNIQUE, "" + definition.isUnique());
		final DBAttribute newAttribute = new DBAttribute( //
				definition.getName(), //
				definition.getType(), //
				attributeMetadata);
		owner.addAttribute(newAttribute);

		/**
		 * adding attribute to descendants
		 */
		if (definition.getOwner() instanceof DBClass) {
			final AttributeMetadata am = attributeCommentToMetadata(comment);
			for (final DBClass descendant : ((DBClass) definition.getOwner()).getDescendants()) {
				am.put(AttributeMetadata.INHERITED, "true");
				final DBAttribute attribute = new DBAttribute( //
						definition.getName(), //
						definition.getType(), //
						am);
				descendant.addAttribute(attribute);
			}
		}
		return newAttribute;
	}

	public DBAttribute updateAttribute(final DBAttributeDefinition definition) {
		final DBEntryType owner = definition.getOwner();
		final String comment = commentFrom(definition);
		final String updatedDefaultValue = definition.getDefaultValue();
		final String existingDefaultValue = owner.getAttribute(definition.getName()).getDefaultValue();
		final boolean isDefaultValueChanged = isDefaultValueChanged(updatedDefaultValue, existingDefaultValue);
		final String domainPrefixForLog = owner instanceof DBDomain ? "Map_" : EMPTY;
		final String logString = definition.getDefaultValue() != null ? "SELECT * FROM cm_modify_attribute('\"%s%s\"'::regclass,'%s','%s','%s',%b,%b,'%s');" //
				: "SELECT * FROM cm_modify_attribute('\"%s%s\"'::regclass,'%s','%s',%s,%b,%b,'%s');";
		dataDefinitionSqlLogger.info(String.format(logString, //
				domainPrefixForLog, //
				owner.getName(), //
				definition.getName(), //
				getSqlTypeString(definition.getType()), //
				definition.getDefaultValue(), //
				definition.isMandatory(), //
				definition.isUnique(), //
				comment));
		jdbcTemplate.queryForObject( //
				"SELECT * FROM cm_modify_attribute(?,?,?,?,?,?,?)", //
				Object.class, //
				new Object[] { //
				owner.getId(), //
						definition.getName(), //
						getSqlTypeString(definition.getType()), //
						isDefaultValueChanged ? updatedDefaultValue : existingDefaultValue, //
						definition.isMandatory(), //
						definition.isUnique(), //
						comment //
				});
		final AttributeMetadata attributeMetadata = attributeCommentToMetadata(comment);
		attributeMetadata.put(AttributeMetadata.DEFAULT, isDefaultValueChanged ? updatedDefaultValue
				: existingDefaultValue);
		attributeMetadata.put(AttributeMetadata.MANDATORY, "" + definition.isMandatory());
		attributeMetadata.put(AttributeMetadata.UNIQUE, "" + definition.isUnique());
		final DBAttribute newAttribute = new DBAttribute( //
				definition.getName(), //
				definition.getType(), //
				attributeMetadata);
		sqlLogger.trace("assigning updated attribute to owner '{}'", nameFrom(owner.getIdentifier()));
		owner.addAttribute(newAttribute);
		return newAttribute;
	}

	private static boolean isDefaultValueChanged(final String newValue, final String existingValue) {
		if (newValue == null && existingValue == null) {
			return false;
		} else if (newValue == null && existingValue != null) {
			return true;
		} else if (!newValue.equals(existingValue)) {
			return true;
		} else {
			return false;
		}
	}

	public void deleteAttribute(final DBAttribute attribute) {
		final DBEntryType owner = attribute.getOwner();
		final String domainPrefixForLog = owner instanceof DBDomain ? "Map_" : EMPTY;
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_delete_attribute('\"%s%s\"'::regclass, '%s');",
				domainPrefixForLog, //
				owner.getName(), //
				attribute.getName()));
		jdbcTemplate.queryForObject( //
				"SELECT * FROM cm_delete_attribute(?,?)", //
				Object.class, //
				new Object[] { owner.getId(), attribute.getName() });
		attribute.getOwner().removeAttribute(attribute);

		/**
		 * removing attribute from descendants
		 */
		if (attribute.getOwner() instanceof DBClass) {
			for (final DBClass descendant : ((DBClass) attribute.getOwner()).getDescendants()) {
				final DBAttribute attributeToRemove = descendant.getAttribute(attribute.getName());
				descendant.removeAttribute(attributeToRemove);
			}
		}
	}

	public void clear(final DBAttribute attribute) {
		final DBEntryType owner = attribute.getOwner();
		final String domainPrefixForLog = owner instanceof DBDomain ? "Map_" : EMPTY;
		final String ownerName = String.format("\"%s%s\"", domainPrefixForLog, owner.getName());
		jdbcTemplate.queryForObject( //
				"SELECT * FROM _cm_disable_triggers_recursively(?)", //
				Object.class, //
				new Object[] { ownerName } //
				);
		jdbcTemplate.execute(String.format( //
				"UPDATE \"%s%s\" SET \"%s\" = null", //
				domainPrefixForLog, //
				owner.getName(), //
				attribute.getName() //
				));
		jdbcTemplate.queryForObject( //
				"SELECT * FROM _cm_enable_triggers_recursively(?)", //
				Object.class, //
				new Object[] { ownerName } //
				);
	}

	private String commentFrom(final DBAttributeDefinition definition) {
		return new CMAttributeTypeVisitor() {

			private final StringBuilder builder = new StringBuilder();

			@Override
			public void visit(final BooleanAttributeType attributeType) {
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				append(DBAttribute.AttributeMetadata.FK_TARGET_CLASS, attributeType.getForeignKeyDestinationClassName());
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				append(DBAttribute.AttributeMetadata.LOOKUP_TYPE, attributeType.getLookupTypeName());
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final CMIdentifier identifier = attributeType.getIdentifier();
				final CMDomain domain = driver.findDomain(identifier.getLocalName(), identifier.getNameSpace());
				Validate.notNull(domain, "unexpected domain not found");
				// TODO really needed?
				append(DBAttribute.AttributeMetadata.REFERENCE_DIRECT, "false");
				append(DBAttribute.AttributeMetadata.REFERENCE_DOMAIN, nameFrom(identifier));
				// TODO really needed?
				append(DBAttribute.AttributeMetadata.REFERENCE_TYPE, "restrict");
				if (definition.getFilter() != null) {
					append(DBAttribute.AttributeMetadata.FILTER, definition.getFilter());
				}
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				append(DBAttribute.AttributeMetadata.EDITOR_TYPE, definition.getEditorType());
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
			}

			@Override
			public void visit(final StringArrayAttributeType attributeType) {
			}

			private void append(final String key, final String value) {
				final CommentMapper commentMapper = CommentMappers.ATTRIBUTE_COMMENT_MAPPER;
				final String commentKey = commentMapper.getCommentNameFromMeta(key);
				if (builder.length() > 0) {
					builder.append("|");
				}
				builder.append(format("%s: %s", commentKey, value));
			}

			public String build(final DBAttributeDefinition definition) {
				definition.getType().accept(this);
				append(EntryTypeMetadata.ACTIVE, definition.isActive() ? "active" : "noactive");
				append(DBAttribute.AttributeMetadata.BASEDSP, Boolean.toString(definition.isDisplayableInList()));
				append(DBAttribute.AttributeMetadata.CLASSORDER, Integer.toString(definition.getClassOrder()));
				append(EntryTypeMetadata.DESCRIPTION, definition.getDescription());
				if (definition.getGroup() != null) {
					append(DBAttribute.AttributeMetadata.GROUP, definition.getGroup());
				}
				append(DBAttribute.AttributeMetadata.INDEX, Integer.toString(definition.getIndex()));
				append(EntryTypeMetadata.MODE, definition.getMode().toString().toLowerCase());
				append(DBAttribute.AttributeMetadata.FIELD_MODE, definition.getMode().toString().toLowerCase());
				return builder.toString();
			}

		} //
		.build(definition);
	}

	/*
	 * Domains
	 */

	public List<DBDomain> findAllDomains() {
		// Exclude Map since we don't need it anymore!
		final List<DBDomain> domainList = jdbcTemplate.query("SELECT domain_id" //
				+ ", _cm_cmtable(domain_id) AS domain_name" //
				+ ", _cm_cmschema(domain_id) as domain_schema" //
				+ ", _cm_comment_for_table_id(domain_id) AS domain_comment" //
				+ " FROM _cm_domain_list() AS domain_id" //
				+ " WHERE domain_id <> '\"Map\"'::regclass", //
				new RowMapper<DBDomain>() {

					@Override
					public DBDomain mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						final Long id = rs.getLong("domain_id");
						final String name = tableNameToDomainName(rs.getString("domain_name"));
						final String namespace = schemaToNamespace(rs.getString("domain_schema"));
						final List<DBAttribute> attributes = userEntryTypeAttributesFor(id);
						final String comment = rs.getString("domain_comment");
						final DomainMetadata meta = domainCommentToMetadata(comment);
						// FIXME we should handle this in another way
						final DBClass class1 = driver.findClass(meta.get(DomainMetadata.CLASS_1));
						final DBClass class2 = driver.findClass(meta.get(DomainMetadata.CLASS_2));
						final DBDomain domain = DBDomain.newDomain() //
								.withIdentifier(fromNameAndNamespace(name, namespace)) //
								.withId(id) //
								.withAllMetadata(meta) //
								.withAllAttributes(attributes) //
								.withClass1(class1) //
								.withClass2(class2) //
								.build();
						return domain;
					}

					private String tableNameToDomainName(final String tableName) {
						if (!tableName.startsWith(DOMAIN_PREFIX)) {
							throw new IllegalArgumentException("Domains should start with " + DOMAIN_PREFIX);
						}
						return tableName.substring(DOMAIN_PREFIX.length());
					}

				});
		return domainList;
	}

	public DBDomain createDomain(final DBDomainDefinition definition) {
		final CMIdentifier identifier = definition.getIdentifier();
		final String name = nameFrom(identifier);
		final String domainComment = commentFrom(definition);
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_create_domain('%s', '%s');", name, domainComment));
		final long id = jdbcTemplate.queryForInt("SELECT * FROM cm_create_domain(?, ?)", //
				new Object[] { name, domainComment });
		return DBDomain.newDomain() //
				.withIdentifier(identifier) //
				.withId(id) //
				.withAllAttributes(userEntryTypeAttributesFor(id)) //
				// FIXME looks ugly!
				// .withAttribute(new DBAttribute(DBRelation._1, new
				// ReferenceAttributeType(id), null)) //
				// .withAttribute(new DBAttribute(DBRelation._2, new
				// ReferenceAttributeType(id), null)) //
				.withAllMetadata(domainCommentToMetadata(domainComment)) //
				.withClass1(definition.getClass1()) //
				.withClass2(definition.getClass2()) //
				.build();
	}

	public DBDomain updateDomain(final DBDomainDefinition definition) {
		final CMIdentifier identifier = definition.getIdentifier();
		final String name = nameFrom(identifier);
		final String domainComment = commentFrom(definition);
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_modify_domain('%s', '%s');", name, domainComment));
		jdbcTemplate.queryForObject("SELECT * FROM cm_modify_domain(?, ?)", //
				Object.class, //
				new Object[] { name, domainComment });
		final long id = definition.getId();
		return DBDomain.newDomain() //
				.withIdentifier(identifier) //
				.withId(id) //
				.withAllAttributes(userEntryTypeAttributesFor(id)) //
				// FIXME looks ugly!
				// .withAttribute(new DBAttribute(DBRelation._1, new
				// ReferenceAttributeType(identifier), null)) //
				// .withAttribute(new DBAttribute(DBRelation._2, new
				// ReferenceAttributeType(identifier), null)) //
				.withAllMetadata(domainCommentToMetadata(domainComment)) //
				.withClass1(definition.getClass1()) //
				.withClass2(definition.getClass2()) //
				.build();
	}

	private String commentFrom(final DBDomainDefinition definition) {
		// TODO handle more that two classes
		return format(
				"LABEL: %s|DESCRDIR: %s|DESCRINV: %s|MODE: write|STATUS: %s|TYPE: domain|CLASS1: %s|CLASS2: %s|CARDIN: %s|MASTERDETAIL: %s|MDLABEL: %s", //
				definition.getDescription(), //
				defaultIfBlank(definition.getDirectDescription(), EMPTY), //
				defaultIfBlank(definition.getInverseDescription(), EMPTY), //
				definition.isActive() ? "active" : "noactive", //
				nameFrom(definition.getClass1()), //
				nameFrom(definition.getClass2()), //
				defaultIfBlank(definition.getCardinality(), "N:N"), //
				Boolean.toString(definition.isMasterDetail()), //
				defaultIfBlank(definition.getMasterDetailDescription(), EMPTY));
	}

	public void deleteDomain(final DBDomain dbDomain) {
		dataDefinitionSqlLogger.info(String.format("SELECT * FROM cm_delete_domain('%s');",
				nameFrom(dbDomain.getIdentifier())));
		jdbcTemplate.queryForObject("SELECT * FROM cm_delete_domain(?)", //
				Object.class, //
				new Object[] { nameFrom(dbDomain.getIdentifier()) });
	}

	/*
	 * Attributes
	 */

	/**
	 * Returns user-only entry type attributes (so, not {@code reserved}
	 * attributes).
	 * 
	 * @param entryTypeId
	 *            is the id of he entry type (e.g. {@link DBClass},
	 *            {@link DBDomain}).
	 * 
	 * @return a list of user attributes.
	 */
	private List<DBAttribute> userEntryTypeAttributesFor(final long entryTypeId) {
		sqlLogger.trace("getting attributes for entry type with id '{}'", entryTypeId);
		// Note: Sort the attributes in the query
		final List<DBAttribute> entityTypeAttributes = jdbcTemplate.query("SELECT A.name" //
				+ ", _cm_comment_for_attribute(A.cid, A.name) AS comment" //
				+ ", _cm_attribute_is_notnull(A.cid, A.name) AS not_null_constraint" //
				+ ", _cm_attribute_is_unique(A.cid, A.name) AS unique_constraint" //
				+ ", _cm_get_attribute_sqltype(A.cid, A.name) AS sql_type" //
				+ ", _cm_attribute_is_inherited(A.cid, name) AS inherited" //
				+ ", _cm_get_attribute_default (A.cid, A.name) AS default_value" //
				+ " FROM (SELECT C.cid, _cm_attribute_list(C.cid) AS name FROM (SELECT ? AS cid) AS C) AS A" //
				+ " WHERE _cm_read_comment(_cm_comment_for_attribute(A.cid, A.name), 'MODE') NOT ILIKE 'reserved'" //
				+ " ORDER BY _cm_read_comment(_cm_comment_for_attribute(A.cid, A.name), 'INDEX')::int", //
				new Object[] { entryTypeId }, new RowMapper<DBAttribute>() {
					@Override
					public DBAttribute mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						final String name = rs.getString("name");
						final String comment = rs.getString("comment");
						final AttributeMetadata meta = attributeCommentToMetadata(comment);
						meta.put(AttributeMetadata.INHERITED, Boolean.toString(rs.getBoolean("inherited")));
						meta.put(AttributeMetadata.DEFAULT, rs.getString("default_value"));
						meta.put(AttributeMetadata.MANDATORY, Boolean.toString(rs.getBoolean("not_null_constraint")));
						meta.put(AttributeMetadata.UNIQUE, Boolean.toString(rs.getBoolean("unique_constraint")));
						final CMAttributeType<?> type = createAttributeType(rs.getString("sql_type"), meta);
						return new DBAttribute(name, type, meta);
					}
				});
		return entityTypeAttributes;
	}

	private enum InputOutput {
		i, o, io;
	}

	public List<DBFunction> findAllFunctions() {
		final List<DBFunction> functionList = jdbcTemplate.query("SELECT * FROM _cm_function_list()",
				new RowMapper<DBFunction>() {
					@Override
					public DBFunction mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						final String name = rs.getString("function_name");
						final Long id = rs.getLong("function_id");
						final boolean returnsSet = rs.getBoolean("returns_set");
						final FunctionMetadata meta = functionCommentToMetadata(rs.getString("comment"));
						final DBFunction function = new DBFunction(fromName(name), id, returnsSet);
						function.addCategories(meta.getCategories());
						addParameters(rs, function);
						return function;
					}

					private void addParameters(final ResultSet rs, final DBFunction function) throws SQLException {
						final String[] argIo = (String[]) rs.getArray("arg_io").getArray();
						final String[] argNames = (String[]) rs.getArray("arg_names").getArray();
						final String[] argTypes = (String[]) rs.getArray("arg_types").getArray();
						if (argIo.length != argNames.length || argNames.length != argTypes.length) {
							return; // Can't happen
						}
						for (int i = 0; i < argIo.length; ++i) {
							final String name = argNames[i];
							final CMAttributeType<?> type = createAttributeType(argTypes[i]);
							final InputOutput io = InputOutput.valueOf(argIo[i]);
							switch (io) {
							case i:
								function.addInputParameter(name, type);
								break;
							case o:
								function.addOutputParameter(name, type);
								break;
							case io:
								function.addInputParameter(name, type);
								function.addOutputParameter(name, type);
								break;
							}
						}
					}
				});
		return functionList;
	}

	/*
	 * Utils
	 */

	private static String nameFrom(final CMEntryType entryType) {
		return nameFrom(entryType.getIdentifier());
	}

	private static String nameFrom(final CMIdentifier identifier) {
		final String name;
		if (identifier.getNameSpace() != null) {
			name = format("%s.%s", identifier.getNameSpace(), identifier.getLocalName());
		} else {
			name = identifier.getLocalName();
		}
		return name;
	}

	private static String schemaToNamespace(final String schema) {
		return DEFAULT_SCHEMA.equals(schema) ? CMIdentifier.DEFAULT_NAMESPACE : schema;
	}

	private static ClassMetadata classCommentToMetadata(final String comment) {
		final ClassMetadata meta = new ClassMetadata();
		extractCommentToMetadata(comment, meta, CommentMappers.CLASS_COMMENT_MAPPER);
		return meta;
	}

	private static AttributeMetadata attributeCommentToMetadata(final String comment) {
		final AttributeMetadata meta = new AttributeMetadata();
		extractCommentToMetadata(comment, meta, CommentMappers.ATTRIBUTE_COMMENT_MAPPER);
		return meta;
	}

	private static DomainMetadata domainCommentToMetadata(final String comment) {
		final DomainMetadata meta = new DomainMetadata();
		extractCommentToMetadata(comment, meta, CommentMappers.DOMAIN_COMMENT_MAPPER);
		return meta;
	}

	private static FunctionMetadata functionCommentToMetadata(final String comment) {
		final FunctionMetadata meta = new FunctionMetadata();
		extractCommentToMetadata(comment, meta, CommentMappers.FUNCTION_COMMENT_MAPPER);
		return meta;
	}

	private static void extractCommentToMetadata(final String comment, final EntryTypeMetadata meta,
			final CommentMapper mapper) {
		if (isNotBlank(comment)) {
			final Matcher commentMatcher = COMMENT_PATTERN.matcher(comment);
			while (commentMatcher.find()) {
				final String commentKey = commentMatcher.group(2);
				final String metaKey = mapper.getMetaNameFromComment(commentKey);
				if (metaKey != null) {
					final String commentValue = commentMatcher.group(3);
					final String metaValue = mapper.getMetaValueFromComment(commentKey, commentValue);
					meta.put(metaKey, metaValue);
				}
			}
		}
	}

}
