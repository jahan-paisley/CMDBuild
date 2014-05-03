-- Relation list view improvement

CREATE OR REPLACE FUNCTION system_check_comment(character varying, character varying, character varying)
  RETURNS BOOLEAN AS
$BODY$
    DECLARE
        classComment ALIAS FOR $1;
        commentName ALIAS FOR $2;
	commentValue ALIAS FOR $3;
    BEGIN
	RETURN (LOWER(system_read_comment(classComment, commentName)) = LOWER(commentValue));
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE VIEW system_relationlist AS 
	SELECT
		pg_class1.relname AS class1,
		pg_class2.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass1"::integer AS idclass1,
		"Map"."IdObj1" AS idobj1,
		"Map"."IdClass2"::integer AS idclass2,
		"Map"."IdObj2" AS idobj2,
		"Map"."BeginDate" AS begindate,
		"Map"."Status" AS status,
		system_read_comment(pg_description0.description, 'DESCRDIR')::text AS domaindescription,
		system_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		system_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		system_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
		TRUE AS direct,
		NULL AS version
	FROM "Map"
	JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid
		AND "Class"."Id" = "Map"."IdObj2"
		AND "Class"."Status" = 'A'::bpchar
	LEFT JOIN pg_class pg_class0
		ON pg_class0.oid = "Map"."IdDomain"::oid
	LEFT JOIN pg_description pg_description0
		ON pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND system_check_comment(pg_description0.description, 'TYPE', 'domain')
		AND system_check_comment(pg_description0.description, 'STATUS', 'active')
	LEFT JOIN pg_class pg_class1
		ON pg_class1.oid = "Map"."IdClass1"::oid
	LEFT JOIN pg_description pg_description1
		ON pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
	LEFT JOIN pg_class pg_class2
		ON pg_class2.oid = "Map"."IdClass2"::oid
	LEFT JOIN pg_description pg_description2
		ON pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0
UNION
	SELECT
		pg_class2.relname AS class1,
		pg_class1.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass2"::integer AS idclass1,
		"Map"."IdObj2" AS idobj1,
		"Map"."IdClass1"::integer AS idclass2,
		"Map"."IdObj1" AS idobj2,
		"Map"."BeginDate" AS begindate,
		"Map"."Status" AS status, 
		system_read_comment(pg_description0.description, 'DESCRINV')::text AS domaindescription,
		system_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		system_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		system_read_comment(pg_description1.description, 'DESCR')::text AS classdescription,
		FALSE AS direct,
		NULL AS version
	FROM "Map"
	JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid
		AND "Class"."Id" = "Map"."IdObj1"
		AND "Class"."Status" = 'A'::bpchar
	LEFT JOIN pg_class pg_class0
		ON pg_class0.oid = "Map"."IdDomain"::oid
	LEFT JOIN pg_description pg_description0
		ON pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND system_check_comment(pg_description0.description, 'TYPE', 'domain')
		AND system_check_comment(pg_description0.description, 'STATUS', 'active')
	LEFT JOIN pg_class pg_class1
		ON pg_class1.oid = "Map"."IdClass1"::oid
	LEFT JOIN pg_description pg_description1
		ON pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
	LEFT JOIN pg_class pg_class2
		ON pg_class2.oid = "Map"."IdClass2"::oid
	LEFT JOIN pg_description pg_description2
		ON pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0;


CREATE OR REPLACE VIEW system_relationlist_history AS 
	SELECT
		pg_class1.relname AS class1,
		pg_class2.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass1"::integer AS idclass1,
		"Map"."IdObj1" AS idobj1,
		"Map"."IdClass2"::integer AS idclass2,
		"Map"."IdObj2" AS idobj2,
		"Map"."Status" AS status,
		system_read_comment(pg_description0.description, 'DESCRDIR')::text AS domaindescription,
		system_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		system_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		system_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
		TRUE AS direct,
		"Map"."User" AS username,
		"Map"."BeginDate" AS begindate,
		"Map"."EndDate" AS enddate,
		NULL AS version
	FROM "Map"
	LEFT JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass2"::oid
		AND "Class"."Id" = "Map"."IdObj2",
		pg_class pg_class0,
		pg_description pg_description0,
		pg_class pg_class1,
		pg_description pg_description1,
		pg_class pg_class2,
		pg_description pg_description2
	WHERE
		"Map"."Status" = 'U'
		AND pg_class1.oid = "Map"."IdClass1"::oid
		AND pg_class2.oid = "Map"."IdClass2"::oid
		AND pg_class0.oid = "Map"."IdDomain"::oid
		AND pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
		AND pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0
		AND system_check_comment(pg_description0.description, 'TYPE', 'domain')
		AND system_check_comment(pg_description0.description, 'STATUS', 'active')
UNION
	SELECT
		pg_class2.relname AS class1,
		pg_class1.relname AS class2,
		"Class"."Code" AS fieldcode,
		"Class"."Description" AS fielddescription,
		pg_class0.relname AS realname,
		"Map"."IdDomain"::integer AS iddomain,
		"Map"."IdClass2"::integer AS idclass1,
		"Map"."IdObj2" AS idobj1,
		"Map"."IdClass1"::integer AS idclass2,
		"Map"."IdObj1" AS idobj2,
		"Map"."Status" AS status,
		system_read_comment(pg_description0.description, 'DESCRINV')::text AS domaindescription,
		system_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		system_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		system_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
		FALSE AS direct,
		"Map"."User" AS username,
		"Map"."BeginDate" AS begindate,
		"Map"."EndDate" AS enddate,
		NULL AS version
	FROM "Map"
	LEFT JOIN "Class"
		ON "Class"."IdClass"::oid = "Map"."IdClass1"::oid
		AND "Class"."Id" = "Map"."IdObj1",
		pg_class pg_class0,
		pg_description pg_description0,
		pg_class pg_class1,
		pg_description pg_description1,
		pg_class pg_class2,
		pg_description pg_description2
	WHERE
		"Map"."Status" = 'U'
		AND pg_class1.oid = "Map"."IdClass1"::oid
		AND pg_class2.oid = "Map"."IdClass2"::oid
		AND pg_class0.oid = "Map"."IdDomain"::oid
		AND pg_description0.objoid = pg_class0.oid
		AND pg_description0.objsubid = 0
		AND pg_description1.objoid = pg_class1.oid
		AND pg_description1.objsubid = 0
		AND pg_description2.objoid = pg_class2.oid
		AND pg_description2.objsubid = 0
		AND system_check_comment(pg_description0.description, 'TYPE', 'domain')
		AND system_check_comment(pg_description0.description, 'STATUS', 'active');
