-- Add columns to bim tables

CREATE OR REPLACE FUNCTION patch_220_03() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'add column to _BimProject';
	PERFORM cm_create_class_attribute('_BimProject', 'ExportProjectId', 'varchar', null, false, false, 'MODE: write|DESCR: ExportProjectId|INDEX: 9|STATUS: active');
	PERFORM cm_create_class_attribute('_BimProject', 'ShapesProjectId', 'varchar', null, false, false, 'MODE: write|DESCR: ShapesProjectId|INDEX: 10|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'RootReference', 'varchar', null, false, false, 'MODE: write|DESCR: RootReference|INDEX: 6|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_220_03();

DROP FUNCTION patch_220_03();