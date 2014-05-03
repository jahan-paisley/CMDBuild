-- Add table to store the configuration of a domains based tree

CREATE OR REPLACE FUNCTION patch_203_01() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class('_DomainTreeNavigation', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'IdParent', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'IdGroup', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'DomainName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'Direct', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'BaseNode', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'TargetClassName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'TargetClassDescription', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_203_01();

DROP FUNCTION patch_203_01();