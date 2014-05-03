-------------------------------------------------------
------           CREATE BACKUP SCHEMA           -------
-------------------------------------------------------

CREATE SCHEMA "backup_09";

COMMENT ON COLUMN "Role"."startingClass" IS 'MODE: write|DESCR: StartingPage|INDEX: 2|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: 0|STATUS: active';

-------------------------------------------------------
------      RENAME Map_ruoli TO Map_UserRole    -------
-------------------------------------------------------
-- Fix because of a serious flaw in CMDBuild 0.9
UPDATE "Map_ruoli" SET "IdClass1" = '"User"', "IdClass2" = '"Role"';

ALTER TABLE "Map_ruoli" RENAME TO "Map_UserRole";
ALTER TABLE "Map_ruoli_history" RENAME TO "Map_UserRole_history";

-------------------------------------------------------
------     CHANGE Class COMMENTS FOR INDEX #     ------
-------------------------------------------------------

COMMENT ON TABLE "Class" IS 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active';
COMMENT ON COLUMN "Class"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Class"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Class"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';

-------------------------------------------------------
------         CHANGE MODE OF Activity          -------
-------------------------------------------------------

COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: Attività|SUPERCLASS: true|MANAGER: activity|STATUS: active';

-------------------------------------------------------
------      CHANGE INHERITANCE OF LookUp        -------
-------------------------------------------------------
-- Note: this script preserves the Ids, so there     --
--       is not the need to modify all other tables. --
-------------------------------------------------------

ALTER TABLE "LookUp" SET SCHEMA "backup_09";
CREATE TABLE "LookUp"
(
  "Type" character varying(32),
  "ParentType" character varying(32),
  "ParentId" integer,
  "Number" integer NOT NULL,
  "IsDefault" boolean NOT NULL,
  CONSTRAINT "LookUp_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");

COMMENT ON TABLE "LookUp" IS 'MODE: reserved|TYPE: class|DESCR: Lookup list|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "LookUp"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Code" IS 'MODE: read|DESCR: Code|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "LookUp"."Description" IS 'MODE: read|DESCR: Description|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "LookUp"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Notes" IS 'MODE: read|DESCR: Annotazioni';
COMMENT ON COLUMN "LookUp"."Type" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."ParentType" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."ParentId" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Number" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."IsDefault" IS 'MODE: reserved';

INSERT INTO "LookUp" ("Id", "IdClass", "Description", "ParentType", "ParentId", "Type", "IsDefault", "Number", "Notes", "Status")
	SELECT "Id", '"LookUp"'::regclass, "Description", "ParentType", "ParentId", "Type", "IsDefault", "Number", "Notes", "Status"
	FROM "backup_09"."LookUp";

-------------------------------------------------------
------           CHANGES for Nav. Menu          -------
-------------------------------------------------------
ALTER TABLE "Menu" ADD COLUMN "Type" character varying(70);
select deletetriggers('Menu');
select createtriggers('Menu');


UPDATE "Menu" set "Code"='folder', "Type"='folder' WHERE "Code" ILIKE 'folder';
UPDATE "Menu" set "Code"='class', "Type"='class' WHERE "Code" ILIKE 'class';
UPDATE "Menu" set "Code"='class', "Type"='superclass' WHERE "Code" ILIKE 'superclass';
UPDATE "Menu" set "Code"='processclass',"Type"='processclass' WHERE "Code" ILIKE 'process';
UPDATE "Menu" set "Code"='processclass',"Type"='processclass' WHERE "Code"='processclass' AND "Type"='process';
UPDATE "Menu" set "Code"='processclass', "Type"='superclassprocess' WHERE "Code" ILIKE 'Process Superclass';
UPDATE "Menu" set "Code"='processclass', "Type"='superclassprocess' WHERE "Code"='processclass' AND "Type"='superclassprocess';
UPDATE "Menu" set "Code"='report', "Type"='reportpdf' WHERE "Code" ILIKE 'Report [PDF]';
UPDATE "Menu" set "Code"='report', "Type"='reportcsv' WHERE "Code" ILIKE 'Report [CSV]';
UPDATE "Menu" set "Code"='report', "Type"='reportodt' WHERE "Code" ILIKE 'OpenOffice [ODT]';
UPDATE "Menu" set "Code"='report', "Type"='reportxml' WHERE "Code" ILIKE 'OpenOffice [XML]';
UPDATE "Menu" SET "Code"='view' WHERE "Code" ILIKE 'View';
UPDATE "Menu" SET "Code"='processclass' WHERE "Code"='process';

