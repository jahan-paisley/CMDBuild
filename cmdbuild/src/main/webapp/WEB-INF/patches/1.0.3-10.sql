-- Adapt the 1.0.3 database to the 1.1.0 version

CREATE OR REPLACE FUNCTION system_isexecutor(oid, integer, character varying) RETURNS boolean
    AS $_$
    DECLARE
        tableoid alias for $1;
        id alias for $2;
        executor alias for $3;
        field_list refcursor;
        ctrl boolean;
        classname varchar;
    BEGIN
        OPEN field_list FOR EXECUTE  
        'select classname::varchar from system_classcatalog where classid='||tableoid;
        FETCH field_list INTO classname;
        CLOSE field_list;
        OPEN field_list FOR EXECUTE  
        'select ((select count(*) from "'||classname||'_history" as ch where ch."CurrentId" = '||id||' and ch."NextExecutor" = '''||executor||''') != 0) or ('''||executor||''' = (select "NextExecutor" from "'||classname||'" where "Id"='||id||'))';
        FETCH field_list INTO ctrl;
        CLOSE field_list;
        return ctrl;
    END;
	$_$
	LANGUAGE 'plpgsql' VOLATILE;

ALTER FUNCTION public.system_isexecutor(oid, integer, character varying) OWNER TO postgres;



CREATE OR REPLACE FUNCTION system_domain_delete(character varying) RETURNS boolean
    AS $_$
    DECLARE
        ClassName ALIAS FOR $1;
		field_list refcursor;
		field_bool bool;
		field_bool2 bool;
		result bool;
    BEGIN
		result = false;
		OPEN field_list FOR EXECUTE 'SELECT system_class_isempty(''Map_'|| ClassName ||''') ';
	        FETCH field_list INTO field_bool;
	        CLOSE field_list;
		IF field_bool THEN 
		   EXECUTE 'DROP TABLE "Map_'|| ClassName ||'" CASCADE';	
		   result = true;
		ELSE
		   RAISE EXCEPTION 'Cannot delete domain %, contains data', ClassName;
		END IF;
		RETURN result;
    END;
	$_$
    LANGUAGE 'plpgsql' VOLATILE;

ALTER FUNCTION public.system_domain_delete(character varying) OWNER TO postgres;



