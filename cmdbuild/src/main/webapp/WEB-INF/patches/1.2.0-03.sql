-- Fix log in function system_reference_inserted

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
		raise notice 'Inserted reference % with a previous relation with idobj %', curvalue, cfrId;
		IF(curvalue <> cfrId) 
		THEN
			-- the relation has two different id (this is a problem... but we will try to update to fix it...)
			raise notice 'Updating relation setting reference from % to % ', curvalue, cfrId;
			EXECUTE 'UPDATE "Map_'||domainName||'" SET "'||curValueName||'"='||cfrId||' WHERE "'||curValueName||'"='||curvalue||' AND "'||nSideName||'"='||nSideId||' AND "Status"=''A'';';
		END IF;
	END IF;
	RETURN true;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
