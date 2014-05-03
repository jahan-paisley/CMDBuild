-- Add table to store the GIS layers configuration

CREATE OR REPLACE FUNCTION _fill_Layer_Table() RETURNS VOID AS $$
BEGIN
	INSERT INTO "_Layer"("User", "Description", "FullName", "Index", "MinimumZoom",
		"MaximumZoom", "MapStyle", "Name", "GeoServerName", "Type", "Visibility", "CardsBinding")
	SELECT DISTINCT 'admin' AS "User", split_part("Code", '_', 3) AS "Description", "Code" AS "FullName",
        (SELECT MAX("Notes")::integer FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Description" = 'system.gis.index' AND meta."Status" = 'A') AS "Index",
        (SELECT MAX("Notes")::integer FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Description" = 'system.gis.minzoom' AND meta."Status" = 'A') AS "Minimumzoom",
        (SELECT MAX("Notes")::integer FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Description" = 'system.gis.maxzoom' AND meta."Status" = 'A') AS "Maximumzoom",
        (SELECT MAX("Notes") FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Description" = 'system.gis.style' and meta."Status" = 'A') AS "Mapstyle",
        (SELECT _cm_read_comment(_cm_comment_for_table_id((split_part(max(meta."Code"), '.', 1) || '."' || split_part(max(meta."Code"), '.', 2) || '"')::regclass::oid), 'DESCR'::text) FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Status" = 'A') AS "Name",
    	null AS "GeoserverName",
        (SELECT _cm_get_geometry_type((split_part(max(meta."Code"), '.', 1) || '."' || split_part(max(meta."Code"), '.', 2) || '"')::regclass::oid, 'Geometry') FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Status" = 'A') AS "Type",
        (SELECT MAX("Notes") FROM "Metadata" meta WHERE meta."Code" = "Metadata"."Code" AND meta."Description" = 'system.gis.visibility' AND meta."Status" = 'A') AS "Visibility",
    	null AS "CardsBinding"
	FROM "Metadata"
	WHERE "Code" LIKE 'gis.%' AND "Status" = 'A'
	GROUP BY "Code";
END 
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION patch_203_02() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class('_Layer', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
	PERFORM cm_create_class_attribute('_Layer', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'FullName', 'character varying', NULL, FALSE, TRUE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Index', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'MinimumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'MaximumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'MapStyle', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Name', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'GeoServerName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Visibility', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'CardsBinding', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

	BEGIN
		PERFORM _fill_Layer_Table();
	EXCEPTION 
		WHEN invalid_schema_name THEN
	END;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_203_02();

DROP FUNCTION patch_203_02();
DROP FUNCTION _fill_Layer_Table();