-- Update domains mode

CREATE OR REPLACE FUNCTION cm_create_domain(CMDomain text, DomainComment text) RETURNS integer AS $$
DECLARE
	DomainId oid;
	HistoryDBName text := _cm_history_dbname_unsafe(_cm_domain_cmname(CMDomain));
BEGIN
	-- TODO: Add Creation of Map (from its name)
	EXECUTE 'CREATE TABLE '|| _cm_domain_dbname_unsafe(CMDomain) ||
		' (CONSTRAINT '|| quote_ident(_cm_domainpk_name(CMDomain)) ||
		' PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate"))'||
		' INHERITS ("Map")';

	DomainId := _cm_domain_id(CMDomain);

	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass ||' IS '|| quote_literal(DomainComment);
	PERFORM _cm_copy_superclass_attribute_comments(DomainId, '"Map"'::regclass);

	EXECUTE 'CREATE TABLE '|| HistoryDBName ||
		' ( CONSTRAINT '|| quote_ident(_cm_historypk_name(_cm_domain_cmname(CMDomain))) ||
		' PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate"))'||
		' INHERITS ('|| DomainId::regclass ||')';
	EXECUTE 'ALTER TABLE '|| HistoryDBName ||' ALTER COLUMN "EndDate" SET DEFAULT now()';

	PERFORM _cm_create_domain_indexes(DomainId);

	PERFORM _cm_create_domain_triggers(DomainId);

	RETURN DomainId;
END
$$ LANGUAGE PLPGSQL;



CREATE OR REPLACE FUNCTION patch_update_domain_mode(DomainId oid, NewMode text) RETURNS VOID AS $$
DECLARE
	OldDomainComment text := _cm_comment_for_table_id(DomainId);
	NewDomainComment text;
BEGIN
	NewDomainComment := regexp_replace(OldDomainComment, E'(^|\\|)(MODE:[ ]*)([^ \\|]+)', E'\\1\\2'||NewMode);
	RAISE DEBUG '... % -> %', OldDomainComment, NewDomainComment;
	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass ||' IS '|| quote_literal(NewDomainComment);
	PERFORM _cm_copy_superclass_attribute_comments(DomainId, '"Map"'::regclass);
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION patch_131_01() RETURNS VOID AS $$
DECLARE
	DomainId oid;
	NewMode text;
BEGIN
	RAISE INFO 'Updating domains mode';

	FOR DomainId IN SELECT _cm_domain_list() LOOP
		SELECT INTO NewMode CASE DomainId
			WHEN '"Map"'::regclass THEN 'baseclass'
			WHEN '"Map_ActivityEmail"'::regclass THEN 'reserved'
			WHEN '"Map_UserRole"'::regclass THEN 'reserved'
			ELSE 'write'
			END;
		RAISE INFO '... % %', DomainId::regclass, UPPER(NewMode);
		PERFORM patch_update_domain_mode(DomainId, NewMode);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT patch_131_01();

DROP FUNCTION patch_update_domain_mode(DomainId oid, NewMode text);
DROP FUNCTION patch_131_01();
