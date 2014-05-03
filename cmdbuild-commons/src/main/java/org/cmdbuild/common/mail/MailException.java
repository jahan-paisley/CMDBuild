package org.cmdbuild.common.mail;

import java.io.IOException;

import javax.mail.MessagingException;

public class MailException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static MailException content(final MessagingException cause) {
		return new MailException(cause);
	}

	public static MailException fetch(final MessagingException cause) {
		return new MailException(cause);
	}

	public static MailException get(final MessagingException cause) {
		return new MailException(cause);
	}

	public static MailException input(final MessagingException cause) {
		return new MailException(cause);
	}

	public static MailException io(final IOException cause) {
		return new MailException(cause);
	}

	public static MailException move(final MessagingException cause) {
		return new MailException(cause);
	}

	private MailException(final Throwable cause) {
		super(cause);
	}

}
