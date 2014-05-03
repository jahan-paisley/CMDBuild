--
-- Stored-procedure called by the BIM features.
--
CREATE OR REPLACE FUNCTION _bim_carddata_from_globalid(IN globalid varchar, OUT "Id" integer, OUT "IdClass" integer, OUT "Description" varchar, OUT "ClassName" varchar)
  RETURNS record AS
$BODY$
DECLARE
	query varchar;
	table_name varchar;
	tables CURSOR FOR SELECT tablename FROM pg_tables WHERE schemaname = 'bim' ORDER BY tablename;
	
BEGIN
	query='';
	FOR table_record IN tables LOOP
		query= query || '
		SELECT	b."Master" as "Id" , 
			p."Description" AS "Description", 
			p."IdClass"::integer as "IdClass" ,
			p."IdClass" as "ClassName"
		FROM bim."' || table_record.tablename || '" AS b 
			JOIN public."' ||  table_record.tablename || '" AS p 
			ON b."Master"=p."Id" 
		WHERE p."Status"=''A'' AND b."GlobalId" = ''' || globalid || ''' UNION ALL';
	END LOOP;

	SELECT substring(query from 0 for LENGTH(query)-9) INTO query;
	RAISE NOTICE 'execute query : %', query;
	EXECUTE(query) INTO "Id","Description","IdClass","ClassName";
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_carddata_from_globalid(character varying) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION cm_attribute_exists(IN schemaname text, IN tablename text, IN attributename text, OUT attribute_exists boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	attribute_name varchar;
BEGIN
	SELECT attname into attribute_name
	FROM pg_attribute 
	WHERE 	attrelid = (SELECT oid FROM pg_class WHERE relname = tablename AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname=schemaname)) AND
		attname = attributename;

	IF(attribute_name is not null) THEN
		attribute_exists = true;
	ELSE
		attribute_exists = false;
	END IF;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION cm_attribute_exists(text, text, text) IS 'TYPE: function';

-- Function: _bim_set_coordinates(character varying, character varying, character varying)

