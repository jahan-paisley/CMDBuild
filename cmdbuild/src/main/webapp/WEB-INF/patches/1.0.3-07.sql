-- Delete attributes on superclasses

CREATE OR REPLACE VIEW system_attributecatalog AS 
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


CREATE OR REPLACE FUNCTION system_deletereference(character varying, character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
    ClassName ALIAS FOR $1;
	AttributeName ALIAS FOR $2;
	field_list refcursor;
	hastrigger bool; --has reference trigger?
	hasfk bool; -- has foreign key?
	reftype varchar(20); -- restriced or cascade
	referenced varchar(100); -- referenced class
	domname varchar(100); -- domain name
	domcard varchar(10); -- domain cardinality
	ctrl bool; -- true if there is a trigger on referenced class
	trgpost varchar(100); -- final part of the trigger name
	
    BEGIN
	OPEN field_list FOR EXECUTE 'SELECT system_class_hasreferencetrigger('''|| ClassName ||''','''||AttributeName||''') ';
        FETCH field_list INTO hastrigger;
        CLOSE field_list;
	OPEN field_list FOR EXECUTE 'SELECT system_class_hasreferencefk('''|| ClassName ||''','''||AttributeName||''') ';
        FETCH field_list INTO hasfk;
        CLOSE field_list;

	raise notice 'Deleting reference %.% (has reference trigger:%) (has fk:%)',ClassName,AttributeName,hastrigger,hasfk;

	-- trigger
	IF (hastrigger) THEN
		EXECUTE 'DROP TRIGGER reference_'||ClassName||'_'||AttributeName||' ON "'||ClassName||'" CASCADE;';
	END IF;
	-- constraint
	IF (hasfk) THEN
		EXECUTE 'ALTER TABLE "'||ClassName||'" DROP CONSTRAINT "'||ClassName||'_'||AttributeName||'_fkey";';
	END IF;
	
	--if is a cascade reference, delete the trigger on refereced class too
	open field_list for execute '
		SELECT attributereferencetype,attributereference::varchar,attributereferencedomain FROM system_attributecatalog WHERE classname='''||ClassName||''' AND attributename='''||AttributeName||''';';
		fetch field_list  INTO reftype,referenced,domname; 
	close field_list;
	
	raise notice 'Reference %.%  has type:%, referenced:%, domname:%',ClassName,AttributeName,reftype,referenced,domname;
	
	-- if the referenced class is not null
	if( referenced is not null ) then
		trgpost = LOWER(domname) || '_' || LOWER(ClassName);
		-- if the reference is restrict
		IF(reftype='restrict') THEN
			select into ctrl system_trigger_exists('restrict_'||trgpost);
			raise notice 'restrict trigger exists: %',ctrl;
			-- if the reference trigger exist
			if( ctrl = true ) then 
				raise notice 'deleting restrict triggers restrict_%_%() on %',domname,ClassName,referenced;
				EXECUTE 'DROP TRIGGER restrict_'||trgpost||' ON "'||referenced||'" CASCADE;';
			end if;
		ELSIF(reftype='cascade') THEN
			select into ctrl system_trigger_exists('cascade_'||trgpost);
			raise notice 'cascade trigger exists: %',ctrl;
			if( ctrl = true ) then 
				raise notice 'deleting cascade triggers cascade_%_%() on %',domname,ClassName,referenced;
				EXECUTE 'DROP TRIGGER cascade_'||trgpost||' ON "'||referenced||'" CASCADE;';
			end if;
		END IF;
		IF(reftype='setnull') THEN
			select into ctrl system_trigger_exists('setnull_'||trgpost);
			raise notice 'setnull trigger exists: %',ctrl;
			if( ctrl = true ) then 
				raise notice 'deleting setnull triggers setnull_%_%() on %',domname,ClassName,referenced;
			EXECUTE 'DROP TRIGGER setnull_'||trgpost||' ON "'||referenced||'"; CASCADE';
			end if;
		END IF;
	else
		raise notice 'cannot find referenced type for %.% (%,%,%) ',ClassName,AttributeName, reftype,referenced,domname;
	end if;

	RETURN true;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_attribute_delete(ClassName character varying,
	AttributeName character varying) RETURNS BOOLEAN AS
$BODY$
	BEGIN
	PERFORM system_attribute_delete_recursion(ClassName, AttributeName, TRUE);
	-- looks like, we don't know how to use exceptions
	RETURN TRUE;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION system_attribute_delete_recursion(ClassName character varying,
	AttributeName character varying, IsBase boolean)
  RETURNS VOID AS
$BODY$
    DECLARE
	isempty bool;
	has_triggers bool;
	is_superclass bool;
	attrlocal bool;
	subclasses refcursor;
	classcomment character varying;
	Subclass character varying;
    BEGIN
	-- stop if the attribute is not empty (even in the history)
	EXECUTE 'SELECT system_attribute_isempty('''|| ClassName ||''', '''|| AttributeName ||''')' INTO isempty;
        IF NOT isempty THEN
		RAISE EXCEPTION 'Cannot delete %.% , contains data', ClassName, AttributeName;
	END IF;

	-- store if the attribute is local for later use
	-- (once the comment is removed, we can't query the attribute catalog)
	EXECUTE 'SELECT attributeislocal FROM system_attributecatalog '||
		'WHERE classname='''|| ClassName ||''' AND attributename='''|| AttributeName ||'''' INTO attrlocal;
	IF (IsBase AND NOT attrlocal) THEN
		RAISE EXCEPTION 'Attribute %.% not local', ClassName, AttributeName;
	END IF;

	-- find if it is a superclass
	EXECUTE 'SELECT classcomment FROM system_classcatalog WHERE classname = '''|| ClassName ||'''' INTO classcomment;
	EXECUTE 'SELECT system_class_issuperclass('''||classcomment||''')' INTO is_superclass;

	-- remove the reference triggers if they exist
	EXECUTE 'SELECT system_deletereference('''|| ClassName ||''', '''|| AttributeName ||''')';
	-- remove the attribute comment
	EXECUTE 'COMMENT ON COLUMN "'|| ClassName ||'"."'||AttributeName||'" IS NULL';

	IF is_superclass THEN
		RAISE NOTICE 'Class % is a superclass', ClassName;
		-- visit childs
		OPEN subclasses FOR EXECUTE 'SELECT child FROM system_treecatalog WHERE parent=''' || ClassName || '''';
		LOOP
			FETCH subclasses INTO Subclass;
			EXIT WHEN NOT FOUND;
			PERFORM system_attribute_delete_recursion(Subclass, AttributeName, FALSE);
		END LOOP;
	ELSE
		RAISE NOTICE 'Class % is not a superclass', ClassName;
		-- recreate after triggers
		EXECUTE 'DROP FUNCTION after_archive_row_'||ClassName||'() CASCADE';
		EXECUTE 'SELECT system_class_createaftertriggers('''|| ClassName ||''') ';
	END IF;

	-- remove the table column if local
	IF attrlocal THEN
		EXECUTE 'ALTER TABLE "'|| ClassName ||'" DROP COLUMN "'|| AttributeName ||'" CASCADE';
	END IF;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
