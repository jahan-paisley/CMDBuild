package org.cmdbuild.common.utils;

public class OS {

	public static boolean isWindows() {
		return (getOsName().toLowerCase().indexOf("windows") >= 0);
	}

	public static String getOsName() {
		return System.getProperty("os.name", "unknown");
	}
}
