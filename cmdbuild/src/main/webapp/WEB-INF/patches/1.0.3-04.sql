-- Allow empty attributes deletion

CREATE OR REPLACE FUNCTION system_attribute_delete(character varying, character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
	ClassName ALIAS FOR $1;
	AttributeName ALIAS FOR $2;
	field_list refcursor;
	field_bool bool;
	has_triggers bool;
	has_child bool;
	result bool;
	domain_name character varying; 

	tmpcname varchar(100);
	attrpresent bool;
    BEGIN
	result = false;
	OPEN field_list FOR EXECUTE 'SELECT system_attribute_isempty('''|| ClassName ||''', '''|| AttributeName ||''') ';
        FETCH field_list INTO field_bool;
        CLOSE field_list;
        IF NOT field_bool THEN
		RAISE EXCEPTION 'Cannot delete %.% , contains data',ClassName,AttributeName;
	END IF;

	OPEN field_list FOR EXECUTE 'SELECT system_class_haschild('''|| ClassName ||''') ';
        FETCH field_list INTO has_child;
        CLOSE field_list;

	OPEN field_list FOR EXECUTE 'SELECT system_class_hastrigger('''|| ClassName ||''') ';
        FETCH field_list INTO has_triggers;
        CLOSE field_list;
        
    OPEN field_list FOR EXECUTE 'SELECT system_read_comment("attributecomment", ''REFERENCEDOM'') FROM system_attributecatalog  WHERE "classname"='''|| ClassName ||''' AND "attributename"='''|| AttributeName ||''';';
        FETCH field_list INTO domain_name;
        CLOSE field_list;

	IF has_child THEN
		OPEN field_list for execute 'SELECT child FROM system_treecatalog WHERE parent=''' || ClassName || ''';';
		LOOP
			FETCH field_list INTO tmpcname;
			EXIT WHEN NOT FOUND;
			perform deleteattribute(tmpcname,AttributeName);
		END LOOP;
		OPEN field_list FOR EXECUTE 'SELECT COUNT(*) FROM pg_attribute,pg_type WHERE attrelid=pg_type.typrelid and pg_type.typname='''|| ClassName ||''' and pg_attribute.attname='''|| AttributeName ||''';';
		FETCH field_list into attrpresent;
		IF (has_triggers) THEN
			EXECUTE 'DROP TRIGGER after_archive_row_'||ClassName||' on "'||ClassName||'";';
			EXECUTE 'DROP FUNCTION after_archive_row_'||ClassName||'();';
		END IF;

		IF (has_triggers) THEN 
			EXECUTE 'SELECT system_class_createaftertriggers('''|| ClassName ||''') ';
		END IF;
		result = true;
	ELSE
		IF (domain_name<>'') THEN
			  EXECUTE  'select system_deletereference('''||ClassName||''', '''||AttributeName||''');';
		END IF;	
		OPEN field_list FOR EXECUTE 'SELECT COUNT(*) FROM pg_attribute,pg_type WHERE attrelid=pg_type.typrelid AND pg_type.typname='''|| ClassName ||''' and pg_attribute.attname='''|| AttributeName ||''';';
		FETCH field_list into attrpresent;
		IF( attrpresent ) THEN
			--ok, the attribute is on the table, delete it.
			EXECUTE 'ALTER TABLE "'|| ClassName ||'" DROP COLUMN "'|| AttributeName ||'" CASCADE; ';
		END IF;
		IF (has_triggers) THEN
			EXECUTE 'DROP TRIGGER after_archive_row_'||ClassName||' on "'||ClassName||'";';
			EXECUTE 'DROP FUNCTION after_archive_row_'||ClassName||'();';
		END IF;
		IF (has_triggers) THEN 
			EXECUTE 'SELECT system_class_createaftertriggers('''|| ClassName ||''') ';
		END IF;
		result = true;
	END IF;
	RETURN result;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_attribute_isempty(character varying, character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
        ClassName ALIAS FOR $1;
        AttributeName ALIAS FOR $2;
	field_list refcursor;
	field_num integer;
    BEGIN
	OPEN field_list FOR EXECUTE 
	     'SELECT COUNT(*) FROM "'|| ClassName ||'" WHERE "'|| AttributeName ||'" IS NOT NULL '
	     || 'AND "' || AttributeName ||'"::text<>'''' LIMIT 1;';
        FETCH field_list INTO field_num;
        CLOSE field_list;
	IF field_num>0
	   THEN RETURN false;
	END IF;
	RETURN true;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
