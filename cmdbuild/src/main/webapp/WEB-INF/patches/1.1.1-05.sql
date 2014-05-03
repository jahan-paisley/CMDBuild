-- Fix report unique names

ALTER TABLE "Report" DROP CONSTRAINT "Report_unique_code";

CREATE UNIQUE INDEX "Report_unique_code"
  ON "Report"
  USING btree
  ((
CASE
    WHEN "Code"::text = ''::text OR "Status"::text <> 'A'::text THEN NULL::text
    ELSE "Code"::text
END));
