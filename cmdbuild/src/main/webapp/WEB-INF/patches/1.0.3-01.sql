-- Change Role class type from reserved to sysread

COMMENT ON TABLE "Role" IS 'MODE: sysread|TYPE: class|DESCR: Roles|SUPERCLASS: false|MANAGER: class|STATUS: active';
