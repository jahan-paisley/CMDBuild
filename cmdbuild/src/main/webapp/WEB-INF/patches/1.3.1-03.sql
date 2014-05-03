-- Don't change the relation ID in the history table

CREATE OR REPLACE FUNCTION _cm_trigger_create_relation_history_row() RETURNS trigger AS $$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Status" = 'U';
		OLD."EndDate" = now();
-- PostgreSQL 8.4
--		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
--			' ('||_cm_attribute_list_cs(TG_RELID)||') VALUES ($1.*)' USING OLD;
-- PostgreSQL 8.3
		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
			' ('||_cm_attribute_list_cs(TG_RELID)||')' ||
			' VALUES (' ||
			' (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').*)';
	ELSIF (TG_OP='DELETE') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;