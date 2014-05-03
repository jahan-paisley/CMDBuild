-- Remove non-system mandatory attributes from sperclasses

CREATE OR REPLACE FUNCTION system_attribute_setmandatory(ClassName character varying, AttributeName character varying,
	IsMandatory boolean)
  RETURNS VOID AS
$BODY$
DECLARE
    is_check boolean;
    is_not_allowed boolean;
BEGIN
    EXECUTE 'SELECT (system_class_issuperclass(c.classcomment)' ||
            ' AND LOWER(system_read_comment(a.attributecomment::character varying, ''MODE''::character varying)) = ''write'')' ||
            ' FROM system_attributecatalog AS a JOIN system_classcatalog AS c ON a.classid = c.classid WHERE a.classname = ''' || ClassName || ''''
              INTO is_not_allowed;
    IF (is_not_allowed AND IsMandatory) THEN
        RAISE EXCEPTION 'Non-system superclass attributes cannot be set mandatory', AttributeName;
    END IF;

    EXECUTE 'SELECT ((TRIM(attributereference)<>'''' OR' ||
        ' LOWER(system_read_comment(attributecomment::character varying, ''MODE''::character varying)) = ''write''))' ||
        ' FROM system_attributecatalog WHERE classname = ''' || ClassName || ''' AND attributename = ''' || AttributeName || ''''
          INTO is_check;
    IF (is_check) THEN
		IF (IsMandatory) THEN
			EXECUTE 'ALTER TABLE "' || ClassName || '" ADD CONSTRAINT ' || AttributeName || '_notnull CHECK ' ||
		        '("Status"<>''A'' OR "' || AttributeName || '" IS NOT NULL)';
		ELSE
			EXECUTE 'ALTER TABLE "' || ClassName || '" DROP CONSTRAINT ' || AttributeName || '_notnull';
		END IF;
	ELSE
		IF (IsMandatory) THEN
			EXECUTE 'ALTER TABLE "' || ClassName || '" ALTER COLUMN "' || AttributeName || '" SET NOT NULL';
		ELSE
			EXECUTE 'ALTER TABLE "' || ClassName || '" ALTER COLUMN "' || AttributeName || '" DROP NOT NULL';
		END IF;
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

-- 
-- remove invalid mandatory attributes
-- 

CREATE OR REPLACE FUNCTION patch_createmandatoryrecursive(
		ClassName character varying,
		AttributeName character varying)
  RETURNS VOID AS
$BODY$
DECLARE
	ChildName varchar(60);
	field_list refcursor;
	is_superclass boolean;
BEGIN
	-- update childs if there are
	OPEN field_list FOR EXECUTE 'SELECT child FROM system_treecatalog WHERE parent='''||ClassName||'''';
	LOOP
		FETCH field_list INTO ChildName;
		EXIT WHEN NOT FOUND;
		PERFORM patch_createmandatoryrecursive(ChildName,AttributeName);
	END LOOP;
	-- finally create in this class if not a superclass
    EXECUTE 'SELECT system_class_issuperclass(classcomment)' ||
        ' FROM system_classcatalog WHERE classname = ''' || ClassName || ''''
          INTO is_superclass;
	IF (NOT is_superclass) THEN
		PERFORM system_attribute_setmandatory(ClassName,AttributeName,true);
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;


CREATE OR REPLACE FUNCTION patch_deleteinvalidmandatory()
  RETURNS VOID AS
$BODY$
DECLARE
    mand_attr refcursor;

    ClassName varchar;
    AttributeName varchar;
BEGIN
	-- delete old triggers
	OPEN mand_attr FOR EXECUTE '
		SELECT a.classname, a.attributename
			FROM system_attributecatalog AS a
				JOIN system_classcatalog AS c ON a.classid = c.classid
			WHERE a.attributenotnull AND NOT c.isview AND system_class_issuperclass(c.classcomment)
				AND LOWER(system_read_comment(a.attributecomment::character varying, ''MODE''::character varying)) = ''write''
		';
	LOOP
		FETCH mand_attr INTO ClassName, AttributeName;
		EXIT WHEN NOT FOUND;
		EXECUTE 'ALTER TABLE "' || ClassName || '" DROP CONSTRAINT ' || AttributeName || '_notnull';
		PERFORM patch_createmandatoryrecursive(ClassName,AttributeName);
	END LOOP;
	CLOSE mand_attr;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;

SELECT patch_deleteinvalidmandatory();

DROP FUNCTION patch_deleteinvalidmandatory();
DROP FUNCTION patch_createmandatoryrecursive(varchar, varchar);
