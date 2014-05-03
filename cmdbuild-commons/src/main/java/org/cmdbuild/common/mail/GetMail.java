package org.cmdbuild.common.mail;

import javax.activation.DataHandler;

/**
 * Get mail interface.
 */
public interface GetMail extends FetchedMail {

	/**
	 * Mail attachment interface.
	 */
	interface Attachment {

		/**
		 * Gets the attachment's name.
		 * 
		 * @return the attachment's name.
		 */
		String getName();

		/**
		 * Gets the attachment's {@link DataHandler}.
		 * 
		 * @return the attachment's {@link DataHandler}.
		 */
		DataHandler getDataHandler();

	}

	/**
	 * Gets mail's FROM address.
	 * 
	 * @return mail's FROM address.
	 */
	String getFrom();

	/**
	 * Gets mail's TO addresses.
	 * 
	 * @return all mail's TO addresses.
	 */
	Iterable<String> getTos();

	/**
	 * Gets mail's CC addresses.
	 * 
	 * @return all mail's CC addresses.
	 */
	Iterable<String> getCcs();

	/**
	 * Gets mail's content.
	 * 
	 * @return the mail's content.
	 */
	String getContent();

	/**
	 * Gets mail's attachments.
	 * 
	 * @return all mail's attachments.
	 */
	Iterable<Attachment> getAttachments();

}
