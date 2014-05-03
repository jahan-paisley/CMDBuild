package org.cmdbuild.dms;

import java.io.InputStream;

public interface StorableDocument extends Document, DocumentWithMetadata {

	String getAuthor();

	InputStream getInputStream();

	String getFileName();

	String getCategory();

	String getDescription();

}
