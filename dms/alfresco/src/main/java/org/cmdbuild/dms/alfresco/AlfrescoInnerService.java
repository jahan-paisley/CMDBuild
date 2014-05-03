package org.cmdbuild.dms.alfresco;

import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;

public abstract class AlfrescoInnerService implements LoggingSupport {

	protected final DmsConfiguration configuration;

	public AlfrescoInnerService(final DmsConfiguration configuration) {
		Validate.notNull(configuration, "null configuration");
		this.configuration = configuration;
	}

	public void delete(final DocumentDelete document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public DataHandler download(final DocumentDownload document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public void updateDescription(final DocumentUpdate document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public void upload(final StorableDocument document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public void updateProperties(final StorableDocument document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public void updateCategory(final StorableDocument document) throws DmsError {
		throw new UnsupportedOperationException();
	}

	public Iterable<DocumentTypeDefinition> getDocumentTypeDefinitions() throws DmsError {
		throw new UnsupportedOperationException();
	}

	public void updateMetadata(final StorableDocument document) {
		throw new UnsupportedOperationException();
	}

	public void clearCache() {
		throw new UnsupportedOperationException();
	}

	public void move(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		throw new UnsupportedOperationException();
	}

}
