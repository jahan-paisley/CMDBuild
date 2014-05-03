package org.cmdbuild.common.mail;

import java.util.Properties;

class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static Properties propertiesPlusSystemOnes(final Properties properties) {
		final Properties p = new Properties(System.getProperties());
		p.putAll(properties);
		return p;
	}

}
