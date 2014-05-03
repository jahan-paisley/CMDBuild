-- Default group for user and group descriptions, next executor for multiple groups

CREATE OR REPLACE VIEW system_attributecatalog AS 
  SELECT pg_class.oid AS classid, pg_class.relname AS classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, 
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
        END AS attributescale, pg_attribute.attnotnull AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, system_attribute_isunique(pg_class.relname::text::character varying, pg_attribute.attname::text::character varying) AS isunique, system_read_comment(attribute_description.description::character varying, 'LOOKUP'::character varying) AS attributelookup, system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying) AS attributereferencedomain, system_read_comment(attribute_description.description::character varying, 'REFERENCETYPE'::character varying) AS attributereferencetype, system_read_comment(attribute_description.description::character varying, 'REFERENCEDIRECT'::character varying) AS attributereferencedirect, 
        CASE
            WHEN system_domaincatalog.domaincardinality = '1:N'::text THEN system_domaincatalog.domainclass1
            ELSE system_domaincatalog.domainclass2
        END AS attributereference
   FROM pg_attribute
   JOIN pg_class ON pg_attribute.attrelid = pg_class.oid
   LEFT JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
   LEFT JOIN pg_description attribute_description ON attribute_description.objoid = pg_class.oid AND attribute_description.objsubid = pg_attribute.attnum
   LEFT JOIN pg_attrdef pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
   LEFT JOIN system_domaincatalog ON system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying)::text = system_domaincatalog.domainname
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;

SELECT system_attribute_create('Map_UserRole', 'DefaultGroup', 'boolean', '', false, false,
	'MODE: read|FIELDMODE: write|DESCR: Default Group|INDEX: 1|BASEDSP: true|STATUS: active',
	'', '', '', null);

UPDATE "Role" SET "Code" = "Description";
ALTER TABLE "Role" ALTER COLUMN "Code" SET NOT NULL;


CREATE OR REPLACE FUNCTION system_isexecutor(oid, integer, character varying)
  RETURNS boolean AS
$BODY$
    declare
        tableoid alias for $1;
        id alias for $2;
        executor alias for $3;
        ctrl boolean;
        classname varchar;
    begin
    EXECUTE 'SELECT classname::varchar FROM system_classcatalog WHERE classid='||tableoid INTO classname;
    EXECUTE 'SELECT (COUNT(*) != 0)
             FROM (SELECT "NextExecutor" FROM "'||classname||'_history" WHERE "CurrentId" = '||id||'
                   UNION SELECT "NextExecutor" FROM "'||classname||'" WHERE "Id" = '||id||') AS q
	     WHERE q."NextExecutor" = ANY (string_to_array('''||executor||''','','')) LIMIT 1' INTO ctrl;
	RETURN ctrl;
    end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
