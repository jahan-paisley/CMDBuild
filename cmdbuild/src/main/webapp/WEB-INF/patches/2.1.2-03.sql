-- Increasing Widgets' definition attribute size

CREATE OR REPLACE FUNCTION patch_212_03() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'creating new attribute';
	PERFORM cm_create_class_attribute('_Widget', 'Definition', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

	RAISE INFO 'moving data';
	ALTER TABLE "_Widget" DISABLE TRIGGER USER;
	UPDATE "_Widget" SET
		-- extracts "type" attribute's value from actual JSON 
		"Description" = trim(replace(split_part(unnest(regexp_matches("Description", '\"type\".*:.*\".*\"')), ':', 2), '"', '')),
		"Definition" = "Description"
		WHERE "Status" = 'A';
	ALTER TABLE "_Widget" ENABLE TRIGGER USER;
END

$$ LANGUAGE PLPGSQL;

SELECT patch_212_03();

DROP FUNCTION patch_212_03();