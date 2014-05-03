package org.cmdbuild.services.template.engine;

public class EngineNames {

	public static final String CLIENT = "client";
	public static final String DB = "db";
	public static final String DB_TEMPLATE = "dbtmpl";
	public static final String DS = "ds";
	public static final String FORM = "form";
	public static final String PARM = "parm";

	public static final String[] ALL_DATA_SOURCES = new String[] { DB, DS };
	public static final String[] ALL_PARAMETERS = new String[] { FORM, PARM };

	private EngineNames() {
		// prevents instantiation
	}

}
