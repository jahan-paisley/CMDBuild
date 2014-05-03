-- Update column privileges to handle none value

CREATE OR REPLACE FUNCTION patch_215_02() RETURNS VOID AS $$

    DECLARE
        oldFilter varchar;
        newFilter varchar;
        row record;
    BEGIN

        FOR row IN
        SELECT "Id", "AttributesPrivileges"
        FROM "Grant"
        WHERE array_length("AttributesPrivileges", 1) > 0 LOOP
            oldFilter = array_to_string(row."AttributesPrivileges", '-separator-');
            newFilter = regexp_replace(oldFilter, ':hidden', ':none');
            RAISE INFO '%', newFilter;

            UPDATE "Grant"
            SET "AttributesPrivileges" = string_to_array(newFilter, '-separator-')
            WHERE "Id" = row."Id";

        END LOOP;

        RETURN;
    END;
$$ LANGUAGE PLPGSQL;

SELECT patch_215_02();
DROP FUNCTION patch_215_02();