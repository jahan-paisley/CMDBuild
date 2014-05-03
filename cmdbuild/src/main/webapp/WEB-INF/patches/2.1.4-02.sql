-- Update Scheduler table to manage Email service

CREATE OR REPLACE FUNCTION patch_214_02() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'Add JobType and Runnig columns';

	PERFORM cm_create_class_attribute('Scheduler', 'JobType', 'text', null, false, false, 'MODE: write|DESCR: JobType|STATUS: active');
	PERFORM cm_create_class_attribute('Scheduler', 'Running', 'boolean', null, false, false, 'MODE: write|DESCR: Running|STATUS: active');

	RAISE INFO 'fill JobType and Runnig columns for existing columns';

	UPDATE "Scheduler"
	SET "JobType" = 'workflow', "Running" = TRUE
	WHERE "Status" = 'A';

END

$$ LANGUAGE PLPGSQL;

SELECT patch_214_02();

DROP FUNCTION patch_214_02();