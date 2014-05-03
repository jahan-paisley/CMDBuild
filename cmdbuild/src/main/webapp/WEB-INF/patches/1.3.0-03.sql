-- Fix non unique index for N:N domains created after 1.3.0

CREATE OR REPLACE FUNCTION cm_delete_domain(DomainId oid) RETURNS void AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'Cannot delete domain %, contains data', DomainId::regclass;
	END IF;

	PERFORM _cm_delete_local_attributes(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_create_domain_indexes(DomainId oid) RETURNS VOID AS $$
DECLARE
    Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	PERFORM _cm_create_index(DomainId, 'IdDomain');
	PERFORM _cm_create_index(DomainId, 'IdObj1');
	PERFORM _cm_create_index(DomainId, 'IdObj2');

	EXECUTE 'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId, 'ActiveRows')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ('||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdDomain" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass2" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj2" END)'||
		')';

	IF substring(Cardinality, 3, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueLeft')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass1" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj1" ELSE NULL END)'||
		' )';
	END IF;

	IF substring(Cardinality, 1, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueRight')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass2" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj2" ELSE NULL END)'||
		' )';
	END IF;
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION patch_130_03() RETURNS VOID AS $$
DECLARE
    DomainId oid;
    IndexName text;
BEGIN
	RAISE INFO 'Recreating domain ActiveRows index';

	FOR DomainId IN SELECT _cm_domain_list() LOOP
		IF (DomainId = '"Map"'::regclass) THEN
			RAISE INFO '... % SKIPPED', DomainId::regclass;
		ELSE
			IndexName := _cm_domainidx_name(DomainId, 'ActiveRows');
			RAISE INFO '... %', DomainId::regclass;
			EXECUTE 'DROP INDEX ' || quote_ident(IndexName);
			EXECUTE 'CREATE UNIQUE INDEX ' || quote_ident(IndexName) ||
				' ON ' || DomainId::regclass ||
				' USING btree ('||
					'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdDomain" END),'||
					'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass1" END),'||
					'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj1" END),'||
					'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass2" END),'||
					'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj2" END)'||
				')';
		END IF;
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_130_03();

DROP FUNCTION patch_130_03();
