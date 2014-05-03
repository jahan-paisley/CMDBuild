-- System classes for manageEmail extended attribute

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
SELECT system_class_create('Email', 'Class', false, 'MODE: reserved|TYPE: class|DESCR: Email|SUPERCLASS: false|MANAGER: class|STATUS: active');

-- ActivityEmail domain
SELECT system_domain_create('ActivityEmail', 'Activity', 'Email', 'MODE: reserved|TYPE: domain|CLASS1: Activity|CLASS2: Email|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active');

-- Email class (attributes)
SELECT system_attribute_create('Email', 'Activity', 'integer', '', true, false,
	'MODE: write|FIELDMODE: write|DESCR: Activity|INDEX: 4|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ActivityEmail|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active',
	'Activity', 'ActivityEmail', 'restrict', null);
SELECT system_attribute_create('Email', 'EmailStatus', 'integer', '', true, false,
	'MODE: write|FIELDMODE: write|DESCR: EmailStatus|INDEX: 5|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: EmailStatus|STATUS: active',
	'', '', '', null);
SELECT system_attribute_create('Email', 'From', 'text', '', false, false,
	'MODE: write|FIELDMODE: write|DESCR: From|INDEX: 6|CLASSORDER: 0|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active',
	'', '', '', null);
SELECT system_attribute_create('Email', 'TO', 'text', '', false, false,
	'MODE: write|FIELDMODE: write|DESCR: TO|INDEX: 7|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active',
	'', '', '', null);
SELECT system_attribute_create('Email', 'CC', 'text', '', false, false,
	'MODE: write|FIELDMODE: write|DESCR: CC|INDEX: 8|CLASSORDER: 0|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active',
	'', '', '', null);
SELECT system_attribute_create('Email', 'Subject', 'text', '', false, false,
	'MODE: write|FIELDMODE: write|DESCR: Subject|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active',
	'', '', '', null);
SELECT system_attribute_create('Email', 'Body', 'text', '', false, false,
	'MODE: write|FIELDMODE: write|DESCR: Body|INDEX: 10|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active',
	'', '', '', null);
