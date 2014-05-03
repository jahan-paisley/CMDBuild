package org.cmdbuild.utils;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.logger.Log;

/**
 * @deprecated use Apache Commons instead.
 */
@Deprecated
public class FileUtils {

	/**
	 * @deprecated use Apache Commons instead.
	 */
	@Deprecated
	public static String getContents(final String file) {
		final File aFile = new File(file);
		try {
			return org.apache.commons.io.FileUtils.readFileToString(aFile);
		} catch (final IOException e) {
			Log.CMDBUILD.error(format("error reading file '%s' content", file), e);
			return StringUtils.EMPTY;
		}
	}

}
