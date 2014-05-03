-- Functions needed by the new schema reports

CREATE OR REPLACE FUNCTION _cm_is_process(ClassId oid) RETURNS boolean AS $$
	SELECT $1 IN (SELECT _cm_subtables_and_itself(_cm_table_id('Activity')));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_process(CMClass text) RETURNS boolean AS $$
	SELECT _cm_is_process(_cm_table_id($1));
$$ LANGUAGE SQL STABLE;
