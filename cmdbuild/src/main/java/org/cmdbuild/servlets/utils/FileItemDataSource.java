package org.cmdbuild.servlets.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.Validate;

public class FileItemDataSource implements DataSource {

	public static FileItemDataSource of(final FileItem fileItem) {
		Validate.notNull(fileItem, "invalid file item");
		return new FileItemDataSource(fileItem);
	}

	private final FileItem fileItem;

	private FileItemDataSource(final FileItem fileItem) {
		this.fileItem = fileItem;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return fileItem.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return fileItem.getOutputStream();
	}

	@Override
	public String getContentType() {
		return fileItem.getContentType();
	}

	@Override
	public String getName() {
		return fileItem.getName();
	}

}
