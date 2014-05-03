-- Remove not null constraint on the Email class to allow email deletion

ALTER TABLE "Email"
   ALTER COLUMN "Activity" DROP NOT NULL;
