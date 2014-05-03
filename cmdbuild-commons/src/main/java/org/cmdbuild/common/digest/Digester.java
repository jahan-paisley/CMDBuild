package org.cmdbuild.common.digest;

/**
 * Strategy pattern
 */
public interface Digester {
	
	public String encrypt(String plainText);
	
	public String decrypt(String cipherText);
	
	public boolean isReversible();

}
