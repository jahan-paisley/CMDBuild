-- Create the table to manage Email templates

CREATE OR REPLACE FUNCTION patch_214_01() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'creating _EmailTemplate table';

	PERFORM cm_create_class('_EmailTemplate', NULL, 'MODE: reserved|TYPE: class|DESCR: Email Templates|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'Owner', 'regclass', null, false, false, 'MODE: write|DESCR: Class owner|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'From', 'text', null, false, false, 'MODE: write|DESCR: From|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'To', 'text', null, false, false, 'MODE: write|DESCR: To|INDEX: 3|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'CC', 'text', null, false, false, 'MODE: write|DESCR: CC|INDEX: 4|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'BCC', 'text', null, false, false, 'MODE: write|DESCR: BCC|INDEX: 5|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'Subject', 'text', null, false, false, 'MODE: write|DESCR: Subject|INDEX: 6|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'Body', 'text', null, false, false, 'MODE: write|DESCR: Body|INDEX: 7|STATUS: active');

	PERFORM _cm_attribute_set_uniqueness('"_EmailTemplate"'::regclass::oid, 'Code', TRUE);
END

$$ LANGUAGE PLPGSQL;

SELECT patch_214_01();

DROP FUNCTION patch_214_01();