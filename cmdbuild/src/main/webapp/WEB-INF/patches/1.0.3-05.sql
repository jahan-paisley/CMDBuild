-- Domain cardinality constraints

DROP FUNCTION IF EXISTS system_domain_createindex(character varying);


CREATE OR REPLACE FUNCTION system_domain_createindex(character varying, text)
  RETURNS VOID AS
$BODY$
    DECLARE
        tableName ALIAS FOR $1;
        cardinality ALIAS FOR $2;
    BEGIN
	EXECUTE'
	CREATE INDEX idx_Map_'|| tableName ||'_iddomain
	ON "Map_'|| tableName ||'"
	USING btree
	("IdDomain"); ';

	EXECUTE'
	CREATE INDEX idx_Map_'|| tableName ||'_idobj1
	ON "Map_'|| tableName ||'"
	USING btree
	("IdObj1"); ';

	EXECUTE'
	CREATE INDEX idx_Map_'|| tableName ||'_idobj2
	ON "Map_'|| tableName ||'"
	USING btree
	("IdObj2"); ';

	EXECUTE 'CREATE UNIQUE INDEX idx_map_'|| tableName ||'_activerows
  				ON "Map_'|| tableName ||'"
  				USING btree
  			((
			CASE
    			WHEN "Status"::text = ''N''::text THEN NULL::regclass
    			ELSE "IdDomain"
			END), (
			CASE
    			WHEN "Status"::text = ''N''::text THEN NULL::regclass
    			ELSE "IdClass1"
			END), (
			CASE
    			WHEN "Status"::text = ''N''::text THEN NULL::integer
    			ELSE "IdObj1"
			END), (
			CASE
    			WHEN "Status"::text = ''N''::text THEN NULL::regclass
    			ELSE "IdClass2"
			END), (
			CASE
    			WHEN "Status"::text = ''N''::text THEN NULL::integer
    			ELSE "IdObj2"
			END), (
			CASE
    			WHEN "Status"::text = ''N''::text THEN NULL::text::bpchar
    			ELSE "Status"
			END));
	';

	IF substring(cardinality, 3, 1) = '1' THEN
		EXECUTE 'CREATE UNIQUE INDEX idx_map_'|| tableName ||'_uniqueleft
			  ON "Map_'|| tableName ||'"
			  USING btree
			  ((CASE
			    WHEN "Status"::text = ''A''::text
					THEN "IdClass1"
					ELSE NULL::regclass
			END), (CASE
			    WHEN "Status"::text = ''A''::text
					THEN "IdObj1"
					ELSE NULL::integer
			END));';
	END IF;

	IF substring(cardinality, 1, 1) = '1' THEN
		EXECUTE 'CREATE UNIQUE INDEX idx_map_'|| tableName ||'_uniqueright
			  ON "Map_'|| tableName ||'"
			  USING btree
			  ((CASE
			    WHEN "Status"::text = ''A''::text
					THEN "IdClass2"
					ELSE NULL::regclass
			END), (CASE
			    WHEN "Status"::text = ''A''::text
					THEN "IdObj2"
					ELSE NULL::integer
			END));';
	END IF;

    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_domain_create(character varying, character varying, character varying, character varying)
  RETURNS int4 AS
$BODY$
    DECLARE
    DomainName ALIAS FOR $1;
    DomainClass1 ALIAS FOR $2;
    DomainClass2 ALIAS FOR $3;
    DomainComment ALIAS FOR $4;

    field_list refcursor;
    domainid int4;

    BEGIN
	EXECUTE 'CREATE TABLE "Map_'|| DomainName ||'" (CONSTRAINT "Map_'|| DomainName ||'_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate")) INHERITS ("Map")';
	EXECUTE 'COMMENT ON TABLE "Map_'|| DomainName || '" IS '''||DomainComment||''' ;';
	EXECUTE 'CREATE TABLE "Map_'|| DomainName ||'_history" ("EndDate" timestamp NOT NULL DEFAULT now(), CONSTRAINT "Map_'|| DomainName || '_history_pkey" PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate")) INHERITS ("Map_' || DomainName ||'")';

	-- create index
	PERFORM system_domain_createindex(DomainName, substring(DomainComment, 'CARDIN: ([^|]*)'::text));

	-- create trigger
	PERFORM system_domain_createtriggers( DomainName );

	OPEN field_list FOR EXECUTE 'SELECT oid FROM pg_class WHERE relname = ''Map_'||DomainName||''';';
	FETCH field_list INTO domainid;
	CLOSE field_list;
	
	RETURN domainid;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE FUNCTION system_patch_domainindexes()
  RETURNS VOID AS
$BODY$
DECLARE
	domains RECORD;
BEGIN
  -- excludes fake view domains (was SELECT * FROM system_domaincatalog)
  FOR domains IN SELECT pg_class.oid AS domainid, "substring"(pg_class.relname::text, 5) AS domainname,
				substring(pg_description.description, 'CARDIN: ([^|]*)'::text) AS domaincardinality
			FROM pg_class
			LEFT JOIN pg_description pg_description ON pg_description.objoid = pg_class.oid AND pg_description.objsubid = 0
			WHERE strpos(pg_description.description, 'TYPE: domain'::text) > 0 AND pg_class.relkind<>'v'
  LOOP
	IF(domains.domainname<>'') THEN
		IF substring(domains.domaincardinality, 3, 1) = '1' THEN
			EXECUTE 'CREATE UNIQUE INDEX idx_map_'|| domains.domainname ||'_uniqueleft
				  ON "Map_'|| domains.domainname ||'"
				  USING btree
				  ((CASE
				    WHEN "Status"::text = ''A''::text
						THEN "IdClass1"
						ELSE NULL::regclass
				END), (CASE
				    WHEN "Status"::text = ''A''::text
						THEN "IdObj1"
						ELSE NULL::integer
				END));';
		END IF;

		IF substring(domains.domaincardinality, 1, 1) = '1' THEN
			EXECUTE 'CREATE UNIQUE INDEX idx_map_'|| domains.domainname ||'_uniqueright
				  ON "Map_'|| domains.domainname ||'"
				  USING btree
				  ((CASE
				    WHEN "Status"::text = ''A''::text
						THEN "IdClass2"
						ELSE NULL::regclass
				END), (CASE
				    WHEN "Status"::text = ''A''::text
						THEN "IdObj2"
						ELSE NULL::integer
				END));';
		END IF;
	END IF;
  END LOOP;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

SELECT system_patch_domainindexes();

DROP FUNCTION system_patch_domainindexes();