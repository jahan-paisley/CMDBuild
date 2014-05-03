--
-- Name handling
--

CREATE OR REPLACE FUNCTION _cm_split_cmname(CMName text) RETURNS text[] AS $$
    SELECT regexp_matches($1,E'(?:([^\\.]+)\\.)?([^\\.]+)?');
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_cmschema(CMName text) RETURNS text AS $$
	SELECT (_cm_split_cmname($1))[1];
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_cmtable(CMName text) RETURNS text AS $$
	SELECT (_cm_split_cmname($1))[2];
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_cmschema(TableId oid) RETURNS text AS $$
	SELECT pg_namespace.nspname::text FROM pg_class
	JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid
	WHERE pg_class.oid=$1
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_cmtable(TableId oid) RETURNS text AS $$
	SELECT pg_class.relname::text FROM pg_class	WHERE pg_class.oid=$1
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_join_cmname(CMSchema name, CMTable name) RETURNS text AS $$
	SELECT $1 || '.' || $2;
$$ LANGUAGE SQL IMMUTABLE;


CREATE OR REPLACE FUNCTION _cm_domain_cmname(CMDomain text) RETURNS text AS $$
	SELECT coalesce(_cm_cmschema($1)||'.','')||coalesce('Map_'||_cm_cmtable($1),'Map');
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_history_cmname(CMClass text) RETURNS text AS $$
	SELECT $1 || '_history';
$$ LANGUAGE SQL IMMUTABLE;


