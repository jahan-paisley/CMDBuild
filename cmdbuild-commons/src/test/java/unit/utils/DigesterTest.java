package unit.utils;

import static org.junit.Assert.assertEquals;

import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.common.digest.Sha1Digester;
import org.junit.Before;
import org.junit.Test;

public class DigesterTest {

	private Digester reversibleDigester;
	private Digester irreversibleDigester;
	private static final String PLAIN_TEXT = "Test";

	@Before
	public void setUp() {
		reversibleDigester = new Base64Digester();
		irreversibleDigester = new Sha1Digester();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void irreversibleDigesterShouldThrowUnsupportedExceptionOnDecrypt() {
		irreversibleDigester.decrypt(PLAIN_TEXT);
	}

	@Test
	public void reversibleDigesterShouldDecryptCorrectly() {
		final String encryptedText = reversibleDigester.encrypt(PLAIN_TEXT);
		assertEquals(PLAIN_TEXT, reversibleDigester.decrypt(encryptedText));
	}

}
