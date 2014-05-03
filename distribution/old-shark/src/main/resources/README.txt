Configure shark/conf/Shark.conf changing the section at the end of the file
named "CMDBuild Custom Components Settings":

 * "org.cmdbuild.ws.url" should be the root URL of CMDBuild
 * "org.cmdbuild.ws.username" and "org.cmdbuild.ws.password" should refer to
   an existing user in the "serviceusers.privileged" of CMDBuild's auth.conf

In shark/META-INF/context.xml change the name of the database, using the name
of the CMDBuild database (ex. url="jdbc:postgresql://localhost/${cmdbuild}"
should be changed to url="jdbc:postgresql://localhost/cmdbuild" for a
database named "cmdbuild" in a local PostgreSQL installation).

Please note that Shark uses the same db of CMDBuild, storing its data inside
the "shark" schema. If you want to restore an empty schema you can run
${cmdbuild_home}/WEB-INF/sql/shark_schema/02_shark_emptydb.sql

The user of shark in postgres is created by CMDBuild with the following sql
(${cmdbuild_home}/WEB-INF/sql/shark_schema/01_shark_user.sql)

	CREATE ROLE shark LOGIN
		ENCRYPTED PASSWORD 'md5088dfc423ab6e29229aeed8eea5ad290'
		NOSUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE;
		ALTER ROLE shark SET search_path=pg_default,shark; 

Please note that the last line is absolutely needed when using Shark on
CMDBuild's database.

When Shark is up and running, configure the workflow module in the
Administration area.

