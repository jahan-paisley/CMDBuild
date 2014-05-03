package org.cmdbuild.common.mail;

import org.cmdbuild.common.mail.MailApi.Configuration;

/**
 * {@link MailApi} factory class.
 */
public abstract class MailApiFactory {

	private Configuration configuration;

	/**
	 * Gets {@link MailApi} configuration.
	 * 
	 * @return {@link MailApi} configuration.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Sets {@link MailApi} configuration.
	 * 
	 * Specifying a new configuration doesn't change already created
	 * {@link MailApi}s.
	 * 
	 * @param configuration
	 *            is the new configuration.
	 */
	public void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Creates a new instance of {@link MailApi} based on the current
	 * configuration.
	 * 
	 * @return a new {@link MailApi} instance.
	 */
	public abstract MailApi createMailApi();

}
