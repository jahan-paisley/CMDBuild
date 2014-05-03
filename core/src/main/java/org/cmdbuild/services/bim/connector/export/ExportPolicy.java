package org.cmdbuild.services.bim.connector.export;

public interface ExportPolicy {

	/**
	 * returns the id of the project for export
	 * **/
	String createProjectForExport(String projectId);

	/**
	 * returns the id of the project for export
	 * **/
	String updateProjectForExport(String projectId);

	/**
	 * performs preliminary operations for the connector
	 * */
	void beforeExport(String exportProjectId);

	/**
	 * tells if the connector execution must be forced whenever its output is
	 * required
	 * */
	boolean forceUpdate();

}
