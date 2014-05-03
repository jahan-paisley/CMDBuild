package unit.logic.mapping.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.cmdbuild.logic.validation.Validator.ValidationError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonSorterMapperTest {

	private static final String CLASS_NAME = "mockedClassName";
	private CMClass mockedClass;

	@Before
	public void setUp() {
		mockedClass = mock(CMClass.class);
		when(mockedClass.getName()).thenReturn(CLASS_NAME);
	}

	@Test
	public void shouldReturnEmptyListIfNullSortersArgument() throws Exception {
		// given
		final SorterMapper mapper = new JsonSorterMapper(mockedClass, null);

		// when
		final List<OrderByClause> orderByClauses = mapper.deserialize();

		// then
		assertTrue(orderByClauses.isEmpty());
	}

	@Test
	public void shouldReturnEmptyListIfEmptySortersArray() throws Exception {
		// given
		final SorterMapper mapper = new JsonSorterMapper(mockedClass, new JSONArray());

		// when
		final List<OrderByClause> orderByClauses = mapper.deserialize();

		// then
		assertTrue(orderByClauses.isEmpty());
	}

	@Test(expected = ValidationError.class)
	public void shouldThrowExceptionIfNotValidKeysInSortersArray() throws Exception {
		// given
		final JSONArray sorters = new JSONArray();
		sorters.put(new JSONObject("{property: attr1, not_existent_key: ASC}"));
		sorters.put(new JSONObject("{property: attr2, direction: DESC}"));
		final SorterMapper mapper = new JsonSorterMapper(mockedClass, sorters);

		// when
		mapper.deserialize();
	}

	@Test(expected = ValidationError.class)
	public void shouldThrowExceptionIfNotValidValuesInSortersArray() throws Exception {
		// given
		final JSONArray sorters = new JSONArray();
		sorters.put(new JSONObject("{property: attr1, direction: NOT_VALID_VALUE}"));
		sorters.put(new JSONObject("{property: attr2, direction: DESC}"));
		final SorterMapper mapper = new JsonSorterMapper(mockedClass, sorters);

		// when
		mapper.deserialize();
	}

	@Test
	public void shouldSuccessfullyDeserializeNotEmptySortersArray() throws Exception {
		// given
		final JSONArray sorters = new JSONArray();
		sorters.put(new JSONObject("{property: attr1, direction: ASC}"));
		sorters.put(new JSONObject("{property: attr2, direction: DESC}"));
		final SorterMapper mapper = new JsonSorterMapper(mockedClass, sorters);

		// when
		final List<OrderByClause> orderByClauses = mapper.deserialize();

		// then
		assertEquals(orderByClauses.size(), 2);
	}

}
