-- Changing User and Role tables to standard classes

CREATE OR REPLACE FUNCTION patch_212_02() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'creating backup schema';
	CREATE SCHEMA "backup_for_2_1_2";

	-- 'User' table
	RAISE INFO 'changing schema for actual User class';
	COMMENT ON TABLE "User" IS '';
	ALTER TABLE "User" SET SCHEMA "backup_for_2_1_2";

	RAISE INFO 'creating new User class';
	PERFORM cm_create_class('User', 'Class', 'MODE: reserved|TYPE: class|DESCR: Users|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('User', 'Username', 'varchar(40)', null, true, true, 'MODE: read|DESCR: Username|INDEX: 5|BASEDSP: true|STATUS: active');
	PERFORM cm_create_class_attribute('User', 'Password', 'varchar(40)', null, false, false, 'MODE: read|DESCR: Password|INDEX: 6|BASEDSP: false|STATUS: active');
	PERFORM cm_create_class_attribute('User', 'Email', 'varchar(320)', null, false, false, 'MODE: read|DESCR: Email|INDEX: 7');
	PERFORM cm_create_class_attribute('User', 'Active', 'boolean', 'true', true, false, 'MODE: read');

	RAISE INFO 'copying data into User table';
	ALTER TABLE "User" DISABLE TRIGGER USER;
	INSERT INTO "User"
		("Id", "IdClass", "User", "BeginDate", "Code", "Description", "Status", "Notes", "Username", "Password", "Email", "Active")
		SELECT "Id", '"User"'::regclass, "User", "BeginDate", "Code", "Description", 'A' AS "Status", "Notes", "Username", "Password", "Email", "Status" = 'A' AS "Active"
			FROM "backup_for_2_1_2"."User";
	ALTER TABLE "User" ENABLE TRIGGER USER;


	-- 'Role' table
	RAISE INFO 'changing schema for actual Role class';
	COMMENT ON TABLE "Role" IS '';
	ALTER TABLE "Role" SET SCHEMA "backup_for_2_1_2";

	RAISE INFO 'creating new Role class';
	PERFORM cm_create_class('Role', 'Class', 'MODE: reserved|TYPE: class|DESCR: Groups|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('Role', 'Administrator', 'boolean', null, false, false, 'MODE: read|DESCR: Administrator|INDEX: 5|STATUS: active');
	PERFORM cm_create_class_attribute('Role', 'startingClass', 'regclass', null, false, false, 'MODE: read|DESCR: Starting Class|INDEX: 6|STATUS: active');
	PERFORM cm_create_class_attribute('Role', 'Email', 'varchar(320)', null, false, false, 'MODE: read|DESCR: Email|INDEX: 7');
	PERFORM cm_create_class_attribute('Role', 'DisabledModules', 'varchar[]', null, false, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'DisabledCardTabs', 'character varying[]', null, false, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'DisabledProcessTabs', 'character varying[]', null, false, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'HideSidePanel', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'FullScreenMode', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'SimpleHistoryModeForCard', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'SimpleHistoryModeForProcess', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'ProcessWidgetAlwaysEnabled', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'CloudAdmin', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'Active', 'boolean', 'true', true, false, 'MODE: read');

	RAISE INFO 'copying data into Role table';
	ALTER TABLE "Role" DISABLE TRIGGER USER;
	INSERT INTO "Role"
		("Id", "IdClass", "User", "BeginDate", "Code", "Description", "Status", "Notes", "Administrator", "startingClass", "Email", "DisabledModules", "DisabledCardTabs", "DisabledProcessTabs", "HideSidePanel", "FullScreenMode", "SimpleHistoryModeForCard", "SimpleHistoryModeForProcess", "ProcessWidgetAlwaysEnabled", "CloudAdmin", "Active")
		SELECT "Id", '"Role"'::regclass, "User", "BeginDate", "Code", "Description", 'A' AS "Status", "Notes", "Administrator", "startingClass", "Email", "DisabledModules", "DisabledCardTabs", "DisabledProcessTabs", "HideSidePanel", "FullScreenMode", "SimpleHistoryModeForCard", "SimpleHistoryModeForProcess", "ProcessWidgetAlwaysEnabled", "CloudAdmin", "Status" = 'A' AS "Active"
			FROM  "backup_for_2_1_2"."Role";
	ALTER TABLE "Role" ENABLE TRIGGER USER;

	
	-- 'Map_UserRole' table
	RAISE INFO 'changing regclasses into Map_UserRole table';
	ALTER TABLE "Map_UserRole" DISABLE TRIGGER USER;
	UPDATE "Map_UserRole" SET "IdClass1" = '"User"'::regclass, "IdClass2" = '"Role"'::regclass;
	ALTER TABLE "Map_UserRole" ENABLE TRIGGER USER;
END

$$ LANGUAGE PLPGSQL;

SELECT patch_212_02();

DROP FUNCTION patch_212_02();
