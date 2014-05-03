package org.cmdbuild.config;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.Settings;

public class DmsProperties extends DefaultProperties implements DmsConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "dms";

	private static final String ENABLED = "enabled";
	private static final String SERVER_URL = "server.url";
	private static final String FILE_SERVER_PORT = "fileserver.port";
	@Legacy("not used now")
	private static final String FILE_SERVER_TYPE = "fileserver.type";
	private static final String FILE_SERVER_URL = "fileserver.url";
	/*
	 * wspath is the path for the base space, fspath is the same thing, in terms
	 * of directories
	 */
	private static final String REPOSITORY_FS_PATH = "repository.fspath";
	private static final String REPOSITORY_WS_PATH = "repository.wspath";
	private static final String REPOSITORY_APP = "repository.app";
	private static final String PASSWORD = "credential.password";
	private static final String USER = "credential.user";
	private static final String CATEGORY_LOOKUP = "category.lookup";
	private static final String CATEGORY_LOOKUP_ATTACHMENTS = "category.lookup.attachments";
	private static final String DELAY = "delay";
	private static final String ALFRESCO_CUSTOM_URI = "alfresco.custom.uri";
	private static final String ALFRESCO_CUSTOM_PREFIX = "alfresco.custom.prefix";
	private static final String ALFRESCO_CUSTOM_MODEL_FILENAME = "alfresco.custom.model.filename";
	private static final String METADATA_AUTOCOMPLETION_FILENAME = "metadata.autocompletion.filename";

	public interface Default {

		String ENABLED = Boolean.FALSE.toString();
		String SERVER_URL = "http://localhost:8181/alfresco/api";
		String FILE_SERVER_PORT = "1121";
		String FILE_SERVER_URL = "localhost";
		String FILE_SERVER_TYPE = "AlfrescoFTP";
		String REPOSITORY_FS_PATH = "/Alfresco/User Homes/cmdbuild";
		String REPOSITORY_WS_PATH = "/app:company_home/app:user_homes/";
		String REPOSITORY_APP = "cm:cmdbuild";
		String USER = "admin";
		String PASSWORD = "admin";
		String CATEGORY_LOOKUP = "AlfrescoCategory";
		String CATEGORY_LOOKUP_ATTACHMENTS = "Attachment";
		String DELAY = "1000";
		String ALFRESCO_CUSTOM_URI = "org.cmdbuild.dms.alfresco";
		String ALFRESCO_CUSTOM_PREFIX = "cmdbuild";
		String ALFRESCO_CUSTOM_MODULE_FILE_NAME = "cmdbuildCustomModel.xml";
		String METADATA_AUTOCOMPLETION_FILENAME = "metadataAutocompletion.xml";

	}

	public DmsProperties() {
		super();
		setProperty(ENABLED, Default.ENABLED);
		setProperty(SERVER_URL, Default.SERVER_URL);
		setProperty(FILE_SERVER_PORT, Default.FILE_SERVER_PORT);
		setProperty(FILE_SERVER_URL, Default.FILE_SERVER_URL);
		setProperty(FILE_SERVER_TYPE, Default.FILE_SERVER_TYPE);
		setProperty(REPOSITORY_FS_PATH, Default.REPOSITORY_FS_PATH);
		setProperty(REPOSITORY_WS_PATH, Default.REPOSITORY_WS_PATH);
		setProperty(REPOSITORY_APP, Default.REPOSITORY_APP);
		setProperty(PASSWORD, Default.PASSWORD);
		setProperty(USER, Default.USER);
		setProperty(CATEGORY_LOOKUP, Default.CATEGORY_LOOKUP);
		setProperty(CATEGORY_LOOKUP_ATTACHMENTS, Default.CATEGORY_LOOKUP_ATTACHMENTS);
		setProperty(DELAY, Default.DELAY);
		setProperty(ALFRESCO_CUSTOM_URI, Default.ALFRESCO_CUSTOM_URI);
		setProperty(ALFRESCO_CUSTOM_PREFIX, Default.ALFRESCO_CUSTOM_PREFIX);
		setProperty(ALFRESCO_CUSTOM_MODEL_FILENAME, Default.ALFRESCO_CUSTOM_MODULE_FILE_NAME);
		setProperty(METADATA_AUTOCOMPLETION_FILENAME, Default.METADATA_AUTOCOMPLETION_FILENAME);
	}

	public static DmsProperties getInstance() {
		return (DmsProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		final String enabled = getProperty(ENABLED, "false");
		return enabled.equals("true");
	}

	@Override
	public String getServerURL() {
		return getProperty(SERVER_URL);
	}

	public void setServerURL(final String url) {
		setProperty(SERVER_URL, url);
	}

	@Override
	public String getFtpPort() {
		return getProperty(FILE_SERVER_PORT);
	}

	public void setFtpPort(final String port) {
		setProperty(FILE_SERVER_PORT, port);
	}

	@Override
	public String getFtpHost() {
		return getProperty(FILE_SERVER_URL);
	}

	public void setFtpHost(final String hostname) {
		setProperty(FILE_SERVER_URL, hostname);
	}

	@Override
	public String getAlfrescoUser() {
		return getProperty(USER);
	}

	public void setAlfrescoUser(final String username) {
		setProperty(USER, username);
	}

	@Override
	public String getAlfrescoPassword() {
		return getProperty(PASSWORD);
	}

	public void setAlfrescoPassword(final String password) {
		setProperty(PASSWORD, password);
	}

	@Override
	public String getCmdbuildCategory() {
		return getProperty(CATEGORY_LOOKUP);
	}

	public void setCmdbuildCategory(final String category) {
		setProperty(CATEGORY_LOOKUP, category);
	}

	@Override
	public String getLookupNameForAttachments() {
		return getProperty(CATEGORY_LOOKUP_ATTACHMENTS);
	}

	public void setLookupNameForAttachments(final String name) {
		setProperty(CATEGORY_LOOKUP_ATTACHMENTS, name);
	}

	@Override
	public String getRepositoryFSPath() {
		return getProperty(REPOSITORY_FS_PATH);
	}

	public void setRepositoryFSPath(final String repository) {
		setProperty(REPOSITORY_FS_PATH, repository);
	}

	@Override
	public String getRepositoryWSPath() {
		return getProperty(REPOSITORY_WS_PATH);
	}

	public void setRepositoryWSPath(final String repository) {
		setProperty(REPOSITORY_WS_PATH, repository);
	}

	@Override
	public String getRepositoryApp() {
		return getProperty(REPOSITORY_APP);
	}

	public void setRepositoryApp(final String repository) {
		setProperty(REPOSITORY_APP, repository);
	}

	@Override
	public String getAlfrescoCustomUri() {
		return getProperty(ALFRESCO_CUSTOM_URI);
	}

	@Override
	public String getAlfrescoCustomPrefix() {
		return getProperty(ALFRESCO_CUSTOM_PREFIX);
	}

	@Override
	public String getAlfrescoCustomModelFileName() {
		return getProperty(ALFRESCO_CUSTOM_MODEL_FILENAME);
	}

	@Override
	public String getAlfrescoCustomModelFileContent() {
		return contentOf(getAlfrescoCustomModelFileName());
	}

	@Override
	public String getMetadataAutocompletionFileName() {
		return getProperty(METADATA_AUTOCOMPLETION_FILENAME);
	}

	@Override
	public String getMetadataAutocompletionFileContent() {
		return contentOf(getMetadataAutocompletionFileName());
	}

	private String contentOf(final String filename) {
		final File configurationPath = getPath();
		final File file = new File(configurationPath, filename);
		if (file.exists()) {
			try {
				final String content = FileUtils.readFileToString(file);
				return content;
			} catch (final IOException e) {
				final String message = format("error reading file '%s'", file);
				Log.DMS.error(message, e);
				return EMPTY;
			}
		} else {
			return EMPTY;
		}
	}

	@Override
	public long getDelayBetweenFtpAndWebserviceOperations() {
		return Long.valueOf(getProperty(DELAY));
	}

}
