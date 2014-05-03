package org.cmdbuild.dms;

import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DmsService {

	public interface LoggingSupport {

		/*
		 * should be better to use dms service class name, but for backward
		 * compatibility...
		 */
		Logger logger = LoggerFactory.getLogger("dms");

	}

	/**
	 * Gets the {@link DmsConfiguration}.
	 * 
	 * @return the actual {@link DmsConfiguration}.
	 */
	DmsConfiguration getConfiguration();

	/**
	 * Sets the {@link DmsConfiguration}.
	 */
	void setConfiguration(DmsConfiguration configuration);

	/**
	 * Gets all {@link DocumentTypeDefinition}s.
	 * 
	 * @return all {@link DocumentTypeDefinition}s.
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError;

	/**
	 * Search for all documents matching the specified query.
	 * 
	 * @param document
	 *            the document query parameters.
	 * 
	 * @return the list found documents (never null).
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	List<StoredDocument> search(DocumentSearch document) throws DmsError;

	/**
	 * Upload the specified document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be upload.
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void upload(StorableDocument document) throws DmsError;

	/**
	 * Downloads the specified document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be downloaded.
	 * 
	 * @return the {@link DataHandler} associated with the document.
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	DataHandler download(DocumentDownload document) throws DmsError;

	/**
	 * Deletes the specified document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be deleted.
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void delete(DocumentDelete document) throws DmsError;

	/**
	 * Updates the description of an existing document.
	 * 
	 * @param document
	 *            the definition for the document that needs to be updated.
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void updateDescriptionAndMetadata(DocumentUpdate document) throws DmsError;

	/**
	 * Gets the auto-completion rules.
	 * 
	 * @return the auto-completion rules.
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	AutocompletionRules getAutoCompletionRules() throws DmsError;

	/**
	 * Clears cache (if supported).
	 */
	public void clearCache();

	/**
	 * Moves a {@link StoredDocument} from a {@link DocumentSearch} position to
	 * a {@link DocumentSearch} position.
	 * 
	 * @param document
	 * @param from
	 * @param to
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void move(StoredDocument document, DocumentSearch from, DocumentSearch to) throws DmsError;

	/**
	 * Copies a {@link StoredDocument} from a {@link DocumentSearch} position to
	 * a {@link DocumentSearch} position.
	 * 
	 * @param document
	 * @param from
	 * @param to
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void copy(StoredDocument document, DocumentSearch from, DocumentSearch to) throws DmsError;

	/**
	 * Creates the specified position if missing.
	 * 
	 * @param position
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void create(DocumentSearch position) throws DmsError;

	/**
	 * Deletes the specified position.
	 * 
	 * @param position
	 * 
	 * @throws DmsError
	 *             if something goes wrong.
	 */
	void delete(DocumentSearch position) throws DmsError;

}
