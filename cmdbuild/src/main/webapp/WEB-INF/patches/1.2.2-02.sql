-- Fix restrict trigger deletion, recreate all reference triggers

CREATE OR REPLACE VIEW system_attributecatalog AS 
 SELECT cmtable.classid, cmtable.classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, 
        CASE
            WHEN strpos(attribute_description.description, 'MODE: reserved'::text) > 0 THEN (-1)
            WHEN strpos(attribute_description.description, 'INDEX: '::text) > 0 THEN "substring"(attribute_description.description, 'INDEX: ([^|]*)'::text)::integer
            ELSE 0
        END AS attributeindex, (pg_attribute.attinhcount = 0) AS attributeislocal, pg_type.typname AS attributetype, 
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
        END AS attributescale, notnulljoin.oid IS NOT NULL OR pg_attribute.attnotnull AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, system_attribute_isunique(cmtable.classname::character varying, pg_attribute.attname::text::character varying) AS isunique, system_read_comment(attribute_description.description::character varying, 'LOOKUP'::character varying) AS attributelookup, system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying) AS attributereferencedomain, system_read_comment(attribute_description.description::character varying, 'REFERENCETYPE'::character varying) AS attributereferencetype, system_read_comment(attribute_description.description::character varying, 'REFERENCEDIRECT'::character varying) AS attributereferencedirect, 
        CASE
            WHEN system_domaincatalog.domaincardinality = '1:N'::text THEN system_domaincatalog.domainclass1
            ELSE system_domaincatalog.domainclass2
        END AS attributereference
   FROM pg_attribute
   JOIN (         SELECT system_classcatalog.classid, system_classcatalog.classname
                   FROM system_classcatalog
        UNION 
                 SELECT system_domaincatalog.domainid AS classid, system_domaincatalog.domainname AS classname
                   FROM system_domaincatalog) cmtable ON pg_attribute.attrelid = cmtable.classid
   LEFT JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
   LEFT JOIN pg_description attribute_description ON attribute_description.objoid = cmtable.classid AND attribute_description.objsubid = pg_attribute.attnum
   LEFT JOIN pg_attrdef pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
   LEFT JOIN system_domaincatalog ON system_read_comment(attribute_description.description::character varying, 'REFERENCEDOM'::character varying)::text = system_domaincatalog.domainname
   LEFT JOIN pg_constraint notnulljoin ON notnulljoin.conrelid = pg_attribute.attrelid AND notnulljoin.conname::text = lower(pg_attribute.attname::text || '_notnull'::text)
  WHERE pg_attribute.atttypid > 0::oid AND pg_attribute.attnum > 0 AND attribute_description.description IS NOT NULL;


CREATE OR REPLACE FUNCTION system_class_delete(
	ClassName character varying)
RETURNS boolean AS
$$
DECLARE
	AttributeReferenceDomain text;
	AttributeReferenceType text;
	AttributeReferenceTarget text;
BEGIN
	IF system_class_hasdomains(ClassName) THEN
		RAISE EXCEPTION 'Cannot delete class %: has domains', ClassName;
	ELSEIF system_class_haschild(ClassName) THEN
		RAISE EXCEPTION 'Cannot delete class %: has childs', ClassName;
	ELSEIF NOT system_class_isempty(ClassName) THEN
		RAISE EXCEPTION 'Cannot delete class %: contains data', ClassName;
	ELSE
		-- removes the extra triggers for reference subclasses (not needed IF there was just one!)
		FOR AttributeReferenceDomain, AttributeReferenceType, AttributeReferenceTarget IN
			EXECUTE 'SELECT attributereferencedomain, attributereferencetype, attributereference' || 
			' FROM system_attributecatalog WHERE classname='||quote_literal(ClassName)||' AND attributereference IS NOT NULL'
		LOOP
			PERFORM system_droptriggersrecursive(AttributeReferenceTarget,
				AttributeReferenceType||'_'||AttributeReferenceDomain||'_'||ClassName);
		END LOOP;
		IF system_class_hastrigger(ClassName) THEN
			PERFORM system_class_deletetriggers(ClassName);
		END IF;
		EXECUTE 'DROP TABLE '|| quote_ident(ClassName) ||' CASCADE';
	END IF;
	RETURN TRUE;
END;
$$
LANGUAGE 'plpgsql';


-- Drop all restrict and reference triggers and recreate them

CREATE OR REPLACE FUNCTION patch_recreate_triggers()
RETURNS VOID AS
$$
DECLARE
	TriggerName name;
	TriggerTableClass regclass;
	RefAttr system_attributecatalog%rowtype;
BEGIN
	FOR TriggerTableClass, TriggerName IN
		SELECT tgrelid::regclass, tgname FROM pg_trigger WHERE tgname LIKE 'restrict_%' OR tgname LIKE 'reference_%'
	LOOP
		EXECUTE 'DROP TRIGGER '||quote_ident(TriggerName)||' ON '||TriggerTableClass||' CASCADE';
	END LOOP;

	FOR RefAttr IN
		SELECT *
		FROM system_attributecatalog
		WHERE coalesce(attributereference, '') <> ''
	LOOP
		PERFORM system_reference_createpkandtriggers(RefAttr.attributereferencetype, RefAttr.classname::varchar,
			RefAttr.attributename::varchar, RefAttr.attributereference::varchar, RefAttr.attributereferencedomain::varchar);
	END LOOP;
END;
$$
LANGUAGE 'plpgsql';

SELECT patch_recreate_triggers();

DROP FUNCTION patch_recreate_triggers();
