--
-- FlowStatus lookup
--

INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.running','FlowStatus', 1, 'Running', true, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.not_running.suspended','FlowStatus', 2, 'Suspended', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.completed','FlowStatus', 3, 'Completed', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.terminated','FlowStatus', 4, 'Terminated', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.aborted','FlowStatus', 5, 'Aborted', false, 'A');

--
-- Activity class
--

CREATE TABLE "Activity"
(
  "FlowStatus" integer,
  "ActivityDefinitionId" character varying[],
  "ProcessCode" text,
  "NextExecutor" character varying[],
  "ActivityInstanceId" character varying[],
  "PrevExecutors" character varying[],
  "UniqueProcessDefinition" text,
  CONSTRAINT "Activity_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");
COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: Activity|SUPERCLASS: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."IdClass" IS 'MODE: reserved|DESCR: Class';
COMMENT ON COLUMN "Activity"."Code" IS 'MODE: read|DESCR: Activity Name|INDEX: 0|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Description" IS 'MODE: read|DESCR: Description|INDEX: 1|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."Notes" IS 'MODE: read|DESCR: Notes';
COMMENT ON COLUMN "Activity"."FlowStatus" IS 'MODE: system|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus';
COMMENT ON COLUMN "Activity"."ActivityDefinitionId" IS 'MODE: system|DESCR: Activity Definition Ids (for speed)';
COMMENT ON COLUMN "Activity"."ProcessCode" IS 'MODE: system|DESCR: Process Instance Id';
COMMENT ON COLUMN "Activity"."NextExecutor" IS 'MODE: system|DESCR: Activity Instance performers';
COMMENT ON COLUMN "Activity"."ActivityInstanceId" IS 'MODE: system|DESCR: Activity Instance Ids';
COMMENT ON COLUMN "Activity"."PrevExecutors" IS 'MODE: system|DESCR: Process Instance performers up to now';
COMMENT ON COLUMN "Activity"."UniqueProcessDefinition" IS 'MODE: system|DESCR: Unique Process Definition (for speed)';

CREATE INDEX idx_activity_code
  ON "Activity"
  USING btree
  ("Code");

CREATE INDEX idx_activity_description
  ON "Activity"
  USING btree
  ("Description");

CREATE INDEX idx_activity_idclass
  ON "Activity"
  USING btree
  ("IdClass");


--
-- Workflow Email
--

-- EmailStatus lookup
INSERT INTO "LookUp" ("IdClass", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'New', 'A', 'EmailStatus', 1, false);
INSERT INTO "LookUp" ("IdClass", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Received', 'A', 'EmailStatus', 2, false);
INSERT INTO "LookUp" ("IdClass", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Draft', 'A', 'EmailStatus', 3, false);
INSERT INTO "LookUp" ("IdClass", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Outgoing', 'A', 'EmailStatus', 4, false);
INSERT INTO "LookUp" ("IdClass", "Description", "Status", "Type", "Number", "IsDefault")
    VALUES ('"LookUp"'::regclass, 'Sent', 'A', 'EmailStatus', 5, false);

-- Email class (base)
SELECT cm_create_class('Email', 'Class', 'MODE: reserved|TYPE: class|DESCR: Email|SUPERCLASS: false|STATUS: active');

-- ActivityEmail domain
SELECT cm_create_domain('ActivityEmail', 'MODE: reserved|TYPE: domain|CLASS1: Activity|CLASS2: Email|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active');

-- Email class (attributes)
SELECT cm_create_class_attribute('Email', 'Activity', 'integer', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: Activity|INDEX: 4|REFERENCEDOM: ActivityEmail|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active');
SELECT cm_create_class_attribute('Email', 'EmailStatus', 'integer', '', true, false, 'MODE: read|FIELDMODE: write|DESCR: EmailStatus|INDEX: 5|BASEDSP: true|LOOKUP: EmailStatus|STATUS: active');
SELECT cm_create_class_attribute('Email', 'FromAddress', 'text', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: From|INDEX: 6|BASEDSP: true|STATUS: active');
SELECT cm_create_class_attribute('Email', 'ToAddresses', 'text', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: TO|INDEX: 7|BASEDSP: true|STATUS: active');
SELECT cm_create_class_attribute('Email', 'CcAddresses', 'text', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: CC|INDEX: 8|BASEDSP: false|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Subject', 'text', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: Subject|INDEX: 9|BASEDSP: true|STATUS: active');
SELECT cm_create_class_attribute('Email', 'Content', 'text', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: Body|INDEX: 10|BASEDSP: false|STATUS: active');
SELECT cm_create_class_attribute('Email', 'NotifyWith', 'text', null, false, false, 'MODE: write|DESCR: NotifyWith|INDEX: 10|BASEDSP: false|STATUS: active');
