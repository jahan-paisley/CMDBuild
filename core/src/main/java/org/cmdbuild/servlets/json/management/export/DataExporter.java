package org.cmdbuild.servlets.json.management.export;

import java.io.File;

public interface DataExporter {

	File export(CMDataSource source);

}
