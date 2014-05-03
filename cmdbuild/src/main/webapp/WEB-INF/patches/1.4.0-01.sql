-- Reorders tree nodes that were not properly ordered when saving them

CREATE OR REPLACE FUNCTION sort_navigationtree_recursive(idparent integer,idgroup integer)
 RETURNS void AS
$BODY$
DECLARE
 menulist RECORD;
 number integer;
BEGIN
 number=0;
 FOR menulist IN (SELECT * FROM "Menu" where "IdGroup" = idgroup and coalesce ("IdParent",0)=idparent and "Status"='A' ORDER BY "Id")  LOOP
  UPDATE "Menu" SET "Number" = number WHERE "Id" = menulist."Id";
  PERFORM sort_navigationtree_recursive(menulist."Id",idgroup);
  number=number+1;
 END LOOP;
END;
$BODY$
 LANGUAGE 'plpgsql' VOLATILE
 COST 100;

 CREATE OR REPLACE FUNCTION sort_navigationtree()
 RETURNS void AS
$BODY$
DECLARE
 rec RECORD;
 idgroup int4;
BEGIN
 FOR idgroup IN (SELECT DISTINCT ("IdGroup") FROM "Menu" WHERE "Status"='A' ORDER BY "IdGroup")  LOOP
  PERFORM sort_navigationtree_recursive(0,idgroup);
 END LOOP;
END;
$BODY$
 LANGUAGE 'plpgsql' VOLATILE
 COST 100;

select sort_navigationtree();

DROP FUNCTION sort_navigationtree_recursive( integer, integer);
DROP FUNCTION sort_navigationtree();