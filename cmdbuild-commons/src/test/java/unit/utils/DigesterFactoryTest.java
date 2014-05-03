package unit.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.security.NoSuchAlgorithmException;

import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.common.digest.DigesterFactory;
import org.junit.Test;

public class DigesterFactoryTest {

	private static final String NOT_VALID_DIGESTER = "NOT_VALID_DIGESTER";
	private static final String SHA1_CASE_INSENSITIVE = "ShA1";
	private static final String MD5_CASE_INSENSITIVE = "md5";

	@Test(expected = NoSuchAlgorithmException.class)
	public void createDigestShouldThrowExceptionIfNoValidAlgorithm() throws NoSuchAlgorithmException {
		DigesterFactory.createDigester(NOT_VALID_DIGESTER);
	}

	@Test
	public void shouldCreateDigestIgnoreCaseSensitivity() {
		try {
			final Digester sha1Digester = DigesterFactory.createDigester(SHA1_CASE_INSENSITIVE);
			final Digester md5Digester = DigesterFactory.createDigester(MD5_CASE_INSENSITIVE);
			assertNotNull(sha1Digester);
			assertNotNull(md5Digester);
		} catch (final NoSuchAlgorithmException e) {
			fail();
		}
	}

}
