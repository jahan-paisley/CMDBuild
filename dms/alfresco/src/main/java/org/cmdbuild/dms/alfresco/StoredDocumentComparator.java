package org.cmdbuild.dms.alfresco;

import java.util.Comparator;

import org.cmdbuild.dms.StoredDocument;

public class StoredDocumentComparator implements Comparator<StoredDocument> {

	private StoredDocumentComparator() {
		// prevents instantiation
	}

	public static final Comparator<StoredDocument> INSTANCE = new StoredDocumentComparator();

	@Override
	public int compare(final StoredDocument document0, final StoredDocument document1) {
		return document0.getName().compareTo(document1.getName());
	}

}
