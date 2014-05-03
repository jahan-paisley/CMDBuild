-- Fix not null constraint on reference attributes

-- Fix mode for system classes

COMMENT ON COLUMN "Email"."Activity" IS 'MODE: read|FIELDMODE: write|DESCR: Activity|INDEX: 4|REFERENCEDOM: ActivityEmail|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active';
COMMENT ON COLUMN "Email"."EmailStatus" IS 'MODE: read|FIELDMODE: write|DESCR: EmailStatus|INDEX: 5|BASEDSP: true|LOOKUP: EmailStatus|STATUS: active';
COMMENT ON COLUMN "Email"."FromAddress" IS 'MODE: read|FIELDMODE: write|DESCR: From|INDEX: 6|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Email"."ToAddresses" IS 'MODE: read|FIELDMODE: write|DESCR: TO|INDEX: 7|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Email"."CcAddresses" IS 'MODE: read|FIELDMODE: write|DESCR: CC|INDEX: 8|CLASSORDER: 0|BASEDSP: false|STATUS: active';
COMMENT ON COLUMN "Email"."Subject" IS 'MODE: read|FIELDMODE: write|DESCR: Subject|INDEX: 9|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Email"."Content" IS 'MODE: read|FIELDMODE: write|DESCR: Body|INDEX: 10|BASEDSP: false|STATUS: active';

COMMENT ON COLUMN "User"."Username" IS 'MODE: read|DESCR: Username|INDEX: 1|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "User"."Password" IS 'MODE: read|DESCR: Password|INDEX: 2|BASEDSP: false|STATUS: active';
COMMENT ON COLUMN "User"."Email" IS 'MODE: read|DESCR: Email|INDEX: 5';

COMMENT ON COLUMN "Role"."Administrator" IS 'MODE: read|DESCR: Administrator|INDEX: 1|STATUS: active';
COMMENT ON COLUMN "Role"."startingClass" IS 'MODE: read|DESCR: Administrator|INDEX: 2|STATUS: active';
COMMENT ON COLUMN "Role"."Email" IS 'MODE: read|DESCR: Email|INDEX: 5';

-- Change attribute create and modify functions

CREATE OR REPLACE FUNCTION system_attribute_setmandatory(ClassName character varying, AttributeName character varying,
	IsMandatory boolean)
  RETURNS VOID AS
$BODY$
DECLARE
    is_check boolean;
    is_local boolean;
