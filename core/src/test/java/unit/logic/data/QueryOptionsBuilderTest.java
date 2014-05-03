package unit.logic.data;

import static org.junit.Assert.assertEquals;

import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class QueryOptionsBuilderTest {

	private static final String EMPTY_OBJECT = "{}";
	private static final String EMPTY_ARRAY = "[]";

	@Test
	public void shouldCreateQueryOptionsWithDefaultValues() {
		// when
		final QueryOptions options = QueryOptions.newQueryOption()//
				.build();

		// then
		assertEquals(Integer.MAX_VALUE, options.getLimit());
		assertEquals(0, options.getOffset());
		assertEquals(EMPTY_OBJECT, options.getFilter().toString());
		assertEquals(EMPTY_ARRAY, options.getSorters().toString());
		assertEquals(EMPTY_ARRAY, options.getAttributes().toString());
	}

	@Test
	public void shouldReturnLimitAndOffsetValuesWhenSet() {
		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.limit(10) //
				.offset(3) //
				.build();

		// then
		assertEquals(10, options.getLimit());
		assertEquals(3, options.getOffset());
		assertEquals(EMPTY_OBJECT, options.getFilter().toString());
		assertEquals(EMPTY_ARRAY, options.getSorters().toString());
		assertEquals(EMPTY_ARRAY, options.getAttributes().toString());
	}

	@Test
	public void shouldReturnSortersWhenSet() throws Exception {
		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.orderBy(new JSONArray("[a, b, 'c,']")) //
				.build();

		// then
		assertEquals(3, options.getSorters().length());
	}

	@Test
	public void shouldReturnEmptyArrayIfSortersIsNull() throws Exception {
		// given
		final JSONArray sorters = null;

		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.orderBy(sorters) //
				.build();

		// then
		assertEquals(EMPTY_ARRAY, options.getSorters().toString());
	}

	@Test
	public void shouldReturnAttributesWhenSet() throws Exception {
		// given
		final JSONArray attributes = new JSONArray();
		attributes.put(new JSONObject("{key1: val1}"));
		attributes.put(new JSONObject("{key2: val2}"));
		attributes.put(new JSONObject("{key3: val3}"));

		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.onlyAttributes(attributes) //
				.build();

		// then
		assertEquals(3, options.getAttributes().length());
	}

	@Test
	public void shouldReturnEmptyArrayIfAttributesIsNull() throws Exception {
		// given
		final JSONArray attributes = null;

		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.onlyAttributes(attributes) //
				.build();

		// then
		assertEquals(EMPTY_ARRAY, options.getAttributes().toString());
	}

}