-------------------------------------------------------
------           CHANGES for GRANT              -------
-------------------------------------------------------

DROP TABLE "Grant_history";
DROP TRIGGER after_archive_row_grant ON "Grant";

insert into "Grant"( "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode")
select "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", system_classcatalog.classid, case when "Mode" = 'x' then 'w' else "Mode" end
from "Grant", system_classcatalog
where "IdGrantedClass" = '-'::regclass and "Status" = 'A' and classid<>'"Class"'::regclass;

delete from "Grant" where ("IdGrantedClass" = '-'::regclass and "Status" = 'A') OR ("Status" <> 'A');

-------------------------------------------------------
------       CHANGES for WORKFLOW LOOKUPS       -------
-------------------------------------------------------
create or replace function updateFlowLookups() 
    returns bool as
$BODY$
DECLARE
    num integer;
    idcl regclass;
BEGIN
    update "LookUp" set "Description"='Interrotto' where "Type"='FlowStatus' and "Description"='Annullato';
    select into num count(*) from "LookUp" where "Type"='FlowStatus';
    select into idcl classid from system_classcatalog where classname='LookUp';

    update "LookUp" set "IsDefault"=false where "Type"='FlowStatus';
    update "LookUp" set "Code"='closed.completed',"Number"=3 where "Type"='FlowStatus' and "Description"='Completato';
    update "LookUp" set "Code"='closed.aborted',"Number"=5 where "Type"='FlowStatus' and "Description"='Interrotto';
    update "LookUp" set "Code"='open.running',"Number"=1,"IsDefault"=true where "Type"='FlowStatus' and "Description"='Avviato';
    if(num = 3) then
        INSERT INTO "LookUp" ("IdClass", "Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES (idcl,'open.not_running.suspended','FlowStatus', 2, 'Sospeso', false, 'A');
        INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES (idcl,'closed.terminated','FlowStatus', 4, 'Terminato', false, 'A');
    end if;
    if (num = 6) then 
        delete from "LookUp" where "Type"='FlowStatus' and "Description"='Non avviato';
        update "LookUp" set "Code"='open.not_running.suspended',"Number"=2 where "Type"='FlowStatus' and "Description"='Sospeso';
        update "LookUp" set "Code"='closed.terminated',"Number"=4 where "Type"='FlowStatus' and "Description"='Terminato';
    end if;
    if(num = 5) then
        update "LookUp" set "Code"='open.not_running.suspended',"Number"=2 where "Type"='FlowStatus' and "Description"='Sospeso';
        update "LookUp" set "Code"='closed.terminated',"Number"=4 where "Type"='FlowStatus' and "Description"='Terminato';
    end if;
    return true;
END;
$BODY$
language plpgsql volatile;

select updateFlowLookups();
drop function updateFlowLookups();

ALTER TABLE "Map_UserRole" DROP CONSTRAINT "Map_ruoli_pkey";
ALTER TABLE "Map_UserRole"
  ADD CONSTRAINT "Map_UserRole_pkey" PRIMARY KEY("IdObj1", "IdObj2");

ALTER TABLE "Map_UserRole_history" DROP CONSTRAINT "Map_ruoli_history_pkey";
ALTER TABLE "Map_UserRole_history"
  ADD CONSTRAINT "Map_UserRole_history_pkey" PRIMARY KEY("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");


CREATE OR REPLACE FUNCTION after_archive_relation_row_map_userrole()
  RETURNS trigger AS
$BODY$
	DECLARE
	BEGIN
		RETURN null;
	END;
	$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

CREATE TRIGGER after_archive_relation_row_map_userrole
  AFTER INSERT OR UPDATE OR DELETE
  ON "Map_UserRole"
  FOR EACH ROW
  EXECUTE PROCEDURE after_archive_relation_row_map_userrole();

CREATE TRIGGER before_archive_relation_row
  BEFORE INSERT OR UPDATE
  ON "Map_UserRole"
  FOR EACH ROW
  EXECUTE PROCEDURE before_archive_relation_row();

DROP VIEW cmdbavailableelements;
DROP VIEW cmdbclasscatalog;
DROP VIEW cmdbdomaincatalog;
DROP VIEW cmdbgroupcatalog;
DROP VIEW cmdbpolicycatalog;
DROP VIEW cmdbtreecatalog;
DROP VIEW cmdbviewcatalog;
DROP VIEW graph_relationlist;
DROP VIEW relationlist;
DROP VIEW reporttree;
DROP VIEW relationhistorylist;
DROP VIEW navigationtree;

DROP FUNCTION IF EXISTS createasset(character varying, character varying, integer, character varying, character varying, boolean, character varying);
DROP FUNCTION IF EXISTS createassetindex(character varying);
DROP FUNCTION IF EXISTS createasset(character varying, character varying, integer, character varying, character varying, boolean, character varying);
DROP FUNCTION IF EXISTS createattribute(character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createattribute(character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createattribute(character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createcascaderelationtriggers(character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createclass(character varying, character varying, character varying, character varying, boolean, character varying);
DROP FUNCTION IF EXISTS createclassindex(character varying);
DROP FUNCTION IF EXISTS createcomment(character varying, character varying);
DROP FUNCTION IF EXISTS createdomain(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, integer);
DROP FUNCTION IF EXISTS createhistory(character varying);
DROP FUNCTION IF EXISTS createplanner(character varying, character varying);
DROP FUNCTION IF EXISTS deleteplanner(character varying, character varying);
DROP FUNCTION IF EXISTS deletereference(character varying, character varying);
DROP FUNCTION IF EXISTS deleterelationtriggers(character varying);
DROP FUNCTION IF EXISTS deletetriggers(character varying);
DROP FUNCTION IF EXISTS deleteview(character varying);
DROP FUNCTION IF EXISTS dropallreferences(character varying);
DROP FUNCTION IF EXISTS droprecursivereference(character varying, character varying, character varying);
DROP FUNCTION IF EXISTS getreference(character varying, boolean);
DROP FUNCTION IF EXISTS createrecursiveplanner(character varying, character varying);
DROP FUNCTION IF EXISTS createrecursivereference(character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createreference(character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createrelation(integer, integer, integer, integer, integer, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createrelationhistory(character varying);
DROP FUNCTION IF EXISTS createrelationindex(character varying);
DROP FUNCTION IF EXISTS createrelationtriggers(character varying);
DROP FUNCTION IF EXISTS createrestrictrelationtriggers(character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createsetnullrelationtriggers(character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS createtriggers(character varying);
DROP FUNCTION IF EXISTS deleteattribute(character varying, character varying);
DROP FUNCTION IF EXISTS deleteclass(character varying);
DROP FUNCTION IF EXISTS deletedomain(character varying);
DROP FUNCTION IF EXISTS deleteviews();
DROP FUNCTION IF EXISTS fixclass04xto05();
DROP FUNCTION IF EXISTS haschild(character varying);
DROP FUNCTION IF EXISTS hasplanner(character varying, character varying);
DROP FUNCTION IF EXISTS hasreference(character varying, character varying);
DROP FUNCTION IF EXISTS hasreferencetrigger(character varying, character varying);
DROP FUNCTION IF EXISTS hasrelationtrigger(character varying);
DROP FUNCTION IF EXISTS hastrigger(character varying);
DROP FUNCTION IF EXISTS isempty(character varying);
DROP FUNCTION IF EXISTS isreferenceholder(character varying, character varying);
DROP FUNCTION IF EXISTS issuperclass(character varying);
DROP FUNCTION IF EXISTS isunique(character varying, character varying);
DROP FUNCTION IF EXISTS makerecursiveunique(character varying, character varying);
DROP FUNCTION IF EXISTS makeunique(character varying, character varying, boolean);
DROP FUNCTION IF EXISTS modifyasset(character varying, character varying, character varying, integer, character varying, boolean, character varying);
DROP FUNCTION IF EXISTS modifyattribute(character varying, character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyattribute(character varying, character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyattribute(character varying, character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyattributecomment(character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyattributecomment(character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyattributecomment(character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyattributesinglecomment(character varying, character varying, character varying, character varying);
DROP FUNCTION IF EXISTS modifyclass(character varying, character varying, character varying, character varying, boolean, character varying);
DROP FUNCTION IF EXISTS modifydomain(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, integer);
DROP FUNCTION IF EXISTS reference_deleted(integer, integer, integer, integer, character varying, integer, boolean);
DROP FUNCTION IF EXISTS reference_inserted(integer, integer, integer, integer, character varying, integer, boolean);
DROP FUNCTION IF EXISTS reference_logicdelete(integer, integer, integer, integer, character varying, integer, boolean);
DROP FUNCTION IF EXISTS reference_updated(integer, integer, integer, integer, character varying, integer, boolean);
DROP FUNCTION IF EXISTS relation_compare(character varying, character varying, character varying, character varying, integer);
DROP FUNCTION IF EXISTS relation_deleted(character varying, character varying, integer);
DROP FUNCTION IF EXISTS relation_getreferencevalue(character varying, integer, character varying);
DROP FUNCTION IF EXISTS relation_getvalue(character varying, integer, character varying);
DROP FUNCTION IF EXISTS relation_inserted(character varying, character varying, integer, integer);
DROP FUNCTION IF EXISTS relation_logicdelete(character varying, character varying, integer, integer);
DROP FUNCTION IF EXISTS relation_updated(character varying, character varying, integer, integer, integer, integer);
DROP FUNCTION IF EXISTS sortlookup();
DROP FUNCTION IF EXISTS trigger_exists(character varying);
DROP FUNCTION IF EXISTS updateallreferences(character varying, character varying);
DROP FUNCTION IF EXISTS updateclasstriggers061();
DROP FUNCTION IF EXISTS updateplanners0xto061();
DROP FUNCTION IF EXISTS updatesequence(character varying);
DROP FUNCTION IF EXISTS updatetriggers();


select system_disablealltriggers();
select system_regeneratereferencetriggers();
select system_disablealltriggers();
select system_updateallaftertriggers();
select system_disablealltriggers();
-- query to select all the relations without cards
-- select "Class1"."Id", "Class2"."Id", "Map".* from "Map" left join "Class" as "Class1" on "Class1"."IdClass" = "Map"."IdClass1" and "Class1"."Id" = "Map"."IdObj1" left join "Class" as "Class2" on "Class2"."IdClass" = "Map"."IdClass2" and "Class2"."Id" = "Map"."IdObj2" where "Class1"."Id" is null or "Class2"."Id" is null;
delete
	from "Map"
	where ("IdDomain", "IdClass1","IdClass1","IdObj1", "IdClass2", "IdObj2") in
		(select "Map"."IdDomain", "Map"."IdClass1","Map"."IdClass1", "Map"."IdObj1", "Map"."IdClass2", "Map"."IdObj2"
			from "Map"
        	left join "Class" as "Class1" on "Class1"."IdClass" = "Map"."IdClass1" and "Class1"."Id" = "Map"."IdObj1"
        	left join "Class" as "Class2" on "Class2"."IdClass" = "Map"."IdClass2" and "Class2"."Id" = "Map"."IdObj2"
			where "Class1"."Id" is null or "Class2"."Id" is null);
select system_updateregclasses(); -- update all the classid putting always the id of the subclass instead of the superclass 
select system_enablealltriggers();

update "LookUp" set "ParentType"=null where "ParentType"='null' OR  "ParentType"='';
delete from "LookUp"  where "Type"='' and "Status"<>'A';
