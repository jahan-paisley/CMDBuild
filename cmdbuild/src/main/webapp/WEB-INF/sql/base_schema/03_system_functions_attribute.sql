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
	IF _cm_is_simpleclass(TableId) THEN
		IF AttributeUnique THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ADD UNIQUE ('|| quote_ident(AttributeName) || ')';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP UNIQUE ('|| quote_ident(AttributeName) || ')';
		END IF;
	ELSE
		IF AttributeUnique THEN
			EXECUTE 'CREATE UNIQUE INDEX '||
				quote_ident(_cm_unique_index_name(TableId, AttributeName)) ||
				' ON '|| TableId::regclass ||' USING btree (('||
				' CASE WHEN "Status"::text = ''N''::text THEN NULL'||
				' ELSE '|| quote_ident(AttributeName) || ' END))';
		ELSE
			EXECUTE 'DROP INDEX '|| _cm_unique_index_id(TableId, AttributeName)::regclass;
		END IF;
	END IF;
END
$$ LANGUAGE PLPGSQL VOLATILE;


CREATE OR REPLACE FUNCTION _cm_attribute_set_uniqueness(TableId oid, AttributeName text, AttributeUnique boolean) RETURNS VOID AS $$
BEGIN
	IF _cm_attribute_is_unique(TableId, AttributeName) <> AttributeUnique THEN
		IF AttributeUnique AND (_cm_is_simpleclass(TableId) OR _cm_is_superclass(TableId)) AND NOT _cm_is_system(TableId) THEN
			RAISE NOTICE 'User defined superclass or simple class attributes cannot be unique';
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
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


/*
 * Creates the foreign key constraints on source and target classes if the
 * attribute is a foreign key, otherwise it returns doing nothing
 */
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
	RefSourceClassIdAttribute text := _cm_get_ref_source_class_domain_attribute(TableId, AttributeName);
	RefTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);

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
			SELECT '|| quote_ident(RefTargetIdAttribute) ||
			' FROM '|| ReferenceDomainId::regclass ||
			' WHERE '|| quote_ident(RefSourceClassIdAttribute) ||'='|| TableId ||
				' AND '|| quote_ident(RefSourceIdAttribute) ||'='|| objid ||
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
		' WHERE "Status"=''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId ||
			' AND ' || quote_ident(RefIdColumn) || ' <> ' || RefId;
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
		' AND coalesce(' || quote_ident(AttributeName) || ', 0) <> ' || coalesce(ReferenceId, 0)::text;
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
		OR _cm_is_system(TableId)
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
    	RAISE NOTICE 'Non-system superclass attributes cannot be not null';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
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
		RAISE NOTICE 'Too many CMDBuild types specified';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	IF SpecialTypeCount = 1 AND SQLType NOT IN ('int4','integer') THEN
		RAISE NOTICE 'The SQL type does not match the CMDBuild type';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
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

/**************************************************************************
 *                                                                        *
 * Public functions                                                       *
 *                                                                        *
 **************************************************************************/

/**
 * TODO: Consistency checks:
 *  - right SQL type for Reference, FK and Lookup
 *  - no Reference, FK and Lookup at the same time
 *  - we could use CMType instead of SQLType (e.g. REFERENCE(DomainName, ConstraintType))
 */
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


/*
 * @param SQLType SQL type containing also length, precision or scale
 */
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
	IF COALESCE(_cm_read_reference_domain_comment(OldComment), '') IS DISTINCT FROM COALESCE(_cm_read_reference_domain_comment(NewComment), '')
		OR  _cm_read_reference_type_comment(OldComment) IS DISTINCT FROM _cm_read_reference_type_comment(NewComment)
		OR  COALESCE(_cm_get_fk_target_comment(OldComment), '') IS DISTINCT FROM COALESCE(_cm_get_fk_target_comment(NewComment), '')
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
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);

	IF GeoType IS NOT NULL THEN
		PERFORM DropGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP COLUMN '|| quote_ident(AttributeName) ||' CASCADE';
	END IF;
END;
$$ LANGUAGE PLPGSQL VOLATILE;

/**************************************************************************
 *                                                                        *
 * Alternate function signatures                                          *
 *                                                                        *
 **************************************************************************/

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


/**************************************************************************
 *                                                                        *
 * Legacy public functions (they add checks for redundant parameters)     *
 *                                                                        *
 **************************************************************************/

/**
 * @param SQLType SQL type containing also length or precision and scale
 * @param AttributeReferenceIsDirect direction (inferred from domain cardinality)
 * @param AttributeReference destination class (inferred from domain cardinality and destination class)
 */
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
