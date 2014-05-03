package unit.services.soap.utils;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.services.soap.types.Filter;
import org.cmdbuild.services.soap.types.Query;
import org.cmdbuild.services.soap.utils.SoapToJsonUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SoapToJsonUtilsTest {

	private static final String LOOKUP_ATTRIBUTE_NAME = "LookupAttribute";
	private static final String LOOKUP_TYPE = "foo";

	private static final String EQUALS_OPERATOR = "EQUALS";

	private CMAttribute attribute;
	private CMClass targetClass;
	private LookupStore lookupStore;

	@Before
	public void setUp() throws Exception {
		attribute = mock(CMAttribute.class);
		when(attribute.getType()) //
				.thenReturn((CMAttributeType) new LookupAttributeType(LOOKUP_TYPE));

		targetClass = mock(CMClass.class);
		when(targetClass.getAttribute(LOOKUP_ATTRIBUTE_NAME)) //
				.thenReturn(attribute);

		lookupStore = mock(LookupStore.class);
	}

	@Test
	public void lookupValueHandledAsId() throws Exception {
		// given
		final Query query = queryForLookupAttribute(LOOKUP_ATTRIBUTE_NAME, "42");

		// when
		final JSONObject filter = SoapToJsonUtils.createJsonFilterFrom(query, null, null, targetClass, lookupStore);

		// then
		assertThat(
				filter.toString(),
				equalTo(jsonOf(
						"{attribute: {simple: {attribute: \"LookupAttribute\", operator: \"equal\", value: [\"42\"]}}}")
						.toString()));
	}

	@Test
	public void lookupValueHandledAsDescription() throws Exception {
		// given
		final Query query = queryForLookupAttribute(LOOKUP_ATTRIBUTE_NAME,
				"Answer to the Ultimate Question of Life, the Universe, and Everything");
		when(lookupStore.listForType(LookupType.newInstance() //
				.withName(LOOKUP_TYPE) //
				.build())) //
				.thenReturn(asList( //
						lookup(24L, "bar"), //
						lookup(42L, "Answer to the Ultimate Question of Life, the Universe, and Everything") //
						));

		// when
		final JSONObject filter = SoapToJsonUtils.createJsonFilterFrom(query, null, null, targetClass, lookupStore);

		// then
		assertThat(
				filter.toString(),
				equalTo(jsonOf(
						"{attribute: {simple: {attribute: \"LookupAttribute\", operator: \"equal\", value: [\"42\"]}}}")
						.toString()));
	}

	@Test
	public void lookupValueNotFound() throws Exception {
		// given
		final Query query = queryForLookupAttribute(LOOKUP_ATTRIBUTE_NAME,
				"Answer to the Ultimate Question of Life, the Universe, and Everything");
		when(lookupStore.listForType(LookupType.newInstance() //
				.withName(LOOKUP_TYPE) //
				.build())) //
				.thenReturn(asList(lookup(24L, "bar")));

		// when
		final JSONObject filter = SoapToJsonUtils.createJsonFilterFrom(query, null, null, targetClass, lookupStore);

		// then
		assertThat(
				filter.toString(),
				equalTo(jsonOf(
						"{attribute: {simple: {attribute: \"LookupAttribute\", operator: \"equal\", value: [\"0\"]}}}")
						.toString()));
	}

	private static Query queryForLookupAttribute(final String name, final String value) {
		final Query query = new Query();
		query.setFilter(new Filter() {
			{
				setName(name);
				setOperator(EQUALS_OPERATOR);
				setValue(asList(value));
			}
		});
		return query;
	}

	private static Lookup lookup(final Long id, final String description) {
		return Lookup.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.build();
	}

	private static JSONObject jsonOf(final String source) throws Exception {
		return new JSONObject(source);
	}

}