CREATE OR REPLACE FUNCTION system_domain_createtriggers(character varying) RETURNS boolean
    AS $_$
    DECLARE
        DomainName ALIAS FOR $1;
        RightDomain varchar;
    BEGIN
	IF( '_' = substring(DomainName from 4 for 1) ) THEN
		RightDomain = substring(DomainName,5);
	ELSE
		RightDomain = DomainName;
	END IF;
	EXECUTE'
	CREATE OR REPLACE FUNCTION after_archive_relation_row_Map_'||RightDomain||'() RETURNS TRIGGER AS 
	$BODY$
	DECLARE
		IdObj1 integer;
		IdObj2 integer;
		IdObj1Old integer;
		IdObj2Old integer;

		ChangeFrom integer;
		ChangeFromOld integer;
		ChangeTo integer;
		ChangeToOld integer;
		ChangeClass varchar(50);

		DomCard varchar(10);
		DomCl1 varchar(50);
		DomCl2 varchar(50);
		hasRef integer;
		RefName varchar(50);
		field_list refcursor;
		intval integer;

		IdClass1 varchar(70);
		IdClass2 varchar(70);
		
	BEGIN
		-- insert into history, if this is not a newly created relation
		IF( TG_OP <> ''INSERT'' ) THEN
			OLD."Status" = ''U'';
    	    	OLD."EndDate" = now();
			INSERT INTO "Map_'||RightDomain||'_history" select OLD.*;
		END IF;

		SELECT INTO DomCard,DomCl1,DomCl2 domaincardinality,domainclass1,domainclass2 FROM system_domaincatalog WHERE domainname='''||RightDomain||''';

		IF( TG_OP = ''DELETE'' ) THEN
			SELECT INTO IdClass1 classname FROM system_classcatalog WHERE "classid"=OLD."IdClass1"::oid;
			SELECT INTO IdClass2 classname FROM system_classcatalog WHERE "classid"=OLD."IdClass2"::oid;
		ELSE
			SELECT INTO IdClass1 classname FROM system_classcatalog WHERE "classid"=NEW."IdClass1"::oid;
			SELECT INTO IdClass2 classname FROM system_classcatalog WHERE "classid"=NEW."IdClass2"::oid;
		END IF;

		IF( TG_OP = ''INSERT'' ) THEN
			--search class, attr.ref.domain, nSide id, 1side id
			IF(DomCard=''1:N'')THEN
				PERFORM system_relation_inserted(IdClass2,'''||RightDomain||''',NEW."IdObj2",NEW."IdObj1");
			ELSIF(DomCard=''N:1'')THEN
				PERFORM system_relation_inserted(IdClass1,'''||RightDomain||''',NEW."IdObj1",NEW."IdObj2");
			END IF;
		ELSIF( TG_OP = ''DELETE'' ) THEN
			IF(DomCard=''1:N'')THEN
				PERFORM system_relation_deleted(IdClass2,'''||RightDomain||''',OLD."IdObj2");
			ELSIF(DomCard=''N:1'')THEN
				PERFORM system_relation_deleted(IdClass1,'''||RightDomain||''',OLD."IdObj1");
			END IF;
		ELSIF( TG_OP = ''UPDATE'' ) THEN
			IF(DomCard=''1:N'')THEN
				IF(NEW."Status"=''A'') THEN
					PERFORM system_relation_updated(IdClass2,'''||RightDomain||''',NEW."IdObj2",OLD."IdObj2",NEW."IdObj1",OLD."IdObj1");
				ELSIF(NEW."Status"=''N'') THEN
					PERFORM system_relation_logicdelete(IdClass2,'''||RightDomain||''',OLD."IdObj2",OLD."IdObj1");
				END IF;
			ELSIF(DomCard=''N:1'')THEN
				IF(NEW."Status"=''A'') THEN
					PERFORM system_relation_updated(IdClass1,'''||RightDomain||''',NEW."IdObj1",OLD."IdObj1",NEW."IdObj2",OLD."IdObj2");
				ELSIF(NEW."Status"=''N'') THEN
					PERFORM system_relation_logicdelete(IdClass1,'''||RightDomain||''',OLD."IdObj1",OLD."IdObj2");
				END IF;
			END IF;
		END IF;

		RETURN null;
	END;
	$BODY$
  	LANGUAGE ''plpgsql'' VOLATILE;
	';

	EXECUTE'
	CREATE TRIGGER before_archive_relation_row
	BEFORE INSERT OR UPDATE
	ON "Map_'||RightDomain||'"
	FOR EACH ROW
	EXECUTE PROCEDURE before_archive_relation_row();';

	EXECUTE'
	CREATE TRIGGER after_archive_relation_row_Map_'||RightDomain||'
	AFTER INSERT OR UPDATE OR DELETE
	ON "Map_'||RightDomain||'"
	FOR EACH ROW
	EXECUTE PROCEDURE after_archive_relation_row_Map_'||RightDomain||'();';

	RETURN true;
    END;
$_$
    LANGUAGE 'plpgsql' VOLATILE;

ALTER FUNCTION public.system_domain_createtriggers(character varying) OWNER TO postgres;


CREATE OR REPLACE FUNCTION system_patch_updatedomaintriggers() RETURNS boolean AS
$BODY$
DECLARE
tables RECORD;
domains RECORD;
field_list refcursor;
has_triggers boolean;

BEGIN

  FOR domains IN SELECT pg_class.oid AS domainid, "substring"(pg_class.relname::text, 5) AS domainname,
				substring(pg_description.description, 'CARDIN: ([^|]*)'::text) AS domaincardinality
			FROM pg_class
			LEFT JOIN pg_description pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
			WHERE strpos(pg_description.description, 'TYPE: domain'::text) > 0 AND pg_class.relkind<>'v'
  LOOP
	IF(domains.domainname<>'')
	THEN
		EXECUTE 'DROP TRIGGER after_archive_relation_row_Map_'||domains.domainname ||' on "Map_'||domains.domainname ||'";';
		EXECUTE 'DROP TRIGGER before_archive_relation_row on "Map_'||domains.domainname ||'";';
		EXECUTE 'DROP FUNCTION after_archive_relation_row_Map_'||domains.domainname ||'();';
		-- create trigger
		EXECUTE 'SELECT system_domain_createtriggers('''||domains.domainname ||''')';

		EXECUTE 'ALTER TABLE "Map_' || domains.domainname || '" DROP CONSTRAINT "Map_' || domains.domainname || '_pkey"';
		EXECUTE 'ALTER TABLE "Map_' || domains.domainname || '_history" DROP CONSTRAINT "Map_' || domains.domainname || '_history_pkey"';

		EXECUTE 'ALTER TABLE "Map_'|| domains.domainname ||'" ADD CONSTRAINT "Map_'|| domains.domainname ||'_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate")';
		EXECUTE 'ALTER TABLE "Map_'|| domains.domainname||'_history" ADD CONSTRAINT "Map_'|| domains.domainname || '_history_pkey" PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate")';

	END IF;
  END LOOP;

  RETURN true;

END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;

SELECT system_patch_updatedomaintriggers();

DROP FUNCTION system_patch_updatedomaintriggers();