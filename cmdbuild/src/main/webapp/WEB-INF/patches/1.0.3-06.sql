-- Rename Email attributes to match shark email tool agent

ALTER TABLE "Email" RENAME COLUMN "From" TO "FromAddress";
ALTER TABLE "Email" RENAME COLUMN "TO" TO "ToAddresses";
ALTER TABLE "Email" RENAME COLUMN "CC" TO "CcAddresses";
ALTER TABLE "Email" RENAME COLUMN "Body" TO "Content";

DROP TRIGGER before_archive_row ON "Email";
DROP TRIGGER after_archive_row_email ON "Email";
DROP FUNCTION after_archive_row_email();
SELECT system_class_createtriggers('Email');
