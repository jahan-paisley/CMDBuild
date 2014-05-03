-- Fix widget Calendar attribute name

CREATE OR REPLACE FUNCTION patch_216_01() RETURNS void AS $$
DECLARE
	oldWidget varchar;
	newWidget varchar;
	row record;
BEGIN
	FOR row IN
	SELECT *
	FROM "_Widget"
	WHERE "Description" = '.Calendar' AND "Status" = 'A'
	LOOP
		oldWidget = row."Definition";
		newWidget = regexp_replace(oldWidget, '"targetClass":', '"eventClass":');
		RAISE INFO '%', newWidget;

		UPDATE "_Widget"
		SET "Definition" = newWidget
		WHERE "Id" = row."Id";

	END LOOP;

	RETURN;
END;
$$ LANGUAGE PLPGSQL;

SELECT patch_216_01();
DROP FUNCTION patch_216_01();