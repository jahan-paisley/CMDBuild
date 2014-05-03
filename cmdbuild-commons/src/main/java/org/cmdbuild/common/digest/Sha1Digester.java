package org.cmdbuild.common.digest;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1Digester implements Digester {

	@Override
	public String encrypt(String plainText) {
		return DigestUtils.shaHex(plainText);
	}

	@Override
	public String decrypt(String cipherText) {
		throw new UnsupportedOperationException("The SHA-1 algorithm does not support the decrypt operation");
	}

	@Override
	public boolean isReversible() {
		return false;
	}
	
	@Override
	public String toString() {
		return "SHA1";
	}

}
