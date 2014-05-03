-- Choose schema for classes

DROP VIEW system_availablemenuitems;
DROP VIEW system_privilegescatalog;
DROP VIEW system_attributecatalog;
DROP VIEW system_classcatalog;

CREATE OR REPLACE VIEW system_classcatalog AS 
 SELECT pg_class.oid AS classid,
 CASE WHEN pg_namespace.nspname = 'public' THEN '' ELSE pg_namespace.nspname || '.' END || pg_class.relname AS classname, pg_description.description AS classcomment, pg_class.relkind = 'v'::"char" AS isview
   FROM pg_class
   JOIN pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0 AND strpos(pg_description.description, 'TYPE: class'::text) > 0 AND strpos(pg_description.description, 'MODE: '::text) > 0
   JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
  WHERE pg_class.reltype > 0::oid;


CREATE VIEW system_attributecatalog AS 
 SELECT cmtable.classid AS classid, cmtable.classname AS classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, 
        CASE
            WHEN strpos(attribute_description.description, 'MODE: reserved'::text) > 0 THEN (-1)
            WHEN strpos(attribute_description.description, 'INDEX: '::text) > 0 THEN "substring"(attribute_description.description, 'INDEX: ([^|]*)'::text)::integer
            ELSE 0
        END AS attributeindex, pg_attribute.attislocal AS attributeislocal, pg_type.typname AS attributetype, 
        CASE
            WHEN pg_type.typname::text ILIKE '%char' THEN pg_attribute.atttypmod - 4
            ELSE NULL::integer
        END AS attributelength, 
        CASE
            WHEN pg_type.typname = 'numeric'::name THEN pg_attribute.atttypmod / 65536
            ELSE NULL::integer
        END AS attributeprecision, 
        CASE
            WHEN pg_type.typname = 'numeric'::name THEN pg_attribute.atttypmod - pg_attribute.atttypmod / 65536 * 65536 - 4
            ELSE NULL::integer
        END AS attributescale, notnulljoin.oid IS NOT NULL OR pg_attribute.attnotnull AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, system_attribute_isunique(cmtable.classname::text, pg_attribute.attname::text::character varying) AS isunique, system_read_comment(attribute_description.description::character varying, 'LOOKUP'::character varying) AS attributelookup, system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying) AS attributereferencedomain, system_read_comment(attribute_description.description::character varying, 'REFERENCETYPE'::character varying) AS attributereferencetype, system_read_comment(attribute_description.description::character varying, 'REFERENCEDIRECT'::character varying) AS attributereferencedirect, 
        CASE
            WHEN system_domaincatalog.domaincardinality = '1:N'::text THEN system_domaincatalog.domainclass1
            ELSE system_domaincatalog.domainclass2
        END AS attributereference
   FROM pg_attribute
   JOIN (SELECT classid, classname FROM system_classcatalog UNION SELECT domainid AS classid, domainname AS classname FROM system_domaincatalog) AS cmtable ON pg_attribute.attrelid = cmtable.classid
   LEFT JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
   LEFT JOIN pg_description attribute_description ON attribute_description.objoid = cmtable.classid AND attribute_description.objsubid = pg_attribute.attnum
   LEFT JOIN pg_attrdef pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
   LEFT JOIN system_domaincatalog ON system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying)::text = system_domaincatalog.domainname
   LEFT JOIN pg_constraint notnulljoin ON notnulljoin.conrelid = pg_attribute.attrelid AND notnulljoin.conname::text = lower(pg_attribute.attname::text || '_notnull'::text)
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;

-- Recreate dropped dependencies

