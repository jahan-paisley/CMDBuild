-- Add a column in the Role table to manage the disabled features

select system_attribute_create('Role','DisabledModules','character varying[]',NULL,FALSE,FALSE,'MODE: read',NULL,NULL,NULL,NULL)