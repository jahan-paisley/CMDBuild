package org.cmdbuild.common.digest;

import org.apache.commons.codec.digest.DigestUtils;

public class Md5Digester implements Digester {

	@Override
	public String encrypt(String plainText) {
		return DigestUtils.md5Hex(plainText);
	}

	@Override
	public String decrypt(String cipherText) {
		throw new UnsupportedOperationException("The MD5 algorithm does not support the decrypt operation");
	}

	@Override
	public boolean isReversible() {
		return false;
	}
	
	@Override
	public String toString() {
		return "MD5";
	}

}
