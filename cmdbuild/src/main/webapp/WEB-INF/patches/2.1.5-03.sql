-- Update User column size

CREATE OR REPLACE FUNCTION patch_215_03() RETURNS void AS $$
DECLARE
	classid oid;
	query varchar;
	NEW_SIZE integer := 100;
BEGIN
	DROP VIEW IF EXISTS system_relationlist_history;
	DROP VIEW IF EXISTS system_privilegescatalog;

	EXECUTE 'ALTER TABLE "Class" ALTER COLUMN "User" TYPE character varying(' || NEW_SIZE || ');';
	EXECUTE 'ALTER TABLE "Report" ALTER COLUMN "User" TYPE character varying(' || NEW_SIZE || ');';

	EXECUTE 'ALTER TABLE "Map" ALTER COLUMN "User" TYPE character varying(' || NEW_SIZE || ');';

	FOR classid IN (SELECT * FROM _cm_class_list()) LOOP
		IF (_cm_is_simpleclass(classid)) THEN
			EXECUTE 'ALTER TABLE ' || classId::regclass::varchar || ' ALTER COLUMN "User" TYPE character varying(' || NEW_SIZE || ');';
		END IF;
	END LOOP;

	CREATE OR REPLACE VIEW system_relationlist_history AS
		SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, "Map"."IdDomain"::integer AS iddomain, "Map"."IdClass1"::integer AS idclass1, "Map"."IdObj1" AS idobj1, "Map"."IdClass2"::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."Status" AS status, _cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text)::text AS domaindescription, _cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text)::text AS domainmasterdetail, _cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text)::text AS domaincardinality, _cm_legacy_read_comment(pg_description2.description, 'DESCR'::text)::text AS classdescription, true AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::text AS version
			FROM "Map"
			LEFT JOIN "Class" ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid AND "Class"."Id" = "Map"."IdObj2", pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2
			WHERE "Map"."Status" = 'U'::bpchar AND pg_class1.oid = "Map"."IdClass1"::oid AND pg_class2.oid = "Map"."IdClass2"::oid AND pg_class0.oid = "Map"."IdDomain"::oid AND pg_description0.objoid = pg_class0.oid AND pg_description0.objsubid = 0 AND pg_description1.objoid = pg_class1.oid AND pg_description1.objsubid = 0 AND pg_description2.objoid = pg_class2.oid AND pg_description2.objsubid = 0 AND _cm_is_domain_comment(pg_description0.description) AND _cm_is_active_comment(pg_description0.description)
		UNION
		SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, "Map"."IdDomain"::integer AS iddomain, "Map"."IdClass2"::integer AS idclass1, "Map"."IdObj2" AS idobj1, "Map"."IdClass1"::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."Status" AS status, _cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text)::text AS domaindescription, _cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text)::text AS domainmasterdetail, _cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text)::text AS domaincardinality, _cm_legacy_read_comment(pg_description2.description, 'DESCR'::text)::text AS classdescription, false AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::text AS version
			FROM "Map"
			LEFT JOIN "Class" ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid AND "Class"."Id" = "Map"."IdObj1", pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2
			WHERE "Map"."Status" = 'U'::bpchar AND pg_class1.oid = "Map"."IdClass1"::oid AND pg_class2.oid = "Map"."IdClass2"::oid AND pg_class0.oid = "Map"."IdDomain"::oid AND pg_description0.objoid = pg_class0.oid AND pg_description0.objsubid = 0 AND pg_description1.objoid = pg_class1.oid AND pg_description1.objsubid = 0 AND pg_description2.objoid = pg_class2.oid AND pg_description2.objsubid = 0 AND _cm_is_domain_comment(pg_description0.description) AND _cm_is_active_comment(pg_description0.description);

	CREATE OR REPLACE VIEW system_privilegescatalog AS
		SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode"
			FROM (
				SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode"
					FROM "Grant"
				UNION
				SELECT (-1), '"Grant"'::regclass AS regclass, ''::character varying AS "varchar", ''::character varying AS "varchar", 'A'::bpchar AS bpchar, 'admin'::character varying AS "varchar", now() AS now, NULL::text AS unknown, "Role"."Id", system_classcatalog.classid::regclass AS classid, '-'::character varying AS "varchar"
					FROM system_classcatalog, "Role"
					WHERE system_classcatalog.classid::regclass::oid <> '"Class"'::regclass::oid AND NOT ("Role"."Id"::text || system_classcatalog.classid::integer::text IN (SELECT "Grant"."IdRole"::text || "Grant"."IdGrantedClass"::oid::integer::text FROM "Grant"))) permission
				JOIN system_classcatalog ON permission."IdGrantedClass"::oid = system_classcatalog.classid AND (_cm_legacy_read_comment(system_classcatalog.classcomment::character varying::text, 'MODE'::character varying::text)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text]))
				ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";
END
$$ LANGUAGE PLPGSQL;

SELECT patch_215_03();
DROP FUNCTION patch_215_03();