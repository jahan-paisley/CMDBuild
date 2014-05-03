-- Add Email field to User and Role

ALTER TABLE "User" ADD COLUMN "Email" character varying(320);
COMMENT ON COLUMN "User"."Email" IS 'MODE: write|DESCR: Email|INDEX: 5';

ALTER TABLE "Role" ADD COLUMN "Email" character varying(320);
COMMENT ON COLUMN "Role"."Email" IS 'MODE: write|DESCR: Email|INDEX: 5';
