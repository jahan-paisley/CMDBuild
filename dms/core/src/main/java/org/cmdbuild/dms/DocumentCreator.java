package org.cmdbuild.dms;

import java.io.InputStream;

public interface DocumentCreator {

	DocumentSearch createDocumentSearch( //
			String className, //
			String cardId);

	StorableDocument createStorableDocument( //
			String author, //
			String className, //
			String cardId, //
			InputStream inputStream, //
			String fileName, //
			String category, //
			String description);

	StorableDocument createStorableDocument( //
			String author, //
			String className, //
			String cardId, //
			InputStream inputStream, //
			String fileName, //
			String category, //
			String description, //
			Iterable<MetadataGroup> metadataGroups);

	DocumentDownload createDocumentDownload( //
			String className, //
			String cardId, //
			String fileName);

	DocumentDelete createDocumentDelete( //
			String className, //
			String cardId, //
			String fileName);

	DocumentUpdate createDocumentUpdate( //
			String className, //
			String cardId, //
			String filename, //
			String description);

	DocumentUpdate createDocumentUpdate( //
			String className, //
			String cardId, //
			String filename, //
			String description, //
			Iterable<MetadataGroup> metadataGroups);

}
