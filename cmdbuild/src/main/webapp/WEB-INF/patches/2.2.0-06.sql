-- Fix e-mail template table

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
BEGIN
	RAISE DEBUG 'disabling triggers';
	ALTER TABLE "_EmailTemplate" DISABLE TRIGGER ALL;
	
	RAISE DEBUG 'clearing data';
	UPDATE "_EmailTemplate" SET "Owner" = NULL;

	RAISE DEBUG 'enabling triggers';
	ALTER TABLE "_EmailTemplate" ENABLE TRIGGER ALL;

	RAISE DEBUG 'removing attribute';
	PERFORM cm_delete_attribute('"_EmailTemplate"'::regclass::oid, 'Owner');
END;
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION IF EXISTS apply_patch();
