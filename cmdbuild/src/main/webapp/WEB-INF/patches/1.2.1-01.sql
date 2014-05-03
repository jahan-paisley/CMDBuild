-- Bugfix not null attribute creation

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
	-- set the comment recursively (needs to be performed before unique and mandatory, because they depend on the comment)
    PERFORM system_attribute_comment(ClassName, AttributeName, AttributeComment);
    -- not null
    IF (AttributeMandatory) THEN
    	PERFORM system_attribute_setmandatory(ClassName, AttributeName, true);
    END IF;
    -- unique
    IF (AttributeUnique) THEN
        PERFORM system_attribute_makeunique(ClassName, AttributeName, true);
    END IF;
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
