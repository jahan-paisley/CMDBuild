-- Creates DB templates table

CREATE OR REPLACE FUNCTION _cm_is_system(TableId oid) RETURNS BOOLEAN AS $$
	SELECT _cm_check_comment(_cm_comment_for_table_id($1), 'MODE', 'reserved')
$$ LANGUAGE SQL STABLE;

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

SELECT cm_create_class('_Templates', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Templates', 'Name', 'text', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Templates', 'Template', 'text', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');
