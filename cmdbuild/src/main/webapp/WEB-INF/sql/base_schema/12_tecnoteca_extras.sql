--
-- Put here what the PM decides that should be added to the empty database
--


-- 
-- Predefined DMS categories
-- 

INSERT INTO "LookUp" ("IdClass","Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'AlfrescoCategory', 1, 'Document', true, 'A');
INSERT INTO "LookUp" ("IdClass","Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'AlfrescoCategory', 2, 'Image', false, 'A');


-- 
-- Custom Functions
-- 

CREATE OR REPLACE FUNCTION _cmf_class_description(cid oid) RETURNS character varying AS $$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'DESCR');
$$ LANGUAGE sql STABLE;


CREATE OR REPLACE FUNCTION _cmf_is_displayable(cid oid) RETURNS boolean AS $$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'MODE') IN
('write','read','baseclass');
$$ LANGUAGE sql STABLE;


CREATE OR REPLACE FUNCTION cmf_active_cards_for_class(IN "ClassName" character varying, OUT "Class" character varying, OUT "Number" integer)
  RETURNS SETOF record AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT _cmf_class_description("IdClass") AS "ClassDescription", COUNT(*)::integer AS "CardCount"' ||
        '    FROM ' || quote_ident($1) ||
        '    WHERE' ||
        '        "Status" = ' || quote_literal('A') ||
        '        AND _cmf_is_displayable("IdClass")' ||
        '        AND "IdClass" not IN (SELECT _cm_subtables_and_itself(_cm_table_id(' || quote_literal('Activity') || ')))'
        '    GROUP BY "IdClass"' ||
        '    ORDER BY "ClassDescription"';
END
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION cmf_active_cards_for_class(character varying) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION cmf_count_active_cards(IN "ClassName" character varying, OUT "Count" integer)
  RETURNS integer AS $$
BEGIN
    EXECUTE 'SELECT count(*) FROM '|| quote_ident("ClassName") ||' WHERE "Status" like ''A''' INTO "Count";
END
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION cmf_count_active_cards(character varying) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION _cm_create_class_default_order_indexes(IN cmclass character varying, OUT always_true boolean) RETURNS boolean AS $$
BEGIN
	PERFORM _cm_create_class_default_order_indexes(_cm_table_id(cmclass));
	always_true = TRUE;
END;
$$ LANGUAGE PLPGSQL;
COMMENT ON FUNCTION _cm_create_class_default_order_indexes(character varying) IS 'TYPE: function|CATEGORIES: system';