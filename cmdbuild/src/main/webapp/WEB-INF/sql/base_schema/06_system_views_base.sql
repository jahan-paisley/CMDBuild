CREATE OR REPLACE FUNCTION _cm_legacy_read_comment(text, text) RETURNS varchar AS $$
	SELECT COALESCE(_cm_read_comment($1, $2), '');
$$ LANGUAGE SQL STABLE;


CREATE OR REPLACE VIEW system_classcatalog AS 
	SELECT pg_class.oid AS classid,
		CASE WHEN pg_namespace.nspname = 'public' THEN '' ELSE pg_namespace.nspname || '.' END || pg_class.relname AS classname, pg_description.description AS classcomment, pg_class.relkind = 'v'::"char" AS isview
	FROM pg_class
		JOIN pg_description
			ON pg_description.objoid = pg_class.oid
				AND pg_description.objsubid = 0
				AND _cm_is_any_class_comment(pg_description.description)
		JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
	WHERE pg_class.reltype > 0::oid;


CREATE OR REPLACE VIEW system_domaincatalog AS
 SELECT
   pg_class.oid AS domainid,
   substring(pg_class.relname from 5) AS domainname,
   substring(pg_description.description, 'CLASS1: ([^|]*)'::text) AS domainclass1,
   substring(pg_description.description, 'CLASS2: ([^|]*)'::text) AS domainclass2,
   substring(pg_description.description, 'CARDIN: ([^|]*)'::text) AS domaincardinality,
   pg_description.description AS domaincomment,
   (pg_class.relkind='v') AS isview
   FROM pg_class
   LEFT JOIN pg_description pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
  WHERE strpos(pg_description.description, 'TYPE: domain'::text) > 0;


CREATE OR REPLACE VIEW system_attributecatalog AS 
 SELECT cmtable.classid, cmtable.classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, 
        CASE
            WHEN strpos(attribute_description.description, 'MODE: reserved'::text) > 0 THEN (-1)
            WHEN strpos(attribute_description.description, 'INDEX: '::text) > 0 THEN "substring"(attribute_description.description, 'INDEX: ([^|]*)'::text)::integer
            ELSE 0
        END AS attributeindex, pg_attribute.attinhcount = 0 AS attributeislocal,
        CASE pg_type.typname
		WHEN 'geometry' THEN _cm_get_geometry_type(cmtable.classid, pg_attribute.attname)
		ELSE pg_type.typname
        END AS attributetype, 
        CASE
            WHEN pg_type.typname = 'varchar'::name THEN pg_attribute.atttypmod - 4
            ELSE NULL::integer
        END AS attributelength, 
        CASE
            WHEN pg_type.typname = 'numeric'::name THEN pg_attribute.atttypmod / 65536
            ELSE NULL::integer
        END AS attributeprecision, 
        CASE
            WHEN pg_type.typname = 'numeric'::name THEN pg_attribute.atttypmod - pg_attribute.atttypmod / 65536 * 65536 - 4
            ELSE NULL::integer
        END AS attributescale, notnulljoin.oid IS NOT NULL OR pg_attribute.attnotnull AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, _cm_attribute_is_unique(cmtable.classid, pg_attribute.attname::text) AS isunique, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'LOOKUP'::character varying::text) AS attributelookup, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCEDOM'::character varying::text) AS attributereferencedomain, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCETYPE'::character varying::text) AS attributereferencetype, _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCEDIRECT'::character varying::text) AS attributereferencedirect, 
        CASE
            WHEN system_domaincatalog.domaincardinality = '1:N'::text THEN system_domaincatalog.domainclass1
            ELSE system_domaincatalog.domainclass2
        END AS attributereference
   FROM pg_attribute
   JOIN ( SELECT system_classcatalog.classid, system_classcatalog.classname
           FROM system_classcatalog
UNION 
         SELECT system_domaincatalog.domainid AS classid, system_domaincatalog.domainname AS classname
           FROM system_domaincatalog) cmtable ON pg_attribute.attrelid = cmtable.classid
   LEFT JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
   LEFT JOIN pg_description attribute_description ON attribute_description.objoid = cmtable.classid AND attribute_description.objsubid = pg_attribute.attnum
   LEFT JOIN pg_attrdef pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
   LEFT JOIN system_domaincatalog ON _cm_legacy_read_comment(attribute_description.description::character varying::text, 'REFERENCEDOM'::character varying::text)::text = system_domaincatalog.domainname
   LEFT JOIN pg_constraint notnulljoin ON notnulljoin.conrelid = pg_attribute.attrelid AND notnulljoin.conname::text = _cm_notnull_constraint_name(pg_attribute.attname::text)
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;


CREATE OR REPLACE VIEW system_inheritcatalog AS
	SELECT inhparent AS parentid, inhrelid AS childid FROM pg_inherits
	UNION -- add views with cmdbuild comments
	SELECT '"Class"'::regclass::oid AS parentid, pg_class.oid AS childid
		FROM pg_class
		JOIN pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
		LEFT JOIN pg_inherits ON pg_inherits.inhrelid = pg_class.oid
	WHERE pg_class.relkind = 'v' AND strpos(pg_description.description, 'TYPE: class'::text) > 0;


CREATE OR REPLACE VIEW system_treecatalog AS 
	SELECT
		parent_class.classid AS parentid,
		parent_class.classname AS parent,
		parent_class.classcomment AS parentcomment,
		child_class.classid AS childid,
		child_class.classname AS child,
		child_class.classcomment AS childcomment
	FROM system_inheritcatalog
		JOIN system_classcatalog AS parent_class
			ON system_inheritcatalog.parentid = parent_class.classid
		JOIN system_classcatalog AS child_class
			ON system_inheritcatalog.childid = child_class.classid;


CREATE OR REPLACE VIEW system_relationlist AS 
	SELECT
		"Map"."Id" AS id,
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
		_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
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
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description)
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
		"Map"."Id" AS id,
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
		_cm_legacy_read_comment(pg_description0.description, 'DESCRINV')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description1.description, 'DESCR')::text AS classdescription,
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
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description)
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
		"Map"."Id" AS id,
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
		_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
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
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description)
UNION
	SELECT
		"Map"."Id" AS id,
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
		_cm_legacy_read_comment(pg_description0.description, 'DESCRINV')::text AS domaindescription,
		_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL')::text AS domainmasterdetail,
		_cm_legacy_read_comment(pg_description0.description, 'CARDIN')::text AS domaincardinality,
		_cm_legacy_read_comment(pg_description2.description, 'DESCR')::text AS classdescription,
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
		AND _cm_is_domain_comment(pg_description0.description)
		AND _cm_is_active_comment(pg_description0.description);
