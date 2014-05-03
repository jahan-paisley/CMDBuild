package org.cmdbuild.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

public interface FilesStore {

	String[] list(final String dir);

	String[] list(final String dir, final String pattern);

	File[] listFiles(final String dir, final String pattern);

	void remove(final String filePath);

	void rename(final String filePath, String newFilePath);

	void save(final FileItem file, final String filePath) throws IOException;

	void save(final InputStream inputStream, final String filePath) throws IOException;

	String getRelativeRootDirectory();

	String getAbsoluteRootDirectory();

	File getFile(final String path);

	boolean isImage(final FileItem file);

	String getExtension(final String fileName);

	String removeExtension(final String fileName);

}
