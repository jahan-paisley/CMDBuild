-- Fix domain tables

DROP FUNCTION IF EXISTS patch_217_01();

CREATE OR REPLACE FUNCTION patch_217_01() RETURNS void AS $$
DECLARE
	domainId oid;
	notFixed int;
BEGIN
	RAISE NOTICE 'disabling triggers';
	FOR domainId IN (SELECT * FROM _cm_domain_list()) LOOP
		RAISE DEBUG 'disabling triggers for domain %s', domainId::regclass;
		EXECUTE 'ALTER TABLE '|| domainId::regclass ||' DISABLE TRIGGER USER';
	END LOOP;

	RAISE NOTICE 'patching data';
	UPDATE "Map"
		SET
			"IdClass1" = coalesce((SELECT c1."IdClass" FROM "Class" AS c1 WHERE c1."Id" = "IdObj1" LIMIT 1), "IdClass1"),
			"IdClass2" = coalesce((SELECT c2."IdClass" FROM "Class" AS c2 WHERE c2."Id" = "IdObj2" LIMIT 1), "IdClass2");

	RAISE NOTICE 'enabling triggers';
	FOR domainId IN (SELECT * FROM _cm_domain_list()) LOOP
		RAISE DEBUG 'enabling triggers for domain %s', domainId::regclass;
		EXECUTE 'ALTER TABLE '|| domainId::regclass ||' ENABLE TRIGGER USER';
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_217_01();

DROP FUNCTION IF EXISTS patch_217_01();
