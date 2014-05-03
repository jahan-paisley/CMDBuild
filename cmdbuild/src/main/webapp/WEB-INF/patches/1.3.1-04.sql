-- Set BeginDate on simple classes

CREATE OR REPLACE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='DELETE') THEN
		-- RETURN NEW would return NULL forbidding the operation
		RETURN OLD;
	ELSE
		NEW."BeginDate" = now();
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;