CREATE OR REPLACE FUNCTION _cm_table_dbname_unsafe(CMName text) RETURNS text AS $$
	SELECT coalesce(quote_ident(_cm_cmschema($1))||'.','')||quote_ident(_cm_cmtable($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_table_dbname(CMName text) RETURNS regclass AS $$
	SELECT _cm_table_dbname_unsafe($1)::regclass;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_table_id(CMName text) RETURNS oid AS $$
	SELECT _cm_table_dbname_unsafe($1)::regclass::oid;
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_domain_dbname_unsafe(CMDomain text) RETURNS text AS $$
	SELECT _cm_table_dbname_unsafe(_cm_domain_cmname($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_domain_dbname(CMDomain text) RETURNS regclass AS $$
	SELECT _cm_table_dbname(_cm_domain_cmname($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_domain_id(CMDomain text) RETURNS oid AS $$
	SELECT _cm_table_id(_cm_domain_cmname($1));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;


CREATE OR REPLACE FUNCTION _cm_history_dbname_unsafe(CMTable text) RETURNS text AS $$
	SELECT _cm_table_dbname_unsafe(_cm_history_cmname($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_history_dbname(CMTable text) RETURNS regclass AS $$
	SELECT _cm_table_dbname(_cm_history_cmname($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_history_id(CMTable text) RETURNS oid AS $$
	SELECT _cm_table_id(_cm_history_cmname($1));
$$ LANGUAGE SQL STABLE;

/*
 * Constraint names
 */

CREATE OR REPLACE FUNCTION _cm_cmtable_lc(CMName text) RETURNS text AS $$
	SELECT lower(_cm_cmtable($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_cmtable_lc(TableId oid) RETURNS text AS $$
	SELECT lower(_cm_cmtable($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_classpk_name(CMClassName text) RETURNS text AS $$
	SELECT _cm_cmtable($1) || '_pkey';
$$ LANGUAGE SQL IMMUTABLE;

-- Remove the underscore to comply with history index names
CREATE OR REPLACE FUNCTION _cm_classidx_name(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT 'idx_' || REPLACE(_cm_cmtable_lc($1), '_', '') || '_' || lower($2);
$$ LANGUAGE SQL IMMUTABLE;

-- Do not remove the underscore (should be the default!)
CREATE OR REPLACE FUNCTION _cm_domainidx_name(DomainId oid, Type text) RETURNS text AS $$
	SELECT 'idx_' || _cm_cmtable_lc($1) || '_' || lower($2);
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_classfk_name(CMClassName text, AttributeName text) RETURNS text AS $$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_classfk_name(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$$ LANGUAGE SQL IMMUTABLE;


CREATE OR REPLACE FUNCTION _cm_domain_cmname_lc(CMDomainName text) RETURNS text AS $$
	SELECT lower(_cm_domain_cmname($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_domainpk_name(CMDomainName text) RETURNS text AS $$
	SELECT _cm_classpk_name(_cm_domain_cmname($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_historypk_name(CMClassName text) RETURNS text AS $$
	SELECT _cm_classpk_name(_cm_history_cmname($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_historyfk_name(CMClassName text, AttributeName text) RETURNS text AS $$
	SELECT _cm_classfk_name(_cm_history_cmname($1), $2);
$$ LANGUAGE SQL IMMUTABLE;


CREATE OR REPLACE FUNCTION _cm_unique_index_name(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT '_Unique_'|| _cm_cmtable($1) ||'_'|| $2;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_unique_index_id(TableId oid, AttributeName text) RETURNS oid AS $$
	SELECT (
		quote_ident(_cm_cmschema($1))
		||'.'||
		quote_ident(_cm_unique_index_name($1, $2))
	)::regclass::oid;
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_update_reference_trigger_name(RefTableId oid, RefAttribute text) RETURNS text AS $$
	SELECT '_UpdRef_'|| _cm_cmtable($1) ||'_'|| $2;
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_update_relation_trigger_name(RefTableId oid, RefAttribute text) RETURNS text AS $$
	SELECT '_UpdRel_'|| _cm_cmtable($1) ||'_'|| $2;
$$ LANGUAGE SQL IMMUTABLE;

-- TODO: Change this name!
CREATE OR REPLACE FUNCTION _cm_notnull_constraint_name(AttributeName text) RETURNS text AS $$
	SELECT '_NotNull_'||$1;
$$ LANGUAGE SQL IMMUTABLE;

/*
 * Utility functions
 */

CREATE OR REPLACE FUNCTION _cm_comment_for_table_id(TableId oid) RETURNS text AS $$
	SELECT description FROM pg_description WHERE objoid = $1;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_read_comment(Comment text, Key text) RETURNS text AS $$
	SELECT TRIM(SUBSTRING($1 FROM E'(?:^|\\|)'||$2||E':[ ]*([^\\|]+)'));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_comment_for_cmobject(TableId oid) RETURNS text AS $$
	SELECT description FROM pg_description
	WHERE objoid = $1 AND objsubid = 0 AND _cm_read_comment(description, 'TYPE') IS NOT NULL LIMIT 1;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_cmobject(TableId oid) RETURNS boolean AS $$
	SELECT _cm_comment_for_cmobject($1) IS NOT NULL;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_comment_for_attribute(TableId oid, AttributeName text) RETURNS text AS $$
SELECT description
FROM pg_description
JOIN pg_attribute ON pg_description.objoid = pg_attribute.attrelid AND pg_description.objsubid = pg_attribute.attnum
WHERE attrelid = $1 and attname = $2 LIMIT 1;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_comment_for_class(CMClass text) RETURNS text AS $$
	SELECT _cm_comment_for_table_id(_cm_table_id($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_comment_for_domain(CMDomain text) RETURNS text AS $$
	SELECT _cm_comment_for_table_id(_cm_domain_id($1));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_check_comment(ClassComment text, Key text, Value text) RETURNS BOOLEAN AS $$
	SELECT (_cm_read_comment($1, $2) ILIKE $3);
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_read_domain_cardinality(AttributeComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'CARDIN');
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_domain_cardinality(DomainId oid) RETURNS text AS $$
	SELECT _cm_read_domain_cardinality(_cm_comment_for_table_id($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_domain_direction(DomainId oid) RETURNS boolean AS $$
DECLARE
	Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	IF Cardinality = 'N:1' THEN
		RETURN TRUE;
	ELSIF Cardinality = '1:N' THEN
		RETURN FALSE;
	ELSE
		RETURN NULL;
	END IF;
END
$$ LANGUAGE PLPGSQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_get_attribute_default(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT pg_attrdef.adsrc
		FROM pg_attribute JOIN pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_get_sqltype_string(SqlTypeId oid, TypeMod integer) RETURNS text AS $$
	SELECT pg_type.typname::text || COALESCE(
			CASE
				WHEN pg_type.typname IN ('varchar','bpchar') THEN '(' || $2 - 4 || ')'
				WHEN pg_type.typname = 'numeric' THEN '(' ||
					$2 / 65536 || ',' ||
					$2 - $2 / 65536 * 65536 - 4|| ')'
			END, '')
		FROM pg_type WHERE pg_type.oid = $1;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_get_attribute_sqltype(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT _cm_get_sqltype_string(pg_attribute.atttypid, pg_attribute.atttypmod)
		FROM pg_attribute
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_attribute_is_notnull(TableId oid, AttributeName text) RETURNS boolean AS $$
SELECT pg_attribute.attnotnull OR c.oid IS NOT NULL
FROM pg_attribute
LEFT JOIN pg_constraint AS c
	ON c.conrelid = pg_attribute.attrelid
	AND c.conname::text = _cm_notnull_constraint_name(pg_attribute.attname::text)
WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION _cm_attribute_is_inherited(TableId oid, AttributeName text) RETURNS boolean AS $$
	SELECT pg_attribute.attinhcount <> 0
	FROM pg_attribute
	WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION _cm_subclassid(SuperClassId oid, CardId integer) RETURNS oid AS $$
DECLARE
	Out integer;
BEGIN
	EXECUTE 'SELECT tableoid FROM '||SuperClassId::regclass||' WHERE "Id"='||CardId||' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$ LANGUAGE plpgsql STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_dest_classid_for_domain_attribute(DomainId oid, AttributeName text) RETURNS oid AS $$
	SELECT _cm_table_id(
		_cm_read_comment(
			_cm_comment_for_table_id($1),
			CASE $2
			WHEN 'IdObj1' THEN
				'CLASS1'
			WHEN 'IdObj2' THEN
				'CLASS2'
			ELSE
				NULL
			END
		)
	);
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_dest_reference_classid(DomainId oid, RefIdColumn text, RefId integer) RETURNS oid AS $$
	SELECT _cm_subclassid(_cm_dest_classid_for_domain_attribute($1, $2), $3)
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

/*
 * @param DomainComment comment for domain
 * @return CMClass if the domain is used for a reference
 */
CREATE OR REPLACE FUNCTION _cm_get_domain_reference_target_comment(DomainComment text) RETURNS text AS $$
	SELECT CASE _cm_read_domain_cardinality($1)
		WHEN '1:N' THEN _cm_read_comment($1, 'CLASS1')
		WHEN 'N:1' THEN _cm_read_comment($1, 'CLASS2')
		ELSE NULL
	END
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_read_reference_domain_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'REFERENCEDOM');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_read_reference_domain_id_comment(AttributeComment text) RETURNS oid AS $$
	SELECT _cm_domain_id(_cm_read_reference_domain_comment($1));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;


CREATE OR REPLACE FUNCTION _cm_get_reference_domain_id(TableId oid, AttributeName text) RETURNS oid AS $$
	SELECT _cm_read_reference_domain_id_comment(_cm_comment_for_attribute($1, $2));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_reference_comment(AttributeComment text) RETURNS boolean AS $$
	SELECT COALESCE(_cm_read_reference_domain_comment($1),'') != '';
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

/*
 * @param AttributeComment comment for reference attribute
 * @return CMClass that the reference points to
 */
CREATE OR REPLACE FUNCTION _cm_read_reference_target_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_get_domain_reference_target_comment(_cm_comment_for_domain(_cm_read_reference_domain_comment($1)));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_read_reference_target_id_comment(AttributeComment text) RETURNS oid AS $$
	SELECT _cm_table_id(_cm_read_reference_target_comment($1));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

/*
 * @return reference constraint type (defaults to restrict)
 */
CREATE OR REPLACE FUNCTION _cm_read_reference_type_comment(AttributeComment text) RETURNS text AS $$
	SELECT COALESCE(NULLIF(_cm_read_comment($1, 'REFERENCETYPE'), ''), 'restrict');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_get_fk_target_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'FKTARGETCLASS');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

/*
 * Note: A reference is a FK associated to a domain, so it has also a FK target
 */
CREATE OR REPLACE FUNCTION _cm_get_fk_target(TableId oid, AttributeName text) RETURNS text AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN COALESCE(
		_cm_get_fk_target_comment(AttributeComment),
		_cm_read_reference_target_comment(AttributeComment)
	);
END
$$ LANGUAGE PLPGSQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_get_fk_target_table_id(TableId oid, AttributeName text) RETURNS oid AS $$ BEGIN
	RETURN _cm_table_id(_cm_get_fk_target($1, $2));
END $$ LANGUAGE PLPGSQL STABLE RETURNS NULL ON NULL INPUT;


CREATE OR REPLACE FUNCTION _cm_get_ref_source_id_domain_attribute(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj1'
		WHEN FALSE THEN 'IdObj2'
		ELSE NULL
	END;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_get_ref_source_class_domain_attribute(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdClass1'
		WHEN FALSE THEN 'IdClass2'
		ELSE NULL
	END;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_get_ref_target_id_domain_attribute(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj2'
		WHEN FALSE THEN 'IdObj1'
		ELSE NULL
	END;
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_get_lookup_type_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'LOOKUP');
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION _cm_get_type_comment(ClassComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'TYPE');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;


-- is class or simpleclass
CREATE OR REPLACE FUNCTION _cm_is_any_class_comment(ClassComment text) RETURNS boolean AS $$
	SELECT _cm_check_comment($1, 'TYPE', '%class');
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_simpleclass_comment(ClassComment text) RETURNS boolean AS $$
	SELECT _cm_check_comment($1, 'TYPE', 'simpleclass');
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_domain_comment(ClassComment text) RETURNS boolean AS $$
	SELECT _cm_check_comment($1, 'TYPE', 'domain');
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_active_comment(ClassComment text) RETURNS boolean AS $$
	SELECT _cm_check_comment($1, 'STATUS', 'active');
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_superclass_comment(ClassComment text) RETURNS boolean AS $$
	SELECT _cm_check_comment($1, 'SUPERCLASS', 'true');
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_is_simpleclass(CMClass text) RETURNS boolean AS $$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_class($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_simpleclass(ClassId oid) RETURNS boolean AS $$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_table_id($1))
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_superclass(CMClass text) RETURNS boolean AS $$
	SELECT _cm_is_superclass_comment(_cm_comment_for_class($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_superclass(ClassId oid) RETURNS boolean AS $$
	SELECT _cm_is_superclass_comment(_cm_comment_for_table_id($1));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_any_class(ClassId oid) RETURNS boolean AS $$
	SELECT _cm_is_any_class_comment(_cm_comment_for_table_id($1))
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_system(TableId oid) RETURNS BOOLEAN AS $$
	SELECT _cm_check_comment(_cm_comment_for_table_id($1), 'MODE', 'reserved')
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_get_geometry_type(TableId oid, Attribute text) RETURNS text AS $$
DECLARE
	GeoType text;
BEGIN
	SELECT geometry_columns.type INTO GeoType
	FROM pg_attribute
	LEFT JOIN geometry_columns
		ON f_table_schema = _cm_cmschema($1)
		AND f_table_name = _cm_cmtable($1)
		AND f_geometry_column = $2
	WHERE attrelid = $1 AND attname = $2 AND attnum > 0 AND atttypid > 0;
	RETURN GeoType;
EXCEPTION WHEN undefined_table THEN
	RETURN NULL;
END
$$ LANGUAGE PLPGSQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_geometry_type(CMAttributeType text) RETURNS boolean AS $$
	SELECT $1 IN ('POINT','LINESTRING','POLYGON');
$$ LANGUAGE SQL STABLE;

/*
 * @return All cmdbuild class ids
 */
CREATE OR REPLACE FUNCTION _cm_class_list() RETURNS SETOF oid AS $$
	SELECT oid FROM pg_class WHERE _cm_is_any_class_comment(_cm_comment_for_cmobject(oid));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_subtables_and_itself(TableId oid) RETURNS SETOF oid AS $$
	SELECT $1 WHERE _cm_is_cmobject($1)
	UNION
	SELECT _cm_subtables_and_itself(inhrelid) FROM pg_inherits WHERE inhparent = $1
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION _cm_parent_id(TableId oid) RETURNS SETOF oid AS $$
	SELECT COALESCE((SELECT inhparent FROM pg_inherits WHERE inhrelid = $1 AND _cm_is_cmobject(inhparent) LIMIT 1), NULL);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION _cm_is_process(ClassId oid) RETURNS boolean AS $$
	SELECT $1 IN (SELECT _cm_subtables_and_itself(_cm_table_id('Activity')));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_is_process(CMClass text) RETURNS boolean AS $$
	SELECT _cm_is_process(_cm_table_id($1));
$$ LANGUAGE SQL STABLE;

/*
 * @return All cmdbuild domain ids
 */
CREATE OR REPLACE FUNCTION _cm_domain_list() RETURNS SETOF oid AS $$
	SELECT oid FROM pg_class WHERE _cm_is_domain_comment(_cm_comment_for_cmobject(oid));
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_attribute_list(TableId oid) RETURNS SETOF text AS $$
	SELECT attname::text FROM pg_attribute WHERE attrelid = $1 AND attnum > 0 AND atttypid > 0 ORDER BY attnum;
$$ LANGUAGE SQL STABLE;

/*
 * @return a list of CSV column names
 * 
 * It would not be needed if the database had been designed
 * correctly without having a separate history table
 */
CREATE OR REPLACE FUNCTION _cm_attribute_list_cs(ClassId oid) RETURNS text AS $$
	SELECT array_to_string(array(
		SELECT quote_ident(name) FROM _cm_attribute_list($1) AS name
	),',');
$$ LANGUAGE SQL STABLE;

/*
 * @return the join of schema and table name
 */
CREATE OR REPLACE FUNCTION _cm_join_cmname(CMSchema name, CMTable name) RETURNS text AS $$
	SELECT $1 || '.' || $2;
$$ LANGUAGE SQL IMMUTABLE;

-- nextval('class_seq') fails if class_seq was not declared
CREATE OR REPLACE FUNCTION _cm_new_card_id() RETURNS integer AS $$
	SELECT nextval(('class_seq'::text)::regclass)::integer;
$$ LANGUAGE SQL VOLATILE;


CREATE OR REPLACE FUNCTION _cm_create_index(TableId oid, AttributeName text) RETURNS void AS $$
BEGIN
	EXECUTE 'CREATE INDEX ' || quote_ident(_cm_classidx_name(TableId, AttributeName)) ||
		' ON ' || TableId::regclass ||
		' USING btree (' || quote_ident(AttributeName) || ')';
EXCEPTION
	WHEN undefined_column THEN
		RAISE LOG 'Index for attribute %.% not created because the attribute does not exist',
			TableId::regclass, quote_ident(AttributeName);
END
$$ LANGUAGE PLPGSQL;


-- string concatenation for Postgres < 9.0
CREATE FUNCTION _cm_string_agg(anyarray)
	RETURNS text LANGUAGE SQL AS
$func$
	SELECT case when trim(array_to_string($1, ', ')) = '' THEN null else array_to_string($1, ', ') END
$func$;


CREATE AGGREGATE _cm_string_agg(anyelement) (
	SFUNC     = array_append
	,STYPE     = anyarray
	,INITCOND  = '{}'
	,FINALFUNC = _cm_string_agg
);


CREATE OR REPLACE FUNCTION _cm_get_safe_classorder(IN tableid regclass, IN attname character varying, OUT classorder integer) RETURNS integer AS $$
BEGIN
	SELECT 
		INTO classorder 
		CASE WHEN (coalesce(_cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER'), '')<>'') THEN _cm_read_comment(_cm_comment_for_attribute(tableid, attname), 'CLASSORDER')::integer
		ELSE 0 END;
END;
$$ LANGUAGE PLPGSQL;


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


CREATE OR REPLACE FUNCTION _cm_trigger_when(tgtype int2) RETURNS text AS $$
	SELECT CASE $1 & cast(2 as int2)
         WHEN 0 THEN 'AFTER'
         ELSE 'BEFORE'
       END || ' ' ||
       CASE $1 & cast(28 as int2)
         WHEN 16 THEN 'UPDATE'
         WHEN  8 THEN 'DELETE'
         WHEN  4 THEN 'INSERT'
         WHEN 20 THEN 'INSERT OR UPDATE'
         WHEN 28 THEN 'INSERT OR UPDATE OR DELETE'
         WHEN 24 THEN 'UPDATE OR DELETE'
         WHEN 12 THEN 'INSERT OR DELETE'
       END;
$$ LANGUAGE SQL IMMUTABLE;


CREATE OR REPLACE FUNCTION _cm_trigger_row_or_statement(tgtype int2) RETURNS text AS $$
	SELECT CASE $1 & cast(1 as int2)
         WHEN 0 THEN 'STATEMENT'
         ELSE 'ROW'
       END;
$$ LANGUAGE SQL IMMUTABLE;


CREATE OR REPLACE FUNCTION _cm_copy_trigger(FromId oid, ToId oid, TriggerNameMatcher text) RETURNS void AS $$
DECLARE
	TriggerData record;
BEGIN
	FOR TriggerData IN
		SELECT
			t.tgname AS TriggerName,
			t.tgtype AS TriggerType,
			p.proname AS TriggerFunction,
			array_to_string(array(
				SELECT quote_literal(q.param)
					FROM (SELECT regexp_split_to_table(encode(tgargs, 'escape'), E'\\\\000') AS param) AS q
					WHERE q.param <> ''
			),',') AS TriggerParams
		FROM pg_trigger t, pg_proc p
		WHERE tgrelid = FromId AND tgname LIKE TriggerNameMatcher AND t.tgfoid = p.oid
	LOOP
		EXECUTE '
			CREATE TRIGGER '|| quote_ident(TriggerData.TriggerName) ||'
				'|| _cm_trigger_when(TriggerData.TriggerType) ||'
				ON '|| ToId::regclass ||'
				FOR EACH '|| _cm_trigger_row_or_statement(TriggerData.TriggerType) ||'
				EXECUTE PROCEDURE '|| quote_ident(TriggerData.TriggerFunction) ||'('|| TriggerData.TriggerParams ||')
		';
	END LOOP;
END;
$$ LANGUAGE PLPGSQL;


/*
 * @trigger _CreateHistoryRow
 * @on Regular classes
 * @when AFTER UPDATE OR DELETE
 */
CREATE OR REPLACE FUNCTION _cm_trigger_create_card_history_row() RETURNS trigger AS $$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Id" = _cm_new_card_id();
		OLD."Status" = 'U';
-- PostgreSQL 8.4
--		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
--			' ('||_cm_attribute_list_cs(TG_RELID)||',"CurrentId","EndDate")' ||
--			' VALUES ($1.*, $2."Id", now())' USING OLD, NEW;
-- PostgreSQL 8.3
		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
			' ('||_cm_attribute_list_cs(TG_RELID)||',"CurrentId","EndDate")' ||
			' VALUES (' ||
			' (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').*, ' ||
			' (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ')."Id", now())';
	ELSIF (TG_OP='DELETE') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_class_history_trigger(TableId oid) RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CreateHistoryRow"
			AFTER DELETE OR UPDATE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_create_card_history_row()
	';
END;
$$ LANGUAGE plpgsql;

/*
 * @trigger _CreateHistoryRow
 * @on Domains
 * @when AFTER UPDATE OR DELETE
 */
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

CREATE OR REPLACE FUNCTION _cm_add_domain_history_trigger(DomainId oid) RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CreateHistoryRow"
			AFTER DELETE OR UPDATE
			ON '|| DomainId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_create_relation_history_row()
	';
END;
$$ LANGUAGE PLPGSQL;

/*
 * @trigger _SanityCheck
 * @on Regular classes and domains
 * @when BEFORE INSERT OR UPDATE OR DELETE
 */
CREATE OR REPLACE FUNCTION _cm_trigger_sanity_check() RETURNS trigger AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		IF (NEW."Status"='N' AND OLD."Status"='N') THEN -- Deletion of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='INSERT') THEN
		IF (NEW."Status" IS NULL) THEN
			NEW."Status"='A';
		ELSIF (NEW."Status"='N') THEN -- Creation of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		NEW."Id" = _cm_new_card_id();
		-- Class ID is needed because of the history tables
		BEGIN
			NEW."IdClass" = TG_RELID;
		EXCEPTION WHEN undefined_column THEN
			NEW."IdDomain" = TG_RELID;
		END;
	ELSE -- TG_OP='DELETE'
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- 'U' is reserved for history tables only
	IF (position(NEW."Status" IN 'AND') = 0) THEN -- Invalid status
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	NEW."BeginDate" = now();
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_class_sanity_check_trigger(TableId oid) RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check();
	';
END;
$$
  LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_domain_sanity_check_trigger(DomainId oid) RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| DomainId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check();
	';
END
$$ LANGUAGE PLPGSQL;

/*
 * @trigger _SanityCheck
 * @on Simple classes
 * @when BEFORE INSERT OR UPDATE OR DELETE
 */
CREATE OR REPLACE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='DELETE') THEN
		-- RETURN NEW would return NULL forbidding the operation
		RETURN OLD;
	ELSE
		NEW."BeginDate" = now();
		NEW."IdClass" = TG_RELID;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_simpleclass_sanity_check_trigger(TableId oid) RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();
	';
END;
$$
  LANGUAGE PLPGSQL;


/*
 * Note: It should delete relations of card OLD."Id" on every domain the
 * class has but for now we handle domains between two classes only and
 * with the same column names (IdObj1/IdObj2 IdClass1/IdClass2)
 * 
 * @trigger _CascadeDeleteOnRelations
 * @on Regular classes
 * @when AFTER UPDATE
 */
CREATE OR REPLACE FUNCTION _cm_trigger_cascade_delete_on_relations() RETURNS trigger AS $$
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (NEW."Status"='N') THEN
		UPDATE "Map" SET "Status"='N'
			WHERE "Status"='A' AND (
				("IdObj1" = OLD."Id" AND "IdClass1" = TG_RELID)
				OR ("IdObj2" = OLD."Id" AND "IdClass2" = TG_RELID)
			);
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_class_cascade_delete_on_relations_trigger(TableId oid) RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CascadeDeleteOnRelations"
			AFTER UPDATE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();
	';
END;
$$ LANGUAGE PLPGSQL;


/*
 * @trigger _Constr_<FKeyClass>_<FKeyAttribute>
 * @on Classes target of a foreign key (or a reference)
 * @when BEFORE UPDATE OR DELETE
 */
CREATE OR REPLACE FUNCTION _cm_trigger_restrict() RETURNS trigger AS $$
DECLARE
	TableId oid := TG_ARGV[0]::regclass::oid;
	AttributeName text := TG_ARGV[1];
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (TG_OP='UPDATE' AND NEW."Status"='N') THEN
		PERFORM _cm_restrict(OLD."Id", TableId, AttributeName);
	END IF;
	RETURN NEW;
END;
$$	LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_restrict_trigger(FKTargetClassId oid, FKClassId oid, FKAttribute text) RETURNS void AS $$
BEGIN
	IF FKClassId IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) || '
			BEFORE UPDATE OR DELETE
			ON ' || FKTargetClassId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_restrict(' ||
					quote_literal(FKClassId::regclass) || ',' ||
					quote_literal(FKAttribute) ||
				');
	';
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _cm_copy_restrict_trigger(FromId oid, ToId oid) RETURNS void AS $$
	SELECT _cm_copy_trigger($1, $2, '_Constr_%');
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION _cm_remove_constraint_trigger(FKTargetClassId oid, FKClassId oid, FKAttribute text) RETURNS void AS $$
BEGIN
	EXECUTE '
		DROP TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) ||
			' ON ' || FKTargetClassId::regclass || ';
	';
END;
$$ LANGUAGE PLPGSQL;


/*
 * @trigger UpdRel_<ReferenceClass>_<ReferenceAttribute>
 * @on Class where the reference is defined and every subclass
 * @when AFTER INSERT OR UPDATE
 */
CREATE OR REPLACE FUNCTION _cm_trigger_update_relation() RETURNS trigger AS $$
DECLARE
	AttributeName text := TG_ARGV[0];
	DomainId oid := TG_ARGV[1]::regclass::oid;
	CardColumn text := TG_ARGV[2]; -- Domain column name for the card id
	RefColumn text := TG_ARGV[3];  -- Domain column name for the reference id

	OldRefValue integer;
	NewRefValue integer;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (TG_OP = 'UPDATE') THEN
--		EXECUTE 'SELECT ($1).' || quote_ident(AttributeName) INTO OldRefValue USING OLD; -- pg84
		EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(AttributeName) INTO OldRefValue;
	END IF;
--	EXECUTE 'SELECT ($1).' || quote_ident(AttributeName) INTO NewRefValue USING NEW; -- pg84
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(AttributeName) INTO NewRefValue;

	IF (NewRefValue IS NOT NULL) THEN
		IF (OldRefValue IS NOT NULL) THEN
			IF (OldRefValue <> NewRefValue) THEN
				PERFORM _cm_update_relation(NEW."User", DomainId, CardColumn, NEW."Id", RefColumn, NewRefValue);
			END IF;
		ELSE
			PERFORM _cm_insert_relation(NEW."User", DomainId, CardColumn, NEW."Id", RefColumn, NewRefValue, TG_RELID);
		END IF;
	ELSE
		IF (OldRefValue IS NOT NULL) THEN
			PERFORM _cm_delete_relation(NEW."User", DomainId, CardColumn, NEW."Id");
		END IF;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_update_relation_trigger(TableId oid, RefTableId oid, RefAttribute text) RETURNS void AS $$
DECLARE
	DomainId oid := _cm_get_reference_domain_id(RefTableId, RefAttribute);
	DomainSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(RefTableId, RefAttribute);
	DomainTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(RefTableId, RefAttribute);
BEGIN
	IF DomainId IS NULL OR DomainSourceIdAttribute IS NULL OR DomainTargetIdAttribute IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_update_relation_trigger_name(RefTableId, RefAttribute)) || '
			AFTER INSERT OR UPDATE
			ON ' || TableId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_update_relation(' ||
				quote_literal(RefAttribute) || ',' ||
				quote_literal(DomainId::regclass) || ',' ||
				quote_literal(DomainSourceIdAttribute) || ',' ||
				quote_literal(DomainTargetIdAttribute) ||
			');
	';
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_copy_update_relation_trigger(FromId oid, ToId oid) RETURNS void AS $$
	SELECT _cm_copy_trigger($1, $2, '_UpdRel_%');
$$ LANGUAGE SQL;


/*
 * @trigger <FKeyClass>_<FKeyAttribute>_fkey
 * @on Foreign Key source
 * @when BEFORE UPDATE OR INSERT
 */
CREATE OR REPLACE FUNCTION _cm_trigger_fk() RETURNS trigger AS $$
DECLARE
	SourceAttribute text := TG_ARGV[0];
	TargetClassId oid := TG_ARGV[1]::regclass::oid;
	TriggerVariant text := TG_ARGV[2];
	RefValue integer;
	ActiveCardsOnly boolean;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
--	EXECUTE 'SELECT ($1).' || quote_ident(SourceAttribute) INTO RefValue USING NEW; -- pg84
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(SourceAttribute) INTO RefValue;

	IF (TriggerVariant = 'simple') THEN
		ActiveCardsOnly := FALSE;
	ELSE
		ActiveCardsOnly := NEW."Status" <> 'A';
	END IF;

	IF NOT _cm_check_id_exists(RefValue, TargetClassId, ActiveCardsOnly) THEN
		RETURN NULL;
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_fk_trigger(TableId oid, FKSourceId oid, FKAttribute text, FKTargetId oid) RETURNS void AS $$
DECLARE
	TriggerVariant text;
BEGIN
	IF _cm_is_simpleclass(FKSourceId) THEN
		TriggerVariant := 'simple';
	ELSE
		TriggerVariant := '';
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_classfk_name(FKSourceId, FKAttribute)) || '
			BEFORE INSERT OR UPDATE
			ON ' || TableId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_fk('||
				quote_literal(FKAttribute) || ',' ||
				quote_literal(FKTargetId::regclass) || ',' ||
				quote_literal(TriggerVariant) ||
			');
	';
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_copy_fk_trigger(FromId oid, ToId oid) RETURNS void AS $$
	SELECT _cm_copy_trigger($1, $2, '%_fkey');
$$ LANGUAGE SQL;


/*
 * @trigger UpdRef_<ReferenceClass>_<ReferenceAttribute>
 * @on Domain used by a reference
 * @when AFTER UPDATE OR INSERT
 */
CREATE OR REPLACE FUNCTION _cm_trigger_update_reference() RETURNS trigger AS $$
DECLARE
	AttributeName text := TG_ARGV[0];
	TableId oid := TG_ARGV[1]::regclass::oid;
	CardColumn text := TG_ARGV[2]; -- Domain column name for the card id
	RefColumn text := TG_ARGV[3];  -- Domain column name for the reference id

	OldCardId integer;
	NewCardId integer;
	OldRefValue integer;
	NewRefValue integer;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (NEW."Status"='A') THEN
--		EXECUTE 'SELECT ($1).' || quote_ident(RefColumn) INTO NewRefValue USING NEW; -- pg84
		EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(RefColumn) INTO NewRefValue;
	ELSIF (NEW."Status"<>'N') THEN
		-- Ignore history rows
		RETURN NEW;
	END IF;

--	EXECUTE 'SELECT ($1).' || quote_ident(CardColumn) INTO NewCardId USING NEW; -- pg84
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(CardColumn) INTO NewCardId;

	IF (TG_OP='UPDATE') THEN
--		EXECUTE 'SELECT ($1).' || quote_ident(CardColumn) INTO OldCardId USING OLD; -- pg84
		EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(CardColumn) INTO OldCardId;
		IF (OldCardId <> NewCardId) THEN -- If the non-reference side changes...
			PERFORM _cm_update_reference(TableId, AttributeName, OldCardId, NULL);
			-- OldRefValue is kept null because it is like a new relation
		ELSE
--			EXECUTE 'SELECT ($1).' || quote_ident(CardColumn) INTO OldRefValue USING OLD; -- pg84
			EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(RefColumn) INTO OldRefValue;
		END IF;
	END IF;

	IF ((NewRefValue IS NULL) OR (OldRefValue IS NULL) OR (OldRefValue <> NewRefValue)) THEN
		PERFORM _cm_update_reference(TableId, AttributeName, NewCardId, NewRefValue);
	END IF;

	RETURN NEW;
END;
$$	LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION _cm_add_update_reference_trigger(TableId oid, RefAttribute text) RETURNS void AS $$
DECLARE
	DomainId oid := _cm_get_reference_domain_id(TableId, RefAttribute);
	DomainSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, RefAttribute);
	DomainTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, RefAttribute);
BEGIN
	IF DomainId IS NULL OR DomainSourceIdAttribute IS NULL OR DomainTargetIdAttribute IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_update_reference_trigger_name(TableId, RefAttribute)) || '
			AFTER INSERT OR UPDATE
			ON ' || DomainId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_update_reference(' ||
					quote_literal(RefAttribute) || ',' ||
					quote_literal(TableId::regclass) || ',' ||
					quote_literal(DomainSourceIdAttribute) || ',' ||
					quote_literal(DomainTargetIdAttribute) ||
				');
	';
END;
$$ LANGUAGE PLPGSQL;

--
-- A temporary sequence is local to every connection,
-- so it is guaranteed not to be accessed concurrently
--

CREATE OR REPLACE FUNCTION _cm_zero_rownum_sequence() RETURNS VOID AS $$
DECLARE
	temp BIGINT;
BEGIN
	SELECT INTO temp setval('rownum', 0, true);
EXCEPTION WHEN undefined_table THEN
	CREATE TEMPORARY SEQUENCE rownum MINVALUE 0 START 1;
END
$$ LANGUAGE PLPGSQL;

-- 
-- Useful functions for patches and such
-- 

CREATE OR REPLACE FUNCTION _cm_disable_triggers_recursively(SuperClass regclass) RETURNS VOID AS $$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::regclass ||' DISABLE TRIGGER USER';
	END LOOP;
END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_enable_triggers_recursively(SuperClass regclass) RETURNS VOID AS $$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::text ||' ENABLE TRIGGER USER';
	END LOOP;
END;
$$
LANGUAGE PLPGSQL;
