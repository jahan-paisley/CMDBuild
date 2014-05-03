package org.cmdbuild.services.email;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.config.EmailConfiguration;

public class ConfigurableEmailServiceFactory implements EmailServiceFactory {

	private final MailApiFactory apiFactory;
	private final EmailPersistence persistence;
	private EmailConfiguration configuration;

	public ConfigurableEmailServiceFactory(final MailApiFactory apiFactory, final EmailPersistence persistence) {
		this.apiFactory = apiFactory;
		this.persistence = persistence;
	}

	public ConfigurableEmailServiceFactory(final EmailConfiguration configuration, final MailApiFactory apiFactory,
			final EmailPersistence persistence) {
		this.apiFactory = apiFactory;
		this.persistence = persistence;
		this.configuration = configuration;
	}

	@Override
	public EmailService create() {
		return create(configuration);
	}

	@Override
	public EmailService create(final EmailConfiguration configuration) {
		Validate.notNull(apiFactory, "null api factory");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(configuration, "null configuration");
		return new DefaultEmailService(configuration, apiFactory, persistence);
	}

}
