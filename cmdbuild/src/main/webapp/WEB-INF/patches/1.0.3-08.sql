-- Bug trigger creation on subclasses

CREATE OR REPLACE FUNCTION system_class_recreateaftertriggersrecursive(ClassName character varying)
  RETURNS VOID AS
$BODY$
DECLARE
	childs refcursor;
	ChildName varchar;
BEGIN
	IF system_class_haschild(ClassName) THEN
		OPEN childs FOR EXECUTE 'SELECT child FROM system_treecatalog WHERE parent='''||ClassName||'''';
		LOOP
			FETCH childs INTO ChildName;
			EXIT WHEN NOT FOUND;
			PERFORM system_class_recreateaftertriggersrecursive(ChildName);
		END LOOP;
		CLOSE childs;
	END IF;
	PERFORM system_class_recreateaftertriggers(ClassName);
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_recreateaftertriggers(ClassName character varying)
  RETURNS VOID AS
$BODY$
BEGIN
	IF system_class_hastrigger(ClassName) THEN
		EXECUTE 'DROP FUNCTION after_archive_row_'||ClassName||'() CASCADE';
		PERFORM system_class_createaftertriggers(ClassName);
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_attribute_ismandatory(ClassName character varying, AttributeName character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
        is_mandatory boolean;
    BEGIN
        EXECUTE 'SELECT attributenotnull FROM system_attributecatalog '||
        	'WHERE classname='''||ClassName||''' AND attributename='''||AttributeName||'''' INTO is_mandatory;
        RETURN is_mandatory;
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
        EXECUTE 'ALTER TABLE "'||ClassName||'" ALTER COLUMN "'||AttributeName||'" SET NOT NULL';
    END IF;
    -- unique
    IF (AttributeUnique) THEN
        PERFORM system_attribute_makeunique(ClassName,AttributeName, true);
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
    is_unique boolean;
    is_mandatory boolean;
	is_local boolean;
BEGIN
    EXECUTE 'SELECT attributedefault, attributeislocal FROM system_attributecatalog '||
    	'WHERE classname='''||ClassName||''' AND attributename='''||AttributeNewName||''''
    	INTO a_default, is_local;

    IF (is_local) THEN
        EXECUTE 'ALTER TABLE "'|| ClassName ||'" ALTER COLUMN "'|| AttributeName ||'" TYPE '|| AttributeType;
        IF (AttributeName <> AttributeNewName) THEN 
            EXECUTE 'ALTER TABLE "'|| ClassName ||'" RENAME COLUMN "'|| AttributeName ||'" TO "'|| AttributeNewName ||'"';
        END IF;
    END IF;

	IF AttributeUnique <> system_attribute_isunique(ClassName,AttributeNewName) THEN
		PERFORM system_attribute_makeunique(ClassName,AttributeNewName,AttributeUnique);
	END IF;

	IF AttributeMandatory <> system_attribute_ismandatory(ClassName,AttributeNewName) THEN
		IF AttributeMandatory THEN
	        EXECUTE 'ALTER TABLE "'||ClassName||'" ALTER COLUMN "'||AttributeName||'" SET NOT NULL';
	    ELSE
	    	EXECUTE 'ALTER TABLE "'||ClassName||'" ALTER COLUMN "'||AttributeName||'" DROP NOT NULL';
	    END IF;
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


CREATE OR REPLACE FUNCTION system_attribute_delete_recursion(ClassName character varying,
	AttributeName character varying, IsBase boolean)
  RETURNS VOID AS
$BODY$
    DECLARE
	is_superclass bool;
	attrlocal bool;
	subclasses refcursor;
	classcomment character varying;
	Subclass character varying;
    BEGIN
	-- stop if the attribute is not empty (even in the history)
    IF NOT system_attribute_isempty(ClassName, AttributeName) THEN
		RAISE EXCEPTION 'Cannot delete %.% , contains data', ClassName, AttributeName;
	END IF;

	-- store if the attribute is local for later use
	-- (once the comment is removed, we can't query the attribute catalog)
	EXECUTE 'SELECT attributeislocal FROM system_attributecatalog '||
		'WHERE classname='''|| ClassName ||''' AND attributename='''|| AttributeName ||'''' INTO attrlocal;
	IF (IsBase AND NOT attrlocal) THEN
		RAISE EXCEPTION 'Attribute %.% not local', ClassName, AttributeName;
	END IF;

	-- remove the reference triggers if they exist
	EXECUTE 'SELECT system_deletereference('''|| ClassName ||''', '''|| AttributeName ||''')';
	-- remove the attribute comment
	EXECUTE 'COMMENT ON COLUMN "'|| ClassName ||'"."'||AttributeName||'" IS NULL';

	-- extract class comment
	EXECUTE 'SELECT classcomment FROM system_classcatalog WHERE classname = '''|| ClassName ||'''' INTO classcomment;
	IF system_class_issuperclass(classcomment) THEN
		-- visit childs
		OPEN subclasses FOR EXECUTE 'SELECT child FROM system_treecatalog WHERE parent=''' || ClassName || '''';
		LOOP
			FETCH subclasses INTO Subclass;
			EXIT WHEN NOT FOUND;
			PERFORM system_attribute_delete_recursion(Subclass, AttributeName, FALSE);
		END LOOP;
	ELSE
		PERFORM system_class_recreateaftertriggers(ClassName);
	END IF;

	-- remove the table column if local
	IF attrlocal THEN
		EXECUTE 'ALTER TABLE "'|| ClassName ||'" DROP COLUMN "'|| AttributeName ||'" CASCADE';
	END IF;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
