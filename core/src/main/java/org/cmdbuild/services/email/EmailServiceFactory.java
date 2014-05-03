package org.cmdbuild.services.email;

import org.cmdbuild.config.EmailConfiguration;

/**
 * {@link EmailService} factory class.
 */
public interface EmailServiceFactory {

	/**
	 * Creates a new {@link EmailService}.
	 * 
	 * @return the created {@link EmailService}.
	 */
	EmailService create();

	/**
	 * Creates a new {@link EmailService} with the specific
	 * {@link EmailConfiguration}.
	 * 
	 * @param configuration
	 * 
	 * @return the created {@link EmailService}.
	 */
	EmailService create(EmailConfiguration configuration);

}
