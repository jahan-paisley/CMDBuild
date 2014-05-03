package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.ForwardingDmsService;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.Email.EmailStatus;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailConfigurationFactory;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EmailLogic implements Logic {

	public static class Upload {

		public static class Builder implements org.cmdbuild.common.Builder<Upload> {

			private String identifier;
			private DataHandler dataHandler;
			private boolean temporary;

			private Builder() {
				// prevents direct instantiation
			}

			@Override
			public Upload build() {
				validate();
				return new Upload(this);
			}

			private void validate() {
				Validate.notNull(dataHandler, "invalid data handler");
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withDataHandler(final DataHandler dataHandler) {
				this.dataHandler = dataHandler;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

		}

		public static Builder newUpload() {
			return new Builder();
		}

		public final String identifier;
		public final DataHandler dataHandler;
		public final boolean temporary;

		private Upload(final Builder builder) {
			this.identifier = builder.identifier;
			this.dataHandler = builder.dataHandler;
			this.temporary = builder.temporary;
		}

	}

	public static class Delete {

		public static class Builder implements org.cmdbuild.common.Builder<Delete> {

			private String identifier;
			private String fileName;
			private boolean temporary;

			private Builder() {
				// prevents direct instantiation
			}

			@Override
			public Delete build() {
				validate();
				return new Delete(this);
			}

			private void validate() {
				Validate.notNull(fileName, "invalid file name");
				Validate.notEmpty(fileName, "invalid file name");
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withFileName(final String fileName) {
				this.fileName = fileName;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

		}

		public static Builder newDelete() {
			return new Builder();
		}

		public final String identifier;
		public final String fileName;
		public final boolean temporary;

		private Delete(final Builder builder) {
			this.identifier = builder.identifier;
			this.fileName = builder.fileName;
			this.temporary = builder.temporary;
		}

	}

	public static class CopiableAttachment {

		public static class Builder implements org.cmdbuild.common.Builder<CopiableAttachment> {

			private String className;
			private Long cardId;
			private String fileName;

			private Builder() {
				// prevents instantiation
			}

			@Override
			public CopiableAttachment build() {
				return new CopiableAttachment(this);
			}

			public Builder withClassName(final String className) {
				this.className = className;
				return this;
			}

			public Builder withCardId(final Long cardId) {
				this.cardId = cardId;
				return this;
			}

			public Builder withFileName(final String fileName) {
				this.fileName = fileName;
				return this;
			}

		}

		public static Builder newCopy() {
			return new Builder();
		}

		public final String className;
		public final Long cardId;
		public final String fileName;

		private CopiableAttachment(final Builder builder) {
			this.className = builder.className;
			this.cardId = builder.cardId;
			this.fileName = builder.fileName;
		}

	}

	public static class Copy {

		public static class Builder implements org.cmdbuild.common.Builder<Copy> {

			private String identifier;
			private boolean temporary;
			private Iterable<CopiableAttachment> attachments;

			private Builder() {
				// prevents direct instantiation
				attachments = Lists.newArrayList();
			}

			@Override
			public Copy build() {
				return new Copy(this);
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

			public Builder withAllAttachments(final Iterable<CopiableAttachment> attachments) {
				this.attachments = attachments;
				return this;
			}

		}

		public static Builder newCopy() {
			return new Builder();
		}

		public final String identifier;
		public final boolean temporary;
		private final Iterable<CopiableAttachment> attachments;

		private Copy(final Builder builder) {
			this.identifier = builder.identifier;
			this.temporary = builder.temporary;
			this.attachments = builder.attachments;
		}

		public Builder modify() {
			return newCopy() //
					.withIdentifier(identifier) //
					.withTemporaryStatus(temporary) //
					.withAllAttachments(attachments);
		}

	}

	public static class EmailWithAttachmentNames {

		private final Email email;
		private final Iterable<String> attachmentNames;

		EmailWithAttachmentNames(final Email email, final Iterable<String> attachmentNames) {
			this.email = email;
			this.attachmentNames = attachmentNames;
		}

		public Email getEmail() {
			return email;
		}

		public Iterable<String> getAttachmentNames() {
			return attachmentNames;
		}

	}

	public static class EmailSubmission extends Email {

		private String temporaryId;

		public EmailSubmission() {
			super();
		}

		public EmailSubmission(final long id) {
			super(id);
		}

		public String getTemporaryId() {
			return temporaryId;
		}

		public void setTemporaryId(final String temporaryId) {
			this.temporaryId = temporaryId;
		}

	}

	private static class ConfigurationAwareDmsService extends ForwardingDmsService {

		private static final List<StoredDocument> EMPTY = Collections.emptyList();

		private final DmsConfiguration dmsConfiguration;

		public ConfigurationAwareDmsService(final DmsService dmsService, final DmsConfiguration dmsConfiguration) {
			super(dmsService);
			this.dmsConfiguration = dmsConfiguration;
		}

		@Override
		public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
			return dmsConfiguration.isEnabled() ? super.search(document) : EMPTY;
		}

	}

	private static final Function<Email, Long> EMAIL_ID_FUNCTION = new Function<Email, Long>() {

		@Override
		public Long apply(final Email input) {
			return input.getId();
		}

	};

	// TODO do in a better way
	private static final boolean TEMPORARY = true;
	private static final boolean FINAL = false;

	private static final EmailStatus MISSING_STATUS = null;

	private static final Collection<EmailStatus> SAVEABLE_STATUSES = Arrays.asList(EmailStatus.DRAFT, MISSING_STATUS);

	private final CMDataView view;
	private final EmailConfigurationFactory configurationFactory;
	private final EmailService service;
	private final SubjectHandler subjectHandler;
	private final DmsConfiguration dmsConfiguration;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final Notifier notifier;
	private final OperationUser operationUser;

	public EmailLogic( //
			final CMDataView dataView, //
			final EmailConfigurationFactory configurationFactory, //
			final EmailService service, //
			final SubjectHandler subjectHandler, //
			final DmsConfiguration dmsConfiguration, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final Notifier notifier, //
			final OperationUser operationUser //
	) {
		this.view = dataView;
		this.configurationFactory = configurationFactory;
		this.service = service;
		this.subjectHandler = subjectHandler;
		this.dmsConfiguration = dmsConfiguration;
		this.dmsService = new ConfigurationAwareDmsService(dmsService, dmsConfiguration);
		this.documentCreatorFactory = documentCreatorFactory;
		this.notifier = notifier;
		this.operationUser = operationUser;
	}

	public Iterable<EmailWithAttachmentNames> getEmails(final Long processCardId) {
		final Function<Email, EmailWithAttachmentNames> TO_EMAIL_WITH_ATTACHMENT_NAMES = new Function<Email, EmailWithAttachmentNames>() {

			@Override
			public EmailWithAttachmentNames apply(final Email input) {
				final List<String> attachmentNames = Lists.newArrayList();
				try {
					final DocumentSearch allDocuments = documentCreator(FINAL) //
							.createDocumentSearch(EMAIL_CLASS_NAME, input.getIdentifier());
					for (final StoredDocument document : dmsService.search(allDocuments)) {
						attachmentNames.add(document.getName());
					}
				} catch (final DmsError e) {
					logger.warn("error getting attachments for email '{}', ignoring it", input.getId());
					logger.warn("... cause was", e);
				}
				return new EmailWithAttachmentNames(input, attachmentNames);
			}

		};
		return from(service.getEmails(processCardId)) //
				.transform(TO_EMAIL_WITH_ATTACHMENT_NAMES);
	}

	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		final EmailConfiguration configuration = configurationFactory.create();
		for (final Email email : service.getOutgoingEmails(processCardId)) {
			if (isEmpty(email.getFromAddress())) {
				email.setFromAddress(configuration.getEmailAddress());
			}
			try {
				// FIXME really needed here?
				email.setSubject(defaultIfBlank(subjectHandler.compile(email).getSubject(), EMPTY));
				service.send(email, attachmentsOf(email));
				email.setStatus(EmailStatus.SENT);
			} catch (final CMDBException e) {
				notifier.warn(e);
				email.setStatus(EmailStatus.OUTGOING);
			} catch (final Throwable e) {
				notifier.warn(CMDBWorkflowException.WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException());
				email.setStatus(EmailStatus.OUTGOING);
			}
			service.save(email);
		}
	}

	private Map<URL, String> attachmentsOf(final Email email) {
		logger.debug("getting attachments of email {}", email.getId());
		final Map<URL, String> attachments = Maps.newHashMap();
		try {
			final String className = EMAIL_CLASS_NAME;
			final DocumentCreator documentCreator = documentCreator(FINAL);
			final String emailId = email.getId().toString();
			final DocumentSearch allDocuments = documentCreator //
					.createDocumentSearch( //
							className, //
							emailId);
			for (final StoredDocument storedDocument : dmsService.search(allDocuments)) {
				logger.debug("downloading attachment with name '{}'", storedDocument.getName());
				final DocumentDownload document = documentCreator //
						.createDocumentDownload( //
								className, //
								emailId, //
								storedDocument.getName());
				final DataHandler dataHandler = dmsService.download(document);
				final TempDataSource tempDataSource = TempDataSource.create(storedDocument.getName());
				copy(dataHandler, tempDataSource);
				final URL url = tempDataSource.getFile().toURI().toURL();
				attachments.put(url, storedDocument.getName());
			}
		} catch (final DmsError e) {
			logger.error("error getting attachment from DMS", e);
		} catch (final IOException e) {
			logger.error("i/o error", e);
		}
		return attachments;
	}

	private void copy(final DataHandler from, final DataSource to) throws IOException {
		final InputStream inputStream = from.getInputStream();
		final OutputStream outputStream = to.getOutputStream();
		IOUtils.copy(inputStream, outputStream);
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
	}

	/**
	 * Deletes all {@link Email}s with the specified id and for the specified
	 * process' id. Only draft mails can be deleted.
	 */
	public void deleteEmails(final Long processCardId, final Iterable<Long> emailIds) {
		if (isEmpty(emailIds)) {
			return;
		}
		final Map<Long, Email> storedEmails = storedEmailsById(processCardId);
		for (final Long emailId : emailIds) {
			final Email found = storedEmails.get(emailId);
			Validate.notNull(found, "email not found");
			Validate.isTrue(SAVEABLE_STATUSES.contains(found.getStatus()), "specified email have no compatible status");
			service.delete(found);
		}
	}

	/**
	 * Saves all specified {@link EmailSubmission}s for the specified process'
	 * id. Only draft mails can be saved, others are skipped.
	 */
	public void saveEmails(final Long processCardId, final Iterable<EmailSubmission> emails) {
		if (isEmpty(emails)) {
			return;
		}
		final Map<Long, Email> storedEmails = storedEmailsById(processCardId);
		for (final EmailSubmission emailSubmission : emails) {
			final Email alreadyStoredEmailSubmission = storedEmails.get(emailSubmission.getId());
			final boolean alreadyStored = (alreadyStoredEmailSubmission != null);
			final Email maybeUpdateable = alreadyStored ? alreadyStoredEmailSubmission : emailSubmission;
			if (SAVEABLE_STATUSES.contains(maybeUpdateable.getStatus())) {
				maybeUpdateable.setActivityId(processCardId);
				final Long savedId = service.save(maybeUpdateable);
				if (!alreadyStored) {
					moveAttachmentsFromTemporaryToFinalPosition(emailSubmission.getTemporaryId(), savedId.toString());
				}
			}

		}
	}

	private void moveAttachmentsFromTemporaryToFinalPosition(final String sourceIdentifier,
			final String destinationIdentifier) {
		logger.debug("moving attachments from temporary '{}' to final '{}' position");
		try {
			final String temporaryId = sourceIdentifier;
			final DocumentSearch from = documentCreator(TEMPORARY) //
					.createDocumentSearch(EMAIL_CLASS_NAME, temporaryId);
			final DocumentSearch to = documentCreator(FINAL) //
					.createDocumentSearch(EMAIL_CLASS_NAME, destinationIdentifier);
			dmsService.create(to);
			for (final StoredDocument storedDocument : dmsService.search(from)) {
				dmsService.move(storedDocument, from, to);
			}
			dmsService.delete(from);
		} catch (final DmsError e) {
			logger.error("error moving attachments");
		}
	}

	private Map<Long, Email> storedEmailsById(final Long processCardId) {
		return Maps.uniqueIndex(service.getEmails(processCardId), EMAIL_ID_FUNCTION);
	}

	public String uploadAttachment( //
			final Upload.Builder builder//
	) throws IOException, CMDBException {
		return uploadAttachment(builder.build());
	}

	public String uploadAttachment( //
			final Upload upload //
	) throws IOException, CMDBException {
		InputStream inputStream = null;
		try {
			inputStream = upload.dataHandler.getInputStream();
			final String usableIdentifier = (upload.identifier == null) ? generateIdentifier() : upload.identifier;
			final StorableDocument document = documentCreator(upload.temporary) //
					.createStorableDocument( //
							operationUser.getAuthenticatedUser().getUsername(), //
							EMAIL_CLASS_NAME, //
							usableIdentifier, //
							inputStream, //
							upload.dataHandler.getName(), //
							dmsConfiguration.getLookupNameForAttachments(), //
							EMPTY);
			dmsService.upload(document);
			return usableIdentifier;
		} catch (final Exception e) {
			logger.error("error uploading document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public void deleteAttachment( //
			final Delete.Builder builder //
	) throws CMDBException {
		deleteAttachment(builder.build());
	}

	public void deleteAttachment( //
			final Delete delete //
	) throws CMDBException {
		try {
			final String usableIdentifier = (delete.identifier == null) ? generateIdentifier() : delete.identifier;
			final DocumentDelete document = documentCreator(delete.temporary) //
					.createDocumentDelete( //
							EMAIL_CLASS_NAME, //
							usableIdentifier, //
							delete.fileName);
			dmsService.delete(document);
		} catch (final Exception e) {
			logger.error("error deleting document");
			throw DmsException.Type.DMS_ATTACHMENT_DELETE_ERROR.createException();
		}
	}

	public Copy copyAttachments( //
			final Copy.Builder builder //
	) throws CMDBException {
		return copyAttachments(builder.build());
	}

	public Copy copyAttachments( //
			final Copy copy //
	) throws CMDBException {
		try {
			final String usableIdentifier = (copy.identifier == null) ? generateIdentifier() : copy.identifier;
			final DocumentSearch destination = documentCreator(copy.temporary) //
					.createDocumentSearch(EMAIL_CLASS_NAME, usableIdentifier);
			dmsService.create(destination);
			final Map<String, List<CopiableAttachment>> attachmentsByClass = mapByClass(copy.attachments);
			for (final List<CopiableAttachment> attachments : attachmentsByClass.values()) {
				for (final CopiableAttachment attachment : attachments) {
					copyAttachment(attachment, destination);
				}
			}
			return copy.modify() //
					.withIdentifier(usableIdentifier) //
					.build();
		} catch (final Exception e) {
			logger.error("error copying document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		}
	}

	private Map<String, List<CopiableAttachment>> mapByClass(final Iterable<CopiableAttachment> attachments) {
		final Map<String, List<CopiableAttachment>> map = Maps.newHashMap();
		for (final CopiableAttachment attachment : attachments) {
			final String className = attachment.className;
			final List<CopiableAttachment> attachmentsWithSameClassName;
			if (!map.containsKey(className)) {
				map.put(className, Lists.<CopiableAttachment> newArrayList());
			}
			attachmentsWithSameClassName = map.get(className);
			attachmentsWithSameClassName.add(attachment);
		}
		return map;
	}

	private void copyAttachment(final CopiableAttachment attachment, final DocumentSearch destination) throws DmsError {
		final CMClass sourceClass = view.findClass(attachment.className);
		final DocumentSearch source = documentCreatorFactory.create(sourceClass) //
				.createDocumentSearch(attachment.className, attachment.cardId.toString());
		for (final StoredDocument storedDocument : dmsService.search(source)) {
			if (storedDocument.getName().equals(attachment.fileName)) {
				dmsService.copy(storedDocument, source, destination);
			}
		}
	}

	private String generateIdentifier() {
		return UUID.randomUUID().toString();
	}

	private DocumentCreator documentCreator(final boolean temporary) {
		final DocumentCreator documentCreator;
		if (temporary) {
			documentCreator = documentCreatorFactory.createTemporary(Arrays.asList(EMAIL_CLASS_NAME));
		} else {
			final CMClass emailClass = view.findClass(EMAIL_CLASS_NAME);
			documentCreator = documentCreatorFactory.create(emailClass);
		}
		return documentCreator;
	}

}
