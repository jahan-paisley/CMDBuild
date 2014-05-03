package org.cmdbuild.services;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.utils.PatternFilenameFilter;

// TODO do it better
public class WebInfFilesStore implements FilesStore {

	private final String relativeRootDirectory = "WEB-INF" + File.separator;
	private final String absoluteRootDirectory = Settings.getInstance().getRootPath() + relativeRootDirectory;

	@Override
	public String[] list(final String dir) {
		return list(dir, null);
	}

	@Override
	public String[] list(final String dir, final String pattern) {
		final File directory = new File(absoluteRootDirectory + dir);
		if (directory.exists()) {
			final FilenameFilter filenameFilter = PatternFilenameFilter.build(pattern);
			return directory.list(filenameFilter);
		} else {
			return new String[0];
		}
	}

	@Override
	// TODO remove duplication
	public File[] listFiles(final String dir, final String pattern) {
		final File directory = new File(absoluteRootDirectory + dir);
		if (directory.exists()) {
			final FilenameFilter filenameFilter = PatternFilenameFilter.build(pattern);
			return directory.listFiles(filenameFilter);
		} else {
			return new File[0];
		}
	}

	@Override
	public void remove(final String filePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rename(final String filePath, String newFilePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(final FileItem file, final String filePath) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(final InputStream inputStream, final String filePath) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRelativeRootDirectory() {
		return relativeRootDirectory;
	}

	@Override
	public String getAbsoluteRootDirectory() {
		return absoluteRootDirectory;
	}

	@Override
	public File getFile(final String path) {
		final File file = new File(path);
		if (file.exists()) {
			return file;
		} else {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
	}

	@Override
	public boolean isImage(final FileItem file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getExtension(final String fileName) {
		throw new UnsupportedOperationException();

	}

	@Override
	public String removeExtension(final String fileName) {
		throw new UnsupportedOperationException();
	}

}
