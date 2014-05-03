-- Function and trigger massive update (might take a long time)

/**************************************************************************
 * Drop legacy views                                                      *
 **************************************************************************/

DROP VIEW system_privilegescatalog;
DROP VIEW system_availablemenuitems;
DROP VIEW system_treecatalog;
DROP VIEW system_attributecatalog;
DROP VIEW system_classcatalog;
DROP VIEW system_domaincatalog;
DROP VIEW system_inheritcatalog;
DROP VIEW system_relationlist;
DROP VIEW system_relationlist_history;

/**************************************************************************
 * Username should be varchar(40)                                         *
 **************************************************************************/

ALTER TABLE "Class" ALTER COLUMN "User" TYPE varchar(40);
ALTER TABLE "Map" ALTER COLUMN "User" TYPE varchar(40);
ALTER TABLE "Report" ALTER COLUMN "User" TYPE varchar(40);

/**************************************************************************
 * Increase Lookup Types name                                             *
 **************************************************************************/

ALTER TABLE "LookUp" ALTER "Type" TYPE character varying(64);
ALTER TABLE "LookUp" ALTER "ParentType" TYPE character varying(64);

/**************************************************************************
 * Drop legacy functions                                                  *
 **************************************************************************/

DROP FUNCTION system_class_create(varchar, varchar, boolean, varchar);
DROP FUNCTION system_class_modify(integer, varchar, boolean, varchar);
DROP FUNCTION system_class_delete(varchar);
DROP FUNCTION system_attribute_create(varchar, varchar, varchar, varchar, boolean, boolean, varchar, varchar, varchar, varchar, boolean);
DROP FUNCTION system_attribute_modify(varchar, varchar, varchar, varchar, varchar, boolean, boolean, varchar);
DROP FUNCTION system_attribute_delete(varchar, varchar);
DROP FUNCTION system_domain_create(varchar, varchar, varchar, varchar);
DROP FUNCTION system_domain_modify(integer, varchar, varchar, varchar, varchar);
DROP FUNCTION system_domain_delete(varchar);

