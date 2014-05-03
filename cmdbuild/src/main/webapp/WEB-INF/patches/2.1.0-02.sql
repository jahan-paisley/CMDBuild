-- Alter "Report", "Menu", "Lookup" tables for the new DAO

CREATE OR REPLACE FUNCTION changeModeInLookupAttributes() RETURNS VOID AS $$
BEGIN
	COMMENT ON COLUMN "LookUp"."Type" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."ParentType" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."ParentId" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."Number" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."IsDefault" IS 'MODE: read';
	
	CREATE TRIGGER "_SanityCheck"
		BEFORE INSERT OR UPDATE OR DELETE
		ON "LookUp"
		FOR EACH ROW
		EXECUTE PROCEDURE _cm_trigger_sanity_check();
END
$$ LANGUAGE PLPGSQL;


SELECT changeModeInLookupAttributes();

DROP FUNCTION changeModeInLookupAttributes();

CREATE OR REPLACE FUNCTION updateMenuTable() RETURNS VOID AS $$
BEGIN
	RAISE INFO 'Creating new menu group reference column';
	ALTER TABLE "Menu" ADD COLUMN "GroupName" text;
	COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';

	RAISE INFO 'Copying menu group references';
	-- The default group has id 0 that is never stored. Use star to
	-- identify the default group menu items
	UPDATE "Menu" SET "GroupName" = (SELECT COALESCE((SELECT "Code" FROM "Role" WHERE "Id"="Menu"."IdGroup"), '*')) WHERE "Status"='A';

	-- Sync the type and code values that differs only for report
	UPDATE "Menu" SET "Type" = "Code" WHERE "Status"='A';

	-- Sync the dashboard management with the report and view management
	-- using the IdElementObj for the dashboard id
	UPDATE "Menu" SET "IdElementObj" = "IdElementClass", "IdElementClass" = '"_Dashboards"'::regclass WHERE "Code" = 'dashboard' AND "Status" = 'A'; 
	
	RAISE INFO 'Dropping old menu group reference column';
	ALTER TABLE "Menu" DROP COLUMN "IdGroup" CASCADE;

	RAISE INFO 'Menu table cleanup';
	ALTER TABLE "Menu" DISABLE TRIGGER USER;
	DELETE FROM "Menu" WHERE "Status"<>'A';
	ALTER TABLE "Menu" ENABLE TRIGGER USER;

	RAISE INFO 'Set to read the base column';
	COMMENT ON COLUMN "Menu"."IdParent" IS 'MODE: read|DESCR: Parent Item, 0 means no parent';
	COMMENT ON COLUMN "Menu"."IdElementClass" IS 'MODE: read|DESCR: Class connect to this item';
	COMMENT ON COLUMN "Menu"."IdElementObj" IS 'MODE: read|DESCR: Object connected to this item, 0 means no object';
	COMMENT ON COLUMN "Menu"."Number" IS 'MODE: read|DESCR: Ordering';
	COMMENT ON COLUMN "Menu"."Type" IS 'MODE: read';
	COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';

	ALTER TABLE "Menu" ALTER COLUMN "GroupName" SET NOT NULL;

END
$$ LANGUAGE PLPGSQL;

SELECT updateMenuTable();

DROP FUNCTION updateMenuTable();

CREATE OR REPLACE FUNCTION updateReportTableAttributes() RETURNS VOID AS $$
DECLARE
	ReportId int;
	ReportName text;
	GroupName varchar;
	AllGroups varchar[];
BEGIN
	RAISE INFO 'Creating new report privileges column';
	ALTER TABLE "Report" ADD COLUMN "NewGroups" character varying[];
	COMMENT ON COLUMN "Report"."NewGroups" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: read';

	RAISE INFO 'Copying report privileges';
	FOR ReportId, ReportName IN SELECT "Id", "Code" FROM "Report" LOOP
		RAISE INFO '... %', ReportName;
		AllGroups := NULL;
		FOR GroupName IN SELECT "Role"."Code" FROM "Role"
				JOIN "Report" ON "Report"."Id" = ReportId AND "Role"."Id" = ANY ("Report"."Groups")
		LOOP
			AllGroups := AllGroups || GroupName;
		END LOOP;
		RAISE INFO '... -> %', AllGroups;
		UPDATE "Report" SET "NewGroups" = AllGroups WHERE "Id" = ReportId;
	END LOOP;

	RAISE INFO 'Dropping old report privileges column';
	ALTER TABLE "Report" DROP COLUMN "Groups";
	ALTER TABLE "Report" RENAME "NewGroups"  TO "Groups";
END
$$ LANGUAGE PLPGSQL;

SELECT updateReportTableAttributes();

DROP FUNCTION updateReportTableAttributes();

CREATE OR REPLACE FUNCTION transformLookupIntoSimpleClass() RETURNS VOID AS $$
BEGIN
	RAISE INFO 'creating backup schema';
	CREATE SCHEMA "backup_lookup_21";
	ALTER TABLE "LookUp" SET SCHEMA "backup_lookup_21";

	RAISE INFO 'creating new table';
	PERFORM cm_create_class('LookUp', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Lookup list|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('LookUp', 'Code', 'character varying(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|BASEDSP: true');
	PERFORM cm_create_class_attribute('LookUp', 'Description', 'character varying(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|BASEDSP: true');
	PERFORM cm_create_class_attribute('LookUp', 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Annotazioni');
	PERFORM cm_create_class_attribute('LookUp', 'Type', 'character varying(64)', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'ParentType', 'character varying(64)', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'ParentId', 'integer', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'Number', 'integer', NULL, TRUE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'IsDefault', 'boolean', NULL, FALSE, FALSE, 'MODE: read');

	RAISE INFO 'copying data';
	INSERT INTO "LookUp"
		("Id", "IdClass", "Code", "Description", "ParentType", "ParentId", "Type", "IsDefault", "Number", "Notes", "Status")
		SELECT "Id", '"LookUp"'::regclass, "Code", "Description", "ParentType", "ParentId", "Type", "IsDefault", "Number", "Notes", "Status"
			FROM "backup_lookup_21"."LookUp";
END
$$ LANGUAGE PLPGSQL;


SELECT transformLookupIntoSimpleClass();

DROP FUNCTION transformLookupIntoSimpleClass();


CREATE OR REPLACE FUNCTION changeModeToReportAttributes() RETURNS VOID AS $$
BEGIN
	RAISE INFO 'Changing mode to reserved to some attributes of Report table...';
	COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."Images" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."ImagesLength" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."ReportLength" IS 'MODE: reserved';
END
$$ LANGUAGE PLPGSQL;

SELECT changeModeToReportAttributes();

DROP FUNCTION changeModeToReportAttributes();
