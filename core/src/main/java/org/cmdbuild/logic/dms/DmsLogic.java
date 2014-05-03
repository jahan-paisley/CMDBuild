package org.cmdbuild.logic.dms;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.logic.Logic;

public interface DmsLogic extends Logic{

	/**
	 * Gets the lookup type that represents attachment categories.
	 * 
	 * @return the name of the lookup type that represents attachment
	 *         categories.
	 */
	String getCategoryLookupType();

	/**
	 * Gets the {@link DocumentTypeDefinition} associated with the specified
	 * category.
	 * 
	 * @param category
	 *            is the {@code Code} of the {@link Lookup}.
	 * 
	 * @return the {@link DocumentTypeDefinition} for the specified category.
	 * 
	 * @throws {@link DmsException} if cannot read definitions.
	 */
	DocumentTypeDefinition getCategoryDefinition(String category);

	/**
	 * Gets all {@link DocumentTypeDefinition}s.
	 * 
	 * @return the all {@link DocumentTypeDefinition}s.
	 * 
	 * @throws DmsError
	 */
	Iterable<DocumentTypeDefinition> getCategoryDefinitions() throws DmsError;

	/**
	 * Gets the auto-completion rules for the specified class.
	 * 
	 * @param classname
	 *            the name of the class.
	 * 
	 * @return maps of metadata names and values grouped by metadata group.
	 * 
	 * @throws DmsError
	 */
	Map<String, Map<String, String>> getAutoCompletionRulesByClass(String classname) throws DmsException;

	List<StoredDocument> search(String className, Long cardId);

	void upload(String author, String className, Long cardId, InputStream inputStream, String fileName,
			String category, String description, Iterable<MetadataGroup> metadataGroups) throws IOException,
			CMDBException;

	DataHandler download(String className, Long cardId, String fileName);

	void delete(String className, Long cardId, String fileName) throws DmsException;

	void updateDescriptionAndMetadata(String className, Long cardId, String filename, String description,
			Iterable<MetadataGroup> metadataGroups);

}