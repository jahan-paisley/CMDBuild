package org.cmdbuild.servlets.json.management.export;

import org.cmdbuild.dao.entry.CMEntry;

public interface CMDataSource {

	Iterable<String> getHeaders();

	Iterable<CMEntry> getEntries();

}
