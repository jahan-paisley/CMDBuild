package util;

public class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static String clean(final String sql) {
		return sql //
				.replace("\n", " ") //
				.replaceAll("[ ]+", " ") //
		;
	}

}
