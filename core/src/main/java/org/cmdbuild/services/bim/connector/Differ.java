package org.cmdbuild.services.bim.connector;

public interface Differ {

	public void findDifferences(final ImportDifferListener listener) throws Exception;

}
