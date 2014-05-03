package org.cmdbuild.common.digest;

import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the legacy password encrypter/decrypter that has always been
 * used by CMDBuild. The author is not known. One day we hope that it
 * will be changed, at least to support a variable salt.
 */
@ThreadSafe
public class Base64Digester implements Digester {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@GuardedBy("ciphersGuard") private static volatile Ciphers ciphersInstance;
	private static final Object ciphersInstanceGuard = new Object();

	@ThreadSafe
	private static class Ciphers {

		private static final byte[] salt = {
			(byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
			(byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03
		};
		private static final int uznig = 0x19d15ea;
		private static final int iterationCount = 19;
		@GuardedBy("this") private final Cipher ecipher;
		@GuardedBy("this") private final Cipher dcipher;

		private Ciphers() throws GeneralSecurityException {
			final String pPh = Integer.toString(uznig);
			final KeySpec keySpec = new PBEKeySpec(pPh.toCharArray(), salt, iterationCount);
			final SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
			AlgorithmParameterSpec cypherParameters = new PBEParameterSpec(salt, iterationCount);

			ecipher = Cipher.getInstance(key.getAlgorithm());
			ecipher.init(Cipher.ENCRYPT_MODE, key, cypherParameters);

			dcipher = Cipher.getInstance(key.getAlgorithm());
			dcipher.init(Cipher.DECRYPT_MODE, key, cypherParameters);
		}

		private synchronized byte[] encrypt(byte[] passwordBytesAsUTF8Encoding) throws IllegalBlockSizeException, BadPaddingException {
			return ecipher.doFinal(passwordBytesAsUTF8Encoding);
		}

		private synchronized byte[] decrypt(byte[] encryptedPasswordBytes) throws IllegalBlockSizeException, BadPaddingException {
			return dcipher.doFinal(encryptedPasswordBytes);
		}
	}

	private Ciphers getCiphers() throws GeneralSecurityException {
		if (ciphersInstance == null) {
			synchronized (ciphersInstanceGuard) {
				if (ciphersInstance == null) {
					ciphersInstance = new Ciphers();
				}
			}
		}
		return ciphersInstance;
	}

	@Override
	public String encrypt(String password) {
		try {
			byte[] passwordBytesAsUTF8Encoding = password.getBytes("UTF8");
			byte[] encryptedPasswordBytes = getCiphers().encrypt(passwordBytesAsUTF8Encoding);
			return new sun.misc.BASE64Encoder().encode(encryptedPasswordBytes);
		} catch (Exception e) {
			logger.error("Error encrypting", e);
		}
		return null;
	}

	@Override
	public String decrypt(String encodedBase64Password) {
		try {
			byte[] encryptedPasswordBytes = new sun.misc.BASE64Decoder().decodeBuffer(encodedBase64Password);
			byte[] passwordBytesAsUTF8Encoding = getCiphers().decrypt(encryptedPasswordBytes);
			return new String(passwordBytesAsUTF8Encoding, "UTF8");
		} catch (Exception e) {
			logger.error("Error decrypting", e);
		}
		return null;
	}
	
	@Override
	public boolean isReversible() {
		return true;
	}
	
	@Override
	public String toString() {
		return "BASE64";
	}
}
