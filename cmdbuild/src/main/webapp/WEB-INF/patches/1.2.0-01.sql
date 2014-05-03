-- Force group name not null and unique

ALTER TABLE "Role" ADD CONSTRAINT code_notnull CHECK ("Status"<>'A' OR "Code" IS NOT NULL);
ALTER TABLE "Role" ADD CONSTRAINT unique_role_code UNIQUE("Code");
