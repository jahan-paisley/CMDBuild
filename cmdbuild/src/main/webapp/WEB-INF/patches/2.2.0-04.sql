-- Migrate legacy scheduler job parameters

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
DECLARE
	schedulerJob record;
BEGIN
	RAISE INFO 'disabling triggers';
	ALTER TABLE "_SchedulerJob" DISABLE TRIGGER ALL;

	RAISE INFO 'moving data';
	FOR schedulerJob IN (
		SELECT *
			FROM "_SchedulerJob"
			WHERE "JobType" = 'workflow' AND "Status" = 'A'
		) LOOP
		RAISE DEBUG 'moving data for element with id %', schedulerJob."Id";
		INSERT INTO "_SchedulerJobParameter" ("SchedulerId", "Key", "Value")
			VALUES
				(schedulerJob."Id", 'classname', schedulerJob."Detail"),
				(schedulerJob."Id", 'attributes', schedulerJob."Notes");
	END LOOP;

	RAISE DEBUG 'changing not-null attribute constraint';
	PERFORM _cm_attribute_set_notnull('"_SchedulerJob"'::regclass::oid, 'Detail', false);

	RAISE INFO 'clean unused columns';
	UPDATE "_SchedulerJob"
		SET "Code" = NULL, "Notes" = NULL
		WHERE "JobType" = 'workflow' AND "Status" = 'A';
	UPDATE "_SchedulerJob"
		SET "Detail" = NULL;

	RAISE DEBUG 'removing unused attribute';
	PERFORM cm_delete_attribute('"_SchedulerJob"'::regclass::oid, 'Detail');

	RAISE INFO 'enabling triggers';
	ALTER TABLE "_SchedulerJob" ENABLE TRIGGER ALL;
END;
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION IF EXISTS apply_patch();