DROP FUNCTION system_attribute_comment(varchar, varchar, varchar);
DROP FUNCTION system_attribute_delete_recursion(varchar, varchar, boolean);
DROP FUNCTION system_attribute_isempty(varchar, varchar);
DROP FUNCTION system_attribute_ismandatory(varchar, varchar);
DROP FUNCTION system_attribute_isunique(varchar, varchar);
DROP FUNCTION system_attribute_makeunique(varchar, varchar, boolean);
DROP FUNCTION system_attribute_setmandatory(varchar, varchar, boolean);
DROP FUNCTION system_check_comment(varchar, varchar, varchar);
DROP FUNCTION system_class_createaftertriggers(varchar);
DROP FUNCTION system_class_createattributecomment(varchar, varchar);
DROP FUNCTION system_class_createhistory(varchar);
DROP FUNCTION system_class_createindex(varchar);
DROP FUNCTION system_class_createreferencetrigger(varchar, varchar);
DROP FUNCTION system_class_createrestricttriggers(varchar, varchar);
DROP FUNCTION system_class_createtriggers(varchar);
DROP FUNCTION system_class_deletetriggers(varchar);
DROP FUNCTION system_class_haschild(varchar);
DROP FUNCTION system_class_hasdomains(varchar);
DROP FUNCTION system_class_hasreferencefk(varchar, varchar);
DROP FUNCTION system_class_hasreferencetrigger(varchar, varchar);
DROP FUNCTION system_class_hastrigger(varchar);
DROP FUNCTION system_class_isempty(varchar);
DROP FUNCTION system_class_isprocess(varchar);
DROP FUNCTION system_class_issuperclass(varchar);
DROP FUNCTION system_class_recreateaftertriggers(varchar);
DROP FUNCTION system_class_recreateaftertriggersrecursive(varchar);
DROP FUNCTION system_deletereference(varchar, varchar);
DROP FUNCTION system_disablealltriggers();
DROP FUNCTION system_domain_createindex(varchar, text);
DROP FUNCTION system_domain_createtriggers(varchar);
DROP FUNCTION system_droptriggersrecursive(varchar, varchar);
DROP FUNCTION system_enablealltriggers();
DROP FUNCTION system_getmenucode(boolean, boolean, boolean, boolean);
DROP FUNCTION system_getmenutype(boolean, boolean, boolean, boolean);
DROP FUNCTION system_isexecutor(oid, integer, varchar);
DROP FUNCTION system_read_comment(varchar, varchar);
DROP FUNCTION system_reference_create(varchar, varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createcascaderelationtriggers(varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createpkandtriggers(varchar, varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createrecursive(varchar, varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createrelationpk(varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createrelationtriggersrecursive(varchar, varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createreferencetrigger(varchar, varchar);
DROP FUNCTION system_reference_createrestrictrelationtriggers(varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_createsetnullrelationtriggers(varchar, varchar, varchar, varchar);
DROP FUNCTION system_reference_deleted(integer, integer, integer, integer, varchar, integer, boolean);
DROP FUNCTION system_reference_inserted(integer, integer, integer, integer, varchar, integer, boolean);
DROP FUNCTION system_reference_logicdelete(integer, integer, integer, integer, varchar, integer, boolean);
DROP FUNCTION system_reference_updated(integer, integer, integer, integer, varchar, integer, boolean);
DROP FUNCTION system_regeneratereferencetriggers();
DROP FUNCTION system_relation_deleted(varchar, varchar, integer);
DROP FUNCTION system_relation_getreferencevalue(varchar, integer, varchar);
DROP FUNCTION system_relation_getvalue(varchar, integer, varchar);
DROP FUNCTION system_relation_inserted(varchar, varchar, integer, integer);
DROP FUNCTION system_relation_logicdelete(varchar, varchar, integer, integer);
DROP FUNCTION system_relation_updated(varchar, varchar, integer, integer, integer, integer);
DROP FUNCTION system_replacereference(varchar, varchar, varchar, varchar, varchar);
DROP FUNCTION system_trigger_exists(varchar);
DROP FUNCTION system_updateallaftertriggers();
DROP FUNCTION system_updateregclasses();
DROP FUNCTION zero_rownum_sequence();

/**************************************************************************
 * Database setup 01-04,06,09                                             *
 **************************************************************************/

/**************************************************************************
 * Database setup 01                                                      *
 **************************************************************************/

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

CREATE OR REPLACE FUNCTION _cm_cmtable_lc(CMName text) RETURNS text AS $$
	SELECT lower(_cm_cmtable($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_cmtable_lc(TableId oid) RETURNS text AS $$
	SELECT lower(_cm_cmtable($1));
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_classpk_name(CMClassName text) RETURNS text AS $$
	SELECT _cm_cmtable($1) || '_pkey';
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_classidx_name(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT 'idx_' || REPLACE(_cm_cmtable_lc($1), '_', '') || '_' || lower($2);
$$ LANGUAGE SQL IMMUTABLE;

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

CREATE OR REPLACE FUNCTION _cm_notnull_constraint_name(AttributeName text) RETURNS text AS $$
	SELECT '_NotNull_'||$1;
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION _cm_comment_for_table_id(TableId oid) RETURNS text AS $$
	SELECT description FROM pg_description WHERE objoid = $1;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_read_comment(Comment text, Key text) RETURNS text AS $$
	SELECT SUBSTRING($1 FROM E'(?:^|\\|)'||$2||E':[ ]*([^ \\|]+)');
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

CREATE OR REPLACE FUNCTION _cm_get_attribute_sqltype(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT pg_type.typname::text || CASE
				WHEN pg_type.typname IN ('varchar','bpchar') THEN '(' || pg_attribute.atttypmod - 4 || ')'
				WHEN pg_type.typname = 'numeric' THEN '(' ||
					pg_attribute.atttypmod / 65536 || ',' ||
					pg_attribute.atttypmod - pg_attribute.atttypmod / 65536 * 65536 - 4|| ')'
				ELSE ''
			END
		FROM pg_attribute JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
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

CREATE OR REPLACE FUNCTION _cm_read_reference_target_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_get_domain_reference_target_comment(_cm_comment_for_domain(_cm_read_reference_domain_comment($1)));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_read_reference_target_id_comment(AttributeComment text) RETURNS oid AS $$
	SELECT _cm_table_id(_cm_read_reference_target_comment($1));
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_read_reference_type_comment(AttributeComment text) RETURNS text AS $$
	SELECT COALESCE(_cm_read_comment($1, 'REFERENCETYPE'),'restrict');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

CREATE OR REPLACE FUNCTION _cm_get_fk_target_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'FKTARGETCLASS');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

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

CREATE OR REPLACE FUNCTION _cm_get_ref_target_id_domain_attribute(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj2'
		WHEN FALSE THEN 'IdObj1'
		ELSE NULL
	END;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_get_ref_target_class_domain_attribute(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdClass2'
		WHEN FALSE THEN 'IdClass1'
		ELSE NULL
	END;
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_get_lookup_type_comment(AttributeComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'LOOKUP');
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION _cm_get_type_comment(ClassComment text) RETURNS text AS $$
	SELECT _cm_read_comment($1, 'TYPE');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;

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

CREATE OR REPLACE FUNCTION _cm_class_list() RETURNS SETOF oid AS $$
	SELECT oid FROM pg_class WHERE _cm_is_any_class_comment(_cm_comment_for_cmobject(oid));
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_subtables_and_itself(TableId oid) RETURNS SETOF oid AS $$
	SELECT $1 WHERE _cm_is_cmobject($1)
	UNION
	SELECT _cm_subtables_and_itself(inhrelid) FROM pg_inherits WHERE inhparent = $1
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION _cm_parent_id(TableId oid) RETURNS SETOF oid AS $$
	SELECT inhparent FROM pg_inherits WHERE inhrelid = $1 AND _cm_is_cmobject(inhparent);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION _cm_domain_list() RETURNS SETOF oid AS $$
	SELECT oid FROM pg_class WHERE _cm_is_domain_comment(_cm_comment_for_cmobject(oid));
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_attribute_list(TableId oid) RETURNS SETOF text AS $$
	SELECT attname::text FROM pg_attribute WHERE attrelid = $1 AND attnum > 0 AND atttypid > 0 ORDER BY attnum;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_attribute_list_cs(ClassId oid) RETURNS text AS $$
	SELECT array_to_string(array(
		SELECT quote_ident(name) FROM _cm_attribute_list($1) AS name
	),',');
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_join_cmname(CMSchema name, CMTable name) RETURNS text AS $$
	SELECT $1 || '.' || $2;
$$ LANGUAGE SQL IMMUTABLE;

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

CREATE OR REPLACE FUNCTION _cm_trigger_create_relation_history_row() RETURNS trigger AS $$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Id" = _cm_new_card_id();
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

CREATE OR REPLACE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='DELETE') THEN
		-- RETURN NEW would return NULL forbidding the operation
		RETURN OLD;
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

CREATE OR REPLACE FUNCTION _cm_zero_rownum_sequence() RETURNS VOID AS $$
DECLARE
	temp BIGINT;
BEGIN
	SELECT INTO temp setval('rownum', 0, true);
EXCEPTION WHEN undefined_table THEN
	CREATE TEMPORARY SEQUENCE rownum MINVALUE 0 START 1;
END
$$ LANGUAGE PLPGSQL;

/**************************************************************************
 * Database setup 02                                                      *
 **************************************************************************/

CREATE OR REPLACE FUNCTION _cm_copy_superclass_attribute_comments(
		TableId oid,
		ParentTableId oid
) RETURNS void AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT * FROM _cm_attribute_list(ParentTableId)
	LOOP
		EXECUTE 'COMMENT ON COLUMN '|| TableId::regclass || '.' || quote_ident(AttributeName) ||
			' IS '|| quote_literal(_cm_comment_for_attribute(ParentTableId, AttributeName));
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_create_class_indexes(TableId oid)
	RETURNS void AS $$
BEGIN
	PERFORM _cm_create_index(TableId, 'Code');
	PERFORM _cm_create_index(TableId, 'Description');
	PERFORM _cm_create_index(TableId, 'IdClass');
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_create_class_history(CMClassName text)
	RETURNS void AS $$
BEGIN
	EXECUTE '
		CREATE TABLE '|| _cm_history_dbname_unsafe(CMClassName) ||'
		(
			"CurrentId" int4 NOT NULL,
			"EndDate" timestamp NOT NULL DEFAULT now(),
			CONSTRAINT ' || quote_ident(_cm_historypk_name(CMClassName)) ||' PRIMARY KEY ("Id"),
			CONSTRAINT '|| quote_ident(_cm_historyfk_name(CMClassName, 'CurrentId')) ||' FOREIGN KEY ("CurrentId")
				REFERENCES '||_cm_table_dbname(CMClassName)||' ("Id") ON UPDATE RESTRICT ON DELETE SET NULL
		) INHERITS ('||_cm_table_dbname(CMClassName)||');
	';
	PERFORM _cm_create_index(_cm_history_id(CMClassName), 'CurrentId');
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_create_class_triggers(TableId oid) RETURNS void AS $$
BEGIN
	IF _cm_is_superclass(TableId) THEN
		RAISE DEBUG 'Not creating triggers for class %', TableId::regclass;
	ELSIF _cm_is_simpleclass(TableId) THEN
		PERFORM _cm_add_simpleclass_sanity_check_trigger(TableId);
	ELSE
		PERFORM _cm_add_class_sanity_check_trigger(TableId);
		PERFORM _cm_add_class_history_trigger(TableId);
		PERFORM _cm_add_class_cascade_delete_on_relations_trigger(TableId);
	END IF;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_table_is_empty(TableId oid) RETURNS boolean AS $$
DECLARE
	NotFound boolean;
BEGIN
	-- Note: FOUND variable is not set on EXECUTE, so we can't use it!
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||' LIMIT 1' INTO NotFound;
	RETURN NotFound;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_class_has_children(TableId oid) RETURNS boolean AS $$
-- Without separate history table
--	SELECT (COUNT(*) > 0) FROM pg_inherits WHERE inhparent = $1 LIMIT 1;
-- With history table design mistake
	SELECT (COUNT(*) > 0) FROM pg_inherits WHERE inhparent = $1 AND _cm_is_cmobject(inhrelid) LIMIT 1;
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_class_has_domains(TableId oid) RETURNS boolean AS $$
	SELECT (COUNT(*) > 0) FROM _cm_domain_list() AS d
	WHERE _cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS1')) = $1 OR
		_cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS2')) = $1;
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION _cm_create_schema_if_needed(CMName text) RETURNS void AS $$
BEGIN
	IF _cm_cmschema(CMName) IS NOT NULL THEN
		EXECUTE 'CREATE SCHEMA '||quote_ident(_cm_cmschema(CMName));
	END IF;
EXCEPTION
	WHEN duplicate_schema THEN
		RETURN;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_attribute_root_table_id(TableId oid, AttributeName text) RETURNS oid AS $$
DECLARE
	CurrentTableId oid := TableId;
BEGIN
	LOOP
	    EXIT WHEN CurrentTableId IS NULL OR _cm_attribute_is_local(CurrentTableId, AttributeName);
		CurrentTableId := _cm_parent_id(CurrentTableId);
	END LOOP;
	RETURN CurrentTableId;
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_propagate_superclass_triggers(TableId oid) RETURNS void AS $$
DECLARE
	ParentId oid := _cm_parent_id(TableId);
BEGIN
	PERFORM _cm_copy_restrict_trigger(ParentId, TableId);
	PERFORM _cm_copy_update_relation_trigger(ParentId, TableId);
	PERFORM _cm_copy_fk_trigger(ParentId, TableId);
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_delete_local_attributes_or_triggers(TableId oid) RETURNS void AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT _cm_attribute_list(TableId) LOOP
		IF _cm_attribute_is_inherited(TableId, AttributeName) THEN
			PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);
		ELSE
			PERFORM cm_delete_attribute(TableId, AttributeName);
		END IF;
	END LOOP;
END
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION cm_create_class(
		CMClass text,
		ParentId oid,
		ClassComment text)
	RETURNS integer AS $$
DECLARE
	IsSimpleClass boolean := _cm_is_simpleclass_comment(ClassComment);
	TableId oid;
BEGIN
	IF (IsSimpleClass AND ParentId IS NOT NULL) OR (NOT _cm_is_any_class_comment(ClassComment))
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	-- TODO: Check if the superclass is a superclass

	PERFORM _cm_create_schema_if_needed(CMClass);

	DECLARE
		DBClassName text := _cm_table_dbname_unsafe(CMClass);
		InheritancePart text;
		AttributesPart text;
	BEGIN
		IF ParentId IS NULL THEN
			AttributesPart := '
				"Id" integer NOT NULL DEFAULT _cm_new_card_id(),
			';
			InheritancePart := '';
		ELSE
			AttributesPart := '';
			InheritancePart := ' INHERITS ('|| ParentId::regclass ||')';
		END IF;
		EXECUTE 'CREATE TABLE '|| DBClassName ||
			'('|| AttributesPart ||
				' CONSTRAINT '|| quote_ident(_cm_classpk_name(CMClass)) ||' PRIMARY KEY ("Id")'||
			')' || InheritancePart;
		EXECUTE 'COMMENT ON TABLE '|| DBClassName ||' IS '|| quote_literal(ClassComment);
		EXECUTE 'COMMENT ON COLUMN '|| DBClassName ||'."Id" IS '|| quote_literal('MODE: reserved');
		TableId := _cm_table_id(CMClass);
	END;

	PERFORM _cm_copy_superclass_attribute_comments(TableId, ParentId);

	PERFORM _cm_create_class_triggers(TableId);

	IF ParentId IS NULL THEN
		IF NOT IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'IdClass', 'regclass', NULL, TRUE, FALSE, 'MODE: reserved');
			PERFORM cm_create_attribute(TableId, 'Code', 'varchar(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true');
			PERFORM cm_create_attribute(TableId, 'Description', 'varchar(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true');
			-- Status is the only attribute needed
			PERFORM cm_create_attribute(TableId, 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: reserved');
		END IF;
		PERFORM cm_create_attribute(TableId, 'User', 'varchar(40)', NULL, FALSE, FALSE, 'MODE: reserved');
		IF IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'BeginDate', 'timestamp', 'now()', TRUE, FALSE, 'MODE: write|FIELDMODE: read|BASEDSP: true');
		ELSE
			PERFORM cm_create_attribute(TableId, 'BeginDate', 'timestamp', 'now()', TRUE, FALSE, 'MODE: reserved');
			PERFORM cm_create_attribute(TableId, 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Notes|INDEX: 3');
		END IF;
	ELSE
	    PERFORM _cm_propagate_superclass_triggers(TableId);
	END IF;

	IF IsSimpleClass THEN
		PERFORM _cm_create_index(TableId, 'BeginDate');
	ELSE
		PERFORM _cm_create_class_indexes(TableId);
		IF NOT _cm_is_superclass_comment(ClassComment) THEN
			PERFORM _cm_create_class_history(CMClass);
		END IF;
	END IF;

	RETURN TableId::integer;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION cm_modify_class(TableId oid, NewComment text) RETURNS void AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(TableId);
BEGIN
	IF _cm_is_superclass_comment(OldComment) <> _cm_is_superclass_comment(NewComment)
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	EXECUTE 'COMMENT ON TABLE ' || TableId::regclass || ' IS ' || quote_literal(NewComment);
END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION cm_delete_class(TableId oid) RETURNS void AS $$
BEGIN
	IF _cm_class_has_domains(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: has domains', TableId::regclass;
	ELSEIF _cm_class_has_children(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: has childs', TableId::regclass;
	ELSEIF NOT _cm_table_is_empty(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: contains data', TableId::regclass;
	END IF;

	PERFORM _cm_delete_local_attributes_or_triggers(TableId);

	-- Cascade for the history table
	EXECUTE 'DROP TABLE '|| TableId::regclass ||' CASCADE';
END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION cm_create_class(CMClass text, CMParentClass text, ClassComment text) RETURNS integer AS $$
	SELECT cm_create_class($1, _cm_table_id($2), $3);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION cm_modify_class(CMClass text, NewComment text) RETURNS void AS $$
	SELECT cm_modify_class(_cm_table_id($1), $2);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION cm_delete_class(CMClass text) RETURNS void AS $$
	SELECT cm_delete_class(_cm_table_id($1));
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION system_class_create(
		ClassName varchar,
		ParentClass varchar,
		IsSuperClass boolean,
		ClassComment varchar)
	RETURNS integer AS $$
BEGIN
	-- consistency checks for wrong signatures
	IF IsSuperClass <> _cm_is_superclass_comment(ClassComment) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	RETURN cm_create_class(ClassName, ParentClass, ClassComment);
END;
$$ LANGUAGE PLPGSQL VOLATILE;


CREATE OR REPLACE FUNCTION system_class_modify(ClassId integer, NewClassName varchar, NewIsSuperClass boolean, NewClassComment varchar) RETURNS boolean AS $$
BEGIN
	IF _cm_cmtable(ClassId) <> NewClassName
		OR _cm_is_superclass_comment(NewClassComment) <> NewIsSuperClass
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_class(ClassId::oid, NewClassComment);
	RETURN TRUE;
END;
$$ LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION system_class_delete(CMClass varchar) RETURNS void AS $$
	SELECT cm_delete_class($1);
$$ LANGUAGE SQL;

/**************************************************************************
 * Database setup 03                                                      *
 **************************************************************************/

CREATE OR REPLACE FUNCTION _cm_restrict(Id integer, TableId oid, AttributeName text) RETURNS VOID AS $$
BEGIN
	IF _cm_check_value_exists($1, $2, $3, FALSE) THEN
--		RAISE restrict_violation; -- pg84
		RAISE EXCEPTION 'CM_RESTRICT_VIOLATION';
	END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_setnull(Id integer, TableId oid, AttributeName text) RETURNS VOID AS $$
BEGIN
	EXECUTE 'UPDATE '|| TableId::regclass ||
		' SET '||quote_ident(AttributeName)||' = NULL'||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_cascade(Id integer, TableId oid, AttributeName text) RETURNS VOID AS $$
BEGIN
	EXECUTE 'DELETE FROM '|| TableId::regclass ||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_check_value_exists(
	Id integer,
	TableId oid,
	AttributeName text,
	DeletedAlso boolean
) RETURNS BOOLEAN AS $$
DECLARE
	Out BOOLEAN := TRUE;
	StatusPart TEXT;
BEGIN
	IF _cm_is_simpleclass(TableId) OR DeletedAlso THEN
		StatusPart := '';
	ELSE
		StatusPart := ' AND "Status"=''A''';
	END IF;
	IF Id IS NOT NULL THEN
		EXECUTE 'SELECT (COUNT(*) > 0) FROM '|| TableId::regclass ||' WHERE '||
		quote_ident(AttributeName)||'='||Id||StatusPart||' LIMIT 1' INTO Out;
	END IF;
	RETURN Out;
END
$$ LANGUAGE PLPGSQL STABLE;


CREATE OR REPLACE FUNCTION _cm_check_id_exists(Id integer, TableId oid, DeletedAlso boolean) RETURNS BOOLEAN AS $$
	SELECT _cm_check_value_exists($1, $2, 'Id', $3);
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_attribute_is_unique(TableId oid, AttributeName text) RETURNS boolean AS $$
DECLARE
	IsUnique boolean;
BEGIN
	SELECT INTO IsUnique (count(*) > 0) FROM pg_class
		JOIN pg_index ON pg_class.oid = pg_index.indexrelid
		WHERE pg_index.indrelid = TableId AND relname = _cm_unique_index_name(TableId, AttributeName);
	RETURN IsUnique;
END;
$$ LANGUAGE PLPGSQL STABLE;


CREATE OR REPLACE FUNCTION _cm_attribute_set_uniqueness_unsafe(TableId oid, AttributeName text, AttributeUnique boolean) RETURNS VOID AS $$
BEGIN
	IF AttributeUnique THEN
		EXECUTE 'CREATE UNIQUE INDEX '||
			quote_ident(_cm_unique_index_name(TableId, AttributeName)) ||
			' ON '|| TableId::regclass ||' USING btree (('||
			' CASE WHEN "Status"::text = ''N''::text THEN NULL'||
			' ELSE '|| quote_ident(AttributeName) || ' END))';
	ELSE
		EXECUTE 'DROP INDEX '|| _cm_unique_index_id(TableId, AttributeName)::regclass;
	END IF;
END
$$ LANGUAGE PLPGSQL VOLATILE;


CREATE OR REPLACE FUNCTION _cm_attribute_set_uniqueness(TableId oid, AttributeName text, AttributeUnique boolean) RETURNS VOID AS $$
BEGIN
	IF _cm_attribute_is_unique(TableId, AttributeName) <> AttributeUnique THEN
		IF AttributeUnique AND (_cm_is_simpleclass(TableId) OR _cm_is_superclass(TableId)) THEN
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: %', 'Superclass or simple class attributes cannot be unique';
		END IF;

		PERFORM _cm_attribute_set_uniqueness_unsafe(TableId, AttributeName, AttributeUnique);
	END IF;
END;
$$ LANGUAGE PLPGSQL VOLATILE;


CREATE OR REPLACE FUNCTION _cm_set_attribute_comment(TableId oid, AttributeName text, Comment text) RETURNS void AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'COMMENT ON COLUMN '|| SubClassId::regclass ||'.'|| quote_ident(AttributeName) ||' IS '|| quote_literal(Comment);
	END LOOP;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_fk_constraints(FKSourceId oid, AttributeName text) RETURNS void AS $$
DECLARE
	FKTargetId oid := _cm_get_fk_target_table_id(FKSourceId, AttributeName);
	SubTableId oid;
BEGIN
	IF FKTargetId IS NULL THEN
		RETURN;
	END IF;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKSourceId) LOOP
		PERFORM _cm_add_fk_trigger(SubTableId, FKSourceId, AttributeName, FKTargetId);
	END LOOP;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKTargetId) LOOP
		PERFORM _cm_add_restrict_trigger(SubTableId, FKSourceId, AttributeName);
	END LOOP;
END;
$$ LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION _cm_remove_fk_constraints(
	FKSourceId oid,
	AttributeName text
) RETURNS void AS $$
DECLARE
	TargetId oid := _cm_get_fk_target_table_id(FKSourceId, AttributeName);
	SubTableId oid;
BEGIN
	IF TargetId IS NULL THEN
		RETURN;
	END IF;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKSourceId) LOOP
		EXECUTE 'DROP TRIGGER '|| quote_ident(_cm_classfk_name(FKSourceId, AttributeName)) ||
			' ON '|| SubTableId::regclass;
	END LOOP;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(TargetId) LOOP
		PERFORM _cm_remove_constraint_trigger(SubTableId, FKSourceId, AttributeName);
	END LOOP;
END;
$$ LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION _cm_attribute_is_local(TableId oid, AttributeName text) RETURNS boolean AS $$
	SELECT (attinhcount = 0) FROM pg_attribute WHERE attrelid = $1 AND attname = $2 LIMIT 1;
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE FUNCTION _cm_remove_attribute_triggers(
	TableId oid,
	AttributeName text
) RETURNS VOID AS $$
BEGIN
	PERFORM _cm_remove_fk_constraints(TableId, AttributeName);
	PERFORM _cm_remove_reference_handling(TableId, AttributeName);
END;
$$ LANGUAGE PLPGSQL VOLATILE;


CREATE OR REPLACE FUNCTION _cm_remove_reference_handling(TableId oid, AttributeName text) RETURNS VOID AS $$
BEGIN
	-- remove UpdRel and UpdRef triggers
	PERFORM _cm_drop_triggers_recursively(
		TableId,
		_cm_update_relation_trigger_name(TableId, AttributeName)
	);
	PERFORM _cm_drop_triggers_recursively(
		_cm_get_reference_domain_id(TableId, AttributeName),
		_cm_update_reference_trigger_name(TableId, AttributeName)
	);
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_attribute_is_empty(TableId oid, AttributeName text) RETURNS boolean AS $$
DECLARE
	Out boolean;
BEGIN
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||
		' WHERE '|| quote_ident(AttributeName) ||' IS NOT NULL' || 
	    ' AND '|| quote_ident(AttributeName) ||'::text <> '''' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_reference_handling(TableId oid, AttributeName text) RETURNS VOID AS $$
DECLARE
	objid integer;
	referencedid integer;
	ctrlint integer;

	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
	ReferenceTargetId oid := _cm_read_reference_target_id_comment(AttributeComment);
	AttributeReferenceType text := _cm_read_reference_type_comment(AttributeComment);
	ReferenceDomainId oid := _cm_read_reference_domain_id_comment(AttributeComment);

	RefSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, AttributeName);
	RefTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);
	RefTargetClassIdAttribute text := _cm_get_ref_target_class_domain_attribute(TableId, AttributeName);

	ChildId oid;
BEGIN
	IF ReferenceTargetId IS NULL OR AttributeReferenceType IS NULL OR ReferenceDomainId IS NULL THEN
		RETURN;
	END IF;

	-- Updates the reference for every relation
	-- TODO: UNDERSTAND WHAT IT DOES AND MAKE IT READABLE!
	FOR objid IN EXECUTE 'SELECT "Id" from '||TableId::regclass||' WHERE "Status"=''A'''
	LOOP
		FOR referencedid IN EXECUTE '
			SELECT '|| quote_ident(RefSourceIdAttribute) ||
			' FROM '|| ReferenceDomainId::regclass ||
			' WHERE '|| quote_ident(RefTargetClassIdAttribute) ||'='|| TableId ||
				' AND '|| quote_ident(RefTargetIdAttribute) ||'='|| objid ||
				' AND "Status"=''A'''
		LOOP
			EXECUTE 'SELECT count(*) FROM '||ReferenceTargetId::regclass||' where "Id"='||referencedid INTO ctrlint;
			IF(ctrlint<>0) THEN
				EXECUTE 'UPDATE '|| TableId::regclass ||
					' SET '|| quote_ident(AttributeName) ||'='|| referencedid ||
					' WHERE "Id"='|| objid;
			END IF;
		END LOOP;
	END LOOP;

	-- Trigger on reference class (reference -> relation)
	FOR ChildId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		PERFORM _cm_add_update_relation_trigger(ChildId, TableId, AttributeName);
	END LOOP;

	-- Trigger on domain (relation -> reference)
	PERFORM _cm_add_update_reference_trigger(TableId, AttributeName);
END;
$$ LANGUAGE PLPGSQL VOLATILE;


CREATE OR REPLACE FUNCTION _cm_drop_triggers_recursively(TableId oid, TriggerName text) RETURNS VOID AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'DROP TRIGGER IF EXISTS '|| quote_ident(TriggerName) ||' ON '|| SubClassId::regclass;
	END LOOP;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_update_relation(
	UserName text,
	DomainId oid,
	CardIdColumn text,
	CardId integer,
	RefIdColumn text,
	RefId integer
) RETURNS void AS $$
DECLARE
	RefClassUpdatePart text;
BEGIN
	-- Needed to update IdClassX (if the domain attributres are IdClass1/2)
	RefClassUpdatePart := coalesce(
		', ' || quote_ident('IdClass'||substring(RefIdColumn from E'^IdObj(\\d)+')) || 
			'=' || _cm_dest_reference_classid(DomainId, RefIdColumn, RefId),
		''
	);

-- coalesce(quote_literal(UserName),'NULL') -> quote_nullable(UserName) -- pg84
	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET ' || quote_ident(RefIdColumn) || ' = ' || RefId ||
			', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
			RefClassUpdatePart ||
		' WHERE "Status"=''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _cm_insert_relation(
	UserName text,
	DomainId oid,
	CardIdColumn text,
	CardId integer,
	RefIdColumn text,
	RefId integer,
	CardClassId oid -- Useless, but the DBA loves it
) RETURNS void AS $$
DECLARE
	CardClassIdColumnPart text;
	RefClassIdColumnPart text;
	CardClassIdValuePart text;
	RefClassIdValuePart text;
	StopRecursion boolean;
BEGIN
	IF (CardId IS NULL OR RefId IS NULL) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Needed for backward compatibility
	CardClassIdColumnPart := coalesce(quote_ident('IdClass'||substring(CardIdColumn from '^IdObj(.)+')) || ', ', '');
	RefClassIdColumnPart := coalesce(quote_ident('IdClass'||substring(RefIdColumn from '^IdObj(.)+')) || ', ', '');
	CardClassIdValuePart := CASE WHEN CardClassIdColumnPart IS NOT NULL THEN (coalesce(CardClassId::text, 'NULL') || ', ') ELSE '' END;
	RefClassIdValuePart := coalesce(_cm_dest_reference_classid(DomainId, RefIdColumn, RefId)::text, 'NULL') || ', ';

	-- Stop trigger recursion
	EXECUTE 'SELECT (COUNT(*) > 0) FROM ' || DomainId::regclass ||
		' WHERE' ||
			' "IdDomain" = ' || DomainId::text || -- NOTE: why is this check done?
			' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId::text ||
			' AND ' || quote_ident(RefIdColumn) || ' = ' || RefId::text ||
			' AND "Status" = ''A''' INTO StopRecursion;
	IF NOT StopRecursion THEN
		EXECUTE 'INSERT INTO ' || DomainId::regclass ||
			' (' ||
				'"IdDomain", ' ||
				quote_ident(CardIdColumn) || ', ' ||
				quote_ident(RefIdColumn) || ', ' ||
				CardClassIdColumnPart ||
				RefClassIdColumnPart ||
				'"Status", ' ||
				'"User"' ||
			') VALUES (' ||
				DomainId::text || ', ' ||
				CardId::text || ', ' ||
				RefId::text || ', ' ||
				CardClassIdValuePart ||
				RefClassIdValuePart ||
				'''A'', ' ||
				coalesce(quote_literal(UserName), 'NULL') ||
			')';
	END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _cm_delete_relation(
	UserName text,
	DomainId oid,
	CardIdColumn text,
	CardId integer
) RETURNS void AS $$
DECLARE
BEGIN
-- coalesce(quote_literal(UserName),'NULL') -> quote_nullable(UserName) -- pg84
	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET "Status" = ''N'', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
		' WHERE "Status" = ''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _cm_update_reference(TableId oid, AttributeName text, CardId integer, ReferenceId integer) RETURNS void AS $$
BEGIN
	EXECUTE 'UPDATE ' || TableId::regclass ||
		' SET ' || quote_ident(AttributeName) || ' = ' || coalesce(ReferenceId::text, 'NULL') ||
		' WHERE "Status"=''A'' AND "Id" = ' || CardId::text ||
		' AND ' || quote_ident(AttributeName) || coalesce(' <> ' || ReferenceId::text, 'IS NOT NULL');
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_attribute_default_to_src(
	TableId oid,
	AttributeName text,
	NewDefault text
) RETURNS text AS $$
DECLARE
	SQLType text := _cm_get_attribute_sqltype(TableId, AttributeName);
BEGIN
	IF (NewDefault IS NULL OR TRIM(NewDefault) = '') THEN
		RETURN NULL;
	END IF;

    IF SQLType ILIKE 'varchar%' OR SQLType = 'text' OR
    	((SQLType = 'date' OR SQLType = 'timestamp') AND TRIM(NewDefault) <> 'now()')
    THEN
		RETURN quote_literal(NewDefault);
	ELSE
		RETURN NewDefault;
	END IF;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_set_attribute_default(
	TableId oid,
	AttributeName text,
	NewDefault text,
	UpdateExisting boolean
) RETURNS void AS $$
DECLARE
	CurrentDefaultSrc text := _cm_get_attribute_default(TableId, AttributeName);
	NewDefaultSrc text := _cm_attribute_default_to_src(TableId, AttributeName, NewDefault);
BEGIN
    IF (NewDefaultSrc IS DISTINCT FROM CurrentDefaultSrc) THEN
    	IF (CurrentDefaultSrc IS NULL) THEN
	        EXECUTE 'ALTER TABLE ' || TableId::regclass ||
					' ALTER COLUMN ' || quote_ident(AttributeName) ||
					' SET DEFAULT ' || NewDefaultSrc;
			IF UpdateExisting THEN
	        	EXECUTE 'UPDATE '|| TableId::regclass ||' SET '|| quote_ident(AttributeName) ||' = '|| NewDefaultSrc;
	        END IF;
	    ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN	'|| quote_ident(AttributeName) ||' DROP DEFAULT';
		END IF;
    END IF;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_attribute_notnull_is_check(TableId oid, AttributeName text) RETURNS boolean AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN NOT (
		_cm_is_simpleclass(TableId)
		OR _cm_check_comment(_cm_comment_for_table_id(TableId), 'MODE', 'reserved')
		OR _cm_check_comment(_cm_comment_for_attribute(TableId, AttributeName), 'MODE', 'reserved')
	);
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_attribute_set_notnull_unsafe(TableId oid, AttributeName text, WillBeNotNull boolean) RETURNS VOID AS $$
DECLARE
    IsCheck boolean := _cm_attribute_notnull_is_check(TableId, AttributeName);
BEGIN
	IF (WillBeNotNull) THEN
		IF (IsCheck) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||
				' ADD CONSTRAINT ' || quote_ident(_cm_notnull_constraint_name(AttributeName)) ||
				' CHECK ("Status"<>''A'' OR ' || quote_ident(AttributeName) || ' IS NOT NULL)';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' SET NOT NULL';
		END IF;
	ELSE
		IF (IsCheck) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP CONSTRAINT '||
				quote_ident(_cm_notnull_constraint_name(AttributeName));
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' DROP NOT NULL';
		END IF;
	END IF;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_attribute_set_notnull(TableId oid, AttributeName text, WillBeNotNull boolean) RETURNS VOID AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF WillBeNotNull = _cm_attribute_is_notnull(TableId, AttributeName) THEN
		RETURN;
	END IF;

    IF WillBeNotNull AND _cm_is_superclass(TableId) AND _cm_check_comment(AttributeComment, 'MODE', 'write')
    THEN
    	RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: %', 'Non-system superclass attributes cannot be not null';
    END IF;

	PERFORM _cm_attribute_set_notnull_unsafe(TableId, AttributeName, WillBeNotNull);
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_check_attribute_comment_and_type(AttributeComment text, SQLType text) RETURNS VOID AS $$
DECLARE
	SpecialTypeCount integer := 0; 
BEGIN
	IF _cm_read_reference_domain_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF _cm_get_fk_target_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF _cm_get_lookup_type_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF (SpecialTypeCount > 1) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: Too many CMDBuild types specified';
	END IF;

	IF SpecialTypeCount = 1 AND SQLType NOT IN ('int4','integer') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: The SQL type does not match the CMDBuild type';
	END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_add_spherical_mercator() RETURNS VOID AS $$
DECLARE
	FoundSrid integer;
BEGIN
	SELECT "srid" INTO FoundSrid FROM "spatial_ref_sys" WHERE "srid" = 900913 LIMIT 1;
	IF NOT FOUND THEN
		INSERT INTO "spatial_ref_sys" ("srid","auth_name","auth_srid","srtext","proj4text") VALUES (900913,'spatialreferencing.org',900913,'','+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +units=m +k=1.0 +nadgrids=@null +no_defs');
	END IF;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION cm_create_attribute(
	TableId oid,
	AttributeName text,
	SQLType text,
	AttributeDefault text,
	AttributeNotNull boolean,
	AttributeUnique boolean,
	AttributeComment text
) RETURNS void AS $$
BEGIN
	PERFORM _cm_check_attribute_comment_and_type(AttributeComment, SQLType);

	IF _cm_is_geometry_type(SQLType) THEN
		PERFORM _cm_add_spherical_mercator();
		PERFORM AddGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName, 900913, SQLType, 2);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ADD COLUMN '|| quote_ident(AttributeName) ||' '|| SQLType;
	END IF;

    PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, TRUE);

	-- set the comment recursively (needs to be performed before unique and notnull, because they depend on the comment)
    PERFORM _cm_set_attribute_comment(TableId, AttributeName, AttributeComment);

	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);

    PERFORM _cm_add_fk_constraints(TableId, AttributeName);
	PERFORM _cm_add_reference_handling(TableId, AttributeName);
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION cm_modify_attribute(
	TableId oid,
	AttributeName text,
	SQLType text,
	AttributeDefault text,
	AttributeNotNull boolean,
	AttributeUnique boolean,
	NewComment text
) RETURNS void AS $$
DECLARE
	OldComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF _cm_read_reference_domain_comment(OldComment) IS DISTINCT FROM _cm_read_reference_domain_comment(NewComment)
		OR  _cm_read_reference_type_comment(OldComment) IS DISTINCT FROM _cm_read_reference_type_comment(NewComment)
		OR  _cm_get_fk_target_comment(OldComment) IS DISTINCT FROM _cm_get_fk_target_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM _cm_check_attribute_comment_and_type(NewComment, SQLType);

	IF _cm_get_attribute_sqltype(TableId, AttributeName) <> trim(SQLType) THEN
		IF _cm_attribute_is_inherited(TableId, AttributeName) THEN
			RAISE NOTICE 'Not altering column type'; -- Fail silently
			--RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' TYPE '|| SQLType;
		END IF;
	END IF;

	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);
	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, FALSE);
	PERFORM _cm_set_attribute_comment(TableId, AttributeName, NewComment);
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION cm_delete_attribute(
	TableId oid,
	AttributeName text
) RETURNS VOID AS $$
DECLARE
	GeoType text := _cm_get_geometry_type(TableId, AttributeName);
BEGIN
	IF NOT _cm_attribute_is_local(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

    IF NOT _cm_attribute_is_empty(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: %', 'Contains data';
	END IF;

	PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);

	IF GeoType IS NOT NULL THEN
		PERFORM DropGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP COLUMN '|| quote_ident(AttributeName) ||' CASCADE';
	END IF;
END;
$$ LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION cm_create_class_attribute(CMClass text, AttributeName text, SQLType text,
	AttributeDefault text, AttributeNotNull boolean, AttributeUnique boolean, AttributeComment text
) RETURNS void AS $$
	SELECT cm_create_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_modify_class_attribute(CMClass text, AttributeName text, SQLType text,
	AttributeDefault text, AttributeNotNull boolean, AttributeUnique boolean, AttributeComment text
) RETURNS void AS $$
	SELECT cm_modify_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_delete_class_attribute(CMClass text, AttributeName text) RETURNS VOID AS $$
	SELECT cm_delete_attribute(_cm_table_id($1), $2);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_create_domain_attribute(CMClass text, AttributeName text, SQLType text,
	AttributeDefault text, AttributeNotNull boolean, AttributeUnique boolean, AttributeComment text
) RETURNS void AS $$
	SELECT cm_create_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_modify_domain_attribute(CMClass text, AttributeName text, SQLType text,
	AttributeDefault text, AttributeNotNull boolean, AttributeUnique boolean, AttributeComment text
) RETURNS void AS $$
	SELECT cm_modify_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_delete_domain_attribute(CMClass text, AttributeName text) RETURNS VOID AS $$
	SELECT cm_delete_attribute(_cm_domain_id($1), $2);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION system_attribute_create(
		CMClass varchar,
		AttributeName varchar,
		DenormalizedSQLType varchar,
		AttributeDefault varchar,
		AttributeNotNull boolean,
		AttributeUnique boolean,
		AttributeComment varchar,
		AttributeReference varchar,
		AttributeReferenceDomain varchar,
		AttributeReferenceType varchar,
		AttributeReferenceIsDirect boolean
) RETURNS integer AS $$
DECLARE
    AttributeIndex integer;
    SQLType varchar;
BEGIN
	-- redundant parameters sanity check
	IF COALESCE(AttributeReferenceDomain,'') <> COALESCE(_cm_read_reference_domain_comment(AttributeComment),'')
		OR (COALESCE(_cm_read_reference_domain_comment(AttributeComment),'') <> '' AND
			(
			COALESCE(AttributeReferenceIsDirect,FALSE) <> COALESCE(_cm_read_comment(AttributeComment, 'REFERENCEDIRECT')::boolean,FALSE)
			OR COALESCE(AttributeReference,'') <> COALESCE(_cm_read_reference_target_comment(AttributeComment),'')
			OR COALESCE(AttributeReferenceType,'') <> COALESCE(_cm_read_comment(AttributeComment, 'REFERENCETYPE'),'')
			)
		)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	IF DenormalizedSQLType ILIKE 'bpchar%' THEN
		SQLType := 'bpchar(1)';
	ELSE
		SQLType := DenormalizedSQLType;
	END IF;

	PERFORM cm_create_class_attribute(CMClass, AttributeName, SQLType, AttributeDefault, AttributeNotNull, AttributeUnique, AttributeComment);

    SELECT CASE
	    	WHEN _cm_check_comment(AttributeComment,'MODE','reserved') THEN -1
			ELSE COALESCE(_cm_read_comment(AttributeComment, 'INDEX'),'0')::integer
		END INTO AttributeIndex;
    RETURN AttributeIndex;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION system_attribute_modify(
	CMClass text,
	AttributeName text,
	AttributeNewName text,
	DenormalizedSQLType text,
	AttributeDefault text,
	AttributeNotNull boolean,
	AttributeUnique boolean,
	AttributeComment text
) RETURNS boolean AS $$
DECLARE
    SQLType varchar;
BEGIN
	IF (AttributeName <> AttributeNewName) THEN 
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
    END IF;

   	IF DenormalizedSQLType ILIKE 'bpchar%' THEN
		SQLType := 'bpchar(1)';
	ELSE
		SQLType := DenormalizedSQLType;
	END IF;

	PERFORM cm_modify_class_attribute(CMClass, AttributeName, SQLType,
		AttributeDefault, AttributeNotNull, AttributeUnique, AttributeComment);
	RETURN TRUE;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION system_attribute_delete(CMClass varchar, AttributeName varchar) RETURNS BOOLEAN AS $$
BEGIN
	PERFORM cm_delete_class_attribute(CMClass, AttributeName);
	RETURN TRUE;
END;
$$ LANGUAGE PLPGSQL;

/**************************************************************************
 * Database setup 04                                                      *
 **************************************************************************/

CREATE OR REPLACE FUNCTION _cm_create_domain_indexes(DomainId oid) RETURNS VOID AS $$
DECLARE
    Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	PERFORM _cm_create_index(DomainId, 'IdDomain');
	PERFORM _cm_create_index(DomainId, 'IdObj1');
	PERFORM _cm_create_index(DomainId, 'IdObj2');

	EXECUTE 'CREATE INDEX ' || quote_ident(_cm_domainidx_name(DomainId, 'ActiveRows')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ('||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdDomain" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass2" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj2" END)'||
		')';

	IF substring(Cardinality, 3, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueLeft')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass1" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj1" ELSE NULL END)'||
		' )';
	END IF;

	IF substring(Cardinality, 1, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueRight')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass2" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj2" ELSE NULL END)'||
		' )';
	END IF;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _cm_create_domain_triggers(DomainId oid) RETURNS void AS $$
BEGIN
	PERFORM _cm_add_domain_sanity_check_trigger(DomainId);
	PERFORM _cm_add_domain_history_trigger(DomainId);
END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION cm_create_domain(CMDomain text, DomainComment text) RETURNS integer AS $$
DECLARE
	DomainId oid;
	HistoryDBName text := _cm_history_dbname_unsafe(_cm_domain_cmname(CMDomain));
BEGIN
	-- TODO: Add Creation of Map (from its name)
	EXECUTE 'CREATE TABLE '|| _cm_domain_dbname_unsafe(CMDomain) ||
		' (CONSTRAINT '|| quote_ident(_cm_domainpk_name(CMDomain)) ||
		' PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate"))'||
		' INHERITS ("Map")';

	DomainId := _cm_domain_id(CMDomain);

	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass ||' IS '|| quote_literal(DomainComment);
	EXECUTE 'CREATE TABLE '|| HistoryDBName ||
		' ( CONSTRAINT '|| quote_ident(_cm_historypk_name(_cm_domain_cmname(CMDomain))) ||
		' PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate"))'||
		' INHERITS ('|| DomainId::regclass ||')';
	EXECUTE 'ALTER TABLE '|| HistoryDBName ||' ALTER COLUMN "EndDate" SET DEFAULT now()';

	PERFORM _cm_create_domain_indexes(DomainId);

	PERFORM _cm_create_domain_triggers(DomainId);

	RETURN DomainId;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION cm_modify_domain(DomainId oid, NewComment text) RETURNS void AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	IF _cm_read_domain_cardinality(OldComment) <> _cm_read_domain_cardinality(NewComment)
		OR _cm_read_comment(OldComment, 'CLASS1') <> _cm_read_comment(NewComment, 'CLASS1')
		OR _cm_read_comment(OldComment, 'CLASS2') <> _cm_read_comment(NewComment, 'CLASS2')
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Check that the cardinality does not change
	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass || ' IS '|| quote_literal(NewComment);
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION cm_delete_domain(DomainId oid) RETURNS void AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'Cannot delete domain %, contains data', DomainId::regclass;
	END IF;

	PERFORM _cm_delete_local_attributes_or_triggers(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION cm_modify_domain(CMDomain text, DomainComment text) RETURNS void AS $$
	SELECT cm_modify_domain(_cm_domain_id($1), $2);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_delete_domain(CMDomain text) RETURNS void AS $$
	SELECT cm_delete_domain(_cm_domain_id($1));
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION system_domain_create(
	CMDomain text,
	DomainClass1 text,
	DomainClass2 text,
	DomainComment text
) RETURNS integer AS $$
DECLARE
	TableName text := _cm_domain_cmname(CMDomain);
	HistoryTableName text := _cm_history_cmname(TableName);
    DomainId oid;
BEGIN
	-- TODO: Check DomainClass1 and DomainClass2

	RETURN cm_create_domain(CMDomain, DomainComment);
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION system_domain_modify(
	DomainId oid,
	DomainName text,
	DomainClass1 text,
	DomainClass2 text,
	NewComment text
) RETURNS boolean AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	-- TODO: Check DomainName, DomainClass1 and DomainClass2
	IF _cm_domain_id(DomainName) <> DomainId
		OR _cm_read_comment(NewComment, 'CLASS1') <> DomainClass1
		OR _cm_read_comment(NewComment, 'CLASS2') <> DomainClass2
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_domain(DomainId, NewComment);

	RETURN TRUE;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION system_domain_delete(CMDomain text) RETURNS void AS $$
	SELECT cm_delete_domain($1);
$$ LANGUAGE SQL;

/**************************************************************************
 * Database setup 06                                                      *
 **************************************************************************/

CREATE OR REPLACE FUNCTION _cm_legacy_read_comment(text, text) RETURNS varchar AS $$
	SELECT COALESCE(_cm_read_comment($1, $2), '');
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE VIEW system_classcatalog AS 
	SELECT pg_class.oid AS classid,
		CASE WHEN pg_namespace.nspname = 'public' THEN '' ELSE pg_namespace.nspname || '.' END || pg_class.relname AS classname, pg_description.description AS classcomment, pg_class.relkind = 'v'::"char" AS isview
	FROM pg_class
		JOIN pg_description
			ON pg_description.objoid = pg_class.oid
				AND pg_description.objsubid = 0
				AND _cm_is_any_class_comment(pg_description.description)
		JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
	WHERE pg_class.reltype > 0::oid;


CREATE OR REPLACE VIEW system_domaincatalog AS
 SELECT
   pg_class.oid AS domainid,
   substring(pg_class.relname from 5) AS domainname,
   substring(pg_description.description, 'CLASS1: ([^|]*)'::text) AS domainclass1,
   substring(pg_description.description, 'CLASS2: ([^|]*)'::text) AS domainclass2,
   substring(pg_description.description, 'CARDIN: ([^|]*)'::text) AS domaincardinality,
   pg_description.description AS domaincomment,
   (pg_class.relkind='v') AS isview
   FROM pg_class
   LEFT JOIN pg_description pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
  WHERE strpos(pg_description.description, 'TYPE: domain'::text) > 0;


CREATE OR REPLACE VIEW system_attributecatalog AS 
 SELECT cmtable.classid, cmtable.classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, 
        CASE
            WHEN strpos(attribute_description.description, 'MODE: reserved'::text) > 0 THEN (-1)
            WHEN strpos(attribute_description.description, 'INDEX: '::text) > 0 THEN "substring"(attribute_description.description, 'INDEX: ([^|]*)'::text)::integer
            ELSE 0
        END AS attributeindex, pg_attribute.attinhcount = 0 AS attributeislocal,
        CASE pg_type.typname
		WHEN 'geometry' THEN _cm_get_geometry_type(cmtable.classid, pg_attribute.attname)
		ELSE pg_type.typname
        END AS attributetype, 
        CASE
            WHEN pg_type.typname = 'varchar'::name THEN pg_attribute.atttypmod - 4
            ELSE NULL::integer
        END AS attributelength, 
        CASE
            WHEN pg_type.typname = 'numeric'::name THEN pg_attribute.atttypmod / 65536
            ELSE NULL::integer
        END AS attributeprecision, 
        CASE
            WHEN pg_type.typname = 'numeric'::name THEN pg_attribute.atttypmod - pg_attribute.atttypmod / 65536 * 65536 - 4
            ELSE NULL::integer
        END AS attributescale, notnulljoin.oid IS NOT NULL OR pg_attribute.attnotnull AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, _cm_attribute_is_unique(cmtable.classid, pg_attribute.attname::text) AS isunique, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'LOOKUP'::character varying::text) AS attributelookup, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCEDOM'::character varying::text) AS attributereferencedomain, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCETYPE'::character varying::text) AS attributereferencetype, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCEDIRECT'::character varying::text) AS attributereferencedirect, 
        CASE
            WHEN system_domaincatalog.domaincardinality = '1:N'::text THEN system_domaincatalog.domainclass1
            ELSE system_domaincatalog.domainclass2
        END AS attributereference
   FROM pg_attribute
   JOIN ( SELECT system_classcatalog.classid, system_classcatalog.classname
           FROM system_classcatalog
UNION 
         SELECT system_domaincatalog.domainid AS classid, system_domaincatalog.domainname AS classname
           FROM system_domaincatalog) cmtable ON pg_attribute.attrelid = cmtable.classid
   LEFT JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
   LEFT JOIN pg_description attribute_description ON attribute_description.objoid = cmtable.classid AND attribute_description.objsubid = pg_attribute.attnum
   LEFT JOIN pg_attrdef pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
   LEFT JOIN system_domaincatalog ON _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCEDOM'::character varying::text)::text = system_domaincatalog.domainname
   LEFT JOIN pg_constraint notnulljoin ON notnulljoin.conrelid = pg_attribute.attrelid AND notnulljoin.conname::text = _cm_notnull_constraint_name(pg_attribute.attname::text)
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;


CREATE OR REPLACE VIEW system_inheritcatalog AS
	SELECT inhparent AS parentid, inhrelid AS childid FROM pg_inherits
	UNION -- add views with cmdbuild comments
	SELECT '"Class"'::regclass::oid AS parentid, pg_class.oid AS childid
		FROM pg_class
		JOIN pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
		LEFT JOIN pg_inherits ON pg_inherits.inhrelid = pg_class.oid
	WHERE pg_class.relkind = 'v' AND strpos(pg_description.description, 'TYPE: class'::text) > 0;


CREATE OR REPLACE VIEW system_treecatalog AS 
	SELECT
		parent_class.classid AS parentid,
		parent_class.classname AS parent,
		parent_class.classcomment AS parentcomment,
		child_class.classid AS childid,
		child_class.classname AS child,
		child_class.classcomment AS childcomment
	FROM system_inheritcatalog
		JOIN system_classcatalog AS parent_class
			ON system_inheritcatalog.parentid = parent_class.classid
		JOIN system_classcatalog AS child_class
			ON system_inheritcatalog.childid = child_class.classid;


CREATE OR REPLACE VIEW system_relationlist AS 
	SELECT
		"Map"."Id" AS id,
		pg_class1.relname AS class1,
		pg_class2.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass1"::integer AS idclass1,
		"Map"."IdObj1" AS idobj1,
		"Map"."IdClass2"::integer AS idclass2,
		"Map"."IdObj2" AS idobj2,
		"Map"."BeginDate" AS begindate,
		"Map"."Status" AS status,
		_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
		TRUE AS direct,
		NULL AS version
	FROM "Map"
	JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid
		AND "Class"."Id" = "Map"."IdObj2"
		AND "Class"."Status" = 'A'::bpchar
	LEFT JOIN pg_class pg_class0
		ON pg_class0.oid = "Map"."IdDomain"::oid
	LEFT JOIN pg_description pg_description0
		ON pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description)
	LEFT JOIN pg_class pg_class1
		ON pg_class1.oid = "Map"."IdClass1"::oid
	LEFT JOIN pg_description pg_description1
		ON pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
	LEFT JOIN pg_class pg_class2
		ON pg_class2.oid = "Map"."IdClass2"::oid
	LEFT JOIN pg_description pg_description2
		ON pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0
UNION
	SELECT
		"Map"."Id" AS id,
		pg_class2.relname AS class1,
		pg_class1.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass2"::integer AS idclass1,
		"Map"."IdObj2" AS idobj1,
		"Map"."IdClass1"::integer AS idclass2,
		"Map"."IdObj1" AS idobj2,
		"Map"."BeginDate" AS begindate,
		"Map"."Status" AS status, 
		_cm_legacy_read_comment(pg_description0.description, 'DESCRINV')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description1.description, 'DESCR')::text AS classdescription,
		FALSE AS direct,
		NULL AS version
	FROM "Map"
	JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid
		AND "Class"."Id" = "Map"."IdObj1"
		AND "Class"."Status" = 'A'::bpchar
	LEFT JOIN pg_class pg_class0
		ON pg_class0.oid = "Map"."IdDomain"::oid
	LEFT JOIN pg_description pg_description0
		ON pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description)
	LEFT JOIN pg_class pg_class1
		ON pg_class1.oid = "Map"."IdClass1"::oid
	LEFT JOIN pg_description pg_description1
		ON pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
	LEFT JOIN pg_class pg_class2
		ON pg_class2.oid = "Map"."IdClass2"::oid
	LEFT JOIN pg_description pg_description2
		ON pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0;


CREATE OR REPLACE VIEW system_relationlist_history AS 
	SELECT
		"Map"."Id" AS id,
		pg_class1.relname AS class1,
		pg_class2.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass1"::integer AS idclass1,
		"Map"."IdObj1" AS idobj1,
		"Map"."IdClass2"::integer AS idclass2,
		"Map"."IdObj2" AS idobj2,
		"Map"."Status" AS status,
		_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
		TRUE AS direct,
		"Map"."User" AS username,
		"Map"."BeginDate" AS begindate,
		"Map"."EndDate" AS enddate,
		NULL AS version
	FROM "Map"
	LEFT JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid
		AND "Class"."Id" = "Map"."IdObj2",
		pg_class pg_class0,
		pg_description pg_description0,
		pg_class pg_class1,
		pg_description pg_description1,
		pg_class pg_class2,
		pg_description pg_description2
	WHERE
		"Map"."Status" = 'U'
		AND pg_class1.oid = "Map"."IdClass1"::oid
		AND pg_class2.oid = "Map"."IdClass2"::oid
		AND pg_class0.oid = "Map"."IdDomain"::oid
		AND pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
		AND pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description)
UNION
	SELECT
		"Map"."Id" AS id,
		pg_class2.relname AS class1,
		pg_class1.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass2"::integer AS idclass1,
		"Map"."IdObj2" AS idobj1,
		"Map"."IdClass1"::integer AS idclass2,
		"Map"."IdObj1" AS idobj2,
		"Map"."Status" AS status,
		_cm_legacy_read_comment(pg_description0.description, 'DESCRINV')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
		FALSE AS direct,
		"Map"."User" AS username,
		"Map"."BeginDate" AS begindate,
		"Map"."EndDate" AS enddate,
		NULL AS version
	FROM "Map"
	LEFT JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid
		AND "Class"."Id" = "Map"."IdObj1",
		pg_class pg_class0,
		pg_description pg_description0,
		pg_class pg_class1,
		pg_description pg_description1,
		pg_class pg_class2,
		pg_description pg_description2
	WHERE
		"Map"."Status" = 'U'
		AND pg_class1.oid = "Map"."IdClass1"::oid
		AND pg_class2.oid = "Map"."IdClass2"::oid
		AND pg_class0.oid = "Map"."IdDomain"::oid
		AND pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
		AND pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description);

/**************************************************************************
 * Database setup 09                                                      *
 **************************************************************************/

CREATE OR REPLACE FUNCTION _cm_legacy_get_menu_type(boolean, boolean, boolean, boolean)
  RETURNS varchar AS
$BODY$
    DECLARE 
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menutype varchar;
    BEGIN
	IF (isprocess) THEN menutype='processclass';
	ELSIF(isview) THEN menutype='view';
	ELSIF(isreport) THEN menutype='report';
	ELSE menutype='class';
	END IF;

	RETURN menutype;
    END;
$BODY$
  LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION _cm_legacy_get_menu_code(boolean, boolean, boolean, boolean)
  RETURNS varchar AS
$BODY$
    DECLARE 
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menucode varchar;
    BEGIN
	IF (issuperclass) THEN IF (isprocess) THEN menucode='superclassprocess'; ELSE menucode='superclass'; END IF;
	ELSIF(isview) THEN menucode='view';
	ELSIF(isreport) THEN menucode='report';
	ELSIF (isprocess) THEN menucode='processclass'; ELSE menucode='class';
	END IF;

	RETURN menucode;
    END;
$BODY$
  LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION _cm_legacy_class_is_process(text) RETURNS boolean AS $$
	SELECT (_cm_legacy_read_comment($1, 'MANAGER') = 'activity');
$$ LANGUAGE SQL;

CREATE OR REPLACE VIEW system_availablemenuitems AS 
        (        (        ( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Code", _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar) AS "Description", 
                                CASE
                                    WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                                       FROM "Menu" "Menu1"
                                      WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                                    ELSE NULL::regclass
                                END AS "IdElementClass", 0 AS "IdElementObj", "Role"."Id" AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Type"
                           FROM system_classcatalog
                      JOIN "Role" ON "Role"."Status" = 'A'::bpchar
                 LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
                WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
                         FROM "Menu"
                        WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu"."IdGroup")) AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'STATUS'::varchar)::text = 'active'::text
                ORDER BY system_classcatalog.classid::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false), _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar), 
                      CASE
                          WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                             FROM "Menu" "Menu1"
                            WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                          ELSE NULL::regclass
                      END, 0::integer, "Role"."Id", _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false))
                UNION 
                         SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", "AllReport"."RoleId" AS "IdGroup", "AllReport"."Type"
                           FROM ( SELECT _cm_legacy_get_menu_type(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type", "Role"."Id" AS "RoleId"
                                   FROM generate_series(1, 6) i(i), "Report"
                              JOIN "Role" ON "Role"."Status" = 'A'::bpchar
                             WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
                                   CASE
                                       WHEN "Report"."Type"::text = 'normal'::text THEN 1
                                       WHEN "Report"."Type"::text = 'custom'::text THEN 2
                                       WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
                                       ELSE 0
                                   END) "AllReport"
                      LEFT JOIN "Menu" ON "AllReport"."IdElementObj" = "Menu"."IdElementObj" AND "Menu"."Status" = 'A'::bpchar AND "AllReport"."RoleId" = "Menu"."IdGroup" AND "AllReport"."Code" = "Menu"."Code"::text
                     WHERE "Menu"."Code" IS NULL)
        UNION 
                ( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Code", _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar) AS "Description", 
                        CASE
                            WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                               FROM "Menu" "Menu1"
                              WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                            ELSE NULL::regclass
                        END AS "IdElementClass", 0 AS "IdElementObj", 0 AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Type"
                   FROM system_classcatalog
              LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
             WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
                      FROM "Menu"
                     WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup")) AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'STATUS'::varchar)::text = 'active'::text
             ORDER BY system_classcatalog.classid::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false), _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar), 
                   CASE
                       WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                          FROM "Menu" "Menu1"
                         WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                       ELSE NULL::regclass
                   END, 0::integer, _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false)))
UNION 
         SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", 0 AS "IdGroup", "AllReport"."Type"
           FROM ( SELECT _cm_legacy_get_menu_type(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type"
                   FROM generate_series(1, 6) i(i), "Report"
                  WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
                        CASE
                            WHEN "Report"."Type"::text = 'normal'::text THEN 1
                            WHEN "Report"."Type"::text = 'custom'::text THEN 2
                            WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
                            ELSE 0
                        END) "AllReport"
      LEFT JOIN "Menu" ON "AllReport"."IdElementObj" = "Menu"."IdElementObj" AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup" AND "AllReport"."Code" = "Menu"."Code"::text
     WHERE "Menu"."Code" IS NULL;


CREATE OR REPLACE VIEW system_privilegescatalog AS 
 SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode"
   FROM (         SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode"
                   FROM "Grant"
        UNION 
                 SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", system_classcatalog.classid::regclass AS classid, '-'
                   FROM system_classcatalog, "Role"
                  WHERE system_classcatalog.classid::regclass::oid <> '"Class"'::regclass::oid AND NOT ("Role"."Id"::text || system_classcatalog.classid::integer::text IN ( SELECT "Grant"."IdRole"::text || "Grant"."IdGrantedClass"::oid::integer::text
                           FROM "Grant"))) permission
   JOIN system_classcatalog ON permission."IdGrantedClass"::oid = system_classcatalog.classid AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text]))
  ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";

/**************************************************************************
 * Apply changes to the database                                          *
 **************************************************************************/

CREATE OR REPLACE FUNCTION patch_drop_before_and_after_triggers() RETURNS VOID AS $$
DECLARE
	FunctionName name;
BEGIN
	RAISE INFO 'Dropping standard before triggers';
	DROP FUNCTION IF EXISTS before_archive_row() CASCADE;
	DROP FUNCTION IF EXISTS before_archive_relation_row() CASCADE;

	RAISE INFO 'Dropping after triggers';
	FOR FunctionName IN
		SELECT proname
		FROM pg_proc
		WHERE proname LIKE 'after_archive_row%' OR proname LIKE 'after_archive_relation_row%'
	LOOP
		RAISE INFO '... %', FunctionName;
		EXECUTE 'DROP FUNCTION '|| quote_ident(FunctionName) ||'() CASCADE';
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_rename_custom_before_triggers() RETURNS VOID AS $$
DECLARE
	TableId oid;
	OldTriggerName name;
	OldFunctionName name;
	NewName name;
BEGIN
	RAISE INFO 'Renaming customized before triggers if any';
	FOR TableId, OldTriggerName, OldFunctionName, NewName IN
		SELECT pg_class.oid, pg_trigger.tgname, pg_proc.proname, 'set_data_'||LOWER(pg_class.relname)
		FROM pg_trigger
			JOIN pg_proc ON pg_trigger.tgfoid = pg_proc.oid
			JOIN pg_class ON tgrelid = pg_class.oid
		WHERE tgname LIKE 'before_archive_row%'
	LOOP
		RAISE INFO '... % on % to %', OldTriggerName, TableId::regclass, NewName;
		EXECUTE 'ALTER TRIGGER '|| quote_ident(OldTriggerName) ||' ON '|| TableId::regclass ||' RENAME TO '|| quote_ident(NewName);
		EXECUTE 'ALTER FUNCTION '|| quote_ident(OldFunctionName) ||'() RENAME TO '|| quote_ident(NewName);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_drop_reference_triggers_and_fk() RETURNS VOID AS $$
DECLARE
	TableId oid;
	FunctionName name;
	FKName name;
BEGIN
	RAISE INFO 'Dropping reference triggers';
	FOR FunctionName IN
		SELECT DISTINCT pg_proc.proname
		FROM pg_proc JOIN pg_trigger ON pg_trigger.tgfoid = pg_proc.oid
		WHERE tgname LIKE 'reference_%'
			OR tgname LIKE 'restrict_%'
			OR tgname LIKE 'setnull_%'
			OR tgname LIKE 'cascade_%'
	LOOP
		RAISE INFO '... %', FunctionName;
		EXECUTE 'DROP FUNCTION '|| quote_ident(FunctionName) ||'() CASCADE';
	END LOOP;

	RAISE INFO 'Dropping reference foreign keys';
	FOR TableId, FKName IN
		SELECT attrelid, conname
		FROM pg_attribute
		JOIN pg_class ON attrelid = pg_class.oid
		JOIN pg_constraint ON conrelid = attrelid AND conname = pg_class.relname ||'_'|| attname ||'_fkey'
		JOIN pg_description ON pg_class.oid = objoid AND objsubid = attnum
		WHERE attnum > 0 AND atttypid > 0 AND pg_class.relkind <> 'v' AND _cm_is_reference_comment(description)
	LOOP
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP CONSTRAINT '|| quote_ident(FKName);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_update_notnull_constraints() RETURNS VOID AS $$
DECLARE
	TableId oid;
	AttributeName name;
	IsNotNull boolean;
	CheckConstraintName name;
BEGIN
	RAISE INFO 'Updating notnull constraints';
	FOR TableId, AttributeName, IsNotNull, CheckConstraintName IN
		SELECT attrelid, attname, attnotnull, c.conname
		FROM pg_attribute
		LEFT JOIN pg_constraint AS c
			ON c.conrelid = attrelid
			AND c.conname ILIKE '%_notnull'
			AND LOWER(attname) = substring(c.conname from '^[^_]+')
		LEFT JOIN pg_inherits AS inh ON c.conrelid = inh.inhrelid
		LEFT JOIN pg_constraint AS sc ON inh.inhparent = sc.conrelid AND sc.conname = c.conname
		WHERE attnum > 0 AND atttypid > 0 AND _cm_is_cmobject(attrelid) AND
			((c.conname IS NOT NULL AND sc.conname IS NULL) OR (attinhcount = 0 AND attnotnull))
	LOOP
		IF CheckConstraintName IS NOT NULL THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP CONSTRAINT '|| quote_ident(CheckConstraintName);
		END IF;

		IF IsNotNull AND _cm_attribute_notnull_is_check(TableId, AttributeName) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' DROP NOT NULL';
		END IF;

		PERFORM _cm_attribute_set_notnull_unsafe(TableId, AttributeName, TRUE);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_rename_unique_constraints() RETURNS VOID AS $$
DECLARE
	OldIndexName name;
	NewIndexName name;
BEGIN
	RAISE INFO 'Updating unique constraints';
	FOR OldIndexName, NewIndexName IN
		SELECT ic.relname, _cm_unique_index_name(attrelid, attname)
		FROM pg_attribute
		JOIN pg_class AS ac ON attrelid = ac.oid
		JOIN pg_index ON attrelid = indrelid
		JOIN pg_class AS ic ON indexrelid = ic.oid
		WHERE attnum > 0 AND atttypid > 0 AND _cm_is_cmobject(attrelid) AND ic.relname = 'unique_'|| ic.relname ||'_'|| attname
	LOOP
		EXECUTE 'ALTER INDEX '|| quote_ident(OldIndexName) ||' RENAME TO '|| quote_ident(NewIndexName);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_create_class_and_domain_triggers() RETURNS VOID AS $$
DECLARE
	TableId oid;
BEGIN
	RAISE INFO 'Creating class triggers';
	FOR TableId IN
		SELECT c FROM (SELECT _cm_class_list() AS c) AS cq WHERE cq.c NOT IN (
			'"Grant"'::regclass::oid,
			'"LookUp"'::regclass::oid,
			'"Report"'::regclass::oid,
			'"User"'::regclass::oid,
			'"Role"'::regclass::oid
		)
	LOOP
		PERFORM _cm_create_class_triggers(TableId);
	END LOOP;

	RAISE INFO 'Creating domain triggers';
	FOR TableId IN
		SELECT c FROM (SELECT _cm_domain_list() AS c) AS cq WHERE cq.c NOT IN ('"Map"'::regclass::oid)
	LOOP
		PERFORM _cm_create_domain_triggers(TableId);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_fix_reference_values() RETURNS VOID AS $$
DECLARE
	TableId oid;
	AttributeName name;
	FKTarget oid;
	UpdatedRows integer;
BEGIN
	RAISE INFO 'Fixing orphan reference values';
	FOR TableId, AttributeName IN
		SELECT attrelid, attname
		FROM pg_attribute
		JOIN pg_class ON attrelid = pg_class.oid
		JOIN pg_description ON pg_class.oid = objoid AND objsubid = 0
		WHERE attnum > 0 AND atttypid > 0 AND pg_attribute.attinhcount = 0
			AND pg_class.relkind <> 'v' AND _cm_is_any_class_comment(description)
	LOOP
		FKTarget := _cm_get_fk_target_table_id(TableId, AttributeName::text);
		IF (FKTarget IS NOT NULL) THEN
			-- Fix old databases
			RAISE INFO '... checking reference %.%', TableId::regclass, quote_ident(AttributeName);
			EXECUTE 'UPDATE '|| TableId::regclass ||
				' SET '|| quote_ident(AttributeName) ||'=NULL'||
				' WHERE NOT _cm_check_id_exists('||
					quote_ident(AttributeName) ||', '||
					quote_literal(FKTarget::regclass) ||'::regclass,"Status"<>''A'')';
			GET DIAGNOSTICS UpdatedRows = ROW_COUNT;
			RAISE INFO '... fixed % rows', UpdatedRows;
		END IF;
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_create_reference_triggers() RETURNS VOID AS $$
DECLARE
	TableId oid;
	AttributeName name;
	FKTarget oid;
BEGIN
	RAISE INFO 'Creating reference triggers';
	FOR TableId, AttributeName IN
		SELECT attrelid, attname
		FROM pg_attribute
		JOIN pg_class ON attrelid = pg_class.oid
		JOIN pg_description ON pg_class.oid = objoid AND objsubid = 0
		WHERE attnum > 0 AND atttypid > 0 AND pg_attribute.attinhcount = 0
			AND pg_class.relkind <> 'v' AND _cm_is_any_class_comment(description)
	LOOP
		RAISE INFO '... creating constraints for %.%', TableId::regclass, quote_ident(AttributeName);
		PERFORM _cm_add_fk_constraints(TableId, AttributeName);
		PERFORM _cm_add_reference_handling(TableId, AttributeName);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_remove_table_oids() RETURNS VOID AS $$
DECLARE
	TableId oid;
BEGIN
	FOR TableId IN SELECT _cm_class_list() UNION SELECT _cm_domain_list() LOOP
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' SET WITHOUT OIDS';
		BEGIN
			EXECUTE 'ALTER TABLE '|| _cm_history_id(_cm_cmtable(TableId))::regclass ||' SET WITHOUT OIDS';
		EXCEPTION WHEN undefined_table THEN
		END;
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_add_bpchar_limit() RETURNS VOID AS $$
DECLARE
	TableId oid;
	AttributeName name;
BEGIN
	FOR TableId, AttributeName IN
		SELECT attrelid, attname
		FROM pg_attribute JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
		WHERE pg_type.typname = 'bpchar' AND pg_attribute.atttypmod <> 5 AND _cm_is_cmobject(attrelid)
	LOOP
		RAISE NOTICE 'Setting char limit on %.%', TableId::regclass, quote_ident(AttributeName);
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||
			' ALTER '|| quote_ident(AttributeName) ||' TYPE bpchar(1)';
	END LOOP;
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_remove_priority_lookup() RETURNS VOID AS $$
DECLARE
	TableId oid;
BEGIN
	RAISE INFO 'Remove priority lookup form attributes';
	FOR TableId IN SELECT _cm_class_list() LOOP
		IF NOT _cm_check_comment(_cm_comment_for_attribute(TableId, 'Priority'), 'MODE', 'write') THEN
			RAISE INFO '... %."Priority"', TableId::regclass;
			EXECUTE 'COMMENT ON COLUMN '|| TableId::regclass ||'."Priority" IS ''MODE: reserved|INDEX: -1''';
		END IF;
	END LOOP;
	RAISE INFO 'Remove priority lookup';
	DELETE FROM "LookUp" WHERE "Type" = 'Priority';
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION patch_123_03() RETURNS VOID AS $$
BEGIN
	PERFORM patch_drop_before_and_after_triggers();
	PERFORM patch_rename_custom_before_triggers();
	PERFORM patch_drop_reference_triggers_and_fk();
	PERFORM patch_update_notnull_constraints();
	PERFORM patch_rename_unique_constraints();
	PERFORM patch_create_class_and_domain_triggers();
	PERFORM patch_fix_reference_values();
	PERFORM patch_create_reference_triggers();
	PERFORM patch_add_bpchar_limit();
	PERFORM patch_remove_priority_lookup();

	PERFORM patch_remove_table_oids();
	ALTER TABLE "Class" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();
	ALTER TABLE "Report" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();
	ALTER TABLE "Map" ALTER COLUMN "Id" SET DEFAULT _cm_new_card_id();

	ALTER INDEX idx_code RENAME TO idx_class_code;
	ALTER INDEX idx_description RENAME TO idx_class_description;

	DROP SEQUENCE report_seq;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_123_03();

DROP FUNCTION patch_123_03();

DROP FUNCTION patch_drop_before_and_after_triggers();
DROP FUNCTION patch_rename_custom_before_triggers();
DROP FUNCTION patch_drop_reference_triggers_and_fk();
DROP FUNCTION patch_update_notnull_constraints();
DROP FUNCTION patch_rename_unique_constraints();
DROP FUNCTION patch_create_class_and_domain_triggers();
DROP FUNCTION patch_fix_reference_values();
DROP FUNCTION patch_create_reference_triggers();
DROP FUNCTION patch_remove_table_oids();
DROP FUNCTION patch_add_bpchar_limit();
DROP FUNCTION patch_remove_priority_lookup();
