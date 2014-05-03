
CREATE OR REPLACE FUNCTION _cm_legacy_get_menu_type(boolean, boolean, boolean, boolean)
  RETURNS varchar AS
$BODY$
    DECLARE
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menutype varchar;
    BEGIN
	IF (isprocess) THEN menutype='processclass';
	ELSIF(isview) THEN menutype='view';
	ELSIF(isreport) THEN menutype='report';
	ELSE menutype='class';
	END IF;

	RETURN menutype;
    END;
$BODY$
  LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION _cm_legacy_get_menu_code(boolean, boolean, boolean, boolean)
  RETURNS varchar AS
$BODY$
    DECLARE
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menucode varchar;
    BEGIN
	IF (issuperclass) THEN IF (isprocess) THEN menucode='superclassprocess'; ELSE menucode='superclass'; END IF;
	ELSIF(isview) THEN menucode='view';
	ELSIF(isreport) THEN menucode='report';
	ELSIF (isprocess) THEN menucode='processclass'; ELSE menucode='class';
	END IF;

	RETURN menucode;
    END;
$BODY$
  LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE VIEW system_privilegescatalog AS
 SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode"
   FROM (         SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode"
                   FROM "Grant"
        UNION
                 SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", system_classcatalog.classid::regclass AS classid, '-'
                   FROM system_classcatalog, "Role"
                  WHERE system_classcatalog.classid::regclass::oid <> '"Class"'::regclass::oid AND NOT ("Role"."Id"::text || system_classcatalog.classid::integer::text IN ( SELECT "Grant"."IdRole"::text || "Grant"."IdGrantedClass"::oid::integer::text
                           FROM "Grant"))) permission
   JOIN system_classcatalog ON permission."IdGrantedClass"::oid = system_classcatalog.classid AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text]))
  ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";
