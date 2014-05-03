CREATE OR REPLACE FUNCTION system_isexecutor(oid, integer, character varying)
  RETURNS boolean AS
$BODY$
    declare
        tableoid alias for $1;
        id alias for $2;
        executor alias for $3;
        field_list refcursor;
        ctrl boolean;
        classname varchar;
    begin
        OPEN field_list FOR EXECUTE  
        'select classname::varchar from system_classcatalog where classid='||tableoid;
        FETCH field_list INTO classname;
        CLOSE field_list;
        OPEN field_list FOR EXECUTE  
        'select ((select count(*) from "'||classname||'_history" as ch where ch."CurrentId" = '||id||' and ch."NextExecutor" = '''||executor||''') != 0) or ('''||executor||''' = (select "NextExecutor" from "'||classname||'" where "Id"='||id||'))';
        FETCH field_list INTO ctrl;
        CLOSE field_list;
        return ctrl;
    end;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;