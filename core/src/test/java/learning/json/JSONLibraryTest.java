package learning.json;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JSONLibraryTest {

	@Test
	public void shouldDeserializeOnlyKeyValueJsonString() {
		// given
		final String simpleJson = "{start: 0, limit: 20}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(simpleJson);
		} catch (final JSONException e) {
			fail();
		}

		// then
		try {
			assertThat((Integer) object.get("start"), is(equalTo(0)));
			assertThat((Integer) object.get("limit"), is(equalTo(20)));
		} catch (final JSONException e) {
			fail();
		}
	}

	@Test(expected = JSONException.class)
	public void shouldThrowExceptionIfNotExistentKey() throws JSONException {
		// given
		final String simpleJson = "{start: 0, limit: 20}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(simpleJson);
		} catch (final JSONException e) {
			fail();
		}

		// then
		final Object notExistentObject = object.get("not_existent_key");
	}

	@Test
	public void shouldDeserializeJsonStringWithArrayOfValues() {
		// given
		final String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (final JSONException e) {
			fail();
		}

		// then
		try {
			final JSONArray deserializedArray = object.getJSONArray("array");
			final int first = deserializedArray.getInt(0);
			assertEquals(first, 10);
		} catch (final JSONException e) {
			fail();
		}
	}

	@Test
	public void shouldReturnFalseIfTheJsonObjectDoesNotHaveASpecificKey() {
		// given
		final String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (final JSONException e) {
			fail();
		}

		// then
		assertFalse(object.has("not_existent_key"));
	}

	@Test
	public void shouldReturnTrueIfTheJsonObjectContainsASpecificKey() {
		// given
		final String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (final JSONException e) {
			fail();
		}

		// then
		assertTrue(object.has("array"));
		assertTrue(object.has("start"));
	}

	@Test
	public void keysOfJsonObjectAreCaseSensitive() {
		// given
		final String jsonString = "{start: 0, limit: 20, array: [10, 50, 100]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (final JSONException e) {
			fail();
		}

		// then
		assertFalse(object.has("arRAy"));
	}

	@Test
	public void shouldDeserializeJsonStringWithArrayOfObjects() {
		// given
		final String jsonString = "{start: 0, limit: 20, array: [{attribute: 'code', operator: 'equal', value: 5}, "
				+ "{attribute: 'description', operator: 'like', value: 'desc'}]}";

		// when
		JSONObject object = null;
		try {
			object = new JSONObject(jsonString);
		} catch (final JSONException e) {
			fail();
		}

		// then
		try {
			final JSONArray deserializedArray = object.getJSONArray("array");
			final JSONObject firstObject = deserializedArray.getJSONObject(0);
			final String attributeName = firstObject.getString("attribute");
			final String operator = firstObject.getString("operator");
			final Object attributeValue = firstObject.get("value");
			assertEquals(attributeName, "code");
			assertEquals(operator, "equal");
			assertEquals(attributeValue, 5);
		} catch (final JSONException e) {
			fail();
		}
	}

	@Test
	public void shouldQuoteSpecialCharacters() throws Exception {
		// given
		final String date = "16/12/1998";

		// when
		final String quotedDate = JSONObject.quote(date);

		// then
		assertEquals("\"" + "16/12/1998" + "\"", quotedDate);
		final JSONObject dateObjectCreatedSuccessfully = new JSONObject("{key: " + quotedDate + "}");
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionIfUnquotedSpecialChar() throws Exception {
		// given
		final String date = "16/12/1998";

		// when
		new JSONObject("{key: " + date + "}");
	}

}
