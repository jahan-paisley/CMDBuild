package org.cmdbuild.services.email;

import java.util.NoSuchElementException;

import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.model.email.Email;

public interface EmailPersistence {

	/**
	 * Gets all mail templates.
	 * 
	 * @return all mail templates.
	 */
	Iterable<EmailTemplate> getEmailTemplates();

	/**
	 * Gets all outgoing emails for the specified process' id.
	 * 
	 * @param processId
	 * 
	 * @return all outgoing emails.
	 */
	Iterable<Email> getOutgoingEmails(Long processId);

	/**
	 * Creates a new {@link Email} and returns the stored one (so with
	 * {@code Id} also).
	 * 
	 * @param email
	 *            is the {@link Email} that needs to be created.
	 * 
	 * @return the created {@link Email}.
	 */
	Email create(Email email);

	/**
	 * Saves (create or updates) the specified email.
	 * 
	 * @param email
	 * 
	 * @return the created or updated {@link Email#getId()}.
	 */
	Long save(Email email);

	/**
	 * Deletes the specified email.
	 * 
	 * @param email
	 */
	void delete(Email email);

	/**
	 * Gets all emails for the specified process' id.
	 * 
	 * @param processId
	 * 
	 * @return all email for the specified process' id.
	 */
	Iterable<Email> getEmails(Long processId);

	/**
	 * Gets the email with the specified id.
	 * 
	 * @param emailId
	 * 
	 * @return the email with the specified id.
	 * 
	 * @throws NoSuchElementException
	 *             if not found.
	 */
	Email getEmail(final Long emailId);

}
