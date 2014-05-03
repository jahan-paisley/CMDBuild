-- Fixed issues related to backup schemas

DROP FUNCTION IF EXISTS system_disablealltriggers();
CREATE OR REPLACE FUNCTION system_disablealltriggers() RETURNS boolean AS $$
DECLARE
	tables RECORD;
	domains RECORD;
BEGIN
	FOR tables IN
		SELECT * FROM _cm_class_list() AS classid
	LOOP
		RAISE DEBUG 'disabling triggers for classname %s', tables.classid::regclass;
		IF (tables.classid IS NOT NULL) THEN
			EXECUTE 'ALTER TABLE '|| tables.classid::regclass || ' DISABLE TRIGGER USER';
		END IF;
	END LOOP;

	FOR domains IN
		SELECT * FROM _cm_domain_list() AS domainid
	LOOP
		RAISE DEBUG 'disabling triggers for domain %s', domains.domainid::regclass;
		IF (domains.domainid IS NOT NULL) THEN
			EXECUTE 'ALTER TABLE ' || domains.domainid::regclass || ' DISABLE TRIGGER USER';
		END IF;
	END LOOP;

	RETURN true;
END;
$$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS system_enablealltriggers();
CREATE OR REPLACE FUNCTION system_enablealltriggers() RETURNS boolean AS $$
DECLARE
	tables RECORD;
	domains RECORD;
BEGIN
	FOR tables IN
		SELECT * FROM _cm_class_list() AS classid
	LOOP
		RAISE DEBUG 'disabling triggers for classname %s', tables.classid::regclass;
		IF (tables.classid IS NOT NULL) THEN
			EXECUTE 'ALTER TABLE '|| tables.classid::regclass || ' ENABLE TRIGGER USER';
		END IF;
	END LOOP;

	FOR domains IN
		SELECT * FROM _cm_domain_list() AS domainid
	LOOP
		RAISE DEBUG 'disabling triggers for domain %s', domains.domainid::regclass;
      	IF (domains.domainid IS NOT NULL) THEN
			EXECUTE 'ALTER TABLE ' || domains.domainid::regclass || ' ENABLE TRIGGER USER';
		END IF;
	END LOOP;

	RETURN true;
END;
$$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
BEGIN
	SELECT system_disablealltriggers();
	UPDATE "Map" SET "IdClass1"='"Role"'::regclass WHERE "IdClass1"::text='backup_users_21."Role"';
	UPDATE "Map" SET "IdClass2"='"Role"'::regclass WHERE "IdClass2"::text='backup_users_21."Role"';
	UPDATE "Map" SET "IdClass1"='"User"'::regclass WHERE "IdClass1"::text='backup_users_21."User"';
	UPDATE "Map" SET "IdClass2"='"User"'::regclass WHERE "IdClass2"::text='backup_users_21."User"';
	UPDATE "Class" SET "IdClass"='"Role"'::regclass WHERE "IdClass"::text='backup_users_21."Role"';
	UPDATE "Class" SET "IdClass"='"User"'::regclass WHERE "IdClass"::text='backup_users_21."User"';
	UPDATE "Grant" SET "IdGrantedClass"='"User"'::regclass, "IdClass"='"Grant"'::regclass WHERE "IdGrantedClass"::text='backup_users_21."User"';
	UPDATE "Grant" SET "IdGrantedClass"='"Role"'::regclass, "IdClass"='"Grant"'::regclass WHERE "IdGrantedClass"::text='backup_users_21."Role"';
	UPDATE "Grant" SET "IdGrantedClass"='"LookUp"'::regclass, "IdClass"='"Grant"'::regclass WHERE "IdGrantedClass"::text='backup_lookup_21."LookUp"';
	SELECT system_enablealltriggers();
END;
$$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS system_disablealltriggers();
DROP FUNCTION IF EXISTS system_enablealltriggers();
DROP FUNCTION IF EXISTS apply_patch();

DROP SCHEMA IF EXISTS backup_for_2_1_2 CASCADE;
DROP SCHEMA IF EXISTS backup_lookup_21 CASCADE;
DROP SCHEMA IF EXISTS backup_users_21 CASCADE;
