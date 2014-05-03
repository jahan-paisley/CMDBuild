package org.cmdbuild.common.mail;

import javax.mail.Message;
import javax.mail.MessagingException;

class JavaxMailUtils {

	public static final String[] NO_HEADER_FOUND = new String[0];

	public static String messageIdOf(final Message message) throws MessagingException {
		return headersOf(message, MailHeaderConstants.MESSAGE_ID)[0];
	}

	/**
	 * Gets the headers from the specified message.
	 * 
	 * @param message
	 * @param name
	 *            is the name of the header.
	 * 
	 * @return an array of found headers or
	 *         {@link JavaxMailUtils.NO_HEADER_FOUND} if no header has been
	 *         found.
	 * 
	 * @throws MessagingException
	 *             if there is any problem.
	 */
	public static String[] headersOf(final Message message, final String name) throws MessagingException {
		final String[] elements = message.getHeader(name);
		return (elements != null) ? elements : NO_HEADER_FOUND;
	}

	private JavaxMailUtils() {
		// prevents instantiation
	}

}
