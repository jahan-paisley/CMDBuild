package org.cmdbuild.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;

public class TempDataSource implements DataSource {

	public static TempDataSource create(final String name, final String contentType) throws IOException {
		return new TempDataSource(name, contentType);
	}

	public static TempDataSource create(final String name) throws IOException {
		return new TempDataSource(name, null);
	}

	private static final String PREFIX = "tempdatasource";

	private final File file;
	private final String name;
	private final String contentType;

	private TempDataSource(final String name, final String contentType) throws IOException {
		this.name = name;
		this.contentType = contentType;
		this.file = File.createTempFile(PREFIX, name);
		file.deleteOnExit();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			file.delete();
		} finally {
			super.finalize();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContentType() {
		return (contentType == null) ? new MimetypesFileTypeMap().getContentType(file) : contentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}

	public File getFile() {
		return file;
	}

}
