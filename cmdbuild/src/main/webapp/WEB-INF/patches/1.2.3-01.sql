-- Add Id to relations

ALTER TABLE "Map" ADD COLUMN "Id" integer NOT NULL DEFAULT nextval(('class_seq'::text)::regclass);
COMMENT ON COLUMN "Map"."Id" IS 'MODE: reserved';

DROP VIEW system_relationlist;
CREATE VIEW system_relationlist AS 
         SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, "Map"."IdDomain"::integer AS iddomain, "Map"."IdClass1"::integer AS idclass1, "Map"."IdObj1" AS idobj1, "Map"."IdClass2"::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, system_read_comment(pg_description0.description::character varying, 'DESCRDIR'::character varying)::text AS domaindescription, system_read_comment(pg_description0.description::character varying, 'MASTERDETAIL'::character varying)::text AS domainmasterdetail, system_read_comment(pg_description0.description::character varying, 'CARDIN'::character varying)::text AS domaincardinality, system_read_comment(pg_description2.description::character varying, 'DESCR'::character varying)::text AS classdescription, true AS direct, NULL::unknown AS version
           FROM "Map"
      JOIN "Class" ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid AND "Class"."Id" = "Map"."IdObj2" AND "Class"."Status" = 'A'::bpchar
   LEFT JOIN pg_class pg_class0 ON pg_class0.oid = "Map"."IdDomain"::oid
   LEFT JOIN pg_description pg_description0 ON pg_description0.objoid = pg_class0.oid AND pg_description0.objsubid = 0 AND system_check_comment(pg_description0.description::character varying, 'TYPE'::character varying, 'domain'::character varying) AND system_check_comment(pg_description0.description::character varying, 'STATUS'::character varying, 'active'::character varying)
   LEFT JOIN pg_class pg_class1 ON pg_class1.oid = "Map"."IdClass1"::oid
   LEFT JOIN pg_description pg_description1 ON pg_description1.objoid = pg_class1.oid AND pg_description1.objsubid = 0
   LEFT JOIN pg_class pg_class2 ON pg_class2.oid = "Map"."IdClass2"::oid
   LEFT JOIN pg_description pg_description2 ON pg_description2.objoid = pg_class2.oid AND pg_description2.objsubid = 0
UNION 
         SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, "Map"."IdDomain"::integer AS iddomain, "Map"."IdClass2"::integer AS idclass1, "Map"."IdObj2" AS idobj1, "Map"."IdClass1"::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, system_read_comment(pg_description0.description::character varying, 'DESCRINV'::character varying)::text AS domaindescription, system_read_comment(pg_description0.description::character varying, 'MASTERDETAIL'::character varying)::text AS domainmasterdetail, system_read_comment(pg_description0.description::character varying, 'CARDIN'::character varying)::text AS domaincardinality, system_read_comment(pg_description1.description::character varying, 'DESCR'::character varying)::text AS classdescription, false AS direct, NULL::unknown AS version
           FROM "Map"
      JOIN "Class" ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid AND "Class"."Id" = "Map"."IdObj1" AND "Class"."Status" = 'A'::bpchar
   LEFT JOIN pg_class pg_class0 ON pg_class0.oid = "Map"."IdDomain"::oid
   LEFT JOIN pg_description pg_description0 ON pg_description0.objoid = pg_class0.oid AND pg_description0.objsubid = 0 AND system_check_comment(pg_description0.description::character varying, 'TYPE'::character varying, 'domain'::character varying) AND system_check_comment(pg_description0.description::character varying, 'STATUS'::character varying, 'active'::character varying)
   LEFT JOIN pg_class pg_class1 ON pg_class1.oid = "Map"."IdClass1"::oid
   LEFT JOIN pg_description pg_description1 ON pg_description1.objoid = pg_class1.oid AND pg_description1.objsubid = 0
   LEFT JOIN pg_class pg_class2 ON pg_class2.oid = "Map"."IdClass2"::oid
   LEFT JOIN pg_description pg_description2 ON pg_description2.objoid = pg_class2.oid AND pg_description2.objsubid = 0;

