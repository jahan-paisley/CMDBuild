-- Set empty strings to null to reflect the new behavior

CREATE OR REPLACE FUNCTION _patch_disable_all_triggers() RETURNS void AS $$
DECLARE
	TableId oid;
BEGIN
	FOR TableId IN SELECT _cm_class_list() UNION SELECT _cm_domain_list() LOOP
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DISABLE TRIGGER USER';
	END LOOP;
END
$$ LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION _patch_enable_all_triggers() RETURNS void AS $$
DECLARE
	TableId oid;
BEGIN
	FOR TableId IN SELECT _cm_class_list() UNION SELECT _cm_domain_list() LOOP
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ENABLE TRIGGER USER';
	END LOOP;
END
$$ LANGUAGE plpgsql VOLATILE;



CREATE OR REPLACE FUNCTION patch_130_06() RETURNS VOID AS $$
DECLARE
	TableId oid;
	AttributeName text;
BEGIN
	PERFORM _patch_disable_all_triggers();

	RAISE INFO 'Setting to null all empty text values';
	FOR TableId IN SELECT _cm_class_list() UNION SELECT _cm_domain_list() LOOP
		RAISE INFO '... %', TableId::regclass;
		FOR AttributeName IN SELECT _cm_attribute_list(TableId) LOOP
			IF _cm_get_attribute_sqltype(TableId, AttributeName) ~* '^(text|[^_].*char)'
				AND NOT _cm_attribute_is_inherited(TableId, AttributeName)
			THEN
				RAISE INFO '...     %', AttributeName;
				EXECUTE 'UPDATE '|| TableId:: regclass ||
					' SET '|| quote_ident(AttributeName) ||' = NULL'||
					' WHERE '|| quote_ident(AttributeName) ||' = ''''';
			END IF;
		END LOOP;
	END LOOP;

	PERFORM _patch_enable_all_triggers();
END
$$ LANGUAGE PLPGSQL;

SELECT patch_130_06();

DROP FUNCTION patch_130_06();
DROP FUNCTION _patch_enable_all_triggers();
DROP FUNCTION _patch_disable_all_triggers();
