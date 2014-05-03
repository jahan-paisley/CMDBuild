-- Add indexes for all classes/tables

DROP FUNCTION IF EXISTS _cm_get_safe_classorder(regclass, character varying);
CREATE OR REPLACE FUNCTION _cm_get_safe_classorder(IN tableid regclass, IN attname character varying, OUT classorder integer) RETURNS integer AS $$
BEGIN
	SELECT 
		INTO classorder 
		CASE WHEN (coalesce(_cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER'), '')<>'') THEN _cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER')::integer
		ELSE 0 END;
END;
$$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS _cm_create_class_default_order_indexes(oid);
CREATE OR REPLACE FUNCTION _cm_create_class_default_order_indexes(tableid oid) RETURNS void AS $$
DECLARE
	classindex text;
	sqlcommand text;
BEGIN
	SELECT INTO classindex coalesce(_cm_string_agg(attname || ' ' || ordermode), '"Description" asc')
	FROM (
		SELECT quote_ident(attname) AS attname, abs(_cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER')::integer), CASE WHEN (_cm_get_safe_classorder(tableid, attname) > 0) THEN 'asc' ELSE 'desc' END AS ordermode
		FROM (
			SELECT _cm_attribute_list(tableid) AS attname) AS a
				WHERE _cm_get_safe_classorder(tableid, attname) <> 0
				ORDER by 2
	) AS b;
	RAISE NOTICE '% %', tableid::regclass, classindex;

	sqlcommand = 'DROP INDEX IF EXISTS idx_' || REPLACE(_cm_cmtable_lc(tableid), '_', '') || '_defaultorder;';
	RAISE NOTICE '... %', sqlcommand;
	EXECUTE sqlcommand;

	sqlcommand = 'CREATE INDEX idx_' || REPLACE(_cm_cmtable_lc(tableid), '_', '') || '_defaultorder' || ' ON ' || tableid::regclass || ' USING btree (' || classindex || ', "Id" asc);';
	RAISE NOTICE '... %', sqlcommand;
	EXECUTE sqlcommand;
END;
$$ LANGUAGE PLPGSQL;
