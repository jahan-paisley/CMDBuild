-- Activity grid speed up

-- drop needed by postgres 8.3
DROP VIEW system_availablemenuitems;
DROP VIEW system_privilegescatalog;
DROP VIEW system_attributecatalog;

DROP VIEW system_classcatalog;
CREATE VIEW system_classcatalog AS
  SELECT
    pg_class.oid AS classid,
    pg_class.relname AS classname,
    pg_description.description AS classcomment,
    (pg_class.relkind='v') AS isview
    FROM pg_class
    INNER JOIN pg_description
      ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
      AND strpos(pg_description.description, 'TYPE: class'::text) > 0
      AND strpos(pg_description.description, 'MODE: '::text) > 0
    INNER JOIN pg_namespace
      ON pg_namespace.oid = pg_class.relnamespace AND pg_namespace.nspname='public'
    WHERE pg_class.reltype > 0::oid;


DROP VIEW system_domaincatalog;
CREATE VIEW system_domaincatalog AS
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


-- recreate deleted views
CREATE VIEW system_attributecatalog AS 
 SELECT system_classcatalog.classid, system_classcatalog.classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, 
        CASE
            WHEN strpos(attribute_description.description, 'MODE: reserved'::text) > 0 THEN (-1)
            WHEN strpos(attribute_description.description, 'INDEX: '::text) > 0 THEN "substring"(attribute_description.description, 'INDEX: ([^|]*)'::text)::integer
            ELSE 0
        END AS attributeindex, pg_attribute.attislocal AS attributeislocal, pg_type.typname AS attributetype, 
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
        END AS attributescale, pg_attribute.attnotnull AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, system_attribute_isunique(system_classcatalog.classname::text::character varying, pg_attribute.attname::text::character varying) AS isunique, system_read_comment(attribute_description.description::character varying, 'LOOKUP'::character varying) AS attributelookup, system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying) AS attributereferencedomain, system_read_comment(attribute_description.description::character varying, 'REFERENCETYPE'::character varying) AS attributereferencetype, system_read_comment(attribute_description.description::character varying, 'REFERENCEDIRECT'::character varying) AS attributereferencedirect, 
        CASE
            WHEN system_domaincatalog.domaincardinality = '1:N'::text THEN system_domaincatalog.domainclass1
            ELSE system_domaincatalog.domainclass2
        END AS attributereference
   FROM pg_attribute
   JOIN system_classcatalog ON pg_attribute.attrelid = system_classcatalog.classid AND strpos(system_classcatalog.classname::text, '_history'::text) = 0 AND strpos(system_classcatalog.classname::text, 'system_'::text) = 0
   LEFT JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
   LEFT JOIN pg_description attribute_description ON attribute_description.objoid = system_classcatalog.classid AND attribute_description.objsubid = pg_attribute.attnum
   LEFT JOIN pg_attrdef pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
   LEFT JOIN system_domaincatalog ON system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying)::text = system_domaincatalog.domainname
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;

  CREATE OR REPLACE VIEW system_availablemenuitems AS 
((( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Code", system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying) AS "Description", 
        CASE
            WHEN system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read') AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
               FROM "Menu" "Menu1"
              WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying, 'report'::character varying, 'view'::character varying, 'Folder'::character varying, 'Report'::character varying, 'View'::character varying]::text[])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
            ELSE NULL::regclass
        END AS "IdElementClass", 0 AS "IdElementObj", "Role"."Id" AS "IdGroup", system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Type"
   FROM system_classcatalog
   JOIN "Role" ON "Role"."Status" = 'A'::bpchar
   LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
  WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
   FROM "Menu"
  WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu"."IdGroup")) AND system_read_comment(system_classcatalog.classcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read') AND system_read_comment(system_classcatalog.classcomment::character varying, 'STATUS'::character varying)::text = 'active'::text
  ORDER BY system_classcatalog.classid::regclass, system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false), system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying), 
CASE
    WHEN system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read') AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
       FROM "Menu" "Menu1"
      WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying, 'report'::character varying, 'view'::character varying, 'Folder'::character varying, 'Report'::character varying, 'View'::character varying]::text[])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
    ELSE NULL::regclass
