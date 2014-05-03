package org.cmdbuild.common.digest;

import java.security.NoSuchAlgorithmException;


public class DigesterFactory {
	
	public static Digester createDigester(String digestAlgorithm) throws NoSuchAlgorithmException {
		if (digestAlgorithm.equalsIgnoreCase("SHA1")) {
			return new Sha1Digester();
		} else if (digestAlgorithm.equalsIgnoreCase("MD5")) {
			return new Md5Digester();
		} else if (digestAlgorithm.equalsIgnoreCase("BASE64")) {
			return new Base64Digester();
		}
		throw new NoSuchAlgorithmException("Not existent digest algorithm: available algorithms are SHA1, MD5, BASE64");
	}
	
}
