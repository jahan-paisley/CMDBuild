-- Update Grant table to define attribute privileges at group level
CREATE OR REPLACE FUNCTION patch_215_01() RETURNS VOID AS $$

    DECLARE
        i integer;
        length integer;
        OldAttributeList varchar[];
        NewAttributeList varchar[];
        Row record;
    BEGIN

        RAISE INFO 'Update DisabledAttributes value to be used for new feature';

        FOR Row IN SELECT "Id", "DisabledAttributes" from "Grant" LOOP
            OldAttributeList := Row."DisabledAttributes";
            IF OldAttributeList IS NOT NULL THEN

                length := array_upper(OldAttributeList, 1);

                IF length IS NOT NULL THEN
                    FOR i IN 1 .. array_upper(OldAttributeList, 1) LOOP
                        RAISE INFO 'AttributeOld: %', OldAttributeList[i];
                        NewAttributeList[i] = OldAttributeList[i] || ':hidden';
                    END LOOP;

                    RAISE INFO 'AttributeListNew: %', NewAttributeList;

                    UPDATE "Grant"
                    SET "DisabledAttributes" = NewAttributeList
                    WHERE "Id" = Row."Id";

                END IF;
            END IF;
        END LOOP;

        RAISE INFO 'Alter DisabledAttributes column name';

        ALTER TABLE "Grant" RENAME COLUMN "DisabledAttributes" TO "AttributesPrivileges";

        RETURN;
    END;
$$ LANGUAGE PLPGSQL;

SELECT patch_215_01();
DROP FUNCTION patch_215_01();