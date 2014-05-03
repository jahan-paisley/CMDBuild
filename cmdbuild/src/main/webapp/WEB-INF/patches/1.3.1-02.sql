-- Force Class Id on insert and minor function changes

CREATE OR REPLACE FUNCTION _cm_trigger_sanity_check() RETURNS trigger AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		IF (NEW."Status"='N' AND OLD."Status"='N') THEN -- Deletion of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='INSERT') THEN
		IF (NEW."Status" IS NULL) THEN
			NEW."Status"='A';
		ELSIF (NEW."Status"='N') THEN -- Creation of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		NEW."Id" = _cm_new_card_id();
		-- Class ID is needed because of the history tables
		BEGIN
			NEW."IdClass" = TG_RELID;
		EXCEPTION WHEN undefined_column THEN
			NEW."IdDomain" = TG_RELID;
		END;
	ELSE -- TG_OP='DELETE'
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- 'U' is reserved for history tables only
	IF (position(NEW."Status" IN 'AND') = 0) THEN -- Invalid status
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	NEW."BeginDate" = now();
	RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_parent_id(TableId oid) RETURNS SETOF oid AS $$
	SELECT COALESCE((SELECT inhparent FROM pg_inherits WHERE inhrelid = $1 AND _cm_is_cmobject(inhparent) LIMIT 1), NULL);
$$ LANGUAGE SQL;
