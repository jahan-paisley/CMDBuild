-- Add/Replace system functions to delete cards. Add "IdClass" attribute to simple classes. Change process system attributes MODE

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


CREATE OR REPLACE FUNCTION add_id_class_attribute_to_simple_classes() RETURNS VOID AS $$
DECLARE
	id regclass;
BEGIN
	FOR id IN
		SELECT table_id
			FROM _cm_class_list() AS table_id
			WHERE _cm_check_comment(_cm_comment_for_table_id(table_id), 'TYPE', 'simpleclass')
	LOOP
		RAISE INFO 'creating IdClass attribute for class %', id;
		PERFORM cm_create_attribute(id, 'IdClass', 'regclass', NULL, FALSE, FALSE, 'MODE: reserved');

		RAISE INFO 'setting IdClass attribute value for class %', id;
		EXECUTE 'UPDATE ' || id::regclass || ' SET "IdClass" = ' || id::oid;

		PERFORM _cm_attribute_set_notnull(id, 'IdClass', TRUE);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT add_id_class_attribute_to_simple_classes();

DROP FUNCTION add_id_class_attribute_to_simple_classes();

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
$$ LANGUAGE plpgsql;

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

CREATE OR REPLACE FUNCTION changeModeToWorkflowAttributes() RETURNS void AS $$
DECLARE
	query text;
	currentClass regclass;
BEGIN
FOR currentClass IN SELECT _cm_subtables_and_itself('"Activity"'::regclass) LOOP
	-- disable trigger
	query = 'alter table ' || currentClass::regclass || ' disable trigger user;';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."FlowStatus" IS ''MODE: system|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."ActivityDefinitionId" IS ''MODE: system|DESCR: Activity Definition Ids (for speed)''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."ProcessCode" IS ''MODE: system|DESCR: Process Instance Id''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."NextExecutor" IS ''MODE: system|DESCR: Activity Instance performers''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."ActivityInstanceId" IS ''MODE: system|DESCR: Activity Instance Ids''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."PrevExecutors" IS ''MODE: system|DESCR: Process Instance performers up to now''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."UniqueProcessDefinition" IS ''MODE: system|DESCR: Unique Process Definition (for speed)''';
	raise notice '%', query;
	execute query;

	-- enable trigger
	query = 'alter table ' || currentClass::regclass || ' enable trigger user;';
	raise notice '%', query;
	execute query;
END LOOP;
END;
$$ LANGUAGE PLPGSQL;

SELECT changeModeToWorkflowAttributes();

DROP FUNCTION changeModeToWorkflowAttributes();

DROP VIEW IF EXISTS system_availablemenuitems;
DROP FUNCTION IF EXISTS _cm_legacy_class_is_process(text);