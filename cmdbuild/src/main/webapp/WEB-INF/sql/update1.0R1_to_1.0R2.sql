COMMENT ON TABLE "Class" IS 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active';
COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: AttivitÃ |SUPERCLASS: true|MANAGER: activity|STATUS: active';
COMMENT ON COLUMN "Activity"."ActivityDescription" IS 'MODE: write|DESCR: Descrizione AttivitÃ |INDEX: 4|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: |STATUS: active';


DROP VIEW system_legacygraph;


INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.running','FlowStatus', 1, 'Avviato', true, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.not_running.suspended','FlowStatus', 2, 'Sospeso', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.completed','FlowStatus', 3, 'Completato', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.terminated','FlowStatus', 4, 'Terminato', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.aborted','FlowStatus', 5, 'Interrotto', false, 'A');


CREATE OR REPLACE FUNCTION system_reference_updated(integer, integer, integer, integer, character varying, integer, boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	class1Id ALIAS FOR $1;
	idobj1 ALIAS FOR $2;
	class2Id ALIAS FOR $3;
	idobj2 ALIAS FOR $4;
	domainName ALIAS FOR $5;
	domainId ALIAS FOR $6;
	direct ALIAS FOR $7;

	curValueName varchar(50); -- the id column name for the reference (side 1 of the relation)
	nSideName varchar(50); -- the id column name for the current card (side N of the relation)
	nSideId int4; -- current card id (side N of the relation)
	curvalue int4;  -- "old" reference id (side 1 of the relation)
	cfrId int4; -- selected reference id (side 1 of the relation)
	refClassId varchar(20); -- classid colum name for the reference (side 1 of the relation) (IdClass1 or IdClass2)
	refClassIdValue oid; -- classid name for the reference (side 1 of the relation) (the value of IdClass1 or IdClass2)
BEGIN
	IF(direct) THEN
		nSideId = idobj2;
		cfrId = idobj1;
		curValueName = 'IdObj1';
		nSideName = 'IdObj2';
		refClassId ='IdClass1';
		refClassIdValue =class1Id;
	ELSE
		nSideId = idobj1;
		cfrId = idobj2;
		curValueName = 'IdObj2';
		nSideName = 'IdObj1';
		refClassId ='IdClass2';
		refClassIdValue =class2Id;
	END IF;
	
	-- search for relation with idObj (side N), if there is any return the id inside curvalue
	SELECT INTO curvalue system_relation_getvalue(domainName,nSideId,curValueName);
	raise notice 'Update relation and reference for card %. Reference was % and will be % ',nSideId,curvalue,curValueName;
	
	-- there is a relation (with id=curvalue) 
	--    AND the new id (cfrId) of the reference selected is not null 
	--    AND old id and new id are not the same  
	--	-> relation must be updated
	IF( curvalue IS NOT NULL AND cfrId IS NOT NULL AND curvalue<>cfrId ) THEN
		EXECUTE 'UPDATE "Map_'||domainName||'" SET "'||curValueName||'"='||cfrId||', "'||refClassId||'"='||refClassIdValue||' WHERE "'||curValueName||'"='||curvalue||' AND "'||nSideName||'"='||nSideId||' AND "Status"=''A'';';
	-- there isn't a relation 
	--    AND the new id (cfrId) of the reference selected is not null
	--	-> relation must be created
	ELSIF( curvalue IS NULL AND cfrId IS NOT NULL) THEN
		EXECUTE 'INSERT INTO "Map_'||domainName||'" ("IdDomain","IdClass1","IdObj1","IdClass2","IdObj2","Status") VALUES ('||domainId||','||class1Id||','||idobj1||','||class2Id||','||idobj2||',''A'');';
	-- there is a previous relation 
	--    AND the new id (cfrId) of the reference selected is null 
	--	-> relation must be deleted
	ELSIF( curvalue IS NOT NULL AND cfrId IS NULL ) THEN
		EXECUTE 'DELETE FROM "Map_'||domainName||'" WHERE "'||curValueName||'"='||curvalue||' AND "'||nSideName||'"='||nSideId||' AND "Status"=''A'';';
	END IF;
	RETURN true;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


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


update "LookUp" set "ParentType"=null where "ParentType"='null' OR  "ParentType"='';
delete from "LookUp"  where "Type"='' and "Status"<>'A';


  
CREATE OR REPLACE FUNCTION system_domain_createtriggers(character varying)
  RETURNS boolean AS
$BODY1$
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

		SELECT INTO IdClass1 classname FROM system_classcatalog WHERE "classid"=NEW."IdClass1"::oid;
		SELECT INTO IdClass2 classname FROM system_classcatalog WHERE "classid"=NEW."IdClass2"::oid;
		

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
$BODY1$
  LANGUAGE 'plpgsql' VOLATILE;

CREATE OR REPLACE FUNCTION system_updatedomainaftertriggers()
  RETURNS boolean AS
$BODY$
DECLARE
tables RECORD;
domains RECORD;
field_list refcursor;
has_triggers boolean;

BEGIN
  FOR tables IN SELECT classname FROM system_classcatalog
  LOOP
    OPEN field_list FOR EXECUTE 'SELECT system_class_hastrigger('''|| tables.classname ||''') ';
        FETCH field_list INTO has_triggers;
        CLOSE field_list;

    IF (has_triggers) THEN
        EXECUTE 'DROP TRIGGER after_archive_row_'||tables.classname||' on "'||tables.classname||'";';
        EXECUTE 'DROP FUNCTION after_archive_row_'||tables.classname||'();';
    END IF;
    IF (has_triggers) THEN
        EXECUTE 'SELECT system_class_createaftertriggers('''|| tables.classname ||''') ';
    END IF;

  END LOOP;

  FOR domains IN SELECT * FROM system_domaincatalog
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
COMMENT ON FUNCTION system_updatedomainaftertriggers() IS 'Enable all the after_archive_row triggers in database';

CREATE OR REPLACE FUNCTION system_reference_inserted(integer, integer, integer, integer, character varying, integer, boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	class1Id ALIAS FOR $1;
	idobj1 ALIAS FOR $2;
	class2Id ALIAS FOR $3;
	idobj2 ALIAS FOR $4;
	domainName ALIAS FOR $5;
	domainId ALIAS FOR $6;
	direct ALIAS FOR $7;

	curValueName varchar(50); -- the id column name for the reference (side 1 of the relation)
	nSideName varchar(50); -- the id column name for the current card (side N of the relation)
	nSideId int4; -- current card id (side N of the relation)
	curvalue int4;  -- "old" reference id (side 1 of the relation)
	cfrId int4; -- selected reference id (side 1 of the relation)
BEGIN
	raise notice 'Domain is direct? %', direct;
	
	IF(direct) THEN
		nSideId = idobj2;
		cfrId = idobj1;
		curValueName = 'IdObj1';
		nSideName = 'IdObj2';
	ELSE
		nSideId = idobj1;
		cfrId = idobj2;
		curValueName = 'IdObj2';
		nSideName = 'IdObj1';
	END IF;

	IF(cfrId IS NULL) THEN
		return true;
	END IF;

	-- search for relation with idObj (side N), if there is any return the id inside curvalue
	SELECT INTO curvalue system_relation_getvalue(domainName,nSideId,curValueName);
	raise notice 'Insert reference % setting reference from % to % ',nSideId,curvalue,curValueName;
		
	IF(curvalue IS NULL) 
	THEN
		-- correct, there isn't relation so we can create it
		EXECUTE 'INSERT INTO "Map_'||domainName||'" ("IdDomain","IdClass1","IdObj1","IdClass2","IdObj2","Status") VALUES ('||domainId||','||class1Id||','||idobj1||','||class2Id||','||idobj2||',''A'');';
	ELSE
		-- there is a relation 
		raise notice 'Inserted reference % with a previous relation with idobj %',curvalue;
		IF(curvalue <> cfrId) 
		THEN
			-- the relation has two different id (this is a problem... but we will try to update to fix it...)
			raise notice 'Updating relation setting reference from % to % ',nSideId,curvalue,curValueName;
			EXECUTE 'UPDATE "Map_'||domainName||'" SET "'||curValueName||'"='||cfrId||' WHERE "'||curValueName||'"='||curvalue||' AND "'||nSideName||'"='||nSideId||' AND "Status"=''A'';';
		END IF;
	END IF;
	RETURN true;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

select system_updatedomainaftertriggers();

DROP FUNCTION system_updatedomainaftertriggers();