DROP VIEW system_relationlist_history;
CREATE VIEW system_relationlist_history AS 
         SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, "Map"."IdDomain"::integer AS iddomain, "Map"."IdClass1"::integer AS idclass1, "Map"."IdObj1" AS idobj1, "Map"."IdClass2"::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."Status" AS status, system_read_comment(pg_description0.description::character varying, 'DESCRDIR'::character varying)::text AS domaindescription, system_read_comment(pg_description0.description::character varying, 'MASTERDETAIL'::character varying)::text AS domainmasterdetail, system_read_comment(pg_description0.description::character varying, 'CARDIN'::character varying)::text AS domaincardinality, system_read_comment(pg_description2.description::character varying, 'DESCR'::character varying)::text AS classdescription, true AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::unknown AS version
           FROM "Map"
      LEFT JOIN "Class" ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid AND "Class"."Id" = "Map"."IdObj2", pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2
     WHERE "Map"."Status" = 'U'::bpchar AND pg_class1.oid = "Map"."IdClass1"::oid AND pg_class2.oid = "Map"."IdClass2"::oid AND pg_class0.oid = "Map"."IdDomain"::oid AND pg_description0.objoid = pg_class0.oid AND pg_description0.objsubid = 0 AND pg_description1.objoid = pg_class1.oid AND pg_description1.objsubid = 0 AND pg_description2.objoid = pg_class2.oid AND pg_description2.objsubid = 0 AND system_check_comment(pg_description0.description::character varying, 'TYPE'::character varying, 'domain'::character varying) AND system_check_comment(pg_description0.description::character varying, 'STATUS'::character varying, 'active'::character varying)
UNION 
         SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, "Map"."IdDomain"::integer AS iddomain, "Map"."IdClass2"::integer AS idclass1, "Map"."IdObj2" AS idobj1, "Map"."IdClass1"::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."Status" AS status, system_read_comment(pg_description0.description::character varying, 'DESCRINV'::character varying)::text AS domaindescription, system_read_comment(pg_description0.description::character varying, 'MASTERDETAIL'::character varying)::text AS domainmasterdetail, system_read_comment(pg_description0.description::character varying, 'CARDIN'::character varying)::text AS domaincardinality, system_read_comment(pg_description2.description::character varying, 'DESCR'::character varying)::text AS classdescription, false AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::unknown AS version
           FROM "Map"
      LEFT JOIN "Class" ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid AND "Class"."Id" = "Map"."IdObj1", pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2
     WHERE "Map"."Status" = 'U'::bpchar AND pg_class1.oid = "Map"."IdClass1"::oid AND pg_class2.oid = "Map"."IdClass2"::oid AND pg_class0.oid = "Map"."IdDomain"::oid AND pg_description0.objoid = pg_class0.oid AND pg_description0.objsubid = 0 AND pg_description1.objoid = pg_class1.oid AND pg_description1.objsubid = 0 AND pg_description2.objoid = pg_class2.oid AND pg_description2.objsubid = 0 AND system_check_comment(pg_description0.description::character varying, 'TYPE'::character varying, 'domain'::character varying) AND system_check_comment(pg_description0.description::character varying, 'STATUS'::character varying, 'active'::character varying);

ALTER TABLE "Report" ALTER COLUMN "Id" SET DEFAULT nextval('class_seq'::regclass);


CREATE OR REPLACE FUNCTION patch_123_01() RETURNS VOID AS $$
DECLARE
	OldReportId int;
	NewReportId int;
BEGIN
	RAISE INFO 'Changing report ids...';
	FOR OldReportId IN SELECT "Id" FROM "Report" LOOP
		NewReportId := nextval('class_seq');
		UPDATE "Menu" SET "IdElementObj" = NewReportId WHERE "IdElementClass"='"Report"'::regclass AND "IdElementObj"=OldReportId;
		UPDATE "Report" SET "Id" = NewReportId WHERE "Id" = OldReportId;
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_123_01();

DROP FUNCTION patch_123_01();
