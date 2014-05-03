package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.IdClass;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowNumber;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.RowsCount;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBFunctionCallOutput;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.DBQueryRow;
import org.cmdbuild.dao.query.ExternalReferenceAliasHandler;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

class EntryQueryCommand implements LoggingSupport {

	private final DBDriver driver;
	private final JdbcTemplate jdbcTemplate;
	private final QuerySpecs querySpecs;

	EntryQueryCommand(final DBDriver driver, final JdbcTemplate jdbcTemplate, final QuerySpecs querySpecs) {
		this.driver = driver;
		this.jdbcTemplate = jdbcTemplate;
		this.querySpecs = querySpecs;
	}

	public CMQueryResult run() {
		sqlLogger.debug("executing query from '{}'", QuerySpecs.class);
		final QueryCreator qc = new QueryCreator(querySpecs);
		final String query = qc.getQuery();
		final Object[] params = qc.getParams();
		final ResultFiller rch = new ResultFiller(qc.getColumnMapper(), querySpecs, driver);
		sqlLogger.debug("query: {}", query);
		sqlLogger.debug("params: {}", Arrays.asList(params));
		jdbcTemplate.query(query, params, rch);
		return rch.getResult();
	}

	private static class ResultFiller implements RowCallbackHandler {

		private final ColumnMapper columnMapper;
		private final QuerySpecs querySpecs;
		private final DBDriver driver;

		private final DBQueryResult result;

		public ResultFiller(final ColumnMapper columnMapper, final QuerySpecs querySpecs, final DBDriver driver) {
			this.columnMapper = columnMapper;
			this.querySpecs = querySpecs;
			this.driver = driver;
			this.result = new DBQueryResult();
		}

		@Override
		public void processRow(final ResultSet rs) throws SQLException {
			try {
				result.setTotalSize(rs.getInt(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowsCount)));
			} catch (final SQLException e) {
				result.setTotalSize(0);
			}
			final DBQueryRow row = new DBQueryRow();
			createRowNumber(rs, row);
			createBasicCards(rs, row);
			createBasicRelations(rs, row);
			createFunctionCallOutput(rs, row);
			result.add(row);
		}

		private void createRowNumber(final ResultSet rs, final DBQueryRow row) {
			try {
				row.setNumber(rs.getLong(nameForSystemAttribute(querySpecs.getFromClause().getAlias(), RowNumber)));
			} catch (final SQLException e) {
				// ignored
			}
		}

		private void createFunctionCallOutput(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (final Alias a : columnMapper.getFunctionCallAliases()) {
				final DBFunctionCallOutput out = new DBFunctionCallOutput();
				final CMEntryType hackSinceAFunctionCanAppearOnlyInTheFromClause = querySpecs.getFromClause().getType();
				for (final EntryTypeAttribute eta : columnMapper.getAttributes(a,
						hackSinceAFunctionCanAppearOnlyInTheFromClause)) {
					final Object sqlValue = rs.getObject(eta.index);
					out.set(eta.name, eta.sqlType.sqlToJavaValue(sqlValue));
				}
				row.setFunctionCallOutput(a, out);
			}
		}

		private void createBasicCards(final ResultSet rs, final DBQueryRow row) throws SQLException {
			sqlLogger.trace("creating cards");

			for (final Alias alias : columnMapper.getClassAliases()) {
				if (columnMapper.getExternalReferenceAliases().contains(alias.toString())) {
					continue;
				}
				sqlLogger.trace("creating card for alias '{}'", alias);
				// Always extract a Long for the Id even if it's integer
				final Long id = rs.getLong(nameForSystemAttribute(alias, Id));
				final Long classId = rs.getLong(nameForSystemAttribute(alias, IdClass));
				final DBClass realClass = driver.findClass(classId);
				if (realClass == null) {
					sqlLogger.trace("class not found for id '{}', skipping creation", classId);
					continue;
				}
				sqlLogger.trace("real class for id '{}' is '{}'", classId, realClass.getIdentifier());
				final DBCard card = DBCard.newInstance(driver, realClass, id);

				card.setUser(rs.getString(nameForSystemAttribute(alias, User)));
				card.setBeginDate(getDateTime(rs, nameForSystemAttribute(alias, BeginDate)));
				card.setEndDate(getDateTime(rs, nameForSystemAttribute(alias, EndDate)));

				addUserAttributes(alias, card, rs);
				row.setCard(alias, card);
			}
		}

