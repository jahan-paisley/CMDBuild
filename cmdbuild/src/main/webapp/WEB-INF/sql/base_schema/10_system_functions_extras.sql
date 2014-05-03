--
-- Function objects
--

CREATE OR REPLACE FUNCTION _cm_function_list(
		OUT function_name text,
		OUT function_id oid,
		OUT arg_io char[],
		OUT arg_names text[],
		OUT arg_types text[],
		OUT returns_set boolean,
		OUT comment text
	) RETURNS SETOF record AS $$
DECLARE
	R record;
	i integer;
BEGIN
	FOR R IN
		SELECT *
		FROM pg_proc
		WHERE _cm_comment_for_cmobject(oid) IS NOT NULL
	LOOP
		function_name := R.proname::text;
		function_id := R.oid;
		returns_set := R.proretset;
		comment := _cm_comment_for_cmobject(R.oid);
		IF R.proargmodes IS NULL
		THEN
			arg_io := '{}'::char[];
			arg_types := '{}'::text[];
			arg_names := '{}'::text[];
			-- add input columns
			FOR i IN SELECT generate_series(1, array_upper(R.proargtypes,1)) LOOP
				arg_io := arg_io || 'i'::char;
				arg_types := arg_types || _cm_get_sqltype_string(R.proargtypes[i], NULL);
				arg_names := arg_names || COALESCE(R.proargnames[i], '$'||i);
			END LOOP;
			-- add single output column
			arg_io := arg_io || 'o'::char;
			arg_types := arg_types || _cm_get_sqltype_string(R.prorettype, NULL);
			arg_names := arg_names || function_name;
		ELSE
			-- just normalize existing columns
			arg_io := R.proargmodes;
			arg_types := '{}'::text[];
			arg_names := R.proargnames;
			FOR i IN SELECT generate_series(1, array_upper(arg_io,1)) LOOP
				-- normalize table output
				IF arg_io[i] = 't' THEN
					arg_io[i] := 'o';
				ELSIF arg_io[i] = 'b' THEN
					arg_io[i] := 'io';
				END IF;
				arg_types := arg_types || _cm_get_sqltype_string(R.proallargtypes[i], NULL);
				IF arg_names[i] = '' THEN
					IF arg_io[i] = 'i' THEN
						arg_names[i] = '$'||i;
					ELSE
						arg_names[i] = 'column'||i;
					END IF;
				END IF;
			END LOOP;
		END IF;
		RETURN NEXT;
	END LOOP;

	RETURN;
END
$$ LANGUAGE PLPGSQL STABLE;
