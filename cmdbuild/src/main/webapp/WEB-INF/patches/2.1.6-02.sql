-- Add indexes for all classes/tables

DROP FUNCTION IF EXISTS _cm_string_agg(anyarray) CASCADE;
CREATE FUNCTION _cm_string_agg(anyarray)
	RETURNS text LANGUAGE SQL AS
$func$
	SELECT case when trim(array_to_string($1, ', ')) = '' THEN null else array_to_string($1, ', ') END
$func$;

DROP AGGREGATE IF EXISTS _cm_string_agg(anyelement) CASCADE;
CREATE AGGREGATE _cm_string_agg(anyelement) (
	SFUNC     = array_append
	,STYPE     = anyarray
	,INITCOND  = '{}'
	,FINALFUNC = _cm_string_agg
);

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

DROP FUNCTION IF EXISTS _cm_create_class_default_order_indexes(character varying);
CREATE OR REPLACE FUNCTION _cm_create_class_default_order_indexes(IN cmclass character varying, OUT always_true boolean) RETURNS boolean AS $$
BEGIN
	PERFORM _cm_create_class_default_order_indexes(_cm_table_id(cmclass));
	always_true = TRUE;
END;
$$ LANGUAGE PLPGSQL;
COMMENT ON FUNCTION _cm_create_class_default_order_indexes(character varying) IS 'TYPE: function|CATEGORIES: system';

DROP FUNCTION IF EXISTS _cm_function_list();
CREATE OR REPLACE FUNCTION _cm_function_list(
		OUT function_name text,
		OUT function_id oid,
		OUT arg_io char[],
		OUT arg_names text[],
		OUT arg_types text[],
		OUT returns_set boolean,
		OUT comment text
	) RETURNS SETOF record AS $$
DECLARE
	R record;
	i integer;
BEGIN
	FOR R IN
		SELECT *
		FROM pg_proc
		WHERE _cm_comment_for_cmobject(oid) IS NOT NULL
	LOOP
		function_name := R.proname::text;
		function_id := R.oid;
		returns_set := R.proretset;
		comment := _cm_comment_for_cmobject(R.oid);
		IF R.proargmodes IS NULL
		THEN
			arg_io := '{}'::char[];
			arg_types := '{}'::text[];
			arg_names := '{}'::text[];
			-- add input columns
			FOR i IN SELECT generate_series(1, array_upper(R.proargtypes,1)) LOOP
				arg_io := arg_io || 'i'::char;
				arg_types := arg_types || _cm_get_sqltype_string(R.proargtypes[i], NULL);
				arg_names := arg_names || COALESCE(R.proargnames[i], '$'||i);
			END LOOP;
			-- add single output column
			arg_io := arg_io || 'o'::char;
			arg_types := arg_types || _cm_get_sqltype_string(R.prorettype, NULL);
			arg_names := arg_names || function_name;
		ELSE
			-- just normalize existing columns
			arg_io := R.proargmodes;
			arg_types := '{}'::text[];
			arg_names := R.proargnames;
			FOR i IN SELECT generate_series(1, array_upper(arg_io,1)) LOOP
				-- normalize table output
				IF arg_io[i] = 't' THEN
					arg_io[i] := 'o';
				ELSIF arg_io[i] = 'b' THEN
					arg_io[i] := 'io';
				END IF;
				arg_types := arg_types || _cm_get_sqltype_string(R.proallargtypes[i], NULL);
				IF arg_names[i] = '' THEN
					IF arg_io[i] = 'i' THEN
						arg_names[i] = '$'||i;
					ELSE
						arg_names[i] = 'column'||i;
					END IF;
				END IF;
			END LOOP;
		END IF;
		RETURN NEXT;
	END LOOP;

	RETURN;
END
$$ LANGUAGE PLPGSQL STABLE;

CREATE OR REPLACE FUNCTION patch_216_02() RETURNS void AS $$
DECLARE
	id regclass;
BEGIN
	FOR id IN
		SELECT table_id
			FROM _cm_class_list() AS table_id
			WHERE _cm_check_comment(_cm_comment_for_table_id(table_id), 'TYPE', 'simpleclass') is false
	LOOP
		PERFORM _cm_create_class_default_order_indexes(id::oid);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_216_02();
DROP FUNCTION patch_216_02();