-- DROP FUNCTION _bim_set_coordinates(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION _bim_set_coordinates(IN globalid character varying, IN classname character varying, IN coords character varying, OUT success boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	query varchar;
BEGIN	

	query = 
	' UPDATE bim."' || classname || '"' ||--
	' SET "Position"= ST_GeomFromText(''' || coords || ''')' || --
	' WHERE "GlobalId"= ''' || globalid || '''';
			
	RAISE NOTICE '%',query;

	EXECUTE(query);

	success = true;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_set_coordinates(character varying, character varying, character varying) IS 'TYPE: function|CATEGORIES: system';

-- Function: _bim_set_room_geometry(character varying, character varying, character varying, character varying)

-- DROP FUNCTION _bim_set_room_geometry(character varying, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION _bim_set_room_geometry(IN globalid character varying, IN classname character varying, IN perimeter character varying, IN height character varying, OUT success boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	query varchar;
BEGIN	

	query = 
	' UPDATE bim."' || classname || '"' ||--
	' SET "Perimeter"= ST_GeomFromText(''' || perimeter || '''), "Height"=' || height ||--
	' WHERE "GlobalId"= ''' || globalid || '''';
			
	RAISE NOTICE '%',query;

	EXECUTE(query);

	success = true;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_set_room_geometry(character varying, character varying, character varying, character varying) IS 'TYPE: function|CATEGORIES: system';


-- Function: _bim_store_data(integer, character varying, character varying, character varying, character varying, character varying)

-- DROP FUNCTION _bim_store_data(integer, character varying, character varying, character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION _bim_store_data(IN cardid integer, IN classname character varying, IN globalid character varying, IN x character varying, IN y character varying, IN z character varying, OUT success boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	query varchar;
	query1 varchar;
	myrecord record;
BEGIN	
	query1 = 'DELETE FROM bim."' || classname || '" where "GlobalId"=''' || globalid || ''';';
	RAISE NOTICE '%',query1;
	EXECUTE(query1);
	
	query = '
		INSERT INTO bim."' || classname || '" ("GlobalId", "Position", "Master")
		VALUES (''' || globalid || ''',' || 'ST_GeomFromText(''POINT(' || x || ' ' || y || ' ' || z || ')''),' || cardid || ');';	
	RAISE NOTICE '%',query;
	EXECUTE(query);
	
	success = true;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_store_data(integer, character varying, character varying, character varying, character varying, character varying) IS 'TYPE: function|CATEGORIES: system';


CREATE OR REPLACE FUNCTION _bim_create_function_for_export(OUT success boolean)
RETURNS boolean AS
$BODY$
DECLARE
	query text;
BEGIN
	query = 'CREATE OR REPLACE FUNCTION _bim_data_for_export(IN id integer, IN "className" character varying, IN "containerAttributeName" character varying, 
		IN "containerClassName" character varying, OUT "Code" character varying, OUT "Description" character varying, OUT "GlobalId" character varying, 
		OUT container_id integer, OUT container_globalid character varying, OUT x character varying, OUT y character varying, OUT z character varying)
		RETURNS record AS
		\$BODY\$
		DECLARE
			query varchar;
			myrecord record;
			objectposition geometry;
			roomperimeter geometry;
			isinside boolean;
		BEGIN	
			query = 
				''SELECT bimclass."Position" '' || -- 
				''FROM bim."'' || "className" || ''" AS bimclass '' || --
				''WHERE "Master"= '' || id || '';'' ;
			
		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO objectposition;


		query = 
			''SELECT "'' || "containerAttributeName" || ''" '' || --
			''FROM "'' || "className" || ''" ''--
			''WHERE "Id"='' || id || '';'';

		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO container_id;

		query = 
			''SELECT "GlobalId"'' || '' '' || --
			''FROM bim."'' || "containerClassName" || ''" ''--
			''WHERE "Master"='' || coalesce(container_id,-1) || '';'';

		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO container_globalid;
		
		
		query = ''SELECT bimclass."Perimeter" '' || -- 
			''FROM bim."'' || "containerClassName" || ''" AS bimclass '' || --
			''WHERE "Master"= '' || coalesce(container_id,-1) || '';'' ;

		RAISE NOTICE ''%'',query;
		EXECUTE(query) INTO roomperimeter;

		isinside = ST_Within(objectposition,roomperimeter);
		RAISE NOTICE ''ok? %'',isinside;
		IF(NOT isinside) THEN
			query = 
				''UPDATE bim."'' || "className" || ''" ''--
				''SET "Position" = null '' || --
				''WHERE "Master"= '' || id || '';'' ;

			RAISE NOTICE ''%'',query;

			EXECUTE(query);
		END IF;

		query = 
			''SELECT master."Code", master."Description", bimclass."GlobalId", st_x(bimclass."Position"),st_y(bimclass."Position"),st_z(bimclass."Position") '' || --
			''FROM "'' || "className" || ''" AS master LEFT JOIN bim."'' || "className" || ''" AS bimclass ON '' || '' bimclass."Master"=master."Id" '' || --
			''WHERE master."Id" = '' || id || '' AND master."Status"=''''A'''''';

		RAISE NOTICE ''%'',query;

		EXECUTE(query) INTO "Code", "Description", "GlobalId", x, y, z;
		END;
		\$BODY\$
		  LANGUAGE plpgsql VOLATILE
	  	COST 100;

	  	COMMENT ON FUNCTION _bim_data_for_export(integer, character varying, character varying, character varying) IS ''TYPE: function|CATEGORIES: system'';
		';

	EXECUTE query;
	success = true;

	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  COMMENT ON FUNCTION _bim_create_function_for_export() IS 'TYPE: function|CATEGORIES: system';

  
  
CREATE OR REPLACE FUNCTION _bim_update_coordinates(IN classname character varying, IN globalid character varying, IN x character varying, IN y character varying, IN z character varying, OUT success boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	query varchar;
	query1 varchar;
	myrecord record;
BEGIN	
	query = 
		'UPDATE bim."' || classname || '" ' || --
		'SET "Position" = ST_GeomFromText(''POINT(' || x || ' ' || y || ' ' || z || ')'') ' || --
		'WHERE "GlobalId"=''' || globalid || ''';';	
	RAISE NOTICE '%',query;
	EXECUTE(query);
	
	success = true;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_update_coordinates(character varying, character varying, character varying, character varying, character varying) IS 'TYPE: function|CATEGORIES: system';

