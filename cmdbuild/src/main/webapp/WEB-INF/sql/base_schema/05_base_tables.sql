CREATE SEQUENCE class_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
COMMENT ON SEQUENCE class_seq IS 'Sequence for autoincrement class';

---------------------------------------------
-- Class
---------------------------------------------

SELECT cm_create_class('Class', NULL, 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active');

CREATE INDEX idx_idclass_id
  ON "Class"
  USING btree
  ("IdClass", "Id");

DROP INDEX idx_class_idclass;

---------------------------------------------
-- Map
---------------------------------------------

CREATE TABLE "Map"
(
  "IdDomain" regclass NOT NULL,
  "IdClass1" regclass NOT NULL,
  "IdObj1" integer NOT NULL,
  "IdClass2" regclass NOT NULL,
  "IdObj2" integer NOT NULL,
  "Status" character(1),
  "User" varchar(100),
  "BeginDate" timestamp without time zone NOT NULL DEFAULT now(),
  "EndDate" timestamp without time zone,
  "Id" integer NOT NULL DEFAULT _cm_new_card_id(),
  CONSTRAINT "Map_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2")
);

COMMENT ON TABLE "Map" IS 'MODE: reserved|TYPE: domain|DESCRDIR: |DESCRINV: |STATUS: active';
COMMENT ON COLUMN "Map"."IdDomain" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdClass1" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdObj1" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdClass2" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdObj2" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."EndDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."Id" IS 'MODE: reserved';

CREATE INDEX idx_map_iddomain
  ON "Map"
  USING btree
  ("IdDomain");

CREATE INDEX idx_map_idobj1
  ON "Map"
  USING btree
  ("IdObj1");

CREATE INDEX idx_map_idobj2
  ON "Map"
  USING btree
  ("IdObj2");

---------------------------------------------
-- Lookup
---------------------------------------------

SELECT cm_create_class('LookUp', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Lookup list|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('LookUp', 'Code', 'character varying(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|BASEDSP: true');
SELECT cm_create_class_attribute('LookUp', 'Description', 'character varying(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|BASEDSP: true');
SELECT cm_create_class_attribute('LookUp', 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: read');
SELECT cm_create_class_attribute('LookUp', 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Annotazioni');
SELECT cm_create_class_attribute('LookUp', 'Type', 'character varying(64)', NULL, FALSE, FALSE, 'MODE: read');
SELECT cm_create_class_attribute('LookUp', 'ParentType', 'character varying(64)', NULL, FALSE, FALSE, 'MODE: read');
SELECT cm_create_class_attribute('LookUp', 'ParentId', 'integer', NULL, FALSE, FALSE, 'MODE: read');
SELECT cm_create_class_attribute('LookUp', 'Number', 'integer', NULL, TRUE, FALSE, 'MODE: read');
SELECT cm_create_class_attribute('LookUp', 'IsDefault', 'boolean', NULL, FALSE, FALSE, 'MODE: read');
