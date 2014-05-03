-- Fixes duplicate update when modifying a relation associated to a reference

CREATE OR REPLACE FUNCTION _cm_update_relation(
	UserName text,
	DomainId oid,
	CardIdColumn text,
	CardId integer,
	RefIdColumn text,
	RefId integer
) RETURNS void AS $$
DECLARE
	RefClassUpdatePart text;
BEGIN
	-- Needed to update IdClassX (if the domain attributres are IdClass1/2)
	RefClassUpdatePart := coalesce(
		', ' || quote_ident('IdClass'||substring(RefIdColumn from E'^IdObj(\\d)+')) || 
			'=' || _cm_dest_reference_classid(DomainId, RefIdColumn, RefId),
		''
	);

-- coalesce(quote_literal(UserName),'NULL') -> quote_nullable(UserName) -- pg84
	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET ' || quote_ident(RefIdColumn) || ' = ' || RefId ||
			', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
			RefClassUpdatePart ||
		' WHERE "Status"=''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId ||
			' AND ' || quote_ident(RefIdColumn) || ' <> ' || RefId;
END;
$$ LANGUAGE plpgsql;