		private void createBasicRelations(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (final Alias alias : columnMapper.getDomainAliases()) {
				final Long id = rs.getLong(nameForSystemAttribute(alias, Id));
				final Long domainId = rs.getLong(nameForSystemAttribute(alias, DomainId));
				final String querySource = rs.getString(nameForSystemAttribute(alias, DomainQuerySource));
				final DBDomain realDomain = driver.findDomain(domainId);
				if (realDomain == null) {
					sqlLogger.trace("domain not found for id '{}', skipping creation", domainId);
					continue;
				}
				final DBRelation relation = DBRelation.newInstance(driver, realDomain, id);

				relation.setUser(rs.getString(nameForSystemAttribute(alias, User)));
				relation.setBeginDate(getDateTime(rs, nameForSystemAttribute(alias, BeginDate)));
				relation.setEndDate(getDateTime(rs, nameForSystemAttribute(alias, EndDate)));
				final Long idObject1 = rs.getLong(nameForSystemAttribute(alias, DomainId1));
				final Long idObject2 = rs.getLong(nameForSystemAttribute(alias, DomainId2));
				relation.setCard1Id(idObject1);
				relation.setCard2Id(idObject2);

				addUserAttributes(alias, relation, rs);

				final QueryRelation queryRelation = QueryRelation.newInstance(relation, querySource);
				row.setRelation(alias, queryRelation);
			}
		}

		private DateTime getDateTime(final ResultSet rs, final String attributeAlias) throws SQLException {
			try {
				final java.sql.Timestamp ts = rs.getTimestamp(attributeAlias);
				if (ts != null) {
					return new DateTime(ts.getTime());
				} else {
					return null;
				}
			} catch (final SQLException ex) {
				return null;
			}
		}

		private void addUserAttributes(final Alias typeAlias, final DBEntry entry, final ResultSet rs)
				throws SQLException {
			sqlLogger.trace("adding user attributes for entry of type '{}' with alias '{}'", //
					entry.getType().getIdentifier(), typeAlias);

			final Iterable<EntryTypeAttribute> attributes = columnMapper.getAttributes(typeAlias, entry.getType());
			if (attributes != null) {
				for (final EntryTypeAttribute attribute : attributes) {
					if (attribute.name != null) {
						final DBAttribute dbAttribute = entry.getType().getAttribute(attribute.name);
						final CMAttributeType<?> attributeType = dbAttribute.getType();

						Object value;
						if (mustBeMappedWithIdAndDescription(attributeType)) {
							final Long id = valueId(rs, attribute);
							final String description = valueDescription(rs, dbAttribute);

							if (isLookup(attributeType)) {
								final String type = ((LookupAttributeType) attributeType).getLookupTypeName(); //
								value = new LookupValue( //
										id, //
										description, //
										type //
								);

							} else {
								value = new IdAndDescription(id, description);
							}

						} else {
							value = rs.getObject(attribute.index);
						}

						entry.setOnly(attribute.name, attribute.sqlType.sqlToJavaValue(value));
					} else {
						// skipping, not belonging to this entry type
					}
				}
			}
		}

		/**
		 * @param rs
		 * @param dbAttribute
		 * @return
		 */
		private String valueDescription(final ResultSet rs, final DBAttribute dbAttribute) {
			String description = null;
			final String referenceAttributeAlias = referenceAttributeAlias(dbAttribute);
			try {
				/**
				 * FIXME: ugly solution introduced to prevent that an exception
				 * in reading reference description, blocks the task of filling
				 * card attributes
				 */
				description = rs.getString(referenceAttributeAlias);
			} catch (final Exception ex) {
				sqlLogger.warn("cannot get content of column '{}'", referenceAttributeAlias);
			}
			return description;
		}

		/**
		 * @param dbAttribute
		 * @return
		 */
		private String referenceAttributeAlias(final DBAttribute dbAttribute) {
			final String referenceAttributeAlias = new ExternalReferenceAliasHandler(querySpecs.getFromClause()
					.getType(), dbAttribute).forResult();
			return referenceAttributeAlias;
		}

		/**
		 * @param rs
		 * @param attribute
		 * @return
		 * @throws SQLException
		 */
		private Long valueId(final ResultSet rs, final EntryTypeAttribute attribute) throws SQLException {
			final Long externalReferenceId = rs.getLong(attribute.index) == 0 ? null : rs.getLong(attribute.index);
			return externalReferenceId;
		}

		private boolean isLookup(final CMAttributeType<?> attributeType) {
			return attributeType instanceof LookupAttributeType;
		}

		private boolean mustBeMappedWithIdAndDescription(final CMAttributeType<?> attributeType) {
			return isLookup(attributeType) //
					|| attributeType instanceof ReferenceAttributeType //
					|| attributeType instanceof ForeignKeyAttributeType;
		}

		private CMQueryResult getResult() {
			return result;
		}
	}

}