CREATE OR REPLACE VIEW system_availablemenuitems AS 
        (        (        ( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Code", system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying) AS "Description", 
                                CASE
                                    WHEN (system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                                       FROM "Menu" "Menu1"
                                      WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying::text, 'report'::character varying::text, 'view'::character varying::text, 'Folder'::character varying::text, 'Report'::character varying::text, 'View'::character varying::text])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                                    ELSE NULL::regclass
                                END AS "IdElementClass", 0 AS "IdElementObj", "Role"."Id" AS "IdGroup", system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Type"
                           FROM system_classcatalog
                      JOIN "Role" ON "Role"."Status" = 'A'::bpchar
                 LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
                WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
                         FROM "Menu"
                        WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu"."IdGroup")) AND (system_read_comment(system_classcatalog.classcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text])) AND system_read_comment(system_classcatalog.classcomment::character varying, 'STATUS'::character varying)::text = 'active'::text
                ORDER BY system_classcatalog.classid::regclass, system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false), system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying), 
                      CASE
                          WHEN (system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                             FROM "Menu" "Menu1"
                            WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying::text, 'report'::character varying::text, 'view'::character varying::text, 'Folder'::character varying::text, 'Report'::character varying::text, 'View'::character varying::text])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                          ELSE NULL::regclass
                      END, 0::integer, "Role"."Id", system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false))
                UNION 
                         SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", "AllReport"."RoleId" AS "IdGroup", "AllReport"."Type"
                           FROM ( SELECT system_getmenutype(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type", "Role"."Id" AS "RoleId"
                                   FROM generate_series(1, 6) i(i), "Report"
                              JOIN "Role" ON "Role"."Status" = 'A'::bpchar
                             WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
                                   CASE
                                       WHEN "Report"."Type"::text = 'normal'::text THEN 1
                                       WHEN "Report"."Type"::text = 'custom'::text THEN 2
                                       WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
                                       ELSE 0
                                   END) "AllReport"
                      LEFT JOIN "Menu" ON "AllReport"."IdElementObj" = "Menu"."IdElementObj" AND "Menu"."Status" = 'A'::bpchar AND "AllReport"."RoleId" = "Menu"."IdGroup" AND "AllReport"."Code" = "Menu"."Code"::text
                     WHERE "Menu"."Code" IS NULL)
        UNION 
                ( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Code", system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying) AS "Description", 
                        CASE
                            WHEN (system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                               FROM "Menu" "Menu1"
                              WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying::text, 'report'::character varying::text, 'view'::character varying::text, 'Folder'::character varying::text, 'Report'::character varying::text, 'View'::character varying::text])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                            ELSE NULL::regclass
                        END AS "IdElementClass", 0 AS "IdElementObj", 0 AS "IdGroup", system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Type"
                   FROM system_classcatalog
              LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
             WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
                      FROM "Menu"
                     WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup")) AND (system_read_comment(system_classcatalog.classcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text])) AND system_read_comment(system_classcatalog.classcomment::character varying, 'STATUS'::character varying)::text = 'active'::text
             ORDER BY system_classcatalog.classid::regclass, system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false), system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying), 
                   CASE
                       WHEN (system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                          FROM "Menu" "Menu1"
                         WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying::text, 'report'::character varying::text, 'view'::character varying::text, 'Folder'::character varying::text, 'Report'::character varying::text, 'View'::character varying::text])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                       ELSE NULL::regclass
                   END, 0::integer, system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false)))
UNION 
         SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", 0 AS "IdGroup", "AllReport"."Type"
           FROM ( SELECT system_getmenutype(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type"
                   FROM generate_series(1, 6) i(i), "Report"
                  WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
                        CASE
                            WHEN "Report"."Type"::text = 'normal'::text THEN 1
                            WHEN "Report"."Type"::text = 'custom'::text THEN 2
                            WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
                            ELSE 0
                        END) "AllReport"
      LEFT JOIN "Menu" ON "AllReport"."IdElementObj" = "Menu"."IdElementObj" AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup" AND "AllReport"."Code" = "Menu"."Code"::text
     WHERE "Menu"."Code" IS NULL;


CREATE OR REPLACE VIEW system_privilegescatalog AS 
 SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode"
   FROM (         SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode"
                   FROM "Grant"
        UNION 
                 SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", system_classcatalog.classid::regclass AS classid, '-'
                   FROM system_classcatalog, "Role"
                  WHERE system_classcatalog.classid::regclass::oid <> '"Class"'::regclass::oid AND NOT ("Role"."Id"::text || system_classcatalog.classid::integer::text IN ( SELECT "Grant"."IdRole"::text || "Grant"."IdGrantedClass"::oid::integer::text
                           FROM "Grant"))) permission
   JOIN system_classcatalog ON permission."IdGrantedClass"::oid = system_classcatalog.classid AND (system_read_comment(system_classcatalog.classcomment::character varying, 'MODE'::character varying)::text = ANY (ARRAY['write'::character varying::text, 'read'::character varying::text]))
  ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";
