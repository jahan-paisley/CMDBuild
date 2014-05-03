package org.cmdbuild.services.bim.connector.export;

public interface ConnectorFramework {
	
	boolean isSynch(Object input);
	
	void executeConnector(Object input, Output output);

	Object getLastGeneratedOutput(Object input);
	
}
