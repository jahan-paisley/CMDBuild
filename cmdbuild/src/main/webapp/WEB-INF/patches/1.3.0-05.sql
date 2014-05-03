-- Improve error messages

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


CREATE OR REPLACE FUNCTION _cm_attribute_set_uniqueness(TableId oid, AttributeName text, AttributeUnique boolean) RETURNS VOID AS $$
BEGIN
	IF _cm_attribute_is_unique(TableId, AttributeName) <> AttributeUnique THEN
		IF AttributeUnique AND (_cm_is_simpleclass(TableId) OR _cm_is_superclass(TableId)) THEN
			RAISE NOTICE 'Superclass or simple class attributes cannot be unique';
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;

		PERFORM _cm_attribute_set_uniqueness_unsafe(TableId, AttributeName, AttributeUnique);
	END IF;
END;
$$ LANGUAGE PLPGSQL VOLATILE;


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

CREATE OR REPLACE FUNCTION cm_delete_domain(DomainId oid) RETURNS void AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$ LANGUAGE PLPGSQL;
