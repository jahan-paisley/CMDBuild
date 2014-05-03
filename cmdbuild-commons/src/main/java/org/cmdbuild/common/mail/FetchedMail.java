package org.cmdbuild.common.mail;

/**
 * Fetched mail interface.
 */
public interface FetchedMail {

	/**
	 * Returns the message id of the fetched mail.
	 * 
	 * @return the message id of the fetched mail.
	 */
	String getId();

	/**
	 * Returns the full name of the folder containing the mail.
	 * 
	 * @return the full name of the folder containing the mail.
	 */
	String getFolder();

	/**
	 * Returns the subject of the fetched mail.
	 * 
	 * @return the subject of the fetched mail.
	 */
	String getSubject();

}
