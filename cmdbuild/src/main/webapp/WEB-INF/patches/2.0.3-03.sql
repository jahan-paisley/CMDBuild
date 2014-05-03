-- Fixed comments and checks for allow activity attributes sorting

CREATE OR REPLACE FUNCTION _cm_read_reference_type_comment(AttributeComment text) RETURNS text AS $$
	SELECT COALESCE(NULLIF(_cm_read_comment($1, 'REFERENCETYPE'), ''), 'restrict');
$$ LANGUAGE SQL STABLE RETURNS NULL ON NULL INPUT;


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


COMMENT ON COLUMN "Activity"."Code" IS 'MODE: read|DESCR: Activity Name|INDEX: 0|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Description" IS 'MODE: read|DESCR: Description|INDEX: 1|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
