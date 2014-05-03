package utils;

import java.util.UUID;

public class XpdlTestUtils {

	private XpdlTestUtils() {
		// prevents instantiation
	}

	/**
	 * Returns a random name (UUID-like).
	 * 
	 * @see {@link UUID}
	 */
	public static final String randomName() {
		return UUID.randomUUID().toString();
	}

}
