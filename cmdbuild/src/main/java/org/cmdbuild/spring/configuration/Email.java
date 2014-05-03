package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.mail.DefaultMailApiFactory;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.data.store.email.EmailConverter;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.logic.email.DefaultEmailAccountLogic;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailAccountLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.DefaultEmailConfigurationFactory;
import org.cmdbuild.services.email.DefaultEmailPersistence;
import org.cmdbuild.services.email.DefaultEmailService;
import org.cmdbuild.services.email.DefaultSubjectHandler;
import org.cmdbuild.services.email.EmailConfigurationFactory;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Email {

	@Autowired
	private Data data;

	@Autowired
	private Dms dms;

	@Autowired
	private Notifier notifier;

	@Autowired
	private Properties properties;

	@Autowired
	private UserStore userStore;

	@Bean
	protected StorableConverter<EmailAccount> emailAccountConverter() {
		return new EmailAccountStorableConverter();
	}

	@Bean
	public Store<EmailAccount> emailAccountStore() {
		return DataViewStore.newInstance( //
				data.systemDataView(), //
				emailAccountConverter());
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailConfigurationFactory defaultEmailConfigurationFactory() {
		return new DefaultEmailConfigurationFactory(emailAccountStore());
	}

	@Bean
	public MailApiFactory mailApiFactory() {
		return new DefaultMailApiFactory();
	}

	@Bean
	public EmailPersistence emailPersistence() {
		return new DefaultEmailPersistence( //
				emailStore(), //
				emailTemplateStore());
	}

	@Bean
	protected Store<org.cmdbuild.model.email.Email> emailStore() {
		return DataViewStore.newInstance( //
				data.systemDataView(), //
				emailStorableConverter());
	}

	@Bean
	protected StorableConverter<org.cmdbuild.model.email.Email> emailStorableConverter() {
		return new EmailConverter(data.lookupStore());
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailService defaultEmailService() {
		return new DefaultEmailService( //
				defaultEmailConfigurationFactory(), //
				mailApiFactory(), //
				emailPersistence());
	}

	@Bean
	public ConfigurableEmailServiceFactory configurableEmailServiceFactory() {
		return new ConfigurableEmailServiceFactory(mailApiFactory(), emailPersistence());
	}

	@Bean
	public SubjectHandler subjectHandler() {
		return new DefaultSubjectHandler();
	}

	@Bean
	protected EmailTemplateStorableConverter emailTemplateStorableConverter() {
		return new EmailTemplateStorableConverter();
	}

	@Bean
	protected Store<EmailTemplate> emailTemplateStore() {
		return DataViewStore.newInstance(data.systemDataView(), emailTemplateStorableConverter());
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailLogic emailLogic() {
		return new EmailLogic( //
				data.systemDataView(), //
				defaultEmailConfigurationFactory(), //
				defaultEmailService(), //
				subjectHandler(), //
				properties.dmsProperties(), //
				dms.dmsService(), //
				dms.documentCreatorFactory(), //
				notifier, //
				userStore.getUser());
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailTemplateLogic emailTemplateLogic() {
		return new DefaultEmailTemplateLogic(emailTemplateStore());
	}

	@Bean
	public EmailAccountLogic emailAccountLogic() {
		return new DefaultEmailAccountLogic(emailAccountStore());
	}

}
