package org.cmdbuild.dms;

import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsError;

public class CachedDmsService extends ForwardingDmsService implements LoggingSupport {

	private final DmsService dmsService;

	private Iterable<DocumentTypeDefinition> cachedDocumentTypeDefinitions;
	private AutocompletionRules cachedAutocompletionRules;

	public CachedDmsService(final DmsService dmsService) {
		super(dmsService);
		this.dmsService = dmsService;
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		synchronized (this) {
			if (cachedDocumentTypeDefinitions == null) {
				logger.info("intializing cache for document type definitions");
				cachedDocumentTypeDefinitions = super.getTypeDefinitions();
			}
			return cachedDocumentTypeDefinitions;
		}
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		synchronized (this) {
			if (cachedAutocompletionRules == null) {
				logger.info("intializing cache for autocompletion rules");
				cachedAutocompletionRules = super.getAutoCompletionRules();
			}
			return cachedAutocompletionRules;
		}
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			logger.info("clearing cache");

			/*
			 * it's so bad to store a reference to the real DMS service, but
			 * actually we need to do it because Alfresco DMS service uses an
			 * internal cache
			 */
			dmsService.clearCache();
			cachedDocumentTypeDefinitions = null;
			cachedAutocompletionRules = null;
		}
	}

}