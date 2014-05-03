-- Add Configuration column for Cloud Administrator

CREATE OR REPLACE FUNCTION patch_203_04() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class_attribute('Role', 'CloudAdmin', 'boolean', 'FALSE', TRUE, FALSE, 'MODE: reserved');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_203_04();

DROP FUNCTION patch_203_04();