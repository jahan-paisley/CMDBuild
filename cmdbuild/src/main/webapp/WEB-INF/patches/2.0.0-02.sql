-- Alter workflow tables

CREATE OR REPLACE FUNCTION _cm_disable_triggers_recursively(SuperClass regclass) RETURNS VOID AS $$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::regclass ||' DISABLE TRIGGER USER';
	END LOOP;
END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_enable_triggers_recursively(SuperClass regclass) RETURNS VOID AS $$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::text ||' ENABLE TRIGGER USER';
	END LOOP;
END;
$$
LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _patch_prevexecutors(Class regclass, Id integer) RETURNS varchar[] AS $$
DECLARE
	ClassName text := _cm_cmtable(Class);
	ClassHistory regclass := _cm_history_dbname(ClassName);
	RetVal varchar[];
BEGIN
	EXECUTE 'SELECT ARRAY(SELECT DISTINCT Q."NextExecutor" FROM (
				SELECT "NextExecutor" FROM '|| Class ||' WHERE "Id"=$1
				UNION
				SELECT "NextExecutor" FROM '|| ClassHistory ||' WHERE "CurrentId"=$1
			) AS Q WHERE Q."NextExecutor" IS NOT NULL)' INTO RetVal USING Id;
	RETURN RetVal;
END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION patch_200_02() RETURNS VOID AS $$
DECLARE
	Activity regclass := '"Activity"'::regclass;
BEGIN
	RAISE INFO 'Altering Activity table';

	PERFORM _cm_disable_triggers_recursively(Activity);

	RAISE INFO '... drop unused columns';

	ALTER TABLE "Activity" DROP COLUMN "Priority";
	ALTER TABLE "Activity" DROP COLUMN "IsQuickAccept";
	ALTER TABLE "Activity" DROP COLUMN "ActivityDescription";

	RAISE INFO '... add new columns';

	ALTER TABLE "Activity" ADD COLUMN "ActivityInstanceId" varchar[];
	ALTER TABLE "Activity" ADD COLUMN "PrevExecutors" varchar[];
	UPDATE "Activity" SET "PrevExecutors" = CASE
			WHEN "Status" = 'A' THEN _patch_prevexecutors("Activity"."IdClass", "Activity"."Id")
			ELSE ARRAY[]::varchar[]
		END;
	ALTER TABLE "Activity" ADD COLUMN "UniqueProcessDefinition" text;

	RAISE INFO '... alter old columns';

	ALTER TABLE "Activity" ALTER COLUMN "ProcessCode" TYPE text;
	ALTER TABLE "Activity" ALTER COLUMN "ActivityDefinitionId" TYPE varchar[] USING ARRAY["ActivityDefinitionId"::varchar];
	ALTER TABLE "Activity" ALTER COLUMN "NextExecutor" TYPE varchar[] USING ARRAY["NextExecutor"::varchar];

	RAISE INFO '... update values (keep history intact but clear the current open activities)';

	UPDATE "Activity" SET "ActivityDefinitionId" = ARRAY[]::varchar[] WHERE "Status"='A' OR "Code" IS NULL;
	UPDATE "Activity" SET "ActivityInstanceId" = CASE
			WHEN "Status"='A' OR "Code" IS NULL THEN ARRAY[]::varchar[]
			ELSE ARRAY[null::varchar]
		END;
	UPDATE "Activity" SET "NextExecutor" = ARRAY[]::varchar[] WHERE "Status"='A' OR "Code" IS NULL;

	UPDATE "Activity" SET "Code" = NULL;

	PERFORM _cm_enable_triggers_recursively(Activity);

	PERFORM _cm_set_attribute_comment(Activity, 'FlowStatus', 'MODE: read|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus');
	PERFORM _cm_set_attribute_comment(Activity, 'ProcessCode', 'MODE: reserved|DESCR: Process Instance Id');
	PERFORM _cm_set_attribute_comment(Activity, 'ActivityInstanceId', 'MODE: reserved|DESCR: Activity Instance Ids');
	PERFORM _cm_set_attribute_comment(Activity, 'NextExecutor', 'MODE: reserved|DESCR: Activity Instance performers');
	PERFORM _cm_set_attribute_comment(Activity, 'PrevExecutors', 'MODE: reserved|DESCR: Process Instance performers up to now');

	PERFORM _cm_set_attribute_comment(Activity, 'UniqueProcessDefinition', 'MODE: reserved|DESCR: Unique Process Definition (for speed)');
	PERFORM _cm_set_attribute_comment(Activity, 'ActivityDefinitionId', 'MODE: reserved|DESCR: Activity Definition Ids (for speed)');

END
$$ LANGUAGE PLPGSQL;

SELECT patch_200_02();

DROP FUNCTION _patch_prevexecutors(Class regclass, Id integer);
DROP FUNCTION patch_200_02();
