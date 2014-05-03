package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entry.DBEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class EntryDeleteCommand {

	private final SimpleJdbcCall call;
	private final SqlParameterSource in;

	public EntryDeleteCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		this.call = new SimpleJdbcCall(jdbcTemplate) //
				.withProcedureName("cm_delete_card");
		this.in = new MapSqlParameterSource() //
				.addValue("cardid", entry.getId()) //
				.addValue("tableid", entry.getType().getId());
	}

	public void execute() {
		call.execute(in);
	}

}
