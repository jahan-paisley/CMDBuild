-- Changing scheduler tables in task tables

CREATE OR REPLACE FUNCTION apply_patch() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'renaming _SchedulerJob table to _Task';
	ALTER TABLE "_SchedulerJob" RENAME TO "_Task";
	ALTER TABLE "_SchedulerJob_history" RENAME TO "_Task_history";

	RAISE INFO 'renaming _Task.JobType column to _Task.Type';
	ALTER TABLE "_Task" RENAME COLUMN "JobType" TO "Type";
	
	RAISE INFO 'updating _Task.CronExpression attribute definition';
	PERFORM cm_modify_class_attribute('_Task', 'CronExpression', 'text', null, false, false, 'MODE: write|DESCR: Cron Expression|STATUS: active');

	RAISE INFO 'updating _Task.CronExpression removing seconds';
	ALTER TABLE "_Task" DISABLE TRIGGER USER;
	UPDATE "_Task" SET "CronExpression" = substr("CronExpression", 3);	
	ALTER TABLE "_Task" ENABLE TRIGGER USER;

	RAISE INFO 'renaming _SchedulerJobParameter table to _TaskParameter';
	ALTER TABLE "_SchedulerJobParameter" RENAME TO "_TaskParameter";
	ALTER TABLE "_SchedulerJobParameter_history" RENAME TO "_TaskParameter_history";

	RAISE INFO 'renaming _TaskParameter.SchedulerId column to _TaskParameter.Owner';
	ALTER TABLE "_TaskParameter" RENAME COLUMN "SchedulerId" TO "Owner";
	
	RAISE INFO 'updating _TaskParameter.Key attribute definition';
	PERFORM cm_modify_class_attribute('_TaskParameter', 'Key', 'text', null, true, false, 'MODE: write|DESCR: Key|INDEX: 2|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION apply_patch();