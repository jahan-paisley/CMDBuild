SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET default_tablespace = '';
SET default_with_oids = false;



CREATE TABLE objectid (
    next numeric(19,0) NOT NULL
);



CREATE TABLE qrtz_blob_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);



CREATE TABLE qrtz_calendars (
    sched_name character varying(120) NOT NULL,
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);



CREATE TABLE qrtz_cron_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120) NOT NULL,
    time_zone_id character varying(80)
);



CREATE TABLE qrtz_fired_triggers (
    sched_name character varying(120) NOT NULL,
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_nonconcurrent boolean,
    requests_recovery boolean
);



CREATE TABLE qrtz_job_details (
    sched_name character varying(120) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250) NOT NULL,
    is_durable boolean NOT NULL,
    is_nonconcurrent boolean NOT NULL,
    is_update_data boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);



CREATE TABLE qrtz_locks (
    sched_name character varying(120) NOT NULL,
    lock_name character varying(40) NOT NULL
);



CREATE TABLE qrtz_paused_trigger_grps (
    sched_name character varying(120) NOT NULL,
    trigger_group character varying(200) NOT NULL
);



CREATE TABLE qrtz_scheduler_state (
    sched_name character varying(120) NOT NULL,
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);



CREATE TABLE qrtz_simple_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);



CREATE TABLE qrtz_simprop_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512),
    str_prop_2 character varying(512),
    str_prop_3 character varying(512),
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean
);



CREATE TABLE qrtz_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr smallint,
    job_data bytea
);



CREATE TABLE shkactivities (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    id character varying(100) NOT NULL,
    activitysetdefinitionid character varying(90),
    activitydefinitionid character varying(90) NOT NULL,
    process numeric(19,0) NOT NULL,
    theresource numeric(19,0),
    pdefname character varying(200) NOT NULL,
    processid character varying(200) NOT NULL,
    resourceid character varying(100),
    state numeric(19,0) NOT NULL,
    blockactivityid character varying(100),
    performer character varying(100),
    isperformerasynchronous boolean,
    priority integer,
    name character varying(254),
    activated bigint NOT NULL,
    activatedtzo bigint NOT NULL,
    accepted bigint,
    acceptedtzo bigint,
    laststatetime bigint NOT NULL,
    laststatetimetzo bigint NOT NULL,
    limittime bigint NOT NULL,
    limittimetzo bigint NOT NULL,
    description text
);



CREATE TABLE shkactivitydata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    activity numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    isresult boolean NOT NULL,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkactivitydatablobs (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    activitydatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL
);



