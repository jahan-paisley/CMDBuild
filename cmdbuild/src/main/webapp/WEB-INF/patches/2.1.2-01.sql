-- Add table to store CMDBf MdrScopedId

CREATE OR REPLACE FUNCTION patch_212_01() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class('_MdrScopedId', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
	PERFORM cm_create_class_attribute('_MdrScopedId', 'MdrScopedId', 'text', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_MdrScopedId', 'IdItem', 'int4', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_212_01();

DROP FUNCTION patch_212_01();
