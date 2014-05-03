-- Fix class deletion (not if domains and delete reference trigger functions)

CREATE OR REPLACE FUNCTION system_class_delete(character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
        ClassName ALIAS FOR $1;
	AttributeName varchar(50);
	AttributeReference varchar(50);
	AttributeReferenceType varchar(50);
	field_list refcursor;
	empty bool;
	has_trigger bool;
	has_child bool;
	has_domains bool;
	result bool;
    BEGIN
	-- ver 0.5
	result = false;
	OPEN field_list FOR EXECUTE 'SELECT system_class_hasdomains('''|| ClassName ||''')';
        FETCH field_list INTO has_domains;
        CLOSE field_list;
	OPEN field_list FOR EXECUTE 'SELECT system_class_isempty('''|| ClassName ||''') ';
        FETCH field_list INTO empty;
        CLOSE field_list;
	OPEN field_list FOR EXECUTE 'SELECT system_class_haschild('''|| ClassName ||''') ';
        FETCH field_list INTO has_child;
        CLOSE field_list;
	OPEN field_list FOR EXECUTE 'SELECT system_class_hastrigger('''|| ClassName ||''') ';
	    FETCH field_list INTO has_trigger;
        CLOSE field_list;

    IF has_domains THEN
    	RAISE EXCEPTION 'Cannot delete class %: has domains', ClassName;
    ELSEIF has_child THEN
		RAISE EXCEPTION 'Cannot delete class %: has childs', ClassName;
	ELSEIF NOT empty THEN
		RAISE EXCEPTION 'Cannot delete class %: contains data', ClassName;
	ELSE
		IF (has_trigger) THEN
		   EXECUTE 'SELECT system_class_deletetriggers('''||ClassName||''')';
		END IF;
		EXECUTE 'DROP TABLE "'|| ClassName ||'" CASCADE';
		result = true;
	END IF;
	RETURN result;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_deletetriggers(character varying) RETURNS VOID
  AS
$BODY$
    DECLARE
        ClassName ALIAS FOR $1;
        trigger RECORD;
    BEGIN
	FOR trigger IN EXECUTE 'SELECT tgname AS name FROM pg_trigger JOIN pg_class ON pg_trigger.tgrelid = pg_class.oid WHERE pg_class.relname = '''||ClassName||''' AND tgisconstraint = false AND tgname <> ''before_archive_row'''
	LOOP
	    EXECUTE 'DROP TRIGGER '||trigger.name||' ON "'||ClassName||'";';
	    EXECUTE 'DROP FUNCTION '||trigger.name||'();';
	END LOOP;
	EXECUTE 'DROP TRIGGER before_archive_row ON "'||ClassName||'";';
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_hasdomains(character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
        ClassName ALIAS FOR $1;
		field_list refcursor;
		field_num integer;
    BEGIN
	OPEN field_list FOR EXECUTE 
	     'SELECT COUNT(*) FROM system_domaincatalog WHERE domainclass1='''||ClassName||''' OR domainclass2='''||ClassName||''' LIMIT 1;';
        FETCH field_list INTO field_num;
        CLOSE field_list;
	IF field_num>0
	   THEN RETURN true;
	END IF;
	RETURN false;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