CREATE TABLE shkactivitydatawob (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    activity numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    isresult boolean NOT NULL,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkactivityhistorydetails (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    activityid character varying(100) NOT NULL,
    activityhistoryinfo numeric(19,0) NOT NULL,
    thetype integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    reassignfrom character varying(100),
    reassignto character varying(100),
    priority integer,
    limittime bigint,
    description text,
    category character varying(254),
    name character varying(254),
    deadlinetimelimit bigint,
    deadlineexceptionname character varying(100),
    deadlineissynchronous boolean,
    variabledefinitionid character varying(100),
    variabletype integer,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkactivityhistoryinfo (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    internalversion integer NOT NULL,
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    processpriority integer,
    processlimittime bigint,
    processdescription text,
    packageid character varying(90) NOT NULL,
    activityid character varying(100) NOT NULL,
    activityname character varying(254),
    activitypriority integer,
    activitylimittime bigint,
    activitydescription text,
    activitycategory character varying(254),
    activitydefinitionid character varying(90) NOT NULL,
    activitydefinitionname character varying(90),
    activitydefinitiontype integer,
    deadlinetimelimit bigint,
    deadlineexceptionname character varying(100),
    deadlineissynchronous boolean,
    createdtime bigint NOT NULL,
    createdtimetzo bigint,
    startedtime bigint,
    startedtimetzo bigint,
    suspendedtime bigint,
    suspendedtimetzo bigint,
    resumedtime bigint,
    resumedtimetzo bigint,
    acceptedtime bigint,
    acceptedtimetzo bigint,
    rejectedtime bigint,
    rejectedtimetzo bigint,
    closedtime bigint,
    closedtimetzo bigint,
    deletiontime bigint,
    deletiontimetzo bigint,
    createdbyusername character varying(100),
    startedbyusername character varying(100),
    suspendedbyusername character varying(100),
    resumedbyusername character varying(100),
    acceptedbyusername character varying(100),
    rejectedbyusername character varying(100),
    closedbyusername character varying(100),
    deletedbyusername character varying(100),
    currentusername character varying(100),
    laststate character varying(100),
    laststatetime bigint,
    laststatetimetzo bigint,
    lastrecordedtime bigint,
    lastrecordedtimetzo bigint,
    lastrecordedbyusername character varying(100),
    activityduration bigint,
    isdeleted boolean
);



CREATE TABLE shkactivitystateeventaudits (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL
);



CREATE TABLE shkactivitystates (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL
);



CREATE TABLE shkandjointable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    process numeric(19,0) NOT NULL,
    blockactivity numeric(19,0),
    activitydefinitionid character varying(90) NOT NULL,
    activity numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkassignmenteventaudits (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(100) NOT NULL,
    activityname character varying(254),
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    activitydefinitionid character varying(90) NOT NULL,
    activitydefinitionname character varying(90),
    activitydefinitiontype integer NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    oldresourceusername character varying(100),
    oldresourcename character varying(100),
    newresourceusername character varying(100) NOT NULL,
    newresourcename character varying(100),
    isaccepted boolean NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkassignmentstable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    activity numeric(19,0) NOT NULL,
    theresource numeric(19,0) NOT NULL,
    activityid character varying(100) NOT NULL,
    activityprocessid character varying(100) NOT NULL,
    activityprocessdefname character varying(200) NOT NULL,
    resourceid character varying(100) NOT NULL,
    isaccepted boolean NOT NULL,
    isvalid boolean NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkcounters (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    name character varying(100) NOT NULL,
    the_number numeric(19,0) NOT NULL
);



CREATE TABLE shkcreateprocesseventaudits (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    pactivityid character varying(100),
    pprocessid character varying(100),
    pprocessname character varying(254),
    pprocessfactoryname character varying(200),
    pprocessfactoryversion character varying(20),
    pactivitydefinitionid character varying(90),
    pactivitydefinitionname character varying(90),
    pprocessdefinitionid character varying(90),
    pprocessdefinitionname character varying(90),
    ppackageid character varying(90),
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkdataeventaudits (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(100),
    activityname character varying(254),
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    activitydefinitionid character varying(90),
    activitydefinitionname character varying(90),
    activitydefinitiontype integer,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkdeadlines (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    process numeric(19,0) NOT NULL,
    activity numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    timelimit bigint NOT NULL,
    timelimittzo bigint NOT NULL,
    exceptionname character varying(100) NOT NULL,
    issynchronous boolean NOT NULL,
    isexecuted boolean NOT NULL
);



CREATE TABLE shkeventtypes (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL
);



CREATE TABLE shkglobaldata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    dataid character varying(100) NOT NULL,
    datatype integer NOT NULL,
    datavalue bytea,
    datavaluexml text,
    datavaluevchar character varying(4000),
    datavaluedbl double precision,
    datavaluelong bigint,
    datavaluedate timestamp without time zone,
    datavaluebool boolean,
    ordno integer NOT NULL
);



CREATE TABLE shkgroupgrouptable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    sub_gid numeric(19,0) NOT NULL,
    groupid numeric(19,0) NOT NULL
);



CREATE TABLE shkgrouptable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    groupid character varying(100) NOT NULL,
    description character varying(254)
);



CREATE TABLE shkgroupuser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    username character varying(100) NOT NULL
);



CREATE TABLE shkgroupuserpacklevelpart (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL
);



CREATE TABLE shkgroupuserproclevelpart (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL
);



CREATE TABLE shkneweventauditdata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkneweventauditdatablobs (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    neweventauditdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL
);



CREATE TABLE shkneweventauditdatawob (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shknextxpdlversions (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdlid character varying(90) NOT NULL,
    nextversion character varying(20) NOT NULL
);



CREATE TABLE shknormaluser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    username character varying(100) NOT NULL
);



CREATE TABLE shkoldeventauditdata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkoldeventauditdatablobs (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    oldeventauditdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL
);



CREATE TABLE shkoldeventauditdatawob (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkpacklevelparticipant (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    participant_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL
);



CREATE TABLE shkpacklevelxpdlapp (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    application_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptaappdetail (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptaappdetusr (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptaappuser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptoolagntapp (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkprocessdata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    process numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkprocessdatablobs (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    processdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL
);



CREATE TABLE shkprocessdatawob (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    process numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkprocessdefinitions (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    name character varying(200) NOT NULL,
    packageid character varying(90) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    processdefinitioncreated bigint NOT NULL,
    processdefinitionversion character varying(20) NOT NULL,
    state integer NOT NULL
);



CREATE TABLE shkprocesses (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    syncversion bigint NOT NULL,
    id character varying(100) NOT NULL,
    processdefinition numeric(19,0) NOT NULL,
    pdefname character varying(200) NOT NULL,
    activityrequesterid character varying(100),
    activityrequesterprocessid character varying(100),
    resourcerequesterid character varying(100) NOT NULL,
    externalrequesterclassname character varying(254),
    state numeric(19,0) NOT NULL,
    priority integer,
    name character varying(254),
    created bigint NOT NULL,
    createdtzo bigint NOT NULL,
    started bigint,
    startedtzo bigint,
    laststatetime bigint NOT NULL,
    laststatetimetzo bigint NOT NULL,
    limittime bigint NOT NULL,
    limittimetzo bigint NOT NULL,
    description text
);



CREATE TABLE shkprocesshistorydetails (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    processid character varying(100) NOT NULL,
    processhistoryinfo numeric(19,0) NOT NULL,
    thetype integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    priority integer,
    limittime bigint,
    description text,
    category character varying(254),
    name character varying(254),
    variabledefinitionid character varying(100),
    variabletype integer,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkprocesshistoryinfo (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    internalversion integer NOT NULL,
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    processpriority integer,
    processlimittime bigint,
    processdescription text,
    processcategory character varying(254),
    packageid character varying(90) NOT NULL,
    pactivityid character varying(100),
    pprocessid character varying(100),
    pprocessname character varying(254),
    pprocessfactoryname character varying(200),
    pprocessfactoryversion character varying(20),
    pactivitydefinitionid character varying(90),
    pactivitydefinitionname character varying(90),
    pprocessdefinitionid character varying(90),
    pprocessdefinitionname character varying(90),
    ppackageid character varying(90),
    createdtime bigint,
    createdtimetzo bigint,
    startedtime bigint,
    startedtimetzo bigint,
    suspendedtime bigint,
    suspendedtimetzo bigint,
    resumedtime bigint,
    resumedtimetzo bigint,
    closedtime bigint,
    closedtimetzo bigint,
    deletiontime bigint,
    deletiontimetzo bigint,
    createdbyusername character varying(100),
    startedbyusername character varying(100),
    suspendedbyusername character varying(100),
    resumedbyusername character varying(100),
    closedbyusername character varying(100),
    deletedbyusername character varying(100),
    laststate character varying(100),
    laststatetime bigint,
    laststatetimetzo bigint,
    lastrecordedtime bigint,
    lastrecordedtimetzo bigint,
    lastrecordedbyusername character varying(100),
    processduration bigint,
    isdeleted boolean
);



CREATE TABLE shkprocessrequesters (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    id character varying(100) NOT NULL,
    activityrequester numeric(19,0),
    resourcerequester numeric(19,0)
);



CREATE TABLE shkprocessstateeventaudits (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL
);



CREATE TABLE shkprocessstates (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL
);



CREATE TABLE shkproclevelparticipant (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    participant_id character varying(90) NOT NULL,
    processoid numeric(19,0) NOT NULL
);



CREATE TABLE shkproclevelxpdlapp (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    application_id character varying(90) NOT NULL,
    processoid numeric(19,0) NOT NULL
);



CREATE TABLE shkproclevelxpdlapptaappdetail (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkproclevelxpdlapptaappdetusr (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkproclevelxpdlapptaappuser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkproclevelxpdlapptoolagntapp (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL
);



CREATE TABLE shkresourcestable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    username character varying(100) NOT NULL,
    name character varying(100)
);



CREATE TABLE shkstateeventaudits (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(100),
    activityname character varying(254),
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    activitydefinitionid character varying(90),
    activitydefinitionname character varying(90),
    activitydefinitiontype integer,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    oldprocessstate numeric(19,0),
    newprocessstate numeric(19,0),
    oldactivitystate numeric(19,0),
    newactivitystate numeric(19,0),
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shktoolagentapp (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    tool_agent_name character varying(250) NOT NULL,
    app_name character varying(90) NOT NULL
);



CREATE TABLE shktoolagentappdetail (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    app_mode numeric(10,0) NOT NULL,
    toolagent_appoid numeric(19,0) NOT NULL
);



CREATE TABLE shktoolagentappdetailuser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    toolagent_appoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL
);



CREATE TABLE shktoolagentappuser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    toolagent_appoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL
);



CREATE TABLE shktoolagentuser (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    username character varying(100) NOT NULL,
    pwd character varying(100)
);



CREATE TABLE shkusergrouptable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    userid numeric(19,0) NOT NULL,
    groupid numeric(19,0) NOT NULL
);



CREATE TABLE shkuserpacklevelpart (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL
);



CREATE TABLE shkuserproclevelparticipant (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL
);



CREATE TABLE shkusertable (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    userid character varying(100) NOT NULL,
    firstname character varying(50),
    lastname character varying(50),
    passwd character varying(50) NOT NULL,
    email character varying(254)
);



CREATE TABLE shkxpdlapplicationpackage (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    package_id character varying(90) NOT NULL
);



CREATE TABLE shkxpdlapplicationprocess (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    process_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL
);



CREATE TABLE shkxpdldata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdlcontent bytea NOT NULL,
    xpdlclasscontent bytea NOT NULL,
    xpdl numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkxpdlhistory (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdlid character varying(90) NOT NULL,
    xpdlversion character varying(20) NOT NULL,
    xpdlclassversion bigint NOT NULL,
    xpdluploadtime timestamp without time zone NOT NULL,
    xpdlhistoryuploadtime timestamp without time zone NOT NULL
);



CREATE TABLE shkxpdlhistorydata (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdlcontent bytea NOT NULL,
    xpdlclasscontent bytea NOT NULL,
    xpdlhistory numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL
);



CREATE TABLE shkxpdlparticipantpackage (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    package_id character varying(90) NOT NULL
);



CREATE TABLE shkxpdlparticipantprocess (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    process_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL
);



CREATE TABLE shkxpdlreferences (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    referredxpdlid character varying(90) NOT NULL,
    referringxpdl numeric(19,0) NOT NULL,
    referredxpdlnumber integer NOT NULL
);



CREATE TABLE shkxpdls (
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL,
    xpdlid character varying(90) NOT NULL,
    xpdlversion character varying(20) NOT NULL,
    xpdlclassversion bigint NOT NULL,
    xpdluploadtime timestamp without time zone NOT NULL
);



INSERT INTO objectid VALUES (1000100);






















































INSERT INTO shkactivitystateeventaudits VALUES (1000013, 0, 'open.running', 'open.running');
INSERT INTO shkactivitystateeventaudits VALUES (1000015, 0, 'open.not_running.not_started', 'open.not_running.not_started');
INSERT INTO shkactivitystateeventaudits VALUES (1000017, 0, 'open.not_running.suspended', 'open.not_running.suspended');
INSERT INTO shkactivitystateeventaudits VALUES (1000019, 0, 'closed.completed', 'closed.completed');
INSERT INTO shkactivitystateeventaudits VALUES (1000021, 0, 'closed.terminated', 'closed.terminated');
INSERT INTO shkactivitystateeventaudits VALUES (1000023, 0, 'closed.aborted', 'closed.aborted');



INSERT INTO shkactivitystates VALUES (1000001, 0, 'open.running', 'open.running');
INSERT INTO shkactivitystates VALUES (1000003, 0, 'open.not_running.not_started', 'open.not_running.not_started');
INSERT INTO shkactivitystates VALUES (1000005, 0, 'open.not_running.suspended', 'open.not_running.suspended');
INSERT INTO shkactivitystates VALUES (1000007, 0, 'closed.completed', 'closed.completed');
INSERT INTO shkactivitystates VALUES (1000009, 0, 'closed.terminated', 'closed.terminated');
INSERT INTO shkactivitystates VALUES (1000011, 0, 'closed.aborted', 'closed.aborted');
























INSERT INTO shkeventtypes VALUES (1000024, 0, 'packageLoaded', 'packageLoaded');
INSERT INTO shkeventtypes VALUES (1000025, 0, 'packageUnloaded', 'packageUnloaded');
INSERT INTO shkeventtypes VALUES (1000026, 0, 'packageUpdated', 'packageUpdated');
INSERT INTO shkeventtypes VALUES (1000027, 0, 'processCreated', 'processCreated');
INSERT INTO shkeventtypes VALUES (1000028, 0, 'processStateChanged', 'processStateChanged');
INSERT INTO shkeventtypes VALUES (1000029, 0, 'processContextChanged', 'processContextChanged');
INSERT INTO shkeventtypes VALUES (1000030, 0, 'activityStateChanged', 'activityStateChanged');
INSERT INTO shkeventtypes VALUES (1000031, 0, 'activityContextChanged', 'activityContextChanged');
INSERT INTO shkeventtypes VALUES (1000032, 0, 'activityResultChanged', 'activityResultChanged');
INSERT INTO shkeventtypes VALUES (1000033, 0, 'activityAssignmentChanged', 'activityAssignmentChanged');























































































INSERT INTO shkprocessstateeventaudits VALUES (1000012, 0, 'open.running', 'open.running');
INSERT INTO shkprocessstateeventaudits VALUES (1000014, 0, 'open.not_running.not_started', 'open.not_running.not_started');
INSERT INTO shkprocessstateeventaudits VALUES (1000016, 0, 'open.not_running.suspended', 'open.not_running.suspended');
INSERT INTO shkprocessstateeventaudits VALUES (1000018, 0, 'closed.completed', 'closed.completed');
INSERT INTO shkprocessstateeventaudits VALUES (1000020, 0, 'closed.terminated', 'closed.terminated');
INSERT INTO shkprocessstateeventaudits VALUES (1000022, 0, 'closed.aborted', 'closed.aborted');



INSERT INTO shkprocessstates VALUES (1000000, 0, 'open.running', 'open.running');
INSERT INTO shkprocessstates VALUES (1000002, 0, 'open.not_running.not_started', 'open.not_running.not_started');
INSERT INTO shkprocessstates VALUES (1000004, 0, 'open.not_running.suspended', 'open.not_running.suspended');
INSERT INTO shkprocessstates VALUES (1000006, 0, 'closed.completed', 'closed.completed');
INSERT INTO shkprocessstates VALUES (1000008, 0, 'closed.terminated', 'closed.terminated');
INSERT INTO shkprocessstates VALUES (1000010, 0, 'closed.aborted', 'closed.aborted');

















































































ALTER TABLE ONLY objectid
    ADD CONSTRAINT objectid_pkey PRIMARY KEY (next);



ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name);



ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id);



ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group);



ALTER TABLE ONLY qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name);



ALTER TABLE ONLY qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group);



ALTER TABLE ONLY qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name);



ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivityhistorydetails
    ADD CONSTRAINT shkactivityhistorydetails_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivityhistoryinfo
    ADD CONSTRAINT shkactivityhistoryinfo_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitystateeventaudits
    ADD CONSTRAINT shkactivitystateeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitystates
    ADD CONSTRAINT shkactivitystates_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkcounters
    ADD CONSTRAINT shkcounters_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkeventtypes
    ADD CONSTRAINT shkeventtypes_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkglobaldata
    ADD CONSTRAINT shkglobaldata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgrouptable
    ADD CONSTRAINT shkgrouptable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupuser
    ADD CONSTRAINT shkgroupuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shknextxpdlversions
    ADD CONSTRAINT shknextxpdlversions_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shknormaluser
    ADD CONSTRAINT shknormaluser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdefinitions
    ADD CONSTRAINT shkprocessdefinitions_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocesshistorydetails
    ADD CONSTRAINT shkprocesshistorydetails_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocesshistoryinfo
    ADD CONSTRAINT shkprocesshistoryinfo_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessstateeventaudits
    ADD CONSTRAINT shkprocessstateeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessstates
    ADD CONSTRAINT shkprocessstates_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkresourcestable
    ADD CONSTRAINT shkresourcestable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentapp
    ADD CONSTRAINT shktoolagentapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentuser
    ADD CONSTRAINT shktoolagentuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkusertable
    ADD CONSTRAINT shkusertable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlapplicationpackage
    ADD CONSTRAINT shkxpdlapplicationpackage_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlhistory
    ADD CONSTRAINT shkxpdlhistory_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlparticipantpackage
    ADD CONSTRAINT shkxpdlparticipantpackage_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdls
    ADD CONSTRAINT shkxpdls_objectid PRIMARY KEY (objectid);



CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);



CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers USING btree (sched_name, job_name, job_group);



CREATE INDEX idx_qrtz_ft_jg ON qrtz_fired_triggers USING btree (sched_name, job_group);



CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);



CREATE INDEX idx_qrtz_ft_tg ON qrtz_fired_triggers USING btree (sched_name, trigger_group);



CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers USING btree (sched_name, instance_name);



CREATE INDEX idx_qrtz_j_grp ON qrtz_job_details USING btree (sched_name, job_group);



CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details USING btree (sched_name, requests_recovery);



CREATE INDEX idx_qrtz_t_c ON qrtz_triggers USING btree (sched_name, calendar_name);



CREATE INDEX idx_qrtz_t_g ON qrtz_triggers USING btree (sched_name, trigger_group);



CREATE INDEX idx_qrtz_t_j ON qrtz_triggers USING btree (sched_name, job_name, job_group);



CREATE INDEX idx_qrtz_t_jg ON qrtz_triggers USING btree (sched_name, job_group);



CREATE INDEX idx_qrtz_t_n_g_state ON qrtz_triggers USING btree (sched_name, trigger_group, trigger_state);



CREATE INDEX idx_qrtz_t_n_state ON qrtz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);



CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers USING btree (sched_name, next_fire_time);



CREATE INDEX idx_qrtz_t_nft_misfire ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time);



CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers USING btree (sched_name, trigger_state, next_fire_time);



CREATE INDEX idx_qrtz_t_nft_st_misfire ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);



CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);



CREATE INDEX idx_qrtz_t_state ON qrtz_triggers USING btree (sched_name, trigger_state);



CREATE UNIQUE INDEX shkactivities_i1 ON shkactivities USING btree (id);



CREATE INDEX shkactivities_i2 ON shkactivities USING btree (process, activitysetdefinitionid, activitydefinitionid);



CREATE INDEX shkactivities_i3 ON shkactivities USING btree (process, state);



CREATE UNIQUE INDEX shkactivitydata_i1 ON shkactivitydata USING btree (cnt);



CREATE UNIQUE INDEX shkactivitydata_i2 ON shkactivitydata USING btree (activity, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkactivitydatablobs_i1 ON shkactivitydatablobs USING btree (activitydatawob, ordno);



CREATE UNIQUE INDEX shkactivitydatawob_i1 ON shkactivitydatawob USING btree (cnt);



CREATE UNIQUE INDEX shkactivitydatawob_i2 ON shkactivitydatawob USING btree (activity, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkactivityhistorydetails_i1 ON shkactivityhistorydetails USING btree (cnt);



CREATE INDEX shkactivityhistorydetails_i2 ON shkactivityhistorydetails USING btree (activityid);



CREATE UNIQUE INDEX shkactivityhistoryinfo_i1 ON shkactivityhistoryinfo USING btree (activityid);



CREATE INDEX shkactivityhistoryinfo_i2 ON shkactivityhistoryinfo USING btree (processid);



CREATE UNIQUE INDEX shkactivitystateeventaudits_i1 ON shkactivitystateeventaudits USING btree (keyvalue);



CREATE UNIQUE INDEX shkactivitystateeventaudits_i2 ON shkactivitystateeventaudits USING btree (name);



CREATE UNIQUE INDEX shkactivitystates_i1 ON shkactivitystates USING btree (keyvalue);



CREATE UNIQUE INDEX shkactivitystates_i2 ON shkactivitystates USING btree (name);



CREATE UNIQUE INDEX shkandjointable_i1 ON shkandjointable USING btree (cnt);



CREATE INDEX shkandjointable_i2 ON shkandjointable USING btree (process, blockactivity, activitydefinitionid);



CREATE INDEX shkandjointable_i3 ON shkandjointable USING btree (activity);



CREATE UNIQUE INDEX shkassignmenteventaudits_i1 ON shkassignmenteventaudits USING btree (cnt);



CREATE UNIQUE INDEX shkassignmentstable_i1 ON shkassignmentstable USING btree (cnt);



CREATE UNIQUE INDEX shkassignmentstable_i2 ON shkassignmentstable USING btree (activity, theresource);



CREATE INDEX shkassignmentstable_i3 ON shkassignmentstable USING btree (theresource, isvalid);



CREATE INDEX shkassignmentstable_i4 ON shkassignmentstable USING btree (activityid);



CREATE INDEX shkassignmentstable_i5 ON shkassignmentstable USING btree (resourceid);



CREATE UNIQUE INDEX shkcounters_i1 ON shkcounters USING btree (name);



CREATE UNIQUE INDEX shkcreateprocesseventaudits_i1 ON shkcreateprocesseventaudits USING btree (cnt);



CREATE UNIQUE INDEX shkdataeventaudits_i1 ON shkdataeventaudits USING btree (cnt);



CREATE UNIQUE INDEX shkdeadlines_i1 ON shkdeadlines USING btree (cnt);



CREATE INDEX shkdeadlines_i2 ON shkdeadlines USING btree (process, timelimit);



CREATE INDEX shkdeadlines_i3 ON shkdeadlines USING btree (activity, timelimit);



CREATE UNIQUE INDEX shkeventtypes_i1 ON shkeventtypes USING btree (keyvalue);



CREATE UNIQUE INDEX shkeventtypes_i2 ON shkeventtypes USING btree (name);



CREATE UNIQUE INDEX shkglobaldata_i1 ON shkglobaldata USING btree (dataid, ordno);



CREATE UNIQUE INDEX shkgroupgrouptable_i1 ON shkgroupgrouptable USING btree (sub_gid, groupid);



CREATE INDEX shkgroupgrouptable_i2 ON shkgroupgrouptable USING btree (groupid);



CREATE UNIQUE INDEX shkgrouptable_i1 ON shkgrouptable USING btree (groupid);



CREATE UNIQUE INDEX shkgroupuser_i1 ON shkgroupuser USING btree (username);



CREATE UNIQUE INDEX shkgroupuserpacklevelpart_i1 ON shkgroupuserpacklevelpart USING btree (participantoid, useroid);



CREATE UNIQUE INDEX shkgroupuserproclevelpart_i1 ON shkgroupuserproclevelpart USING btree (participantoid, useroid);



CREATE UNIQUE INDEX shkneweventauditdata_i1 ON shkneweventauditdata USING btree (cnt);



CREATE UNIQUE INDEX shkneweventauditdata_i2 ON shkneweventauditdata USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkneweventauditdatablobs_i1 ON shkneweventauditdatablobs USING btree (neweventauditdatawob, ordno);



CREATE UNIQUE INDEX shkneweventauditdatawob_i1 ON shkneweventauditdatawob USING btree (cnt);



CREATE UNIQUE INDEX shkneweventauditdatawob_i2 ON shkneweventauditdatawob USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shknextxpdlversions_i1 ON shknextxpdlversions USING btree (xpdlid, nextversion);



CREATE UNIQUE INDEX shknormaluser_i1 ON shknormaluser USING btree (username);



CREATE UNIQUE INDEX shkoldeventauditdata_i1 ON shkoldeventauditdata USING btree (cnt);



CREATE UNIQUE INDEX shkoldeventauditdata_i2 ON shkoldeventauditdata USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkoldeventauditdatablobs_i1 ON shkoldeventauditdatablobs USING btree (oldeventauditdatawob, ordno);



CREATE UNIQUE INDEX shkoldeventauditdatawob_i1 ON shkoldeventauditdatawob USING btree (cnt);



CREATE UNIQUE INDEX shkoldeventauditdatawob_i2 ON shkoldeventauditdatawob USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkpacklevelparticipant_i1 ON shkpacklevelparticipant USING btree (participant_id, packageoid);



CREATE UNIQUE INDEX shkpacklevelxpdlapp_i1 ON shkpacklevelxpdlapp USING btree (application_id, packageoid);



CREATE UNIQUE INDEX shkpacklevelxpdlapptaappdetail_i1 ON shkpacklevelxpdlapptaappdetail USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkpacklevelxpdlapptaappdetusr_i1 ON shkpacklevelxpdlapptaappdetusr USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkpacklevelxpdlapptaappuser_i1 ON shkpacklevelxpdlapptaappuser USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkpacklevelxpdlapptoolagntapp_i1 ON shkpacklevelxpdlapptoolagntapp USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkprocessdata_i1 ON shkprocessdata USING btree (cnt);



CREATE UNIQUE INDEX shkprocessdata_i2 ON shkprocessdata USING btree (process, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkprocessdatablobs_i1 ON shkprocessdatablobs USING btree (processdatawob, ordno);



CREATE UNIQUE INDEX shkprocessdatawob_i1 ON shkprocessdatawob USING btree (cnt);



CREATE UNIQUE INDEX shkprocessdatawob_i2 ON shkprocessdatawob USING btree (process, variabledefinitionid, ordno);



CREATE UNIQUE INDEX shkprocessdefinitions_i1 ON shkprocessdefinitions USING btree (name);



CREATE UNIQUE INDEX shkprocesses_i1 ON shkprocesses USING btree (id);



CREATE INDEX shkprocesses_i2 ON shkprocesses USING btree (processdefinition);



CREATE INDEX shkprocesses_i3 ON shkprocesses USING btree (state);



CREATE INDEX shkprocesses_i4 ON shkprocesses USING btree (activityrequesterid);



CREATE INDEX shkprocesses_i5 ON shkprocesses USING btree (resourcerequesterid);



CREATE UNIQUE INDEX shkprocesshistorydetails_i1 ON shkprocesshistorydetails USING btree (cnt);



CREATE INDEX shkprocesshistorydetails_i2 ON shkprocesshistorydetails USING btree (processid);



CREATE UNIQUE INDEX shkprocesshistoryinfo_i1 ON shkprocesshistoryinfo USING btree (processid);



CREATE UNIQUE INDEX shkprocessrequesters_i1 ON shkprocessrequesters USING btree (id);



CREATE INDEX shkprocessrequesters_i2 ON shkprocessrequesters USING btree (activityrequester);



CREATE INDEX shkprocessrequesters_i3 ON shkprocessrequesters USING btree (resourcerequester);



CREATE UNIQUE INDEX shkprocessstateeventaudits_i1 ON shkprocessstateeventaudits USING btree (keyvalue);



CREATE UNIQUE INDEX shkprocessstateeventaudits_i2 ON shkprocessstateeventaudits USING btree (name);



CREATE UNIQUE INDEX shkprocessstates_i1 ON shkprocessstates USING btree (keyvalue);



CREATE UNIQUE INDEX shkprocessstates_i2 ON shkprocessstates USING btree (name);



CREATE UNIQUE INDEX shkproclevelparticipant_i1 ON shkproclevelparticipant USING btree (participant_id, processoid);



CREATE UNIQUE INDEX shkproclevelxpdlapp_i1 ON shkproclevelxpdlapp USING btree (application_id, processoid);



CREATE UNIQUE INDEX shkproclevelxpdlapptaappdetail_i1 ON shkproclevelxpdlapptaappdetail USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkproclevelxpdlapptaappdetusr_i1 ON shkproclevelxpdlapptaappdetusr USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkproclevelxpdlapptaappuser_i1 ON shkproclevelxpdlapptaappuser USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkproclevelxpdlapptoolagntapp_i1 ON shkproclevelxpdlapptoolagntapp USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX shkresourcestable_i1 ON shkresourcestable USING btree (username);



CREATE UNIQUE INDEX shkstateeventaudits_i1 ON shkstateeventaudits USING btree (cnt);



CREATE UNIQUE INDEX shktoolagentapp_i1 ON shktoolagentapp USING btree (tool_agent_name, app_name);



CREATE UNIQUE INDEX shktoolagentappdetail_i1 ON shktoolagentappdetail USING btree (app_mode, toolagent_appoid);



CREATE UNIQUE INDEX shktoolagentappdetailuser_i1 ON shktoolagentappdetailuser USING btree (toolagent_appoid, useroid);



CREATE UNIQUE INDEX shktoolagentappuser_i1 ON shktoolagentappuser USING btree (toolagent_appoid, useroid);



CREATE UNIQUE INDEX shktoolagentuser_i1 ON shktoolagentuser USING btree (username);



CREATE UNIQUE INDEX shkusergrouptable_i1 ON shkusergrouptable USING btree (userid, groupid);



CREATE UNIQUE INDEX shkuserpacklevelpart_i1 ON shkuserpacklevelpart USING btree (participantoid, useroid);



CREATE UNIQUE INDEX shkuserproclevelparticipant_i1 ON shkuserproclevelparticipant USING btree (participantoid, useroid);



CREATE UNIQUE INDEX shkusertable_i1 ON shkusertable USING btree (userid);



CREATE UNIQUE INDEX shkxpdlapplicationpackage_i1 ON shkxpdlapplicationpackage USING btree (package_id);



CREATE UNIQUE INDEX shkxpdlapplicationprocess_i1 ON shkxpdlapplicationprocess USING btree (process_id, packageoid);



CREATE UNIQUE INDEX shkxpdldata_i1 ON shkxpdldata USING btree (cnt);



CREATE UNIQUE INDEX shkxpdldata_i2 ON shkxpdldata USING btree (xpdl);



CREATE UNIQUE INDEX shkxpdlhistory_i1 ON shkxpdlhistory USING btree (xpdlid, xpdlversion);



CREATE UNIQUE INDEX shkxpdlhistorydata_i1 ON shkxpdlhistorydata USING btree (cnt);



CREATE UNIQUE INDEX shkxpdlparticipantpackage_i1 ON shkxpdlparticipantpackage USING btree (package_id);



CREATE UNIQUE INDEX shkxpdlparticipantprocess_i1 ON shkxpdlparticipantprocess USING btree (process_id, packageoid);



CREATE UNIQUE INDEX shkxpdlreferences_i1 ON shkxpdlreferences USING btree (referredxpdlid, referringxpdl);



CREATE UNIQUE INDEX shkxpdls_i1 ON shkxpdls USING btree (xpdlid, xpdlversion);



ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES qrtz_triggers(sched_name, trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_sched_name_fkey FOREIGN KEY (sched_name, job_name, job_group) REFERENCES qrtz_job_details(sched_name, job_name, job_group);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_state FOREIGN KEY (state) REFERENCES shkactivitystates(objectid);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_theresource FOREIGN KEY (theresource) REFERENCES shkresourcestable(objectid);



ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_activitydatawob FOREIGN KEY (activitydatawob) REFERENCES shkactivitydatawob(objectid);



ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkactivityhistorydetails
    ADD CONSTRAINT shkactivityhistorydetails_activityhistoryinfo FOREIGN KEY (activityhistoryinfo) REFERENCES shkactivityhistoryinfo(objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_blockactivity FOREIGN KEY (blockactivity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_theresource FOREIGN KEY (theresource) REFERENCES shkresourcestable(objectid);



ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_groupid FOREIGN KEY (groupid) REFERENCES shkgrouptable(objectid);



ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_sub_gid FOREIGN KEY (sub_gid) REFERENCES shkgrouptable(objectid);



ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkpacklevelparticipant(objectid);



ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_useroid FOREIGN KEY (useroid) REFERENCES shkgroupuser(objectid);



ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkproclevelparticipant(objectid);



ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_useroid FOREIGN KEY (useroid) REFERENCES shkgroupuser(objectid);



ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_neweventauditdatawob FOREIGN KEY (neweventauditdatawob) REFERENCES shkneweventauditdatawob(objectid);



ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_oldeventauditdatawob FOREIGN KEY (oldeventauditdatawob) REFERENCES shkoldeventauditdatawob(objectid);



ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlparticipantpackage(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlapplicationpackage(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetail(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetailuser(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappuser(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_processdatawob FOREIGN KEY (processdatawob) REFERENCES shkprocessdatawob(objectid);



ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_processdefinition FOREIGN KEY (processdefinition) REFERENCES shkprocessdefinitions(objectid);



ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_state FOREIGN KEY (state) REFERENCES shkprocessstates(objectid);



ALTER TABLE ONLY shkprocesshistorydetails
    ADD CONSTRAINT shkprocesshistorydetails_processhistoryinfo FOREIGN KEY (processhistoryinfo) REFERENCES shkprocesshistoryinfo(objectid);



ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_activityrequester FOREIGN KEY (activityrequester) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_resourcerequester FOREIGN KEY (resourcerequester) REFERENCES shkresourcestable(objectid);



ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_processoid FOREIGN KEY (processoid) REFERENCES shkxpdlparticipantprocess(objectid);



ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_processoid FOREIGN KEY (processoid) REFERENCES shkxpdlapplicationprocess(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetail(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetailuser(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappuser(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_newactivitystate FOREIGN KEY (newactivitystate) REFERENCES shkactivitystateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_newprocessstate FOREIGN KEY (newprocessstate) REFERENCES shkprocessstateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_oldactivitystate FOREIGN KEY (oldactivitystate) REFERENCES shkactivitystateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_oldprocessstate FOREIGN KEY (oldprocessstate) REFERENCES shkprocessstateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentappdetail(objectid);



ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_useroid FOREIGN KEY (useroid) REFERENCES shktoolagentuser(objectid);



ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_useroid FOREIGN KEY (useroid) REFERENCES shktoolagentuser(objectid);



ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_groupid FOREIGN KEY (groupid) REFERENCES shkgrouptable(objectid);



ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_userid FOREIGN KEY (userid) REFERENCES shkusertable(objectid);



ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkpacklevelparticipant(objectid);



ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_useroid FOREIGN KEY (useroid) REFERENCES shknormaluser(objectid);



ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_participantoid FOREIGN KEY (participantoid) REFERENCES shkproclevelparticipant(objectid);



ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_useroid FOREIGN KEY (useroid) REFERENCES shknormaluser(objectid);



ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlapplicationpackage(objectid);



ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_xpdl FOREIGN KEY (xpdl) REFERENCES shkxpdls(objectid);



ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_xpdlhistory FOREIGN KEY (xpdlhistory) REFERENCES shkxpdlhistory(objectid);



ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlparticipantpackage(objectid);



ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_referringxpdl FOREIGN KEY (referringxpdl) REFERENCES shkxpdls(objectid);
