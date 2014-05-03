package utils;

import static org.hamcrest.Matchers.containsString;

import org.hamcrest.Matcher;

public abstract class JsonMatchers {

	public static Matcher<String> containsPair(final String key, final Object value) {
		String valueString;
		if (value == null) {
			valueString = "null";
		} else if (value instanceof String) {
			valueString = String.format("\"%s\"", value);
		} else {
			valueString = value.toString();
		}
		return containsString(String.format("\"%s\":%s", key, valueString));
	}

	public static Matcher<String> containsKey(final String key) {
		return containsString(String.format("\"%s\":", key));
	}

	public static Matcher<String> containsArrayWithKey(final String array, final String key) {
		return containsString(String.format("\"%s\":%s", key, array));
	}

	public static Matcher<String> containsObjectWithKey(final String object, final String key) {
		return containsString(String.format("\"%s\":%s", key, object));
	}
}
