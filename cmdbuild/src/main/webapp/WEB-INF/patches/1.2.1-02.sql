-- Bugfix restrict trigger on superclasses


CREATE OR REPLACE FUNCTION system_class_createreferencetrigger(
		ClassName character varying,
		ParentClassName character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
	AttributeName varchar(100);
	AttributeReference varchar(100);
	AttributeReferenceType varchar(100);
	AttributeReferenceDomain varchar(100);
	-- other
	field_list refcursor;
    BEGIN
	OPEN field_list FOR EXECUTE 'SELECT attributename,attributereference,attributereferencetype, attributereferencedomain FROM system_attributecatalog WHERE classname='''||ClassName||''' AND attributereference is not null AND attributereference <>'''';';
	LOOP
			FETCH field_list INTO AttributeName,AttributeReference,AttributeReferenceType,AttributeReferenceDomain;
			EXIT WHEN NOT FOUND;
			PERFORM system_reference_create(ClassName,AttributeName,AttributeReference,AttributeReferenceType,AttributeReferenceDomain);
	END LOOP;
	CLOSE field_list;
	RETURN true;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_createrestricttriggers(
		ClassName character varying,
		ParentClassName character varying)
  RETURNS VOID AS
$BODY$
    DECLARE
	AttributeName varchar(100);
	AttributeReference varchar(100);
	AttributeReferenceType varchar(100);
	AttributeReferenceDomain varchar(100);
	-- other
	triggername_list refcursor;
	triggername text;
    BEGIN
	OPEN triggername_list FOR EXECUTE 'SELECT tgname FROM pg_trigger WHERE tgrelid = ''"'||ParentClassName||'"''::regclass AND tgname SIMILAR TO ''(restrict|notnull|cascade)_%''';
	LOOP
			FETCH triggername_list INTO triggername;
			EXIT WHEN NOT FOUND;
			EXECUTE 'CREATE TRIGGER '||triggername||'
				AFTER UPDATE
				ON "'||ClassName||'"
				FOR EACH ROW
				EXECUTE PROCEDURE '||triggername||'()';
	END LOOP;
	CLOSE triggername_list;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_create(character varying, character varying, boolean, character varying)
  RETURNS int AS
$BODY$
    DECLARE
        ClassName ALIAS FOR $1;
        ParentClass ALIAS FOR $2;
        isSuperClass ALIAS FOR $3;
	ClassComment ALIAS FOR $4;
	
	field_list refcursor;
	classid int;
    BEGIN
    -- table
	EXECUTE 'CREATE TABLE "'|| ClassName ||'" (CONSTRAINT "'|| ClassName ||'_pkey" PRIMARY KEY ("Id")) INHERITS ("'|| ParentClass ||'");';
	-- comment
	EXECUTE 'COMMENT ON TABLE "'|| ClassName ||'" IS '''|| ClassComment ||''';';
	EXECUTE 'SELECT system_class_createattributecomment('''||ClassName||''','''||ParentClass||''');';
	-- index
	EXECUTE 'SELECT system_class_createindex('''||ClassName||''')';
	IF (isSuperClass=false) THEN
		-- history
		EXECUTE 'SELECT system_class_createhistory('''|| ClassName ||''');';
		-- triggers
		EXECUTE 'SELECT system_class_createtriggers('''|| ClassName ||''');';
	END IF;

	--if there is a superclass...
	IF (ParentClass<>'') THEN
	    -- propagate its triggers if any
		PERFORM system_class_createreferencetrigger(ClassName,ParentClass);
		PERFORM system_class_createrestricttriggers(ClassName,ParentClass);
		-- and if this isn't a superclass
		IF (isSuperClass=false) THEN
			-- create the reference fk (if any)
			--PERFORM system_class_createreferencefk(ClassName);
		END IF;
	END IF;
	
	OPEN field_list FOR EXECUTE 'SELECT oid FROM pg_class WHERE relname = '''||ClassName||''';';
	FETCH field_list INTO classid;
	CLOSE field_list;
	
	RETURN classid;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_createreferencetrigger(
		ClassName character varying,
		AttributeName character varying)
RETURNS VOID AS
$BODY$
BEGIN
	EXECUTE '
		CREATE OR REPLACE FUNCTION reference_'||ClassName||'_'||AttributeName||'()
 		 RETURNS TRIGGER AS
		$BODY1$
		DECLARE
			ReferenceDomain varchar(40);
			direct varchar(10);
			directb bool;
			domcard varchar(10);
			Reference varchar(40);
			ReferenceValue integer;
			OldReferenceValue integer;
			DomainId integer;
			-- other
			field_list refcursor;
			thisid integer; 
			otherid integer; 
			refdom varchar(50);
			rowinmap integer;

		BEGIN

			-- obtain necessary infos
			OPEN field_list FOR EXECUTE ''
				SELECT "classid",case when(system_read_comment("attributecomment", ''''REFERENCEDIRECT'''' ) ILIKE ''''true'''') then domainclass2 else domainclass1 end,system_read_comment("attributecomment", 
			  ''''REFERENCEDOM'''' ),system_read_comment("attributecomment", ''''REFERENCEDIRECT'''' )  FROM "system_attributecatalog" JOIN system_domaincatalog ON 
		  system_domaincatalog."domainname"=system_read_comment("attributecomment", ''''REFERENCEDOM'''') WHERE "classname"=''''''||TG_RELNAME||'''''' AND "attributename"='''''||AttributeName||''''';'';
				FETCH field_list INTO thisid,Reference,ReferenceDomain,direct;
			CLOSE field_list;
			refdom = ''''||ReferenceDomain||'''';
		
			OPEN field_list FOR EXECUTE ''
				SELECT "domainid",system_read_comment("domaincomment", ''''CARDIN''''::varchar) FROM "system_domaincatalog" WHERE "domainname"=''''''||refdom||'''''';'';
				FETCH field_list INTO DomainId,domcard;
			CLOSE field_list;


				IF (NEW."'||AttributeName||'" is null) 
				THEN otherid=null;
				ELSE
					OPEN field_list FOR EXECUTE ''
		  				SELECT "IdClass"::oid FROM "''||Reference||''" WHERE "Id"=''||NEW."'||AttributeName||'"||'';'';
		  				FETCH field_list INTO otherid;
		  			CLOSE field_list;
				END IF;

				IF(domcard=''1:N'')
				THEN directb=true;
				ELSE directb=false; END IF;

				-- delegate operations to other functions
				IF(TG_OP=''INSERT'') 
				THEN
					IF(directb) THEN
						PERFORM system_reference_inserted(otherid,NEW."'||AttributeName||'",thisid,NEW."Id",ReferenceDomain,DomainId,directb);
					ELSE
						PERFORM system_reference_inserted(thisid,NEW."Id",otherid,NEW."'||AttributeName||'",ReferenceDomain,DomainId,directb);
					END IF;
				ELSIF(TG_OP=''DELETE'') 
				THEN
					IF(directb) THEN
						PERFORM system_reference_deleted(otherid,OLD."'||AttributeName||'",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
					ELSE
						PERFORM system_reference_deleted(thisid,OLD."Id",otherid,OLD."'||AttributeName||'",ReferenceDomain,DomainId,directb);
					END IF;
				ELSIF(TG_OP=''UPDATE'') 
				THEN
					IF(directb) THEN
						IF(NEW."Status"=''A'' AND ( (NEW."'||AttributeName||'" IS NULL AND OLD."'||AttributeName||'" IS NOT NULL) OR (NEW."'||AttributeName||'" IS NOT NULL AND OLD."'||AttributeName||'" IS NULL) OR (NEW."'||AttributeName||'"<>OLD."'||AttributeName||'") ) ) THEN
							PERFORM system_reference_updated(otherid,NEW."'||AttributeName||'",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
						ELSIF(NEW."Status"=''N'') THEN
							PERFORM system_reference_logicdelete(otherid,NEW."'||AttributeName||'",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
						END IF;
					ELSE
						IF(NEW."Status"=''A'' AND ( (NEW."'||AttributeName||'" IS NULL AND OLD."'||AttributeName||'" IS NOT NULL) OR (NEW."'||AttributeName||'" IS NOT NULL AND OLD."'||AttributeName||'" IS NULL) OR (NEW."'||AttributeName||'"<>OLD."'||AttributeName||'") ) ) THEN
							PERFORM system_reference_updated(thisid,OLD."Id",otherid,NEW."'||AttributeName||'",ReferenceDomain,DomainId,directb);
						ELSIF(NEW."Status"=''N'') THEN
							PERFORM system_reference_logicdelete(thisid,OLD."Id",otherid,NEW."'||AttributeName||'",ReferenceDomain,DomainId,directb);
							END IF;
						END IF;
					END IF;
		  RETURN NEW;
		  END;
		  $BODY1$
  		  LANGUAGE ''plpgsql'' VOLATILE';

	  EXECUTE'
		CREATE TRIGGER reference_'||ClassName||'_'||AttributeName||'
		AFTER INSERT OR UPDATE OR DELETE
		ON "'||ClassName||'"
		FOR EACH ROW
		EXECUTE PROCEDURE reference_'||ClassName||'_'||AttributeName||'();';
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_createpkandtriggers(
		AttributeReferenceTypeOrEmpty character varying,
		ClassName character varying,
		AttributeName character varying,
		AttributeReference character varying,
		AttributeReferenceDomain character varying)
RETURNS VOID AS
$BODY$
DECLARE
	AttributeReferenceType varchar;
	hasfk boolean;
	issuper boolean;
	refclassissuper boolean;
	comment text;
BEGIN
	IF (AttributeReferenceTypeOrEmpty = '') THEN
		AttributeReferenceType := 'restrict';
	ELSE
		AttributeReferenceType := AttributeReferenceTypeOrEmpty;
	END IF;

	PERFORM system_reference_createreferencetrigger(ClassName,AttributeName);

	-- foreign key generation
	SELECT INTO hasfk system_class_hasreferencefk(ClassName,AttributeName);
	EXECUTE 'SELECT classcomment from system_classcatalog where classname = '''||ClassName||'''' INTO comment;
    SELECT INTO issuper system_class_issuperclass(comment);
    EXECUTE 'SELECT system_class_issuperclass(classcomment) from system_classcatalog where classname='''|| AttributeReference ||'''' INTO refclassissuper;

	IF (hasfk=false AND issuper=false AND refclassissuper=false) THEN
		PERFORM system_reference_createrelationpk(AttributeReferenceType,ClassName,AttributeName,AttributeReference);
	END IF;
	-- note: AttributeReference is the target class!!!!!!
	PERFORM system_reference_createrelationtriggersrecursive(AttributeReferenceType,ClassName,AttributeName,AttributeReference,AttributeReferenceDomain);
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_create(
		ClassName character varying,
		AttributeName character varying,
		AttributeReference character varying, -- target class
		AttributeReferenceTypeOrEmpty character varying,
		AttributeReferenceDomain character varying)
  RETURNS boolean AS
$BODY1$
    DECLARE
	field_list refcursor;
	field_list2 refcursor;
	field_list3 refcursor;
	objid integer;
	classid integer;
	referencedid integer;
	domcardinality varchar(10);
	selclid varchar(20);
	selobjid varchar(20);
	seloobj varchar(20);
	ctrlint integer;
    AttributeReferenceType varchar;
    BEGIN
	IF (AttributeReferenceTypeOrEmpty = '') THEN
		AttributeReferenceType := 'restrict';
	ELSE
		AttributeReferenceType := AttributeReferenceTypeOrEmpty;
	END IF;
	--which side of the domain i am in? selct IdClass1/2 and IdObj1/2?
	OPEN field_list FOR execute '
	SELECT system_read_comment("domaincomment", ''CARDIN''::varchar) FROM "system_domaincatalog" WHERE "domainname"='''||AttributeReferenceDomain||''';
	';
	FETCH field_list INTO domcardinality;
	CLOSE field_list;

	IF(domcardinality='1:N')
	THEN
		selclid='IdClass2';
		selobjid='IdObj2';
		seloobj='IdObj1';
	ELSEIF(domcardinality='N:1')
	THEN
		selclid='IdClass1';
		selobjid='IdObj1';
		seloobj='IdObj2';
	ELSE
		raise exception 'Reference creation for domains other than 1:N or N:1 is not supported';
	END IF;

	RAISE LOG 'create reference SP called...';

	OPEN field_list FOR execute '
	SELECT "classid" FROM "system_attributecatalog" WHERE  "classname"='''||ClassName||''' AND "attributename"='''||AttributeName||''';
	';
	FETCH field_list INTO classid;
	CLOSE field_list;

	-- Updates the reference for every relation
	OPEN field_list FOR execute 'SELECT "Id" from "'||ClassName||'" WHERE "Status"=''A''';
	LOOP
		FETCH field_list INTO objid;
		EXIT WHEN NOT FOUND;
		RAISE LOG '(%)',objid;
		OPEN field_list2 FOR execute '
		SELECT "'||seloobj||'" FROM "Map_'||AttributeReferenceDomain||'" WHERE "'||selclid||'"='||classid||' AND "'||selobjid||'"='||objid||' AND "Status"=''A'';
		';
		FETCH field_list2 INTO referencedid;
		CLOSE field_list2;
		IF FOUND THEN
			RAISE LOG 'found relation (%)',referencedid;
			OPEN field_list3 FOR execute 'select count(*) from "'||AttributeReference||'" where "Id"='||referencedid||';';
			FETCH field_list3 INTO ctrlint;
			CLOSE field_list3;
			IF(ctrlint<>0) THEN
				EXECUTE 'UPDATE "'||ClassName||'" SET "'||AttributeName||'"='||referencedid||' WHERE "Id"='||objid||'';
			END IF;
		END IF;
	END LOOP;
	CLOSE field_list;

	PERFORM system_reference_createpkandtriggers(AttributeReferenceType,ClassName,AttributeName,AttributeReference,AttributeReferenceDomain);

	RETURN true;
	END;
$BODY1$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_createrelationpk(
		AttributeReferenceType character varying,
		ClassName character varying,
		AttributeName character varying,
		AttributeReference character varying)
  RETURNS VOID AS
$BODY$
    DECLARE
	RestrictType text;

    BEGIN
	IF (AttributeReferenceType='setnull') THEN
		RestrictType := 'SET NULL';
	ELSEIF (AttributeReferenceType='cascade') THEN
		RestrictType := 'CASCADE';
	ELSE
		RestrictType := 'RESTRICT';
	END IF;
	EXECUTE '
		ALTER TABLE "'||ClassName||'"
		ADD CONSTRAINT "'||ClassName||'_'||AttributeName||'_fkey" FOREIGN KEY ("'||AttributeName||'")
		REFERENCES "'||AttributeReference||'" ("Id") MATCH SIMPLE
		ON DELETE '||RestrictType;
	END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_createrelationtriggersrecursive(
		ReferenceType character varying,
		ClassName character varying,
		AttributeName character varying,
		TargetClass character varying,
		ReferenceDomain character varying)
  RETURNS VOID AS
$BODY$
    DECLARE
	ChildName varchar(60);
	field_list refcursor;
    BEGIN
	-- update childs if there are
	OPEN field_list FOR EXECUTE 'SELECT child FROM system_treecatalog WHERE parent='''||TargetClass||'''';
	LOOP
		FETCH field_list INTO ChildName;
		EXIT WHEN NOT FOUND;
		PERFORM system_reference_createrelationtriggersrecursive(ReferenceType,ClassName,AttributeName,ChildName,ReferenceDomain);
	END LOOP;
	-- finally create in this class
	EXECUTE 'SELECT system_reference_create'||ReferenceType||'relationtriggers('''||ReferenceDomain||''','''||TargetClass||''','''||ClassName||''','''||AttributeName||''')';
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_createrestrictrelationtriggers(
	DomainName character varying,
	ReferencedClass character varying,
	ReferrerClass character varying,
	AttrName character varying)
  RETURNS boolean AS
$BODY1$
    BEGIN

	EXECUTE '
	CREATE OR REPLACE FUNCTION restrict_'||DomainName||'_'||ReferrerClass||'()
		RETURNS "trigger" AS
	$BODY$
	DECLARE
		ctrl int4;
	BEGIN
	IF( TG_OP=''UPDATE'' AND NEW."Status"=''N'' AND OLD."Status"=''A'') THEN
		SELECT INTO ctrl COUNT(*) FROM "'|| ReferrerClass ||'" WHERE "'||AttrName||'"=NEW."Id" AND "Status"=''A'';
		IF(ctrl <> 0) THEN
			RAISE EXCEPTION '''||ReferencedClass||' instance has relations on domain '||DomainName||' and is restricted'';
		END IF;
	END IF;
	RETURN NEW;
	END;
	$BODY$
	LANGUAGE plpgsql;
	';

	-- Father, please forgive me for I have sinned
	EXECUTE 'DROP TRIGGER IF EXISTS restrict_'||DomainName||'_'||ReferrerClass||' ON "'||ReferencedClass||'"';
	EXECUTE'
		CREATE TRIGGER restrict_'||DomainName||'_'||ReferrerClass||'
		AFTER UPDATE
		ON "'||ReferencedClass||'"
		FOR EACH ROW
		EXECUTE PROCEDURE restrict_'||DomainName||'_'||ReferrerClass||'();
	';

	RETURN true;
    END;
$BODY1$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_reference_createcascaderelationtriggers(
	DomainName character varying,
	ReferencedClass character varying,
	ReferrerClass character varying,
	AttrName character varying)
  RETURNS boolean AS
$BODY1$
    BEGIN
		
	EXECUTE '
	CREATE OR REPLACE FUNCTION cascade_'||DomainName||'_'||ReferrerClass||'()
		RETURNS "trigger" AS
	$BODY$
	DECLARE
	BEGIN
	IF( TG_OP=''UPDATE'' AND NEW."Status"=''N'' AND OLD."Status"=''A'') THEN
		UPDATE "'||ReferrerClass||'" SET "Status"=''N'' WHERE "'||AttrName||'"=NEW."Id";
	END IF;
	RETURN NEW;
	END;
	$BODY$
	LANGUAGE plpgsql;
	';
	
	EXECUTE'
		CREATE TRIGGER cascade_'||DomainName||'_'||ReferrerClass||'
		AFTER UPDATE
		ON "'||ReferencedClass||'"
		FOR EACH ROW
		EXECUTE PROCEDURE cascade_'||DomainName||'_'||ReferrerClass||'();
	';

	RETURN true;
    END;
$BODY1$
  LANGUAGE 'plpgsql' VOLATILE;
  
  
CREATE OR REPLACE FUNCTION system_reference_createsetnullrelationtriggers(
	DomainName character varying,
	ReferencedClass character varying,
	ReferrerClass character varying,
	AttrName character varying)
  RETURNS boolean AS
$BODY1$
    BEGIN
		
	EXECUTE '
	CREATE OR REPLACE FUNCTION setnull_'||DomainName||'_'||ReferrerClass||'()
		RETURNS "trigger" AS
	$BODY$
	DECLARE
	BEGIN
	IF( TG_OP=''UPDATE'' AND NEW."Status"=''N'' AND OLD."Status"=''A'') THEN
		UPDATE "'||ReferrerClass||'" SET "'||AttrName||'"=null WHERE "'||AttrName||'"=NEW."Id" AND "Status"=''A'';
	END IF;
	RETURN NEW;
	END;
	$BODY$
	LANGUAGE plpgsql;
	';
	
	EXECUTE'
		CREATE TRIGGER setnull_'||DomainName||'_'||ReferrerClass||'
		AFTER UPDATE
		ON "'||ReferencedClass||'"
		FOR EACH ROW
		EXECUTE PROCEDURE setnull_'||DomainName||'_'||ReferrerClass||'();
	';

	RETURN true;
    END;
$BODY1$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_deletereference(character varying, character varying)
  RETURNS boolean AS
$BODY$
    DECLARE
    ClassName ALIAS FOR $1;
	AttributeName ALIAS FOR $2;
	field_list refcursor;
	hastrigger bool; --has reference trigger?
	hasfk bool; -- has foreign key?
	reftype varchar(20); -- restriced or cascade
	referenced varchar(100); -- referenced class
	domname varchar(100); -- domain name
	domcard varchar(10); -- domain cardinality
	ctrl bool; -- true if there is a trigger on referenced class
	trgname text;

    BEGIN
	OPEN field_list FOR EXECUTE 'SELECT system_class_hasreferencetrigger('''|| ClassName ||''','''||AttributeName||''') ';
        FETCH field_list INTO hastrigger;
        CLOSE field_list;
	OPEN field_list FOR EXECUTE 'SELECT system_class_hasreferencefk('''|| ClassName ||''','''||AttributeName||''') ';
        FETCH field_list INTO hasfk;
        CLOSE field_list;

	raise notice 'Deleting reference %.% (has reference trigger:%) (has fk:%)',ClassName,AttributeName,hastrigger,hasfk;

	-- trigger
	IF (hastrigger) THEN
		EXECUTE 'DROP TRIGGER reference_'||ClassName||'_'||AttributeName||' ON "'||ClassName||'" CASCADE;';
	END IF;
	-- constraint
	IF (hasfk) THEN
		EXECUTE 'ALTER TABLE "'||ClassName||'" DROP CONSTRAINT "'||ClassName||'_'||AttributeName||'_fkey";';
	END IF;
	
	--if is a cascade reference, delete the trigger on refereced class too
	open field_list for execute '
		SELECT attributereferencetype,attributereference::varchar,attributereferencedomain FROM system_attributecatalog WHERE classname='''||ClassName||''' AND attributename='''||AttributeName||''';';
		fetch field_list INTO reftype,referenced,domname;
	close field_list;

	-- if the referenced class is not null
	if (referenced is not null) then
		trgname = reftype || '_' || LOWER(domname) || '_' || LOWER(ClassName);
		-- if the reference is restrict
		select into ctrl system_trigger_exists(trgname);
		--raise notice reftype || ' trigger exists? %',ctrl;
		-- if the reference trigger exist
		if (ctrl) then 
			PERFORM system_droptriggersrecursive(referenced, trgname);
		end if;
	end if;

	RETURN true;
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_droptriggersrecursive(
		ClassName character varying,
		TriggerName character varying)
  RETURNS VOID AS
$BODY$
    DECLARE
	ChildName varchar(60);
	field_list refcursor;
    BEGIN
	OPEN field_list FOR EXECUTE 'SELECT child FROM system_treecatalog WHERE parent='''||ClassName||'''';
	LOOP
		FETCH field_list INTO ChildName;
		EXIT WHEN NOT FOUND;
		PERFORM system_droptriggersrecursive(ChildName,TriggerName);
	END LOOP;
	CLOSE field_list;
	--raise notice 'deleting trigger %', TriggerName;
	EXECUTE 'DROP TRIGGER IF EXISTS '||TriggerName||' ON "'||ClassName||'" CASCADE';
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION system_class_deletetriggers(character varying)
  RETURNS void AS
$BODY$
    DECLARE
        ClassName ALIAS FOR $1;
        trigger RECORD;
    BEGIN
	FOR trigger IN EXECUTE 'SELECT tgname AS name FROM pg_trigger JOIN pg_class ON pg_trigger.tgrelid = pg_class.oid WHERE pg_class.relname = '''||ClassName||''' AND tgisconstraint = false AND tgname <> ''before_archive_row'''
	LOOP
	    EXECUTE 'DROP TRIGGER '||trigger.name||' ON "'||ClassName||'"';
	    BEGIN
		EXECUTE 'DROP FUNCTION '||trigger.name||'()';
	    EXCEPTION WHEN dependent_objects_still_exist THEN
		RAISE LOG 'Trigger function not dropped because there are still references';
	    END;
	END LOOP;
	EXECUTE 'DROP TRIGGER before_archive_row ON "'||ClassName||'"';
    END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


-- 
-- update reference triggers (restrict & friends also)
-- 

CREATE OR REPLACE FUNCTION patch_recreatereferencetriggers()
  RETURNS VOID AS
$BODY$
DECLARE
    ref_spec refcursor;

    attributeislocal boolean;
    classname varchar;
    attributename varchar;
    attributereference varchar;
    attributereferencetype varchar;
    attributereferencedomain varchar;

    triggername varchar;
BEGIN
	-- delete old triggers
	OPEN ref_spec FOR EXECUTE '
		SELECT classname,attributename,attributereference,attributereferencetype,attributereferencedomain
		FROM system_attributecatalog
		WHERE coalesce("attributereferencedomain",'''') <> ''''
	';
	LOOP
		FETCH ref_spec INTO classname,attributename,attributereference,attributereferencetype,attributereferencedomain;
		EXIT WHEN NOT FOUND;
		EXECUTE 'DROP TRIGGER IF EXISTS reference_'||classname||'_'||attributename||' ON "'||classname||'" CASCADE';
		triggername := attributereferencetype||'_'||attributereferencedomain||'_'||classname;
		PERFORM system_droptriggersrecursive(attributereference, triggername);
	END LOOP;
	CLOSE ref_spec;

	-- create new triggers
	OPEN ref_spec FOR EXECUTE '
		SELECT classname,attributename,attributereference,attributereferencetype,attributereferencedomain
		FROM system_attributecatalog
		WHERE coalesce("attributereferencedomain",'''') <> ''''
	';
	LOOP
		FETCH ref_spec INTO classname,attributename,attributereference,attributereferencetype,attributereferencedomain;
		EXIT WHEN NOT FOUND;
		PERFORM system_reference_createpkandtriggers(attributereferencetype,classname,attributename,attributereference,attributereferencedomain);
	END LOOP;
	CLOSE ref_spec;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

SELECT patch_recreatereferencetriggers();

DROP FUNCTION patch_recreatereferencetriggers();
