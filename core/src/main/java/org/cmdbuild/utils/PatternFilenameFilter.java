package org.cmdbuild.utils;

import java.io.File;
import java.io.FilenameFilter;

public class PatternFilenameFilter implements FilenameFilter {

	final String pattern;

	private PatternFilenameFilter(final String pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(final File dir, final String name) {
		return name.matches(pattern);
	}

	public static final FilenameFilter build(final String pattern) {
		FilenameFilter filter = null;
		if (pattern != null) {
			filter = new PatternFilenameFilter(pattern);
		}
		return filter;
	}
}
