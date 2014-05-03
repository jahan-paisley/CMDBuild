-- Fixes reference values filling on attribute creation

CREATE OR REPLACE FUNCTION _cm_get_ref_source_class_domain_attribute(TableId oid, AttributeName text) RETURNS text AS $$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdClass1'
		WHEN FALSE THEN 'IdClass2'
		ELSE NULL
	END;
$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION _cm_add_reference_handling(TableId oid, AttributeName text) RETURNS VOID AS $$
DECLARE
	objid integer;
	referencedid integer;
	ctrlint integer;

	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
	ReferenceTargetId oid := _cm_read_reference_target_id_comment(AttributeComment);
	AttributeReferenceType text := _cm_read_reference_type_comment(AttributeComment);
	ReferenceDomainId oid := _cm_read_reference_domain_id_comment(AttributeComment);

	RefSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, AttributeName);
	RefSourceClassIdAttribute text := _cm_get_ref_source_class_domain_attribute(TableId, AttributeName);
	RefTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);

	ChildId oid;
BEGIN
	IF ReferenceTargetId IS NULL OR AttributeReferenceType IS NULL OR ReferenceDomainId IS NULL THEN
		RETURN;
	END IF;

	-- Updates the reference for every relation
	-- TODO: UNDERSTAND WHAT IT DOES AND MAKE IT READABLE!
	FOR objid IN EXECUTE 'SELECT "Id" from '||TableId::regclass||' WHERE "Status"=''A'''
	LOOP
		FOR referencedid IN EXECUTE '
			SELECT '|| quote_ident(RefTargetIdAttribute) ||
			' FROM '|| ReferenceDomainId::regclass ||
			' WHERE '|| quote_ident(RefSourceClassIdAttribute) ||'='|| TableId ||
				' AND '|| quote_ident(RefSourceIdAttribute) ||'='|| objid ||
				' AND "Status"=''A'''
		LOOP
			EXECUTE 'SELECT count(*) FROM '||ReferenceTargetId::regclass||' where "Id"='||referencedid INTO ctrlint;
			IF(ctrlint<>0) THEN
				EXECUTE 'UPDATE '|| TableId::regclass ||
					' SET '|| quote_ident(AttributeName) ||'='|| referencedid ||
					' WHERE "Id"='|| objid;
			END IF;
		END LOOP;
	END LOOP;

	-- Trigger on reference class (reference -> relation)
	FOR ChildId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		PERFORM _cm_add_update_relation_trigger(ChildId, TableId, AttributeName);
	END LOOP;

	-- Trigger on domain (relation -> reference)
	PERFORM _cm_add_update_reference_trigger(TableId, AttributeName);
END;
$$ LANGUAGE PLPGSQL VOLATILE;

DROP FUNCTION _cm_get_ref_target_class_domain_attribute(oid, text);