BEGIN
	EXECUTE 'SELECT attributeislocal, ((TRIM(attributereference)<>'''' OR' ||
                ' LOWER(system_read_comment(attributecomment::character varying, ''MODE''::character varying)) = ''write''))' ||
                ' FROM system_attributecatalog WHERE classname = ''' || ClassName || ''' AND attributename = ''' || AttributeName || ''''
                INTO is_local, is_check;
	IF (NOT is_local) THEN
		RAISE EXCEPTION 'Can''t modify the mandatory property of the inherited attribute %', AttributeName;
	END IF;
	IF (is_check) THEN
		IF (IsMandatory) THEN
			EXECUTE 'ALTER TABLE "' || ClassName || '" ADD CONSTRAINT ' || AttributeName || '_notnull CHECK ' ||
		        '("Status"<>''A'' OR "' || AttributeName || '" IS NOT NULL)';
		ELSE
			EXECUTE 'ALTER TABLE "' || ClassName || '" DROP CONSTRAINT ' || AttributeName || '_notnull';
		END IF;
	ELSE
		IF (IsMandatory) THEN
			EXECUTE 'ALTER TABLE "' || ClassName || '" ALTER COLUMN "' || AttributeName || '" SET NOT NULL';
		ELSE
			EXECUTE 'ALTER TABLE "' || ClassName || '" ALTER COLUMN "' || AttributeName || '" DROP NOT NULL';
		END IF;
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION system_attribute_create(ClassName character varying, AttributeName character varying,
	AttributeType character varying, AttributeDefault character varying, AttributeMandatory boolean, AttributeUnique boolean,
	AttributeComment character varying, AttributeReference character varying, AttributeReferenceDomain character varying,
	AttributeReferenceType character varying, AttributeReferenceIsDirect boolean)
-- AttributeType contains also length, precision or scale (it's the SQL type)
-- NdPaolo: Why can't we just generate the comment from the parameters OR extract the parameters from the comment?!
  RETURNS integer AS
$BODY$
DECLARE
    has_trigger boolean;
    dbindex integer;
BEGIN
	-- create attribute
    EXECUTE 'ALTER TABLE "'|| ClassName ||'" ADD COLUMN "'|| AttributeName ||'" '|| AttributeType;
    -- default value
    IF (AttributeDefault <> '') THEN
        IF (strpos(AttributeType,'varchar') > 0 OR strpos(AttributeType,'text') > 0 OR
				strpos(AttributeType,'date') > 0 OR strpos(AttributeType,'timestamp') > 0) THEN
			-- default needs to be escaped
            EXECUTE 'ALTER TABLE "'||ClassName||'" ALTER COLUMN "'||AttributeName||'" SET DEFAULT '''||AttributeDefault||''';';
            EXECUTE 'UPDATE "' || ClassName || '" SET "' || AttributeName || '"='''|| AttributeDefault ||'''' ;
        ELSE
            EXECUTE 'ALTER TABLE "'||ClassName||'" ALTER COLUMN "'||AttributeName||'" SET DEFAULT '||AttributeDefault||';';
            EXECUTE 'UPDATE "' || ClassName || '" SET "' || AttributeName || '"='|| AttributeDefault;
        END IF;
    END IF;
    -- not null
    IF (AttributeMandatory) THEN
    	PERFORM system_attribute_setmandatory(ClassName, AttributeName, true);
    END IF;
    -- unique
    IF (AttributeUnique) THEN
        PERFORM system_attribute_makeunique(ClassName, AttributeName, true);
    END IF;
	-- set the comment recursively
    PERFORM system_attribute_comment(ClassName, AttributeName, AttributeComment);
    -- create reference triggers
    IF (AttributeReference <> '' AND AttributeReferenceDomain <> '') THEN
        PERFORM system_reference_createrecursive(ClassName,AttributeName,AttributeReference,AttributeReferenceType,AttributeReferenceDomain);
    END IF;
	-- after triggers
    PERFORM system_class_recreateaftertriggersrecursive(ClassName);

    EXECUTE 'SELECT attributeindex FROM system_attributecatalog '||
    	'WHERE classname='''|| ClassName ||''' AND attributename='''|| AttributeName ||''''
    	INTO dbindex;
    RETURN dbindex;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_attribute_modify(ClassName character varying, AttributeName character varying,
	AttributeNewName character varying, AttributeType character varying, AttributeDefault character varying,
	AttributeMandatory boolean, AttributeUnique boolean, AttributeComment character varying)
-- AttributeType contains also length, precision or scale (it's the SQL type)
  RETURNS boolean AS
$BODY$
DECLARE
	has_trigger boolean;
	is_empty boolean;
	a_default varchar(50);
    is_mandatory boolean;
	is_local boolean;
	old_type varchar;
BEGIN
    EXECUTE 'SELECT attributedefault, attributeislocal, attributetype::varchar FROM system_attributecatalog '||
    	'WHERE classname='''||ClassName||''' AND attributename='''||AttributeNewName||''''
    	INTO a_default, is_local, old_type;

	SELECT INTO is_mandatory system_attribute_ismandatory(ClassName,AttributeNewName);

    IF (is_local) THEN
    	IF (old_type <> AttributeType) THEN
    		IF (is_mandatory) THEN -- disable the mandatory constraint
    			PERFORM system_attribute_setmandatory(ClassName, AttributeName, false);
    		END IF;
    		EXECUTE 'ALTER TABLE "'|| ClassName ||'" ALTER COLUMN "'|| AttributeName ||'" TYPE '|| AttributeType;
    		IF (is_mandatory) THEN -- reenable the mandatory constraint
    			PERFORM system_attribute_setmandatory(ClassName, AttributeName, true);
    		END IF;
    	END IF;
        IF (AttributeName <> AttributeNewName) THEN 
            EXECUTE 'ALTER TABLE "'|| ClassName ||'" RENAME COLUMN "'|| AttributeName ||'" TO "'|| AttributeNewName ||'"';
        END IF;
    END IF;

	IF AttributeUnique <> system_attribute_isunique(ClassName,AttributeNewName) THEN
		PERFORM system_attribute_makeunique(ClassName,AttributeNewName,AttributeUnique);
	END IF;

	IF AttributeMandatory <> is_mandatory THEN
		PERFORM system_attribute_setmandatory(ClassName, AttributeName, AttributeMandatory);
	END IF;

	PERFORM system_class_recreateaftertriggers(ClassName);

	IF (AttributeDefault <> '') THEN
		IF ( strpos(AttributeType,'varchar') > 0 or strpos(AttributeType,'text') > 0 or strpos(AttributeType,'date') > 0 or strpos(AttributeType,'timestamp') > 0  ) and AttributeDefault <> '' THEN
			EXECUTE 'ALTER TABLE "'|| ClassName ||'" ALTER "'|| AttributeNewName ||'" SET DEFAULT '''|| AttributeDefault|| '''';	 			  
		ELSE
			EXECUTE 'ALTER TABLE "'|| ClassName ||'" ALTER "'|| AttributeNewName ||'" SET DEFAULT '|| AttributeDefault;
		END IF;
	ELSE
		IF(a_default<>'') THEN
			EXECUTE 'ALTER TABLE "'|| ClassName ||'" ALTER COLUMN	"'|| AttributeNewName ||'" DROP DEFAULT';
		END IF;
	END IF;

	PERFORM system_attribute_comment(ClassName, AttributeNewName, AttributeComment);

	RETURN true;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


-- Change not null constraint to check

CREATE OR REPLACE FUNCTION system_patch_recreatenotnullconstraints()
  RETURNS VOID AS
$BODY$
DECLARE
tablename varchar(100);
loopvar record;
BEGIN
  FOR loopvar IN SELECT classname, attributename FROM system_attributecatalog
		WHERE attributeislocal AND attributenotnull AND (TRIM(attributereference)<>'' OR
			LOWER(system_read_comment(attributecomment::character varying, 'MODE'::character varying)) = 'write')
  LOOP
  	EXECUTE 'ALTER TABLE "' || loopvar.classname || '" ALTER COLUMN "' || loopvar.attributename || '" DROP NOT NULL';
	EXECUTE 'ALTER TABLE "' || loopvar.classname || '" ADD CONSTRAINT ' || loopvar.attributename || '_notnull CHECK ' ||
	        '("Status"<>''A'' OR "' || loopvar.attributename || '" IS NOT NULL)';
  END LOOP;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

SELECT system_patch_recreatenotnullconstraints();

DROP FUNCTION system_patch_recreatenotnullconstraints();

-- Recreate system_attributecatalog

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
        END AS attributescale,
        (notnulljoin.oid IS NOT NULL OR pg_attribute.attnotnull) AS attributenotnull, -- 'Id' is a real not null column since it is part of the key
        pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, system_attribute_isunique(pg_class.relname::text::character varying, pg_attribute.attname::text::character varying) AS isunique, system_read_comment(attribute_description.description::character varying, 'LOOKUP'::character varying) AS attributelookup, system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying) AS attributereferencedomain, system_read_comment(attribute_description.description::character varying, 'REFERENCETYPE'::character varying) AS attributereferencetype, system_read_comment(attribute_description.description::character varying, 'REFERENCEDIRECT'::character varying) AS attributereferencedirect, 
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
   LEFT JOIN pg_constraint notnulljoin ON notnulljoin.conrelid = pg_attribute.attrelid AND notnulljoin.conname = LOWER(pg_attribute.attname || '_notnull')
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;
