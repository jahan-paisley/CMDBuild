package org.cmdbuild.common.mail;

/**
 * Select mail interface.
 */
public interface SelectMail {

	/**
	 * Gets the selected mail.
	 * 
	 * @return a new {@link GetMail} object.
	 * 
	 * @throws MailException
	 *             if there is any problem.
	 */
	GetMail get() throws MailException;

	/**
	 * Selects a target folder.
	 * 
	 * @param folder
	 *            is the selected folder.
	 * 
	 * @return the {@link SelectMail} object itself.
	 */
	SelectMail selectTargetFolder(final String folder);

	/**
	 * Moves current mail into target folder.
	 * 
	 * @throws MailException
	 *             if there is any problem.
	 */
	void move() throws MailException;

}
