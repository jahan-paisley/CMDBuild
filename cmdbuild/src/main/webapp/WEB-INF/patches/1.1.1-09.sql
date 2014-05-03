-- Adapt the 1.1.1 database to the 1.2.0 version

CREATE UNIQUE INDEX idx_map_userrole_defaultgroup
  ON "Map_UserRole"
  USING btree
  ((
CASE
    WHEN "Status"::text = 'N'::text THEN NULL::regclass
    ELSE "IdClass1"
END), (
CASE
    WHEN "Status"::text = 'N'::text THEN NULL::integer
    ELSE "IdObj1"
END), (
CASE
    WHEN "DefaultGroup" THEN TRUE
    ELSE NULL::boolean
END));


ALTER TABLE "Role"
   ALTER COLUMN "Code" DROP NOT NULL;

COMMENT ON COLUMN "Role"."Notes" IS 'MODE: read|DESCR: Notes';
COMMENT ON COLUMN "User"."Notes" IS 'MODE: read|DESCR: Notes';

COMMENT ON TABLE "Patch" IS 'MODE: reserved|TYPE: class|DESCR: |SUPERCLASS: false|STATUS: active';
