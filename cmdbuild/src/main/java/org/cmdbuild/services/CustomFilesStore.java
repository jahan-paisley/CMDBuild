package org.cmdbuild.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.utils.PatternFilenameFilter;

public class CustomFilesStore implements FilesStore {

	private static final String ps = File.separator;
	private final String relativeRootDirectory = "upload" + ps;
	private final String absoluteRootDirectory = Settings.getInstance().getRootPath() + relativeRootDirectory;

	private static final String[] ALLOWED_IMAGE_TYPES = { "image/png", "image/gif", "image/jpeg", "image/pjpeg",
			"image/x-png" };

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
		final File theFile = new File(absoluteRootDirectory + filePath);
		if (theFile.exists()) {
			theFile.delete();
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	@Override
	public void rename(final String filePath, String newFilePath) {
		final File theFile = new File(absoluteRootDirectory + filePath);
		if (theFile.exists()) {
			final String extension = getExtension(theFile.getName());
			if (!"".equals(extension)) {
				newFilePath = newFilePath + extension;
			}

			final File newFile = newFile(absoluteRootDirectory + newFilePath);
			theFile.renameTo(newFile);
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	@Override
	public void save(final FileItem file, final String filePath) throws IOException {
		save(file.getInputStream(), filePath);
	}

	@Override
	public void save(final InputStream inputStream, final String filePath) throws IOException {
		final String destinationPath = absoluteRootDirectory + filePath;
		FileOutputStream outputStream = null;
		try {
			final File destinationFile = newFile(destinationPath);
			final File dir = destinationFile.getParentFile();
			dir.mkdirs();

			outputStream = new FileOutputStream(destinationFile);
			final byte[] buf = new byte[1024];
			int i = 0;
			while ((i = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, i);
			}
		} catch (final FileNotFoundException e) {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		} catch (final IOException e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	private File newFile(final String destinationPath) {
		final File destinationFile = new File(destinationPath);

		if (destinationFile.exists()) {
			throw ORMExceptionType.ORM_ICONS_FILE_ALREADY_EXISTS.createException(destinationFile.getName());
		}
		return destinationFile;
	}

	@Override
	public String getRelativeRootDirectory() {
		return this.relativeRootDirectory;
	}

	@Override
	public String getAbsoluteRootDirectory() {
		return this.absoluteRootDirectory;
	}

	@Override
	public File getFile(final String path) {
		final File file = new File(path);
		if (file.exists()) {
			return file;
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	@Override
	public boolean isImage(final FileItem file) {
		boolean valid = false;
		for (final String type : ALLOWED_IMAGE_TYPES) {
			if (type.equalsIgnoreCase(file.getContentType())) {
				valid = true;
				break;
			}
		}
		return valid;
	}

	@Override
	public String getExtension(final String fileName) {
		String ext = "";
		final int lastIndexOfPoint = fileName.lastIndexOf(".");
		if (lastIndexOfPoint >= 0) {
			ext = fileName.substring(lastIndexOfPoint);
		}
		return ext;
	}

	@Override
	public String removeExtension(final String fileName) {
		String cleanedFileName = fileName;
		final int lastIndexOfPoint = fileName.lastIndexOf(".");
		if (lastIndexOfPoint >= 0) {
			cleanedFileName = fileName.substring(0, lastIndexOfPoint);
		}
		return cleanedFileName;
	}

}
