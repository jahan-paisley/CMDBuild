package org.cmdbuild.dms.alfresco.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.exception.DmsError;

class AlfrescoFtpClient implements FtpClient, LoggingSupport {

	private final DmsConfiguration configuration;

	public AlfrescoFtpClient(final DmsConfiguration configuration) {
		this.configuration = configuration;
	}

	private static FTPClient createFtpClient() {
		logger.info("creating ftp client");
		final FTPClient ftpClient = new FTPClient();
		ftpClient.setAutodetectUTF8(true);
		return ftpClient;
	}

	@Override
	public void mkdir(final List<String> path) throws DmsError {
		final FTPClient ftp = createFtpClient();
		try {
			connect(ftp, configuration.getFtpHost(), configuration.getFtpPort());
			login(ftp, configuration.getAlfrescoUser(), configuration.getAlfrescoPassword());
			changeDirectory(ftp, configuration.getRepositoryFSPath());
			changeDirectory(ftp, path, true);
			logout(ftp);
		} finally {
			disconnect(ftp);
		}
	}

	@Override
	public void delete(final String filename, final List<String> path) throws DmsError {
		final FTPClient ftp = createFtpClient();
		try {
			connect(ftp, configuration.getFtpHost(), configuration.getFtpPort());
			login(ftp, configuration.getAlfrescoUser(), configuration.getAlfrescoPassword());
			changeDirectory(ftp, configuration.getRepositoryFSPath());
			changeDirectory(ftp, path, false);
			delete(ftp, filename);
			logout(ftp);
		} finally {
			disconnect(ftp);
		}
	}

	@Override
	public DataHandler download(final String filename, final List<String> path) throws DmsError {
		final FTPClient ftp = createFtpClient();
		try {
			connect(ftp, configuration.getFtpHost(), configuration.getFtpPort());
			login(ftp, configuration.getAlfrescoUser(), configuration.getAlfrescoPassword());
			changeDirectory(ftp, configuration.getRepositoryFSPath());
			changeDirectory(ftp, path, false);
			final DataHandler dataHandler = download(ftp, filename);
			logout(ftp);
			return dataHandler;
		} finally {
			disconnect(ftp);
		}
	}

	@Override
	public void upload(final String filename, final InputStream is, final List<String> path) throws DmsError {
		final FTPClient ftp = createFtpClient();
		try {
			connect(ftp, configuration.getFtpHost(), configuration.getFtpPort());
			login(ftp, configuration.getAlfrescoUser(), configuration.getAlfrescoPassword());
			changeDirectory(ftp, configuration.getRepositoryFSPath());
			changeDirectory(ftp, path, true);
			upload(ftp, filename, is);
			logout(ftp);
		} finally {
			disconnect(ftp);
		}
	}

	private void connect(final FTPClient ftpClient, final String host, final String port) throws DmsError {
		try {
			logger.info("connecting to '{}:{}'", host, port);
			ftpClient.connect(host, Integer.parseInt(port));
			final int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				throw DmsError.ftpConnectionError(host, port);
			}
		} catch (final Exception e) {
			throw DmsError.ftpConnectionError(host, port);
		}
	}

	private void disconnect(final FTPClient ftpClient) {
		try {
			logger.info("disconnecting");
			ftpClient.disconnect();
		} catch (final IOException e) {
			logger.warn("error disconnecting", e);
		}
	}

	private void login(final FTPClient ftpClient, final String username, final String password) throws DmsError {
		try {
			logger.info("logging in with username '{}'", username);
			logger.debug("... and password '{}'", password);
			if (!ftpClient.login(username, password)) {
				throw DmsError.fptLoginError(username, password);
			}
		} catch (final IOException e) {
			throw DmsError.fptLoginError(username, password);
		}
	}

	private void logout(final FTPClient ftpClient) {
		try {
			logger.info("logging out");
			ftpClient.logout();
		} catch (final IOException e) {
			logger.warn("error logging out", e);
		}
	}

	private void changeDirectory(final FTPClient ftpClient, final List<String> dirs, final boolean create)
			throws DmsError {
		logger.info("changing directory to '{}'", dirs);
		for (final String dir : dirs) {
			try {
				changeDirectory(ftpClient, dir);
			} catch (final DmsError e) {
				logger.warn("error changing directory", e);
				if (create) {
					makeDirectory(ftpClient, dir);
					changeDirectory(ftpClient, dir);
				} else {
					throw e;
				}
			}
		}
	}

	private void changeDirectory(final FTPClient ftpClient, final String dir) throws DmsError {
		try {
			logger.info("changing directory to '{}'", dir);
			if (!ftpClient.changeWorkingDirectory(dir)) {
				final String message = String.format("error changing working directory to '%s'", dir);
				throw DmsError.ftpOperationError(message);
			}
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	private void makeDirectory(final FTPClient ftpClient, final String dir) throws DmsError {
		try {
			logger.info("creating directory '{}'", dir);
			if (!ftpClient.makeDirectory(dir)) {
				final String message = String.format("error creating directory '%s'", dir);
				throw DmsError.ftpOperationError(message);
			}
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	private void delete(final FTPClient ftpClient, final String filename) throws DmsError {
		try {
			logger.info("deleting file '{}'", filename);
			if (!ftpClient.deleteFile(filename)) {
				throw DmsError.ftpDeleteError(filename);
			}
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	private DataHandler download(final FTPClient ftpClient, final String filename) throws DmsError {
		/*
		 * this could be ugly: download the file from the ftp then return the
		 * inputstream, based on the retrieved file. i'd like to use the
		 * ftp.retrieveStreamFile, but i don't see an easy way to return the
		 * inputstream, return it to the user via a download action, and then
		 * close the stream and the ftp connection.
		 */
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			final DataSource dataSource = TempDataSource.create(filename);
			final OutputStream os = dataSource.getOutputStream();
			if (ftpClient.retrieveFile(filename, os)) {
				os.flush();
				os.close();
				final DataHandler attachment = new DataHandler(dataSource);
				return attachment;
			}

			/*
			 * WHAT CAN BE DONE:: create a tmp file in local filesystem, the
			 * return the input stream from THAT file.
			 */

			throw DmsError.ftpDownloadError(filename);
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	private void upload(final FTPClient ftpClient, final String filename, final InputStream is) throws DmsError {
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			if (!ftpClient.storeFile(filename, is)) {
				throw DmsError.ftpUploadError(filename);
			}
		} catch (final Exception e) {
			throw DmsError.forward(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final Exception e) {
					logger.warn("error closing input stream", e);
				}
			}
		}
	}

}
