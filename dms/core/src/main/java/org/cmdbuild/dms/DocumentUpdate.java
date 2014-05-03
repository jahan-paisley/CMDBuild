package org.cmdbuild.dms;

public interface DocumentUpdate extends Document, DocumentWithMetadata {

	String getFileName();

	String getDescription();

}
