package org.cmdbuild.logic.email;

import org.cmdbuild.logic.Logic;

public interface EmailAccountLogic extends Logic {

	interface Account {

		Long getId();

		String getName();

		boolean isDefault();

		String getUsername();

		String getPassword();

		String getAddress();

		String getSmtpServer();

		Integer getSmtpPort();

		boolean isSmtpSsl();

		String getImapServer();

		Integer getImapPort();

		boolean isImapSsl();

		String getInputFolder();

		String getProcessedFolder();

		String getRejectedFolder();

		boolean isRejectNotMatching();

	}

	/**
	 * Creates the specified account.
	 *
	 * @param account
	 *            is the {@link Account} that needs to be created.
	 *
	 * @return the id of the created {@link Account}.
	 *
	 * @throws RuntimeException
	 *             if there is any problem.
	 */

	Long create(Account account);

	/**
	 * Updates the specified account.
	 *
	 * @param account
	 *            is the {@link Account} that needs to be updated.
	 *
	 * @throws RuntimeException
	 *             if there is any problem.
	 */
	void update(Account account);

	/**
	 * Gets all available accounts.
	 *
	 * @return all {@link Account}s.
	 */
	Iterable<Account> getAll();

	/**
	 * Gets the {@link Account} for the specified name.
	 *
	 * @param name
	 *
	 * @return the {@link Account} for the specified id.
	 *
	 * @throws RuntimeException
	 *             if there is any problem.
	 */
	Account getAccount(String name);

	/**
	 * Deletes the {@link Account} for the specified name.
	 *
	 * @param name
	 *
	 * @throws RuntimeException
	 *             if there is any problem.
	 */
	void delete(String name);

	/**
	 * Sets to default the {@link Account} with the specified name.
	 *
	 * @param name
	 *
	 * @throws RuntimeException
	 *             if there is any problem.
	 */
	void setDefault(String name);

}
