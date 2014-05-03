package utils;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.dms.DmsConfiguration;

public class TestConfiguration implements DmsConfiguration {

	private static final String FTP_HOST = "localhost";
	private static final String FTP_PORT = "2121";
	private static final String FTP_BASE_PATH = "/Alfresco/User Homes/cmdbuild";

	private static final String WS_URL = "http://localhost:10080/alfresco/api";
	private static final String WS_PATH = "/app:company_home/app:user_homes/";
	private static final String REPOSITORY = "cm:cmdbuild";

	private static final String USERNAME = "admin";
	private static final String PASSWORD = "admin";

	private static final String ALFRESCO_CATEGORY = "AlfrescoCategory";
	private static final String LOOKUP_NAME_FOR_ATTACHMENTS = "Attachment";

	private static final String ALFRESCO_CUSTOM_URI = "org.cmdbuild.dms.alfresco";
	private static final String ALFRESCO_CUSTOM_PREFIX = "cmdbuild";
	private static final String ALFRESCO_CUSTOM_MODEL_FILENAME = "cmdbuildCustomModel.xml";
	private static final String METADATA_AUTOCOMPLETION_FILENAME = "metadataAutocompletion.xml";

	private static final long DELAY = 1000L;

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getFtpHost() {
		return FTP_HOST;
	}

	@Override
	public String getFtpPort() {
		return FTP_PORT;
	}

	@Override
	public String getRepositoryFSPath() {
		return FTP_BASE_PATH;
	}

	@Override
	public String getServerURL() {
		return WS_URL;
	}

	@Override
	public String getRepositoryWSPath() {
		return WS_PATH;
	}

	@Override
	public String getRepositoryApp() {
		return REPOSITORY;
	}

	@Override
	public String getCmdbuildCategory() {
		return ALFRESCO_CATEGORY;
	}

	@Override
	public String getLookupNameForAttachments() {
		return LOOKUP_NAME_FOR_ATTACHMENTS;
	}

	@Override
	public String getAlfrescoUser() {
		return USERNAME;
	}

	@Override
	public String getAlfrescoPassword() {
		return PASSWORD;
	}

	@Override
	public String getAlfrescoCustomUri() {
		return ALFRESCO_CUSTOM_URI;
	}

	@Override
	public String getAlfrescoCustomPrefix() {
		return ALFRESCO_CUSTOM_PREFIX;
	}

	@Override
	public String getAlfrescoCustomModelFileName() {
		return ALFRESCO_CUSTOM_MODEL_FILENAME;
	}

	@Override
	public String getAlfrescoCustomModelFileContent() {
		return contentOf(getAlfrescoCustomModelFileName());
	}

	@Override
	public String getMetadataAutocompletionFileName() {
		return METADATA_AUTOCOMPLETION_FILENAME;
	}

	@Override
	public String getMetadataAutocompletionFileContent() {
		return contentOf(getMetadataAutocompletionFileName());
	}

	private String contentOf(final String filename) {
		try {
			final File file = new File(ClassLoader.getSystemResource(filename).toURI());
			if (file.exists()) {
				return FileUtils.readFileToString(file);
			} else {
				return EMPTY;
			}
		} catch (final Exception e) {
			return EMPTY;
		}
	};

	@Override
	public long getDelayBetweenFtpAndWebserviceOperations() {
		return DELAY;
	}

}
