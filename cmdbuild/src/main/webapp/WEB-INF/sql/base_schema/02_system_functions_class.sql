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


CREATE OR REPLACE FUNCTION _cm_delete_local_attributes(TableId oid) RETURNS void AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT _cm_attribute_list(TableId) LOOP
		IF NOT _cm_attribute_is_inherited(TableId, AttributeName) THEN
			PERFORM cm_delete_attribute(TableId, AttributeName);
		END IF;
	END LOOP;
END
$$ LANGUAGE plpgsql VOLATILE;


/**************************************************************************
 *                                                                        *
 * Public functions                                                       *
 *                                                                        *
 **************************************************************************/


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
		PERFORM cm_create_attribute(TableId, 'IdClass', 'regclass', NULL, TRUE, FALSE, 'MODE: reserved');
		IF NOT IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'Code', 'varchar(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true');
			PERFORM cm_create_attribute(TableId, 'Description', 'varchar(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true');
			-- Status is the only attribute needed
			PERFORM cm_create_attribute(TableId, 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: reserved');
		END IF;
		PERFORM cm_create_attribute(TableId, 'User', 'varchar(100)', NULL, FALSE, FALSE, 'MODE: reserved');
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
		RAISE EXCEPTION 'CM_HAS_DOMAINS';
	ELSEIF _cm_class_has_children(TableId) THEN
		RAISE EXCEPTION 'CM_HAS_CHILDREN';
	ELSEIF NOT _cm_table_is_empty(TableId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(TableId);

	-- Cascade for the history table
	EXECUTE 'DROP TABLE '|| TableId::regclass ||' CASCADE';
END;
$$ LANGUAGE plpgsql VOLATILE;


CREATE OR REPLACE FUNCTION cm_delete_card(CardId integer, TableId oid) RETURNS void AS $$
DECLARE
	ClassComment text := _cm_comment_for_table_id(TableId);
	IsSimpleClass boolean := _cm_is_simpleclass_comment(ClassComment);
BEGIN
	IF IsSimpleClass THEN
		RAISE DEBUG 'deleting a card from a simple class';
		EXECUTE 'DELETE FROM ' || TableId::regclass || ' WHERE "Id" = ' || CardId;
	ELSE
		RAISE DEBUG 'deleting a card from a standard class';
		EXECUTE 'UPDATE ' || TableId::regclass || ' SET "Status" = ''N'' WHERE "Id" = ' || CardId;
	END IF;
END;
$$ LANGUAGE plpgsql VOLATILE;


/**************************************************************************
 *                                                                        *
 * Alternate function signatures                                          *
 *                                                                        *
 **************************************************************************/


CREATE OR REPLACE FUNCTION cm_create_class(CMClass text, CMParentClass text, ClassComment text) RETURNS integer AS $$
	SELECT cm_create_class($1, _cm_table_id($2), $3);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION cm_modify_class(CMClass text, NewComment text) RETURNS void AS $$
	SELECT cm_modify_class(_cm_table_id($1), $2);
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION cm_delete_class(CMClass text) RETURNS void AS $$
	SELECT cm_delete_class(_cm_table_id($1));
$$ LANGUAGE SQL;


/**************************************************************************
 *                                                                        *
 * Legacy public functions (they add checks for redundant parameters)     *
 *                                                                        *
 **************************************************************************/


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
