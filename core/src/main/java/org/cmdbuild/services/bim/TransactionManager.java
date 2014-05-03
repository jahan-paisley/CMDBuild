package org.cmdbuild.services.bim;

// TODO move away
public interface TransactionManager {

	/**
	 * Opens a new transaction.
	 * @param projectId 
	 * 
	 * @throws IllegalStateException
	 *             if there is already a current transaction.
	 */
	void open(String projectId);
	
	boolean hasTransaction();

	/**
	 * Returns the id of the current transaction.
	 * 
	 * @throws IllegalStateException
	 *             if there is no current transaction.
	 */
	String getId();

	/**
	 * Commits all operations performed within the current transaction and
	 * returns an identifier of the revision (after the commit).
	 * 
	 * @throws IllegalStateException
	 *             if there is no current transaction.
	 */
	String commit();

	/**
	 * @throws IllegalStateException
	 *             if there is no current transaction.
	 */
	void abort();

}