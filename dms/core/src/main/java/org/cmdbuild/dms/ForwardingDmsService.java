package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsError;

public abstract class ForwardingDmsService implements DmsService {

	private final DmsService delegate;

	protected ForwardingDmsService(final DmsService delegate) {
		this.delegate = delegate;
	}

	@Override
	public DmsConfiguration getConfiguration() {
		return delegate.getConfiguration();
	}

	@Override
	public void setConfiguration(final DmsConfiguration configuration) {
		delegate.setConfiguration(configuration);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		return delegate.getTypeDefinitions();
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		return delegate.search(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		delegate.upload(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		return delegate.download(document);
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		delegate.delete(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsError {
		delegate.updateDescriptionAndMetadata(document);
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		return delegate.getAutoCompletionRules();
	}

	@Override
	public void clearCache() {
		delegate.clearCache();
	}

	@Override
	public void move(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		delegate.move(document, from, to);
	}

	@Override
	public void copy(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		delegate.copy(document, from, to);
	}

	@Override
	public void create(final DocumentSearch position) throws DmsError {
		delegate.create(position);
	}

	@Override
	public void delete(final DocumentSearch position) throws DmsError {
		delegate.delete(position);
	}

}
