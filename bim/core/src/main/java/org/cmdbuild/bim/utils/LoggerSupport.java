package org.cmdbuild.bim.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerSupport {

	public static final Logger logger = LoggerFactory.getLogger("BIM");

	public static final Logger geom_logger = LoggerFactory.getLogger("GEOM");

	public static final Logger test_logger = LoggerFactory.getLogger("TEST");

	private LoggerSupport() {
	};
}
