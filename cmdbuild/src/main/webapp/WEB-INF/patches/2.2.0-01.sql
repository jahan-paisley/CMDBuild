-- Create tables to manage Bim Module

CREATE OR REPLACE FUNCTION patch_220_01() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'create table _BimProject';

	PERFORM cm_create_class('_BimProject', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'ProjectId', 'varchar', null, true, true, 'MODE: write|DESCR: Project ID|INDEX: 3|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'Active', 'boolean', 'TRUE', true, false, 'MODE: write|DESCR: Active|INDEX: 4|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'LastCheckin', 'timestamp', null, false, false, 'MODE: write|DESCR: Last Checkin|INDEX: 5|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'Synchronized', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Synchronized|INDEX: 6|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'ImportMapping', 'text', null, false, false, 'MODE: write|DESCR: ImportMapping|INDEX: 7|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'ExportMapping', 'text', null, false, false, 'MODE: write|DESCR: ImportMapping|INDEX: 8|STATUS: active');

	RAISE INFO 'create table _BimLayer';
	
	PERFORM cm_create_class('_BimLayer', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'ClassName', 'varchar', null, true, true, 'MODE: write|DESCR: ClassName|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Root', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Root|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Active', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Active|INDEX: 3|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Export', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Export|INDEX: 4|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Container', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Container|INDEX: 5|STATUS: active');	
END

$$ LANGUAGE PLPGSQL;

SELECT patch_220_01();

DROP FUNCTION patch_220_01();
