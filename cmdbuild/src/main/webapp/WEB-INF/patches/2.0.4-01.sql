-- Set inconsistent processes to closed aborted

CREATE OR REPLACE FUNCTION patch_204_01() RETURNS void AS $$
DECLARE
	query text;
	currentClass regclass;
BEGIN
	FOR currentClass IN SELECT _cm_subtables_and_itself('"Activity"'::regclass) LOOP
	IF (NOT (_cm_is_superclass(currentClass))) THEN
		-- disable trigger
		query = 'alter table ' || currentClass::regclass || ' disable trigger user;';
		raise notice '%', query;
		execute query;

		-- set inconsistent processes to closed.aborted
		query = 'update ' || currentClass::regclass || ' set "FlowStatus" = (select "Id" from "LookUp" where "Type"=''FlowStatus'' and "Code"=''closed.aborted'') where "FlowStatus" is null and "Status"=''A'';';
		raise notice '%', query;
		execute query;

		-- enable trigger
		query = 'alter table ' || currentClass::regclass || ' enable trigger user;';
		raise notice '%', query;
		execute query;
	END IF;
END LOOP;
END;
$$ LANGUAGE PLPGSQL;

select patch_204_01();

DROP FUNCTION patch_204_01();
