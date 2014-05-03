package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;
import static org.cmdbuild.common.Constants.LOOKUP_CLASS_NAME;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.driver.postgres.quote.IdentQuoter;
import org.cmdbuild.dao.entry.DBEntry;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

public class EntryUpdateCommand extends EntryCommand implements LoggingSupport {

	private final List<AttributeValueType> attributesToBeUpdated;

	public EntryUpdateCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		super(jdbcTemplate, entry);
		this.attributesToBeUpdated = userAttributesFor(entry);
	}

	public void execute() {
		final String sql = format("UPDATE %s SET %s WHERE %s = ?", //
				EntryTypeQuoter.quote(entry().getType()), //
				columns(), //
				IdentQuoter.quote(entry().getType().getKeyAttributeName()));
		final Object[] arguments = arguments();

		logOnlyLookupUpdate();

		jdbcTemplate().update(sql, arguments);
	}

	private String columns() {
		final List<String> columns = Lists.newArrayList();
		for (final AttributeValueType attributeValueType : attributesToBeUpdated) {
			String sqlCast;
			sqlCast = SqlType.getSqlType(attributeValueType.getType()).sqlCast();
			if (sqlCast == null) {
				columns.add(format("%s = ?%s", IdentQuoter.quote(attributeValueType.getName()), ""));
			} else {
				columns.add(format("%s = ?%s", IdentQuoter.quote(attributeValueType.getName()), "::" + sqlCast));
			}
		}
		return join(columns, ", ");
	}

	private Object[] arguments() {
		final List<Object> arguments = Lists.newArrayList();
		for (final AttributeValueType avt : attributesToBeUpdated) {
			if (!(avt.getValue() instanceof String[])) {
				arguments.add(avt.getValue());
			} else {
				try {
					arguments.add(new PostgreSQLArray((String[]) avt.getValue()));
				} catch (final Exception ex) {
				}

			}
		}
		arguments.add(entry().getId());
		return arguments.toArray();
	}

	private void logOnlyLookupUpdate() {
		if (entry().getType().getName().equals(LOOKUP_CLASS_NAME)) {
			final String insertStringToLog = "UPDATE " + EntryTypeQuoter.quote(entry().getType()) + " SET ";
			final StringBuilder sb = new StringBuilder(insertStringToLog);
			for (final AttributeValueType avt : attributesToBeUpdated) {
				sb.append(IdentQuoter.quote(avt.getName()));
				sb.append(" = ");
				sb.append(avt.getValue() != null ? "'" : "");
				sb.append(avt.getValue());
				sb.append(avt.getValue() != null ? "'" : "");
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1); // to delete the last comma
			sb.append(" WHERE " + IdentQuoter.quote(entry().getType().getKeyAttributeName()));
			sb.append(" = ");
			sb.append(entry().getId());
			sb.append(";");
			dataDefinitionSqlLogger.info(sb.toString());
		}
	}

}
