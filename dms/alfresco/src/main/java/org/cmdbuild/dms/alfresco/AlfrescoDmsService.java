package org.cmdbuild.dms.alfresco;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dms.BaseDmsService;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.MetadataAutocompletion;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.ftp.AlfrescoFtpService;
import org.cmdbuild.dms.alfresco.utils.XmlAutocompletionReader;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoWsService;
import org.cmdbuild.dms.exception.DmsError;

public class AlfrescoDmsService extends BaseDmsService implements LoggingSupport {

	private static final AutocompletionRules NULL_AUTOCOMPLETION_RULES = new AutocompletionRules() {

		@Override
		public Iterable<String> getMetadataGroupNames() {
			return Collections.emptyList();
		}

		@Override
		public Iterable<String> getMetadataNamesForGroup(final String groupName) {
			return Collections.emptyList();
		}

		@Override
		public Map<String, String> getRulesForGroupAndMetadata(final String groupName, final String metadataName) {
			return Collections.emptyMap();
		}

	};

	private AlfrescoFtpService ftpService;
	private AlfrescoWsService wsService;

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		super.setConfiguration(configuration);

		logger.info("initializing Alfresco inner services for ftp/ws");
		ftpService = new AlfrescoFtpService(configuration);
		wsService = new AlfrescoWsService(configuration);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		return wsService.getDocumentTypeDefinitions();
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		ftpService.delete(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		return ftpService.download(document);
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		return wsService.search(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsError {
		wsService.updateDescription(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		ftpService.upload(document);
		waitForSomeTimeBetweenFtpAndWebserviceOperations();
		try {
			wsService.updateCategory(document);
			wsService.updateProperties(document);
		} catch (final Exception e) {
			final String message = format("error updating metadata for file '%s' at path '%s'", //
					document.getFileName(), document.getPath());
			logger.error(message, e);
			ftpService.delete(documentDeleteFrom(document));
			throw DmsError.forward(e);
		}
	}

	/**
	 * This is very ugly! Old tests shows some problems if Webservice operations
	 * follows immediately FTP operations, so this delay was introduced.
	 */
	private void waitForSomeTimeBetweenFtpAndWebserviceOperations() {
		try {
			Thread.sleep(getConfiguration().getDelayBetweenFtpAndWebserviceOperations());
		} catch (final InterruptedException e) {
			logger.warn("should never happen... so why?", e);
		}
	}

	private DocumentDelete documentDeleteFrom(final StorableDocument document) {
		return new DocumentDelete() {

			@Override
			public List<String> getPath() {
				return document.getPath();
			}

			@Override
			public String getClassName() {
				return document.getClassName();
			}

			@Override
			public String getCardId() {
				return document.getCardId();
			}

			@Override
			public String getFileName() {
				return document.getFileName();
			}

		};
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		try {
			final String content = getConfiguration().getMetadataAutocompletionFileContent();
			final AutocompletionRules autocompletionRules;
			if (isNotBlank(content)) {
				final MetadataAutocompletion.Reader reader = new XmlAutocompletionReader(content);
				autocompletionRules = reader.read();
			} else {
				autocompletionRules = NULL_AUTOCOMPLETION_RULES;
			}
			return autocompletionRules;
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	@Override
	public void clearCache() {
		final boolean isAlfrescoConfigured = wsService != null;
		if (isAlfrescoConfigured) {
			wsService.clearCache();
		}
	}

	@Override
	public void move(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		wsService.move(document, from, to);
	}

	@Override
	public void copy(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		wsService.copy(document, from, to);
	}

	@Override
	public void create(final DocumentSearch position) throws DmsError {
		ftpService.create(position);
	}

	@Override
	public void delete(final DocumentSearch position) throws DmsError {
		wsService.delete(position);
	}

}
