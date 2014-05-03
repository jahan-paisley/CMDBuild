package org.cmdbuild.servlets.json.management;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;

public class ExportCSV extends JSONBaseWithSpringContext {

	@JSONExported(contentType = "text/csv")
	public DataHandler export( //
			@Parameter(SEPARATOR) final String separator, //
			@Parameter(CLASS_NAME) final String className) //
			throws IOException {
		final File csvFile = systemDataAccessLogic().exportClassAsCsvFile(className, separator);
		return createDataHandler(csvFile);
	}

	private DataHandler createDataHandler(final File file) throws FileNotFoundException, IOException {
		final FileInputStream in = new FileInputStream(file);
		final ByteArrayDataSource ds = new ByteArrayDataSource(in, "text/csv");
		ds.setName(file.getName());
		return new DataHandler(ds);
	}

}
