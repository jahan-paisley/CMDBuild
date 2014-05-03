-- Update Email table to handle notify templates

CREATE OR REPLACE FUNCTION patch_214_03() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class_attribute('Email', 'NotifyWith', 'text', null, false, false, 'MODE: write|DESCR: NotifyWith|INDEX: 10|BASEDSP: false|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_214_03();

DROP FUNCTION patch_214_03();