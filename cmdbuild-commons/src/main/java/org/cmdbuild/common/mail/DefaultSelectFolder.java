package org.cmdbuild.common.mail;

import static org.cmdbuild.common.mail.JavaxMailUtils.messageIdOf;

import java.util.Arrays;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import org.cmdbuild.common.mail.InputTemplate.Hooks;
import org.cmdbuild.common.mail.MailApi.InputConfiguration;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

class DefaultSelectFolder implements SelectFolder {

	private final InputConfiguration configuration;
	private final String folderName;
	private final Logger logger;

	public DefaultSelectFolder(final InputConfiguration configuration, final String folder) {
		this.configuration = configuration;
		this.folderName = folder;
		this.logger = configuration.getLogger();
	}

	@Override
	public List<FetchedMail> fetch() throws MailException {
		logger.info("fetching folder '{}' for mails", folderName);
		final List<FetchedMail> fetchedMails = Lists.newArrayList();
		final InputTemplate inputTemplate = new InputTemplate(configuration);
		inputTemplate.execute(new Hooks() {

			@Override
			public void connected(final Store store) {
				try {
					final Folder folder = store.getFolder(folderName);
					folder.open(Folder.READ_ONLY);

					final List<Message> messages = Arrays.asList(folder.getMessages());
					for (final Message message : messages) {
						fetchedMails.add(transform(message));
					}
				} catch (final MessagingException e) {
					logger.error("error fetching mails", e);
					throw MailException.fetch(e);
				}
			}

		});
		return fetchedMails;
	}

	private FetchedMail transform(final Message message) throws MessagingException {
		return DefaultFetchedMail.newInstance() //
				.withId(messageIdOf(message)) //
				.withFolder(message.getFolder().getFullName()) //
				.withSubject(message.getSubject()) //
				.build();
	}

}
