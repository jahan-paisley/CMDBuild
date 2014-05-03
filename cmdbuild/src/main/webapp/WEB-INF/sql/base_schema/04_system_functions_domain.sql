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


CREATE OR REPLACE FUNCTION _cm_create_domain_triggers(DomainId oid) RETURNS void AS $$
BEGIN
	PERFORM _cm_add_domain_sanity_check_trigger(DomainId);
	PERFORM _cm_add_domain_history_trigger(DomainId);
END;
$$ LANGUAGE plpgsql VOLATILE;


/**************************************************************************
 *                                                                        *
 * Public functions                                                       *
 *                                                                        *
 **************************************************************************/


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


CREATE OR REPLACE FUNCTION cm_modify_domain(DomainId oid, NewComment text) RETURNS void AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	IF _cm_read_domain_cardinality(OldComment) <> _cm_read_domain_cardinality(NewComment)
		OR _cm_read_comment(OldComment, 'CLASS1') <> _cm_read_comment(NewComment, 'CLASS1')
		OR _cm_read_comment(OldComment, 'CLASS2') <> _cm_read_comment(NewComment, 'CLASS2')
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Check that the cardinality does not change
	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass || ' IS '|| quote_literal(NewComment);
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION cm_delete_domain(DomainId oid) RETURNS void AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$ LANGUAGE PLPGSQL;


/**************************************************************************
 *                                                                        *
 * Alternate function signatures                                          *
 *                                                                        *
 **************************************************************************/


CREATE OR REPLACE FUNCTION cm_modify_domain(CMDomain text, DomainComment text) RETURNS void AS $$
	SELECT cm_modify_domain(_cm_domain_id($1), $2);
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION cm_delete_domain(CMDomain text) RETURNS void AS $$
	SELECT cm_delete_domain(_cm_domain_id($1));
$$ LANGUAGE SQL;


/**************************************************************************
 *                                                                        *
 * Legacy public functions (they add checks for redundant parameters)     *
 *                                                                        *
 **************************************************************************/

CREATE OR REPLACE FUNCTION system_domain_create(
	CMDomain text,
	DomainClass1 text,
	DomainClass2 text,
	DomainComment text
) RETURNS integer AS $$
DECLARE
	TableName text := _cm_domain_cmname(CMDomain);
	HistoryTableName text := _cm_history_cmname(TableName);
    DomainId oid;
BEGIN
	-- TODO: Check DomainClass1 and DomainClass2

	RETURN cm_create_domain(CMDomain, DomainComment);
END
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION system_domain_modify(
	DomainId oid,
	DomainName text,
	DomainClass1 text,
	DomainClass2 text,
	NewComment text
) RETURNS boolean AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	-- TODO: Check DomainName, DomainClass1 and DomainClass2
	IF _cm_domain_id(DomainName) <> DomainId
		OR _cm_read_comment(NewComment, 'CLASS1') <> DomainClass1
		OR _cm_read_comment(NewComment, 'CLASS2') <> DomainClass2
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_domain(DomainId, NewComment);

	RETURN TRUE;
END;
$$ LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION system_domain_delete(CMDomain text) RETURNS void AS $$
	SELECT cm_delete_domain($1);
$$ LANGUAGE SQL;
