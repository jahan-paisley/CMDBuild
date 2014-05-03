-- Comments on base classes for 0.90 migration compatibility

COMMENT ON TABLE "Class" IS 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active';
COMMENT ON COLUMN "Class"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Class"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Class"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Class"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Class"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Class"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Class"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Class"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';


COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: Attività|SUPERCLASS: true|MANAGER: activity|STATUS: active';
COMMENT ON COLUMN "Activity"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."IdClass" IS 'MODE: reserved|DESCR: Classe';
COMMENT ON COLUMN "Activity"."Code" IS 'MODE: read|DESCR: Nome Attività|INDEX: 0||LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Description" IS 'MODE: read|DESCR: Descrizione|INDEX: 1|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: true|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."Notes" IS 'MODE: read|DESCR: Annotazioni';
COMMENT ON COLUMN "Activity"."FlowStatus" IS 'MODE: read|DESCR: Stato attività|INDEX: 2|LOOKUP: FlowStatus|REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: false|STATUS: active';
COMMENT ON COLUMN "Activity"."Priority" IS 'MODE: read|DESCR: Priorità|INDEX: 3|LOOKUP: Priority';
COMMENT ON COLUMN "Activity"."ActivityDefinitionId" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."ProcessCode" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."IsQuickAccept" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."ActivityDescription" IS 'MODE: write|DESCR: Descrizione Attività|INDEX: 4|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: |STATUS: active';
COMMENT ON COLUMN "Activity"."NextExecutor" IS 'MODE: reserved';


COMMENT ON TABLE "Grant" IS 'MODE: reserved|TYPE: class|DESCR: Grants|STATUS: active';
COMMENT ON COLUMN "Grant"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Grant"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Grant"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';
COMMENT ON COLUMN "Grant"."IdRole" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."IdGrantedClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."Mode" IS 'MODE: reserved';


COMMENT ON TABLE "LookUp" IS 'MODE: reserved|TYPE: class|DESCR: Lookup list|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "LookUp"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Code" IS 'MODE: read|DESCR: Code|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "LookUp"."Description" IS 'MODE: read|DESCR: Description|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "LookUp"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Notes" IS 'MODE: read|DESCR: Annotazioni';
COMMENT ON COLUMN "LookUp"."Type" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."ParentType" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."ParentId" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Number" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."IsDefault" IS 'MODE: reserved';


COMMENT ON TABLE "Map" IS 'MODE: reserved|TYPE: domain|DESCRDIR: è in relazione con|DESCRINV: è in relazione con|STATUS: active';
COMMENT ON COLUMN "Map"."IdDomain" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdClass1" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdObj1" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdClass2" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdObj2" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."EndDate" IS 'MODE: reserved';


COMMENT ON TABLE "Menu" IS 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|MANAGER: class|STATUS: active';
COMMENT ON COLUMN "Menu"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Menu"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Menu"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Menu"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Menu"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Menu"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Menu"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Menu"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';
COMMENT ON COLUMN "Menu"."IdParent" IS 'MODE: reserved|DESCR: Parent Item, 0 means no parent';
COMMENT ON COLUMN "Menu"."IdElementClass" IS 'MODE: reserved|DESCR: Class connect to this item';
COMMENT ON COLUMN "Menu"."IdElementObj" IS 'MODE: reserved|DESCR: Object connected to this item, 0 means no object';
COMMENT ON COLUMN "Menu"."Number" IS 'MODE: reserved|DESCR: Ordering';
COMMENT ON COLUMN "Menu"."IdGroup" IS 'MODE: reserved|DESCR: Group owner of this item, 0 means default group';
COMMENT ON COLUMN "Menu"."Type" IS 'MODE: reserved|DESCR: Group owner of this item, 0 means default group';


COMMENT ON TABLE "Role" IS 'MODE: reserved|TYPE: class|DESCR: Roles|SUPERCLASS: false|MANAGER: class|STATUS: active';
COMMENT ON COLUMN "Role"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Role"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Role"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';
COMMENT ON COLUMN "Role"."Administrator" IS 'MODE: write|DESCR: Administrator|INDEX: 1|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: 0|STATUS: active';
COMMENT ON COLUMN "Role"."startingClass" IS 'MODE: write|DESCR: Administrator|INDEX: 2|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: 0|STATUS: active';


COMMENT ON TABLE "User" IS 'MODE: reserved|TYPE: class|DESCR: Utenti|SUPERCLASS: false|MANAGER: class|STATUS: active';
COMMENT ON COLUMN "User"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "User"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "User"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';
COMMENT ON COLUMN "User"."Username" IS 'MODE: write|DESCR: Username|INDEX: 1|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: 0|STATUS: active';
COMMENT ON COLUMN "User"."Password" IS 'MODE: write|DESCR: Password|INDEX: 2|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: false|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: 0|STATUS: active';


COMMENT ON TABLE "Report" IS 'MODE: reserved|TYPE: class|DESCR: Report|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "Report"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Code" IS 'MODE: read|DESCR: Codice';
COMMENT ON COLUMN "Report"."Description" IS 'MODE: read|DESCR: Descrizione';
COMMENT ON COLUMN "Report"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Type" IS 'MODE: read|DESCR: Tipo';
COMMENT ON COLUMN "Report"."Query" IS 'MODE: read|DESCR: Query';
COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Images" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ImagesLength" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ReportLength" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Groups" IS 'MODE: reserved';
