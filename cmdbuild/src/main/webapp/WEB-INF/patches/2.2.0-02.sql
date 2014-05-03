-- Create tables for using e-mails as source event for starting workflows

CREATE OR REPLACE FUNCTION patch_220_02() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'creating _EmailAccount table';
	PERFORM cm_create_class('_EmailAccount', NULL, 'MODE: reserved|TYPE: class|DESCR: Email Accounts|SUPERCLASS: false|STATUS: active');
	PERFORM _cm_attribute_set_uniqueness('"_EmailAccount"'::regclass::oid, 'Code', TRUE);
	PERFORM cm_create_class_attribute('_EmailAccount', 'IsDefault', 'boolean', null, false, false, 'MODE: write|DESCR: Is default|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'Address', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Address|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'Username', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Username|INDEX: 3|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'Password', 'varchar(100)', null, false, false, 'MODE: write|DESCR: Password|INDEX: 4|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'SmtpServer', 'varchar(100)', null, false, false, 'MODE: write|DESCR: SMTP server|INDEX: 5|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'SmtpPort', 'int4', null, false, false, 'MODE: write|DESCR: SMTP port|INDEX: 6|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'SmtpSsl', 'boolean', null, false, false, 'MODE: write|DESCR: SMTP SSL|INDEX: 7|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'ImapServer', 'varchar(100)', null, false, false, 'MODE: write|DESCR: IMAP server|INDEX: 8|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'ImapPort', 'int4', null, false, false, 'MODE: write|DESCR: IMAP port|INDEX: 9|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'ImapSsl', 'boolean', null, false, false, 'MODE: write|DESCR: IMAP SSL|INDEX: 10|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'InputFolder', 'varchar(50)', null, false, false, 'MODE: write|DESCR: Input folder|INDEX: 11|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'ProcessedFolder', 'varchar(50)', null, false, false, 'MODE: write|DESCR: Processed folder|INDEX: 12|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'RejectedFolder', 'varchar(50)', null, false, false, 'MODE: write|DESCR: Rejected folder|INDEX: 13|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailAccount', 'RejectNotMatching', 'boolean', null, false, false, 'MODE: write|DESCR: Reject not matching|INDEX: 14|STATUS: active');
	
	RAISE INFO 'renaming Scheduler table to _SchedulerJob';
	ALTER TABLE "Scheduler" RENAME TO "_SchedulerJob";
	ALTER TABLE "Scheduler_history" RENAME TO "_SchedulerJob_history";

	RAISE INFO 'creating _SchedulerJobParameter table';
	PERFORM cm_create_class('_SchedulerJobParameter', NULL, 'MODE: reserved|TYPE: class|DESCR: Email Accounts|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_SchedulerJobParameter', 'SchedulerId', 'int4', null, false, false, 'MODE: write|DESCR: Scheduler Id|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_SchedulerJobParameter', 'Key', 'text', null, false, false, 'MODE: write|DESCR: Key|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_SchedulerJobParameter', 'Value', 'text', null, false, false, 'MODE: write|DESCR: Value|INDEX: 3|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_220_02();

DROP FUNCTION patch_220_02();