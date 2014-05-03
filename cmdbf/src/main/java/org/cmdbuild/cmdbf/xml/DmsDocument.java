package org.cmdbuild.cmdbf.xml;

import java.io.InputStream;

import org.cmdbuild.dms.StoredDocument;

public class DmsDocument extends StoredDocument {
	private InputStream inputStream;

	public DmsDocument() {
	}

	public DmsDocument(final StoredDocument document, final InputStream inputStream) {
		setAuthor(document.getAuthor());
		setDescription(document.getDescription());
		setName(document.getName());
		setPath(document.getPath());
		setUuid(document.getUuid());
		setVersion(document.getVersion());
		setCategory(document.getCategory());
		setCreated(document.getCreated());
		setModified(document.getModified());
		setMetadataGroups(document.getMetadataGroups());
		setInputStream(inputStream);
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(final InputStream inputStream) {
		this.inputStream = inputStream;
	}
}
