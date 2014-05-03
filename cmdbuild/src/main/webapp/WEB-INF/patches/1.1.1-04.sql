-- Add a column in report table to manage images and add a unique constraint for the report name

ALTER TABLE "Report" ADD COLUMN "ImagesName" character varying[];
COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: reserved';

-- Create a fake constraint to be removed by the next patch
-- for the databases where this patch does not work
ALTER TABLE "Report" ADD CONSTRAINT "Report_unique_code" UNIQUE("Id");
