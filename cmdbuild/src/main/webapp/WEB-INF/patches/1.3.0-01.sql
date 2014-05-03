-- Fix reference update when creating the relation

CREATE OR REPLACE FUNCTION _cm_update_reference(TableId oid, AttributeName text, CardId integer, ReferenceId integer) RETURNS void AS $$
BEGIN
	EXECUTE 'UPDATE ' || TableId::regclass ||
		' SET ' || quote_ident(AttributeName) || ' = ' || coalesce(ReferenceId::text, 'NULL') ||
		' WHERE "Status"=''A'' AND "Id" = ' || CardId::text ||
		' AND coalesce(' || quote_ident(AttributeName) || ', 0) <> ' || coalesce(ReferenceId, 0)::text;
END;
$$ LANGUAGE PLPGSQL;

--
-- Update wrong references
--

CREATE OR REPLACE FUNCTION patch_130_01() RETURNS VOID AS $$
DECLARE
	TableId oid;
	AttributeName name;

	DomainId oid;
	DomainSourceIdAttribute text;
	DomainTargetIdAttribute text;

	RefId int;
	CardId int;

	Date12303 timestamp;
	UpdatedRows integer;
	UpdatedRowsForReference integer;
BEGIN
	SELECT INTO Date12303 "BeginDate" FROM "Patch" WHERE "Code" = '1.2.3-03';
	RAISE INFO 'Fixing reference values after %', Date12303;

	FOR TableId, AttributeName IN
		SELECT attrelid, attname
		FROM pg_attribute
		JOIN pg_class ON attrelid = pg_class.oid
		JOIN pg_description ON pg_class.oid = objoid AND objsubid = 0
		WHERE attnum > 0 AND atttypid > 0 AND pg_attribute.attinhcount = 0
			AND pg_class.relkind <> 'v' AND _cm_is_any_class_comment(description)
	LOOP
		DomainId := _cm_get_reference_domain_id(TableId, AttributeName);
		IF (DomainId IS NOT NULL) THEN -- is it a reference?
			RAISE INFO '... updating reference %.%', TableId::regclass, quote_ident(AttributeName);

			DomainSourceIdAttribute := _cm_get_ref_source_id_domain_attribute(TableId, AttributeName);
			DomainTargetIdAttribute := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);
			UpdatedRowsForReference := 0;
	
			FOR CardId, RefId IN EXECUTE
				'SELECT '|| quote_ident(DomainSourceIdAttribute) ||','|| quote_ident(DomainTargetIdAttribute) ||
				' FROM '|| DomainId::regclass ||
				' WHERE "BeginDate" > '|| quote_literal(Date12303) ||' AND "Status"=''A'''
			LOOP
				EXECUTE 'UPDATE '|| TableId::regclass ||
					' SET '|| quote_ident(AttributeName) ||'='|| RefId ||
					' WHERE "Id"='|| CardId ||
					' AND COALESCE('|| quote_ident(AttributeName) ||',0) <> '|| RefId;
				GET DIAGNOSTICS UpdatedRows = ROW_COUNT;
				UpdatedRowsForReference := UpdatedRowsForReference + UpdatedRows;
			END LOOP;
			RAISE INFO '... fixed % rows', UpdatedRowsForReference;
		END IF;
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_130_01();

DROP FUNCTION patch_130_01();
