-- Fix class deletion when having inherited reference attributes

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

CREATE OR REPLACE FUNCTION cm_delete_class(TableId oid) RETURNS void AS $$
BEGIN
	IF _cm_class_has_domains(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: has domains', TableId::regclass;
	ELSEIF _cm_class_has_children(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: has childs', TableId::regclass;
	ELSEIF NOT _cm_table_is_empty(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: contains data', TableId::regclass;
	END IF;

	PERFORM _cm_delete_local_attributes(TableId);

	-- Cascade for the history table
	EXECUTE 'DROP TABLE '|| TableId::regclass ||' CASCADE';
END;
$$ LANGUAGE plpgsql VOLATILE;

DROP FUNCTION _cm_delete_local_attributes_or_triggers(TableId oid);
