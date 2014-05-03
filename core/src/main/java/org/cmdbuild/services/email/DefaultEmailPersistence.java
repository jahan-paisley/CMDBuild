package org.cmdbuild.services.email;

import static com.google.common.collect.FluentIterable.from;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailOwnerGroupable;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class DefaultEmailPersistence implements EmailPersistence {

	private static final Logger logger = Log.PERSISTENCE;

	private static class EmailIdPredicate implements Predicate<Email> {

		public static EmailIdPredicate of(final Long id) {
			return new EmailIdPredicate(id);
		}

		private final Long id;

		public EmailIdPredicate(final Long id) {
			Validate.notNull(id, "null id");
			this.id = id;
		}

		@Override
		public boolean apply(final Email input) {
			return id.equals(input.getId());
		}

	}

	private static class DraftAndOutgoingEmails implements Predicate<Email> {

		@Override
		public boolean apply(final Email input) {
			return (EmailStatus.DRAFT.equals(input.getStatus()) || EmailStatus.OUTGOING.equals(input.getStatus()));
		}

	}

	private static DraftAndOutgoingEmails DRAFT_AND_OUTGOING_EMAILS = new DraftAndOutgoingEmails();

	private final Store<Email> emailStore;
	private final Store<EmailTemplate> emailTemplateStore;

	public DefaultEmailPersistence(final Store<Email> emailStore, final Store<EmailTemplate> emailTemplateStore) {
		this.emailStore = emailStore;
		this.emailTemplateStore = emailTemplateStore;
	}

	@Override
	public Iterable<EmailTemplate> getEmailTemplates() {
		logger.info("getting all email templates");
		return emailTemplateStore.list();
	}

	@Override
	public Iterable<Email> getOutgoingEmails(final Long processId) {
		logger.info("getting all outgoing emails for process with id '{}'", processId);
		return from(getEmails(processId)) //
				.filter(DRAFT_AND_OUTGOING_EMAILS);
	}

	@Override
	public Email create(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Storable storable = emailStore.create(email);
		final Email storedEmail = emailStore.read(storable);
		return storedEmail;
	}

	@Override
	public Long save(final Email email) {
		logger.info("saving email with id '{}' and process' id '{}'", email.getId(), email.getActivityId());
		final Long processCardId = email.getActivityId();
		email.setActivityId(processCardId);
		final Long id;
		if (email.getId() == null) {
			logger.debug("creating new email");
			/*
			 * FIXME
			 * 
			 * Awful hack needed for fix a bug related to legacy e-mail
			 * management. Persistence should not be responsible for setting a
			 * status, the code that uses it should!
			 */
			if (email.getStatus() != EmailStatus.RECEIVED) {
				email.setStatus(EmailStatus.DRAFT);
			}
			final Storable storable = emailStore.create(email);
			id = Long.valueOf(storable.getIdentifier());
		} else {
			logger.debug("updating existing email");
			emailStore.update(email);
			id = email.getId();
		}
		return id;
	}

	@Override
	public void delete(final Email email) {
		logger.info("deleting email with id '{}'", email.getId());
		final Optional<Email> optional = from(emailStore.list()) //
				.filter(EmailIdPredicate.of(email.getId())) //
				.first();
		if (optional.isPresent()) {
			emailStore.delete(optional.get());
		} else {
			logger.warn("deleting email with id '{}' not found", email.getId());
		}
	}

	@Override
	public Email getEmail(final Long emailId) {
		logger.info("getting email with id '{}'", emailId);
		final Email email = emailStore.read(new Storable() {

			@Override
			public String getIdentifier() {
				return emailId.toString();
			}

		});
		return email;
	}

	@Override
	public Iterable<Email> getEmails(final Long processId) {
		logger.info("getting all emails for process' id '{}'", processId);
		return from(emailStore.list(EmailOwnerGroupable.of(processId)));
	}

}