END, 0::integer, "Role"."Id", system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false))
UNION 
 SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", "AllReport"."RoleId" AS "IdGroup", "AllReport"."Type"
   FROM
	(SELECT system_getmenutype(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type", "Role"."Id" AS "RoleId" FROM generate_series(1, 6) i(i), "Report"
	JOIN "Role" ON "Role"."Status" = 'A'::bpchar
	 WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
        CASE
            WHEN "Report"."Type"::text = 'normal'::text THEN 1
            WHEN "Report"."Type"::text = 'custom'::text THEN 2
            WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
            ELSE 0
        END) AS "AllReport"
   LEFT JOIN "Menu"
  ON "AllReport"."IdElementObj" = "Menu"."IdElementObj"
  AND "Menu"."Status" = 'A'::bpchar AND "AllReport"."RoleId" = "Menu"."IdGroup"
  AND "AllReport"."Code" = "Menu"."Code"
WHERE "Menu"."Code" IS NULL
)
UNION 
( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Code", system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying) AS "Description", 
        CASE
            WHEN system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read') AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
               FROM "Menu" "Menu1"
              WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying, 'report'::character varying, 'view'::character varying, 'Folder'::character varying, 'Report'::character varying, 'View'::character varying]::text[])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
            ELSE NULL::regclass
        END AS "IdElementClass", 0 AS "IdElementObj", 0 AS "IdGroup", system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false) AS "Type"
   FROM system_classcatalog
   LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
  WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
      FROM "Menu"
     WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup")) AND system_read_comment(system_classcatalog.classcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read') AND system_read_comment(system_classcatalog.classcomment::character varying, 'STATUS'::character varying)::text = 'active'::text
  ORDER BY system_classcatalog.classid::regclass, system_getmenutype(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false), system_read_comment(system_classcatalog.classcomment::character varying, 'DESCR'::character varying), 
   CASE
       WHEN system_read_comment(system_treecatalog.childcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read') AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
          FROM "Menu" "Menu1"
         WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::character varying, 'report'::character varying, 'view'::character varying, 'Folder'::character varying, 'Report'::character varying, 'View'::character varying]::text[])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
       ELSE NULL::regclass
   END, 0::integer, system_getmenucode(system_class_issuperclass(system_treecatalog.childcomment::character varying), system_class_isprocess(system_classcatalog.classcomment::character varying), false, false), 0::integer))
UNION 
 SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", 0 AS "IdGroup", "AllReport"."Type"
   FROM
	(SELECT system_getmenutype(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type" FROM generate_series(1, 6) i(i), "Report" WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
        CASE
            WHEN "Report"."Type"::text = 'normal'::text THEN 1
            WHEN "Report"."Type"::text = 'custom'::text THEN 2
            WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
            ELSE 0
        END) AS "AllReport"
   LEFT JOIN "Menu"
  ON "AllReport"."IdElementObj" = "Menu"."IdElementObj"
  AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup"
  AND "AllReport"."Code" = "Menu"."Code"
WHERE "Menu"."Code" IS NULL;

CREATE OR REPLACE VIEW system_privilegescatalog AS 
 SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode"
   FROM ( SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode"
           FROM "Grant"
UNION 
         SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", system_classcatalog.classid::regclass AS classid, '-'
           FROM system_classcatalog, "Role"
          WHERE system_classcatalog.classid::regclass::oid <> '"Class"'::regclass::oid AND NOT ("Role"."Id"::text || system_classcatalog.classid::integer::text IN ( SELECT "Grant"."IdRole"::text || "Grant"."IdGrantedClass"::oid::integer::text
                   FROM "Grant"))) permission
   JOIN system_classcatalog ON permission."IdGrantedClass"::oid = system_classcatalog.classid AND system_read_comment(system_classcatalog.classcomment::character varying, 'MODE'::character varying)::varchar IN ('write','read')
  ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";


CREATE OR REPLACE FUNCTION system_isexecutor(oid, integer, character varying)
  RETURNS boolean AS
$BODY$
    declare
        tableoid alias for $1;
        id alias for $2;
        executor alias for $3;
        field_list refcursor;
        ctrl boolean;
        classname varchar;
    begin
    EXECUTE 'select classname::varchar from system_classcatalog where classid='||tableoid into classname;
    EXECUTE 'select ((select count(*) from "'||classname||'_history" as ch where ch."CurrentId" = '||id||' and ch."NextExecutor" = '''||executor||''') != 0) or ('''||executor||''' = (select "NextExecutor" from "'||classname||'" where "Id"='||id||'))' into ctrl;
        return ctrl;
    end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_createhistory(character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
        tableName ALIAS FOR $1;
    BEGIN
	EXECUTE '
		CREATE TABLE "'||tableName||'_history"
		(
			"CurrentId" int4 NOT NULL,
			"EndDate" timestamp NOT NULL DEFAULT now(),
			CONSTRAINT "'||tableName||'_history_pkey" PRIMARY KEY ("Id"),
			CONSTRAINT "'||tableName||'_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "'||tableName||'" ("Id") ON UPDATE RESTRICT ON DELETE SET NULL
		) INHERITS ("'||tableName||'") 
		WITH OIDS;
	';
	EXECUTE 'CREATE INDEX idx_' || tablename || 'history_currentid  ON "' || tablename || '_history"  USING btree  ("CurrentId")';
    RETURN TRUE;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_patch_createindexhistorycurrentid()
  RETURNS VOID AS
$BODY$
DECLARE
tablename varchar(100);

BEGIN
  FOR tablename IN SELECT classname FROM system_classcatalog where classname not in ('Role', 'Grant', 'LookUp', 'Menu', 'Report', 'User') and strpos(classcomment, 'SUPERCLASS: false') > 0 and not isview
  LOOP
	EXECUTE 'DROP INDEX IF EXISTS idx_' || tablename || 'history_currentid';
	EXECUTE 'CREATE INDEX idx_' || tablename || 'history_currentid  ON "' || tablename || '_history"  USING btree  ("CurrentId")';
  END LOOP;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

SELECT system_patch_createindexhistorycurrentid();
DROP FUNCTION system_patch_createindexhistorycurrentid();
