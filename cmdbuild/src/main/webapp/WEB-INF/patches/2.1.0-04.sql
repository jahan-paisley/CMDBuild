-- Create Filter, Widget, View table. Import data from Metadata table

SELECT cm_create_class('_Filter', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Filter|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'IdOwner', 'int', null, false, false, 'MODE: write|DESCR: IdOwner|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Filter', 'text', null, false, false, 'MODE: write|DESCR: Filter|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'IdSourceClass', 'regclass', null, true, false, 'MODE: write|DESCR: Class Reference|INDEX: 5|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Template', 'boolean', 'false', true, false, 'MODE: write|DESCR: User or group filter|INDEX: 6|STATUS: active');

ALTER TABLE "_Filter" ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "IdOwner", "IdSourceClass");

DROP TABLE IF EXISTS "_Widget" CASCADE;

SELECT cm_create_class('_Widget', 'Class', 'MODE: reserved|TYPE: class|DESCR: Widget|SUPERCLASS: false|STATUS: active');

CREATE OR REPLACE FUNCTION widget_table_creation_and_import_from_metadata() RETURNS VOID AS $$
DECLARE
	targetClass varchar;
	widgets text;
	metadataToRemoveId integer;
	singleWidget text;
	
BEGIN
	FOR metadataToRemoveId, targetClass, widgets IN
			SELECT "Id" as id, "Code" as code, "Notes" as notes
				FROM "Metadata"
				WHERE "Metadata"."Status" = 'A' AND "Description" = 'system.widgets'
	LOOP

	RAISE INFO 'widgets: %, targetClass: %', widgets, targetClass; 

		FOR singleWidget IN 	 
			SELECT * FROM regexp_split_to_table(
			       regexp_replace(
				       regexp_replace(widgets, E'(^\\[|\\]$)', '', 'g'),
				       E'},{',
				       '}|{',
				       'g'),
			       E'\\|') 
		LOOP

		RAISE INFO 'single widget to insert: %', singleWidget;	

		INSERT INTO "_Widget" ("Code", "Description") VALUES(targetClass, singleWidget);

		END LOOP;

		RAISE INFO 'deleting widgets from Metadata table...';

		PERFORM cm_delete_card(metadataToRemoveId, _cm_table_id('Metadata'));

	END LOOP;


END
$$ LANGUAGE PLPGSQL;

SELECT widget_table_creation_and_import_from_metadata();

DROP FUNCTION widget_table_creation_and_import_from_metadata();

CREATE OR REPLACE FUNCTION createViewTable() RETURNS VOID AS $$

BEGIN

	RAISE INFO 'Creating _View table';
	PERFORM cm_create_class('_View', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
	PERFORM cm_create_class_attribute('_View', 'Name', 'character varying', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'Filter', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'IdSourceClass', 'regclass', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'SourceFunction', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'Type', 'character varying', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

END
$$ LANGUAGE PLPGSQL;

SELECT createViewTable();

DROP FUNCTION createViewTable();
